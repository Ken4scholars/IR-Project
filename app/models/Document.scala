package models

import org.joda.time.DateTime
import play.api.libs.json.Json

import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.Play
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import mappings.SlickMapping.jodaDateTimeMapping


/**
  * Created by kenneth on 14.11.16.
  */
case class Document(id: Option[Long], title: String, url: String, summary: String)

object Document{
  implicit val documentFormat = Json.format[Document]


  class DocumentTable(tag: Tag) extends Table[Document](tag, "document"){

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def title = column[String]("title")
    def url = column[String]("url")
    def summary = column[String]("summary")

    def * = (id.?, title,  url, summary) <> ((Document.apply _).tupled, Document.unapply)

  }

  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)
  import dbConfig.driver.api._

  val documents = TableQuery[DocumentTable]

  def add(document: Document): Future[String] = {
    dbConfig.db.run(documents += document).map(res => "User successfully added").recover {
      case ex: Exception => ex.getCause.getMessage
    }
  }
  def delete(id: Long): Future[Int] = {
    dbConfig.db.run(documents.filter(_.id === id).delete)
  }

  def getByID(id: Long): Future[Option[Document]] = {
    dbConfig.db.run(documents.filter(_.id === id).result.headOption)
  }
  def getByUrl(url: String): Future[Option[Document]] = {
    dbConfig.db.run(documents.filter(_.url === url).result.headOption)
  }

  def listAll: Future[Seq[Document]] = {
    dbConfig.db.run(documents.result)
  }

}
