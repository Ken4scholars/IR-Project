package engine

import java.io.File

import breeze.linalg.{SparseVector, normalize}
import engine.Utils._

import scala.collection.mutable
import scala.io.Source

/**
  * Created by Gregory on 27.11.16.
  */
trait InvertedIndex {

  def getTopKSimilarDocs(query: String, numberOfDocs: Int): Seq[String]

}


object InvertedIndexImpl extends InvertedIndex {

  var initialized = false
  val terms = new mutable.HashSet[String] //vocabulary
  val docIdUrl = new mutable.HashMap[Int, String]()
  val termNumber = new mutable.HashMap[String, Int] //number of dimension for term
  val termNumberOfOccurrences = new mutable.HashMap[String, Int]() { override def default(key: String) = 0 }
  val docsTfIdfScores = new mutable.HashMap[Int, SparseVector[Double]]
  val invertedDocumentFrequency = new mutable.HashMap[String, Double]()

  val measuredQueries = new mutable.HashMap[String, Int]()
  val relevantDocs = new mutable.HashMap[Int, mutable.Set[Int]]()
  val metricsDocs = new mutable.HashMap[Int, String]()
  var numberOfMeasuredQueries = 0
  var precisionSum = 0.0
  var map = 0.0




  def init(dirName: String, measuredQueriesFileName: String, relevantDocsFileName: String, docsFileName: String) = {

    if (!initialized) {
      initialized = true
      val dir = new File(dirName)
      assert(dir.exists() && dir.isDirectory)
      dir.listFiles().foreach(fileRoutine)
      dir.listFiles().foreach(computeDocTfIdfScore)
      terms.foreach(term => {
        invertedDocumentFrequency(term) = math.log(terms.size / termNumberOfOccurrences(term))
      })

      initMetrics(measuredQueriesFileName, relevantDocsFileName, docsFileName)
    }

  }

  private def fileRoutine(f: File) = {
    val lines = Source.fromFile(f).getLines()
    docIdUrl(Integer.parseInt(f.getName.split(".")(0))) = lines.toSeq.head
    val fileTerms = mutable.HashSet(stemTokens(tokenize(lines.drop(1).mkString(" "))) :_*)

    fileTerms.foreach(term => {
      termNumberOfOccurrences(term) += 1
      if (!terms.contains(term)) {
        terms.add(term)
        termNumber(term) = terms.size - 1
      }
    })
  }


  def computeDocTfIdfScore(f: File) = {
    val vec = SparseVector.zeros[Double](terms.size)
    val lines = Source.fromFile(f).getLines()
    val text = lines.drop(1).mkString(" ")
    val fileTerms = mutable.HashSet(stemTokens(tokenize(text)) :_*)

    fileTerms.foreach(term => vec(termNumber(term)) = termFrequency(term, text)) //LNC variant

    docsTfIdfScores(Integer.parseInt(f.getName.split(".")(0))) = normalize(vec)
  }

  def computeDocTfIdfScore(text: String) = {
    val vec = SparseVector.zeros[Double](terms.size)
    val docTerms = mutable.HashSet(stemTokens(tokenize(text)) :_*)

    docTerms.foreach(term => vec(termNumber(term)) = termFrequency(term, text)) //LNC variant

    normalize(vec)
  }

  override def getTopKSimilarDocs(query: String, numberOfDocs: Int): Seq[String] = {
    val queryScore = queryTfIdfScore(query)
    docsTfIdfScores
      .map(t => (t._1, t._2.dot(queryScore)))
      .toSeq
      .sortBy(- _._2)
      .take(numberOfDocs)
      .map(t => t._1)
      .map(docId => docIdUrl(docId))
  }

  //LTC variant
  def queryTfIdfScore(query: String): SparseVector[Double] = {
    val vec = SparseVector.zeros[Double](terms.size)
    val queryTerms = stemTokens(tokenize(query))
    queryTerms.foreach(term => vec(termNumber(term)) = termFrequency(term, query) * invertedDocumentFrequency(term))

    normalize(vec)
  }

  def termFrequency(term: String, text: String): Double = { 1 + math.log(text.count(_ == term)) }

  def initMetrics(measuredQueriesFileName: String, relevantDocsFileName: String, docsFileName: String) = {

    var count = 1
    Source.fromFile(measuredQueriesFileName).getLines().foreach(line => {
      measuredQueries(line.toLowerCase) = count
      count += 1
    })

    count = 1
    Source.fromFile(relevantDocsFileName).getLines().foreach(line => {
      var docs = new mutable.HashSet[Int]
      if (line == "/") {
        relevantDocs(count) = docs
        count += 1
        docs = new mutable.HashSet[Int]()
      } else {
        docs = docs | mutable.HashSet(line.split("\\s+").map(x => Integer.parseInt(x)) :_*)
      }
    })

    count = 1
    var str = new StringBuilder()
    Source.fromFile(docsFileName).getLines().foreach(line => {
      if (!line.isEmpty) {
        if (line.replaceAll("\\s+","") == "/") {
          metricsDocs(count) = str.toString()
          count += 1
          str = new StringBuilder
        } else {
          str.append(line)
        }
      }
    })
  }

  def calcPrecisionAtK(query: String, retrievedDocs: Seq[Int]): Double = {
    if (measuredQueries.contains(query.toLowerCase())) {
      val queryRelevantDocs = relevantDocs(measuredQueries(query.toLowerCase())).map(metricsDocs)
      var relevantCount = 0

      retrievedDocs.foreach(docId => {
        val retrievedDoc = Source.fromFile(docId.toString + ".txt").mkString(" ")
        queryRelevantDocs.foreach(relevantDoc => {
          if (retrievedDoc.startsWith(relevantDoc)) relevantCount += 1
        })

        numberOfMeasuredQueries += 1
        precisionSum += relevantCount / retrievedDocs.length
        map = precisionSum / numberOfMeasuredQueries
        relevantCount / retrievedDocs.length
      })
    }
    0.0
  }

}