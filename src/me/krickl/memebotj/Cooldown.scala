package me.krickl.memebotj

import scala.beans.BeanProperty
;

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
			return false
		}

    true
	}
}
