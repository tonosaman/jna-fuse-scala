package org.improving.fuse

import com.sun.jna._
import jnajava.C._
import jnajava.Fuse._
import Errno._
import CSizes._
import Libc._
import Libc.Conversions._
import Attributes._
import FuseUtil._
import scala.collection.mutable.HashMap
import Mapper._

class LoopbackFS extends ScalaFS
{
  val name: String = "loopbackfs"
  debug = true
  
  object Implicits {
    implicit def fildesToDirp(fh: Long): StructDIR.ByReference = dirps(fh).dp
        
    implicit def unboxShort(s: ShortSize): Short = s.intValue.toShort
    implicit def unboxInt(s: IntSize): Int = s.intValue
    implicit def unboxLong(s: LongSize): Long = s.longValue
    
    // XXX not a good idea...
    implicit def whatCouldPossiblyGoWrong(fh: Long): Int = fh.toInt
  }
  import Implicits._

  // during the window between opendir and releasedir, we associate some info with the fd for readdir.
  // In the C version this is done by stuffing a pointer into a long - rather than messing with the
  // rather painful JNA unions we'll just track some short-term global data.
  val dirps = new HashMap[Long, StructDirentIterator]
  
  // we might want to loopback something other than "/"
  val root: String = System.getenv("LOOPBACKFS_ROOT") match { case null => "" ; case x => x }
  def translate(path: String) = if (root == "") path else if (root endsWith "/") root + path else root + "/" + path
  
  // avoid infinite loop if we look at e.g. /mnt/mnt
  private def isLoopbackLoop(cpath: CanonPath) =
    (cpath == mountpoint) || (cpath.append("/") == mountpoint) || (cpath.dir startsWith mountpoint)
    
  /**** Callbacks ****/
  
  override def getattr(path: String, stbuf: StructStat): Int =
    if (isLoopbackLoop(path)) -ENOENT  
    else unlessErrorZero(LIBC.lstat(translate(path), stbuf))
    
  override def fgetattr(path: String, stbuf: StructStat, fi: StructFuseFileInfo): Int =
    unlessErrorZero(LIBC.fstat(fi.fh, stbuf))
  
  override def readlink(path: String, buf: Pointer, size: Int): Int = {    
    val res = LIBC.readlink(translate(path), buf, size - 1)
    unlessError(res, { buf.setChar(res, '\0') ; 0 })
  }
  
  override def opendir(path: String, fi: StructFuseFileInfo): Int = {  
    if (isLoopbackLoop(path)) return -ENOENT
    val sd = LIBC.opendir(translate(path))
    sd.read   // XXX don't touch!
    dirps(fi.fh) = sd.iterator
  }
  
  override def readdir(path: String, buf: Pointer, filler: FuseFillDirT, offset: Long, fi: StructFuseFileInfo): Int = {
    val cpath = CanonPath(path)
    val dirp = dirps(fi.fh)
    dirp.ensureOffset(offset)

    while(dirp.hasNext) {
      val entry = dirp.next
      entry.read  // XXX don't touch!
      val d_name = Native.toString(entry.d_name)
      
      if (isLoopbackLoop(cpath.append(d_name)))
        DBGLN("Hiding " + d_name + " from directory listing because it's the mountpoint.")
      else if (filler.invoke(buf, d_name, dirp.stat, dirp.offset) != 0)
        return 0
    }
        
    return 0  
  }
  
  override def releasedir(path: String, fi: StructFuseFileInfo): Int = {
    LIBC.closedir(dirps(fi.fh).dp)
    dirps -= fi.fh
  }
  override def release(path: String, fi: StructFuseFileInfo): Int = LIBC.close(fi.fh)  

  override def mknod(path: String, mode: Short, rdev: Int): Int = 
    unlessErrorZero(
      if (S_ISFIFO(mode)) LIBC.mkfifo(translate(path), mode)
      else LIBC.mknod(translate(path), mode, rdev)
    )

