package me.krickl.memebotj;

class Cooldown(val cdLen: Integer) {
	private var cooldownLen: Integer = cdLen
	private var cooldownStart: Integer = 0;
	private var cooldownEnd: Integer = 0;

	def startCooldown() = {
		this.cooldownStart = (System.currentTimeMillis() / 1000).intValue()
		this.cooldownEnd = (System.currentTimeMillis() / 1000L).intValue() + this.cooldownLen;
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
		if (this.cooldownEnd > (System.currentTimeMillis() / 1000).intValue()) {
			return false;
		}

		return true;
	}
}
