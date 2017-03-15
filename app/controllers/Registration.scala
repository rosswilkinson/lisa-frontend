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

import config.{FrontendAuthConnector, ShortLivedCache}
import models.{OrganisationDetails, LisaRegistration, TradingDetails, YourDetails}
import play.api.{Environment, Play}
import play.api.Play.current
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.Messages.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, _}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.frontend.Redirects
import uk.gov.hmrc.play.frontend.controller.FrontendController

import scala.concurrent.Future

object Registration extends Registration {
  val authConnector = FrontendAuthConnector
  val config = Play.current.configuration
  val env = Environment(Play.current.path, Play.current.classloader, Play.current.mode)
}

trait Registration extends FrontendController with AuthorisedFunctions with Redirects {

  val cacheId = "cacheID" // to be made dynamic later

  implicit val organisationDetailsFormats = Json.format[OrganisationDetails]
  implicit val tradingDetailsFormats = Json.format[TradingDetails]
  implicit val yourDetailsFormats = Json.format[YourDetails]
  implicit val registrationFormats = Json.format[LisaRegistration]

  private val organisationForm = Form(
    mapping(
      "companyName" -> nonEmptyText,
      "ctrNumber" -> nonEmptyText
    )(OrganisationDetails.apply)(OrganisationDetails.unapply)
  )

  private val tradingForm = Form(
    mapping(
      "tradingName" -> nonEmptyText,
      "fsrRefNumber" -> nonEmptyText,
      "isaProviderRefNumber" -> nonEmptyText
    )(TradingDetails.apply)(TradingDetails.unapply)
  )

  private val yourForm = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "role" -> nonEmptyText,
      "phone" -> nonEmptyText,
      "email" -> nonEmptyText
    )(YourDetails.apply)(YourDetails.unapply)
  )

  val organisationDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      ShortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, "organisationDetails").map {
        case Some(data) => Ok(views.html.registration.organisation_details(organisationForm.fill(data)))
        case None => Ok(views.html.registration.organisation_details(organisationForm))
      }

    } recoverWith {
      handleFailure
    }
  }

  val submitOrganisationDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      organisationForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.organisation_details(formWithErrors)))
        },
        data => {
          ShortLivedCache.cache[OrganisationDetails](cacheId, "organisationDetails", data)

          Future.successful(Redirect(routes.Registration.tradingDetails()))
        }
      )
    } recoverWith {
      handleFailure
    }
  }

  val tradingDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      ShortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, "tradingDetails").map {
        case Some(data) => Ok(views.html.registration.trading_details(tradingForm.fill(data)))
        case None => Ok(views.html.registration.trading_details(tradingForm))
      }

    } recoverWith {
      handleFailure
    }
  }

  val submitTradingDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      tradingForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.trading_details(formWithErrors)))
        },
        data => {
          ShortLivedCache.cache[TradingDetails](cacheId, "tradingDetails", data)

          Future.successful(Redirect(routes.Registration.yourDetails()))
        }
      )
    } recoverWith {
      handleFailure
    }
  }

  val yourDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      ShortLivedCache.fetchAndGetEntry[YourDetails](cacheId, "yourDetails").map {
        case Some(data) => Ok(views.html.registration.your_details(yourForm.fill(data)))
        case None => Ok(views.html.registration.your_details(yourForm))
      }

    } recoverWith {
      handleFailure
    }
  }

  val submitYourDetails: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {
      yourForm.bindFromRequest.fold(
        formWithErrors => {
          Future.successful(BadRequest(views.html.registration.your_details(formWithErrors)))
        },
        data => {
          ShortLivedCache.cache[YourDetails](cacheId, "yourDetails", data)

          Future.successful(Redirect(routes.Registration.summary()))
        }
      )
    } recoverWith {
      handleFailure
    }
  }

  val summary: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {

      // get organisation details
      ShortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, "organisationDetails").flatMap {
        case None => Future.successful(Redirect(routes.Registration.organisationDetails()))
        case Some(orgData) => {

          // get trading details
          ShortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, "tradingDetails").flatMap {
            case None => Future.successful(Redirect(routes.Registration.organisationDetails()))
            case Some(tradData) => {

              // get user details
              ShortLivedCache.fetchAndGetEntry[YourDetails](cacheId, "yourDetails").map {
                case None => Redirect(routes.Registration.yourDetails())
                case Some(yourData) => {
                  Ok(views.html.registration.summary(new LisaRegistration(orgData, tradData, yourData)))
                }
              }
            }
          }
        }
      }

    } recoverWith {
      handleFailure
    }
  }

  val submit: Action[AnyContent] = Action.async { implicit request =>
    authorised((Enrolment("IR-CT") or Enrolment("HMCE-VATDEC-ORG") or Enrolment("HMCE-VATVAR-ORG")) and AuthProviders(GovernmentGateway)) {

      // get organisation details
      ShortLivedCache.fetchAndGetEntry[OrganisationDetails](cacheId, "organisationDetails").flatMap {
        case None => Future.successful(Redirect(routes.Registration.organisationDetails()))
        case Some(orgData) => {

          // get trading details
          ShortLivedCache.fetchAndGetEntry[TradingDetails](cacheId, "tradingDetails").flatMap {
            case None => Future.successful(Redirect(routes.Registration.organisationDetails()))
            case Some(tradData) => {

              // get user details
              ShortLivedCache.fetchAndGetEntry[YourDetails](cacheId, "yourDetails").map {
                case None => Redirect(routes.Registration.yourDetails())
                case Some(yourData) => {
                  NotImplemented(Json.toJson[LisaRegistration](new LisaRegistration(orgData, tradData, yourData)))
                }
              }
            }
          }
        }
      }

    } recoverWith {
      handleFailure
    }
  }

  private def handleFailure(implicit request: Request[_]) = PartialFunction[Throwable, Future[Result]] {
    case _ : NoActiveSession => Future.successful(toGGLogin("/lifetime-isa/register/organisation-details"))

    // todo: dont assume any controller exception is related to auth - it may be an error in the application code
    case _ => Future.successful(Forbidden(views.html.error.access_denied()))
  }

}