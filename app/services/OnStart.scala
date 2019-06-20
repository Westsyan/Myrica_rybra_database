package services

import javax.inject._

import play.api.Logger

@Singleton
class OnStart@Inject()() {


  Logger.info("开启成功！")

}
