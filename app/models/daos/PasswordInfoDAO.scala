package models.daos

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.PasswordInfo
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import mongoContext._
import play.api.Play.current

import scala.concurrent.Future

case class PersistentPasswordInfo(loginInfo: LoginInfo, authInfo: PasswordInfo)

object PasswordInfoDAO extends SalatDAO[PersistentPasswordInfo, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db").get
  )("passwords"))

class PasswordInfoDAO extends DelegableAuthInfoDAO[PasswordInfo] {

  def save(loginInfo: LoginInfo, authInfo: PasswordInfo) = {
    PasswordInfoDAO.save(PersistentPasswordInfo(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  def find(loginInfo: LoginInfo) = Future.successful {
    PasswordInfoDAO.findOne($and(
      "loginInfo.providerID" -> loginInfo.providerID,
      "loginInfo.providerKey" -> loginInfo.providerKey
    )) match {
      case Some(persistentPasswordInfo) => Some(persistentPasswordInfo.authInfo)
      case None => None
    }
  }
}


