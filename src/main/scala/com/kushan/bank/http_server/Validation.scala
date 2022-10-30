package com.kushan.bank.http_server

import cats.data.ValidatedNel
import cats.implicits._

/**
 * @author Ravindu
 *         10/30/2022
 */
object Validation {
  // based on cats.Validated
/*  In a ValidatedNel (Nel = non-empty list), we always have either
  a value of type A (the desired value)
  a non-empty list of ValidationFailures*/
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]

  // validation failures
  trait ValidationFailure {
    def errorMessage: String
  }
  // field must be present
  trait Required[A] extends (A => Boolean)
  // minimum value
  trait Minimum[A] extends ((A, Double) => Boolean) // for numerical fields
  trait MinimumAbs[A] extends ((A, Double) => Boolean) // for numerical fields
  // would be `given` instances in Scala 3
  //Let’s further assume that for certain types, e.g. Int or String, we already have some instances that make sense all (or almost all) the time:
  implicit val requiredString: Required[String] = _.nonEmpty
  implicit val minimumInt: Minimum[Int] = _ >= _
  implicit val minimumDouble: Minimum[Double] = _ >= _
  implicit val minimumIntAbs: MinimumAbs[Int] = Math.abs(_) >= _
  implicit val minimumDoubleAbs: MinimumAbs[Double] = Math.abs(_) >= _

  //An “internal” validation API that would use these instances would look something like this:
  case class EmptyField(fieldName: String) extends ValidationFailure {
    override def errorMessage = s"$fieldName is empty"
  }

  case class NegativeValue(fieldName: String) extends ValidationFailure {
    override def errorMessage = s"$fieldName is negative"
  }

  case class BelowMinimumValue(fieldName: String, min: Double) extends ValidationFailure {
    override def errorMessage = s"$fieldName is below the minimum threshold $min"
  }

  /*in terms of something that we would offer to the outside world in terms of data validation, we can expose general APIs for every type of
  validation we need, in our case “required field”, “above a minimum value”, “above a minimum value in absolute value”.*/

  // usage
  def required[A](value: A)(implicit req: Required[A]): Boolean = req(value)
  def minimum[A](value: A, threshold: Double)(implicit min: Minimum[A]): Boolean = min(value, threshold)
  def minimumAbs[A](value: A, threshold: Double)(implicit min: MinimumAbs[A]): Boolean = min(value, threshold)

  // "main" API
  def validateMinimum[A: Minimum](value: A, threshold: Double, fieldName: String): ValidationResult[A] = {
    if (minimum(value, threshold)) value.validNel
    else if (threshold == 0) NegativeValue(fieldName).invalidNel
    else BelowMinimumValue(fieldName, threshold).invalidNel
  }

  def validateMinimumAbs[A: MinimumAbs](value: A, threshold: Double, fieldName: String): ValidationResult[A] = {
    if (minimumAbs(value, threshold)) value.validNel
    else BelowMinimumValue(fieldName, threshold).invalidNel
  }

  def validateRequired[A: Required](value: A, fieldName: String): ValidationResult[A] =
    if (required(value)) value.validNel
    else EmptyField(fieldName).invalidNel

  trait Validator[A] {
    def validate(value: A): ValidationResult[A]
  }

  def validateEntity[A](value: A)(implicit validator: Validator[A]): ValidationResult[A] =
    validator.validate(value)
}
