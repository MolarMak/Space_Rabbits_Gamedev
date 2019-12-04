package repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import entities.{Statistic, User}
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class StatisticRepositoryTest
  extends TestKit(ActorSystem("test-system"))
      with FlatSpecLike
      with Matchers
      with ScalaFutures
      with H2 {

  implicit val ec: ExecutionContext = system.dispatcher

  val repoUser: UserRepository = new UserRepository(db)
  val repo: StatisticRepository = new StatisticRepository(db)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(5 seconds)

  "StatisticRepository" should "handle a in memory database" in {
    whenReady(repo.all)(_.size shouldBe 0)
  }

  "StatisticRepository" should "insert 2 users and 2 statistics" in {
    for(i <- 1 until 3) {
      whenReady(repoUser.insert(User(i, s"login$i", s"password$i", s"token$i"))) { result =>
        result shouldBe 1
      }
      whenReady(repoUser.getUserIdByToken(s"token$i")) {
        case Some(userId) =>
          whenReady(repo.insert(Statistic(1, 0, 0, userId))) { result =>
            result shouldBe 1
          }
        case _ => false shouldEqual true
      }
    }
  }

  "StatisticRepository" should "put statistic to user1 and check it" in {
    whenReady(repoUser.checkAccount(s"login1", s"password1")) {
      case Some(token) =>
        whenReady(repo.getUserStatistic(token)) {
          case Some(stat) =>
            stat.online shouldEqual 0
            stat.offline shouldEqual 0
            whenReady(repo.updateOffline(stat.id, 10)) { result =>
              result shouldEqual 1
            }
          case _ => false shouldEqual true
        }
        whenReady(repo.getUserStatistic(token)) {
          case Some(stat) =>
            stat.online shouldEqual 0
            stat.offline shouldEqual 10
          case _ => false shouldEqual true
        }
      case _ => false shouldEqual true
    }
  }

  "StatisticRepository" should "put statistic to user2 and check it" in {
    whenReady(repoUser.checkAccount(s"login2", s"password2")) {
      case Some(token) =>
        whenReady(repo.getUserStatistic(token)) {
          case Some(stat) =>
            stat.online shouldEqual 0
            stat.offline shouldEqual 0
            whenReady(repo.updateOffline(stat.id, 15)) { result =>
              result shouldEqual 1
            }
          case _ => false shouldEqual true
        }
        whenReady(repo.getUserStatistic(token)) {
          case Some(stat) =>
            stat.online shouldEqual 0
            stat.offline shouldEqual 15
          case _ => false shouldEqual true
        }
      case _ => false shouldEqual true
    }
  }

  "StatisticRepository" should "double check user1 and user2 statistics" in {
    whenReady(repoUser.checkAccount(s"login1", s"password1")) {
      case Some(token) =>
        whenReady(repo.getUserStatistic(token)) {
          case Some(stat) =>
            stat.online shouldEqual 0
            stat.offline shouldEqual 10
          case _ => false shouldEqual true
        }
      case _ => false shouldEqual true
    }
    whenReady(repoUser.checkAccount(s"login2", s"password2")) {
      case Some(token) =>
        whenReady(repo.getUserStatistic(token)) {
          case Some(stat) =>
            stat.online shouldEqual 0
            stat.offline shouldEqual 15
          case _ => false shouldEqual true
        }
      case _ => false shouldEqual true
    }
  }

  "StatisticRepository" should "drop all records" in {
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
  }
}