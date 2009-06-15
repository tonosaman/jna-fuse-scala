package org.improving.fuse.jnajava;

// import static org.improving.fuse.CommonStruct.*;
import static org.improving.fuse.jnajava.C.*;
import static org.improving.fuse.CSizes.*;
import com.sun.jna.*;

// XXX
// find fuse.h on local system and parse fuse_operations to create scala code

public interface Fuse {
  public static final int FUSE_OPERATION_CALLBACKS = 58;
  public static class ScalaCallback extends Structure { public Callback cb; }
  public static class Operations extends Structure    {
    public ScalaCallback[] callbacks = new ScalaCallback[FUSE_OPERATION_CALLBACKS];
  }
      
  public class StructFuseConfig extends Structure {
  	public int uid;
  	public int gid;
  	public int umask;
  	public double entry_timeout;
  	public double negative_timeout;
  	public double attr_timeout;
  	public double ac_attr_timeout;
  	public int ac_attr_timeout_set;
  	public int debug;
  	public int hard_remove;
  	public int use_ino;
  	public int readdir_ino;
  	public int set_mode;
  	public int set_uid;
  	public int set_gid;
  	public int direct_io;
  	public int kernel_cache;
  	public int auto_cache;
  	public int intr;
  	public int intr_signal;
  	public int help;
  	public String modules;
  }
    
  public class StructFuseSession extends Structure {
    public static class ByReference extends StructFuseSession implements Structure.ByReference { }
    public StructFuseSessionOps op;
    public Pointer data;
    public volatile int exited;
    public StructFuseChan.ByReference ch;
  }
  
  public class StructFuseSessionOps extends Structure {
    public Pointer process;
    public Pointer exit;
    public Pointer exited;
    public Pointer destroy;
  }
    
  public class StructFuseChan extends Structure {
    public static class ByReference extends StructFuseChan implements Structure.ByReference { }
    public StructFuseChanOps op;
    public StructFuseSession.ByReference se;
    public int fd;
    public int bufsize;
    public Pointer data;
    public int compat;
  }
  
  public class StructFuseChanOps extends Structure {
    public Pointer receive;
    public Pointer send;
    public Pointer destroy;
  } 
  
  public class StructFuse extends Structure {
    public static class ByReference extends StructFuse implements Structure.ByReference { }
    public StructFuseSession.ByReference se;  // struct fuse_session *se;
  	public Pointer name_table;         // struct node **name_table;
    public int name_table_size;        // size_t name_table_size;
    public Pointer id_table;           // struct node **id_table;
    public int id_table_size;          // size_t id_table_size;
    public int ctr;                    // fuse_ino_t ctr;
    public int generation;             // unsigned int generation;
    public int hidectr;                // unsigned int hidectr;
    public byte[] lock = new byte[44]; // pthread_mutex_t lock;
    public byte[] tree_lock = new byte[128]; // pthread_rwlock_t tree_lock;
    public StructFuseConfig conf;      // struct fuse_config conf;
    public int intr_installed;         // int intr_installed;
    public Pointer fs;                 // struct fuse_fs *fs;
  }
  
  public class StructFuseContext extends Structure {
    public StructFuse.ByReference fuse;   // struct fuse * fuse
    public int uid;
    public int gid;
    public int pid;
    public Pointer private_data;
  }
  
  public class StructFuseFileInfo extends Structure {
    public int flags;
    public int fh_old;
    public int writepage;
    public int bitfields;
    // direct_io: 1
    // keep_cache: 1
    // flush: 1
    // #if (__FreeBSD__ >= 10) padding: 27, purge_attr: 1, purge_ubc: 1
    // #else padding: 29
    public long fh;
    public long lock_owner;
  }
  
  public class StructSetattrX extends Structure {
    public int valid;
  	public mode_t mode;
  	public uid_t uid;
  	public gid_t gid;
  	public off_t size;
  	public StructTimespec acctime;
  	public StructTimespec modtime;
  	public StructTimespec crtime;
  	public StructTimespec chgtime;
  	public StructTimespec bkuptime;
  	public uint32_t flags;
  }

  public interface FuseFillDirT extends Callback {
    public int invoke(Pointer buf, String name, final StructStat stbuf, long off);
  }

  public class FuseConnInfoFlags extends Structure {
    public int bitfield;
    int case_insensitive, setvolname, xtimes;
    
    public void updateBitfield() {
      bitfield = (case_insensitive << 0) | (setvolname << 1) | (xtimes << 2);
    }
    
    @Override
    public void write() {
      updateBitfield();
      super.write();
    }
    
    @Override
    public void read() {
      super.read();
      case_insensitive = bitfield & (1 << 0);
      setvolname = bitfield & (1 << 1);
      xtimes = bitfield & (1 << 2);
    }
  }
  
  public class StructFuseConnInfo extends Structure {
    public static class ByValue extends StructFuseConnInfo implements Structure.ByValue { }
    public int proto_major;
    public int proto_minor;
    public int async_read;
    public int max_write;
    public int max_readahead;
    public FuseConnInfoFlags enable;
    public int[] reserved = new int[26];
        
    public void enableSETVOLNAME() { enable.setvolname = 1; enable.write(); }
    public void enableCASE_INSENSITIVE() { enable.case_insensitive = 1; enable.write(); }
    public void enableXTIMES() { enable.xtimes = 1; enable.write(); }
  }
}