package controllers

import java.util
import javax.inject._

import play.api._
import play.api.mvc._
import searchAPI._

/**
  * Created by kenneth on 11.11.16.
  */
@Singleton
class SearchController @Inject()  extends Controller{

  def search = Action {
    Ok(views.html.results())
  }
}
