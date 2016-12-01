package services

import javax.inject._

import engine.InvertedIndexImpl

/**
  * Created by kenneth on 29.11.16.
  */
@Singleton
class InitService {
//  private val dirname = "wikipedia_data"
  private val dirname = "/media/kenneth/documents/data"


  def init() = {
    if(!InvertedIndexImpl.initialized){
      print("Init started....")
      InvertedIndexImpl.init(dirname)
    }
  }

}
