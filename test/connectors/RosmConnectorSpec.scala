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

package connectors

import models._
import org.joda.time.DateTime
import org.mockito.Matchers._
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpPost, HttpResponse}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class RosmConnectorSpec extends PlaySpec
  with MockitoSugar
  with OneAppPerSuite
  with RosmJsonFormats {

  "Register Once endpoint" must {

    "return success" when {
      "rosm returns a success message" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.toJson(rosmSuccessResponse))
          )))

        doRegistrationRequest { response =>
          response mustBe rosmSuccessResponse
        }
      }
      "rosm returns a success message without a dob" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(
              Json.toJson(rosmSuccessResponseNoDob)
            )
          )))

        doRegistrationRequest {
          case s: RosmRegistrationSuccessResponse => {
            s.individual.getOrElse(fail()).dateOfBirth mustBe None
          }
          case _ => fail()
        }
      }
    }

    "return failure" when {
      "rosm returns a success status but a failure response" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.toJson(rosmFailureResponse)))))

        doRegistrationRequest { response =>
          response mustBe rosmFailureResponse
        }
      }
      "rosm returns a success status and an unexpected json response" in {
        when(mockHttpPost.POST[RosmRegistration, HttpResponse](any(), any(), any())(any(), any(), any())).
          thenReturn(Future.successful(HttpResponse(
            responseStatus = CREATED,
            responseJson = Some(Json.parse("{}")))))

        doRegistrationRequest { response =>
          response mustBe RosmRegistrationFailureResponse(
            code = "INTERNAL_SERVER_ERROR",
            reason = "Internal Server Error"
          )
        }
      }
    }

  }

  private def doRegistrationRequest(callback: (RosmRegistrationResponse) => Unit) = {
    val request = RosmRegistration(regime = "LISA", requiresNameMatch = false, isAnAgent = false)
    val response = Await.result(SUT.registerOnce("1234567890", request), Duration.Inf)

    callback(response)
  }

  val mockHttpPost = mock[HttpPost]
  implicit val hc = HeaderCarrier()

  object SUT extends RosmConnector {
    override val httpPost = mockHttpPost
  }

  val rosmIndividual = RosmIndividual(
    firstName = "Test",
    lastName = "User",
    dateOfBirth = Some(new DateTime("1980-01-01"))
  )

  val rosmIndividualNoDob = RosmIndividual(
    firstName = "Test",
    lastName = "User"
  )

  val rosmAddress = RosmAddress(
    addressLine1 = "Address Line 1",
    postalCode = Some("AB1 1AB"),
    countryCode = "GB"
  )

  val rosmContactDetails = RosmContactDetails(
    primaryPhoneNumber = Some("0123 456 7890"),
    emailAddress = Some("test@test.com")
  )

  val rosmSuccessResponse = RosmRegistrationSuccessResponse(
    safeId = "XE0001234567890",
    agentReferenceNumber = "AARN1234567",
    isEditable = true,
    isAnAgent = false,
    isAnASAgent = false,
    isAnIndividual = true,
    individual = Some(rosmIndividual),
    address = rosmAddress,
    contactDetails = rosmContactDetails
  )

  val rosmSuccessResponseNoDob = RosmRegistrationSuccessResponse(
    safeId = "XE0001234567890",
    agentReferenceNumber = "AARN1234567",
    isEditable = true,
    isAnAgent = false,
    isAnASAgent = false,
    isAnIndividual = true,
    individual = Some(rosmIndividualNoDob),
    address = rosmAddress,
    contactDetails = rosmContactDetails
  )

  val rosmFailureResponse = RosmRegistrationFailureResponse(
    code = "SERVICE_UNAVAILABLE",
    reason = "Dependent systems are currently not responding."
  )

}