package controllers

import dao.{expressionDao, geneseqDao, genomeDao}
import javax.inject.{Inject, Singleton}
import models.Tables.GenomeRow
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

@Singleton
class GenomeController @Inject()(genomedao: genomeDao, geneseqdao: geneseqDao, expressiondao: expressionDao
                                 , cc: ControllerComponents)
                                (implicit exec: ExecutionContext) extends AbstractController(cc) {


  def browseBefore: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.genome.browse())
  }

  def getAllGenome: Action[AnyContent] = Action { implicit request =>
    val page = TableUtils.pageForm.bindFromRequest.get
    val genome = TableUtils.genomeSeq
    val orderX = TableUtils.dealDataByPage(genome, page)
    val total = orderX.size
    val tmpX = orderX.slice(page.offset, page.offset + page.limit)
    val row = tmpX.map { y =>
      val go = y.go match {
        case "--" => "--"
        case g =>
          g.split(";;").map { x =>
            val index = x.indexOf("GO:")
            x.slice(index, index + 10)
          }.mkString(";")
      }
      val kegg = y.kegg match {
        case "--" => "--"
        case k => k.take(6)
      }

      Json.obj("id" -> y.id, "geneid" -> y.geneid, "chr" -> y.chr, "start" -> y.start, "end" -> y.end,
        "strand" -> y.strand, "cogClass" -> y.cogClass, "cogAnno" -> y.cogAnno, "go" -> go, "kegg" -> kegg,
        "kogClass" -> y.kogClass, "kogAnno" -> y.kogAnno, "pfam" -> y.pfam, "swiss" -> y.swiss, "trembl" -> y.trembl,
        "nr" -> y.nr)
    }

    Ok(Json.obj("rows" -> row, "total" -> total))
  }

  def getJson(x: Seq[GenomeRow]) = {
    x.map { y =>
      val go = y.go match {
        case "--" => "--"
        case g =>
          g.split(";;").map { x =>
            val index = x.indexOf("GO:")
            x.slice(index, index + 10)
          }.mkString(";")
      }
      val kegg = y.kegg match {
        case "--" => "--"
        case k => k.take(6)
      }
      Json.obj("id" -> y.id, "geneid" -> y.geneid, "chr" -> y.chr, "start" -> y.start, "end" -> y.end,
        "strand" -> y.strand, "cogClass" -> y.cogClass, "cogAnno" -> y.cogAnno, "go" -> go, "kegg" -> kegg,
        "kogClass" -> y.kogClass, "kogAnno" -> y.kogAnno, "pfam" -> y.pfam, "swiss" -> y.swiss, "trembl" -> y.trembl,
        "nr" -> y.nr)
    }
  }

  def conditionSearchBefore: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.genome.conditionSearch())
  }

  case class geneIdData(gene: String)

  val geneIdForm = Form(
    mapping(
      "gene" -> text
    )(geneIdData.apply)(geneIdData.unapply)
  )

  def searchById: Action[AnyContent] = Action.async { implicit request =>
    val data = geneIdForm.bindFromRequest.get
    val id = data.gene.split(",").map(_.trim).distinct
    genomedao.getByGeneIds(id).map { x =>
      Ok(Json.toJson(getJson(x)))
    }
  }

  case class regionData(chr: String, start: String, end: String)

  val regionForm = Form(
    mapping(
      "chr" -> text,
      "start" -> text,
      "end" -> text
    )(regionData.apply)(regionData.unapply)
  )

  def searchByRegion: Action[AnyContent] = Action.async { implicit request =>
    val data = regionForm.bindFromRequest.get
    genomedao.getByRegion(data.chr, data.start.toInt, data.end.toInt).map { x =>
      Ok(Json.toJson(getJson(x)))
    }
  }

  def moreInfo(gene:String) = Action.async{implicit request=>
    genomedao.getById(
      gene match {
        case x if x.startsWith("ID") => gene.drop(3).toInt
        case x if x.startsWith("GeneId") => TableUtils.geneToId(gene.drop(7))
      }
    ).map { x =>
      val seq = Await.result(geneseqdao.getSeqById(x.id), Duration.Inf)
      val expre = Await.result(expressiondao.getById(x.id), Duration.Inf).nonEmpty
      Ok(views.html.genome.moreInfo(x, seq, expre))
    }
  }


  def charts(id: Int) = Action { implicit request =>
    Ok(views.html.genome.charts(id))
  }


  def updateSwiss = Action { implicit request =>
    genomedao.getAllGenome.map { x =>
      x.foreach { y =>
        if (y.swiss != "--") {
          val a1 = y.swiss.split("OS=").last
          val a2 = if(a1.contains("(")){
            a1.indexOf("(")
          }else{
            a1.indexOf("PE=")
          }
          val key = a1.take(a2)
          val s = y.swiss.replace( key,"<i>" + key + "</i>")
          println(y.id,key)
          Await.result(genomedao.updateSwiss(y.id, s), Duration.Inf)
        }
      }
    }
    Ok(Json.toJson("1"))
  }

  def updateSwiss2 = Action.async {
    genomedao.getAllGenome.map { x =>
      x.foreach { y =>
        if (y.swiss == "--OS=<i></i>--") {
          println(y.id)
          Await.result(genomedao.updateSwiss(y.id, "--"), Duration.Inf)
        }
      }
      Ok(Json.toJson("1"))
    }
  }

  def updateTrembl = Action { implicit request =>
    genomedao.getAllGenome.map { x =>
      x.foreach { y =>
        if (y.trembl != "--") {
          val a1 = y.trembl.split("OS=").last
          val a2 = if (a1.contains("(")) {
            a1.indexOf("(")
          } else {
            a1.indexOf("PE=")
          }
          val key = a1.take(a2)
          val s = y.trembl.replace(key, "<i>" + key + "</i>")
          println(y.id)
          Await.result(genomedao.updateTre(y.id, s), Duration.Inf)
        }
      }
    }
    Ok(Json.toJson("1"))
  }

}
