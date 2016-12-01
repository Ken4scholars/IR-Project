package engine

import scala.collection.mutable
import scala.io.Source
import engine.Utils._
import InvertedIndexImpl._
import breeze.linalg.SparseVector

/**
  * Created by Gregory on 28.11.16.
  */
object EvaluationRoutine {

  def main(args: Array[String]): Unit = {

    val queries = Source.fromFile("/Users/Gregory/Documents/Innopolis/IR/IR-Project/measuredqueries").getLines()
    val avgPrecisions = new Array[Double](queries.length)
    val terms = new mutable.HashSet[String]
    val evalDocsTfIdfScores = new mutable.HashMap[Int, SparseVector[Double]]

    val  metricsDocs = new mutable.HashMap[Int, String]()
    var count = 1
    var str = new StringBuilder()
    Source.fromFile("/Users/Gregory/Documents/Innopolis/IR/IR-Project/metricdocs").getLines().foreach(line => {
      if (!line.isEmpty) {
        if (line.replaceAll("\\s+","") == "/") {
          metricsDocs(count) = str.toString()
          str = new StringBuilder
          count += 1
        } else {
          str.append(line)
        }
      }
    })

    val relevantDocs = new mutable.HashMap[Int, mutable.Set[Int]]()
    count = 1
    Source.fromFile("/Users/Gregory/Documents/Innopolis/IR/IR-Project/relevantdocs").getLines().foreach(line => {
      var docs = new mutable.HashSet[Int]
      if (line == "/") {
        relevantDocs(count) = docs
        count += 1
        docs = new mutable.HashSet[Int]()
      } else {
        docs = docs | mutable.HashSet(line.split("\\s+").map(x => Integer.parseInt(x)) :_*)
      }
    })

    metricsDocs.foreach(t => stemTokens(tokenize(t._2)).foreach(term => terms.add(term)))
    metricsDocs.foreach(t => evalDocsTfIdfScores(t._1) = computeDocTfIdfScore(t._2))

    def getTopKSimilarDocs(query: String, numberOfDocs: Int): Seq[Int] = {
      val queryScore = queryTfIdfScore(query)
      evalDocsTfIdfScores
        .map(t => (t._1, t._2.dot(queryScore)))
        .toSeq
        .sortBy(- _._2)
        .take(numberOfDocs)
        .map(t => t._1)
    }

    count = 1
    queries.foreach(query => {
      val results = getTopKSimilarDocs(query, relevantDocs(count).size)
      avgPrecisions :+ calcPrecisionAtK(query, results)
    })

    println(avgPrecisions.sum / avgPrecisions.length)

  }

}
