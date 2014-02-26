package edu.umass.ciir.strepsimur.galago.termcounts

import edu.umass.ciir.strepsi.termcounts.TermCollectionCountsMap.{TermFreq, DocFreq, TermCollectionCounts}
import edu.umass.ciir.strepsimur.galago.GalagoSearcher

/**
 * User: dietz
 * Date: 2/25/14
 * Time: 4:34 PM
 */
class GalagoBackenedTermCounts(backgroundCollection:GalagoSearcher) {
  def collectionLength:Long = backgroundCollection.getUnderlyingRetrieval().getIndexPartStatistics("postings")
    .collectionLength

  def unigramCounts():TermCollectionCounts = {
    def fetchUnigram(term:String):(TermFreq, DocFreq) = {
      val stats = backgroundCollection.getStatistics(term)
      (stats.nodeFrequency, stats.nodeDocumentCount)
    }
    (collectionLength , fetchUnigram)
  }
  
  def bigramCounts():TermCollectionCounts = {
    def fetchBigram(bigram:String):(TermFreq, DocFreq) = {
      val stats = backgroundCollection.getStatistics(s"#ordered:1($bigram)")
      (stats.nodeFrequency, stats.nodeDocumentCount)
    }          
    (collectionLength , fetchBigram)
  }

  def windowbigramCounts():TermCollectionCounts = {
    def fetchBigram(bigram:String):(TermFreq, DocFreq) = {
      val stats = backgroundCollection.getStatistics(s"#ordered:8($bigram)")
      (stats.nodeFrequency, stats.nodeDocumentCount)
    }          
    (collectionLength , fetchBigram)
  }

}
