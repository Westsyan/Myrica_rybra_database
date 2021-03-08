package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class germDao @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insert(row: Seq[GermRow]): Future[Unit] = {
    db.run(Germ ++= row).map(_ => ())
  }

  def getAllGerm: Future[Seq[GermusRow]] = {
    db.run(Germus.result)
  }

  def getById(id: Int): Future[GermusRow] = {
    db.run(Germus.filter(_.id === id).result.head)
  }

}
