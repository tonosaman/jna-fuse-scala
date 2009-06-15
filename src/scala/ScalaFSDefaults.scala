package org.improving.fuse

import jnajava.C._
import jnajava.Fuse._
import FuseUtil._
import Errno._
import Fcntl._
import com.sun.jna._
import Mapper._

// some default callback implementations if you want
trait ScalaFSDefaults extends ScalaFS
{
  override def getattr(path: String, stbuf: StructStat): Int = {
    stbuf.clear
    if (lookup(path, stbuf).isEmpty) -ENOENT else 0
  }
  
  // default assumes read-only access
  override def open(path: String, fi: StructFuseFileInfo): Int = lookup(path) match {
    case None                                           => -ENOENT
    case Some(_) if (fi.flags & O_ACCMODE) != O_RDONLY  => -EACCES
    case Some(x: DynamicFile)                           => fi.setDirectIO ; 0
    case _                                              => 0
  }
      
  override def readdir(path: String, buf: Pointer, filler: FuseFillDirT, offset: Long, fi: StructFuseFileInfo): Int =
    lookup(path) match {
      case Some(x: Dir) => fillDirT(filler, buf, x.names.toList)
      case x            => -ENOENT
    }  
  
  override def read(path: String, buf: Pointer, size: Int, offset: Long, fi: StructFuseFileInfo): Int = {
    val file: File = lookup(path) match {
      case Some(x: File)  => if (offset >= x.size) return 0 else x
      case _              => return -ENOENT
    }
    val bytes = file.bytes
    val sizeToWrite: Int =
      if (offset + size > file.size) file.size - offset.toInt
      else size
    
    buf.write(0, bytes, offset.toInt, sizeToWrite)
    size
  }
}