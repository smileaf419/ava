package org.smileaf.game;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Character and Monster Abilities and Spells
 * @author smileaf (smileaf@me.com)
 *
 */
public class Ability extends Resource {
	private static final long serialVersionUID = 1L;
	/**
	 * Type of attack: stat used Attack
	 */
	public static final boolean PHYSICAL = true;
	/**
	 * Type of attack: stat used Magic
	 */
	public static final boolean MAGIC = false;
	/**
	 * Distance of attack
	 */
	public static final int MELEE = 0;
	/**
	 * Distance of attack
	 */
	public static final int RANGED = 1;
	
	/**
	 * Effect values
	 */
	public static final int NORMAL   		= 0; // Normal damage or apply status effect
	/**
	 * Effect values: Contains a buff
	 */
	public static final int BUFF 			= 1;
	/**
	 * Effect values: Contains a debuff
	 */
	public static final int DEBUFF 			= 2;
	/**
	 * Effect values: Ability causes a loss of turn
	 */
	public static final int LOSETURN        = (int) Math.pow(2, 2);
	/**
	 * Effect values: To be calculated at end of turn rather than at the beginning.
	 */
	public static final int ONTURNEND 	    = (int) Math.pow(2, 3);
	/**
	 * Effect values: Heals the user
	 */
	public static final int HEAL	  		= (int) Math.pow(2, 4);
	/**
	 * Effect values: Damages the opponent.
	 */
	public static final int DAMAGE          = (int) Math.pow(2, 5);
	/**
	 * Effect values: Has a chance to cause an effect
	 */
	public static final int CHANCETO        = (int) Math.pow(2, 6);
	/**
	 * Effect values: Uses the user's Magic stat instead of Attack.
	 */
	public static final int MAGICAL         = (int) Math.pow(2, 7); 
	/**
	 * Effect values: Grants a protection from an effect
	 */
	public static final int PROTECTION      = (int) Math.pow(2, 8); 
	/**
	 * Effect values: Debuff type: disallows the use of abilities
	 */
	public static final int SILENCE			= (int) Math.pow(2, 9);
	/**
	 * Effect values
	 */
	public static final int NOATTACK        = (int) Math.pow(2, 10);
	/**
	 * Effect values: Debuff Type: Causes damage if the ability misses
	 */
	public static final int DMGONFAIL       = (int) Math.pow(2, 11);

	/**
	 * Effect values: Helper Type: DAMAGE + HEAL
	 */
	public static final int ABSORB			= Ability.DAMAGE + Ability.HEAL;
	/**
	 * Effect values: Helper Type: LOSETURN + DEBUFF
	 */
	public static final int INCAPACITATE	= Ability.LOSETURN + Ability.DEBUFF;
	/**
	 * Effect values: Helper Type: BUFF + HEAL
	 */
	public static final int HOT 			= Ability.BUFF + Ability.HEAL;
	/**
	 * Effect values: Helper Type: DEBUFF + DAMAGE
	 */
	public static final int DOT 			= Ability.DEBUFF + Ability.DAMAGE;
	/**
	 * Effect values: Helper Type: DOT + CHANCETO
	 */
	public static final int CONFUSE			= Ability.DOT+ Ability.CHANCETO;
	/**
	 * Effect values: Helper Type: DEBUFF + CHANCETO + LOSETURN
	 */
	public static final int SLEEP			= Ability.DEBUFF + Ability.CHANCETO + Ability.LOSETURN;
	/**
	 * Ability Effect Value are used in battle calculations
	 */
	protected int effect = 0;
	/**
	 * Accuracy of Abilities or apply a status effect
	 * Maybe used with Effect Types ex: Ability.BUFF
	 *  0: Ability
	 *  1: Buff
	 *  2: Debuff
	 */
	protected int[] accuracy = { 100, 100, 100 };
	/**
	 * Level the ability is learned at.
	 */
	protected int level=0;
	/**
	 * Power of the ability
	 * (Attack | Magic) * scaling + power
	 */
	private int power=0;
	/**
	 * Scaling of the ability
	 * (Attack | Magic) * scaling + power
	 */
	private float scaling = 1.f;
	/**
	 * Type of attack: Sets which stat to use (Attack or Magic)
	 */
	private boolean physical = Ability.PHYSICAL;
	
