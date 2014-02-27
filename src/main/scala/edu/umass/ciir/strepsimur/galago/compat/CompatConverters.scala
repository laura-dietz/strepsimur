package edu.umass.ciir.strepsimur.galago.compat

import edu.umass.ciir.strepsi.galagocompat.GalagoTag
import org.lemurproject.galago.core.parse.{Document, Tag}
import scala.collection.JavaConversions._

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
    document.tags.map(strepsiTagToGalagoTag(_, document.name))
  }
}
