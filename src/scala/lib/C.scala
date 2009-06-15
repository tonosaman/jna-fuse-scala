package org.improving.fuse

import com.sun.jna._
import ptr._
import jnajava.C._
import jnajava.Stdio._

object Libc {
  final val LIBC: CLibrary = Native.loadLibrary("c", classOf[CLibrary]).asInstanceOf[CLibrary]
  
  abstract trait CLibrary extends Library {
    def chmod(path: String, mode: Short): Int
    def chown(path: String, owner: Int, group: Int): Int
    def close(fildes: Int): Int
    def closedir(dirp: StructDIR): Int
    def dup(fildes: Int): Int
    def dup2(fd1: Int, fd2: Int): Int
    def exchangedata(path1: String, path2: String, options: Int): Int   
    def fchmod(fildes: Int, mode: Short): Int
    def fchown(fildes: Int, owner: Int, group: Int): Int
    def fgetxattr(fd: Int, name: String, value: Pointer, size: Int, position: Int, options: Int): Int
    def flistxattr(fd: Int, namebuf: Pointer, size: Int, options: Int): Int    
    def fremovexattr(fd: Int, name: String, options: Int): Int
    def fsetxattr(fd: Int, name: String, value: String, size: Int, position: Int, options: Int): Int
    def fstat(filedes: Int, buf: StructStat): Int
    def fsync(fildes: Int): Int
    def ftruncate(fildes: Int, length: Long): Int
    def futimes(fildes: Int, times: Array[StructTimeval]): Int
    def getattrlist(path: String, attrlist: StructAttrlist, attrBuf: Pointer, attrBufSize: Int, options: Int): Int    
    def gettimeofday(tp: Array[StructTimeval], tzp: Pointer): Int
    def getxattr(path: String, name: String, value: Pointer, size: Int, position: Int, options: Int): Int
    def lchflags(path: String, flags: Int): Int
    def lchmod(path: String, flags: Short): Int    
    def lchown(path: String, owner: Int, group: Int): Int
    def link(path1: String, path2: String): Int
    def listxattr(path: String, namebuf: Pointer, size: Int, options: Int): Int
    def lstat(path: String, buf: StructStat): Int
    def mkdir(path: String, mode: Short): Int
    def mkfifo(path: String, mode: Short): Int
    def mknod(path: String, mode: Short, dev: Int): Int
    def open(path: String, oflag: Int): Int
    def open(path: String, oflag: Int, mode: Short): Int
    // def opendir(dirname: String): StructDIR
    def opendir(dirname: String): StructDIR.ByReference
    def pread(fd: Int, buf: Pointer, nbyte: Int, offset: Long): Int
    def pwrite(fd: Int, buf: Pointer, nbyte: Int, offset: Long): Int    
    def readdir(dirp: StructDIR): StructDirent
    def readlink(path: String, buf: Pointer, bufsize: Int): Int   
    def removexattr(path: String, name: String, options: Int): Int
    def rename(oldpath: String, newpath: String): Int
    def rmdir(path: String): Int
    def seekdir(dirp: StructDIR, loc: Int): Unit
    def setattrlist(path: String, attrlist: StructAttrlist, attrBuf: Pointer, attrBufSize: Int, options: Int): Int
    def setxattr(path: String, name: String, value: String, size: Int, position: Int, options: Int): Int
    def stat(path: String, buf: StructStat): Int
    def statvfs(path: String, buf: StructStatvfs): Int    
    def symlink(path1: String, path2: String): Int
    def telldir(dirp: StructDIR): Int
    def truncate(path: String, length: Long): Int
    def unlink(path: String): Int
    def utimes(path: String, times: Array[StructTimeval]): Int
    
    def read(fildes: Int, buf: Pointer, nbyte: Int): Int
    def write(fildes: Int, buf: CString, nbyte: Int): Int
    def mkdtemp(template: CString): Pointer
    def mkstemps(template: CString, suffixlen: Int): Int
    def mkstemp(template: CString): Int
    def daemon(nochdir: Int, noclose: Int): Int   
    
    def umask(cmask: Short): Short    
    def fdopen(fildes: Int, mode: String): StructFILE
    def fopen(filename: String, mode: String): StructFILE
    def freopen(filename: String, mode: String, stream: StructFILE): StructFILE    
    
