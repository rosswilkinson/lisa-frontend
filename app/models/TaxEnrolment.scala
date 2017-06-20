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

package models

import org.joda.time.DateTime

case class TaxEnrolmentAddSubscriberRequest(serviceName: String, callback: String, etmpId: String)

trait TaxEnrolmentAddSubscriberResponse
case object TaxEnrolmentAddSubscriberSucceeded extends TaxEnrolmentAddSubscriberResponse
case object TaxEnrolmentAddSubscriberFailed extends TaxEnrolmentAddSubscriberResponse

case class TaxEnrolmentSubscription(
                                     created: DateTime,
                                     lastModified: DateTime,
                                     credId: String,
                                     serviceName: String,
                                     identifiers: List[TaxEnrolmentIdentifier],
                                     callback: String,
                                     state: TaxEnrolmentState,
                                     etmpId: String,
                                     groupIdentifier: String)

trait TaxEnrolmentState
case object TaxEnrolmentPending extends TaxEnrolmentState
case object TaxEnrolmentError extends TaxEnrolmentState
case object TaxEnrolmentSuccess extends TaxEnrolmentState

case class TaxEnrolmentIdentifier(key: String, value: String)