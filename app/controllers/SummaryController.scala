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

import config.{FrontendAuthConnector, LisaShortLivedCache}
import connectors.UserDetailsConnector
import play.api.Play.current
import play.api.i18n.Messages.Implicits._
import play.api.mvc.{Action, _}
import play.api.{Configuration, Environment, Play}
import services.{AuthorisationService, TaxEnrolmentService}

import scala.concurrent.Future

trait SummaryController extends LisaBaseController {

  val get: Action[AnyContent] = Action.async { implicit request =>
    authorisedForLisa { (cacheId) =>
      hasAllSubmissionData(cacheId) { (data) =>
        Future(Ok(views.html.registration.summary(data)))
      }
    }
  }

}

object SummaryController extends SummaryController {
  val config: Configuration = Play.current.configuration
  val env: Environment = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
  override val cache = LisaShortLivedCache
  override val authorisationService = AuthorisationService
}