    def sigaction(sig: Int, act: StructSigaction, oact: StructSigaction): Int
    def sigprocmask(how: Int, set: IntByReference, oset: IntByReference): Int   
    def sigaddset(set: IntByReference, signo: Int): Int
    def sigismember(set: IntByReference, signo: Int): Int

    // to eliminate
    def memmove(s1: Pointer, s2: Pointer, n: Int): Pointer
    def memcpy(s1: Pointer, s2: Pointer, n: Int): Pointer
    def memset(b: Pointer, c: Int, n: Int): Pointer
    
    // not exactly libc functions
    def errno(): Int = Native.getLastError    
  }
    
  object Conversions {
    implicit def javaStringToC(s: String): CString = new CString(s)
    implicit def cStringToJava(cs: CString): String = Native.toString(cs.buffer);
    implicit def cStringToPointer(cs: CString): Pointer = cs.getPointer;
    
    // by doing these conversions implicitly we avoid NPEs when the libc func returns null
    implicit def structDirentByRef(x: StructDirent): StructDirent.ByReference = 
      if (x == null) null else x.getByReference
    implicit def structDIRByRef(x: StructDIR): StructDIR.ByReference =
      if (x == null) null else x.getByReference
    implicit def richStructDIR(sd: StructDIR.ByReference): RichStructDIR = new RichStructDIR(sd)
    
    // timeval uses microseconds, timespec uses nanoseconds
    // XXX
    // implicit def timespecToTimeval(ts: StructTimespec): StructTimeval = {
    //   val tv = new StructTimeval
    //   tv.tv_sec = ts.tv_sec
    //   tv.tv_usec = ts.tv_nsec / 1000
    //   tv
    // }
    // implicit def timevalToTimespec(tv: StructTimeval): StructTimespec = {
    //   val ts = new StructTimespec
    //   ts.tv_sec = tv.tv_sec
    //   ts.tv_nsec = tv.tv_usec / 1000
    //   ts
    // }
  }
  
  class StructDirentIterator(val dp: StructDIR.ByReference) extends Iterator[StructDirent.ByReference]
  {
    import Conversions._
    private var latest: StructDirent.ByReference = null // head of the list
    private var latestRead: Boolean = false // has latest contents been accessed via next()
    dp.read     // don't touch!
  
    def ensureOffset(o: Int): Unit = 
      if (LIBC.telldir(dp) != o) {
        LIBC.seekdir(dp, o.toInt)
        latest = null
        latestRead = false
      }
  
    def hasNext(): Boolean = {
      if (latest == null || latestRead == true) {
        // avoid overwriting latest unless it's legit
        val entry = LIBC.readdir(dp)
        if (entry == null) return false
      
        latest = entry
        latestRead = false
      }
      
      latest != null
    }
  
    def next(): StructDirent.ByReference = {
      if (!hasNext)
        throw new Exception
      
      latest.read    // don't touch!
      latestRead = true
      latest
    }
  
    def offset(): Int = LIBC.telldir(dp)
    def stat(): StructStat = {
      val st = new StructStat
      st.st_ino = latest.d_ino
      st.st_mode = (latest.d_type << 12).toShort
      st
    }
  }
  

  class RichStructDIR(sd: StructDIR.ByReference) {
    def iterator(): StructDirentIterator = new StructDirentIterator(sd)
  }
  
  final val signals = List("SIGXXX",
    "SIGHUP", "SIGINT", "SIGQUIT", "SIGILL", "SIGTRAP", "SIGABRT", "SIGPOLL",
    "SIGFPE", "SIGKILL", "SIGBUS", "SIGSEGV", "SIGSYS", "SIGPIPE", "SIGALRM",
    "SIGTERM", "SIGURG", "SIGSTOP", "SIGTSTP", "SIGCONT", "SIGCHLD", "SIGTTIN",
    "SIGTTOU", "SIGIO", "SIGXCPU", "SIGXFSZ", "SIGVTALRM", "SIGPROF", "SIGWINCH",
    "SIGINFO", "SIGUSR1", "SIGUSR2"
  )
}