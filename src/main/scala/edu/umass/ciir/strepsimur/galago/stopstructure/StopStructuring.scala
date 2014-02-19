package edu.umass.ciir.strepsimur.galago.stopstructure

import scala.collection.mutable
import org.lemurproject.galago.core.util.WordLists
import org.lemurproject.galago.core.retrieval.Retrieval

/**
 * User: dietz
 * Date: 2/19/14
 * Time: 2:48 PM
 */
class StopStructuring(retrieval: Retrieval) {
  val stopstructureMap: mutable.TreeSet[String] = {
    val stopstructurelist = retrieval.getGlobalParameters.get("stopstructurelist",
      "stopStructure")
    import scala.collection.JavaConversions._

    val ss_set: Set[String] = WordLists.getWordList(stopstructurelist).toSet
    val defaultStopStructures = new mutable.TreeSet[String]()
    for (ss <- ss_set) {
      defaultStopStructures.add(ss.trim + " ")
    }
    defaultStopStructures
  }


  def removeStopStructure(origQueryText: String): String = {
    val tokens = origQueryText.toLowerCase.replaceAll("[^a-z]", " ").split(" ") // split on non-alpha characters
    val stopPrefixes = (1 to tokens.length).reverse
        .map(tokens.slice(0, _).mkString("", " ", " "))

    val stopStructure = stopPrefixes.find(stopstructureMap.contains)

    val queryText = stopStructure match {
      case None => origQueryText
      case Some(structure) => origQueryText.substring(structure.length)
    }
    queryText
  }


  def removeStopStructure(tokenizedQuery: Seq[String]): Seq[String] = {
    val tokens = tokenizedQuery.map(_.toLowerCase).map(_.replaceAll("[^a-z]", "")) // zap nonalpha characters
    val stopPrefixes = (1 to tokens.length).reverse
        .map(tokens.slice(0, _).mkString("", " ", " "))

    val stopStructure = stopPrefixes.find(stopstructureMap.contains)

    val queryText = stopStructure match {
      case None => tokenizedQuery
      case Some(structure) => {
        val countStructureTokens = structure.count(_ == " ")
        // the structure is appended by a space,
        // i.e. 1 space = 1 token
        tokenizedQuery.slice(countStructureTokens, tokenizedQuery.length)
      }
    }
    queryText
  }

}
