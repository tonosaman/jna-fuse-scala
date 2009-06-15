package org.improving.fuse

import com.sun.jna._
import Libc.LIBC
import jnajava.C._

object Stat
{
  def main(argv: Array[String]) = {
    // val x = new IntByReference
    // val res = LIBC.sigprocmask(0, null, x)
    // println(x + " = " + x.getValue)
    
    import Util._
    val x = new ptr.IntByReference
    val res = LIBC.sigprocmask(0, null, x)
    println("INIT: x = " + x.getValue)
    var sig = x.getValue
    for (i <- 0 to 31) {
      if (LIBC.sigismember(x, i) != 0)
        println("LIBC.sigismember(" + i + ") = true")
    }
    
    
    // val buf = new StructStat
    // val res = LIBC.stat(argv(0), buf)
    // println(buf)
    // println("stat result = " + res)
    // if (res < 0) println("errno = " + LIBC.errno())
    // val buf2 = new StructStatvfs
    // val res2 = LIBC.statvfs(argv(0), buf2)
    // println(buf2)
    // println("statvfs result = " + res2)
  }
}