  override def mkdir(path: String, mode: Short): Int =  unlessErrorZero(LIBC.mkdir(translate(path), mode))
  override def unlink(path: String): Int =              unlessErrorZero(LIBC.unlink(translate(path)))
  override def rmdir(path: String): Int =               unlessErrorZero(LIBC.rmdir(translate(path)))
  override def symlink(from: String, to: String): Int = unlessErrorZero(LIBC.symlink(from, to))
  override def rename(from: String, to: String): Int =  unlessErrorZero(LIBC.rename(from, to))
  override def link(from: String, to: String): Int =    unlessErrorZero(LIBC.link(from, to))

  override def exchange(oldpath: String, newpath: String, flags: Int): Int =
    unlessErrorZero(LIBC.exchangedata(translate(oldpath), translate(newpath), flags))

  override def open(path: String, fi: StructFuseFileInfo): Int = {
    val fd = LIBC.open(translate(path), fi.flags)
    unlessError(fd, { fi.fh = fd ; 0 })
  }
  
  override def read(path: String, buf: Pointer, size: Int, offset: Long, fi: StructFuseFileInfo): Int =
    unlessErrorArg(LIBC.pread(fi.fh, buf, size, offset))
  
  override def write(path: String, buf: Pointer, size: Int, offset: Long, fi: StructFuseFileInfo): Int =
    unlessErrorArg(LIBC.pwrite(fi.fh, buf, size, offset))

  override def statfs(path: String, buf: StructStatvfs): Int =
    unlessErrorZero(LIBC.statvfs(translate(path), buf))

  override def create(path: String, mode: Short, fi: StructFuseFileInfo): Int = {
    val fd = LIBC.open(translate(path), fi.flags, mode)
    unlessError(fd, { fi.fh = fd ; 0 })
  }
  
  override def flush(path: String, fi: StructFuseFileInfo): Int =
    unlessErrorZero(LIBC.close(LIBC.dup(fi.fh)))  
  
  override def fsync(path: String, datasync: Int, fi: StructFuseFileInfo): Int =
    unlessErrorZero(LIBC.fsync(fi.fh))
    
  override def setxattr(path: String, name: String, value: String, size: Int, flags: Int, position: Int): Int = {
    val fs = if (name startsWith XATTR_APPLE_PREFIX) flags & ~XATTR_NOSECURITY else flags
    unlessErrorZero(LIBC.setxattr(translate(path), kauthSanitize(name), value, size, position, fs))
  }  

  override def listxattr(path: String, list: Pointer, size: Int): Int = {
    def listSize(xs: List[String]) = xs.map(_.length + 1).foldLeft(0)(_+_)
    def processList(): Int = {
      val res: Int = LIBC.listxattr(translate(path), list, size, XATTR_NOFOLLOW)
      if (res == -1) return -1
      
      val xs = Util.listFromNullSep(list, res)
      val (front, back) = xs.span(_ != G_KAUTH_FILESEC_XATTR)
      if (front.length != xs.length) {
        val index = listSize(front)
        LIBC.memmove(list.share(index), list.share(index + back.head.length + 1), listSize(back.tail))
      }
      res
    }
    
    unlessErrorArg(processList)
  }
  override def getxattr(path: String, name: String, value: Pointer, size: Int, position: Int): Int = {
    printSigMask
    unlessErrorArg(LIBC.getxattr(translate(path), kauthSanitize(name), value, size, position, XATTR_NOFOLLOW))
  }
    
  override def removexattr(path: String, name: String): Int =
    unlessErrorZero(LIBC.removexattr(translate(path), kauthSanitize(name), XATTR_NOFOLLOW))
    
