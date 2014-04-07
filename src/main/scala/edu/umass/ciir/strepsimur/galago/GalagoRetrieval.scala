package edu.umass.ciir.strepsimur.galago

import org.lemurproject.galago.core.retrieval.{ScoredPassage, ScoredDocument}
import org.lemurproject.galago.core.tokenize.Tokenizer

import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.tupleflow.Parameters
import scala.collection.JavaConversions._


/**
 * User: dietz
 * Date: 7/15/13
 * Time: 4:53 PM
 */
class GalagoRetrieval(val galagoParams: Parameters) {
  val searcher = new GalagoSearcher(galagoParams)

  def retrieveDocs(query: ParametrizedQuery, numResults: Int): Seq[ScoredDocument] = {
   def debug(x: org.lemurproject.galago.core.retrieval.query.Node,
              y: org.lemurproject.galago.core.retrieval.query.Node) {
     println("run query x: " + x.toPrettyString)
     println("run query y: " + y.toPrettyString)
   }
    searcher.retrieveScoredDocuments(query.queryStr, Some(query.parameters), numResults)
    //    searcher.retrieveScoredDocuments(query.queryStr, Some(query.parameters), numResults, debug)
  }

  def retrievePassages(query: ParametrizedQuery, numResults: Int = 100): Seq[ScoredPassage] = {
    searcher.retrieveScoredPassages(query.queryStr, Some(query.parameters), numResults)
  }


  def fakeTokenize(text: String): Document = {
    val tokenizer = Tokenizer.instance(galagoParams)
    tokenizer.tokenize(text)
  }

  def fakeRetokenize(doc: Document) {
    val tokenizer = Tokenizer.instance(galagoParams)
    tokenizer.tokenize(doc)
  }

  def retrievePassageTerms(docname: String, beginToken: Int, endToken: Int): Seq[String] = {
    searcher.pullDocumentWithTokens(docname).terms.subList(beginToken, endToken)
  }

  def retrievePassageDocs(query: ParametrizedQuery, numResults: Int = 100): Seq[FetchedScoredPassage] = {
    val results = searcher.retrieveScoredPassages(query.queryStr, Some(query.parameters), numResults)
    val fetched = searcher.fetchPassages(results)
    for (FetchedScoredPassage(_, doc) <- fetched) {
      if (doc.termCharBegin == null || doc.termCharBegin.size() == 0) {
        fakeRetokenize(doc)
      }
    }
    fetched
  }
}
