#include <stdio.h>
#include <sys/stat.h>

int total = 0;

int counts(char *name, int size, int howmany);
int count(char *name, int size);

int main(int argc, char **argv) {
  printf("struct stat          = %ld\n", sizeof(struct stat));
  
  total += count("dev_t", sizeof(dev_t));
  total += count("ino_t", sizeof(ino_t));
  total += count("mode_t", sizeof(mode_t));
  total += count("nlink_t", sizeof(nlink_t));
  total += count("uid_t", sizeof(uid_t));
  total += count("gid_t", sizeof(gid_t));
  total += count("dev_t", sizeof(dev_t));
  // total += counts("struct timespec", sizeof(struct timespec), 3);
  total += counts("time_t", sizeof(time_t), 3);
  total += counts("long", sizeof(long), 3);
  total += count("off_t", sizeof(off_t));
  total += count("blkcnt_t", sizeof(blkcnt_t));
  total += count("blksize_t", sizeof(blksize_t));
  total += counts("__uint32_t", sizeof(__uint32_t), 2);
  total += count("__int32_t", sizeof(__int32_t));
  total += counts("__int64_t", sizeof(__int64_t), 2);
  
  printf("total                = %d\n", total);
  return 0;
}

int counts(char *name, int size, int howmany) {
  printf("(%d) %16s = %d\n", howmany, name, size);
  return size * howmany;
}
  
int count(char *name, int size) {
  printf("%20s = %d\n", name, size);
  return size;
}


          //  dev_t   st_dev;   /* [XSI] ID of device containing file */
          //  ino_t     st_ino;   /* [XSI] File serial number */
          //  mode_t    st_mode;  /* [XSI] Mode of file (see below) */
          //  nlink_t   st_nlink; /* [XSI] Number of hard links */
          //  uid_t   st_uid;   /* [XSI] User ID of the file */
          //  gid_t   st_gid;   /* [XSI] Group ID of the file */
          //  dev_t   st_rdev;  /* [XSI] Device ID */
          // #if !defined(_POSIX_C_SOURCE) || defined(_DARWIN_C_SOURCE)
          //  struct  timespec st_atimespec;  /* time of last access */
          //  struct  timespec st_mtimespec;  /* time of last data modification */
          //  struct  timespec st_ctimespec;  /* time of last status change */
          // #else
          //  time_t    st_atime; /* [XSI] Time of last access */
          //  long    st_atimensec; /* nsec of last access */
          //  time_t    st_mtime; /* [XSI] Last data modification time */
          //  long    st_mtimensec; /* last data modification nsec */
          //  time_t    st_ctime; /* [XSI] Time of last status change */
          //  long    st_ctimensec; /* nsec of last status change */
          // #endif
          //  off_t   st_size;  /* [XSI] file size, in bytes */
          //  blkcnt_t  st_blocks;  /* [XSI] blocks allocated for file */
          //  blksize_t st_blksize; /* [XSI] optimal blocksize for I/O */
          //  __uint32_t  st_flags; /* user defined flags for file */
          //  __uint32_t  st_gen;   /* file generation number */
          //  __int32_t st_lspare;  /* RESERVED: DO NOT USE! */
          //  __int64_t st_qspare[2]; /* RESERVED: DO NOT USE! */          
