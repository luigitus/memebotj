package me.krickl.memebotj;

public class Cooldown {
	private int cooldownLen = 0;
	private int cooldownStart = 0;
	private int cooldownEnd = 0;

	public Cooldown(int cooldownLen) {
		this.cooldownLen = cooldownLen;
	}

	public void startCooldown() {
		this.cooldownStart = (int) (System.currentTimeMillis() / 1000L);
		this.cooldownEnd = (int) (System.currentTimeMillis() / 1000L) + this.cooldownLen;
	}

	public int getCooldownLen() {
		return cooldownLen;
	}

	public void setCooldownLen(int cooldownLen) {
		this.cooldownLen = cooldownLen;
	}

	public void setCooldownEnd(int cooldownEnd) {
		this.cooldownEnd = cooldownEnd;
	}

	public boolean canContinue() {
		if (this.cooldownEnd > (int) (System.currentTimeMillis() / 1000L)) {
			return false;
		}

		return true;
	}
}
