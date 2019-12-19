package main

import akka.http.scaladsl.server.Route

trait QuizApiTrait {

  def login: Route

  def register: Route

  def logout: Route

  def synchroniseFacts: Route

  def loadScores: Route

  def onlineGameRoom: Route

  def wsEcho: Route

}
