package edu.umass.ciir.strepsimur.galago.compat

import edu.umass.ciir.strepsi.galagocompat.GalagoTag
import org.lemurproject.galago.core.parse.Tag

/**
 * User: dietz
 * Date: 2/25/14
 * Time: 6:11 PM
 */
object CompatConverters {
  def strepsiTagToGalagoTag(tag:Tag, documentName:String):GalagoTag = {
    GalagoTag(tag.name, tag.begin, tag.end, documentName)
  }

}
