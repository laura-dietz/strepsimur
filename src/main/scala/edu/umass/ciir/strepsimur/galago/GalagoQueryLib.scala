package edu.umass.ciir.strepsimur.galago

import org.lemurproject.galago.utility.Parameters
import scala.collection.JavaConversions._
import edu.umass.ciir.strepsi.StopWordList

/**
 * User: dietz
 * Date: 3/18/13
 * Time: 10:39 AM
 */
object GalagoQueryLib {

  def buildWSumNoNorm(weightedQueryStrs: Seq[(String, Double)]): String = {
    val filteredWeightedQueryStrs = weightedQueryStrs

    val weightsStr =
      for ((weight, idx) <- filteredWeightedQueryStrs.map(_._2).zipWithIndex) yield {
        idx + "=" + weight
      }

    if (filteredWeightedQueryStrs.isEmpty) ""
    else {
      val subqueries = filteredWeightedQueryStrs.map(_._1)
      "#wsum" + weightsStr.mkString(":", ":", "") + "(" + subqueries.mkString(" ") + ")"
    }
  }

  // ============== build raw query strings =============

  def buildSeqDepForString(string: String, fields: Seq[(String, Double)] = Seq.empty): String = {
    val filteredString = normalize(string).filterNot(StopWordList.isStopWord)
    if (filteredString.size > 0) {
      if (fields.isEmpty) {
        "#sdm(" + filteredString.mkString(" ") + ")"
      } else if (fields.length == 1) {
        val singleField = fields.head._1
        "#sdm(" + filteredString.map(_ + "." + singleField).mkString(" ") + ")"
      } else {
        val nestedQueries =
          for ((field, weight) <- fields) yield {
            val query = "#sdm(" + filteredString.map(_ + "." + field).mkString(" ") + ")"
            query -> weight
          }
        buildWeightedCombine(nestedQueries)
      }
    }
    else ""
  }

  def buildBigramForString(string: String): String = {
    val filteredString = normalize(string).filterNot(StopWordList.isStopWord)
    if (filteredString.size > 1) {
      "#combine(" + filteredString.sliding(2).map(ngram => "#ordered:1(" + ngram(0) + " " + ngram(1) + ") ").mkString(
        " ") + ")"
    }
    else ""
  }

  def buildWindowedBigramForString(string: String): String = {
    val filteredString = normalize(string).filterNot(StopWordList.isStopWord)
    if (filteredString.size > 1) {
      "#combine(" + filteredString.sliding(2).map(ngram => "#unordered:8(" + ngram(0) + " " + ngram(1) + ") ").mkString(
        " ") + ")"
    }
    else ""
  }

  def buildTermQueryForString(string: String): String = {
    val filteredString = normalize(string).filterNot(StopWordList.isStopWord)
    if (filteredString.size > 0) filteredString.mkString(" ")
    else ""

  }

  def buildOrderedWindowQueryForCounting(string: String, windowSize: Int = 1, filterStopwords: Boolean = true,
                                         replaceStopWithWildcard: Boolean = false
                                          ): String = {
    def buildOrdered(filteredString: Seq[String], windowSize: Int): String = {
      if (filteredString.size > 0) {
        "#ordered:" + windowSize + "(" + filteredString.mkString(" ") + ")"
      }
      else ""
    }


    if (filterStopwords) {
      buildOrdered(normalize(string).filterNot(StopWordList.isStopWord), windowSize)
    } else if (replaceStopWithWildcard) {
      val (stops, terms) = normalize(string).partition(StopWordList.isStopWord)
      buildOrdered(terms, windowSize + stops.size)

    } else {
      buildOrdered(normalize(string), windowSize)
    }


  }

  def buildOrderedWindowQuery(string: String, windowSize: Int = 1): String = {
    val filteredString = normalize(string).filterNot(StopWordList.isStopWord(_))
    if (filteredString.size > 0) {
      "#combine( #od:" + windowSize + "(" + filteredString.mkString(" ") + ") )"
    }
    else ""
  }


  def buildMultiPhraseQuery(phrases: Seq[String]): String = {
    "#combine(  " + phrases.map(buildSeqDepForString(_)).mkString(" ") + ")"
  }

  def buildBooleanQuery(queryStr: String): String = {
    if (queryStr.length == 0) throw new IllegalArgumentException("#require #all cannot be applied to an empty query. " +
      "QueryStr = " + queryStr)
    "#all( " + queryStr + " )"
  }

