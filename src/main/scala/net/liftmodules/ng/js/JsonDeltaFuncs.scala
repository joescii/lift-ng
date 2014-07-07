package net.liftmodules.ng.js

import net.liftweb._
import http.js._
import json._
import JsCmds._
import JE._

/**
 * Contains utility for generating a JsCmd that will convert one JSON object into another.  This utility is meant
 * to be optimal for minor changes to a model object.  Large model changes will not benefit from this utility.
 * In typical cases, we can expect small incremental changes to a model on the server, which this utility will pluck
 * out that diff and send the minimal information to the client to update.
 */
object JsonDeltaFuncs {
  obj =>
  /** Calculate the delta function to convert the first argument into the second argument. */
  def dfn(val1: JValue, val2: JValue): JsVar => JsCmd = (val1, val2) match {
    case (x, y) if x == y => ref => JsCmds.Noop
    case (JObject(xs), JObject(ys)) => dfnFields(xs, ys)
    case (JArray(xs), JArray(ys)) => dfnArrays(xs, ys)
    case (x, y) => ref => SetExp(ref, y)
  }

  /**
   * Calculates the delta function between two arrays.  Currently best cases are appending values or changing
   * values 1-for-1.  Prepending and big changes result in basically the same payload as the entire array reassigned.
   */
  private def dfnArrays(xs: List[JValue], ys: List[JValue]): JsVar => JsCmd = {
    ref =>
      val len = Math.max(xs.length, ys.length)
      val xsp = xs.padTo(len, JNull)

      val deltas = for {
        ((x, y), i) <- xsp.zip(ys).zipWithIndex
      } yield {
        dfn(x, y)(JsVar(ref.varName + "[" + i + "]"))
      }

      val pops = if (xs.length > ys.length)
        Some(JsFor(JsRaw("i=0"), JsLt(JsVar("i"), JInt(xs.length - ys.length)), JsRaw("i++"), Call(ref.varName + ".pop")))
      else None

      (deltas ++ pops).reduceLeftOption(_ & _).getOrElse(JsCmds.Noop)
  }

  private def dfnFields(xs: List[JField], ys: List[JField]): JsVar => JsCmd = {
    ref =>
      def toMap(fs: List[JField]) = (fs map {
        case JField(name, value) => name -> value
      }).toMap
      def toVar(k: String) = JsVar(ref.varName + "[\"" + k + "\"]")

      val xm = toMap(xs)
      val ym = toMap(ys)

      val updates = for {
        (k, yv) <- ym
      } yield {
        val xv = xm.get(k).getOrElse(JNull)
        dfn(xv, yv)(toVar(k))
      }

      val voids = for {
        k <- xm.keySet if !ym.contains(k)
      } yield {
        SetExp(toVar(k), JsRaw("void 0"))
      }

      (updates ++ voids).reduceLeftOption(_ & _).getOrElse(JsCmds.Noop)
  }

  implicit def ToJsonDeltaFunc(j: JValue) = new JValueWithDfn(j)

  class JValueWithDfn(j:JValue) {
    /** Calculates the delta function to convert this JSON object into the argument */
    def dfn(other: JValue): JsVar => JsCmd = obj.dfn(j, other)
  }

}