package services

import models._

import scala.concurrent.Future

/**
  * Created by kenneth on 15.11.16.
  */
object DocumentService {

  def addDocument(document: Document): Future[String] = {
    Document.add(document)
  }

  def deleteDocument(id: Long): Future[Int] = {
    Document.delete(id)
  }

  def getDocumentByID(id: Long): Future[Option[Document]] = {
    Document.getByID(id)
  }

  def getDocumentByUrl(url: String): Future[Option[Document]] = {
    Document.getByUrl(url)
  }


  def listAllDocuments: Future[Seq[Document]] = {
    Document.listAll
  }

}
