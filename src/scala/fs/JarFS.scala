package org.improving.fuse

import com.sun.jna._
import ptr._
import Fuse._
import jnajava.C._
import jnajava.Fuse._
import Util._
import Errno._
import Fcntl._
import CSizes._
import java.util.jar._
import Libc.LIBC
import scala.collection.mutable.{ HashSet, HashMap, ListBuffer }
import Mapper._
import java.util.Date

object JarFS extends ScalaFS with ScalaFSDefaults with Attributes
{
  val name: String = "jarfs"
  val jarfspath: String =
    System.getenv("JARFSPATH") match { case null => throw new Exception("JARFSPATH not specified") ; case x => x }
  lazy val jarmapper: JarMapper = new JarMapper(jarfspath.split(":"))
  
  val JARATTR_PREFIX = "org.improving.fuse.Jar"
  val JARATTR_NAME = JARATTR_PREFIX + ".name"
  val JARATTR_COLLISIONS = JARATTR_PREFIX + ".collisions"
  
  class JarEntryFile(val source: JarSource, val entry: JarEntry)
  extends Mapper.File(CanonPath(entry.getName)) with Mapper.FUSEAttributes
  {
    override lazy val size: Int = entry.getSize.toInt
    override lazy val lastModTime: Long = entry.getTime / 1000 // in seconds
    override lazy val bytes: Array[Byte] = readBytes
    lazy val lastModStr = new Date(entry.getTime).toString
    
    // set extended attributes
    xattrs(JARATTR_NAME) = source.javaFile.getCanonicalPath
    
    private def readBytes(): Array[Byte] = {
      val in = source.jar getInputStream entry
      val count = entry.getSize.toInt
      val ret = new Array[Byte](count)
      var bytesRead = in.read(ret, 0, count)
      
      while (bytesRead < count) {
        val bytes = in.read(ret, bytesRead, count - bytesRead)
        if (bytes == -1)
          return null
          
        bytesRead += bytes
      }
      
      if (bytesRead == count) ret else null
    }
    def setCollisionList(s: String) = xattrs(JARATTR_COLLISIONS) = s
  }
    
  class JarDir(val source: JarSource, cpath: CanonPath)
  extends Mapper.Dir(cpath) with Mapper.FUSEAttributes
  {
    override lazy val lastModTime: Long = source.lastModTime
    xattrs(JARATTR_NAME) = source.javaFile.getCanonicalPath
  }    
  
  class JarMapper(cps: Seq[String]) extends Mapper {
    private var currentSource: JarSource = null // XXX
    override def newDir(cpath: CanonPath): JarDir = new JarDir(currentSource, cpath)
    
    private var _totalFiles: Int = 0
    private var _totalSize: Long = 0
    def totalFiles(): Int = _totalFiles
    def totalSize(): Long = _totalSize
    
    // open scope so hashmaps can be collected
    {
      val names = cps.flatMap(getFiles).map(_.getCanonicalPath).filter(_ endsWith ".jar").toList.removeDuplicates
      val collisions: HashMap[CanonPath, ListBuffer[JarEntryFile]] = 
        new HashMap[CanonPath, ListBuffer[JarEntryFile]] {
          override def default(key: CanonPath) = {
            this(key) = new ListBuffer[JarEntryFile]
            this(key)
          }
        }
      
      def addJarEntry(obj: JarEntryFile): Unit = {
        lookup(obj.path) match {
          // collision? if so go with the newer file
          case Some(x: JarEntryFile)  =>
            collisions(obj.cpath) ++= Set(x, obj)
            if (obj.lastModTime > x.lastModTime)
              addObject(obj)
          case _                      =>
            addObject(obj)
        }
      
        _totalFiles += 1
        _totalSize += obj.size
      }
      
      for (name <- names) {
        val source = new JarSource(name)
        currentSource = source
        source.directories.foreach { x => addObject(new JarDir(source, x)) }
        source.files.foreach { x => addJarEntry(new JarEntryFile(source, x)) }
      }
    
      // makes a /collisions dir with info about conflicts
      for ((cpath, set) <- collisions) {
        val contents =
          set.toList.sort((x, y) => x.lastModTime > y.lastModTime).
          map(x => x.source.jar.getName + " (" + x.lastModStr + ")\n").mkString
      
        addFile(cpath.prepend("/collisions"), contents)
      }
      
      // add xattr to the winner listing the losers
      for ((cpath, set) <- collisions) {
        val winner: JarEntryFile = lookup(cpath) match {
          case Some(x: JarEntryFile)  => x
          case _                      => throw new Exception("Unexpected file type")
        }
        val losers = set.toList.filter(_ ne winner).map(_.source.jarName).mkString(",")
        winner.setCollisionList(losers)
      }
    }
  }
    
  class JarSource(val path: String) {
    val javaFile = new java.io.File(path)
    val lastModTime = javaFile.lastModified / 1000    
    val jar = new JarFile(path)
    val jarName = CanonPath(jar.getName).file
    val files = new HashSet[JarEntry]
    val directories = new HashSet[CanonPath]
    
    // open scope
    {
      val entries = jar.entries
      while (entries.hasMoreElements) entries.nextElement match {
        case e if e.isDirectory => directories += CanonPath(e.getName)
        case e                  => files += e
      }
    }
  }
    
  override def statfs(path: String, buf: StructStatvfs): Int = {
    buf.clear
    buf.f_frsize = 512
    buf.f_files = jarmapper.totalFiles
    buf.f_blocks = (jarmapper.totalSize / 512).toInt
  }
  
  override def init(conn: StructFuseConnInfo): Pointer = {
    debug = true
    addMapper(jarmapper)
    
    // tell spotlight to back off
    jarmapper.addEmptyFile("/.metadata_never_index")
    
    null
  }
  
  // struct fuse *
  override def destroy(fuse: Pointer): Unit = {
    println("DESTROYED")
  }
  
}
