package utils

import play.shaded.oauth.org.apache.commons.codec.digest.DigestUtils


object md5 {


  def  encryption(pwd : String) = {
    val md5 = DigestUtils.md5(pwd.getBytes())
    md5.reverse
  }


  def main(args: Array[String]): Unit = {
    val x = DigestUtils.md5Hex("pol")
    println(x.toUpperCase)
    println(DigestUtils.md5(x.getBytes()))
  }
}
