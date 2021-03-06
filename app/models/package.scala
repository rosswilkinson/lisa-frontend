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

import play.api.data.validation.Constraints._
import play.api.data.validation.{Valid, ValidationError, Invalid, Constraint}


package object models extends Constants{

  val companyPattern: Constraint[String] = pattern("""^[a-zA-Z0-9 '&\\/]{0,105}$""".r, error="Invalid company name")

  def nonEmptyTextLisa[T](messageKey:String): Constraint[String] = Constraint[String](required) { text =>
    if (text == null) Invalid(messageKey) else if (text.trim.isEmpty) Invalid(ValidationError(messageKey)) else Valid
  }
}

trait Constants {

  val company_error_key:String = "org.compName.mandatory"
  val ctutr_error_key:String =  "org.ctUtr.mandatory"
  val compLabel:String = "companyName"
  val utrLabel:String = "ctrNumber"
  val required :String= "constraint.required"
}