  def buildWeightedCombine(weightedQueryStrs: Seq[(String, Double)]): String = {
    val filteredWeightedQueryStrs =
      renormalize(weightedQueryStrs.filter({
        case (subquery, weight) => subquery.length > 0 && weight > 0.0
      }))


    val weightsStr =
      for ((weight, idx) <- filteredWeightedQueryStrs.map(_._2).zipWithIndex) yield {
        idx + "=" + weight
      }

    if (filteredWeightedQueryStrs.isEmpty) ""
    else {
      val subqueries = filteredWeightedQueryStrs.map(_._1)
      "#combine" + weightsStr.mkString(":", ":", "") + "(" + subqueries.mkString(" ") + ")"
    }
  }


  private def renormalize(weightedTerms: Seq[(String, Double)]): Seq[(String, Double)] = {
    if (weightedTerms.size == 0) weightedTerms
    else {
      val sum = weightedTerms.map(_._2).sum
      for ((term, weight) <- weightedTerms) yield (term -> weight / sum)
    }
  }

  private def buildMultiTermQuery(phrases: Seq[String]): String = {
    "#combine(  " + phrases.flatMap(normalize(_).filterNot(StopWordList.isStopWord)).mkString(" ") + ")"
  }


  // ======== configure Parameter object ==================
  def paramStemmedRetrieval(p: Parameters, stemRetrieval: Option[Boolean] = None): Parameters = {
    if (stemRetrieval.isDefined) {
      p.set("stemming", stemRetrieval.get)
    }
    p
  }


  def paramWorkingSet(p: Parameters, workingSet: List[String]): Parameters = {
    p.set("working", seqAsJavaList(workingSet))
    p
  }

  def paramAnnotation(p: Parameters, annotationsOn: Boolean): Parameters = {
    p.set("annotate", annotationsOn)
    p
  }

  def paramSeqDep(p: Parameters, seqDepParam: (Double, Double, Double)): Parameters = {
    val (uniw, odw, uww) = seqDepParam
    p.set("uniw", uniw)
    p.set("odw", odw)
    p.set("uww", uww)
    p
  }

  def paramRM(p: Parameters, fbOrigWt: Double, fbDocs: Int, fbTerms: Int): Parameters = {
    p.set("fbOrigWt", fbOrigWt)
    p.set("fbDocs", fbDocs)
    p.set("fbTerm", fbTerms)
    p
  }

  def paramSmoothingMu(p: Parameters, smoothingMu: Double): Parameters = {
    p.set("mu", smoothingMu)
    p
  }

  def paramPassageRetrieval(p: Parameters, workingSet: List[String], defaultPassageSize: Int = 50,
                            defaultPassageShift: Int = 25
                             ): Parameters = {
    p.set("passageQuery", true)
    p.set("passageSize", defaultPassageSize)
    p.set("passageShift", defaultPassageShift)
    paramWorkingSet(p, workingSet)
    p
  }

  def paramPassageExtentRetrieval(p: Parameters, workingSet: List[String], defaultPassageSize: Int = 50,
                                  defaultPassageShift: Int = 25, extentName:String
                                   ): Parameters = {
    p.set("extentQuery", true)
    p.set("extent", extentName)
    p.set("extentCount", defaultPassageSize)
    p.set("extentShift", defaultPassageShift)
    paramWorkingSet(p, workingSet)
    p
  }


  def noFlat(p: Parameters): Parameters = {
    p.set("norm", false)
    p
  }

  def noDeltaReady(p: Parameters): Parameters = {
    p.set("deltaReady", false)
    p
  }


  // ======== Helpers ===========================

  def normalize(query: String): Seq[String] = {
    val useTokenNormalization = System.getProperty("token.normalize", "true").toBoolean
    if (useTokenNormalization) {
      query.replace("-", " ").split("\\s+").map(cleanString(_).toLowerCase).filter(_.length() > 1)
    } else {
      query.split("\\s+")
    }
  }

  /**
   * Ensure a safe galago query term.
   *
   * @param queryTerm
   * @return
   */
  def cleanString(queryTerm: String): String = {
    queryTerm.replaceAllLiterally("-", " ").replaceAll("[^a-zA-Z0-9]", "")
  }

  def escapeTerm(term: String): String = {
    "@\"" + term + "\""
  }


}
