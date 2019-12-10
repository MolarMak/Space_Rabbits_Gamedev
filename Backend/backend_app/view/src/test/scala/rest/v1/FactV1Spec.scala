package rest.v1

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import apiVersions.v1.QuizApiV1
import controllers.Errors
import entities.Fact
import io.circe.parser.decode
import models.FactResponse
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}
import repositories.FactRepository
import slick.jdbc.PostgresProfile.api._

class FactV1Spec extends WordSpec with Matchers with ScalatestRouteTest with ScalaFutures {

  val db = Database.forConfig("database")
  val errors = new Errors("en")

  val quizApi = new QuizApiV1(db)
  val repo: FactRepository = new FactRepository(db)

  "insert 20 facts" in {
    for(i <- 1 until 21) {
      whenReady(repo.insert(Fact(i, s"fact$i", s"trueFact$i", s"falseFact$i", if(i % 2 == 0) 1 else 2))) { result =>
        result shouldBe 1
      }
    }

    whenReady(repo.all) { facts =>
      whenReady(repo.findById(facts(0).id)) { fact =>
        fact.get.fact shouldBe "fact1"
      }
      whenReady(repo.findById(facts(1).id)) { user =>
        user.get.fact shouldBe "fact2"
      }
    }
  }

  "version 0, offset 0(10,20), limit 10, load all" in {
    Get("/api/v1/synchroniseFacts?version=0&offset=0&limit=10") ~> Route.seal(quizApi.synchroniseFacts) ~> check {
      status shouldEqual StatusCodes.OK
      decode[FactResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          json.data.offset shouldEqual 0
          json.data.limit shouldEqual 10
          json.data.hasNext shouldEqual true
          json.data.facts.size shouldEqual 10
        case _ => false shouldEqual true
      }
    }
    Get("/api/v1/synchroniseFacts?version=0&offset=10&limit=10") ~> Route.seal(quizApi.synchroniseFacts) ~> check {
      status shouldEqual StatusCodes.OK
      decode[FactResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          json.data.offset shouldEqual 10
          json.data.limit shouldEqual 10
          json.data.hasNext shouldEqual true
          json.data.facts.size shouldEqual 10
        case _ => false shouldEqual true
      }
    }
    Get("/api/v1/synchroniseFacts?version=0&offset=20&limit=10") ~> Route.seal(quizApi.synchroniseFacts) ~> check {
      status shouldEqual StatusCodes.OK
      decode[FactResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          json.data.offset shouldEqual 20
          json.data.limit shouldEqual 10
          json.data.hasNext shouldEqual false
          json.data.facts.size shouldEqual 0
        case _ => false shouldEqual true
      }
    }
  }

  "version 1, offset 0(10), limit 10, load all version 2" in {
    Get("/api/v1/synchroniseFacts?version=1&offset=0&limit=10") ~> Route.seal(quizApi.synchroniseFacts) ~> check {
      status shouldEqual StatusCodes.OK
      decode[FactResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          json.data.offset shouldEqual 0
          json.data.limit shouldEqual 10
          json.data.hasNext shouldEqual true
          json.data.facts.size shouldEqual 10
          json.data.facts.count(_.factVersion > 1) shouldEqual 10
        case _ => false shouldEqual true
      }
    }
    Get("/api/v1/synchroniseFacts?version=1&offset=10&limit=10") ~> Route.seal(quizApi.synchroniseFacts) ~> check {
      status shouldEqual StatusCodes.OK
      decode[FactResponse](responseAs[String]) match {
        case Right(json) =>
          json.result shouldEqual true
          json.data.offset shouldEqual 10
          json.data.limit shouldEqual 10
          json.data.hasNext shouldEqual false
          json.data.facts.size shouldEqual 0
        case _ => false shouldEqual true
      }
    }
  }

  "drop all records" in {
    whenReady(repo.all) { facts =>
      whenReady(repo.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(repo.all)(_.size shouldBe 0)
    }
  }

}
