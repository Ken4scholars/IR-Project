package engine

/**
  * Created by Gregory on 27.11.16.
  */
trait Engine {
  def searchResult(query: String): Seq[String]
}

object EngineImpl extends Engine {

  override def searchResult(query: String): Seq[String] = InvertedIndexImpl.getTopKSimilarDocs(query, 100)

}

