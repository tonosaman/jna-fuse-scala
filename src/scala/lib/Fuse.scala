package org.improving.fuse

import com.sun.jna._
import jnajava.C._
import jnajava.Fuse._
import java.util.{ Map, HashMap }
import jnajava.Fuse.Operations
import java.{ lang => jl }
import jl.reflect.{ Method, InvocationHandler }

object Fuse {
  // fuse_main is implemented as a macro:
  //   #define fuse_main(argc, argv, op, user_data) fuse_main_real(argc, argv, op, sizeof(*(op)), user_data)
  final val invocationMapper: InvocationMapper = new InvocationMapper() {
    def getInvocationHandler(lib: NativeLibrary, m: Method): InvocationHandler = {
      if (m.getName != "fuse_main") null
      else {
        val f: Function = lib getFunction "fuse_main_real"
        
        new InvocationHandler {
          def invoke(proxy: Object, method: Method, args: Array[Object]): Object = {
            val newArgs = new Array[Object](5)
            newArgs(0) = args(0).asInstanceOf[jl.Integer]
            newArgs(1) = args(1).asInstanceOf[Array[String]]
            newArgs(2) = args(2)
            newArgs(3) = new jl.Integer(args(2).asInstanceOf[Operations].size)
            newArgs(4) = args(3)
            
            new jl.Integer(f invokeInt newArgs)
            // 
            // int argc = (Integer)args[0];
            // String[] argv = (String[])args[1];
            // Object[] newArgs = new Object[5];
            // FuseLibrary.FuseOperations fops = (FuseLibrary.FuseOperations)args[2];
            // newArgs[0] = new Integer(argc);
            // newArgs[1] = argv;
            // newArgs[2] = args[2];
            // newArgs[3] = ((FuseLibrary.FuseOperations)args[2]).size();
            // newArgs[4] = args[3];
            // 
            // return f.invokeInt(newArgs);
          }
        }
      }
    }
  }
  
  final val libOptions: java.util.Map[String, Object] =
    new java.util.HashMap[String, Object]
  libOptions.put(Library.OPTION_INVOCATION_MAPPER, invocationMapper)
  
  final val LIBFUSE: FuseLibrary = 
    Native.loadLibrary("fuse", classOf[FuseLibrary], libOptions).asInstanceOf[FuseLibrary]
    
  abstract trait FuseLibrary extends Library {
    def fuse_main(argc: Int, argv: Array[String], op: Operations, user_data: Pointer)
    def fuse_get_context(): StructFuseContext
  }
}


// package org.improving.fuse;
// 
// import static org.improving.fuse.CommonStruct.*;
// import static org.improving.fuse.CommonFuse.*;
// import com.sun.jna.*;
// import java.util.HashMap;
// import java.util.Map;
// 
// public interface FuseLibrary extends Library {
//   FuseLibrary LIBFUSE = (FuseLibrary)Native.loadLibrary("fuse", FuseLibrary.class, Fuse.libOptions);
//   // FuseLibrary SYNC_INSTANCE = (FuseLibrary)Native.synchronizedLibrary(LIBFUSE);
// 
//   // public static class ScalaCallback extends Structure   { public Callback cb; }
//   public static class FuseOperations extends Structure  {
//     public ScalaCallback[] callbacks = new ScalaCallback[FUSE_OPERATION_CALLBACKS];
//   }
// 
//   // int fuse_main(int argc, String argv[], const struct fuse_operations *op, void *user_data);
//   public int fuse_main(int argc, String argv[], FuseOperations op, Pointer user_data);
//   
//   // struct fuse_context *fuse_get_context(void);
//   public StructFuseContext fuse_get_context();
// }