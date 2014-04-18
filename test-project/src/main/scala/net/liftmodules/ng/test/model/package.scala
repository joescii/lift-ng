package net.liftmodules.ng
package test

import Angular.NgModel

package object model {
  case class Test2Obj(str1:String, str2:String) extends NgModel
  case class BroadcastObj(num:Int, char:String)
  case class StringInt(str:String, num:Int)
}
