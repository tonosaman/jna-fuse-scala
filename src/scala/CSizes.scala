package org.improving.fuse

import com.sun.jna.IntegerType

object CSizes {
  import java.{ lang => jl }
  
  abstract class Boxable[T <: Object](val sizeInBytes: Int) extends IntegerType(sizeInBytes) { def box: T }
  class ShortSize extends Boxable[jl.Short](2)    { def box = new jl.Short(this.intValue.toShort) }
  class IntSize extends Boxable[jl.Integer](4)    { def box = new jl.Integer(this.intValue) }
  class LongSize extends Boxable[jl.Long](8)      { def box = new jl.Long(this.longValue) }
  
  class dev_t extends IntSize
  class ino_t extends IntSize
  class mode_t extends ShortSize
  class nlink_t extends ShortSize
  class uid_t extends IntSize
  class gid_t extends IntSize
  class time_t extends IntSize
  class off_t extends LongSize
  class blkcnt_t extends LongSize
  class blksize_t extends IntSize
  class __uint32_t extends IntSize
  class __int32_t extends IntSize
  class __int64_t extends LongSize
  class size_t extends IntSize
  class pid_t extends IntSize
  class fuse_dirh_t extends IntSize
  class fuse_dirfil_t extends IntSize
  class fuse_fill_dir_t extends IntSize
  class int32_t extends IntSize
  class uint32_t extends IntSize
  
  def sizeOf(s: String): Int = {
    try {
      // val clazz = Class.forName("CSizes$" + s)
      val clazz = Class.forName("org.improving.fuse.CSizes$" + s)
      clazz.newInstance.asInstanceOf[Boxable[_]].sizeInBytes
    }
    catch {
      case e: Exception => -1
    }
  }
}