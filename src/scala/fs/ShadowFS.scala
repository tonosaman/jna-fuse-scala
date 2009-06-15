package org.improving.fuse

trait ShadowFS extends ScalaFS
{
  def filter(content: Array[Byte]): Array[Byte]
  def shadowpath(path: String): String
}