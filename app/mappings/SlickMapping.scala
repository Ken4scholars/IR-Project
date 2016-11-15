package mappings


import org.joda.time.DateTime
import java.sql.Timestamp
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import play.api.Play.current

/**
  * Created by kenneth on 15.11.16.
  */
object SlickMapping {
  protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile](current)
  import dbConfig.driver.api._

  implicit val jodaDateTimeMapping = {
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts))
  }
}