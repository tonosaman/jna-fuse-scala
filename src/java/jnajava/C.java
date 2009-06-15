package org.improving.fuse.jnajava;

import com.sun.jna.*;

public interface C {  
  public interface FUSEByReference<T extends Structure.ByReference> {
    public T getByReference();
  }
  
  public class StructDIR extends Structure implements FUSEByReference {
    public static class ByReference extends StructDIR implements Structure.ByReference { 
      public void useMemory(Pointer p) { super.useMemory(p); }
    }
    public StructDIR.ByReference getByReference() { 
      StructDIR.ByReference x = new StructDIR.ByReference();
      x.useMemory(this.getPointer());
      return x;
    }

    public int __dd_fd;
    public int __dd_loc;
    public int __dd_size;
    public Pointer __dd_buf;
    public int __dd_len;
    public int __dd_seek;
    public int __dd_rewind;
    public int __dd_flags;
    public int __dd_lock;
    public Pointer __dd_td;
  }
  
  public class StructTimespec extends Structure {
    public static class ByValue extends StructTimespec implements Structure.ByValue { }
    public int tv_sec;
    public int tv_nsec; 
  }
  public class StructTimeval extends Structure {
    public int tv_sec;
    public int tv_usec;
  }
  
  // sizeof = 12
  //     union __sigaction_u __sigaction_u;  /* signal handler */
  // sigset_t sa_mask;    /* signal mask to apply */
  // int  sa_flags;   /* see signal options below */
  public class StructSigaction extends Structure {
    public Pointer __sigaction_u;
    // public ScalaCallback __sigaction_u;
    public int sa_mask;
    public int sa_flags;
  }   
  
  public class StructStat extends Structure {
    public int st_dev;
    public int st_ino;
    public short st_mode;
    public short st_nlink;
    public int st_uid;
    public int st_gid;
    public int st_rdev;
    // public StructTimespec st_atimespec;
    // public StructTimespec st_mtimespec;
    // public StructTimespec st_ctimespec;
    public int st_atime;
    public int st_atimensec;
    public int st_mtime;
    public int st_mtimensec;
    public int st_ctime;
    public int st_ctimensec;
    public long st_size;
    public long st_blocks;
    public int st_blksize;
    public int st_flags;
    public int st_gen;
    public int st_lspare;           // reserved
    public long st_qspare1;
    public long st_qspare2;
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

  public class StructFlock extends Structure {
    public long l_start;
    public long l_len;
    public int l_pid;
    public short l_type;
    public short l_whence;
  }
  
  public class StructAttrlist extends Structure {
    public short bitmapcount;
    public short reserved;
    public int commonattr;
    public int volattr;
    public int dirattr;
    public int fileattr;
    public int forkattr;
  }
  
  public class StructXtimeattrbuf extends Structure {
    public int size;
    public StructTimespec xtime;
  }  
    
  public class StructDirent extends Structure implements FUSEByReference {
    public static class ByReference extends StructDirent implements Structure.ByReference {
      public void useMemory(Pointer p) { super.useMemory(p); }
    }
    public StructDirent.ByReference getByReference() { 
      StructDirent.ByReference x = new StructDirent.ByReference();
      x.useMemory(this.getPointer());
      return x;
    }

    public int d_ino;
    public short d_reclen;
    public byte d_type;
    public byte d_namlen;
    public byte[] d_name = new byte[256]; // __DARWIN_MAXNAMLEN + 1
  }
}