	/** 
	 * Used in abilities that cause an effect on physical contact.
	 */
	protected int distance = 0;
	
	/**
	 *  Math.round(level / costScaling) * cost + costBase;
	 */
	protected int cost = 0;
	/**
	 *  Math.round(level / costScaling) * cost + costBase;
	 */
	protected int costBase = 0;
	/**
	 *  Math.round(level / costScaling) * cost + costBase;
	 * If set to 0 will be reset to 1.
	 */
	protected int costScaling = 1;
	/**
	 * Number of turns Ability is to be on cooldown
	 */
	protected int cooldown = 0;
	/**
	 * Number of turns Ability has been on cooldown.
	 */
	protected int oncooldown = 0;
	/**
	 * Number of turns Ability is to be charging up.
	 */
	protected int chargeup = 0;
	/**
	 * Number of turns Ability has been charging.
	 */
	protected int charging = 0;
	/**
	 * Each Slot should offer a unique ability which either scales with the user, 
	 *    or allows for an upgrade later within the sword or job class.
	 *	 0. Defending (Defense booster) (Earth Elemental)
	 *	 1. Melee: MP:0 Basic Attack
	 *	 2. Healing ability (Water Elemental)
	 *	 3. Sword Ability: Quick attack or 1st turn boosted attack?
	 *	 4. Sword Ability: Attack Booster
	 *	 5. Job Ability
	 *	 6. Job Ability
	 *	 7. Job Ability
	 *	 8. Earth Offensive Magic
	 *	 9. Fire Offensive Magic
	 *	10. Wind Offensive Magic
	 *	11. Water Offensive Magic
	 */
	protected int slot = 0;
	/**
	 * Dialog for the ability
	 */
	protected Ability.AbilityDialog dialog = new Ability.AbilityDialog();
	/**
	 * Abilities may have both a buff and a debuff
	 */
	protected Buff debuff;
	/**
	 * Abilities may have both a buff and a debuff
	 */
	protected Buff buff;
	/**
	 * Ability has a buff: Default false
	 */
	protected boolean hasBuff = false;
	/**
	 * Ability has a debuff: Default false
	 */
	protected boolean hasDebuff = false;
	
