package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class referDao  @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {


  import profile.api._

  def getAll : Future[Seq[ReferRow]] = {
    db.run(Refer.result)
  }

  def getById(id:Int) : Future[ReferRow] = {
    db.run(Refer.filter(_.id === id).result.head)
  }

  def update(id:Int,row:ReferRow):Future[Unit] = {
    db.run(Refer.filter(_.id === id).update(row)).map(_=>())
  }


}
