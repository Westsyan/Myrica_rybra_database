package controllers

import dao.{expressionDao, geneseqDao, genomeDao, germDao}
import javax.inject.{Inject, Singleton}
import models.Tables.{ExpressionRow, GeneseqRow, GenomeRow, GermRow}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.Utils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class InsertController@Inject()(genomedao:genomeDao,geneseqdao:geneseqDao,expressiondao:expressionDao,
                                germdao:germDao, cc:ControllerComponents) extends AbstractController(cc) {


  def insertGenome = Action{
    val info = Utils.readLines("F:\\database\\MRDB\\data/Myrica.gff")
    val func = Utils.readLines("F:\\database\\MRDB\\data/function.txt")

    val infoMap = info.map(_.split("\t")).map(x=> (x.head,x.drop(2)))
    val funcMap = func.map(_.split("\t")).map(x=> (x.head,x.tail)).toMap

    val genome = infoMap.map{x=>
      funcMap.getOrElse(x._1,None) match{
        case None => GenomeRow(0,x._1,x._2(0),x._2(1).toInt,x._2(2).toInt,x._2(3),"--","--","--","--","--","--","--","--","--","--")
        case y : Array[String] =>
          GenomeRow(0,x._1,x._2(0),x._2(1).toInt,x._2(2).toInt,x._2(3),y(0),y(1),y(2),y(3),y(4),y(5),y(6),y(7),y(8),y(9))
      }
    }
    Await.result(genomedao.insertGenome(genome),Duration.Inf)

    Ok(Json.toJson(1))
  }

  def insertGeneSeq = Action{

    val cds = Utils.readFileToString("F:\\database\\MRDB\\blastData/Myrica.fa")
    val pep = Utils.readFileToString("F:\\database\\MRDB\\blastData/Myrica.pep")

   val cdsMap = cds.split(">").tail.map{x=>
     val c = x.split("\n")
      (c.head.trim,c.last.trim)
    }

    val pepMap = pep.split(">").tail.map{x=>
      val c = x.split("\n")
      (c.head.trim,c.last.trim)
    }.toMap

   val row = cdsMap.map{x=>
      GeneseqRow(0,x._1,x._2,pepMap(x._1))
    }

    Await.result(geneseqdao.insertGeneSeq(row),Duration.Inf)

    Ok(Json.toJson(1))
  }

  def insertExpression = Action{
    val f = Utils.readLines("D:\\杨梅数据库平台资料\\杨梅数据内容\\BMK160513-B890-浙江省农科院30个杨梅转录组测序/All_gene_fpkm.list.xls")
    val gene = Await.result(genomedao.getAllGenome,Duration.Inf)
    val geneMap = gene.map(x=> (x.geneid,x.id)).toMap

    val row = f.tail.map(_.split("\t")).map{x=>
      val json = "{\"T01\":\"" + x(1) + "\",\"T02\":\"" + x(2) + "\",\"T03\":\"" + x(3) + "\",\"T04\":\"" + x(4) +"\",\"T05\":\"" + x(5) +
        "\",\"T06\":\"" + x(6) + "\",\"T07\":\"" + x(7) + "\"," + "\"T08\":\"" + x(8) + "\",\"T09\":\"" + x(9) + "\",\"T10\":\"" + x(10) +
        "\",\"T11\":\"" + x(11) + "\",\"T12\":\"" + x(12) + "\",\"T13\":\"" + x(13) + "\",\"T14\":\"" + x(14) + "\",\"T15\":\"" + x(15) +
        "\",\"T16\":\"" + x(16) + "\",\"T17\":\"" + x(17) + "\",\"T18\":\"" + x(18) + "\",\"T19\":\"" + x(19) + "\",\"T20\":\"" + x(20) +
        "\",\"T21\":\"" + x(21) + "\",\"T22\":\"" + x(22) + "\",\"T23\":\"" + x(23) + "\",\"T24\":\"" + x(24) + "\",\"T25\":\"" + x(25) +
        "\",\"T26\":\"" + x(26) + "\",\"T27\":\"" + x(27) + "\",\"T28\":\"" + x(28) + "\",\"T29\":\"" + x(29) + "\",\"T30\":\"" + x(30) + "\"}"

  //    val o = scala.util.parsing.json.JSON.parseRaw(json).get.asInstanceOf[Map[String, String]]

      val geneid = x.head.split("-").head

      (geneMap(geneid),geneid,json)
    }.groupBy(_._1).map{x=>
      val y = x._2.head
      ExpressionRow(y._1,y._2,y._3)
    }.toSeq
    println(row.length)

    Await.result(expressiondao.add(row),Duration.Inf)

    Ok(Json.toJson("1"))
  }

  def insertGerm = Action{
    val germ = Utils.readLines("D:\\杨梅数据库平台资料\\杨梅数据内容\\杨梅种质资源照片/germ.txt")
    val row = germ.map{x=>
      val c = x.split("\t")
      GermRow(0,c.head,c(1),c(2),c(3),c(4),c(5),c(6),c(7),c(8),c(9),c(10),c(11),c(12),c(13),c(14),c(15),c(16),c(17),
        c(18),c(19))
    }

    Await.result(germdao.insert(row),Duration.Inf)

    Ok(Json.toJson(""))
  }




}
