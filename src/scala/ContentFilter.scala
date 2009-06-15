package org.improving.fuse

import java.io._
import scala.collection.mutable.HashMap

abstract trait ContentFilter {
  def getSource(path: String): Option[String]
  def transform(path: String, in: Array[Byte]): Array[Byte]
  def getSize(path: String): Int
}

class JavapFilter(val prefix: String) extends ContentFilter {
  final val CMD = "/local/bin/jad -p "
  val cache = new HashMap[String, String]
  
  private def runJad(path: String): String = {
    if (cache contains path) return cache(path)
    
    val proc = Runtime.getRuntime.exec(CMD + path)
    val in = new BufferedReader(new InputStreamReader(proc.getInputStream))
    def getLines(b: BufferedReader): List[String] = b.readLine match {
      case null => Nil
      case x    => (x + "\n") :: getLines(b)
    }
    val out = getLines(in).mkString
    cache(path) = out
    out
  }
  
  def getSource(path: String): Option[String] = if (path startsWith prefix) Some(path.substring(prefix.length)) else None
  def transform(path: String, content: Array[Byte]): Array[Byte] = runJad(path).toArray.map(_.toByte)  
  def getSize(path: String): Int = runJad(path).size
}