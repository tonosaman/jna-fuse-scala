package org.improving.fuse.fs

import com.sun.jna._
import jnajava.Fuse._
import Mapper._

object HttpFS extends ScalaFS with ScalaFSDefaults
{  
  val name: String = "httpfs"
  var mountedPath: String = _

  override def init(conn: StructFuseConnInfo): Pointer = {
    val mapper = new Mapper
    // mapper.addFile("/hello.txt", "Hello World!\n")
    // mapper.addDynamicString("/now.txt", (new java.util.Date).toString + "\n")
    // mapper.addDynamicFile(mountedPath)
    addMapper(mapper)
    null
  }
  
  // static int httpfs_read(const char *path, char *buf, size_t size, off_t offset,
  //                       struct fuse_file_info *fi) {
  //     int fd;
  //     int res;
  //     size_t got;
  //     (void) fi;
  //     if (strcmp(path, httpfs_path) == 0) {
  //  if (offset < file_size) {
  //             if (offset + size > file_size)
  //    size = file_size - offset;
  //      got = HttpGet(offset, size, buf);
  //      if (got < 0)
  //    return -EIO;
  //      if (got != size) {
  //    close(sockfd);
  //    sockfd = open_client_socket(host, port);
  //    got = HttpGet(offset, size, buf);
  //    if (got != size)
  //        return -EIO;
  //      }
  //  } else
  //             got = 0;
  //  return got;
  //     } else {
  //  fd = open(path+1, O_RDONLY);
  //  if (fd == -1)
  //             return -errno;
  //  res = pread(fd, buf, size, offset);
  //  if (res == -1)
  //             res = -errno;
  //  close(fd);
  //  return res;
  //     }
  // }
  // 
  // static int httpfs_write(const char *path, const char *buf, size_t size, 
  //      off_t offset, struct fuse_file_info *fi) {
  //     int fd;
  //     int res;
  // 
  //     (void) fi;
  //     if (strcmp(path, httpfs_path) == 0) {
  //  return -EACCES;
  //     }
  //     fd = open(path+1, O_WRONLY);
  //     if (fd == -1)
  //         return -errno;
  // 
  //     res = pwrite(fd, buf, size, offset);
  //     if (res == -1)
  //         res = -errno;
  // 
  //     close(fd);
  //     return res;
  // }
  // 
  
}


