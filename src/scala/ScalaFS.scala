package org.improving.fuse

import com.sun.jna._
import scala.collection.mutable.HashMap
import Mapper._
import jnajava.C._
import jnajava.Fuse._
import Fuse.LIBFUSE

// callback prototypes are mixed in so we can get compile-time checking on our overrides
abstract class ScalaFS extends GeneratedFuse.Operations {
  self: ScalaFS =>  
  
  val name: String
  var debug = false
  private[this] var _mountpoint = ""
  final lazy val mountpoint: CanonPath =
    if (_mountpoint == "") null else CanonPath(_mountpoint)
    
  private[this] var mapper: Mapper = null  // list of mappers to try for a path
  def addMapper(m: Mapper) = {
    if (mapper == null) mapper = m else mapper.addMapper(m)
    DBGLN("Added mapper: " + m)
  }
  def lookup(path: String): Option[FUSEObject] =
    if (mapper == null) throw new Exception("No mappers defined")
    else mapper.lookup(path)
  def lookup(path: String, stbuf: StructStat): Option[FUSEObject] = {
    val ret = lookup(path)
    ret.map(_.stat(stbuf))
    ret
  }
  
  def DBG(msgs: Any*): Unit = if (debug) for (m <- msgs) print(m)
  def DBGLN(msgs: Any*): Unit = DBG(msgs ++ Array("\n"): _*)
  
  // frequently occuring patterns
  def unlessErrorZero(cond: Int): Int =         unlessError(cond, 0)
  def unlessErrorArg(cond: Int): Int =          if (cond == -1) -errno else cond
  def unlessError(cond: Int, ok: => Int): Int = if (cond == -1) -errno else ok
  
  // avoid having to return 0 if we're lazy (XXX this is a bad idea)
  implicit def unitToInt(x: Unit): Int = 0
  
  // move these implicits later XXX
  implicit def enrichFuseFileInfo(x: StructFuseFileInfo) = new RichStructFuseFileInfo(x)
  
  // it's more unixy
  def errno(): Int = Native.getLastError
  
  // peering outside fuseland
  private def getContext() = LIBFUSE.fuse_get_context
  def getUID(): Int = getContext.uid
  def getGID(): Int = getContext.gid
  def getPID(): Int = getContext.pid
      
  def start(argv: Array[String]): Int = start(argv, null)
  def start(argv: Array[String], data: Pointer): Int = {
    // save errno
    Native.getPreserveLastError
    
    // we use getDeclaredMethods to limit our involvement to fuse_operations methods that are overridden
    val declaredMethods = this.getClass.getDeclaredMethods
    
    // set up callback structure
    val fops = ScalaFSCallbacks.setupCallbacks(declaredMethods, self)
    
    // XXX full option parsing
    _mountpoint = argv.last + "/"
    
    // invoke fuse
    LIBFUSE.fuse_main(argv.length, argv, fops, data)
  }
  
  // default main
  def main(argv: Array[String]): Unit = start(Array(name) ++ argv)
}  
