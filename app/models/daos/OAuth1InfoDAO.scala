package models.daos

import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth1Info
import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.Imports._
import com.novus.salat.dao.SalatDAO
import mongoContext._
import org.bson.types.ObjectId

import play.api.Play.current

import scala.concurrent.Future

case class PersistentOAuth1Info(loginInfo: LoginInfo, authInfo: OAuth1Info)

object OAuth1InfoDAO extends SalatDAO[PersistentOAuth1Info, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db").get
  )("oauth1"))

class OAuth1InfoDAO extends DelegableAuthInfoDAO[OAuth1Info] {

  def save(loginInfo: LoginInfo, authInfo: OAuth1Info) = {
    OAuth1InfoDAO.save(PersistentOAuth1Info(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  def find(loginInfo: LoginInfo) = Future.successful {
    OAuth1InfoDAO.findOne($and(
      "loginInfo.providerID" -> loginInfo.providerID,
      "loginInfo.providerKey" -> loginInfo.providerKey
    )) match {
      case Some(persistentOAuth1Info) => Some(persistentOAuth1Info.authInfo)
      case None => None
    }
  }
}

