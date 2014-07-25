package edu.umass.ciir.strepsimur.galagoexport;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import edu.umass.ciir.strepsi.FileTools;
import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.tupleflow.Parameters;
import org.lemurproject.galago.tupleflow.StreamCreator;

public class CorpusExport {

    private static PrintWriter outWriter(String outputDir, String outputPrefix, int outputIndex) throws IOException {
        String filePath = new File(outputDir, outputPrefix+outputIndex+".trectext.gz").getAbsolutePath();
        DataOutputStream dos = StreamCreator.openOutputStream(filePath);
        return new PrintWriter(dos);
    }

    public static void main(String[] args) throws IOException, Exception {
        Parameters argp = Parameters.parseArgs(args);

        String inputIndex = argp.getString("inputIndex");
        String outputDir = argp.getString("outputDir");
        String outputPrefix = argp.get("outputPrefix", "fromIndex");
        long documentsPerSplit = argp.get("numDocs", 50000);

        File corpus = new File(inputIndex, "corpus");
        if(!corpus.exists()) {
            throw new RuntimeException("No corpus file or directory found for index!");
        }

        FileTools.makeNecessaryDirs(outputDir);

        DocumentReader dr = new CorpusReader(corpus.getAbsolutePath());
        DocumentReader.DocumentIterator iter = (DocumentReader.DocumentIterator) dr.getIterator();

        int outputIndex = 0;
        long numDocuments = 0;

//        Parameters docP = new Parameters();
//        docP.set("metadata", true);
//        docP.set("terms", false);
//        docP.set("text", true);

        PrintWriter out = outWriter(outputDir, outputPrefix, outputIndex);
        while(!iter.isDone()) {
            Document doc = iter.getDocument(new Document.DocumentComponents(true, true, false));
            iter.nextKey();
            if(doc == null) continue;

            // output trec to output stream
            out.println("<DOC>");

            out.print("<DOCNO>");
            out.print(doc.name);
            out.println("</DOCNO>");

            out.println("<META>");
            for(String key:doc.metadata.keySet()){
                String value = doc.metadata.get(key);
                if(value == null) {
                    out.println("<" + key + "/>");
                }
                else if(value.trim().startsWith("<?xml ")) {
                    out.println("<" + key + "> <![CDATA[" + value + "]]> </" + key + ">");
                } else {
                    out.println("<" + key + "> " + value  + "</" + key + ">");
                }
            }
            out.println("</META>");

            out.println("<TEXT>");
            out.println(doc.text);
            out.println("</TEXT>");

            out.println("</DOC>");

            numDocuments += 1;
            if(numDocuments == documentsPerSplit) {
                numDocuments = 0;
                out.close();
                out = outWriter(outputDir, outputPrefix, ++outputIndex);
            }
        }
        out.close();
    }
}

