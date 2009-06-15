package org.improving.fuse

import java.lang.reflect.Method
import com.sun.jna._
import GeneratedFuse.callbackNames
import jnajava.Fuse._
import jnajava.C._
// import FuseLibrary.FuseOperations
import jnajava.Fuse._
import CSizes.Boxable

object ScalaFSCallbacks {  
  // when our arguments are in JNA descended types (e.g. size_t) they need to be put into e.g. Integer boxes.
  private def boxArg(arg: Object): Object = arg match {
    case x: Boxable[_]  => x.box
    case _              => arg
  } 
  private def readStruct(arg: Object): Unit = arg match {
    case x: Structure   => x.read
    case _              =>
  }
  private def writeStruct(arg: Object): Unit = arg match {
    case x: Structure   => x.write
    case _              =>
  }
  
  def setupCallbacks(declaredMethods: Array[Method], self: AnyRef): Operations = {
    def doCallback[T](args: Object*)(implicit m: Method): T = {
      args foreach readStruct
      val ret: T = m.invoke(self, args.map(boxArg): _*).asInstanceOf[T]
      args foreach writeStruct
      ret
    }
    
    val fops = new Operations
    
    implicit def mkScalaCallback(x: Callback): ScalaCallback = {
      val sb = new ScalaCallback 
      sb.cb = x
      sb
    }
      
    implicit def boxNum[T <: AnyVal](x: T) = new AnyRef {
      def box(): java.lang.Object = x match {
        case x: Short   => new java.lang.Short(x)
        case x: Int     => new java.lang.Integer(x)
        case x: Long    => new java.lang.Long(x)
      }
    }
    
    // because certain scala techniques lead to other methods appearing in the Callback object,
    // to be safe we need to always call the method "callback" so JNA can identify it.
    for (m <- declaredMethods ; val name = m.getName ; val idx = callbackNames.findIndexOf(_ == name) ; if idx > -1) {
      implicit val currentMethod = m
      fops.callbacks(idx) = name match {
        case "init"     => new Callback {
          def callback(conn: StructFuseConnInfo): Pointer = doCallback[Pointer](conn)
        }
        case "destroy"  => new Callback {
          def callback(sf: Pointer): Unit = doCallback[Unit](sf)
        }
        case "getattr"  => new Callback {
          def callback(path: String, stbuf: StructStat): Int = doCallback[Int](path, stbuf)
        }
        case "fgetattr" => new Callback {
          def callback(path: String, stbuf: StructStat, fi: StructFuseFileInfo): Int =
            doCallback[Int](path, stbuf, fi)
        }
        case "setxattr" => new Callback {
          def callback(path: String, name: String, value: String, size: Int, flags: Int, position: Int): Int =
            doCallback[Int](path, name, value, size.box, flags.box, position.box)
        }
        case "getxattr" => new Callback {
          def callback(path: String, name: String, value: Pointer, size: Int, position: Int): Int =
            doCallback[Int](path, name, value, size.box, position.box)
        }
        case "listxattr" => new Callback {
          def callback(path: String, list: Pointer, size: Int): Int =
            doCallback[Int](path, list, size.box)
        }
        case "removexattr" => new Callback {
          def callback(path: String, name: String): Int = doCallback[Int](path, name)
        }
        case "getxtimes" => new Callback {
          def callback(path: String, bkuptime: StructTimespec, crtime: StructTimespec): Int =
            doCallback[Int](path, bkuptime, crtime)
        }
        case "setattr_x" => new Callback {
          def setattr_x(path: String, attr: StructSetattrX): Int = doCallback[Int](path, attr)
        }
        case "fsetattr_x" => new Callback {
          def fsetattr_x(path: String, attr: StructSetattrX, fi: StructFuseFileInfo): Int =
            doCallback[Int](path, attr, fi)
        }
        case "open" => new Callback {
          def callback(path: String, fi: StructFuseFileInfo): Int = doCallback[Int](path, fi)
        }
        case "create" => new Callback {
          def callback(path: String, mode: Short, fi: StructFuseFileInfo): Int = doCallback[Int](path, mode.box, fi)
        }
        case "read" | "write" => new Callback {
          def read(path: String, buf: Pointer, size: Int, off: Long, fi: StructFuseFileInfo): Int =
            doCallback[Int](path, buf, size.box, off.box, fi)
        }
        case "readdir" => new Callback {
          def callback(path: String, buf: Pointer, filler: FuseFillDirT, off: Long, fi: StructFuseFileInfo): Int = 
            doCallback[Int](path, buf, filler, off.box, fi)
        }
        case "readlink" => new Callback {
          def callback(path: String, buf: Pointer, size: Int): Int =
            doCallback[Int](path, buf, size.box)
        }
        case "chmod" => new Callback {
          def callback(path: String, mode: Short): Int = doCallback[Int](path, mode.box)
        }
        case "chown" => new Callback {
          def callback(path: String, uid: Int, gid: Int): Int = doCallback[Int](path, uid.box, gid.box)
        }
        case "truncate" => new Callback {
          def callback(path: String, size: Long): Int = doCallback[Int](path, size.box)
        }
        case "utime" => new Callback {
          def callback(path: String, times: Pointer): Int = doCallback[Int](path, times)
        }
        case "statfs" => new Callback {
          def callback(path: String, buf: StructStatvfs): Int = doCallback[Int](path, buf)
        }
        case "access" => new Callback {
          def callback(path: String, mask: Int): Int = doCallback[Int](path, mask.box)
        }
        case "opendir" => new Callback {
          def callback(path: String, fi: StructFuseFileInfo): Int = doCallback[Int](path, fi)
        }
        case "mkdir" => new Callback {
          def callback(path: String, mode: Short): Int = doCallback[Int](path, mode.box)
        }
        case "release" | "releasedir" => new Callback {
          def callback(path: String, fi: StructFuseFileInfo): Int = doCallback[Int](path, fi)
        }
        case "mknod" => new Callback {
          def callback(path: String, mode: Short, rdev: Int): Int = doCallback[Int](path, mode.box, rdev.box)
        }
        case "link" => new Callback {
          def callback(oldpath: String, newpath: String): Int = doCallback[Int](oldpath, newpath)
        }
        case "unlink" | "rmdir" => new Callback {
          def callback(path: String): Int = doCallback[Int](path)
        }
        case "symlink" | "rename" => new Callback {
          def callback(from: String, to: String): Int = doCallback[Int](from, to)
        }
        case "exchange" => new Callback {
          def callback(oldpath: String, newpath: String, flags: NativeLong): Int =
            doCallback[Int](oldpath, newpath, flags.intValue.box)
        }        
        case "flush" => new Callback {
          def callback(path: String, fi: StructFuseFileInfo): Int = doCallback[Int](path, fi)
        }
        case "fsync" => new Callback {
          def callback(path: String, datasync: Int, fi: StructFuseFileInfo): Int =
            doCallback[Int](path, datasync.box, fi)
        }
        
        case _ => throw new Exception("No callback available for method: " + name)
      }
    }
    
    fops
  }
}