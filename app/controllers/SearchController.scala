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
class SearchController extends Controller{

//  case class queryForm(query: String)
//  val contactForm = Form(
//    mapping(
//      "query" -> Forms.nonEmptyText
//    )(queryForm.apply)(queryForm.unapply)
//  )
//  val queryForm = Form(
//    single("query" -> text)
//  )

  def search(query: String) = Action.async{ request =>
    val result = SearchEngine.searchResult(query)
    val docs = result.map { res =>
      val f: Future[Option[Document]] = DocumentService.getDocumentByID(res)
      f.collect { case Some(doc) => doc }
    }
    val futures = Future.sequence(docs.toTraversable)
    futures map{documents =>
      Ok(views.html.results(documents.toArray))
    }
  }

}
