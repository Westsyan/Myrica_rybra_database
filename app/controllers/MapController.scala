package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

@Singleton
class MapController@Inject()(cc:ControllerComponents) extends AbstractController(cc) {



  def annotation : Action[AnyContent]= Action{ implicit request=>
    Ok(views.html.map.annotation())
  }

  def evolutionary : Action[AnyContent] = Action{ implicit request=>
    Ok(views.html.map.evolutionary())
  }

  def genetic : Action[AnyContent] = Action{implicit request=>
    Ok(views.html.map.genetic())
  }

}
