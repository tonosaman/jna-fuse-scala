package org.improving.fuse.scratch;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

/** Simple example of native library declaration and usage. */
// System.out.println("data model: " + System.getProperty("sun.arch.data.model"));          

public class Exp {
  
  public interface SystemLibrary extends Library {
    SystemLibrary INSTANCE = (SystemLibrary)Native.loadLibrary("System", SystemLibrary.class);
        
    public static class HashNode extends Structure {
      public String key;
      public Pointer data;
      public Pointer next;
    }
    public static class HashTable extends Structure {
      public int size;      
      public PointerByReference buckets;
    }
    
    public interface HashDestroyFunc extends Callback {
      public void invoke(String k, Pointer d);
    }
    public interface HashReplaceFunc extends Callback {
      public void invoke(Pointer d);
    }
    
    
    public HashTable hash_create(int size);
    public void hash_destroy(HashTable table, String key, HashDestroyFunc nukefunc);
    public Pointer hash_search(HashTable table, String key, Structure datum, Callback HashReplaceFunc);
    
    
    // typedef struct _node {
    //     char *key;
    //     void *data;
    //     struct _node *next;
    // } hash_node;
    // 
    // typedef struct {
    //     int size;
    //     hash_node **buckets;
    // } hash_table;
    // 
    // hash_table *hash_create(int size);
    // void hash_destroy(hash_table *table, char *key,
    //         void (*nukefunc)(char *k, void *d));
    // void *hash_search(hash_table *table, char *key, void *datum,
    //         void (*replace_func)(void *d));
    // void hash_traverse(hash_table *table,
    //          int (*func)(char *k, void *d, void *arg), void *arg);
    // void hash_purge(hash_table *table, void (*purge_func)(char *k, void *d));    
  }

    public interface CLibrary extends Library {
        CLibrary INSTANCE = (CLibrary)Native.loadLibrary("c", CLibrary.class);
        // struct tm {
        //     int  tm_sec;   /* seconds after the minute [0-60] */
        //     int  tm_min;   /* minutes after the hour [0-59] */
        //     int  tm_hour;  /* hours since midnight [0-23] */
        //     int  tm_mday;  /* day of the month [1-31] */
        //     int  tm_mon;   /* months since January [0-11] */
        //     int  tm_year;  /* years since 1900 */
        //     int  tm_wday;  /* days since Sunday [0-6] */
        //     int  tm_yday;  /* days since January 1 [0-365] */
        //     int  tm_isdst; /* Daylight Savings Time flag */
        //     long tm_gmtoff;  /* offset from CUT in seconds */
        //     char *tm_zone; /* timezone abbreviation */
        // };
        //      
        
         public static class StructTM extends Structure {
             public int sec;
             public int min;
             public int hour;
             public int mday;
             public int month;
             public int year;
             public int wday;
             public int yday;
             public int isDaylightSavings;
             public int gmtOffset;
             public String tmZone;
          }
          
          public class StructTimespec extends Structure {
            public int  sec;
            public int nsec; 
          }
          public class StructStatvfs extends Structure {
            public int f_bsize;
            public int f_frsize;
            public int f_blocks;
            public int f_bfree;
            public int f_bavail;
            public int f_files;
            public int f_ffree;
            public int f_favail;
            public int f_fsid;
            public int f_flag;
            public int f_namemax;
          }
          
          public class StructStat extends Structure {
            public int st_dev;
            public int st_ino;
            public short st_mode;
            public short st_nlink;
            public int st_uid;
            public int st_gid;
            public int st_rdev;
            public StructTimespec st_atimespec;
            public StructTimespec st_mtimespec;
            public StructTimespec st_ctimespec;
            public long st_size;
            public long st_blocks;
            public int st_blksize;
            public int st_flags;
            public int st_gen;
            public int st_lspare;           // reserved
            public long st_qspare1;
            public long st_qspare2;
          }
          
          public class StructFlock extends Structure {
            public long l_start;
            public long l_len;
            public int l_pid;
            public short l_type;
            public short l_whence;
          }
          
          public interface AtExitFunc extends Callback {
            public void invoke();
          }
          
          // // Original C code
          // struct _functions {
          //   int (*open)(const char*,int);
          //   int (*close)(int);
          // };
          // 
          // // Equivalent JNA mapping
          // public class Functions extends Structure {
          //   public static interface OpenFunc extends Callback {
          //     int invoke(String name, int options);
          //   }
          //   public static interface CloseFunc extends Callback {
          //     int invoke(int fd);
          //   }
          //   public OpenFunc open;
          //   public CloseFunc close;
          // }
          // ...
          // Functions funcs = new Functions();
          // lib.init(funcs);
          // int fd = funcs.open.invoke("myfile", 0);
          // funcs.close.invoke(fd);
          
        // struct tm* gmtime(const time_t* tp);
        // time_t time(time_t *);
        // int	 atexit(void (*)(void));
        StructTM gmtime(LongByReference tp);
        int time(IntByReference t);
        int atexit(AtExitFunc ae);
        void printf(String format, Object... args);
    }

    public static void main(String[] args) {
        CLibrary.INSTANCE.printf("Hello, World\n");
        for (int i=0;i < args.length;i++) {
            CLibrary.INSTANCE.printf("Argument %d: %s\n", i, args[i]);
        }
        LongByReference i = new LongByReference();
        i.setValue(238904957);
        CLibrary.StructTM tm = CLibrary.INSTANCE.gmtime(i);
        CLibrary.StructStat stat = new CLibrary.StructStat();
        CLibrary.StructStatvfs statvfs = new CLibrary.StructStatvfs();
        CLibrary.StructFlock flock = new CLibrary.StructFlock();
        
        System.out.println("struct tm size = " + tm.size());
        System.out.println("struct stat size = " + stat.size());
        System.out.println("struct stat vfs size = " + statvfs.size());
        System.out.println("struct flock size = " + flock.size());
        
        
        // System.out.println(tm.toString());
        // 
        // SystemLibrary.HashTable ht = SystemLibrary.INSTANCE.hash_create(500);
        // System.out.println(ht.toString());
        // SystemLibrary.HashDestroyFunc hdf = new SystemLibrary.HashDestroyFunc() {
        //   public void invoke(String k, Pointer d) {
        //     System.out.println("I am destroyed.  Aaagh!");
        //   }
        // };
        // 
        // String key = "bloop".intern();
        // SystemLibrary.HashNode value = new SystemLibrary.HashNode();
        // SystemLibrary.INSTANCE.hash_search(ht, key, value, null);
        // SystemLibrary.INSTANCE.hash_destroy(ht, key, hdf);
        
        // CLibrary.AtExitFunc ae = new CLibrary.AtExitFunc() { public void invoke() { System.out.println("I was invoked."); } };
        // CLibrary.INSTANCE.atexit(ae);
    }
}