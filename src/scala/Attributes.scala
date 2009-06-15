package org.improving.fuse

import jnajava.Fuse._
import Errno._

// Feature: New callbacks `setattr_x` and `fsetattr_x`; provides support for setting many attributes in a single call.
// Not only Mac OS X has a large number of settable attributes, heavy file system metadata activity, which is quite
// common and can occur behind the scenes, can generate a really large number of calls to set one or more attributes. In
// line with the "keeping things simple" philosophy, the MacFUSE API fans out a kernel-level `setattr` call into
// individual calls such as `chmod`, `chown`, `utimens`, `truncate`, `ftruncate`, and the newly introduced `chflags`,
// `setbkuptime`, and `setcrtime`. Depending on your user-space file system, you may really wish that you could handle
// all this in one call instead of receiving numerous back-to-back calls. `setattr_x` and `fsetattr_x` let you do that.
// *NOTE* that if you implement these calls, you will *NOT* receive *ANY* of the other "set" calls even if you do
// implement the latter. In other words, you will only receive `setattr_x` and `fsetattr_x`; the `chmod`, `chown`,
// `utimens`, `truncate`, `ftruncate`, `chflags`, `setcrtime`, and `setbkuptime` callbacks will never be called. (You
// must therefore handle everything at once.) Use this callback only if you *know* you need to use it. See the reference
// file system source (`loopbackc`) to see an example of how to use `[f]setattr_x`.

object Attributes {
  // implicits
  implicit def enrichStructSetattrX(x: StructSetattrX): RichStructSetattrX = new RichStructSetattrX(x)
  
  // macfuse loopback.c
  final val G_PREFIX              = "org"
  final val G_KAUTH_FILESEC_XATTR = G_PREFIX + ".apple.system.Security"
  final val A_PREFIX              = "com"
  final val A_KAUTH_FILESEC_XATTR = A_PREFIX + ".apple.system.Security"
  final val XATTR_APPLE_PREFIX    = "com.apple."
  private final val GETXATTR_ALT_NAME =
    G_PREFIX.substring(0, G_PREFIX.length - 1) + A_KAUTH_FILESEC_XATTR.substring(G_PREFIX.length - 1)

  // sys/attr.h
  final val FSOPT_NOFOLLOW      = 0x00000001
  final val ATTR_BIT_MAP_COUNT  = 5  
  final val ATTR_CMN_CRTIME     = 0x00000200
  final val ATTR_CMN_CHGTIME    = 0x00000800
  final val ATTR_CMN_BKUPTIME   = 0x00002000
  
  // sys/xattr.h
  final val XATTR_NOFOLLOW    = 0x0001    /* Don't follow symbolic links */
  final val XATTR_CREATE      = 0x0002    /* set the value, fail if attr already exists */
  final val XATTR_REPLACE     = 0x0004    /* set the value, fail if attr does not exist */
  final val XATTR_NOSECURITY  = 0x0008    /* bypass authorization checking */
    
  // XXX make sure this is doing whatever
  def kauthSanitize(name: String) = if (name == A_KAUTH_FILESEC_XATTR) GETXATTR_ALT_NAME else name
          
  class RichStructSetattrX(x: StructSetattrX) {
    private def isValid(bit: Int) = (x.valid & (1 << bit)) != 0

    def wantsMode() = isValid(0)
    def wantsUID() = isValid(1)
    def wantsGID() = isValid(2)
    def wantsSize() = isValid(3)
    def wantsAcctime() = isValid(4)
    def wantsModtime() = isValid(5)
    def wantsCrtime() = isValid(28)
    def wantsChgtime() = isValid(29)
    def wantsBkuptime() = isValid(30)
    def wantsFlags() = isValid(31)
  }
}

// these implementations simply delegate xattr calls to the individual FUSEObjects
trait Attributes extends ScalaFS
{
  import Attributes._
  import scala.collection.mutable.HashMap
  import com.sun.jna._
  import Util._
    
  override def listxattr(path: String, list: Pointer, size: Int): Int = {
    val obj = lookup(path) getOrElse (return -EACCES)
    val xattrs = obj.listxattr
    
    // if inbound pointer is null we return the length of the list so it can allocate
    if (xattrs.isEmpty || list == null) nullLength(xattrs) 
    else Util.listToNullSep(xattrs, list)
  }
  
  override def getxattr(path: String, name: String, value: Pointer, size: Int, position: Int): Int = {
    val obj = lookup(path) getOrElse (return -EACCES)
    obj.getxattr(name) match {
      case Some(x)  => if (value == null) x.length + 1 else setString(value, x)
      case None     => -ENOATTR
    }
  }

  override def setxattr(path: String, name: String, value: String, size: Int, flags: Int, position: Int): Int = {
    val obj = lookup(path) getOrElse (return -EACCES)
    
    if (flags.hasFlag(XATTR_CREATE) && obj.getxattr(name).isDefined) -EEXIST
    else if (flags.hasFlag(XATTR_REPLACE) && obj.getxattr(name).isEmpty) -ENOATTR
    else if (!obj.setxattr(name, value)) -ENOTSUP
    else 0
  }
}
