package controllers

import java.util
import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc._
import play.api.libs.json.Json
import services.DocumentService
import engine._
import models.Document
import play.api.data.Forms._
import play.api.data._

import views.html.helper.form

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Created by kenneth on 11.11.16.
  */
class SearchController extends Controller {

  var data: Array[Document] = Array.empty[Document]
  val pageLength = 10

  def search(query: String) = Action.async { request =>
    println(query)
    val result = EngineImpl.searchResult(query)
    val docs = result.map { res =>
      val f: Future[Option[Document]] = DocumentService.getDocumentByUrl(res)
      f.collect { case Some(doc) => doc }
    }
    val futures = Future.sequence(docs.toTraversable)
    futures map { documents =>
      data = documents.toArray
      Ok(views.html.results(data.slice(0, pageLength), data.length, 1, pageLength))
    }
  }

  def apiSearch(query: String) = Action.async { request =>
    println(query)
    val result = EngineImpl.searchResult(query)
    val docs = result.map { res =>
      val f: Future[Option[Document]] = DocumentService.getDocumentByUrl(res)
      f.collect { case Some(doc) => doc }
    }
    val futures = Future.sequence(docs.toTraversable)
    futures map { documents =>
      Ok(Json.toJson(documents.toArray))
    }
  }

  def searchList(query: String = "hello", page: Int) = Action { request =>
    if (data.isEmpty) Redirect(routes.SearchController.search(query))
    else {
      val beg = (page - 1) * pageLength
      val documents = data.slice(beg, beg + pageLength)
      val count = data.length
      Ok(views.html.results(documents, count, page, pageLength))
    }
  }

}
