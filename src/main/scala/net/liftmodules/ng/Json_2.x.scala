package net.liftmodules.ng

import net.liftweb.json.{JsonParser, JsonAST}

object Json {
  def slash(value:JsonAST.JValue, name:String) = value \\ name
}
