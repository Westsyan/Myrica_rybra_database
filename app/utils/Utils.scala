package utils

import java.io.File
import java.lang.reflect.Field
import java.text.SimpleDateFormat
import java.util.Date

import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api.libs.json.JsObject
import play.api.mvc.{AnyContent, Request}

import scala.collection.JavaConverters._
import scala.collection.mutable

object Utils {

  def random :String = {
    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890"
    var value = ""
    for (i <- 0 to 20){
      val ran = Math.random()*62
      val char = source.charAt(ran.toInt)
      value += char
    }
    value
  }


  val scriptHtml: String =
    """
      |<script>
      |	$(function () {
      |			    $("footer:first").remove()
      |        $("#content").css("margin","0")
      |       $(".linkheader>a").each(function () {
      |				   var text=$(this).text()
      |				   $(this).replaceWith("<span style='color: #222222;'>"+text+"</span>")
      |			   })
      |
      |      $("tr").each(function () {
      |         var a=$(this).find("td>a:last")
      |					var text=a.text()
      |					a.replaceWith("<span style='color: #222222;'>"+text+"</span>")
      |				})
      |
      |       $("p.titleinfo>a").each(function () {
      |				   var text=$(this).text()
      |				   $(this).replaceWith("<span style='color: #606060;'>"+text+"</span>")
      |			   })
      |
      |       $(".param:eq(1)").parent().hide()
      |       $(".linkheader").hide()
      |
      |			})
      |</script>
    """.stripMargin


  def dealDataByPage[T](x: Seq[T], page: PageData): Seq[T] = {
    val searchX = x.filter { y =>
      page.search match {
        case None => true
        case Some(text) =>
          val array = text.split("\\s+").map(_.toUpperCase).toBuffer
          val texts = y.getClass.getDeclaredFields.toBuffer.map { z: Field =>
            z.setAccessible(true)
            z.get(y).toString
          }.init
          array.forall { z =>
            texts.map(_.toUpperCase.indexOf(z) != -1).reduce(_ || _)
          }
      }
    }

    val sortX = page.sort match {
      case None => searchX
      case Some(y) =>
        val b = searchX.take(1000).forall { tmpData =>
          val method = tmpData.getClass.getDeclaredMethod(y)
          val value = method.invoke(tmpData).toString
          Utils.isDouble(value)
        }
        if (b) {
          searchX.sortBy { z =>
            val method = z.getClass.getDeclaredMethod(y)
            method.invoke(z).toString.toDouble
          }
        } else {
          searchX.sortBy { z =>
            val method = z.getClass.getDeclaredMethod(y)
            method.invoke(z).toString
          }
        }
    }

    val orderX = page.order match {
      case "asc" => sortX
      case "desc" => sortX.reverse
    }

    orderX

  }

  def getTime(startTime: Long)  :Double = {
    val endTime = System.currentTimeMillis()
    (endTime - startTime) / 1000.0
  }

  def deleteDirectory(direcotry: File) :Unit = {
    try {
      FileUtils.deleteDirectory(direcotry)
    } catch {
      case _ : Throwable =>
    }
  }

  def deleteDirectory(tmpDir: String):Unit = {
    val direcotry = new File(tmpDir)
    deleteDirectory(direcotry)
  }

  def readLines(path :String): mutable.Buffer[String] = {
    val file = new File(path)
    val encoding = EncodingDetect.getJavaEncode(path)
    FileUtils.readLines(file,encoding).asScala
  }

  def readLines(file:File) : mutable.Buffer[String] = {
    val encoding = EncodingDetect.getJavaEncode(file.getAbsolutePath)
    FileUtils.readLines(file,encoding).asScala
  }

  def readFileToString(path:String) :String  = {
    val file = new File(path)
    val encoding = EncodingDetect.getJavaEncode(path)
    FileUtils.readFileToString(file,encoding)
  }

  def readFileToString(file : File) : String = {
    val encoding = EncodingDetect.getJavaEncode(file.getAbsolutePath)
    FileUtils.readFileToString(file,encoding)
  }

  def isDouble(value: String): Boolean = {
    try {
      value.toDouble
    } catch {
      case _: Exception =>
        return false
    }
    true
  }

  def refer(request:Request[AnyContent]):String = {
    val header = request.headers.toMap
    header.filter(_._1 == "Referer").values.head.head
  }

  def date : DateTime = {
    val now = new Date()
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    val time = dateFormat.format(now)
    val date = new DateTime(dateFormat.parse(time).getTime)
    date
  }

  def date2 : String = {
    val now = new Date()
    val dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
    val date = dateFormat.format(now)
    date
  }

  var searchMap : mutable.HashMap[String,Seq[JsObject]] = mutable.HashMap()

  var searchTimeMap : mutable.HashMap[String,Long] = mutable.HashMap()



  val windowsPath = "I:\\MRDB"
  val linuxPath = "/mnt/vdb/xwq/projects/MRDB"
  val path : String= {
    if (new File(windowsPath).exists()) windowsPath else linuxPath
  }

  val suffix :String = {
    if (new File(windowsPath).exists()) ".exe" else " "

  }

  val tmpPath :String = path + "/tmp"

  val enrichPath :String = path + "/enrichData/"

}
