package test

import play.api.libs.json.Json
import utils.Utils

import scala.util.parsing.json.JSON

object test02 {

  def main(args: Array[String]): Unit = {
    val f = Utils.readLines("D:\\杨梅数据库平台资料\\杨梅数据内容\\BMK160513-B890-浙江省农科院30个杨梅转录组测序/All_gene_fpkm.list.xls")

    val head = Array("T01","T02","T03","T04","T05","T06","T07","T08","T09","T10","T11","T12","T13","T14","T15","T16",
      "T17","T18","T19","T20","T21","T22","T23","T24","T25","T26","T27","T28","T29","T30")

    f.map(_.split("\t")).map{x=>
      val r = x.tail.zipWithIndex.map{y=>
        head(y._2)->y._1
      }.toMap

      val j = Json.toJson(r)
      println(j)
    }

    f.map(_.split("\t")).map{x=>
         val json = "{\"T01\":\"" + x(1) + "\",\"T02\":\"" + x(2) + "\",\"T03\":\"" + x(3) + "\",\"T04\":\"" + x(4) +"\",\"T05\":\"" + x(5) +
        "\",\"T06\":\"" + x(6) + "\",\"T07\":\"" + x(7) + "\"," + "\"T08\":\"" + x(8) + "\",\"T09\":\"" + x(9) + "\",\"T10\":\"" + x(10) +
        "\",\"T11\":\"" + x(11) + "\",\"T12\":\"" + x(12) + "\",\"T13\":\"" + x(13) + "\",\"T14\":\"" + x(14) + "\",\"T15\":\"" + x(15) +
        "\",\"T16\":\"" + x(16) + "\",\"T17\":\"" + x(17) + "\",\"T18\":\"" + x(18) + "\",\"T19\":\"" + x(19) + "\",\"T20\":\"" + x(20) +
        "\",\"T21\":\"" + x(21) + "\",\"T22\":\"" + x(22) + "\",\"T23\":\"" + x(23) + "\",\"T24\":\"" + x(24) + "\",\"T25\":\"" + x(25) +
        "\",\"T26\":\"" + x(26) + "\",\"T27\":\"" + x(27) + "\",\"T28\":\"" + x(28) + "\",\"T29\":\"" + x(29) + "\",\"T30\":\"" + x(30) + "\"}"
      //println(j)
      val j = JSON.parseFull(json)
      //println(j)
      val o = JSON.parseFull(json).get.asInstanceOf[Map[String, String]]

      val e = Json.toJson(o)
  //println(e.toString())
      (x.head,json)
    }
  }
}
