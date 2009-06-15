#define FUSE_USE_VERSION 26
#define __FreeBSD__ 10
#define _FILE_OFFSET_BITS 64

#include <stdio.h>
#include <fuse.h>
#include <sys/dirent.h>
#include <sys/signal.h>
#include "fuse_kernel.h"

struct fuse_config {
	unsigned int uid;
	unsigned int gid;
	unsigned int  umask;
	double entry_timeout;
	double negative_timeout;
	double attr_timeout;
	double ac_attr_timeout;
	int ac_attr_timeout_set;
	int debug;
	int hard_remove;
	int use_ino;
	int readdir_ino;
	int set_mode;
	int set_uid;
	int set_gid;
	int direct_io;
	int kernel_cache;
	int auto_cache;
	int intr;
	int intr_signal;
	int help;
	char *modules;
};

struct fuse {
	struct fuse_session *se;
	struct node **name_table;
	size_t name_table_size;
	struct node **id_table;
	size_t id_table_size;
  int ctr;
  // fuse_ino_t ctr;
	unsigned int generation;
	unsigned int hidectr;
	pthread_mutex_t lock;
	pthread_rwlock_t tree_lock;
	struct fuse_config conf;
	int intr_installed;
	struct fuse_fs *fs;
};

int main(int argc, char **argv) {
  printf("sigset_t = %ld\n", sizeof(sigset_t));
  printf("struct sigaction = %ld\n", sizeof(struct sigaction));
  printf("struct fuse = %ld\n", sizeof(struct fuse));
  printf("struct fuse_config = %ld\n", sizeof(struct fuse_config));
  printf("struct fuse_operations = %ld\n", sizeof(struct fuse_operations));
  printf("struct fuse_file_info = %ld\n", sizeof(struct fuse_file_info));
  printf("struct fuse_conn_info = %ld\n", sizeof(struct fuse_conn_info));
  printf("struct fuse_out_header = %ld\n", sizeof(struct fuse_out_header));
  printf("struct statvfs = %ld\n", sizeof(struct statvfs));
  printf("struct flock = %ld\n", sizeof(struct flock));
  printf("size_t = %ld\n", sizeof(size_t));
  printf("ssize_t = %ld\n", sizeof(ssize_t));
  printf("__uint16_t = %ld\n", sizeof(__uint16_t));
  printf("struct dirent = %ld\n", sizeof(struct dirent));
  printf("char = %ld\n", sizeof(char));
  printf("FILE * = %ld\n", sizeof(FILE));
  
  printf("fuse_fill_dir_t = %ld\n", sizeof(fuse_fill_dir_t));
  printf("fuse_dirfil_t = %ld\n", sizeof(fuse_dirfil_t));
  printf("fuse_dirh_t = %ld\n", sizeof(fuse_dirh_t));
  printf("fsblkcnt_t = %ld\n", sizeof(fsblkcnt_t));
  printf("fsfilcnt_t = %ld\n", sizeof(fsfilcnt_t));
  printf("pid_t = %ld\n", sizeof(pid_t));
  printf("struct_tm = %ld\n", sizeof(struct tm));
  printf("char* = %ld\n", sizeof(char *));
  printf("long = %ld\n", sizeof(long));
  printf("long long = %ld\n", sizeof(long long));
  printf("off_t = %ld\n", sizeof(off_t));

  
  return 0;
}
