package repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import entities.{Fact, OnlineGame, User}
import io.circe.syntax._
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class OnlineGameRepositoryTest
  extends TestKit(ActorSystem("test-system"))
      with FlatSpecLike
      with Matchers
      with ScalaFutures
      with H2 {

  implicit val ec: ExecutionContext = system.dispatcher

  val repoUser: UserRepository = new UserRepository(db)
  val repoFact: FactRepository = new FactRepository(db)
  val repo: OnlineGameRepository = new OnlineGameRepository(db)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(5 seconds)

  def createEmptyOnlineGame(creatorUserId: Int) : OnlineGame = {
    //not to change harcoded string (need to test data consistency)
    val factsString = "[{\"id\":1,\"fact\":\"fact1\",\"trueFact\":\"trueFact1\",\"falseFact\":\"falseFact1\",\"factVersion\":2},{\"id\":2,\"fact\":\"fact2\",\"trueFact\":\"trueFact2\",\"falseFact\":\"falseFact2\",\"factVersion\":1},{\"id\":3,\"fact\":\"fact3\",\"trueFact\":\"trueFact3\",\"falseFact\":\"falseFact3\",\"factVersion\":2},{\"id\":4,\"fact\":\"fact4\",\"trueFact\":\"trueFact4\",\"falseFact\":\"falseFact4\",\"factVersion\":1},{\"id\":5,\"fact\":\"fact5\",\"trueFact\":\"trueFact5\",\"falseFact\":\"falseFact5\",\"factVersion\":2}]"
    return OnlineGame(1, "test1", creatorUserId, None, factsString, List(0,0,0,0,0).asJson.noSpaces, List(0,0,0,0,0).asJson.noSpaces, 0)
  }

  "OnlineGameRepositoryTest" should "handle a in memory database" in {
    whenReady(repo.all)(_.size shouldBe 0)
  }

  "OnlineGameRepositoryTest" should "insert 2 users, 20 facts, and 1 online game" in {
    for(i <- 1 until 3) {
      whenReady(repoUser.insert(User(i, s"login$i", s"password$i", s"token$i"))) { result =>
        result shouldBe 1
      }
    }
    for(i <- 1 until 21) {
      whenReady(repoFact.insert(Fact(i, s"fact$i", s"trueFact$i", s"falseFact$i", if(i % 2 == 0) 1 else 2))) { result =>
        result shouldBe 1
      }
    }
    whenReady(repoUser.getUserIdByToken("token1")) {
      case Some(userId) =>
        whenReady(repo.insert(createEmptyOnlineGame(userId))) { result =>
          result shouldBe 1
        }
      case _ => false shouldEqual true
    }
  }

  "OnlineGameRepositoryTest" should "search for game with free spot" in {
    whenReady(repo.searchFreeGameSpot) {
      case Some(game) =>
        whenReady(repoUser.getUserIdByToken("token2")) {
          case Some(userId) =>
            whenReady(repo.joinToGame(game.id, userId)) { result =>
              result shouldEqual 1
            }
          case _ => false shouldEqual true
        }
      case _ => false shouldEqual true
    }
    whenReady(repo.searchFreeGameSpot) { result =>
      result shouldEqual None
    }
  }

  "OnlineGameRepositoryTest" should "drop all records" in {
    whenReady(repo.all) { stats =>
      whenReady(repo.deleteAll) { res =>
        res shouldBe stats.size
      }
      whenReady(repo.all)(_.size shouldBe 0)
    }
    whenReady(repoUser.all) { users =>
      whenReady(repoUser.deleteAll) { res =>
        res shouldBe users.size
      }
      whenReady(repoUser.all)(_.size shouldBe 0)
    }
    whenReady(repoFact.all) { users =>
      whenReady(repoFact.deleteAll) { res =>
        res shouldBe users.size
      }
      whenReady(repoFact.all)(_.size shouldBe 0)
    }
  }
}