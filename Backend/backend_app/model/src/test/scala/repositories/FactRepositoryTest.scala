package repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import entities.Fact
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class FactRepositoryTest
  extends TestKit(ActorSystem("test-system"))
      with FlatSpecLike
      with Matchers
      with ScalaFutures
      with H2 {

  implicit val ec: ExecutionContext = system.dispatcher

  val repo: FactRepository = new FactRepository(db)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(5 seconds)

  "FactRepository" should "handle a in memory database" in {
    whenReady(repo.all)(_.size shouldBe 0)
  }

  "FactRepository" should "insert 20 facts" in {
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

  "FactRepository" should "update a single entity" in {
    whenReady(repo.all) { facts =>
      val testEntity: Fact = repo.findById(facts(1).id).futureValue.get

      val result = repo.update(testEntity).futureValue

      result shouldBe 1
    }
  }


  /** versions tests START**/
  "FactRepository" should "0 version, offset 0, limit 20, returns all facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(0, 0, 20))(_.size shouldBe 20)
  }

  "FactRepository" should "1 version, offset 0, limit 20, returns 10 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(1, 0, 20))(_.size shouldBe 10)
  }

  "FactRepository" should "2 version, offset 0, limit 20, returns 0 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(2, 0, 20))(_.size shouldBe 0)
  }
  /** versions tests END**/


  /** limit, offset tests START**/
  "FactRepository" should "0 version, offset 0, limit 10, returns 10 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(0, 0, 10))(_.size shouldBe 10)
  }

  "FactRepository" should "0 version, offset 5, limit 20, returns 15 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(0, 5, 20))(_.size shouldBe 15)
  }

  "FactRepository" should "0 version, offset 10, limit 20, returns 10 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(0, 10, 20))(_.size shouldBe 10)
  }

  "FactRepository" should "0 version, offset 10, limit 5, returns 5 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(0, 10, 5))(_.size shouldBe 5)
  }

  "FactRepository" should "0 version, offset 16, limit 10, returns 4 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(0, 16, 10))(_.size shouldBe 4)
  }

  "FactRepository" should "1 version, offset 5, limit 10, returns 5 facts" in {
    whenReady(repo.getAllGreaterThanVersionLimitBy(1, 5, 10))(_.size shouldBe 5)
  }
  /** limit, offset tests START**/

  "FactRepository" should "delete a single fact by id" in {
    whenReady(repo.all) { facts =>
      whenReady(repo.deleteById(facts(0).id)) { result =>
        result shouldBe 1
      }

      whenReady(repo.findById(facts(0).id)) { fact =>
        fact shouldBe None
      }

      whenReady(repo.all)(_.size shouldBe facts.size - 1)
    }
  }

  "FactRepository" should "drop all records" in {
    whenReady(repo.all) { facts =>
      whenReady(repo.deleteAll) { res =>
        res shouldBe facts.size
      }
      whenReady(repo.all)(_.size shouldBe 0)
    }
  }
}