// 
// /*
//  * HTTPFS: import a file from a web server to local file system
//  * the main use is, to mount an iso on a web server with loop device
//  *
//  * depends on:
//  * FUSE: Filesystem in Userspace
//  * Copyright (C) 2001-2006  Miklos Szeredi <miklos@szeredi.hu>
//  *
//  * This program can be distributed under the terms of the GNU GPL.
//  *
//  */
// 
// /* 
//  * This 'beam me up, Scotty'-branch of httpfs tries to achieve,
//  * that the mount-point-folder behaves as before.
//  * But how can you access the original folder after the mount?
//  * Answer comes from FuseCompress:
//  * Open the folder before the mount,
//  * keep it open all the time,
//  * make a chdir to it
//  * and always use a relative path.
//  * It suffices not to chdir in main() and it's unnecessary to
//  * do it in every function. httpfs_init is the right place.
//  */
// 
// 
// #include <fuse.h>
// #include <unistd.h>
// #include <stdlib.h>
// #include <stdio.h>
// #include <string.h>
// #include <errno.h>
// #include <fcntl.h>
// #include <sys/stat.h>
// #include <sys/dir.h>
// #include <sys/types.h>
// #include <sys/socket.h>
// #include <netinet/in.h>
// #include <netdb.h>
// #include <time.h>
// 
// #ifdef USE_SSL
// #include <openssl/ssl.h>
// #include <openssl/err.h>
// #endif
// 
// #define TIMEOUT 30
// #define VERSION "2.06.08.26"
// 
// static char* argv0;
// static char* argv1;
// static char* argv2;
// static long long int file_size;
// static int protocol;
// static char host[1000];
// static unsigned short port;
// static char* file_name;
// 
// 
// static char httpfs_path[256];
// static int targetFd;
// static int sockfd;
// 
// /* Protocol symbols. */
// #define PROTO_HTTP 0
// #ifdef USE_SSL
// #define PROTO_HTTPS 1
// #endif
// 
// #ifdef USE_AUTH
// static char auth_buf[1000];
// 
// static char b64_encode_table[64] = {
//     'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',  /* 0-7 */
//     'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',  /* 8-15 */
//     'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',  /* 16-23 */
//     'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f',  /* 24-31 */
//     'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n',  /* 32-39 */
//     'o', 'p', 'q', 'r', 's', 't', 'u', 'v',  /* 40-47 */
//     'w', 'x', 'y', 'z', '0', '1', '2', '3',  /* 48-55 */
//     '4', '5', '6', '7', '8', '9', '+', '/'   /* 56-63 */
// };
//     
// /* Do base-64 encoding on a hunk of bytes.   Return the actual number of
// ** bytes generated.  Base-64 encoding takes up 4/3 the space of the original,
// ** plus a bit for end-padding.  3/2+5 gives a safe margin.
// */
// static int b64_encode(unsigned char* ptr, int len, char* space, int size) {
//     int ptr_idx;
//     unsigned char c = 0;
//     unsigned char d = 0;
//     int space_idx = 0;
//     int phase = 0;
// 
//     if (size <= 0) return 0;
// 
//     for (ptr_idx = 0; ptr_idx < len; ++ptr_idx) {
//  switch (phase++) {
//      case 0:
//    c = ptr[ptr_idx] >> 2;
//    d = (ptr[ptr_idx] & 0x3) << 4;
//    break;
//      case 1:
//    c = d | (ptr[ptr_idx] >> 4);
//    d = (ptr[ptr_idx] & 0xf) << 2;
//    break;
//      case 2:
//    c = d | (ptr[ptr_idx] >> 6);
//    if (space_idx < size) space[space_idx++] = b64_encode_table[c];
//    c = ptr[ptr_idx] & 0x3f;
//    break;
//  }
//  space[space_idx++] = b64_encode_table[c];
//  if (space_idx == size) return space_idx;
//  phase %= 3;
//     }
//     if (phase != 0) {
//  space[space_idx++] = b64_encode_table[d];
//  if (space_idx == size) return space_idx;
//  /* Pad with ='s. */
//  while (phase++ > 0) {
//      space[space_idx++] = '=';
//      if (space_idx == size) return space_idx;
//      phase %= 3;
//  }
//     }
//     return space_idx;
// }
// 
// #endif /* USE_AUTH */
// 
// /*
//  * Function yields host, file, port and auth_buf
//  * (auth_buf is global)
//  * Besides protocol is returned
//  * (-1 in case of error)
//  */
// 
// static int parseURL(char* url, char* host, char** ptrfile, unsigned short* port) {
//     int host_len, protocol, proto_len;
//     char* file;
//  
//     char* http = "http://";
// #ifdef USE_SSL
//     char* https = "https://";
// #endif /* USE_SSL */
// 
// 
//     if (url == NULL) {
//  (void) fprintf(stderr, "%s: null URL\n", argv0);
//         return -1;
//     }
//     if (strncmp(http, url, strlen(http)) == 0) {
//  proto_len = strlen(http);
//  protocol = PROTO_HTTP;
//  *port = 80; 
// #ifdef USE_SSL
//     } else if (strncmp(https, url, strlen(https)) == 0) {
//  proto_len = strlen(https);
//  protocol = PROTO_HTTPS;
//  *port = 443;
// #endif /* USE_SSL */
//     } else {
//  (void) fprintf(stderr, "%s: non-http URL\n", argv0);
//         return -1;
//     }
// 
// #ifdef USE_AUTH
//     /* Get user and password */
//     auth_buf[0] = '\0';
//     for (file = url + proto_len; *file != '\0' && *file != '/'; ++file) {
//  if (*file == '@') {
//      auth_buf[b64_encode(url + proto_len, file - url - proto_len, auth_buf, sizeof(auth_buf))] = '\0';
//      proto_len = file - url + 1;
//      break;
//  }
//     }
// #endif /* USE_AUTH */
//     
//     /* Get the host name. */
//     for (file = url + proto_len; *file != '\0' && *file != ':' && *file != '/'; ++file)
//  ;
//     host_len = file - url - proto_len;
//     strncpy(host, url + proto_len, host_len);
//     host[host_len] = '\0';
// 
//     /* Get port number. */
//     if (*file == ':') {
//  *port = (unsigned short) atoi(++file);
//  for (; *file != '\0' && *file != '/'; ++file)
//      ;
//     }
//     
//     /* Get the file name. */
//     if (*file == '\0')
//  file = "/";
//     
//     *ptrfile = file;
//     return protocol;
// }
// 
// 
// /* 
//  * Function yields either a 'connected' socket for
//  * host 'hostname' on port 'port'  or < 0 in case of error
//  *
//  * hostname is something like 'www.tmtd.de' or 192.168.0.86
//  * port is expected in machine order (not net order)
//  *
//  * ((Flonix  defines USE_IPV6))
//  * 
// */
// 
// #if defined(AF_INET6) && defined(IN6_IS_ADDR_V4MAPPED)
// #define USE_IPV6
// #endif
// 
// static int open_client_socket(char* hostname, unsigned short port) {
// #ifdef USE_IPV6
//     struct addrinfo hints;
//     char portstr[10];
//     int gaierr;
//     struct addrinfo* ai;
//     struct addrinfo* aiv4;
//     struct addrinfo* aiv6 = 0;
//     struct sockaddr_in6 sa;
// #else /* USE_IPV6 */
//     struct hostent *he;
//     struct sockaddr_in sa;
// #endif /* USE_IPV6 */
//     int sa_len, sock_family, sock_type, sock_protocol;
//     int sockfd;
// 
//     (void) memset((void*) &sa, 0, sizeof(sa));
// 
// #ifdef USE_IPV6
//     (void) memset(&hints, 0, sizeof(hints));
//     hints.ai_family = PF_UNSPEC;
//     hints.ai_socktype = SOCK_STREAM;
//     (void) snprintf(portstr, sizeof(portstr), "%d", (int) port);
//     if ((gaierr = getaddrinfo(hostname, portstr, &hints, &ai)) != 0) {
//  (void) fprintf(stderr, "%s: getaddrinfo %s - %s\n", argv0, hostname, gai_strerror(gaierr));
//  return -1;
//     }
// 
//     /* Find the first IPv4 and IPv6 entries. */
//     for (aiv4 = ai; aiv4 != NULL; aiv4 = aiv4->ai_next) {
//  if (aiv4->ai_family == AF_INET)
//      break;
//  if ((aiv4->ai_family == AF_INET6) && (aiv6 == NULL))
//      aiv6 = aiv4;
//     }
// 
//     /* If there's an IPv4 address, use that, otherwise try IPv6. */
//     if (aiv4 == NULL) 
//      aiv4 = aiv6;
//     if (aiv4 == NULL) {
//  (void) fprintf(stderr, "%s: no valid address found for host %s\n", argv0, hostname);
//  return -1;
//     }
//     if (sizeof(sa) < aiv4->ai_addrlen) {
//  (void) fprintf(stderr, "%s - sockaddr too small (%lu < %lu)\n", hostname, (unsigned long) sizeof(sa), (unsigned long) aiv4->ai_addrlen);
//  return -1;
//     }
//     sock_family = aiv4->ai_family;
//     sock_type = aiv4->ai_socktype;
//     sock_protocol = aiv4->ai_protocol;
//     sa_len = aiv4->ai_addrlen;
//     (void) memmove(&sa, aiv4->ai_addr, sa_len);
//     freeaddrinfo(ai);
// 
// #else /* USE_IPV6 */
// 
//     he = gethostbyname(hostname);
//     if (he == NULL) {
//  (void) fprintf(stderr, "%s: unknown host - %s\n", argv0, hostname);
//  return -1;
//     }
//     sock_family = sa.sin_family = he->h_addrtype;
//     sock_type = SOCK_STREAM;
//     sock_protocol = 0;
//     sa_len = sizeof(sa);
//     (void) memmove(&sa.sin_addr, he->h_addr, he->h_length);
//     sa.sin_port = htons(port);
// 
// #endif /* USE_IPV6 */
// 
//     sockfd = socket(sock_family, sock_type, sock_protocol);
//     if (sockfd < 0) {
//  (void) fprintf(stderr, "%s: couldn't get socket, got errno: %u\n", argv0, errno);
//  return -1;
//     }
//     if (connect(sockfd, (struct sockaddr*) &sa, sa_len) < 0) {
//  (void) fprintf(stderr, "%s: couldn't connect socket, got errno: %u\n", argv0, errno);
//  return -1;
//     }
//     return sockfd;
// }
// 
// /* 
//  * Function uses HEAD-HTTP-Request
//  * to determine the file size
//  * This is done only once
//  */
// 
// int getSize() {
// #ifdef USE_SSL
//     SSL_CTX* ssl_ctx;
//     SSL* ssl;
// #endif
//     char buf[1000];
//     char* b;
//     struct timeval timeout;
//     int bytes, status;
// 
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS) {
//  /* Make SSL connection. */
//  int r;
//  SSL_load_error_strings();
//  SSLeay_add_ssl_algorithms();
//  ssl_ctx = SSL_CTX_new(SSLv23_client_method());
//  ssl = SSL_new(ssl_ctx);
//  SSL_set_fd(ssl, sockfd);
//  r = SSL_connect(ssl);
//  if (r <= 0) {
//      (void) fprintf(stderr, "%s: %s:%d - SSL connection failed - %d\n", argv0, host, port, r);
//      ERR_print_errors_fp(stderr);
//      exit(1);
//  }
//     }
// #endif
// 
//     /* Build request buffer, starting with the HEAD. */
// 
//     bytes = snprintf(buf, sizeof(buf), "HEAD %s HTTP/1.1\r\nHost: %s\r\n", file_name, host);
// #ifdef USE_AUTH
//     if ( *auth_buf != '\0' )
//  bytes += snprintf(&buf[bytes], sizeof(buf) - bytes, "Authorization: Basic %s\r\n", auth_buf);
// #endif
//     bytes += snprintf(&buf[bytes], sizeof(buf) - bytes, "\r\n");
// 
//     /* Now actually send it. */
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS)
//  (void) SSL_write(ssl, buf, bytes);
//     else
// #endif
//     (void) write(sockfd, buf, bytes);
// 
//     timeout.tv_sec = TIMEOUT;
//     timeout.tv_usec = 0;
//     setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
// 
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS)
//  bytes = SSL_read(ssl, buf, sizeof(buf));
//     else
// #else
//     bytes = read(sockfd, buf, sizeof(buf));
// #endif
//     if (bytes <= 0) {
//  fprintf(stderr, "%s: HEAD (read) failed\n", argv0);
//  return -1;
//     }
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS) {
//  SSL_free(ssl);
//  SSL_CTX_free(ssl_ctx);
//     }
// #endif
// 
//     (void) sscanf(buf, "HTTP/1.1 %d ", &status);
//     if (status != 200) {
//  fprintf(stderr, "%s: HEAD (read) failed with Status %d\n", argv0, status);
//  return -1;
//     }
//     b = strstr(buf,"Content-Length:");
//     if (b == NULL) {
//  fprintf(stderr, "%s: HEAD-Reply didn't contain Content-Length\n", argv0);
//  return -1;
//     }
//     file_size = atoll(b+16);
//     return 0;
// }
// 
// /* 
//  * HttpGet does all the magic
//  * a GET-Request with Range-Header
//  * allows to read arbitrary bytes
//  */
// 
// int HttpGet(off_t start, size_t size, char * destination) {
// #ifdef USE_SSL
//     SSL_CTX* ssl_ctx;
//     SSL* ssl;
// #endif
//     char buf[1000];
//     char* b;
//     size_t end = start + size - 1;
//     struct timeval timeout;
//     int bytes, status;
// 
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS) {
//  /* Make SSL connection. */
//  int r;
//  SSL_load_error_strings();
//  SSLeay_add_ssl_algorithms();
//  ssl_ctx = SSL_CTX_new(SSLv23_client_method());
//  ssl = SSL_new(ssl_ctx);
//  SSL_set_fd(ssl, sockfd);
//  r = SSL_connect(ssl);
//  if (r <= 0) {
//      (void) fprintf(stderr, "%s: %s:%d - SSL connection failed - %d\n", argv0, host, port, r);
//      ERR_print_errors_fp(stderr);
//      exit(1);
//  }
//     }
// #endif
// 
//     /* Build request buffer, starting with the GET. */
// 
//     bytes = snprintf(buf, sizeof(buf), "GET %s HTTP/1.1\r\nHost: %s\r\n", file_name, host);
//     bytes += snprintf(&buf[bytes], sizeof(buf) - bytes, "Range: bytes=%llu-%llu\r\n", (unsigned long long) start, (unsigned long long) end);
// #ifdef USE_AUTH
//     if ( *auth_buf != '\0' )
//  bytes += snprintf(&buf[bytes], sizeof(buf) - bytes, "Authorization: Basic %s\r\n", auth_buf);
// #endif
//     bytes += snprintf(&buf[bytes], sizeof(buf) - bytes, "\r\n");
// 
//     /* Now actually send it. */
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS)
//  (void) SSL_write(ssl, buf, bytes);
//     else
// #endif
//     (void) write(sockfd, buf, bytes);
// 
//     timeout.tv_sec = TIMEOUT;
//     timeout.tv_usec = 0;
//     setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
// 
// 
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS)
//  bytes = SSL_read(ssl, buf, sizeof(buf));
//     else
// #else
//  bytes = read(sockfd, buf, sizeof(buf));
// #endif
//     if (bytes <= 0) {
//  fprintf(stderr, "%s: GET (read) failed with bytes= %d\n", argv0, bytes);
//  return bytes;
//     }
//     (void) sscanf(buf, "HTTP/1.1 %d ", &status);
//     if ((status != 200) && (status != 206)) {
//  fprintf(stderr, "%s: GET (read) failed with Status %d\n", argv0, status);
//  return -1;
//     }
//     b = strstr(buf,"Content-Length:");
//     if (b == NULL) {
//  fprintf(stderr, "%s: GET-Reply didn't contain Content-Length\n", argv0);
//  return -1;
//     }
//     if (atoll(b+16) != size) {
//    fprintf(stderr, "%s: GET didn't yield whole piece\n", argv0);
//  size = (size_t) atoll(b+16);
//     }
//     b = strstr(buf,"\r\n\r\n")+4;
// 
//     
//     bytes -= (b - buf);
//     memcpy(destination, b, bytes);
//     size -= bytes;
//     for (; size > 0; size -= bytes) {
//  destination += bytes;
//  timeout.tv_sec = TIMEOUT;
//  timeout.tv_usec = 0;
//  setsockopt(sockfd, SOL_SOCKET, SO_RCVTIMEO, &timeout, sizeof(timeout));
// #ifdef USE_SSL
//  if (protocol == PROTO_HTTPS)
//      bytes = SSL_read(ssl, destination, size);
//  else
// #else
//      bytes = read(sockfd, destination, size);
// #endif
//  
//  if (bytes < 0) {
//      fprintf(stderr, "%s: GET (read) failed\n", argv0);
//      return bytes;
//  }
//  if (bytes == 0) {
//      break;
//  }
//     }
// 
// #ifdef USE_SSL
//     if (protocol == PROTO_HTTPS) {
//  SSL_free(ssl);
//  SSL_CTX_free(ssl_ctx);
//     }
// #endif
// 
//     return end - start + 1 - size;
// }
// 
// static int httpfs_getattr(const char *path, struct stat *stbuf) {
// 
//     memset(stbuf, 0, sizeof(struct stat));
//     if (strcmp(path, "/") == 0) {
//         stbuf->st_mode = S_IFDIR | 0755; /* read, write and search by owner, read and write by all other */
//         stbuf->st_nlink = 2;
//     } else if (strcmp(path, httpfs_path) == 0) {
//         stbuf->st_mode = S_IFREG | 0555; /* read and execute for everybody */
//         stbuf->st_nlink = 1;
//         stbuf->st_size = file_size;
//     } else {
//  if (lstat(path+1, stbuf) == -1)
//      return -ENOENT;
//     }
//     return 0;
// }
// 
// static int httpfs_access(const char *path, int mask) {
//     int res;
// 
//     if (strcmp(path, "/") == 0) {
//         return 0;
//     } else if (strcmp(path, httpfs_path) == 0) {
//  return ((mask & W_OK) == W_OK) ? -1 : 0;
//     } else {
//  res = access(path+1, mask);
//  if (res == -1)
//      return -errno;
//     }
//     return 0;
// }
// 
// static int httpfs_readlink(const char *path, char *buf, size_t size) {
//     int res;
// 
//     res = readlink(path+1, buf, size - 1);
//     if (res == -1)
//         return -errno;
// 
//     buf[res] = '\0';
//     return 0;
// }
// 
// static int httpfs_readdir(const char *path, void *buf, fuse_fill_dir_t filler,
//                          off_t offset, struct fuse_file_info *fi) {
//     DIR *dp;
//     struct dirent *de;
//     (void) offset;
//     (void) fi;
//    
//     if(strcmp(path, "/") != 0) {
//  dp = opendir(path + 1);
//     } else {
//  filler(buf, httpfs_path + 1, NULL, 0);
//  dp = opendir(".");
//     }
//     if (dp == NULL)
//  return -errno;
//  
//     while ((de = readdir(dp)) != NULL) {
//         struct stat st;
//         memset(&st, 0, sizeof(st));
//         st.st_ino = de->d_ino;
//         st.st_mode = de->d_type << 12;
//         if (filler(buf, de->d_name, &st, 0))
//      break;
//     }
// 
//     closedir(dp);
// 
//     return 0;
// }
// 
// static int httpfs_mknod(const char *path, mode_t mode, dev_t rdev) {
//     int res;
// 
//     /* On Linux this could just be 'mknod(path, mode, rdev)' but this is more portable */
//     if (S_ISREG(mode)) {
//         res = open(path+1, O_CREAT | O_EXCL | O_WRONLY, mode);
//         if (res >= 0)
//      res = close(res);
//     } else if (S_ISFIFO(mode))
//         res = mkfifo(path, mode);
//     else
//         res = mknod(path, mode, rdev);
//     if (res == -1)
//         return -errno;
//     return 0;
// }
// 
// static int httpfs_mkdir(const char *path, mode_t mode) {
// 
//     if (mkdir(path+1, mode) == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_symlink(const char *from, const char *to) {
// 
// //       This function is only called, if "to" happens to reside in  mount-point-folder.
// //      "from" is always an ordinary path
//  
//     if (symlink(from, to+1) == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_unlink(const char *path) {
//     int res;
// 
//     res = unlink(path+1);
//     if (res == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_rmdir(const char *path) {
//     int res;
// 
//     res = rmdir(path+1);
//     if (res == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_rename(const char *from, const char *to) {
// 
// //      seems to be never called (cross-device link)
// //      replaced by copy and delete ?
// 
//     if (rename(from, to+1) == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_link(const char *from, const char *to) {
//     int res;
// 
//     res = link(from+1, to+1);
//     if (res == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_chmod(const char *path, mode_t mode) {
// 
//     if (strcmp(path, httpfs_path) == 0) {
//  return -EACCES;
//     } else {
//  if (chmod(path+1, mode) == -1)
//             return -errno;
//     } 
//     return 0;
// }
// 
// static int httpfs_chown(const char *path, uid_t uid, gid_t gid) {
// 
//     if (strcmp(path, httpfs_path) == 0) {
//  return -EACCES;
//     } else {
//  if (lchown(path+1, uid, gid) == -1)
//             return -errno;
//     }
//     return 0;
// }
// 
// static int httpfs_truncate(const char *path, off_t size) {
// 
//     if (strcmp(path, httpfs_path) == 0) {
//  return -EACCES;
//     } else {
//  if (truncate(path+1, size) == -1)
//             return -errno;
//     }
//     return 0;
// }
// 
// static int httpfs_utime(const char *path, struct utimbuf *buf) {
// 
//     if (strcmp(path, httpfs_path) == 0) {
//  return -EACCES;
//     } else {
//  if (utime(path+1, buf) == -1)
//             return -errno;
//     }
//     return 0;
// }
// 
// static int httpfs_open(const char *path, struct fuse_file_info *fi) {
//     int res;
// 
//     if (strcmp(path, httpfs_path) == 0) {
//  if((fi->flags & 3) != O_RDONLY) {
//      return -EACCES;
//  } else {
//      return 0;
//  }
//     }
//     res = open(path+1, fi->flags);
//     if (res == -1)
//         return -errno;
// 
//     close(res);
//     return 0;
// 
// }
// 
// static int httpfs_read(const char *path, char *buf, size_t size, off_t offset,
//                       struct fuse_file_info *fi) {
//     int fd;
//     int res;
//     size_t got;
//     (void) fi;
//     if (strcmp(path, httpfs_path) == 0) {
//  if (offset < file_size) {
//             if (offset + size > file_size)
//    size = file_size - offset;
//      got = HttpGet(offset, size, buf);
//      if (got < 0)
//    return -EIO;
//      if (got != size) {
//    close(sockfd);
//    sockfd = open_client_socket(host, port);
//    got = HttpGet(offset, size, buf);
//    if (got != size)
//        return -EIO;
//      }
//  } else
//             got = 0;
//  return got;
//     } else {
//  fd = open(path+1, O_RDONLY);
//  if (fd == -1)
//             return -errno;
//  res = pread(fd, buf, size, offset);
//  if (res == -1)
//             res = -errno;
//  close(fd);
//  return res;
//     }
// }
// 
// static int httpfs_write(const char *path, const char *buf, size_t size, 
//      off_t offset, struct fuse_file_info *fi) {
//     int fd;
//     int res;
// 
//     (void) fi;
//     if (strcmp(path, httpfs_path) == 0) {
//  return -EACCES;
//     }
//     fd = open(path+1, O_WRONLY);
//     if (fd == -1)
//         return -errno;
// 
//     res = pwrite(fd, buf, size, offset);
//     if (res == -1)
//         res = -errno;
// 
//     close(fd);
//     return res;
// }
// 
// static int httpfs_statfs(const char *path, struct statvfs *stbuf) {
//     int res;
//     (void) path;
// 
//     res = statvfs(".", stbuf);
//     if (res == -1)
//         return -errno;
// 
//     return 0;
// }
// 
// static int httpfs_release(const char *path, struct fuse_file_info *fi) {
//  
//     /* Just a stub.  This method is optional and can safely be left unimplemented */
//     (void) path;
//     (void) fi;
//     return 0;
// }
// 
// static int httpfs_fsync(const char *path, int isdatasync, struct fuse_file_info *fi) {
// 
//     /* Just a stub.  This method is optional and can safely be left unimplemented */
//     (void) path;
//     (void) isdatasync;
//     (void) fi;
//     return 0;
// }
// 
// static void *httpfs_init(void) {
//     fchdir(targetFd);  /* that's the catch */
//     return NULL;
// }
// 
// static void httpfs_destroy(void *arg) {
//     close(targetFd);
// }
// 
// #ifdef HAVE_SETXATTR
// /* xattr operations are optional and can safely be left unimplemented */
// static int httpfs_setxattr(const char *path, const char *name, const char *value,
//                         size_t size, int flags) {
// 
//     if (strcmp(path, httpfs_path) == 0)
//  return -EACCES;
//     if (lsetxattr(path+1, name, value, size, flags) == -1)
//         return -errno;
//     return 0;
// }
// 
// static int httpfs_getxattr(const char *path, const char *name, char *value,
//                     size_t size) {
//     int res;
// 
//     if (strcmp(path, httpfs_path) == 0)
//  return -EACCES;
//     res = lgetxattr(path+1, name, value, size);
//     if (res == -1)
//         return -errno;
//     return res;
// }
// 
// static int httpfs_listxattr(const char *path, char *list, size_t size) {
//     int res;
// 
//     if (strcmp(path, httpfs_path) == 0)
//  return -EACCES;
//     res = llistxattr(path+1, list, size);
//     if (res == -1)
//         return -errno;
//     return res;
// }
// 
// static int httpfs_removexattr(const char *path, const char *name) {
// 
//     if (strcmp(path, httpfs_path) == 0)
//  return -EACCES;
//     if (lremovexattr(path+1, name) == -1)
//         return -errno;
//     return 0;
// }
// #endif /* HAVE_SETXATTR */
// 
// static struct fuse_operations httpfs_oper = {
//     .getattr = httpfs_getattr,
//     .access  = httpfs_access,
//     .readlink  = httpfs_readlink,
//     .readdir = httpfs_readdir,
//     .mknod = httpfs_mknod,
//     .mkdir = httpfs_mkdir,
//     .symlink = httpfs_symlink,
//     .unlink  = httpfs_unlink,
//     .rmdir = httpfs_rmdir,
//     .rename  = httpfs_rename,
//     .link  = httpfs_link,
//     .chmod = httpfs_chmod,
//     .chown = httpfs_chown,
//     .truncate  = httpfs_truncate,
//     .utime = httpfs_utime,
//     .open  = httpfs_open,
//     .read  = httpfs_read,
//     .write = httpfs_write,
//     .statfs  = httpfs_statfs,
//     .release = httpfs_release,
//     .fsync = httpfs_fsync,
//     .init       = httpfs_init,
//     .destroy    = httpfs_destroy,
// #ifdef HAVE_SETXATTR
//     .setxattr  = httpfs_setxattr,
//     .getxattr  = httpfs_getxattr,
//     .listxattr = httpfs_listxattr,
//     .removexattr= httpfs_removexattr,
// #endif
// };
// 
// int main(int argc, char *argv[]) {
//     struct stat mpstat;
//     int sr;
//     char* ri;
//     char* fusev[4];
//  
//     argv0 = argv[0];
//     if (argc != 3) {
//  (void) fprintf(stderr, "usage:  %s url mount-point\n", argv0);
//  (void) fprintf(stderr, ">>> Version: %s <<<\n", VERSION);
//  return 1;
//     }
//     argv1 = argv[1];
//     argv2 = argv[2];
//     
//     protocol = parseURL(argv1, host, &file_name, &port);
//     if (protocol == -1) 
//  return 1;
// 
//     sockfd = open_client_socket(host, port);
//     if (sockfd < 0)
//  return 1;
// 
//     if (getSize() != 0) 
//  return 1;
// 
//     sr = stat(argv2, &mpstat);
//     if (sr < 0) {
//  (void) fprintf(stderr, "%s: bad mount-point %s\n", argv0, argv2);
//  return 1;
//     }
// 
//     if ((mpstat.st_mode & S_IFDIR) == 0) {
//  (void) fprintf(stderr, "%s: %s is not a directory\n", argv0, argv2);
//  return 1;
//     }
//     
//     if ((targetFd = open(argv2, 0)) == -1) {
//  (void) fprintf(stderr, "%s: open %s failed\n", argv0, argv2);
//  return 1;
//     }
//     
//     ri = rindex(file_name, '/');
//     if (ri == (char *) 0) {
//  httpfs_path[0] = '/';
//  strcpy(&httpfs_path[1], file_name);
//     } else
//  strcpy(httpfs_path, ri);
// 
//     fusev[0] = argv0;
//     fusev[1] = "-o";
//     fusev[2] = "nonempty";
//     fusev[3] = argv2;
//     return fuse_main(argc+1, fusev, &httpfs_oper);
//     /*    close(sockfd); */
// }
