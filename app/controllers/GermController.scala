package controllers

import java.io.File

import dao.germDao
import javax.inject.Inject
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import utils.Utils

import scala.concurrent.ExecutionContext

class GermController@Inject()(germdao:germDao,cc:ControllerComponents)
                             (implicit exec : ExecutionContext) extends AbstractController(cc) {


  def germBefore : Action[AnyContent] = Action{implicit request=>
    Ok(views.html.germ.browse())
  }

  def getAllGerm : Action[AnyContent] = Action.async{ implicit request=>
    germdao.getAllGerm.map{x=>
      val json = x.map{x=>
        val image = new File(Utils.path + "/images/" + x.id + ".JPG").exists()
        Json.obj("id" -> x.id,"name"->x.name,"classes" -> x.classes,"location" -> x.location,
          "longitude" -> x.longitude,"latitude" -> x.latitude,"germClasses" -> x.germClasses,
          "saveLocation"->x.saveLocation,"height"->x.height,"vigor"->x.vigor,"bladeShape"->x.bladeShape,
        "beginTime"->x.beginTime,"fullTime"->x.fullTime,"matureTime"->x.matureTime,"weight"->x.weight,"shape"->x.shape,
        "color"->x.color,"solubleSolid"->x.solubleSolid,"evaluate"->x.evaluate,"store"->x.store,"disease"->x.disease,
          "images" -> image)
      }
      Ok(Json.toJson(json))
    }
  }


  def moreGermInfo(id:Int) : Action[AnyContent] = Action.async{implicit request=>
    germdao.getById(id).map{x=>
      val image = new File(Utils.path + "/images/" + x.id + ".JPG").exists()
      val images =  if(image){
        x.id + ".JPG"
      }else{
        "false"
      }

      Ok(views.html.germ.moreInfo(x,images))
    }
  }

}
