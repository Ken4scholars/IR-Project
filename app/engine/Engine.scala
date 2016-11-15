package engine

/**
  * Created by kenneth on 15.11.16.
  */
trait Engine {
  def searchResult(query: String): Seq[Int]

}
