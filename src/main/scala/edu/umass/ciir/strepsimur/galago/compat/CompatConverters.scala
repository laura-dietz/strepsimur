package edu.umass.ciir.strepsimur.galago.compat

import edu.umass.ciir.strepsi.galagocompat.GalagoTag
import org.lemurproject.galago.core.parse.{Document, Tag}
import scala.collection.JavaConversions._
import edu.umass.ciir.strepsi.{ScoredPassage, ScoredDocument}

/**
 * User: dietz
 * Date: 2/25/14
 * Time: 6:11 PM
 */
object CompatConverters {
  def strepsiTagToGalagoTag(tag:Tag, documentName:String):GalagoTag = {
    GalagoTag(tag.name, tag.begin, tag.end, documentName)
  }

  def doc2Tag(document:Document):Seq[GalagoTag] = {
    if(document.tags == null) Seq.empty
    else document.tags.map(strepsiTagToGalagoTag(_, document.name))
  }

  def doc2TermTag(document:Document):(Seq[String],Seq[GalagoTag]) = {
    (document.terms, doc2Tag(document))
  }

  def gDoc2sDoc(scoredDocument:org.lemurproject.galago.core.retrieval.ScoredDocument):ScoredDocument = {
    new ScoredDocument(scoredDocument.documentName, scoredDocument.rank, scoredDocument.score)
  }
  def sDoc2gDoc(scoredDocument:ScoredDocument):org.lemurproject.galago.core.retrieval.ScoredDocument = {
    new org.lemurproject.galago.core.retrieval.ScoredDocument(scoredDocument.documentName, scoredDocument.rank, scoredDocument.score)
  }
  def gDocSeq2sDocSeq(gDocSeq:Seq[org.lemurproject.galago.core.retrieval.ScoredDocument]):Seq[ScoredDocument] = {
    for (doc <- gDocSeq) yield CompatConverters.gDoc2sDoc(doc)
  }
  def gPsg2sPsg(scoredPsg:org.lemurproject.galago.core.retrieval.ScoredPassage):ScoredPassage = {
    new ScoredPassage(scoredPsg.documentName, scoredPsg.begin, scoredPsg.end, scoredPsg.rank, scoredPsg.score)
  }
  def gPsgSeq2sPsgSeq(gPsgSeq:Seq[org.lemurproject.galago.core.retrieval.ScoredPassage]):Seq[ScoredPassage] = {
    for (doc <- gPsgSeq) yield CompatConverters.gPsg2sPsg(doc)
  }
}
