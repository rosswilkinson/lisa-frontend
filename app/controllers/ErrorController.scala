/*
 * Copyright 2017 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, AnyContent}
import play.api.{Configuration, Environment, Play}
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

trait ErrorController extends FrontendController
  with Redirects {

  val accessDenied: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Forbidden(views.html.error.access_denied(ggLoginUrl + "?origin=lisa-api&continue=/lifetime-isa/register/organisation-details")))
  }

  val error: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(InternalServerError(views.html.error.internal_server_error()))
  }

}

object ErrorController extends ErrorController {
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}