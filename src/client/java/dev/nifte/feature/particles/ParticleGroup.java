package dev.nifte.feature.particles;

public enum ParticleGroup {
	COMBAT("nifte.config.particles.group.combat"),
	MAGIC("nifte.config.particles.group.magic"),
	FIRE("nifte.config.particles.group.fire"),
	WATER("nifte.config.particles.group.water"),
	DRIPS("nifte.config.particles.group.drips"),
	BLOCKS("nifte.config.particles.group.blocks"),
	NATURE("nifte.config.particles.group.nature"),
	MOBS("nifte.config.particles.group.mobs"),
	SCULK("nifte.config.particles.group.sculk"),
	PORTALS("nifte.config.particles.group.portals"),
	TRIAL("nifte.config.particles.group.trial"),
	OTHER("nifte.config.particles.group.other");

	private final String langKey;

	ParticleGroup(String langKey) {
		this.langKey = langKey;
	}

	public String langKey() {
		return this.langKey;
	}
}
