package dao



import javax.inject.Inject
import models.Tables._
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class genomeDao@Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit exec:ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  def insertGenome(row:Seq[GenomeRow]) : Future[Unit] = db.run(Genome ++= row).map(_=>())

  def getAllGenome : Future[Seq[GenomeRow]] = db.run(Genome.result)

  def getByGeneIds(ids:Seq[String]) : Future[Seq[GenomeRow]] = db.run(Genome.filter(_.geneid.inSetBind(ids)).result)

  def getByRegion(chr:String,start:Int,end:Int) : Future[Seq[GenomeRow]] =
    db.run(Genome.filter(_.chr === chr).filter(_.start > start).filter(_.end < end).result)

  def getById(id:Int) : Future[GenomeRow] = db.run(Genome.filter(_.id === id).result.head)

  def searchByInput(input: String): Future[Seq[GenomeRow]] = {
    val i = "%" + input + "%"
    db.run(Genome.filter(x => x.geneid.like(i) || x.go.like(i) || x.kegg.like(i) || x.cogAnno.like(i) ||
      x.kogAnno.like(i) || x.pfam.like(i) || x.swiss.like(i) || x.trembl.like(i) || x.nr.like(i)).result)
  }

  def updateSwiss(id:Int,swiss:String) : Future[Unit] ={
    db.run(Genome.filter(_.id === id).map(_.swiss).update(swiss)).map(_=>())
  }

  def updateTre(id:Int,tre:String) : Future[Unit] ={
    db.run(Genome.filter(_.id === id).map(_.trembl).update(tre)).map(_=>())
  }

}



