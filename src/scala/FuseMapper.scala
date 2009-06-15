package org.improving.fuse

import Fcntl._
import FuseUtil._
import jnajava.C._
import scala.collection.mutable
import mutable.{ HashMap, HashSet }

// TODO...
trait FUSEFactory {
  type DirType <: Mapper.Dir
  // type FileType <: Mapper.File
  
  def newDir(cpath: CanonPath): DirType
  // def newFile(cpath: CanonPath): FileType
}

object Mapper {
  implicit def canonicalizePath(path: String): CanonPath = CanonPath(path)
  implicit def string2ByteFun(s: String): () => Array[Byte] = () => s.toArray.map(_.toByte)
  
  def stringToBytes(s: String): Array[Byte] = s.toArray.map(_.toByte)
  def nowInSeconds(): Long = System.currentTimeMillis / 1000
  val empty: Array[Byte] = new Array[Byte](0)
  
  sealed abstract class FUSEObject {
    val cpath: CanonPath
    lazy val path = cpath.path
    
    def stat(stbuf: StructStat): Unit = {
      stbuf.st_mode = mode
      stbuf.st_nlink = nlink
      stbuf.st_size = size
      stbuf.st_mtime = lastModTime.toInt
      stbuf.write // XXX ? I think this fixed my open() issues.
    }
    def +=(entry: String): Unit
    def size(): Int = 0
    def mode(): Short = 0
    def nlink(): Short = 0
    def lastModTime(): Long = 0L
    def listxattr(): List[String] = Nil
    def getxattr(key: String): Option[String] = None
    def setxattr(key: String, value: String): Boolean = false   // false means disabled
  }
  
  trait FUSEAttributes extends FUSEObject {
    import Util._
    protected val xattrs = new HashMap[String, String]
    override def listxattr(): List[String] = xattrs.keys.toList
    override def getxattr(key: String): Option[String] = xattrs.get(key)
    override def setxattr(key: String, value: String): Boolean = {
      xattrs(key) = value
      true
    }
  }
  
  class DynamicFile(cpath: CanonPath, bytesFun: () => Array[Byte]) extends File(cpath) {
    case class LastRun(output: Array[Byte])
    private var lastRun: Option[LastRun] = None
    
    private def updateLastRun(): Array[Byte] = {
      val output = bytesFun()
      lastRun = Some(LastRun(output))
      output
    }
    
    override def bytes(): Array[Byte] = lastRun match {
      case Some(LastRun(output)) => lastRun = None ; output
      case None => updateLastRun
    }
    override def size(): Int = lastRun match {
      case Some(LastRun(output)) => output.size
      case None => updateLastRun().size
    }
    
    override def lastModTime(): Long = nowInSeconds
    override def toString(): String = "DynamicFile(" + path + ")"
  }
  
  class File(val cpath: CanonPath, val bytesIn: Array[Byte]) extends FUSEObject {
    def this(cpath: CanonPath) = this(cpath, empty)
    def this(cpath: CanonPath, contents: String) = this(cpath, stringToBytes(contents))
    
    override lazy val mode = (S_IFREG | 0444).toShort
    override lazy val nlink = 1.toShort
    
    override def size(): Int = bytes.size
    def bytes(): Array[Byte] = bytesIn
    def +=(entry: String): Unit = throw new Exception("not a directory: " + path)
    override def toString(): String = "File(" + path + ")"
  }
  
  class Dir(val cpath: CanonPath, val names: mutable.Set[String]) extends FUSEObject {
    def this(cpath: CanonPath, names: Seq[String]) = this(cpath, HashSet(names: _*))
    def this(cpath: CanonPath) = this(cpath, new HashSet[String])
    
    override def size(): Int = 34 * nlink
    override def mode() = (S_IFDIR | 0555).toShort
    override def nlink() = (names.size + 2).toShort   // 2 == ".", ".."
    
    def +=(entry: String): Unit = names += entry
    override def toString(): String = "Dir(" + names.toList.toString + ")"
  }
  
  // class Link(path: String) extends FUSEObject { }
}

class Mapper private (private var next: Option[Mapper]) extends FUSEFactory {
  import Mapper._
  def this() = this(None)
  type DirType = Dir
  def newDir(cpath: CanonPath): DirType = new Dir(cpath)
  
  // type FileType = File
  
  private val contents: HashMap[CanonPath, FUSEObject] = new HashMap[CanonPath, FUSEObject] 

  def addObject(obj: FUSEObject)/*(implicit dirfactory: DirFactory)*/: Unit = {
    val cpath = obj.cpath
    if (contents contains cpath) return
    
    // this makes a zipped list e.g. for /foo/bar/baz.html (/,foo), (/foo,bar), (/foo/bar,"")
    // then adds "foo" to /, "bar" to /foo, and so on
    for ((cdir, subdir) <- cpath.alldirs.zip(cpath.segments ::: List(""))) {
      if (!contents.contains(cdir))
        contents(cdir) = newDir(cdir) // dirfactory(cdir)
      else if (subdir != "")
        contents(cdir) += subdir
    }
    
    // if it's a file we still need to add it to main contents, and its filename to the dir's contents
    obj match {
      case x: File  => contents(cpath) = obj ; contents(cpath.dir) += cpath.file
      case _        => 
    }
  }

  def addFile(cpath: CanonPath, bytesFun: () => Array[Byte]) = {
    val obj = new File(cpath) { override def bytes(): Array[Byte] = bytesFun() }
    addObject(obj)
  }

  // the addDynamic functions will have their second argument re-evaluted every time
  // their content is accessed
  def addDynamicString(cpath: CanonPath, stringFun: => String) =
    addObject(new DynamicFile(cpath, () => stringToBytes(stringFun)))
  def addDynamicFile(cpath: CanonPath, bytesFun: => Array[Byte]) =
    addObject(new DynamicFile(cpath, () => bytesFun))
  def addEmptyFile(cpath: CanonPath) =
    addObject(new File(cpath, empty))

  // linked list of mappers and lookup function which traverses them
  def addMapper(m: Mapper): Unit =
    if (next.isEmpty) next = Some(m) else next.get.addMapper(m)
  def lookup(cpath: CanonPath): Option[FUSEObject] = 
    contents.get(cpath) orElse next.flatMap(_.lookup(cpath))
  
  override def toString(): String = contents.toString
}
