package edu.umass.ciir.strepsimur

import org.lemurproject.galago.core.btree.simple.DiskMapReader
import scala.collection.JavaConversions._
import java.nio.charset.Charset
import scala.collection.{immutable, mutable}
import java.nio.{FloatBuffer, ByteBuffer}


/**
 * Wrapping library to store arbitrary things in a galago DiskMapReader
 */
object DiskBackerConv {
  def string2byte(string:String):Array[Byte] = {
    string.getBytes(Charset.forName("UTF-8"))
  }
  def byte2string(bytes:Array[Byte]):String = {
    new String(bytes, "UTF-8")
  }

  def long2byte(long:Long):Array[Byte] = {
    Array(long.toByte)
  }

  def byte2long(bytes:Array[Byte]):Long = {
    val bb = ByteBuffer.wrap(bytes)
    bb.getLong
  }

  def int2byte(int:Int):Array[Byte] = {
    Array(int.toByte)
  }

  def byte2int(bytes:Array[Byte]):Int = {
    val bb = ByteBuffer.wrap(bytes)
    bb.getInt
  }

  def longlong2byte(long:(Long,Long)):Array[Byte] = {
    val bb =ByteBuffer.allocate(16)
    bb.putLong(long._1)
    bb.putLong(long._2)
    bb.array()
  }


  def byte2longlong(bytes:Array[Byte]):(Long,Long) = {
    val bb = ByteBuffer.wrap(bytes)
    val x = bb.asLongBuffer()
    val long1 = x.get()
    val long2 = x.get()
    (long1,long2)
  }



}

object DiskBacking {
  def createDiskBacking[Key,Value](string2stringMap: mutable.Map[Key, Value], filename:String, 
                                   key2Bytes:(Key)=>Array[Byte],
                                   value2Bytes:(Value)=>Array[Byte]){
    val inputMap = string2stringMap.map(entry => (key2Bytes(entry._1), value2Bytes(entry._2)))
    DiskMapReader.fromMap(filename, inputMap)
  }

  def createDiskBackingImm[Key, Value](string2stringMap: immutable.Map[Key, Value], filename: String,
                                       key2Bytes: (Key) => Array[Byte],
                                       value2Bytes: (Value) => Array[Byte]) {
    val inputMap = string2stringMap.map(entry => (key2Bytes(entry._1), value2Bytes(entry._2)))
    DiskMapReader.fromMap(filename, inputMap)
  }

  def createStringStringDiskBacking(map:mutable.Map[String,String], filename:String)= {
    createDiskBacking[String,String](
      map,
      filename,
      key2Bytes = DiskBackerConv.string2byte,
      value2Bytes = DiskBackerConv.string2byte
    )
  }

  def createStringStringDiskBackingImm(map: immutable.Map[String, String], filename: String) = {
    createDiskBackingImm[String, String](
      map,
      filename,
      key2Bytes = DiskBackerConv.string2byte,
      value2Bytes = DiskBackerConv.string2byte
    )
  }

  def createStringLongLongDiskBacking(map:mutable.Map[String,(Long,Long)], filename:String)= {
    DiskBacking.createDiskBacking[String,(Long,Long)](
      map,
      filename,
      key2Bytes = DiskBackerConv.string2byte,
      value2Bytes = DiskBackerConv.longlong2byte
    )
  }
  
  def createStringIntDiskBacking(map:mutable.Map[String,Int], filename:String)= {
    DiskBacking.createDiskBacking[String,Int](
      map,
      filename,
      key2Bytes = DiskBackerConv.string2byte,
      value2Bytes = DiskBackerConv.int2byte
    )
  }


}

trait DiskBacking[Key,Value]{
  val path:String
  val dmr = new DiskMapReader(path)
  def key2b(k:Key):Array[Byte]
  def value2b(k:Value):Array[Byte]
  def b2key(bytes:Array[Byte]):Key
  def b2value(bytes:Array[Byte]):Value

  def apply(key:Key):Value= {
    b2value(dmr.get(key2b(key)))
  }
  def containsKey(key:Key):Boolean = {
    dmr.containsKey(key2b(key))
  }

  def keySet():Set[Key]={
    dmr.keySet().map(b2key).toSet
  }
}


trait StringKeyBacking[Value] extends DiskBacking[String, Value] {
  def key2b(k: String) = DiskBackerConv.string2byte(k)
  def b2key(bytes: Array[Byte]) = DiskBackerConv.byte2string(bytes)
}

trait StringValueBacking[Key] extends DiskBacking[Key, String] {
  def value2b(k: String) = DiskBackerConv.string2byte(k)
  def b2value(bytes: Array[Byte]) = DiskBackerConv.byte2string(bytes)
}
trait LongValueBacking[Key] extends DiskBacking[Key, Long] {
  def value2b(k: Long) = DiskBackerConv.long2byte(k)
  def b2value(bytes: Array[Byte]):Long = DiskBackerConv.byte2long(bytes)
}
trait IntValueBacking[Key] extends DiskBacking[Key, Int] {
  def value2b(k: Int) = DiskBackerConv.int2byte(k)
  def b2value(bytes: Array[Byte]):Int = DiskBackerConv.byte2int(bytes)
}
trait LongLongValueBacking[Key] extends DiskBacking[Key, (Long,Long)] {
  def value2b(k: (Long, Long)) = DiskBackerConv.longlong2byte(k)
  def b2value(bytes: Array[Byte]):(Long,Long) = DiskBackerConv.byte2longlong(bytes)
}



class String2StringDiskBacking(val path:String)
  extends DiskBacking[String,String]
  with StringKeyBacking[String]
  with StringValueBacking[String]{
  def getString(key:java.lang.String):java.lang.String = {
    try {
      this.apply(key).asInstanceOf[java.lang.String]
    } catch {
      case ex: NullPointerException => null
    }
  }
}

class String2IntDiskBacking(val path:String)
  extends DiskBacking[String,Int]
  with StringKeyBacking[Int]
  with IntValueBacking[String]{
  def getString(key:java.lang.String):java.lang.Integer = {
    try {
      this.apply(key).asInstanceOf[java.lang.Integer]
    } catch {
      case ex: NullPointerException => null
    }
  }
}

class String2LongDiskBacking(val path:String)
  extends DiskBacking[String,Long]
  with StringKeyBacking[Long]
  with LongValueBacking[String]{}

class String2LongLongDiskBacking(val path:String)
  extends DiskBacking[String,(Long,Long)]
  with StringKeyBacking[(Long,Long)]
  with LongLongValueBacking[String]{}


