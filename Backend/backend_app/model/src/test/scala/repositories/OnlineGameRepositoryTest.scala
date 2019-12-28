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

  def createEmptyOnlineGame : OnlineGame = {
    //not to change harcoded string (need to test data consistency)
    val factsString = "[{\"fact\":{\"id\":6,\"fact\":\"fact6\",\"trueFact\":\"trueFact6\",\"falseFact\":\"falseFact6\",\"factVersion\":1},\"useTrueQuestion\":false},{\"fact\":{\"id\":16,\"fact\":\"fact16\",\"trueFact\":\"trueFact16\",\"falseFact\":\"falseFact16\",\"factVersion\":1},\"useTrueQuestion\":true},{\"fact\":{\"id\":12,\"fact\":\"fact12\",\"trueFact\":\"trueFact12\",\"falseFact\":\"falseFact12\",\"factVersion\":1},\"useTrueQuestion\":false},{\"fact\":{\"id\":19,\"fact\":\"fact19\",\"trueFact\":\"trueFact19\",\"falseFact\":\"falseFact19\",\"factVersion\":2},\"useTrueQuestion\":false},{\"fact\":{\"id\":12,\"fact\":\"fact12\",\"trueFact\":\"trueFact12\",\"falseFact\":\"falseFact12\",\"factVersion\":1},\"useTrueQuestion\":true}]"
    return OnlineGame(1, "test1", None, None, factsString, List(0,0,0,0,0).asJson.noSpaces, List(0,0,0,0,0).asJson.noSpaces, isGameStarted = false, 0)
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
    whenReady(repo.insert(createEmptyOnlineGame)) { result =>
      result shouldBe 1
    }
  }

  "OnlineGameRepositoryTest" should "get game room by gameRoomId" in {
    whenReady(repo.all) { games =>
      whenReady(repo.getRoomInfo(games(0).gameRoomId)) {
        case Some(game) => game.id shouldEqual games(0).id
        case _ => false shouldEqual true
      }
    }
  }

  "OnlineGameRepositoryTest" should "search for game with free spot for second user" in {
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