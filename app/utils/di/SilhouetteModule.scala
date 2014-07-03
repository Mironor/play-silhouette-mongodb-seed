package utils.di

import com.mohiva.play.silhouette.contrib.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.contrib.services._
import com.mohiva.play.silhouette.contrib.utils._
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.providers.oauth1._
import com.mohiva.play.silhouette.core.providers.oauth2._
import com.mohiva.play.silhouette.core.services._
import com.mohiva.play.silhouette.core.utils._
import com.mohiva.play.silhouette.core.{Environment, EventBus}
import models.User
import models.daos._
import models.services.UserService
import scaldi.Module

class SilhouetteModule extends Module {

  bind[PasswordInfoDAO] to new PasswordInfoDAO
  bind[OAuth1InfoDAO] to new OAuth1InfoDAO
  bind[OAuth2InfoDAO] to new OAuth2InfoDAO
  bind[UserDAO] to new UserDAO
  bind[UserService] to new UserService
  bind[DelegableAuthInfoDAO[PasswordInfo]] to new PasswordInfoDAO
  bind[DelegableAuthInfoDAO[OAuth1Info]] to new OAuth1InfoDAO
  bind[DelegableAuthInfoDAO[OAuth2Info]] to new OAuth2InfoDAO
  bind[CacheLayer] to new PlayCacheLayer
  bind[HTTPLayer] to new PlayHTTPLayer
  bind[IDGenerator] to new SecureRandomIDGenerator
  bind[PasswordHasher] to new BCryptPasswordHasher
  bind[EventBus] to new EventBus



  binding toProvider new CachedCookieAuthenticatorService(CachedCookieAuthenticatorSettings(
    cookieName = inject[String]("silhouette.authenticator.cookieName"),
    cookiePath = inject[String]("silhouette.authenticator.cookiePath"),
    secureCookie = inject[Boolean]("silhouette.authenticator.secureCookie"),
    httpOnlyCookie = inject[Boolean]("silhouette.authenticator.httpOnlyCookie"),
    cookieIdleTimeout = inject[Int]("silhouette.authenticator.cookieIdleTimeout"),
    cookieAbsoluteTimeout = Some(inject[Int]("silhouette.authenticator.cookieAbsoluteTimeout")),
    authenticatorExpiry = inject[Int]("silhouette.authenticator.authenticatorExpiry")
  ), inject[CacheLayer], inject[IDGenerator], Clock())

  bind[AuthInfoService] toProvider new DelegableAuthInfoService(inject[PasswordInfoDAO], inject[OAuth1InfoDAO], inject[OAuth2InfoDAO])

  bind[AvatarService] toProvider new GravatarService(inject[HTTPLayer])

  // Auth providers //
  bind[CredentialsProvider] toProvider new CredentialsProvider(inject[AuthInfoService], inject[PasswordHasher], Seq(inject[PasswordHasher]))

  bind[FacebookProvider] toProvider {
    val settings = OAuth2Settings(
      authorizationURL = inject[String]("silhouette.facebook.authorizationURL"),
      accessTokenURL = inject[String]("silhouette.facebook.accessTokenURL"),
      redirectURL = inject[String]("silhouette.facebook.redirectURL"),
      clientID = inject[String]("silhouette.facebook.clientID"),
      clientSecret = inject[String]("silhouette.facebook.clientSecret"),
      scope = Some(inject[String]("silhouette.facebook.scope"))
    )

    FacebookProvider(inject[CacheLayer], inject[HTTPLayer], settings)
  }

  bind[GoogleProvider] toProvider {
    val settings = OAuth2Settings(
      authorizationURL = inject[String]("silhouette.google.authorizationURL"),
      accessTokenURL = inject[String]("silhouette.google.accessTokenURL"),
      redirectURL = inject[String]("silhouette.google.redirectURL"),
      clientID = inject[String]("silhouette.google.clientID"),
      clientSecret = inject[String]("silhouette.google.clientSecret"),
      scope = Some(inject[String]("silhouette.google.scope"))
    )

    GoogleProvider(inject[CacheLayer], inject[HTTPLayer], settings)
  }

  bind[TwitterProvider] toProvider {
    val settings = OAuth1Settings(
      requestTokenURL = inject[String]("silhouette.twitter.requestTokenURL"),
      accessTokenURL = inject[String]("silhouette.twitter.accessTokenURL"),
      authorizationURL = inject[String]("silhouette.twitter.authorizationURL"),
      callbackURL = inject[String]("silhouette.twitter.callbackURL"),
      consumerKey = inject[String]("silhouette.twitter.consumerKey"),
      consumerSecret = inject[String]("silhouette.twitter.consumerSecret")
    )

    TwitterProvider(inject[CacheLayer], inject[HTTPLayer], new PlayOAuth1Service(settings), settings)
  }

  // Main env injection in each Controller //
  bind[Environment[User, CachedCookieAuthenticator]] toProvider {
    val credentialsProvider = inject[CredentialsProvider]
    val facebookProvider = inject[FacebookProvider]
    val googleProvider = inject[GoogleProvider]
    val twitterProvider = inject[TwitterProvider]

    Environment[User, CachedCookieAuthenticator](
      inject[UserService],
      inject[AuthenticatorService[CachedCookieAuthenticator]],
      Map(
        credentialsProvider.id -> credentialsProvider,
        facebookProvider.id -> facebookProvider,
        googleProvider.id -> googleProvider,
        twitterProvider.id -> twitterProvider
      ),
      inject[EventBus]
    )
  }
}
