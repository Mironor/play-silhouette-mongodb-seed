package controllers

import org.bson.types.ObjectId
import scaldi.{Injectable, Injector}

import scala.concurrent.Future
import play.api.mvc.Action
import play.api.libs.concurrent.Execution.Implicits._
import com.mohiva.play.silhouette.core._
import com.mohiva.play.silhouette.core.providers._
import com.mohiva.play.silhouette.core.utils.PasswordHasher
import com.mohiva.play.silhouette.core.services.{AvatarService, AuthInfoService}
import com.mohiva.play.silhouette.core.exceptions.AuthenticationException
import com.mohiva.play.silhouette.contrib.services.CachedCookieAuthenticator
import models.services.UserService
import models.User
import forms.SignUpForm

class SignUpController(implicit inj: Injector) extends Silhouette[User, CachedCookieAuthenticator] with Injectable {

  val env = inject[Environment[User, CachedCookieAuthenticator]]

  val userService = inject[UserService]
  val authInfoService = inject[AuthInfoService]
  val avatarService = inject[AvatarService]
  val passwordHasher = inject[PasswordHasher]

  /**
   * Registers a new user.
   *
   * @return The result to display.
   */
  def signUp = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(

      formWithErrors => Future.successful(BadRequest(views.html.signUp(formWithErrors))),

      data => {
        val loginInfo = LoginInfo(CredentialsProvider.Credentials, data.email)
        val authInfo = passwordHasher.hash(data.password)
        val user = User(
          userID = new ObjectId(),
          loginInfo = loginInfo,
          firstName = Some(data.firstName),
          lastName = Some(data.lastName),
          fullName = Some(data.firstName + " " + data.lastName),
          email = Some(data.email),
          avatarURL = None
        )

        for {
          avatar <- avatarService.retrieveURL(data.email)
          user <- userService.save(user.copy(avatarURL = avatar))
          authInfo <- authInfoService.save(loginInfo, authInfo)
          maybeAuthenticator <- env.authenticatorService.create(user)
        } yield {
          maybeAuthenticator match {
            case Some(authenticator) =>
              env.eventBus.publish(SignUpEvent(user, request, request2lang))
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              env.authenticatorService.send(authenticator, Redirect(routes.ApplicationController.index))
            case None => throw new AuthenticationException("Couldn't create an authenticator")
          }
        }
      }
    )
  }
}
