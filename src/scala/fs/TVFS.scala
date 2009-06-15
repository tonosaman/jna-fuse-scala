package org.improving.fuse

import java.net.{ URL, URLConnection }
import java.io.InputStream
import scala.xml._
import scala.xml.parsing._
// import scala.io.Source

import org.ccil.cowan.tagsoup.Parser
import org.w3c.dom.{ Node, Document }
import javax.xml.transform.dom.DOMResult
import javax.xml.transform.TransformerFactory
import javax.xml.transform.sax.SAXTransformerFactory
import org.xml.sax.InputSource

// mapreduce
// implicit def nodeToElem(node: org.w3c.dom.Node): scala.xml.Elem = { val writer = new StringWriter; TransformerFactory.newInstance.newTransformer.transform(new DOMSource(node), new StreamResult(writer)); XML.loadString(writer.toString) }


object TVFS /* extends ScalaFS */ {
  val showsPage = new URL("http://tvrss.net/shows/")
  // val showsPage = new URL("file:///tmp/ts.html")

  def showsXH(): NodeSeq = {  
    XhtmlParser(scala.io.Source.fromURL(showsPage))
  }
  
  def shows(): Elem = {
    // val conn = url.openConnection
    // XML.load(showsPage.openConnection.getInputStream)
    XML.loadString(cleanHTML(showsPage))
  }
  
  // http://blog.oroup.com/2006/11/05/the-joys-of-screenscraping/
  def cleanHTML(url: URL) = {
    val stf = TransformerFactory.newInstance.asInstanceOf[SAXTransformerFactory]
    val th = stf.newTransformerHandler

    // This dom result will contain the results of the transformation
    val dr = new DOMResult
    th.setResult(dr);
    val parser = new Parser
    parser setContentHandler th
    val stream = url.openConnection.getInputStream
  
    // This is where the magic happens to convert HTML to XML
    parser.parse(new InputSource(stream))
    nodeToString(dr.getNode)
  }
  
  // http://faq.javaranch.com/java/DocumentToString
  def nodeToString(node: Node): String = {
    import org.w3c.dom._
    import java.io._
    import javax.xml.transform._
    import javax.xml.transform.dom._
    import javax.xml.transform.stream._
    
    try {
      val source: Source = new DOMSource(node)
      val stringWriter = new StringWriter
      val result: Result = new StreamResult(stringWriter)
      val factory = TransformerFactory.newInstance
      val transformer: Transformer = factory.newTransformer
      transformer.transform(source, result)
      return stringWriter.getBuffer.toString
    } 
    catch {
      case e: TransformerConfigurationException => e.printStackTrace
      case e: TransformerException => e.printStackTrace
    }
    
    null
  }
}