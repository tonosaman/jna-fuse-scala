package org.improving.fuse.test

import _root_.org.specs._
import jnajava.C._
import java.nio.{ CharBuffer, ByteBuffer }

object libcSpec extends Specification {
  import com.sun.jna.{ Native, Memory }
  import Libc.LIBC
  import Libc.Conversions._
  import Fcntl._
  import jnajava.Stdio._
    
  // def umask(cmask: Short): Short    
  // def fdopen(fildes: Int, mode: String): StructFILE
  // def fopen(filename: String, mode: String): StructFILE
  // def freopen(filename: String, mode: String, stream: StructFILE): StructFILE
  // 
  
  // def chmod(path: String, mode: Short): Int
  // def chown(path: String, owner: Int, group: Int): Int
  // def close(fildes: Int): Int
  // def closedir(dirp: StructDIR): Int
  // def dup(fildes: Int): Int
  // def dup2(fd1: Int, fd2: Int): Int
  // def exchangedata(path1: String, path2: String, options: Int): Int   
  // def fchmod(fildes: Int, mode: Short): Int
  // def fchown(fildes: Int, owner: Int, group: Int): Int
  // def fgetxattr(fd: Int, name: String, value: Pointer, size: Int, position: Int, options: Int): Int
  // def flistxattr(fd: Int, namebuf: Pointer, size: Int, options: Int): Int    
  // def fremovexattr(fd: Int, name: String, options: Int): Int
  // def fsetxattr(fd: Int, name: String, value: String, size: Int, position: Int, options: Int): Int
  // def fstat(filedes: Int, buf: StructStat): Int
  // def fsync(fildes: Int): Int
  // def ftruncate(fildes: Int, length: Long): Int
  // def futimes(fildes: Int, times: Array[StructTimeval]): Int
  // def getattrlist(path: String, attrlist: StructAttrlist, attrBuf: Pointer, attrBufSize: Int, options: Int): Int    
  // def gettimeofday(tp: Array[StructTimeval], tzp: Pointer): Int
  // def getxattr(path: String, name: String, value: Pointer, size: Int, position: Int, options: Int): Int
  // def lchflags(path: String, flags: Int): Int
  // def lchmod(path: String, flags: Short): Int    
  // def lchown(path: String, owner: Int, group: Int): Int
  // def link(path1: String, path2: String): Int
  // def listxattr(path: String, namebuf: Pointer, size: Int, options: Int): Int
  // def lstat(path: String, buf: StructStat): Int
  // def mkdir(path: String, mode: Short): Int
  // def mkfifo(path: String, mode: Short): Int
  // def mknod(path: String, mode: Short, dev: Int): Int
  // def open(path: String, oflag: Int): Int
  // def open(path: String, oflag: Int, mode: Short): Int
  // def opendir(dirname: String): StructDIR
  // def pread(fd: Int, buf: Pointer, nbyte: Int, offset: Long): Int
  // def pwrite(fd: Int, buf: Pointer, nbyte: Int, offset: Long): Int    
  // def readdir(dirp: StructDIR): StructDirent
  // def readlink(path: String, buf: Pointer, bufsize: Int): Int   
  // def removexattr(path: String, name: String, options: Int): Int
  // def rename(oldpath: String, newpath: String): Int
  // def rmdir(path: String): Int
  // def seekdir(dirp: StructDIR, loc: Int): Unit
  // def setattrlist(path: String, attrlist: StructAttrlist, attrBuf: Pointer, attrBufSize: Int, options: Int): Int
  // def setxattr(path: String, name: String, value: String, size: Int, position: Int, options: Int): Int
  // def statvfs(path: String, buf: StructStatvfs): Int    
  // def symlink(path1: String, path2: String): Int
  // def telldir(dirp: StructDIR): Int
  // def truncate(path: String, length: Long): Int
  // def unlink(path: String): Int
  // def utimes(path: String, times: Array[StructTimeval]): Int
  // 
  
  lazy val tmpdir = LIBC.mkdtemp("/tmp/scuse.XXXXXX").getString(0)
  def mkTmpfile(): String = {
    val cs = new CString(tmpdir + "/file.XXXXXX")
    LIBC.mkstemp(cs)
    cs
  }
  
  "create a temp directory" in {
    tmpdir must beADirectoryPath
  }
  
  "create and unlink a file" in {
    val path = mkTmpfile
    val ret = LIBC.open(path, O_CREAT, 0664)
    ret must_!= -1
    path must beAnExistingPath
    LIBC.unlink(path) must_== 0
    path must not(beAnExistingPath)
  }
  
  "open a directory and print the files contained within" in {
    val dirp = LIBC.opendir("/tmp")
    // dirents(dirp) foreach { x => println(Native.toString(x.d_name)) }
    LIBC.closedir(dirp) must_== 0
  }
  
  "output a file and read it back" in {
    val path = mkTmpfile
    val cs = new CString(path)
    
    // create a temporary file
    val fd = LIBC.open(path, O_RDWR | O_CREAT)
    fd must_!= -1
    path must beAFilePath
    
    // write its name to it and close it
    LIBC.write(fd, cs, path.length) must_== path.length
    LIBC.close(fd) must_== 0
    
    // clear the C string memory and read the file contents back into it
    cs.clear
    val fd2 = LIBC.open(path, O_RDONLY)
    fd2 must_!= -1
    LIBC.read(fd2, cs, path.length) must_== path.length
    
    // ascertain write == read
    cs.toString must_== path
  }
  
  // object LibcTest {
  //   def main(argv: Array[String]): Unit = {
  //     val ss = new StructStat
  //     Libc.INSTANCE.lstat("/etc/passwd", ss)
  //     println(ss.toString)
  //   }
  // }  
}