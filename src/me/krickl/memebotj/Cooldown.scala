package me.krickl.memebotj;

class Cooldown(val cdLen: Integer) {
	private var cooldownLen: Integer = cdLen
	private var cooldownStart = 0;
	private var cooldownEnd = 0;

	def startCooldown() = {
		this.cooldownStart = (System.currentTimeMillis() / 1000).toInt
		this.cooldownEnd = (System.currentTimeMillis() / 1000L).toInt + this.cooldownLen;
	}

	def getCooldownLen(): Integer = {
		return cooldownLen;
	}

	def setCooldownLen(cdLen: Integer) = {
		this.cooldownLen = cdLen;
	}

	def setCooldownEnd(cooldownEnd: Integer) = {
		this.cooldownEnd = cooldownEnd;
	}

	def canContinue(): Boolean = {
		if (this.cooldownEnd > (System.currentTimeMillis() / 1000).toInt) {
			return false;
		}

		return true;
	}
}
