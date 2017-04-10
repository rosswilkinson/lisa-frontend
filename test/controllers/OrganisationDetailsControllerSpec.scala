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

import java.io.File

import connectors.RosmConnector
import helpers.CSRFTest
import models.OrganisationDetails
import org.mockito.Matchers._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsJson, Headers}
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers._
import play.api.{Configuration, Environment, Mode}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.http.cache.client.ShortLivedCache
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.Future

class OrganisationDetailsControllerSpec extends PlaySpec
  with GuiceOneAppPerSuite
  with MockitoSugar
  with CSRFTest {

  "GET Organisation Details" must {

    "return a populated form" when {

      "the cache returns a value" in {
        val organisationForm = new OrganisationDetails("Test Company Name", "1234567890")

        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(Some(organisationForm)))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Organisation details</h1>")
        content must include ("Test Company Name")
      }

    }

    "return a blank form" when {

      "the cache does not return a value" in {
        when(mockCache.fetchAndGetEntry[OrganisationDetails](any(), any())(any(), any())).
          thenReturn(Future.successful(None))

        val result = SUT.get(fakeRequest)

        status(result) mustBe Status.OK

        val content = contentAsString(result)

        content must include ("<h1>Organisation details</h1>")
        content must include ("value=\"\"")
      }

    }

  }

  "POST Organisation Details" must {

    "return validation errors" when {
      "the submitted data is incomplete" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj()))
        val result = SUT.post()(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include ("<h1>Organisation details</h1>")
        content must include ("This field is required")
      }
      "the company tax reference number is invalid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "X")))
        val result = SUT.post(request)

        status(result) mustBe Status.BAD_REQUEST

        val content = contentAsString(result)

        content must include ("<h1>Organisation details</h1>")
        content must include ("Numeric 10 character value required")
      }
    }

    "redirect the user to trading details" when {
      "the submitted data is valid" in {
        val uri = controllers.routes.OrganisationDetailsController.post().url
        val request = createFakePostRequest[AnyContentAsJson](uri, AnyContentAsJson(json = Json.obj("companyName" -> "X", "ctrNumber" -> "1234567890")))
        val result = SUT.post(request)

        verify(mockCache, times(1)).cache[OrganisationDetails] _

        status(result) mustBe Status.SEE_OTHER

        redirectLocation(result) mustBe Some(controllers.routes.TradingDetailsController.get().url)
      }
    }

  }

  implicit val hc:HeaderCarrier = HeaderCarrier()

  val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = addToken(FakeRequest("GET", "/"))

  def createFakePostRequest[T](uri: String, body:T):FakeRequest[T] = {
    addToken(FakeRequest("POST", uri, FakeHeaders(), body))
  }

  val mockAuthConnector: PlayAuthConnector = mock[PlayAuthConnector]
  val mockRosmConnector: RosmConnector = mock[RosmConnector]
  val mockConfig: Configuration = mock[Configuration]
  val mockEnvironment: Environment = Environment(mock[File], mock[ClassLoader], Mode.Test)
  val mockCache: ShortLivedCache = mock[ShortLivedCache]

  object SUT extends OrganisationDetailsController {
    override val authConnector: PlayAuthConnector = mockAuthConnector
    override val rosmConnector: RosmConnector = mockRosmConnector
    override val config: Configuration = mockConfig
    override val env: Environment = mockEnvironment
    override val cache: ShortLivedCache = mockCache
  }

  when(mockAuthConnector.authorise[Option[String]](any(), any())(any())).
    thenReturn(Future.successful(Some("1234")))

  when(mockConfig.getString(matches("^appName$"), any())).
    thenReturn(Some("lisa-frontend"))

  when(mockConfig.getString(matches("^.*company-auth-frontend.host$"), any())).
    thenReturn(Some(""))

  when(mockConfig.getString(matches("^sosOrigin$"), any())).
    thenReturn(None)

}
