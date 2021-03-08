package dao

import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class expressionDao@Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def add(row:Seq[ExpressionRow]) : Future[Unit] = {
    db.run(Expression ++= row).map(_=>())
  }

  def getAll : Future[Seq[ExpressionRow]] = {
    db.run(Expression.result)
  }

  def getById(id:Int) : Future[Seq[ExpressionRow]] = {
    db.run(Expression.filter(_.id === id).result)
  }
}