	public Ability() {}
	/**
	 * Loads the XML Ability node
	 * 
	 * @param node node to load
	 */
	@Override
	public void load(Element node, ResourceStore store) throws ResourceLoadException {
		this.name = ResourceStore.getValueFrom(node, "name", this.name, false);
		this.slot = ResourceStore.getValueFrom(node, "slot", this.slot, false);
		this.level = ResourceStore.getValueFrom(node, "level", this.level, false);
		this.setPhysical(ResourceStore.getValueFrom(node, "Physical", this.isPhysical(), true));
		switch (ResourceStore.getValueFrom(node, "Distance", "Melee", true)) {
		case "Melee": this.distance = Ability.MELEE; break;
		case "Ranged": this.distance = Ability.RANGED; break;
		}
		
		// Add Effects
		Element effects = (Element)ResourceStore.getChildNodeByName(node, "Effects");
		this.effect  = ResourceStore.getValueFrom(effects, "Damage", false, true)?Ability.DAMAGE:0;
		this.effect += ResourceStore.getValueFrom(effects, "Healing", false, true)?Ability.HEAL:0;
		int ch = ResourceStore.getValueFrom(effects, "ChanceToApply", 0, true);
		if (ch > 0) this.effect += Ability.CHANCETO;
		switch (ResourceStore.getValueFrom(effects, "StatusEffect", "", true).toLowerCase()) {
		case "buff":
			this.effect += Ability.BUFF;
			this.accuracy[Ability.BUFF] = ch;
			break;
		case "debuff":
			this.effect += Ability.DEBUFF;
			this.accuracy[Ability.DEBUFF] = ch;
			break;
		}
		switch (ResourceStore.getValueFrom(effects, "OnMiss", "", true).toLowerCase()) {
		case "takedamage":
			this.effect += Ability.DMGONFAIL;
			break;
		}
		this.effect += ResourceStore.getValueFrom(effects, "LoseTurn", false, true)?Ability.LOSETURN:0;
		
		Element battle = (Element)ResourceStore.getChildNodeByName(node, "Battle");
		this.accuracy[Ability.NORMAL] = ResourceStore.getValueFrom(battle, "accuracy", this.accuracy[Ability.NORMAL], false);
		this.setPower(ResourceStore.getValueFrom(battle, "power", this.getPower(), false));
		this.setScaling(ResourceStore.getValueFrom(battle, "scaling", this.getScaling(), false));
		
		Element limits = (Element)ResourceStore.getChildNodeByName(node, "Limits");
		this.chargeup = ResourceStore.getValueFrom(limits, "chargeup", this.chargeup, false);
		this.cooldown = ResourceStore.getValueFrom(limits, "cooldown", this.cooldown, false);
		
		Element cost = (Element)ResourceStore.getChildNodeByName(node, "Cost");
		this.cost = ResourceStore.getValueFrom(cost, "cost", this.cost, false);
		this.costScaling = ResourceStore.getValueFrom(cost, "scaling", this.costScaling, false);
		this.costBase = ResourceStore.getValueFrom(cost, "base", this.costBase, false);

		// Load Dialogs
		Element dialog = (Element)ResourceStore.getChildNodeByName(node, "Dialog");
		if (dialog != null && !dialog.hasChildNodes())
			throw new ResourceLoadException("Error loading ability dialogs for " + this.name + ": Missing Dialog!");
		this.dialog.attack = ResourceStore.getValueFrom(dialog, "Attack", this.name+":"+this.dialog.attack, true);
		this.dialog.failed = ResourceStore.getValueFrom(dialog, "Failed", this.name+":"+this.dialog.failed, true);
		this.dialog.missed = ResourceStore.getValueFrom(dialog, "Missed", this.name+":"+this.dialog.missed, true);
		this.dialog.buffMissed = ResourceStore.getValueFrom(dialog, "BuffMissed", this.name+":"+this.dialog.buffMissed, true);
		this.dialog.charging = ResourceStore.getValueFrom(dialog, "Charging", this.name+":"+this.dialog.charging, true);
		
		// Add Buffs
		this.buff = new Buff();
		Node nbuff = ResourceStore.getChildNodeByName(node, "Buff");
		if (nbuff != null && nbuff.hasChildNodes()) {
			this.hasBuff = true;
			this.buff.isBuff = true;
			this.buff.load((Element)nbuff, store);
			Dialog.p("B");
		}
		
		// Add Debuffs
		this.debuff = new Buff();
		nbuff = ResourceStore.getChildNodeByName(node, "Debuff");
		if (nbuff != null && nbuff.hasChildNodes()) {
			this.hasDebuff = true;
			this.debuff.isDebuff = true;
			this.debuff.load((Element)nbuff, store);
			Dialog.p("D");
		}
	}
	/**
	 * Sets the scaling of the ability
	 * (Att | Magic) * scaling + power
	 * @param scaling Scaling of the ability.
	 */
	public void setScaling(float scaling) { this.scaling = scaling; }
	/**
	 * Sets the power of the ability
	 * (Att | Magic) * scaling + power
	 * @param power Power of the ability
	 */
	public void setPower (int power) { this.power = power; }
	/**
	 * Sets the Level at which the ability is learned.
	 * @param level Level ability is learned
	 */
	public void setLevel(int level) { this.level = level; }
	/**
	 * Sets the slot the ability is loaded into.
	 * @param slot Slot to load ability to.
	 */
	public void setSlot(int slot) { this.slot = slot; }
	/**
	 * Calculates the cost of the ability based on its level.
	 *  (level / scaling) * cost + base
	 * @param level level to base the MP cost on
	 * @return Amount of MP based on the level
	 */
	public int calcCost(int level) {
		return Math.round(level / this.costScaling) * this.cost + this.costBase;
	}
	/**
	 * Method to be called every turn to adjust the cooldown and chargeup values
	 * @return always returns true to be easily added into an if statement.
	 */
	public boolean playerTurn() {
		if (this.cooldown > 0) {
			this.oncooldown--;
			if (this.oncooldown < 0) this.oncooldown = 0;
		}
		if (this.chargeup > 0) {
			this.charging++;
			if (this.charging >= this.chargeup) this.charging = this.chargeup;
		}
		return true;
	}
	/**
	 * Method to be called when an ability is used to set the initial values of a cooldown or chargeup ability.
	 */
	public void used() {
		this.oncooldown = this.cooldown;
		this.charging = 0;
	}
	/**
	 * 
	 * @return true if the ability is charged up, false if its not.
	 */
	public boolean charged() {
		if (this.chargeup == 0) return false;
		return (this.charging >= this.chargeup);
	}
	/**
	 * 
	 * @return true if the ability is still charging, false if its not
	 */
	public boolean charging() {
		if (this.chargeup == 0) return false;
		return (this.charging < this.chargeup);
	}
	/**
	 * 
	 * @return true if the ability is on cooldown.
	 */
	public boolean onCooldown() {
		return (this.oncooldown > 0);
	}
	
