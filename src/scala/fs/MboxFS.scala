// package org.improving.fuse
// 
// import com.sun.jna._
// import ptr._
// import Fuse._
// import jnajava.C._
// import CommonFuse._
// import FuseLibrary._
// import FuseUtil._
// import Errno._
// import Fcntl._
// import CSizes._
// import java.util.jar._
// import Libc.{ INSTANCE => LIBC }
// import scala.collection.mutable.{ HashSet, HashMap, ListBuffer }
// import java.io.{ File, FileReader, FileInputStream, BufferedReader }
// import java.util.regex.Pattern
// // import javax.mail.internet._
// // import javax.mail.{ URLName, Session }
// import javax.mail._
// import gnu.mail.providers.mbox._
// import java.util.Date
// 
// object MboxFS extends ScalaFS
// {  
//   val name: String = "mboxfs"
//   debug = true
//   
//   val sessionProperties = {
//     val sp = new java.util.Properties
//     sp.setProperty("mail.mbox.mailhome", "/Users/paulp/mail")
//     sp.setProperty("mail.mbox.inbox", "/Users/paulp/mbox")
//     sp
//   }
//   val session = Session.getInstance(sessionProperties)
//   val store = session.getStore("mbox")
//   store.connect
//   val mbox = store.getFolder("szigeti")
//   mbox.open(Folder.READ_ONLY)
//   val msgs = new HashMap[String, Set[Message]]
//   val rootFolders = new HashSet[String]
//   
//   class Email(mm: MimeMessage) extends FUSEFile {
//     // m.contentStream
//     def from(): Address = mm.getFrom()(0)
//     def date(): Date = mm.getSentDate
//     def size(): Int = mm.getSize
//   }
//   
//   override def init(conn: StructFuseConnInfo): Pointer = {
//     for (mm: MimeMessage <- mbox.getMessages) {
//       val em = new Email(mm)
//       if (msgs.get(em.from).isEmpty)
//         msgs(em.from) = new HashSet[Message]
//         
//       msgs(em.from) += em
//       rootFolders += "/" + em.from
//     }
//       
//     null
//   }
//   //   
//   // def go() = {
//   //   for (m <- msgs) println(m.date)
//   // }
//   
//   sealed abstract trait FUSEObject {
//     val path: String
//   }
//     
//   sealed abstract trait FUSEEntity extends FUSEObject {
//     def stat(stbuf: StructStat): Unit
//     def size(): Int
//   }
//   abstract trait FUSEFile extends FUSEEntity {
//     def stat(stbuf: StructStat): Unit = {
//       stbuf.st_mode = (S_IFREG | 0444).toShort
//       stbuf.st_nlink = 1
//       stbuf.st_size = size
//     }
//   }
//   abstract trait FUSEDir extends FUSEEntity {
//     def contents(): List[FUSEObject]
//     def stat(stbuf: StructStat): Unit = {    
//       stbuf.st_mode = (S_IFDIR | 0555).toShort
//       stbuf.st_nlink = contents.size.toShort
//       stbuf.st_size = contents.size * 34
//     }
//   }
//   
//   trait FUSEError extends FUSEObject
//   case class DoesNotExist(val path: String) extends FUSEError
//   
//   def lookupPath(path: String): FUSEObject = {
//     if (path == "/") FuseDir("/", rootFolders.toList)
//     else {
//       val (dir, file) = dirAndFile(path)
//       if (dir == "/" && rootFolders.contains(file))
//       
//       msgs.get(dir) match {
//         case None => 
//       
//   }
//   
//   override def getattr(path: String, stbuf: StructStat): Int = {
//     stbuf.clear
//     
//     lookupPath(path) match {
//       case DoesNotExist => -ENOENT
//       case x            => x.stat(stbuf) ; 0
//     }
//   }
//   
//   //         Store store = session.getStore(new URLName("mbox:///home/
//   // matej/.eclipse/workspace/Mdir2mbox/examples/"));
//   //         Folder test = store.getFolder("test");
//   //         test.open(Folder.READ_WRITE);
//   // 
//   // object EmailMsg {
//   //   final val emailRegex = """^[a-zA-Z][\w\.-]*[a-zA-Z0-9]@[a-zA-Z0-9][\w\.-]*[a-zA-Z0-9]\.[a-zA-Z][a-zA-Z\.]*[a-zA-Z]$"""
//   //   def apply(xs: List[String]): EmailMsg = {
//   //     assert(xs != null && xs.size > 0)
//   //     assert(xs.head startsWith "From ")
//   //     val blank = xs.findIndexOf(_.length == 0)
//   //     if (blank == -1) throw new Exception
//   //     
//   //     // coalesce multi-line headers
//   //     val headers = xs.take(blank).foldRight(List[String]())((x, y) =>
//   //       if (y.isEmpty) x :: y
//   //       else if (y.head.length > 0 && !y.head(0).isWhitespace) x :: y
//   //       else if (x.length > 0 && x(0).isWhitespace) (x + " " + y.head) :: y.tail
//   //       else x :: y
//   //     )
//   // 
//   //     new EmailMsg(headers, xs.drop(blank + 1).mkString("\n") + "\n")
//   //   }
//   // }
//   // 
//   // class EmailMsg private (val headers: List[String], val body: String) {
//   //   lazy val messageID = extractHeader("Message-Id")
//   //   lazy val from: InternetAddress = extractHeader("From").map(new InternetAddress(_)) getOrElse null
//   //   lazy val email = from.getAddress
//   //   lazy val personal = from.getPersonal
//   //   
//   //   def extractHeader(name: String): Option[String] = {
//   //     headers .
//   //       find(_.matches("(?i)^" + name + ":.*")) .
//   //       map(_.substring(name.length + 1) .
//   //           dropWhile(_.isWhitespace) .
//   //           mkString
//   //       )
//   //   }
//   //   
//   //   override def toString() = {
//   //     headers.toString + "\n" + body
//   //   }
//   // }
//   //   
//   // class Mboxes(path: String) {
//   //   // val f = new File(path)
//   //   // val mboxes = f.listFiles.filter(_.canRead)
//   //   val mboxes = List(new File("/Users/paulp/mbox"))
//   //   val msgs = new HashMap[String, EmailMsg]
//   //   val msgsNoId = new ListBuffer[EmailMsg]
//   //   
//   //   private def addMbox(f: File): Unit = {
//   //     val reader = new BufferedReader(new FileReader(f))
//   //     val lb = new ListBuffer[String]
//   //     lb += reader.readLine      
//   //     if (!lb(0).startsWith("From ")) return    // make sure it's really an mbox
//   //     
//   //     while (true) {
//   //       val line = reader.readLine
//   //       if (line == null || line.startsWith("From ")) {
//   //         addMessage(lb.toList.mkString("\n") + "\n")
//   //         lb.clear
//   //       }
//   //       if (line == null) return
//   //       lb += line
//   //     }
//   //   }
//   //   
//   //   private def addMessage(msg: String) = {
//   //     val em = new MimeMessage
//   //     em.content = msg
//   //     println(em)
//   //   }
//   //   
//   //   // private def addMessage(lines: List[String]) = {
//   //   //   val em = EmailMsg(lines)
//   //   //   em.messageID match {
//   //   //     case Some(id) => msgs(id) = em
//   //   //     case None     => msgsNoId += em
//   //   //   }
//   //   // }
//   //   
//   //   // constructor
//   //   mboxes foreach addMbox
//   // }
//   // 
//   
//   // val jc = new JarCollection
//   // 
//   // class JarCollection {
//   //   val jars  = new ListBuffer[JarSource]
//   //   def +=(js: JarSource) = jars += js
//   //   def canon(s: String): String = if (s startsWith "/") s else "/" + s
//   //   
//   //   def totalSize = jars.flatMap(_.files.values.toList).map(_.getSize).foldLeft(0L)(_+_).toInt
//   //   def totalFiles = jars.flatMap(_.files.values.toList).size
//   //   
//   //   // contents of this path, or empty list if it doesn't exist
//   //   def dir(s: String): List[String] = {
//   //     DBG("dir(", s, ") = ")
//   //     val ret = {
//   //       val contents = jars.flatMap(_.directories.get(canon(s))).foldLeft(HashSet[String]())(_ ++ _).toList
//   //       if (contents.isEmpty) {
//   //         val emptyDir = jars.exists(_.directories contains canon(s))
//   //         if (emptyDir) List(".", "..") else Nil
//   //       }
//   //       else "." :: ".." :: contents
//   //     }
//   //     DBGLN(ret)
//   //     ret
//   //   }
//   //   
//   //   // # of 512K blocks used by this dir
//   //   def dirSize(s: String): Int = (dir(s).size) * 34
//   //    
//   //   // jar entry corresponding to this path
//   //   def file(s: String): Option[JarEntry] = {
//   //     DBG("file(" + s + ") = ")
//   //     def ret(): Option[JarEntry] = {
//   //       for (jar <- jars ; e <- jar.files.get(canon(s)))
//   //         return Some(e)
//   //       
//   //       None
//   //     }
//   //     DBGLN(ret)
//   //     ret
//   //   }
//   //   
//   //   // jar file containing this path
//   //   def jar(s: String): Option[JarSource] =
//   //     jars.find(x => x.directories.contains(canon(s)) || x.files.contains(canon(s)))
//   //   
//   //   // contents of this path or null if non-existent
//   //   def contents(s: String): Array[Byte] = {
//   //     DBGLN("contents(" + s + ")")
//   //     val entry = file(s) getOrElse (return null)
//   //     val jarsource = jar(s) getOrElse (return null)
//   //     val in = jarsource.jar.getInputStream(entry)
//   //     // val count = in.available
//   //     val count = entry.getSize.toInt
//   //     val ret = new Array[Byte](count)
//   //     var bytesRead = in.read(ret, 0, count)
//   //     
//   //     while (bytesRead < count) {
//   //       val bytes = in.read(ret, bytesRead, count - bytesRead)
//   //       if (bytes == -1) {
//   //         DBGLN("Early EOF: wanted " + count + ", got " + bytes)
//   //         return null
//   //       }
//   //       bytesRead += bytes
//   //     }
//   //     
//   //     if (bytesRead == count) ret
//   //     else {
//   //       DBGLN("Wanted " + count + ", got " + bytesRead)
//   //       null
//   //     }
//   //   }
//   // }
//   // 
//   // object JarSource {
//   //   private def dirAndFile(path: String): (String, String) = {
//   //     val s = if (path startsWith "/") path else "/" + path
//   //     val ix = s.lastIndexOf('/')
//   // 
//   //     if (s == 0) ("", path)
//   //     else if (s endsWith "/") (s.substring(0, s.length - 1), "")
//   //     else (s.substring(0, ix), s.substring(ix + 1))
//   //   }
//   // }
//   // 
//   // class JarSource(val path: String) {
//   //   println("JarSource: " + path)
//   //   import JarSource._
//   //   
//   //   val jar = new JarFile(path)
//   //   val files = new HashMap[String, JarEntry]
//   //   val directories: HashMap[String, HashSet[String]] = new HashMap[String, HashSet[String]] {
//   //     override def default(key: String) = {
//   //       directories(key) = new HashSet[String]
//   //       directories(key)
//   //     }
//   //   }
//   //   
//   //   private def addFile(entry: JarEntry) = {
//   //     files("/" + entry.getName) = entry
//   //     val (dir, file) = dirAndFile(entry.getName)
//   //     directories(dir) += file
//   //   }
//   //   
//   //   private def addDir(entry: JarEntry) = {
//   //     val segments = entry.getName.split('/').filter(_ != "").toList
//   // 
//   //     for (i <- 0 to segments.length) {
//   //       val dir = "/" + segments.take(i).mkString("/")
//   //       if (i < segments.length)
//   //         directories(dir) += segments(i)
//   //       else if (!directories.contains(dir))
//   //         directories(dir) = new HashSet[String]
//   //     }
//   //   }
//   //   
//   //   def init() = {
//   //     val entries = jar.entries
//   //     while (entries.hasMoreElements) {
//   //       entries.nextElement match {
//   //         case e if e.isDirectory => addDir(e)
//   //         case e                  => addFile(e)
//   //       }
//   //     }
//   //   }
//   //   
//   //   // constructor
//   //   init
//   // }
//   //   
//   // 
//   // override def open(path: String, fi: StructFuseFileInfo): Int =
//   //   if (jc.dir(path).isEmpty && jc.file(path).isEmpty) -ENOENT
//   //   else if ((fi.flags & O_ACCMODE) != O_RDONLY) -EACCES
//   //   else 0
//   // 
//   // override def read(path: String, buf: Pointer, size: Int, offset: Long, fi: StructFuseFileInfo): Int = {
//   //   val content = jc.contents(path)
//   //   if (content == null) return -ENOENT
//   //   
//   //   val sizeToWrite: Int =
//   //     if (offset + size > content.length) content.length - offset.toInt
//   //     else size
//   //   
//   //   println("sizeToWrite = " + sizeToWrite)
//   //   buf.write(0, content, offset.toInt, sizeToWrite)
//   //   sizeToWrite
//   // }
//   // 
//   //   
//   // override def readdir(path: String, buf: Pointer, filler: FuseFillDirT, offset: Long, fi: StructFuseFileInfo): Int = {
//   //   jc.dir(path) match {
//   //     case Nil  => -ENOENT
//   //     case xs   => fillDirT(filler, buf, xs)
//   //   }
//   // }
//   // 
//   // override def statfs(path: String, buf: StructStatvfs): Int = {
//   //   buf.clear
//   //   buf.f_frsize = 512
//   //   buf.f_files = jc.totalFiles
//   //   buf.f_blocks = jc.totalSize / 512
//   // }
//   // 
//   // override def init(conn: StructFuseConnInfo): Pointer = {
//   //   debug = true
//   //   val jarfspath = System.getenv("JARFSPATH")
//   //   if (jarfspath == null) return null
//   //   
//   //   val cps = jarfspath.split(":")
//   //   val jarnames = 
//   //     cps.flatMap { x =>
//   //       val f = new java.io.File(x)
//   //       if (f.isDirectory) f.listFiles.filter(_.canRead).map(_.getPath).filter(_ endsWith ".jar")
//   //       else List(f.getPath)
//   //     }
//   //   
//   //   jarnames foreach { x => jc += new JarSource(x) }
//   //   // 
//   //   // cps foreach { s => 
//   //   //   val f = new java.io.File(s)
//   //   //   if (f.isDirectory) {
//   //   //     f.listFiles.filter(_.canRead).map(_.getPath).filter(_ endsWith ".jar").foreach { x => jc += new JarSource(x) }
//   //   //   }
//   //   //   else {
//   //   //     jc += new JarSource(s)
//   //   //   }
//   //   //   
//   //   //   jc += new JarSource(x)
//   //   // }
//   //   // 
//   //   null
//   // }
//   // 
//   // def main(argv: Array[String]): Unit = {
//   //   // System.getenv("JARFSPATH") match {
//   //   //   case null => jc += new JarSource(argv(0))
//   //   //   case path =>
//   //   //     for (s <- path.split(":")) {
//   //   //       println(s)
//   //   //       val f = new java.io.File(s)
//   //   //       if (f.isDirectory) {
//   //   //         f.listFiles.filter(_.canRead).map(_.getPath).filter(_ endsWith ".jar").foreach { x => jc += new JarSource(x) }
//   //   //       }
//   //   //       else {
//   //   //         jc += new JarSource(s)
//   //   //       }
//   //   //     }
//   //   // }
//   // 
//   //   start(Array(name) ++ argv)
//   // }
// }
