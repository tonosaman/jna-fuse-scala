package org.improving.fuse.jnajava;

import com.sun.jna.*;

public interface Stdio {  
  /* stdio buffers */
  public class StructSbuf extends Structure {
    public Pointer _base;
    public int _size;
  }
  
  public class StructFILE extends Structure {
    public Pointer _p;
    public int _r;
    public int _w;
    public short _flags;
    public short _file;
    public StructSbuf _bf;
    public int _lbfsize;
    
    public Pointer _cookie;
    public Pointer _close;
    public Pointer _read;
    public Pointer _seek;
    public Pointer _write;
    
    public StructSbuf _ub;
    public Pointer _extra;    // external additions to FILE to preserve ABI
    public int _ur;
    
    public byte[] _ubuf = new byte[3];
    public byte[] _nbuf = new byte[1];
    public StructSbuf _lb;
    
    public int _blksize;
    public long _offset;
  }
  
  public class CString extends Structure {
    int length;
    int capacity;
    public byte[] buffer;
    
    public CString(int capacity) {
      super();
      this.capacity = capacity;
      this.length = 0;
      buffer = new byte[capacity + 1];
      buffer[0] = 0;
      allocateMemory();
    }
    
    public CString(String s) {
      super();
      byte[] bytes = s.getBytes();
      this.length = bytes.length;
      this.capacity = bytes.length;
      buffer = new byte[bytes.length + 1];
      System.arraycopy(bytes, 0, buffer, 0, bytes.length);
      buffer[bytes.length] = 0;
      allocateMemory();
    }
    
    public String toString() {
      return Native.toString(buffer);
    }
  }
}