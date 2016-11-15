package controllers

import java.util
import javax.inject._

import play.api._
import play.api.mvc._

import searchAPI._
import services.DocumentService


/**
  * Created by kenneth on 11.11.16.
  */
class SearchController extends Controller{

  def search(query: String) = Action.async { implicit request =>
    resu
    Ok(views.html.results())
  }
}
