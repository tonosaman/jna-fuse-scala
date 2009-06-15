package org.improving.fuse

import com.sun.jna.Pointer
import jnajava.Fuse._
import Mapper._

object HelloScala extends ScalaFS with ScalaFSDefaults
{  
  val name: String = "hellofs"

  override def init(conn: StructFuseConnInfo): Pointer = {
    val mapper = new Mapper
    mapper.addFile("/hello.txt", "Hello World!\n")
    mapper.addDynamicString("/now.txt", (new java.util.Date).toString + "\n")
    addMapper(mapper)
    null
  }
}
