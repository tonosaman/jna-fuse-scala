// #define FUSE_USE_VERSION 22
// #define _FILE_OFFSET_BITS 64
// #include <fuse.h>
// #include <stdio.h>
// #include <stdlib.h>
// #include <string.h>
// #include <errno.h>
// #include <fcntl.h>
// #include <pthread.h>
// #include "bs.h"
// 
// static pthread_mutex_t bsDatabaseAccessMutex;
// 
// /*-------------------------------------------------------------------------------------------------
//   Shut down this fusesys mount, ending it's thread.
// -------------------------------------------------------------------------------------------------*/
// static void stopFusesysThread(
//     bsFusesys fusesys)
// {
//     fuse_unmount(bsFusesysGetMountpoint(fusesys));
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Initialize FUSE module.
// -------------------------------------------------------------------------------------------------*/
// void bsFuseStart(void)
// {
//     /* Nothing for now */
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Free memory used in the FUSE module.
// -------------------------------------------------------------------------------------------------*/
// void bsFuseStop(void)
// {
//     bsTorrent torrent;
//     bsFusesys fusesys;
// 
//     bsForeachRootTorrent(bsTheRoot, torrent) {
//  bsSafeForeachTorrentFusesys(torrent, fusesys) {
//      stopFusesysThread(fusesys);
//  } bsEndSafeForeachTorrentFusesys;
//     } bsEndForeachRootTorrent;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Lock the database access mutex.
// -------------------------------------------------------------------------------------------------*/
// void bsLockDatabaseAccess(void)
// {
//     pthread_mutex_lock(&bsDatabaseAccessMutex);
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Unlock the database access mutex.
// -------------------------------------------------------------------------------------------------*/
// void bsUnlockDatabaseAccess(void)
// {
//     pthread_mutex_unlock(&bsDatabaseAccessMutex);
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Convert an S64 to an off_t.
// -------------------------------------------------------------------------------------------------*/
// static off_t convS64ToOfft(
//     S64 value)
// {
//     return (((off_t)utgS64Upper(value)) << 32) | utgS64Lower(value);
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Convert an off_t to an S64.
// -------------------------------------------------------------------------------------------------*/
// static S64 convOfftToS64(
//     off_t value)
// {
//     S64 retVal;
// 
//     utrS64Upper(retVal, value >> 32);
//     utrS64Lower(retVal, value & 0xffffffff);
//     return retVal;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Find the directory from the path.
// -------------------------------------------------------------------------------------------------*/
// bsDir bsTorrentLookupDir(
//     bsTorrent torrent,
//     char *path)
// {
//     bsDir dir = bsTorrentGetRootDir(torrent);
//     utSym sym;
//     char *className;
// 
//     if(*path == UTDIRSEP) {
//  path++;
//     }
//     while(*path != '\0') {
//  className = utSkipToNextDirInPath(&path);
//  sym = utSymCreate((char *)className);
//  dir = bsDirFindDir(dir, sym);
//  if(dir == bsDirNull) {
//      return bsDirNull;
//  }
//     }
//     return dir;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Find the current fusesys for this thread.
// -------------------------------------------------------------------------------------------------*/
// static bsFusesys findCurrentFusesys(void)
// {
//     bsTorrent torrent;
//     bsFusesys fusesys;
//     //pthread_t threadID = pthread_self();
// 
// //temp: assume we have only one fuse thingy for now
//     bsForeachRootTorrent(bsTheRoot, torrent) {
//  bsForeachTorrentFusesys(torrent, fusesys) {
//      //if(bsFusesysGetThreadID(fusesys) == threadID) {
//    return fusesys;
//      //}
//  } bsEndForeachTorrentFusesys;
//     } bsEndForeachRootTorrent;
//     return bsFusesysNull;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Get the file attributes for a path.
// -------------------------------------------------------------------------------------------------*/
// static UTINT bsGetattr(
//     const char *path,
//     struct stat *stbuf)
// {
//     bsTorrent torrent;
//     bsDir dir;
//     bsFile file;
//     S64 length;
// 
//     bsLockDatabaseAccess();
//     torrent = bsFusesysGetTorrent(findCurrentFusesys());
//     dir = bsTorrentLookupDir(torrent, (char *)path);
//     utIfDebug(1) {
//         printf("get attr of %s\n", path);
//     }
//     memset(stbuf, 0, sizeof(struct stat));
//     if(dir == bsDirNull) {
//  bsUnlockDatabaseAccess();
//  return -ENOENT;
//     }
//     file = bsDirGetFile(dir);
//     if(file == bsFileNull) {
//  stbuf->st_mode = S_IFDIR | 0755;
//  stbuf->st_nlink = bsDirCountDirs(dir);
//     } else {
//         stbuf->st_mode = S_IFREG | 0444;
//         stbuf->st_nlink = 1;
//  length = bsFileGetLength(file);
//         stbuf->st_size = (((off_t)utgS64Upper(length)) << 32) | utgS64Lower(length);
//     }
//     bsUnlockDatabaseAccess();
//     return 0;
// 
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Open the file.  This really doesn't do anything but check that the named file is legal.
// -------------------------------------------------------------------------------------------------*/
// static UTINT bsOpen(
//     const char *path,
//     struct fuse_file_info *fi)
// {
//     struct stat stbuf;
// 
//     utIfDebug(1) {
//         printf("open of %s\n", path);
//     }
//     if((fi->flags & 3) != O_RDONLY) {
//         return -EACCES;
//     }
//     memset(&stbuf, 0, sizeof(struct stat));
//     if(bsGetattr(path, &stbuf) != ENOENT && (stbuf.st_mode & S_IFDIR) == 0) {
//         return 0;
//     }
//     return -ENOENT;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   List the directory entries.
// -------------------------------------------------------------------------------------------------*/
// static int bsReaddir(
//     const char *path,
//     void *buf,
//     fuse_fill_dir_t filler,
//     off_t offset,
//     struct fuse_file_info *fi)
// {
//     bsTorrent torrent;
//     bsDir dir;
//     bsDir child;
//     bsFile file;
// 
//     bsLockDatabaseAccess();
//     torrent = bsFusesysGetTorrent(findCurrentFusesys());
//     dir = bsTorrentLookupDir(torrent, (char *)path);
//     utIfDebug(1) {
//         printf("get dir of %s\n", path);
//     }
//     if(*path == UTDIRSEP) {
//  path++;
//     }
//     if(dir == bsDirNull) {
//  bsUnlockDatabaseAccess();
//  return -ENOENT;
//     }
//     filler(buf, ".", NULL, 0);
//     filler(buf, "..", NULL, 0);
//     file = bsDirGetFile(dir);
//     if(file != bsFileNull) {
//         filler(buf, bsDirGetName(dir), NULL, 0);
//     } else {
//  bsForeachDirDir(dir, child) {
//      filler(buf, bsDirGetName(child), NULL, 0);
//  } bsEndForeachDirDir;
//     }
//     bsUnlockDatabaseAccess();
//     return 0;
// }
// 
// /*--------------------------------------------------------------------------------------------------
//   Write piece data to the buffer.
// --------------------------------------------------------------------------------------------------*/
// static void writeFilePieces(
//     bsFile file,
//     char *buf,
//     off_t offset,
//     size_t length)
// {
//     bsTorrent torrent = bsFileGetTorrent(file);
// 
//     U32 xPiece = utConvertS64ToU32(utDivideS64ByS32(bsFileGetBegin(file),
//         bsTorrentGetPieceLength(torrent)));
//     bsPiece piece = bsTorrentGetiPiece(torrent, xPiece);
//     S64 pieceStart = utMultiplyS64(utMakeS64(xPiece), utMakeS64(bsTorrentGetPieceLength(torrent)));
//     U32 position = utConvertS64ToU32(utSubtractS64(bsFileGetBegin(file), pieceStart));
//     U32 size;
//     S64 remaining = utMakeS64(length);
//     size_t bufPos = 0;
// 
//     if(utgtS64(bsFileGetLength(file), remaining)) {
//  remaining = bsFileGetLength(file);
//     }
//     while(!uttzeroS64(remaining)) {
//         size = bsPieceGetSize(piece);
//  if(utgtS64(utMakeS64(size), remaining)) {
//      size = utConvertS64ToU32(remaining);
//  }
//  if(bsPieceFlushedToDisk(piece)) {
//      bsPieceLoadFromDisk(piece);
//  }
//  memcpy(buf + bufPos, bsPieceGetDatas(piece) + position, size);
//  bufPos += size;
//  remaining = utSubtractS64(remaining, utMakeS64(size));
//  if(!uttzeroS64(remaining)) {
//      xPiece++;
//      piece = bsTorrentGetiPiece(torrent, xPiece);
//      position = 0;
//  }
//     }
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Build objects to request piece downloads needed.  These objects should cause the needed pieces
//   to have higher priority for download than others, and the last ones should be downloaded in
//   end-game mode.  When a piece is downloaded, all the fusereq objects on that piece should be
//   deleted, and when their owning readreq objects don't have any remaining fusereqs, they should be
//   woken up with a pthread_cond_signal.
// -------------------------------------------------------------------------------------------------*/
// static void buildFusereqs(
//     bsReadreq readreq)
// {
//     bsTorrent torrent = bsReadreqGetTorrent(readreq);
//     bsFusereq fusereq;
//     bsFile file = bsReadreqGetFile(readreq);
//     U32 xPiece = utConvertS64ToU32(utDivideS64ByS32(bsFileGetBegin(file),
//         bsTorrentGetPieceLength(torrent)));
//     bsPiece piece = bsTorrentGetiPiece(torrent, xPiece);
//     S64 pieceStart = utMultiplyS64(utMakeS64(xPiece), utMakeS64(bsTorrentGetPieceLength(torrent)));
//     U32 position = utConvertS64ToU32(utSubtractS64(bsFileGetBegin(file), pieceStart));
//     U32 size;
//     S64 remaining = utMakeS64(bsReadreqGetSize(readreq));
//     size_t bufPos = 0;
// 
//     if(utgtS64(bsFileGetLength(file), remaining)) {
//  remaining = bsFileGetLength(file);
//     }
//     while(!uttzeroS64(remaining)) {
//         size = bsPieceGetSize(piece);
//  if(utgtS64(utMakeS64(size), remaining)) {
//      size = utConvertS64ToU32(remaining);
//  }
//  fusereq = bsPieceGetfirstFusereq(piece);
//  if(!bsPieceHave(piece) && (fusereq == bsFusereqNull ||
//          bsFusereqGetReadreq(fusereq) != readreq)) {
//      fusereq = bsFusereqAlloc();
//      bsPieceInsertFusereq(piece, fusereq);
//      bsReadreqInsertFusereq(readreq, fusereq);
//  }
//  bufPos += size;
//  remaining = utSubtractS64(remaining, utMakeS64(size));
//  if(!uttzeroS64(remaining)) {
//      xPiece++;
//      piece = bsTorrentGetiPiece(torrent, xPiece);
//      position = 0;
//  }
//     }
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Signal that the FUSE read request can now be satisfied.
// -------------------------------------------------------------------------------------------------*/
// void bsReadreqSignalFulfilled(
//     bsReadreq readreq)
// {
//     pthread_cond_signal(&bsReadreqGetConditionVar(readreq));
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Download the file section needed.
// -------------------------------------------------------------------------------------------------*/
// static void downloadFileSection(
//     bsFile file,
//     S64 offset,
//     U32 size)
// {
//     bsTorrent torrent = bsFileGetTorrent(file);
//     bsReadreq readreq = bsReadreqAlloc();
// 
//     bsReadreqSetOffset(readreq, offset);
//     bsReadreqSetSize(readreq, size);
//     bsReadreqSetFile(readreq, file);
//     bsTorrentInsertReadreq(torrent, readreq);
//     buildFusereqs(readreq); /* Creates connection objects to all needed file pieces */
//     if(bsReadreqGetfirstFusereq(readreq) != bsFusereqNull) {
//  pthread_cond_init(&bsReadreqGetConditionVar(readreq), NULL);
//  pthread_cond_wait(&bsReadreqGetConditionVar(readreq), &bsDatabaseAccessMutex);
//     }
//     utAssert(bsReadreqGetfirstFusereq(readreq) == bsFusereqNull);
//     bsReadreqDestroy(readreq);
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Read data from the file specified by the path.
// -------------------------------------------------------------------------------------------------*/
// static UTINT bsRead(
//     const char *path,
//     char *buf,
//     size_t size,
//     off_t offset,
//     struct fuse_file_info *fi)
// {
//     bsTorrent torrent;
//     bsDir dir;
//     bsFile file;
//     off_t length;
// 
//     bsLockDatabaseAccess();
//     torrent = bsFusesysGetTorrent(findCurrentFusesys());
//     dir = bsTorrentLookupDir(torrent, (char *)path);
//     utIfDebug(1) {
//         printf("read %s\n", path);
//     }
//     if(dir == bsDirNull) {
//  bsUnlockDatabaseAccess();
//  return -ENOENT;
//     }
//     file = bsDirGetFile(dir);
//     if(file == bsFileNull) {
//  bsUnlockDatabaseAccess();
//  return -ENOENT;
//     }
//     length = convS64ToOfft(bsFileGetLength(file));
//     if(offset >= length) {
//  bsUnlockDatabaseAccess();
//         return 0;
//     }
//     if(offset + size > length) {
//         size = length - offset;
//     }
//     if(!bsTorrentEntered(torrent)) {
//  if(!bsTorrentEnter(torrent)) {
//      return -ENOENT;
//  }
//  bsTorrentSetLazyDownload(torrent, true); /* Only download what we request */
//     }
//     downloadFileSection(file, convOfftToS64(offset), (U32)size);
//     writeFilePieces(file, buf, offset, size);
//     bsUnlockDatabaseAccess();
//     return size;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Write the fuse operations structure.
// -------------------------------------------------------------------------------------------------*/
// static struct fuse_operations bsOper = {
//     .getattr = bsGetattr,
//     .open = bsOpen,
//     .readdir = bsReaddir,
//     .read = bsRead,
// };
// 
// /*-------------------------------------------------------------------------------------------------
//   This is the thread-main for each mount.  Just call fuse_main.
// -------------------------------------------------------------------------------------------------*/
// static void *fuseMain(
//     void *fusesysPtr)
// {
//     bsFusesys fusesys = (bsFusesys)fusesysPtr;
// 
//     fuse_main(bsFusesysGetArgc(fusesys), bsFusesysGetArgvs(fusesys), &bsOper);
//     bsLockDatabaseAccess();
//     bsFusesysDestroy(fusesys);
//     bsUnlockDatabaseAccess();
//     return NULL;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Start the fuse file system representing the database.  This opens the fuse file descriptor as a
//   socket, and creates a fusesys object for the torrent.  The main select loop has to process this
//   socket along with the others.
// -------------------------------------------------------------------------------------------------*/
// bsFusesys bsFusesysMount(
//     bsTorrent torrent,
//     char *mountPoint,
//     U32 argc,
//     char **argv)
// {
//     bsFusesys fusesys;
//     pthread_t threadID;
//     U32 xArg;
// 
//     fusesys = bsFusesysAlloc();
//     bsFusesysAllocMountpoints(fusesys, strlen(mountPoint) + 1);
//     strcpy(bsFusesysGetMountpoints(fusesys), mountPoint);
//     bsTorrentInsertFusesys(torrent, fusesys);
//     bsFusesysSetArgc(fusesys, argc + 3);
//     bsFusesysReallocArgvs(fusesys, argc + 3);
//     bsFusesysSetiArgv(fusesys, 0, utBaseName(utGetExeFullPath()));
//     bsFusesysSetiArgv(fusesys, 1, "-f");
//     for(xArg = 0; xArg < argc; xArg++) {
//  bsFusesysSetiArgv(fusesys, xArg + 2, argv[xArg]);
//     }
//     bsFusesysSetiArgv(fusesys, argc + 2, bsFusesysGetMountpoint(fusesys));
//     pthread_create(&threadID, NULL, fuseMain, fusesys);
//     bsFusesysSetThreadID(fusesys, threadID);
//     return fusesys;
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Unmount the fuse file system.
// -------------------------------------------------------------------------------------------------*/
// void bsFusesysUnmount(
//     bsFusesys fusesys)
// {
//     // Here's where we need to stop the thread!
//     stopFusesysThread(fusesys);
// }
// 
// /*-------------------------------------------------------------------------------------------------
//   Find a fuse file system mounted at the mount point.
// -------------------------------------------------------------------------------------------------*/
// bsFusesys bsFindFusesys(
//     char *mountPoint)
// {
//     bsTorrent torrent;
//     bsFusesys fusesys;
// 
//     bsForeachRootTorrent(bsTheRoot, torrent) {
//  bsForeachTorrentFusesys(torrent, fusesys) {
//      if(!strcmp(bsFusesysGetMountpoint(fusesys), mountPoint)) {
//    return fusesys;
//      }
//  } bsEndForeachTorrentFusesys;
//     } bsEndForeachRootTorrent;
//     return bsFusesysNull;
// }
