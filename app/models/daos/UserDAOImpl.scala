package models.daos

import com.mohiva.play.silhouette.core.LoginInfo
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.novus.salat.dao.SalatDAO
import models.User
import mongoContext._
import org.bson.types.ObjectId
import play.api.Play.current

import scala.concurrent.Future


object UserMongoDAO extends SalatDAO[User, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db").get
  )("users"))

/**
 * Give access to the user object.
 */
class UserDAOImpl extends UserDAO {

  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo) = Future.successful(

    UserMongoDAO.findOne($and(
      "loginInfo.providerID" -> loginInfo.providerID,
      "loginInfo.providerKey" -> loginInfo.providerKey
    ))
  )

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: ObjectId) = Future.successful(UserMongoDAO.findOneById( userID))

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */

  def save(user: User) = {
    UserMongoDAO.save(user)
    Future.successful(user)
  }
}
