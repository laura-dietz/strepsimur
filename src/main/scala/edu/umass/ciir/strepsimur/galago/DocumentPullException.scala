package edu.umass.ciir.strepsimur.galago

/**
 * User: dietz
 * Date: 4/3/14
 * Time: 3:45 PM
 */
class DocumentPullException(val docIdentifier:String) extends RuntimeException("Could not pull document identifier "+docIdentifier){
}