  override def getxtimes(path: String, bkuptime: StructTimespec, crtime: StructTimespec): Int = {
    val stSize = bkuptime.size
    val attributes = new StructAttrlist
    val buf = new StructXtimeattrbuf
    attributes.bitmapcount = ATTR_BIT_MAP_COUNT
    
    def getList(attr: Int, mem: Pointer) = {
      attributes.commonattr = attr
      buf.write
      val res = LIBC.getattrlist(translate(path), attributes, buf.getPointer, buf.size, FSOPT_NOFOLLOW)
      buf.read

      if (res == 0) LIBC.memcpy(mem, buf.xtime.getPointer, stSize)
      else LIBC.memset(mem, 0, stSize)
    }
    
    getList(ATTR_CMN_BKUPTIME, bkuptime.getPointer)
    getList(ATTR_CMN_CRTIME, crtime.getPointer)
    0
  }
  
  override def init(conn: StructFuseConnInfo): Pointer = {
    conn.enableSETVOLNAME
    conn.enableXTIMES
    
    printSigMask    
    null
  }
  
  private def printSigMask(): Unit = {
    import Util._
    val x = new ptr.IntByReference
    val res = LIBC.sigprocmask(0, null, x)
    println("INIT: x = " + x.getValue)
    var sig = x.getValue
    for (i <- 0 to 31) {
      if (LIBC.sigismember(x, i) != 0)
        DBG(signals(i) + ", ")
    }
    DBGLN()
  }
    
  override def setattr_x(path: String, attr: StructSetattrX): Int = fsetattr_x(translate(path), attr, null)  
  override def fsetattr_x(path: String, attr: StructSetattrX, fi: StructFuseFileInfo): Int = {
    if (attr.wantsMode)
      if (LIBC.lchmod(translate(path), attr.mode) == -1) return -errno
    
    val uid: Int = if (attr.wantsUID) attr.uid else -1
    val gid: Int = if (attr.wantsGID) attr.gid else -1
    if (uid != -1 || gid != -1)
      if (LIBC.lchown(translate(path), uid, gid) == -1) return -errno
      
    if (attr.wantsSize) {
      val res = 
        if (fi != null) LIBC.ftruncate(fi.fh, attr.size)
        else LIBC.truncate(translate(path), attr.size)
      if (res == -1) return -errno
    }
    
    if (attr.wantsModtime) {
      val tv = (new StructTimeval).toArray(2).asInstanceOf[Array[StructTimeval]]
      def setTV(lhs: StructTimeval, rhs: StructTimespec) = {
        lhs.tv_sec = rhs.tv_sec
        lhs.tv_usec = rhs.tv_nsec / 1000
        lhs.write
      }
      if (!attr.wantsAcctime) LIBC.gettimeofday(tv, null)
      else setTV(tv(0), attr.acctime)
      
      setTV(tv(1), attr.modtime)
      if (LIBC.utimes(translate(path), tv) == -1) return -errno
    }
    
    def setAttrList(anAttr: Int): Int = {
      val attributes = new StructAttrlist
      val struct = anAttr match {
        case ATTR_CMN_CRTIME  => attr.crtime
        case ATTR_CMN_CHGTIME => attr.chgtime
        case ATTR_CMN_BKUPTIME => attr.bkuptime
      }
      attributes.bitmapcount = ATTR_BIT_MAP_COUNT
      attributes.commonattr = anAttr
      attributes.write
      LIBC.setattrlist(translate(path), attributes, struct.getPointer, struct.size, FSOPT_NOFOLLOW)
    }
    
    if (attr.wantsCrtime && setAttrList(ATTR_CMN_CRTIME) == -1) return -errno
    if (attr.wantsChgtime && setAttrList(ATTR_CMN_CHGTIME) == -1) return -errno    
    if (attr.wantsBkuptime && setAttrList(ATTR_CMN_BKUPTIME) == -1) return -errno
    if (attr.wantsFlags && LIBC.lchflags(translate(path), attr.flags) == -1) return -errno
    
    attr.write
    0
  }
}

object Loopback {
  def main(args: Array[String]) = (new LoopbackFS).main(args)
}