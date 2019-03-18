package org.incal.dl4j

import org.datavec.api.split.BaseInputSplit
import java.io.InputStream
import java.io.OutputStream
import java.util.{LinkedList, List}
import scala.collection.JavaConversions._

/**
  * @see org.datavec.api.split.CollectionInputSplit
  */
class IndexInputSplit(
  val baseString: String,
  val indeces: List[Integer]
) extends BaseInputSplit {
  uriStrings = new LinkedList[String]
  for (i <- indeces) {
    uriStrings.add(String.format(baseString, i))
  }

  override def updateSplitLocations(reset: Boolean) = {}

  override def needsBootstrapForWrite = false

  override def bootStrapForWrite(): Unit = {}

  @throws[Exception]
  override def openOutputStreamFor(location: String): OutputStream = null

  @throws[Exception]
  override def openInputStreamFor(location: String): InputStream = null

  override def length: Long = uriStrings.size

  override def reset(): Unit = {
    //No op
  }

  override def resetSupported = true
}