	/**
	 * Checks for the presence of effect within inEffect
	 * @param effect Effect to check
	 * @param inEffect the set to check if the effect is within.
	 * @return true if the bit is on.
	 */
	public static boolean hasEffect(int effect, int inEffect) {
		return ((effect & inEffect) == effect);
	}
	/**
	 * Checks for the presence of an effect bit within the Abilities Effects
	 * @param effect Effect to check
	 * @return true if the bit is set.
	 */
	public boolean hasEffect(int effect) {
		return ((effect & this.effect) == effect);
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	public boolean isPhysical() {
		return physical;
	}
	public void setPhysical(boolean physical) {
		this.physical = physical;
	}
	public float getScaling() {
		return scaling;
	}
	public int getPower() {
		return power;
	}
	public Buff buff() { return this.buff; }
	public Buff debuff() { return this.debuff; }
	public boolean hasBuff() { return this.hasBuff; }
	public boolean hasDebuff() { return this.hasDebuff; }
	public int effect() { return this.effect; }
	public int cooldown() { return this.cooldown; }
	public int chargeUp() { return this.chargeup; }
	public AbilityDialog dialog() { return this.dialog; }
	public float getAccuracy(int type) {
		return accuracy[type];
	}
	
	/**
	 * Internal class to easily store dialog of the ability
	 * @author smileaf
	 *
	 */
	public class AbilityDialog implements Serializable {
		private static final long serialVersionUID = 1L;
		/**
		 * A successful attack
		 */
		public String attack = "Attack: please replace";
		/**
		 * A failed attack
		 */
		public String failed = "Failed: please replace";
		/**
		 * A missed attack
		 */
		public String missed = "Missed: please replace";
		/**
		 * 1st turn of a charged attack
		 */
		public String charging = "Charging: please replace";
		/**
		 * if the buff fails to apply
		 */
		public String buffMissed = "buffMissed: please replace";
		/**
		 * When the buff is applied
		 */
		public String buff = "Buff: please replace";
		/**
		 * Every turn the buff is active
		 */
		public String buffPeriod = "BuffPeriod: please replace";
	 	/**
	 	 * if the buff has a chance, and the chance fails
	 	 */
		public String buffFailed = "BuffFailed: please replace";
	}
}
