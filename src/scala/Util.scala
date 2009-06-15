package org.improving.fuse

import com.sun.jna._
import java.io.File

// TODO: look into NativeString, also Native.toByteArray(s) & Native.toCharArray(s)
object Util
{
  def nullTerminate(xs: List[String]) = xs.map(_ + 0.toChar)
  def nullLength(xs: List[String]) = nullTerminate(xs).map(_.length).foldLeft(0)(_+_)
  
  // null separated list to list of strings
  def listFromNullSep(list: Pointer, size: Int): List[String] = {
    if (list == null || size <= 0) return Nil
    
    val s = list.getString(0, false)   // equiva to Native.toString(list) (?)
    val len = s.length + 1
    s :: listFromNullSep(list.share(len), size - len)
  }
    
  // list of strings to null separated list, allocating new memory
  def listToNullSep(xs: List[String]): Pointer = {
    val bytes = nullLength(xs)
    val mem = new Memory(bytes)
    listToNullSep(xs, mem)
    mem
  }
    
  // list of strings to null separated list, using supplied pointer
  // returns # of chars written
  def listToNullSep(xs: List[String], buf: Pointer): Int = {    
    var len: Int = 0
    for (x <- xs) {
      buf.setString(len, x, false)
      len += x.length + 1
    }
    len
  }
  
  def setString(buf: Pointer, s: String): Int = {
    buf.setString(0, s, false)
    s.length
  }
  
  // flag check methods on ints
  implicit def int2flagger(x: Int): Flagger = new Flagger(x)
  class Flagger(val flags: Int) {
    def hasFlag(x: Int) = (flags & x) != 0
  }
  
  // files
  def getFiles(path: String): List[File] = {
    val f = new File(path)
    
    if (!f.canRead) Nil
    else if (!f.isDirectory) List(f)
    else f.listFiles.filter(_.canRead).toList
  }
}

object CanonPath {
  private def canon(s: String): String = (if (s startsWith "/") s else "/" + s).replaceAll("""/+""", "/")
  def apply(s: String*) = new CanonPath(canon(s.mkString("/")))
  def unapply(cp: CanonPath): Option[String] = Some(cp.path)
}
  
// parses full path into (directory, path) in consistent way
final class CanonPath private(val path: String) {
  private lazy val javaFile = new java.io.File(path)
  
  lazy val isDirectory = path endsWith "/"
  lazy val lastModTime: Long = javaFile.lastModified / 1000
  lazy val lastSlash = path.lastIndexOf('/')
  lazy val segments: List[String] =
    path.split('/').filter(_ != "").toList.dropRight(if (isDirectory) 0 else 1)  
  
  def prepend(pre: CanonPath): CanonPath = CanonPath(Seq(pre.path, path): _*)
  def prepend(segs: String*): CanonPath = CanonPath(segs ++ Seq(path): _*)
  def append(segs: String*): CanonPath = CanonPath(Seq(path) ++ segs: _*)
  def append(app: CanonPath): CanonPath = CanonPath(Seq(path, app.path): _*)

  def startsWith(other: CanonPath) = path startsWith other.path
    
  def file(): String = path.substring(lastSlash + 1)
  def dir(): CanonPath = CanonPath(if (lastSlash == 0) "/" else path.substring(0, lastSlash))
  def parent(): Option[CanonPath] = if (segments.isEmpty) None else Some(CanonPath(segments.drop(1): _*))
  def alldirs(): List[CanonPath] =
    for (i <- List.range(0, segments.length + 1)) yield
      CanonPath(segments.take(i): _*)
  
  override def toString(): String = "Canon(" + path + ")"
  override def equals(other: Any) = other match {
    case x: CanonPath   => path == x.path
    case x: String      => path == x
    case x              => println("CanonPath not ==: " + x.toString) ; false
  }
  override def hashCode(): Int = path.hashCode
}