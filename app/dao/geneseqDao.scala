package dao

import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import models.Tables._

import scala.concurrent.{ExecutionContext, Future}

class geneseqDao@Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insertGeneSeq(row:Seq[GeneseqRow]) : Future[Unit] = db.run(Geneseq ++= row).map(_=>())

  def getSeqById(id:Int) : Future[GeneseqRow] = db.run(Geneseq.filter(_.id === id).result.head)
}
