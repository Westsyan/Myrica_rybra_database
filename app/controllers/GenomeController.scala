package controllers

import javax.inject.{Inject, Singleton}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}

@Singleton
class GenomeController@Inject()(cc:ControllerComponents) extends AbstractController(cc){


  def conditionSearchBefore : Action[AnyContent] = Action{ implicit request=>
    Ok(views.html.genome.conditionSearch())
  }

}
