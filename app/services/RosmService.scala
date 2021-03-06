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

package services

import connectors.{RosmJsonFormats, RosmConnector}
import controllers.routes
import models._
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.play.http.{HttpResponse, HeaderCarrier}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.control.NonFatal

trait RosmService extends RosmJsonFormats{

  val rosmConnector:RosmConnector

  private def handleErrorResponse(response:HttpResponse)  =  response.json.validate[DesFailureResponse] match {
      case failureResponse: JsSuccess[DesFailureResponse] =>
        Logger.error(s"Des FailureResponse : ${failureResponse.get.code}")
        Left(failureResponse.get.code)
      case _: JsError => Logger.error("JsError in Response")
        Left("INTERNAL_SERVER_ERROR")
    }

  def rosmRegister(businessStructure:BusinessStructure, orgDetails: OrganisationDetails)(implicit hc:HeaderCarrier): Future[Either[String,String]] =
  {
    val rosmRegistration = RosmRegistration("LISA",true,false,Organisation(orgDetails.companyName,businessStructure.businessStructure))

    rosmConnector.registerOnce(orgDetails.ctrNumber , rosmRegistration).map { res =>
      Logger.debug("response is : " + res.json)
      res.json.validate[RosmRegistrationSuccessResponse] match {
        case successResponse: JsSuccess[RosmRegistrationSuccessResponse] =>  Right(successResponse.get.safeId)
        case _ : JsError => handleErrorResponse(res)
      }
    }.recover {
      case NonFatal(ex: Throwable) =>
      {
        Logger.error(s"rosm registration error: ${ex.getMessage}")
        Left("INTERNAL_SERVER_ERROR")
      }

    }
  }


  def performSubscription(registration: LisaRegistration)(implicit hc:HeaderCarrier) : Future[Either[String,String]] = {

    val utr = registration.organisationDetails.ctrNumber
    val companyName = registration.organisationDetails.companyName
    val safeId = registration.organisationDetails.safeId.get
    val applicantDetails = ApplicantDetails(
      name = registration.yourDetails.firstName,
      surname = registration.yourDetails.lastName,
      position = registration.yourDetails.role,
      contactDetails = ContactDetails(
        phoneNumber = registration.yourDetails.phone,
        emailAddress = registration.yourDetails.email))

    rosmConnector.subscribe(
      lisaManagerRef = registration.tradingDetails.isaProviderRefNumber,
      lisaSubscribe = LisaSubscription(
        utr = utr,
        safeId = safeId,
        approvalNumber = registration.tradingDetails.fsrRefNumber,
        companyName = companyName,
        applicantDetails = applicantDetails)
    ).map(
      subscribed => subscribed.json.validate[DesSubscriptionSuccessResponse] match {
        case successResponse: JsSuccess[DesSubscriptionSuccessResponse] => Right(successResponse.get.subscriptionId)
        case _: JsError => handleErrorResponse(subscribed)
      })
  }


}
object RosmService extends RosmService{
 override val rosmConnector: RosmConnector = RosmConnector

}
