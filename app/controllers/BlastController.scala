package controllers

import java.io.File

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, ControllerComponents}
import utils.Utils.windowsPath
import utils.{ExecCommand, Utils}

import scala.concurrent.ExecutionContext

@Singleton
class BlastController@Inject()(cc:ControllerComponents)
                              (implicit exec:ExecutionContext) extends AbstractController(cc){






  def downloadBlastByRange(name: String, range: String, blastType: String) = Action {
    val fasta = blastType match {
      case "gene" => "gene/Myrica.fa"
      case "genome" => "genome/Myrica.genome"
      case "pep" => "pep/Myrica.pep"
      case "cds" => "cds/Myrica.cds"
      case "genome2" => "genome2/Myrica.genome"
    }
    val suffix =  if(blastType == "genome2")"MORELLA" else ""

    val execCommand = new ExecCommand
    range.split("Range").tail.foreach { x =>
      val r = x.trim.split(":").last.split("to").map(_.trim).sortBy(_.toInt)
      val ra = suffix + name + ":" + r.mkString("-")
      val command = if (new File(windowsPath).exists()) {
        Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/" + fasta + " " + ra
      } else {
        "samtools faidx " + Utils.path + "/blastData/" + fasta + " " + ra
      }
      execCommand.exec(command)
    }

    val seq = execCommand.getOutStr.replaceAll(suffix,"")
    Ok(seq).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + name + ".fasta"),
      CONTENT_TYPE -> "application/x-download"
    )

  }

  def downloadBlastByName(name: String, blastType: String) = Action {

    val fasta = blastType match {
      case "gene" => "gene/Myrica.fa"
      case "genome" => "genome/Myrica.genome"
      case "pep" => "pep/Myrica.pep"
      case "cds" => "cds/Myrica.cds"
      case "genome2" => "genome2/Myrica.genome"
    }
    val suffix =  if(blastType == "genome2")"MORELLA" else ""
    val execCommand = new ExecCommand
    val ra = suffix + name

    val command = if (new File(windowsPath).exists()) {
      Utils.path + "/tools/samtools-0.1.19/samtools.exe faidx " + Utils.path + "/blastData/" + fasta + " " + ra
    } else {
      "samtools faidx " + Utils.path + "/blastData/" + fasta + " " + ra
    }

    execCommand.exec(command)
    val seq = execCommand.getOutStr.replaceAll(suffix,"")

    Ok(seq).withHeaders(
      //缓存
      CACHE_CONTROL -> "max-age=3600",
      CONTENT_DISPOSITION -> ("attachment; filename=" + name + ".fasta"),
      CONTENT_TYPE -> "application/x-download"
    )

  }




}
