/*
 * Copyright (c) 2017 BalaBit
 * All rights reserved.
 */

package coed.client

case class KeyPressMessage(keyPress: KeyPress)

sealed trait KeyPress
case class Character(c: Char) extends KeyPress
case object Escape extends KeyPress
case class Unknown(code: Int) extends KeyPress
case object Enter extends KeyPress
