// #define FUSE_USE_VERSION 26
// #define __FreeBSD__ 10
// #define _FILE_OFFSET_BITS 64

#include <stdio.h>
// #include <fuse.h>

int main(int argc, char **argv) {
  printf("%ld\n", sizeof(off_t));
  return 0;
}