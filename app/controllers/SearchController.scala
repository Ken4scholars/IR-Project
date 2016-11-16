package controllers

import java.util
import javax.inject._

import play.api.libs.concurrent.Execution.Implicits._
import play.api._
import play.api.mvc._
import searchAPI._
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

  //  case class queryForm(query: String)
  //  val contactForm = Form(
  //    mapping(
  //      "query" -> Forms.nonEmptyText
  //    )(queryForm.apply)(queryForm.unapply)
  //  )
  //  val queryForm = Form(
  //    single("query" -> text)
  //  )
  //val page = 0
  var data: Array[Document] = Array.empty[Document]
  val pageLength = 5

  def search(query: String) = Action.async { request =>
    println(query)
    val result = SearchEngine.searchResult(query)
    val docs = result.map { res =>
      val f: Future[Option[Document]] = DocumentService.getDocumentByID(res)
      f.collect { case Some(doc) => doc }
    }
    val futures = Future.sequence(docs.toTraversable)
    futures map { documents =>
      data = documents.toArray
      Ok(views.html.result2(data.slice(0, pageLength), data.length, 1, pageLength))
    }
  }

  def searchList(query: String = "hello", page: Int) = Action { request =>
    if (data.isEmpty) Redirect(routes.SearchController.search(query))
    else {
      val beg = (page - 1) * pageLength
      val documents = data.slice(beg, beg + pageLength)
      //val notifications = Model.getNotifiactionsByUser(user, (page-1)*pageLength, pageLength)
      val count = data.length
      Ok(views.html.result2(documents, count, page, pageLength))
    }
  }

}
