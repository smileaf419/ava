package org.smileaf.game;

import java.util.ArrayList;

/**
 * Abstract Class to base Participants within a battle system off of.
 * @author smileaf (smileaf@me.com)
 */
public abstract class Participant extends Resource {
	private static final long serialVersionUID = 1L;
	/**
	 * Participant's Name: everyon'e got one :)
	 */
	protected String name = "Name";
	/**
	 * Base Stats to start all Participants off with.
	 */
	protected static float[] baseStat = {15f, 5f, 5f, 5f, 10f, 10f, 0f};

	/**
	 * Base Stats: Unbuffed Stats
	 */
	protected Stats stats = new Stats().set(baseStat);
	/**
	 * Base Stats: Buffed Stats
	 */
	private Stats bStats = new Stats();
	/**
	 * Battle Stat: Current HP
	 */
	protected float hp = 0;
	/**
	 * Battle Stat: Current MP
	 */
	protected float mp = 0;
	/**
	 * List of all Buffs and Debuffs
	 */
	protected ArrayList<Buff> buffs = new ArrayList<Buff>();
	/**
	 * List of all known Abilities
	 */
	protected ArrayList<Ability> ability = new ArrayList<Ability>();
	/**
	 * Participant's Level
	 */
	protected int level = 0;
	/**
	 * Experience accumulator
	 */
	protected int exp = 0;
	// 
	private boolean isPlayer = false;
	/**
	 * Set when the Participant is Charging an Ability
	 */
	public boolean isCharging = false;
	/**
	 * Set when the Participant enters Battle
	 */
	public boolean inBattle = false;

	public int level() { return this.level; }
	public float hp() { return this.hp; }
	public float stamina() { return (this.inBattle)?this.stats.tAdd(getbStats()).stamina:this.stats.stamina; }
	public float attack() { return (this.inBattle)?this.stats.tAdd(getbStats()).attack:this.stats.attack; }
	public float defense() { return (this.inBattle)?this.stats.tAdd(getbStats()).defense:this.stats.defense; }
	public float speed() { return (this.inBattle)?this.stats.tAdd(getbStats()).speed:this.stats.speed; }
	public float magic() { return (this.inBattle)?this.stats.tAdd(getbStats()).magic:this.stats.magic; }
	public float magicDefense() { return (this.inBattle)?this.stats.tAdd(getbStats()).magicDefense:this.stats.magicDefense; }
	public float mp() { return this.mp; }
	/**
	 * Returns if the Participant is a Player
	 * @return True if the Participant is a Player
	 */
	public boolean isPlayer() { return this.isPlayer; }
	/**
	 * Sets if the Participant is a Player
	 * @param player True if a Player.
	 */
	public void isPlayer(boolean player) { this.isPlayer = player; }
	/**
	 * Returns a list of known Abilities
	 * @return List of known Abilities
	 */
	public ArrayList<Ability> ability() { return this.ability; }
	/**
	 * List of all Buffs and Debuffs
	 * @return List of all Buffs and Debuffs
	 */
	public ArrayList<Buff> buffs() { return this.buffs; }
	/**
	 * Base Stats: Players this will be their total stats
	 * @return Participant's Stats
	 */
	public Stats stats() { return this.stats.add(getbStats()); }
	/**
	 * Sets the Participant's level.
	 * @param level Level of the Participant.
	 */
	public abstract void setLevel(int level);
	/**
	 * Clear buffs, removes Buff Stats and fixes HP and MP if above threshold due to a Buff.
	 */
	public void endBattle() {
		this.buffs.clear();
		//this.stats.sub(getbStats());
		if (this.hp > this.stats.stamina) this.hp = this.stats.stamina;
		if (this.mp > this.stats.magic) this.mp = this.stats.magic;
		this.inBattle = false;
	}
	
	/**
	 * Adds a Buff
	 * @param buff Buff to add.
	 */
	public void addBuff(Buff buff) {
		for (int i = 0; i < this.buffs.size(); i++) {
			if (this.buffs.get(i).name.equals(buff.name)) {
				// Resets the duration rather than adding another buff of the same.
				this.buffs.get(i).reset();
				return;
			}
		}
		
		this.buffs.add(buff);
		this.getbStats().add(buff.setStats(this.stats));
	}
	/**
	 * Removes a buff at a set Index
	 * @param index index of Buff to remove.
	 */
	public void removeBuff(int index) {
		this.removeBuff(this.buffs.get(index));
	}
	/**
	 * Removes a Buff
	 * @param buff Buff to remove.
	 */
	public void removeBuff (Buff buff) {
		this.getbStats().sub(buff.bStats);
		this.buffs.remove(buff);
	}
	/**
	 * Removes all Buffs
	 */
	public void clearBuffs() {
		if (this.buffs == null | this.buffs.size() ==0) return;
		for (int i = 0; i < this.buffs.size(); i++) {
			this.removeBuff(this.buffs.get(i));
		}
		this.buffs.clear();
	}
	/**
	 * Refresh's HP and MP Useful for cases where HP or MP levels are increased beyond unbuffed levels.
	 */
	public void refreshPoints() {
		setHP(hp());
		setMP(mp());
	}
	/**
	 * Sets HP and MP to full.
	 */
	public void heal() {
		setHP(stamina());
		setMP(magic());
	}
	/**
	 * Sets HP
	 * @param hp HP to set
	 */
	public void setHP(float hp) {
		this.hp = hp;
		if (this.hp > this.stamina()) this.hp = this.stamina();
		else this.hp = hp;
	}
	/**
	 * Sets MP
	 * @param mp MP to set
	 */
	public void setMP(float mp) {
		this.mp = mp;
		if (this.mp > this.magic()) this.mp = this.magic();
		else this.mp = mp;
	}
	/**
	 * Filters Abilities by Level
	 */
	public void filterAbilityByLevel() {
		this.ability = ResourceStore.filter(this.ability, c -> ((Ability) c).level <= this.level);
	}
	/**
	 * Returns a List of Abilities which are able to be used in the given Turn.
	 * @return List of usable Abilities
	 */
	public ArrayList<Ability> useableAbilities() {
		return ResourceStore.filter(this.ability, c -> (((Ability) c).playerTurn() && 
												  ((Ability) c).calcCost(this.level) <= this.mp()) && 
												 !((Ability) c).onCooldown()
												  );
	}
	/**
	 * Returns Buffed Stats
	 * @return Buffed Stats
	 */
	public Stats getbStats() {
		return bStats;
	}
	/**
	 * Sets the Buffed Stats
	 * @param bStats Buffed Stats
	 */
	public void setbStats(Stats bStats) {
		this.bStats = bStats;
	}
}
