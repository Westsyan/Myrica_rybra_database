package controllers

import dao.expressionDao
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.TableUtils

import scala.concurrent.ExecutionContext

@Singleton
class ExpressionController @Inject()(cc: ControllerComponents,expressiondao:expressionDao)(implicit exec:ExecutionContext) extends AbstractController(cc) {



  def expressionBefore = Action{implicit request=>
    Ok(views.html.expression.expression())
  }

  def getAllExpression = Action{implicit request=>
    val page = TableUtils.pageForm.bindFromRequest.get
    val genome = TableUtils.expressionMap
    val orderX = TableUtils.dealDataByPage(genome,page)
    val total = orderX.size
    val tmpX = orderX.slice(page.offset,page.offset + page.limit)
    val row = tmpX.map{x=>
      val exp = scala.util.parsing.json.JSON.parseFull(x.expression).get.asInstanceOf[Map[String, String]]
      Json.obj("id" -> x.id,"geneid" -> x.geneid,"T01" -> exp("T01"),"T02" -> exp("T02"),"T03" -> exp("T03"),"T04" -> exp("T04"),
        "T05" -> exp("T05"), "T06" -> exp("T06"),"T07" -> exp("T07"),"T08" -> exp("T08"),"T09" -> exp("T09"),"T10" -> exp("T10"),
        "T11" -> exp("T11"),"T12" -> exp("T12"), "T13" -> exp("T13"),"T14" -> exp("T14"),"T15" -> exp("T15"),"T16" -> exp("T16"),
        "T17" -> exp("T17"),"T18" -> exp("T18"),"T19" -> exp("T19"), "T20" -> exp("T20"),"T21" -> exp("T21"),"T22" -> exp("T22"),
        "T23" -> exp("T23"),"T24" -> exp("T24"),"T25" -> exp("T25"),"T26" -> exp("T26"), "T27" -> exp("T27"),"T28" -> exp("T28"),
        "T29" -> exp("T29"),"T30" -> exp("T30"))
    }

    Ok(Json.obj("rows" -> row,"total" -> total))
  }


  def getColumnChartsData(id: Int) = Action.async { implicit request =>
    expressiondao.getById(id).map { x =>
      val exp = scala.util.parsing.json.JSON.parseFull(x.head.expression).get.asInstanceOf[Map[String, String]]
      val json = (0 to 2).map{x=>
        Json.obj("data" -> Seq(exp("T"+x+"1"),exp("T"+x+"2"),exp("T"+x+"3"),exp("T"+x+"4"),exp("T"+x+"5"),
          exp("T"+x+"6"),exp("T"+x+"7"),exp("T"+x+"8"),exp("T"+x+"9"),exp("T"+(x+10))).map(_.toDouble))
      }
      Ok(Json.toJson(json))
    }
  }


}
