import com.mohiva.play.silhouette.core.{Logger, SecuredSettings}
import play.api.i18n.{Messages, Lang}
import play.api.mvc.Results._
import play.api.GlobalSettings
import play.api.mvc.{Result, RequestHeader}
import scaldi.play.ScaldiSupport
import utils.di.{WebModule, SilhouetteModule}
import scala.concurrent.Future
import controllers.routes

/**
 * The global configuration.
 */
object Global extends GlobalSettings with ScaldiSupport with SecuredSettings with Logger {


  def applicationModule = new WebModule :: new SilhouetteModule

  /**
   * Called when a user is not authenticated.
   *
   * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthenticated(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.ApplicationController.signIn)))
  }

  /**
   * Called when a user is authenticated but not authorized.
   *
   * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
   *
   * @param request The request header.
   * @param lang The currently selected language.
   * @return The result to send to the client.
   */
  override def onNotAuthorized(request: RequestHeader, lang: Lang): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.ApplicationController.signIn).flashing("error" -> Messages("access.denied"))))
  }
}
