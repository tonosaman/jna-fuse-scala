// package org.improving.fuse;
// import com.sun.jna.*;
// 
// import static org.improving.fuse.Fuse.*;
// import static org.improving.fuse.CommonStruct.*;
// import static org.improving.fuse.CommonFuse.*;
// import static org.improving.fuse.Fuse.FuseLibrary.*;
// import static org.improving.fuse.Fuse.FuseLibrary.FuseOperations.*;
// import static org.improving.fuse.Errno.*;
// import static org.improving.fuse.Fcntl.*;

// public class Hello {
//   static String file_path = "/hello.txt";
//   static String file_content = "Hello World!\n";
//   static int file_size = file_content.length();
//   static FuseOperations fops = new FuseOperations();
//   static boolean debug = true;
// 
//   static void DBG(String... msgs) {
//     if (!debug) return;
//     for (String msg : msgs) {
//       System.out.println(msg);
//     }
//   }
//   
//   static {    
//     fops.getattr = new GetattrFunc() {
//       public int invoke(String path, StructStat stbuf) {
//         // stbuf.setAutoSynch(true);
//         // StructFuseContext sfc = FuseLibrary.INSTANCE.fuse_get_context();
//         
//         stbuf.clear();
//         if (path.compareTo("/") == 0) {
//           stbuf.st_mode = (short)(S_IFDIR | 0755);
//           stbuf.st_nlink = (short)3;
//         }
//         else if (path.compareTo(file_path) == 0) {
//           stbuf.st_mode = (short)(S_IFREG | 0444);
//           stbuf.st_nlink = (short)1;
//           stbuf.st_size = file_size;
//         }
//         else return -ENOENT;
//         
//         stbuf.write();
//         return 0;
//       }
//     };
//     
//     fops.open = new OpenFunc() {
//       public int invoke(String path, StructFuseFileInfo fi) {
//         DBG("open " + path);
//         if (path.compareTo(file_path) != 0)
//           return -ENOENT;
//           
//         if ((fi.flags & O_ACCMODE) != O_RDONLY)
//           return -EACCES;
//         
//         return 0;
//       }
//     };
//     
//     fops.read = new ReadFunc() {
//       public int invoke(String path, Pointer buf, int size, long offset, StructFuseFileInfo fi) {
//         if (path.compareTo(file_path) != 0)
//           return -ENOENT;
//           
//         if (offset >= file_size)
//           return 0;
//           
//         byte[] content = file_content.getBytes();
//         if (offset + size > content.length)
//           size = content.length - (int)offset;
//         
//         buf.write(0, content, (int)offset, size);
//         return size;        
//       }
//     };
//     
//     fops.readdir = new ReaddirFunc() {
//       public int invoke(String path, Pointer buf, FuseFillDirT filler, long off, StructFuseFileInfo fi) {
//         if (path.compareTo("/") != 0)
//           return -ENOENT;
//                 
//         filler.invoke(buf, ".", null, 0);
//         filler.invoke(buf, "..", null, 0);
//         filler.invoke(buf, file_path.substring(1), null, 0);
//         return 0;
//       }
//     };  
//     
//     fops.statfs = new StatfsFunc() {
//       public synchronized int invoke(String path, StructStatvfs buf) {
//         // System.out.println("statfs: " + path);
//         // System.out.println("buf: " + buf);
//         buf.f_bsize = 1024;
//         buf.f_blocks = 10000;
//         buf.f_bfree = 5000;
//         buf.f_bavail = 4000;
//         buf.write();
//         // System.out.println("buf after: " + buf);
//         return 0;
//       }
//     };
//     
//     fops.init = new InitFunc() {
//       public void invoke(StructFuseConnInfo conn) {
//         System.out.println("init: " + conn);
//       }
//     };      
//   };
//   
//   public static void main(String[] args) {
//     String[] myargs = { "hello", "-d" };
//     String[] argv = new String[args.length + myargs.length];
//     System.arraycopy(myargs, 0, argv, 0, myargs.length);
//     System.arraycopy(args, 0, argv, myargs.length, args.length);
// 
//     FuseLibrary.INSTANCE.fuse_main(argv.length, argv, fops, null);
//     // FuseLibrary.INSTANCE.fuse_main_real(argv.length, argv, fops, fops.size(), Pointer.NULL);
//   }
// }