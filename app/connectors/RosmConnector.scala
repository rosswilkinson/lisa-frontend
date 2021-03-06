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

import config.WSHttp
import models._
import play.api.Logger
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http._

import scala.concurrent.Future

trait RosmConnector extends ServicesConfig with RosmJsonFormats {

  val httpPost:HttpPost = WSHttp
  lazy val rosmUrl = baseUrl("lisa")

  val httpReads:HttpReads[HttpResponse] = new HttpReads[HttpResponse] {
    override def read(method: String, url: String, response: HttpResponse) = response
  }

  def registerOnce(utr: String, request:RosmRegistration)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val uri = s"$rosmUrl/lisa/$utr/register"
    httpPost.POST[RosmRegistration, HttpResponse](uri, request)(implicitly, httpReads, implicitly)
  }

  def subscribe(lisaManagerRef: String, lisaSubscribe:LisaSubscription)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val uri = s"$rosmUrl/lisa/${lisaSubscribe.utr}/subscribe/$lisaManagerRef"
      httpPost.POST[LisaSubscription, HttpResponse](uri, lisaSubscribe)(implicitly, httpReads, implicitly)
  }

}

object RosmConnector extends RosmConnector {

}