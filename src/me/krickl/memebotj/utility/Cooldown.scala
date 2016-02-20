package me.krickl.memebotj.Utility

import scala.beans.BeanProperty

class Cooldown(val cdLen: Integer) {
  @BeanProperty
  var cooldownLen: Integer = cdLen
  @BeanProperty
  var cooldownStart = 0
  @BeanProperty
  var cooldownEnd = 0

  def startCooldown() = {
    this.cooldownStart = (System.currentTimeMillis() / 1000).toInt
    this.cooldownEnd = (System.currentTimeMillis() / 1000L).toInt + this.cooldownLen
  }

  def canContinue: Boolean = {
    if (this.cooldownEnd > (System.currentTimeMillis() / 1000).toInt) {
      //this.cooldownEnd = this.cooldownEnd + this.cooldownEnd / 100 * 5 // make cooldown 5% longer if required
      return false
    }

    true
  }
}
