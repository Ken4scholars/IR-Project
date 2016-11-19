package engine

/**
  * Created by kenneth on 15.11.16.
  */
trait Engine {
  def searchResult(query: String): Array[Int]

}

object SearchEngine extends Engine {
  override def searchResult(query: String): Array[Int] = 1.until(13).toArray
}
