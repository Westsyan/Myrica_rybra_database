package services

import dao.{expressionDao, genomeDao}
import javax.inject._
import models.Tables.GenomeRow
import play.api.Logger
import utils.TableUtils

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@Singleton
class OnStart@Inject()(genomedao:genomeDao,expressiondao:expressionDao) {


  Logger.info("开启成功！")

   TableUtils.genomeSeq = Await.result(genomedao.getAllGenome,Duration.Inf)

  Logger.info("GenomeSeq 添加成功")

  val genome : Seq[GenomeRow] = TableUtils.genomeSeq

  TableUtils.geneToId = genome.map(x=> x.geneid->x.id).toMap

  Logger.info("GeneToId 添加成功")


  TableUtils.chrMap = genome.map(_.chr).distinct.sortBy(_.drop(8).toInt)

  Logger.info("ChrSeq 添加成功")

  TableUtils.geneIdSeq = genome.map(_.geneid).distinct

  Logger.info("GeneIdSeq 添加成功")

  TableUtils.expressionMap = Await.result(expressiondao.getAll,Duration.Inf)

  Logger.info("ExpressionMap 添加成功")


}
