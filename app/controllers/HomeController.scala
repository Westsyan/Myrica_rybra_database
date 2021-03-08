package controllers

import java.io.File

import dao.referDao
import javax.inject._
import models.Tables.ReferRow
import org.apache.commons.io.FileUtils
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{Await, ExecutionContext}
import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(referdao:referDao,cc: ControllerComponents)
                              (implicit exec:ExecutionContext) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index: Action[AnyContent] = Action { implicit request=>
    Ok(views.html.home.index())
  }

  def reference : Action[AnyContent] = Action {implicit request=>
    Ok(views.html.reference.reference())
  }

  def getReference : Action[AnyContent] = Action.async{implicit request=>
    //val ref = new File(Utils.path + "/reference").listFiles().map(x => Json.obj( "reference" -> x.getName))

    referdao.getAll.map{x=>
      val json = x.sortBy(_.date).reverse.map{y=>
        Json.obj("id" -> y.id,"reference" -> y.name,"english" -> y.english,"date" -> y.date,"author" -> y.author)
      }
      Ok(Json.toJson(json))
    }
  }


  def updateRefer = Action{

    val lines =  FileUtils.readLines(new File("D:\\杨梅数据库平台资料/newRefer.txt")).asScala

    lines.foreach{x=>
      val line = x.split("\t").map(_.trim)
      val id = line.head.tail.toInt
      val en = line.last
      val row = Await.result(referdao.getById(id),Duration.Inf)
      val newRow = ReferRow(row.id,row.name.trim,row.date,row.author,en)
      Await.result(referdao.update(row.id,newRow),Duration.Inf)
    }



    Ok(Json.toJson("1"))
  }


  def contactPage = Action{implicit request=>
    Ok(views.html.contact.contactPage())
  }

}
