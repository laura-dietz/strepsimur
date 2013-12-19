package edu.umass.ciir.strepsimur.galago

import org.lemurproject.galago.core.retrieval.{ScoredPassage, ScoredDocument}
import org.lemurproject.galago.core.retrieval.prf.{RelevanceModel1, WeightedTerm, RelevanceModel3}
import org.lemurproject.galago.core.tokenize.Tokenizer

// todo merge conflict
//import edu.umass.ciir.sentopic.rm.RelevanceModelExpander
import java.io.IOException
import scala.collection.JavaConversions._
import org.lemurproject.galago.core.parse.{TagTokenizer, Document}
import org.lemurproject.galago.tupleflow.{Parameters, FakeParameters}

/**
 * User: dietz
 * Date: 7/15/13
 * Time: 4:53 PM
 */
class GalagoRetrieval(val galagoParams:Parameters) {
  val searcher = new GalagoSearcher(galagoParams)

  def retrieveDocs(query:ParametrizedQuery, numResults:Int):Seq[ScoredDocument] = {
    def debug(x: org.lemurproject.galago.core.retrieval.query.Node, y:org.lemurproject.galago.core.retrieval.query.Node){
      println(x.toPrettyString)
    }
    searcher.retrieveScoredDocuments(query.queryStr, Some(query.parameters), numResults)
  }

  // todo merge conflict
//  def runPRF(query:ParametrizedQuery):Seq[(String, Double)] = {
//    try {
//      val expander = new RelevanceModel1(searcher.m_searcher)//, FigConf.galagoParams)
//      val expansionTerms = expander.runExpansion(query.queryStr, query.parameters)
//
//      for (wt: WeightedTerm <- expansionTerms.toSeq) yield {
//        wt.getTerm -> wt.getWeight
//      }
//    }
//    catch {
//      case ex:IOException => throw new RuntimeException(ex)
//    }
//  }
//

  def retrievePassages(query:ParametrizedQuery, numResults:Int = 100):Seq[ScoredPassage] = {
    searcher.retrieveScoredPassages(query.queryStr, Some(query.parameters), numResults)
  }


  def fakeTokenize(text: String): Document = {
    //    val tagTokenizer = new TagTokenizer(new FakeParameters(galagoParams))
    //    tagTokenizer.tokenize(text)
    val tokenizer = Tokenizer.instance(galagoParams)
    tokenizer.tokenize(text)
  }

  def fakeRetokenize(doc:Document) {
    val tokenizer = Tokenizer.instance(galagoParams)
    tokenizer.tokenize(doc)
    //
    //
    //    val tagTokenizer = new TagTokenizer(new FakeParameters(galagoParams))
    //    val fakeDoc = tagTokenizer.tokenize(doc.text)
    //    doc.termCharBegin = fakeDoc.termCharBegin
    //    doc.termCharEnd = fakeDoc.termCharEnd
    //    doc.terms = fakeDoc.terms
  }


  def retrievePassageDocs(query:ParametrizedQuery, numResults:Int = 100):Seq[FetchedScoredPassage] = {
    val results = searcher.retrieveScoredPassages(query.queryStr, Some(query.parameters), numResults)
    val fetched = searcher.fetchPassages(results)
    for(FetchedScoredPassage(_, doc) <- fetched) {
      if (doc.termCharBegin == null || doc.termCharBegin.size() ==0 ){
        fakeRetokenize(doc)
      }
    }
    fetched
  }
}
