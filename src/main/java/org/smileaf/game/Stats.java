package org.smileaf.game;

import java.io.Serializable;

/**
 * Container class to store stats for various resources
 * 
 * Initial Stat Values
 *  Job Added Values on a Per Level Basis
 *  Weapon/Armor Added Values
 *  Buffs / Debuffs (Temp Battle Only)
 */
public class Stats implements Cloneable, Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Basic Stat
	 */
	public static final int STAMINA			= 0;
	/**
	 * Basic Stat
	 */
	public static final int ATTACK			= 1;
	/**
	 * Basic Stat
	 */
	public static final int DEFENSE			= 2;
	/**
	 * Basic Stat
	 */
	public static final int SPEED			= 3;
	/**
	 * Basic Stat
	 */
	public static final int MAGIC			= 4;
	/**
	 * Battle Stat
	 */
	public static final int MAGICDEFENSE	= 5;
	/**
	 * Battle Stat
	 */
	public static final int EVASION			= 6;
	/**
	 * Battle Stat
	 */
	public static final int HP      		= 7;
	/**
	 * Battle Stat
	 */
	public static final int MP      		= 8;
	
	/**
	 * Basic Stat
	 */
	public float stamina = 0;
	/**
	 * Basic Stat
	 */
	public float attack = 0;
	/**
	 * Basic Stat
	 */
	public float defense = 0;
	/**
	 * Basic Stat
	 */
	public float speed = 0;
	/**
	 * Basic Stat
	 */
	public float magic = 0;

	/**
	 * Battle Stat
	 */
	public float magicDefense = 0;
	/**
	 * Battle Stat
	 */
	public float evasion = 0;
	
	public Stats() {
		
	}

	/**
	 * Accepts a Variable length array.
	 * However it must be at least 5 long.
	 * Always returns itself.
	 * @param s Stats to be set.
	 * @return Stats after being adjusted.
	 */
	public Stats set(float[] s) {
		if (s.length <= 4) return this;
		this.stamina = s[STAMINA];
		this.attack = s[ATTACK];
		this.defense = s[DEFENSE];
		this.speed = s[SPEED];
		this.magic = s[MAGIC];
		
		if (s.length <= 5) return this;
		this.magicDefense = s[MAGICDEFENSE];
		if (s.length < 6) return this;
		this.evasion = s[EVASION];
		return this;
	}
	/**
	 * Sets Stats
	 * @param s Stats to be set
	 * @return Stats after being adjusted.
	 */
	public Stats set(Stats s) {
		this.stamina = s.stamina;
		this.attack = s.attack;
		this.defense = s.defense;
		this.speed = s.speed;
		this.magic = s.magic;
		this.magicDefense = s.magicDefense;
		this.evasion = s.evasion;
		return this;
	}
	/**
	 * Adds Stats to the current Stats
	 * @param s Stats to be added
	 * @return Stats after being added together.
	 */
	public Stats add(Stats s) {
		this.stamina += s.stamina;
		this.attack += s.attack;
		this.defense += s.defense;
		this.speed += s.speed;
		this.magic += s.magic;
		this.magicDefense += s.magicDefense;
		this.evasion += s.evasion;
		return this;
	}
	/**
	 * Subtracts Stats from the current Stats
	 * @param s Stats to be subtracted
	 * @return Stats after being subtracted from the current stats.
	 */
	public Stats sub(Stats s) {
		this.stamina -= s.stamina;
		this.attack -= s.attack;
		this.defense -= s.defense;
		this.speed -= s.speed;
		this.magic -= s.magic;
		this.magicDefense -= s.magicDefense;
		this.evasion -= s.evasion;
		return this;
	}
	/**
	 * Adds together Stats, however does not modify the current stats
	 * @param s Stats to be Added
	 * @return new Stats of current stats + given stats.
	 */
	public Stats tAdd(Stats s) {
		Stats t = new Stats();
		t.stamina = this.stamina + s.stamina;
		t.attack = this.attack + s.attack;
		t.defense = this.defense + s.defense;
		t.speed = this.speed + s.speed;
		t.magic = this.magic + s.magic;
		t.magicDefense = this.magicDefense + s.magicDefense;
		t.evasion = this.evasion + s.evasion;
		return t;
	}

	/**
	 * Calculates the Stats to be added(or subtracted if -) via a Buff
	 * @param s Buff to be calculated.
	 * @return Stats to be added.
	 */
	public Stats buff(Stats s) {
		Stats t = new Stats();
		t.stamina = this.stamina * (s.stamina - 1);
		t.attack = this.attack * (s.attack - 1);
		t.defense = this.defense * (s.defense - 1);
		t.speed = this.speed * (s.speed - 1);
		t.magic = this.magic * (s.magic - 1);
		t.magicDefense = this.magicDefense * (s.magicDefense - 1);
		t.evasion += s.evasion;
		return t;
	}
	/**
	 * Multiplies all Stats by a given value.
	 * @param i given value to multiply all stats by.
	 * @return new Stats after being Multiplied.
	 */
	public Stats multiply(int i) {
		Stats t = new Stats();
		t.stamina = this.stamina * i;
		t.attack = this.attack * i;
		t.defense = this.defense * i;
		t.speed = this.speed * i;
		t.magic = this.magic * i;
		t.magicDefense = this.magicDefense * i;
		t.evasion = this.evasion * i;
		return t;
	}
	/**
	 * Multiplies all Stats by a given value.
	 * @param i given value to multiply all stats by.
	 * @return new Stats after being Multiplied.
	 */
	public Stats multiply(float i) {
		Stats t = new Stats();
		t.stamina = this.stamina * i;
		t.attack = this.attack * i;
		t.defense = this.defense * i;
		t.speed = this.speed * i;
		t.magic = this.magic * i;
		t.magicDefense = this.magicDefense * i;
		t.evasion = this.evasion * i;
		return t;
	}
	
	@Override
	public String toString() {
		return  "HP: "+this.stamina+
				" A: "+this.attack+
				" D: "+this.defense+
				" S: "+this.speed+
				" M: "+this.magic;
	}
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
