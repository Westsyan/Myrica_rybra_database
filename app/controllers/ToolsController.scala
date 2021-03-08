package controllers

import java.io.File
import java.nio.file.Files

import javax.inject.{Inject, Singleton}
import org.apache.commons.io.FileUtils
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsObject, Json}
import play.api.mvc._
import utils.{ExecCommand, Utils}

import scala.collection.JavaConverters._
import scala.collection.mutable

@Singleton
class ToolsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def blastBefore: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.tools.blast())
  }

  case class blastData(blastType: String, method: String, queryText: String, db: String, evalue: String, wordSize: String, maxTargetSeqs: String)

  val blastForm = Form(
    mapping(
      "blastType" -> text,
      "method" -> text,
      "queryText" -> text,
      "db" -> text,
      "evalue" -> text,
      "wordSize" -> text,
      "maxTargetSeqs" -> text
    )(blastData.apply)(blastData.unapply)
  )

  def blastRun = Action(parse.multipartFormData) { implicit request =>
    val data = blastForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val seqFile = new File(tmpDir, "seq.fa")
    data.method match {
      case "text" =>
        FileUtils.writeStringToFile(seqFile, data.queryText)
      case "file" =>
        val file = request.body.file("file").get
        file.ref.moveTo(seqFile, replace = true)
    }

    val outXml = new File(tmpDir, "out.xml")
    val outHtml = new File(tmpDir, "out.html")
    val outTable = new File(tmpDir, "table.xls")
    val execCommand = new ExecCommand

    val blastType = data.blastType match {
      case "blastn" => "blastn"
      case "blastp" => "blastp"
      case "blastx" => "blastx"
    }


    val database = Utils.path + "/blastData/" + data.db +"/myrica"

    val command1 = Utils.path + "/tools/ncbi-blast-2.6.0+/bin/%s%s -query ".format(blastType, Utils.suffix) + seqFile.getAbsolutePath +
      " -db " + database + " -outfmt 5 -evalue " + data.evalue + " -max_target_seqs " + data.maxTargetSeqs +
      " -word_size " + data.wordSize + " -out " + outXml.getAbsolutePath
    val command2 = "python " + Utils.path + "/tools/blast2html/blast2html.py -i " + outXml.getAbsolutePath + " -o " +
      outHtml.getAbsolutePath + " --template %s/tools/blast2html/%s.jinja".format(Utils.path, blastType)
    val command3 = "perl " + Utils.path + "/tools/Blast2table -format 10 -xml " + outXml.getAbsolutePath + " -header "


    execCommand.exect(Array(command1, command2, command3), tmpDir)
    if (execCommand.isSuccess) {
      val excel = execCommand.getOutStr
      FileUtils.writeStringToFile(outTable,excel)
      Ok(Json.obj("html" -> tmpDir.replaceAll("\\\\", "/"), "excel" -> excel, "types" -> data.db))
    } else {
      Utils.deleteDirectory(tmpDir)
      Ok(Json.obj("valid" -> "false", "message" -> execCommand.getErrStr))
    }
  }

  def blastResultBefore(path: String,types:String): Action[AnyContent] = Action { implicit request =>
    Ok(views.html.tools.blastResult(path,types))
  }

  def blastResult(path: String): Action[AnyContent] = Action { implicit request =>
    var html = Utils.readFileToString(new File(path + "/out.html"))

    //由于MORELLA建库不成功，所以加了前缀
    if(html.contains("morella")||html.contains("MORELLA")){
     html = html.replaceAll("morella","").replaceAll("MORELLA","")
    }
    Utils.deleteDirectory(path)
    Ok(Json.obj("html" -> (html + "\n" + Utils.scriptHtml)))
  }


  def seqFetchBefore: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.tools.seqFetch())
  }

  case class regData(species: String, region: String)

  val regForm = Form(
    mapping(
      "species" -> text,
      "region" -> text
    )(regData.apply)(regData.unapply)
  )

  def seqRegion: Action[AnyContent] = Action { implicit request =>
    val data = regForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val outFile = new File(tmpDir, "data.txt")
    val execCommand = new ExecCommand()
    val command = if (new File(Utils.windowsPath).exists()) {
      Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/" + "Myrica.genome" + " " + data.region
    } else {
      "samtools faidx " + Utils.path + "/blastData/" + "Myrica.genome" + " " + data.region
    }
    execCommand.execo(command, outFile)
    val (status, dataStr) = if (execCommand.isSuccess) (1, Utils.readFileToString(outFile)) else (0, "")
    Utils.deleteDirectory(tmpDir)
    Ok(Json.obj("status" -> status, "data" -> dataStr, "message" -> execCommand.getErrStr))
  }


  def enrichmentBefore: Action[AnyContent] = Action { implicit request =>
    Ok(views.html.tools.enrichment())
  }

  case class enrichData(method: String, dataType: String, gene: String, db: String, pValue: String)

  val enrichForm = Form(
    mapping(
      "method" -> text,
      "dataType" -> text,
      "gene" -> text,
      "db" -> text,
      "pValue" -> text
    )(enrichData.apply)(enrichData.unapply)
  )


  def enrichment = Action(parse.multipartFormData) { implicit request =>
    val data = enrichForm.bindFromRequest.get
    val tmpDir = Files.createTempDirectory("tmpDir").toString
    val seqFile = new File(tmpDir, "tmp.txt")
    data.dataType match {
      case "text" =>
        val geneId = data.gene.split(",").map(_.trim).distinct.toBuffer
        FileUtils.writeLines(seqFile, geneId.asJava)
      case "file" =>
        val file = request.body.file("file").get
        file.ref.moveTo(seqFile, replace = true)
    }

    data.method match {
      case "kegg" => kegg(data, tmpDir, seqFile.getAbsolutePath)
      case "go" => go(data, tmpDir, seqFile.getAbsolutePath)
    }
  }

  def kegg(data: enrichData, tmpDir: String, study: String): Result = {

    val population = Utils.enrichPath + "gene.txt"
    val association = Utils.enrichPath + "kegg.txt"

    val output = new File(tmpDir, "KEGG_enrichment.txt")
    val o = output.getAbsolutePath
    // println(study,population,association,m,n,o,c,pval)
    //QVALUE在unix转译文本后可以使用
    val execCommand = new ExecCommand
    val command = "perl " + Utils.path + "/tools/identify.pl -study=" + study + " -population=" + population +
      " -association=" + association + " -m=b" + " -n=BH" + " -o=" + o + " -c=5" + " -maxp=" + data.pValue
    execCommand.exect(command, tmpDir)

    val (status, jsons) = if (execCommand.isSuccess) {
      val keggInfo = Utils.readLines(output)
      val json = keggInfo.filter(_.split("\t").length == 9).map { x =>
        val all = x.split("\t")
        val hyper = "<a target='_blank' href='" + all(8) + "'>linked</a><a style='display: none'>" + all(8) + "</a>"
        Json.obj("term" -> all.head, "database" -> all(1), "id" -> all(2), "input_num" -> all(3), "back_num" -> all(4),
          "p-value" -> all(5), "correct_pval" -> all(6), "input" -> all(7), "hyperlink" -> hyper)
      }.drop(1)
      (1, json)
    } else (0, mutable.Buffer[JsObject]())

    Utils.deleteDirectory(tmpDir)
    Ok(Json.obj("status" -> status, "data" -> jsons, "message" -> execCommand.getErrStr))
  }

  def go(data: enrichData, tmpDir: String, study: String): Result = {

    val population = Utils.enrichPath + "gene.txt"
    val association = Utils.enrichPath + "go.txt"

    val o = new File(tmpDir, "GO_enrichment.txt")
    val execCommand = new ExecCommand
    val command = "python " + Utils.path + "/tools/goatools-0.5.7/scripts/find_enrichment.py --alpha=0.05 " +
      " --pval=" + data.pValue + " --output " + o.getAbsolutePath + " " + study + " " + population + " " + association
    execCommand.exect(command, tmpDir)
    val (status, jsons) = if (execCommand.isSuccess) {
      val goInfo = Utils.readLines(o).drop(1)
      val json = goInfo.map { x =>
        val all = x.split("\t")
        val goLink = "<a target='_blank' href='http://amigo.geneontology.org/amigo/term/" + all(0) + "'>" + all(0) + "</a>"
        Json.obj("id" -> goLink, "enrichment" -> all(1), "description" -> all(2), "ratio_in_study" -> all(3),
          "ratio_in_pop" -> all(4), "p_uncorrected" -> all(5), "p_bonferroni" -> all(6), "p_holm" -> all(7),
          "p_sidak" -> all(8), "p_fdr" -> all(9), "namespace" -> all(10), "genes_in_study" -> all(11))
      }
      (1, json)
    } else (0, mutable.Buffer[JsObject]())
    Utils.deleteDirectory(tmpDir)
    Ok(Json.obj("status" -> status, "data" -> jsons, "message" -> execCommand.getErrStr))
  }

}
