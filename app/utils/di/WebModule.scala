package utils.di

import controllers.{SocialAuthController, SignUpController, CredentialsAuthController, ApplicationController}
import scaldi.Module

class WebModule extends Module{
  binding to new ApplicationController
  binding to new CredentialsAuthController
  binding to new SignUpController
  binding to new SocialAuthController
}
