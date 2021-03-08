package controllers

import java.io.File
import java.nio.file.Files

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents, RangeResult}
import utils.{TableUtils, Utils}

import scala.concurrent.ExecutionContext

@Singleton
class UtilsController @Inject()(cc: ControllerComponents)
                               (implicit exec: ExecutionContext) extends AbstractController(cc) {


  def getAllChr: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(TableUtils.chrMap))
  }

  def getAllGeneId: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(TableUtils.geneIdSeq))
  }

  def getEgGene: Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson(TableUtils.geneIdSeq.take(10)))
  }


  def getImageByPhotoId(name:String) = Action { implicit request =>
    val ifModifiedSinceStr = request.headers.get(IF_MODIFIED_SINCE)

    val path = Utils.path + "/images/" + name

    val file =if(new File(path).exists()){
      new File(path)
    }else{
      new File(Utils.path + "/images/zanwu.jpg")
    }

    val lastModifiedStr = file.lastModified().toString
    val MimeType = "image/jpg"
    val byteArray = Files.readAllBytes(file.toPath)
    if (ifModifiedSinceStr.isDefined && ifModifiedSinceStr.get == lastModifiedStr) {
      NotModified
    } else {
      Ok(byteArray).as(MimeType).withHeaders(LAST_MODIFIED -> file.lastModified().toString)
    }
  }

  def downloadExample(example: String) = Action { implicit request =>
    val filename = "\"" + example + "\""
    Ok.sendFile(new File(Utils.path + "/example/" + example)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + filename),
      CONTENT_TYPE -> "application/x-download"
    )
  }


  //断点续传
  def linkIgvData(path: String) = Action { implicit request =>
    RangeResult.ofFile(new File(Utils.path + "/igvData/" + path), request.headers.get(RANGE), Some("application/octet-stream"))
  }

  def download(file: String) = Action { implicit request =>
    val name = file.split("/").last
    val filename = "\"" + name + "\""
    Ok.sendFile(new File(Utils.path + "/download/" + file)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + filename),
      CONTENT_TYPE -> "application/x-download"
    )
  }

  def downloadPdf(file: String) = Action { implicit request =>
    val name = file.split("/").last
    val filename = new String(("attachment;filename=\"" + name + "\"").getBytes("GBK"), "ISO_8859_1")
    Ok.sendFile(new File(Utils.path + "/reference/" + file)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> filename,
      CONTENT_TYPE -> "application/x-download"
    )
  }

  def openPdf(file: String) = Action { implicit request =>
    val name = file.split("/").last
    val filename = new String(("filename=\"" + name + "\"").getBytes("GBK"), "ISO-8859-1")
    Ok.sendFile(new File(Utils.path + "/reference/" + file)).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> filename,
      CONTENT_TYPE -> "text/plain"
    )
  }

}

