package edu.umass.ciir.strepsimur.galago

import org.lemurproject.galago.tupleflow.Parameters

/**
 * User: dietz
 * Date: 3/29/13
 * Time: 3:39 PM
 */
object GalagoQueryBuilder {

  case class SeqDep(term: Double, bigram: Double, windowedBigram: Double) {
    def asTuple = (term, bigram, windowedBigram)

    def _1 = term

    def _2 = bigram

    def _3 = windowedBigram
  }


  def rm(origQuery: ParametrizedQuery, fbOrigWt: Double, fbDocs: Int, fbTerms: Int): ParametrizedQuery = {
    val params: Parameters = GalagoQueryLib.paramRM(origQuery.parameters, fbOrigWt, fbDocs, fbTerms)
    ParametrizedQuery("#rm ( " + origQuery.queryStr + ")", params)
  }

  def seqdep(query: String, seqdepParams: Option[SeqDep] = None, mu: Option[Double] = None, fields: Seq[(String, Double)] = Seq.empty): ParametrizedQuery = {
    val param = new Parameters()
    if (seqdepParams.isDefined) GalagoQueryLib.paramSeqDep(param, seqdepParams.get.asTuple)
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    ParametrizedQuery(GalagoQueryLib.buildSeqDepForString(query, fields), param)
  }

  def weightedMultiSeqdep(weightedqueries: Seq[(String, Double)], seqdepParams: Option[SeqDep] = None, mu: Option[Double] = None): ParametrizedQuery = {
    val param = new Parameters()
    if (seqdepParams.isDefined) GalagoQueryLib.paramSeqDep(param, seqdepParams.get.asTuple)
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    val weightedSeqDeps =
      for ((query, weight) <- weightedqueries) yield (GalagoQueryLib.buildSeqDepForString(query), weight)
    val rawQuery = GalagoQueryLib.buildWeightedCombine(weightedSeqDeps)
    ParametrizedQuery(rawQuery, param)
  }

  def unigram(weightedqueries: Seq[(String, Double)], mu: Option[Double] = None): ParametrizedQuery = {
    val param = new Parameters()
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    val weightedBigrams =
      for ((query, weight) <- weightedqueries) yield (GalagoQueryLib.buildTermQueryForString(query), weight)
    val rawQuery = GalagoQueryLib.buildWeightedCombine(weightedBigrams)
    ParametrizedQuery(rawQuery, param)
  }

  def bigram(weightedqueries: Seq[(String, Double)], mu: Option[Double] = None): ParametrizedQuery = {
    val param = new Parameters()
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    val weightedBigrams =
      for ((query, weight) <- weightedqueries) yield (GalagoQueryLib.buildBigramForString(query), weight)
    val rawQuery = GalagoQueryLib.buildWeightedCombine(weightedBigrams)
    ParametrizedQuery(rawQuery, param)
  }

  def windowedBigram(weightedqueries: Seq[(String, Double)], mu: Option[Double] = None): ParametrizedQuery = {
    val param = new Parameters()
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    val weightedBigrams =
      for ((query, weight) <- weightedqueries) yield (GalagoQueryLib.buildWindowedBigramForString(query), weight)
    val rawQuery = GalagoQueryLib.buildWeightedCombine(weightedBigrams)
    ParametrizedQuery(rawQuery, param)
  }

  def passageRetrieval(initialQuery: ParametrizedQuery, workingSet: List[String], passageSize: Int, passageShift: Int, seqdepParams: Option[SeqDep] = None, mu: Option[Double] = None): ParametrizedQuery = {
    val param = new Parameters()
    GalagoParamTools.myParamCopyFrom(param, initialQuery.parameters)
    if (seqdepParams.isDefined) GalagoQueryLib.paramSeqDep(param, seqdepParams.get.asTuple)
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    GalagoQueryLib.paramPassageRetrieval(param, workingSet, passageSize, passageShift)
    ParametrizedQuery(initialQuery.queryStr, param)
  }

  def seqdepPassage(question: String, workingSet: List[String], passageSize: Int, passageShift: Int, seqdepParams: Option[SeqDep] = None, mu: Option[Double] = None): ParametrizedQuery = {
    val param = new Parameters()
    if (seqdepParams.isDefined) GalagoQueryLib.paramSeqDep(param, seqdepParams.get.asTuple)
    if (mu.isDefined) GalagoQueryLib.paramSmoothingMu(param, mu.get)
    GalagoQueryLib.paramPassageRetrieval(param, workingSet, passageSize, passageShift)
    ParametrizedQuery(GalagoQueryLib.buildSeqDepForString(question), param)
  }

  def expandQuery(origQuery: ParametrizedQuery, expansionTerms: Seq[(String, Double)], origWeight: Double): ParametrizedQuery = {
    val queryStr =
      GalagoQueryLib.buildWeightedCombine(Seq(
        origQuery.queryStr -> origWeight,
        GalagoQueryLib.buildWeightedCombine(expansionTerms) -> (1.0 - origWeight)
      ))
    ParametrizedQuery(queryStr, origQuery.parameters)
  }

}

case class ParametrizedQuery(queryStr: String, parameters: Parameters)
