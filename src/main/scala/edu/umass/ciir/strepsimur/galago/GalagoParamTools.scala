package edu.umass.ciir.strepsimur.galago

import org.lemurproject.galago.utility.Parameters
import scala.collection.JavaConversions._

/**
 * User: dietz
 * Date: 12/5/13
 * Time: 2:52 PM
 */
object GalagoParamTools {
  def myParamCopyFrom(toParams: Parameters, fromParams: Parameters): Parameters = {
    for (key <- fromParams.getKeys) {
      if (fromParams.isBoolean(key)) toParams.set(key, fromParams.getBoolean(key))
      else if (fromParams.isDouble(key)) toParams.set(key, fromParams.getDouble(key))
      else if (fromParams.isLong(key)) toParams.set(key, fromParams.getLong(key))
      else if (fromParams.isString(key)) toParams.set(key, fromParams.getString(key))
      else if (fromParams.isMap(key)) toParams.set(key, fromParams.getMap(key))
      else if (fromParams.isList(key)) toParams.set(key, fromParams.getAsList(key))
      else {
        throw new RuntimeException(
          "Try to copy params: errornous key " + key + " has unknown type. " + fromParams.toPrettyString)
      }

      //      else if (fromParams.isMap(key)){
      //        val mparams = new Parameters()
      //        fromParams.getMap(key).copyTo(mparams)
      //        toParams.set(key,mparams)
      //    }
    }
    toParams
  }
}
