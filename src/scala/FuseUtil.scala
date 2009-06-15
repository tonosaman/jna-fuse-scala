package org.improving.fuse

import com.sun.jna._
import ptr._
import jnajava.C._
import jnajava.Fuse._
import jnajava.Stdio._
import java.io.File

object FuseUtil
{
  val DEFAULT_FUSE_OPTS = "allow_other,direct_io,nobrowse,nolocalcaches,ro,iosize=1048576,volname=ProcFS"
  
  // calls filler callback on each given string
  def fillDirT(filler: FuseFillDirT, buf: Pointer, xs: List[String]): Int = {
    xs foreach { filler.invoke(buf, _, null, 0) }
    0
  }
  
  // returns list of options specified via -o
  def fsOptions(argv: Array[String]): List[String] = {
    println(argv)
    val ix = argv.findIndexOf(_ == "-o")
    if (ix == -1 || (ix + 1) >= argv.length) Nil
    else argv(ix + 1).split(",").toList
  }
  
  // #define  S_ISBLK(m)  (((m) & 0170000) == 0060000)  /* block special */
  // #define  S_ISCHR(m)  (((m) & 0170000) == 0020000)  /* char special */
  // #define  S_ISDIR(m)  (((m) & 0170000) == 0040000)  /* directory */
  // #define  S_ISFIFO(m) (((m) & 0170000) == 0010000)  /* fifo or socket */
  // #define  S_ISREG(m)  (((m) & 0170000) == 0100000)  /* regular file */
  // #define  S_ISLNK(m)  (((m) & 0170000) == 0120000)  /* symbolic link */
  // #define  S_ISSOCK(m) (((m) & 0170000) == 0140000)  /* socket */
  // #if !defined(_POSIX_C_SOURCE) || defined(_DARWIN_C_SOURCE)
  // #define  S_ISWHT(m)  (((m) & 0170000) == 0160000)  /* whiteout */
  // #define S_ISXATTR(m) (((m) & 0200000) == 0200000)  /* extended attribute */
  // #endif

  def S_ISFIFO(m: Int) = (m & 0170000) == 0010000
}