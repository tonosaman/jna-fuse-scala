package org.improving.fuse

import jnajava.Fuse._

class RichStructFuseFileInfo(fi: StructFuseFileInfo) {
  // direct_io: 1
  // keep_cache: 1
  // flush: 1
  // #if (__FreeBSD__ >= 10) padding: 27, purge_attr: 1, purge_ubc: 1
  // #else padding: 29
  // XXX presumably either this is right or it's the other way around
  private def setBit(bit: Int) = fi.bitfields |= (1 << bit)
  def setDirectIO()     = setBit(0)
  def setKeepCache()    = setBit(1)
  def setFlush()        = setBit(2)
  def setPurgeAttr()    = setBit(30)
  def setPurgeUbc()     = setBit(31)
}