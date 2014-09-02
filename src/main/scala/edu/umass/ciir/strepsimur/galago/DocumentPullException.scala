package edu.umass.ciir.strepsimur.galago

import edu.umass.ciir.strepsimur.galago.GalagoParamTools._
import org.lemurproject.galago.core.parse.Document
import org.lemurproject.galago.utility.Parameters

/**
 * User: dietz
 * Date: 4/3/14
 * Time: 3:45 PM
 */
class DocumentPullException(val docIdentifier:String) extends RuntimeException("Could not pull document identifier "+docIdentifier){
}

trait DocumentPuller[DocumentType] {
  /** @throws edu.umass.ciir.strepsimur.galago.DocumentPullException */
  def pullDocument(documentName: String, params: Parameters = new Parameters()): DocumentType

}
