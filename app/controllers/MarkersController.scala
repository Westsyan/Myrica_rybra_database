package controllers

import dao.markersDao
import javax.inject.{Inject, Singleton}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.ExecutionContext

@Singleton
class MarkersController@Inject()(markersdao:markersDao,cc:ControllerComponents)(implicit exec: ExecutionContext)
  extends AbstractController(cc) {

  def markers = Action{implicit request=>
    Ok(views.html.markers.marker())
  }

  def getAll = Action.async{implicit request=>
    markersdao.getAll.map{x=>
      val json = x.map{y=>
        Json.obj("name" -> y.name,"forward" -> y.forward,"reverse"->y.reverse,"tm"->y.tm,"bp"->y.bp,"na"->y.na,
          "ne"->y.ne,"h"->y.h,"pic"->y.pic)
      }
      Ok(Json.toJson(json))
    }
  }

}
