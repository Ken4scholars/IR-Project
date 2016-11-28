package engine

import epic.features.PorterStemmer
import epic.preprocess.MLSentenceSegmenter

/**
  * Created by Gregory on 27.11.16.
  */
object Utils {

  def tokenize(text: String): Seq[String] = {
    val splitter = MLSentenceSegmenter.bundled().get
    val tokenizer = new epic.preprocess.TreebankTokenizer()

    splitter(text.toLowerCase()).flatMap(tokenizer)

  }

  def stemTokens(tokens: Seq[String]): Seq[String] = {
    val stemmer = new PorterStemmer()

    tokens.map(stemmer)

  }

  private def normalizedString(text: String): String = {
    stemTokens(tokenize(text)).mkString(" ")
  }




}
