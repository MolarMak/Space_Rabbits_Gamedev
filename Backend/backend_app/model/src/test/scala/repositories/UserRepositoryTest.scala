package repositories

import akka.actor.ActorSystem
import akka.testkit.TestKit
import entities.User
import org.scalatest._
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.language.postfixOps

class UserRepositoryTest
  extends TestKit(ActorSystem("test-system"))
    with FlatSpecLike
    with Matchers
    with ScalaFutures
    with H2 {

  implicit val ec: ExecutionContext = system.dispatcher

  val repo: UserRepository = new UserRepository(db)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(5 seconds)

  "UserRepository" should "handle a in memory database" in {
    whenReady(repo.all)(_.size shouldBe 0)
  }

  "UserRepository" should "insert 2 users" in {
    whenReady(repo.insert(User(1, "test1", "password1", "token1"))) { result =>
      result shouldBe 1
    }
    whenReady(repo.insert(User(1, "test2", "password2", "token2"))) { result =>
      result shouldBe 1
    }

    whenReady(repo.all) { users =>
      whenReady(repo.findById(users(0).id)) { user =>
        user.get.login shouldBe "test1"
      }
      whenReady(repo.findById(users(1).id)) { user =>
        user.get.login shouldBe "test2"
      }
    }
  }

  "UserRepository" should "get username by id" in {
    whenReady(repo.all) { users =>
      whenReady(repo.getUserNameById(Some(users(0).id))) { user =>
        user.get shouldBe "test1"
      }
      whenReady(repo.getUserNameById(Some(users(1).id))) { user =>
        user.get shouldBe "test2"
      }
      whenReady(repo.getUserNameById(None)) { user =>
        user shouldBe None
      }
    }
  }

  "UserRepository" should "update a single entity" in {
    whenReady(repo.all) { users =>
      val testEntity: User = repo.findById(users(1).id).futureValue.get

      val result = repo.update(testEntity).futureValue

      result shouldBe 1
    }
  }

  "UserRepository" should "return user id on token" in {
    whenReady(repo.all) { users =>
      val user = users.find(_.token == "token1").get
      whenReady(repo.getUserIdByToken(user.token)) {
        case Some(userId) => userId shouldEqual user.id
        case _ => true shouldEqual false
      }
    }
  }

  "UserRepository" should "load user profile data by token" in {
    whenReady(repo.all) { users =>
      val user = users.find(_.token == "token1").get
      whenReady(repo.getUserProfileDataByToken(user.token)) {
        case Some(login) => login shouldEqual user.login
        case _ => true shouldEqual false
      }
    }
  }

  "UserRepository" should "check user token exists" in {
    whenReady(repo.all) { users =>
      val user = users.find(_.token == "token1").get
      whenReady(repo.checkToken(user.token)) {
        case true => true shouldEqual true
        case _ => true shouldEqual false
      }
      whenReady(repo.checkToken("notToken")) {
        case false => false shouldEqual false
        case _ => true shouldEqual false
      }
    }
  }

  /** test (checkAccount) function:
    * 1) Found user's token by login and pass
    * 2) Don't find user's token by login and pass
    */
  "UserRepository" should "search user by login and password" in {
    whenReady(repo.checkAccount("test1", "password1")) { token =>
      token shouldBe Some("token1")
    }
    whenReady(repo.checkAccount("test2", "12345")) { token =>
      token shouldBe None
    }
  }

  /** test (checkExists) function:
    * 1) Found user by login and pass
    * 2) Don't find user by login and pass
    */
  "UserRepository" should "check exists user by login and password" in {
    whenReady(repo.checkExists("test1", "password1")) { exists =>
      exists shouldBe true
    }
    whenReady(repo.checkExists("test2", "12345")) { exists =>
      exists shouldBe false
    }
  }

  /** test (logoutByToken) function:
    * 1) Logout user
    * 2) Check that token new
    */
  "UserRepository" should "check logout user function change token" in {
    whenReady(repo.all) { users =>
      whenReady(repo.logoutByToken("token2", "token123")) { success =>
        success shouldBe 1
      }
      whenReady(repo.findById(users(1).id)) { user =>
        user.get.token shouldBe "token123"
      }
    }
  }

  "UserRepository" should "delete a single user by id" in {
    whenReady(repo.all) { users =>
      whenReady(repo.deleteById(users(0).id)) { result =>
        result shouldBe 1
      }

      whenReady(repo.findById(users(0).id)) { user =>
        user shouldBe None
      }

      whenReady(repo.all)(_.size shouldBe 1)
    }
  }

  "UserRepository" should "drop all records" in {
    whenReady(repo.deleteAll) { res =>
      res shouldBe 1
    }
    whenReady(repo.all)(_.size shouldBe 0)
  }
}