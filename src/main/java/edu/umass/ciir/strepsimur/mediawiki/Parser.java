package edu.umass.ciir.strepsimur.mediawiki;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

public class Parser {

	private static String emitLink(String href, String label) {
		if (label.trim().isEmpty()) {
			label = href;
		} else if (href.trim().isEmpty()) {
			href = label;
		}
		return "<a href=\"" + href + "\">" + label + "</a>";
	}

	private static String convertInternalLink(String input) {
		String path = input;
		String label = input;

		int vbar = input.indexOf('|');
		if (vbar > 0) {
			path = input.substring(0, vbar);
			label = input.substring(vbar + 1);
		}

		String href = path;
		try {
			href = "http://en.wikipedia.org/wiki/" + URLEncoder.encode(path, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException("UTF-8 should be supported or die.", ex);
		}

		return emitLink(href, label);
	}

	private static String convertExternalLink(String input) {
		int space = input.indexOf(' ');
		String label = input;
		String href = input;
		if (space > 0) {
			label = input.substring(space + 1);
			href = input.substring(0, space);
		}
		return emitLink(href, label);
	}

	private static String convertParameter(String input) {
		System.out.println("convertParameter: " + input);
		return input;
	}

	public static Set<String> ignore = new HashSet<String>();
	static {
		ignore.add("pp-protected");
		ignore.add("lang");
		ignore.add("pp-move-indef");
		ignore.add("use dmy dates");
	}
	
	private static String convertTemplate(String input) {
		String[] oper = input.split("\\|");
		String cmd = oper[0].trim().toLowerCase();

		if(ignore.contains(cmd))
			return "";
		
		if("main".equals(cmd) || cmd.startsWith("cite")) {
			StringBuilder sb = new StringBuilder();
			for(String arg: oper) {
				sb.append(arg).append(' ');
			}
			return sb.toString();
		}

		return input;
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {
		String path = "anarchism.wikixml";
		BufferedReader br = new BufferedReader(new FileReader(path));

		StringBuilder currentPage = new StringBuilder();
		String currentTitle = null;
		boolean inPage = false;

		while (true) {
			String line = br.readLine();
			if (line == null) {
				break;
			}

			// start page event
			if (line.contains("<page>")) {
				assert (line.trim().equals("<page>"));
				currentPage.append(line);
				currentPage.append('\n');
				inPage = true;
				continue;
			}

			// page or end page event
			if (inPage) {
				currentPage.append(line);
				currentPage.append('\n');
				if (line.contains("<title>")) {
					assert (line.contains("</title>"));
					int start = line.indexOf("<title>") + "<title>".length();
					int end = line.indexOf("</title>");

					currentTitle = line.substring(start, end);
				} else if (line.contains("</page>")) {
					assert (line.trim().equals("</page>"));
					inPage = false;
					processPage(currentTitle, currentPage.toString());

					currentTitle = null;
					currentPage = new StringBuilder(); // clear and reset
				}
			}
		}

		String leftovers = currentPage.toString();
		if (!leftovers.isEmpty()) {
			processPage(currentTitle, leftovers);
		}

	}

	private static final XMLInputFactory XML = XMLInputFactory.newInstance();
	public static final String XMLVersion = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";

	public static String getText(String inputXML) throws XMLStreamException {
		XMLEventReader xml = XML.createXMLEventReader(new StringReader(XMLVersion + inputXML));

		boolean inTitle = false;
		boolean inText = false;
		StringBuilder text = new StringBuilder();
		String title = "";

		while (xml.hasNext()) {
			XMLEvent evt = xml.nextEvent();
			int type = evt.getEventType();
			if (type == XMLEvent.START_ELEMENT && evt.asStartElement().getName().toString().equals("text")) {
				inText = true;
			} else if (inText && type == XMLEvent.CHARACTERS) {
				text.append(evt.asCharacters().getData());
			} else if (inText && evt.isEndElement() && evt.asEndElement().getName().toString().equals("text")) {
				inText = false;
				break;
			}
		}

		return text.toString();
	}

	public static boolean substringMatches(String haystack, int offset, String query) {
		for (int i = 0; i < query.length(); i++) {
			if (i + offset >= haystack.length()) {
				return false;
			}
			if (haystack.charAt(i + offset) != query.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	public static String relocateReferences(String text) {

		String lower = text.toLowerCase();
		final String reflist = "{{reflist";
		final int refListStart = lower.indexOf(reflist);
		if (refListStart == -1) {
			return text;
		}

		StringBuilder refb = new StringBuilder();
		StringBuilder sb = new StringBuilder();

		int offset = 0;
		// only apply to one reflist
		for (;;) {
			// collect any <ref>.*</ref>
			int thisRef = lower.indexOf("<ref>", offset);
			if (thisRef == -1 || thisRef > refListStart) {
				// this skips references after the reflist is pasted...
				break;
			}
			// copy untouched part of document
			sb.append(text.substring(offset, thisRef));
			// find end of this tag:
			int afterTag = lower.indexOf("</ref>", thisRef + "<ref>".length()) + "</ref>".length();
			// keep this reference
			refb.append(text.substring(thisRef, afterTag)).append('\n');
			// update offset
			offset = afterTag;
		}

		assert (offset < refListStart);

		// copy before reflist insert
		sb.append(text.substring(offset, refListStart));
		// add references raw here
		sb.append("<references>").append(refb).append("</references>");
		// skip rest of reflist template
		offset = text.indexOf("}}", refListStart);
		sb.append(text.substring(offset));

		return sb.toString();
	}

	public static String fixMediaWikiLinks(String text) {
		StringBuilder sb = new StringBuilder();

		int startFrom = 0;

		for (int i = 0; i < text.length(); i++) {

			// find start of the internal link
			if (text.charAt(i) == '[' && text.charAt(i + 1) == '[') {
				int afterPrevious = startFrom;
				int beforeLink = i;
				int startLink = i + 2;

				// find end of the link
				int endLink = text.indexOf("]]", startLink);
				int afterLink = endLink + "]]".length();

				// copy previous chunk to new document
				sb.append(text.substring(afterPrevious, beforeLink));
				sb.append(convertInternalLink(text.substring(startLink, endLink)));

				startFrom = afterLink;
				i = afterLink - 1;
				continue;
			}

			// find the start of an external link:
			if (text.charAt(i) == '[' && text.charAt(i + 1) != '[') {
				int afterPrevious = startFrom;
				int beforeLink = i;
				int startLink = i + 1;

				// find end of link:
				int endLink = text.indexOf("]", startLink);
				int afterLink = endLink + 1;

				//copy previous chunk to document
				sb.append(text.substring(afterPrevious, beforeLink));
				// convert link
				sb.append(convertExternalLink(text.substring(startLink, endLink)));

				//update pointers and continue
				startFrom = afterLink;
				i = afterLink - 1;
				continue;
			}

			if (text.charAt(i) == '{' && text.charAt(i + 1) == '{' && text.charAt(i + 2) == '{') {
				int afterPrevious = startFrom;
				int beforeTmpl = i;
				int startTmpl = i + 3;

				// find end of the template
				int endTmpl = text.indexOf("}}}", startTmpl);
				int afterTmpl = endTmpl + "}}}".length();

				// copy previous chunk to new document
				sb.append(text.substring(afterPrevious, beforeTmpl));
				sb.append(convertParameter(text.substring(startTmpl, endTmpl)));

				startFrom = afterTmpl;
				i = afterTmpl - 1;
				continue;
			}

			if (text.charAt(i) == '{' && text.charAt(i + 1) == '{') {
				int afterPrevious = startFrom;
				int beforeTmpl = i;
				int startTmpl = i + 2;

				// find end of the template
				int endTmpl = text.indexOf("}}", startTmpl);
				int afterTmpl = endTmpl + "}}".length();

				// copy previous chunk to new document
				sb.append(text.substring(afterPrevious, beforeTmpl));
				sb.append(convertTemplate(text.substring(startTmpl, endTmpl)));

				startFrom = afterTmpl;
				i = afterTmpl - 1;
				continue;
			}
		}

		// copy any dangling pieces:
		sb.append(text.substring(startFrom));

		return sb.toString();
	}

	public static String fixHeadings(String text) {
		StringBuilder out = new StringBuilder();
		BufferedReader rdr = new BufferedReader(new StringReader(text));

		while (true) {
			String line = null;
			try {
				line = rdr.readLine();
			} catch (IOException ex) {
			}
			if (line == null) {
				break;
			}

			if (!line.isEmpty() && line.charAt(0) == '=') {
				int hlevel = 1;
				for (; hlevel < 6; hlevel++) {
					if (line.charAt(hlevel) != '=') {
						break;
					}
				}
				int chop = line.indexOf("=", hlevel);
				out.append("<h").append(hlevel).append(">")
								.append(line.substring(hlevel, chop).trim())
								.append("</h").append(hlevel).append(">")
								.append('\n');
			} else {
				out.append(line).append('\n');
			}
		}

		return out.toString();
	}

	private static void processPage(String title, String toString) {
		try {
			String text = getText(toString);
			if (text.startsWith("#REDIRECT")) {
				// TODO log redirects
				return;
			}
			String html = fixHeadings(fixMediaWikiLinks(relocateReferences(text)));
			System.out.println("processPage: " + title + " : " + html);

		} catch (XMLStreamException ex) {
			Logger.getLogger(Parser.class
							.getName()).log(Level.SEVERE, null, ex);
		}
	}

}
