package models.daos

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.core.LoginInfo
import com.mohiva.play.silhouette.core.providers.OAuth2Info
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoConnection
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import mongoContext._
import play.api.Play.current

import scala.concurrent.Future

case class PersistentOAuth2Info(loginInfo: LoginInfo, authInfo: OAuth2Info)

object OAuth2InfoDAO extends SalatDAO[PersistentOAuth2Info, ObjectId](
  collection = MongoConnection()(
    current.configuration.getString("mongodb.default.db").get
  )("oauth2"))

class OAuth2InfoDAO extends DelegableAuthInfoDAO[OAuth2Info] {

  def save(loginInfo: LoginInfo, authInfo: OAuth2Info) = {
    OAuth2InfoDAO.save(PersistentOAuth2Info(loginInfo, authInfo))
    Future.successful(authInfo)
  }

  def find(loginInfo: LoginInfo) = Future.successful {
    OAuth2InfoDAO.findOne($and(
      "loginInfo.providerID" -> loginInfo.providerID,
      "loginInfo.providerKey" -> loginInfo.providerKey
    )) match {
      case Some(persistentOAuth2Info) => Some(persistentOAuth2Info.authInfo)
      case None => None
    }
  }
}

