package org.smileaf.game;

import java.io.Serializable;

import org.w3c.dom.Element;

/**
 * Ability Buff: 
 * Contains information describing both Buffs and Debuffs
 * @author smileaf (smileaf@me.com)
 */
public class Buff extends Ability implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Stat Modifications
	 */
	protected Stats stats = new Stats();
	/**
	 * Buff Stats: Contains the Stats added to the Participants
	 */
	protected Stats bStats = new Stats();
	/**
	 * Chance the Buff is activated
	 */
	protected int chance = 100;
	/**
	 * Buff is removed should the chance fail
	 */
	protected boolean removeOnFail = false;
	/**
	 * Buff is removed should the chance be successful
	 */
	protected boolean removeOnSuccess = false;
	/**
	 * Counter variable, Counts down.
	 */
	protected int duration = 0;
	/**
	 * Number of Turns the Buff is active.
	 */
	protected int maxDuration = 0;
	/**
	 * Is a Buff
	 */
	protected boolean isBuff = false;
	/**
	 * Is a Debuff
	 */
	protected boolean isDebuff = false;
	
	public Buff () {}

	/**
	 * Loads a Buff from a XML Node
	 * <br>
	 * {@code
	 * 	<Debuff name="Wrap">
	 *		<Duration>1</Duration>
	 *		<Battle power="0" scaling="1" />
	 *		<Physical>false</Physical>
	 *		<Effects>
	 *			<Damage>false</Damage>
	 *			<Healing>false</Healing>
	 *			<Chance>100</Chance>
	 *			<OnTurnEnd>false</OnTurnEnd>
	 *			<LoseTurn>true</LoseTurn>
	 *			<Protection></Protection>
	 *		</Effects>
	 *		<StatEffects modifyby="percent">
	 *			<Stamina>0</Stamina>
	 *			<Attack>0</Attack>
	 *			<Defense>0</Defense>
	 *			<Speed>0</Speed>
	 *			<Magic>0</Magic>
	 *		</StatEffects>
	 *		<Dialog>
	 *			<BuffPeriod>%M's wrap prevented you from moving!</BuffPeriod>
	 *		</Dialog>
	 *	</Debuff>
	 *	}
	 */
	@Override
	public void load(Element node, ResourceStore store) throws ResourceLoadException {
		this.name = ResourceStore.getValueFrom(node, "name", this.name, false);
		
		Element effects = (Element)ResourceStore.getChildNodeByName(node, "Effects");
		this.effect  = (ResourceStore.getValueFrom(effects, "Damage", true, true))?Ability.DAMAGE:0;
		this.effect += (ResourceStore.getValueFrom(effects, "Healing", true, true))?Ability.HEAL:0;
		this.effect += (ResourceStore.getValueFrom(effects, "LoseTurn", true, true))?Ability.LOSETURN:0;
		this.effect += (ResourceStore.getValueFrom(effects, "OnTurnEnd", false, true))?Ability.ONTURNEND:0;

		this.chance = ResourceStore.getValueFrom(effects, "Chance", this.chance, true);
		if (this.chance < 0) this.chance = 0;
		if (this.chance > 0) this.effect += Ability.CHANCETO;

		this.removeOnFail = ResourceStore.getValueFrom(effects, "RemoveOnFail", this.removeOnFail, true);
		this.removeOnSuccess = ResourceStore.getValueFrom(effects, "RemoveOnSuccess", this.removeOnSuccess, true);
		
		// Stat Effects
		Element statEffects = (Element)ResourceStore.getChildNodeByName(node, "StatEffects");
		if (statEffects != null && statEffects.hasChildNodes()) {
			this.stats.stamina = ResourceStore.getValueFrom(statEffects, "Stamina", this.stats.stamina, true);
			if (this.stats.stamina == 0) this.stats.stamina = 1;
			this.stats.attack = ResourceStore.getValueFrom(statEffects, "Attack", this.stats.attack, true);
			if (this.stats.attack == 0) this.stats.attack = 1;
			this.stats.defense = ResourceStore.getValueFrom(statEffects, "Defense", this.stats.defense, true);
			if (this.stats.defense == 0) this.stats.defense = 1;
			this.stats.speed = ResourceStore.getValueFrom(statEffects, "Speed", this.stats.speed, true);
			if (this.stats.speed == 0) this.stats.speed = 1;
			this.stats.magic = ResourceStore.getValueFrom(statEffects, "Magic", this.stats.magic, true);
			if (this.stats.magic == 0) this.stats.magic = 1;
			this.stats.magicDefense = ResourceStore.getValueFrom(statEffects, "MagicDefense", this.stats.magicDefense, true);
			if (this.stats.magicDefense == 0) this.stats.magicDefense = 1;
			this.stats.evasion = ResourceStore.getValueFrom(statEffects, "Evasion", this.stats.evasion, true);
		}

		this.duration = ResourceStore.getValueFrom(node, "Duration", this.duration, true);
		if (this.duration < -1) this.duration = -1;
		this.maxDuration = this.duration;
		
		Element battle = (Element)ResourceStore.getChildNodeByName(node, "Battle");
		if (battle != null && battle.hasAttributes()) {
			this.setPower(ResourceStore.getValueFrom(battle, "power", this.getPower(), false));
			this.setScaling(ResourceStore.getValueFrom(battle, "scaling", this.getScaling(), false));
			if (this.getScaling() < 0) this.setScaling(0);
		}
		this.setPhysical(ResourceStore.getValueFrom(node, "Physical", this.isPhysical(), true));

		Element dialog = (Element)ResourceStore.getChildNodeByName(node, "Dialog");
		if (dialog == null || !dialog.hasChildNodes())
			throw new ResourceLoadException("Error loading ability buff dialogs for " + this.name + ": Missing Dialog!");
		this.dialog.buff = ResourceStore.getValueFrom(dialog, "Buff", this.name+":"+this.dialog.buff, true);
		this.dialog.buffPeriod = ResourceStore.getValueFrom(dialog, "BuffPeriod", this.name+":"+this.dialog.buffPeriod, true);
		this.dialog.buffFailed = ResourceStore.getValueFrom(dialog, "BuffFailed", this.name+":"+this.dialog.buffFailed, true);
	}
	/**
	 * Sets the Stats of the Buff's Stats. 
	 * @param s Players current Stats
	 * @return Stats the buff offers.
	 */
	public Stats setStats(Stats s) {
		bStats.set(bStats.set(s).buff(this.stats));
		return bStats;
	}
	/**
	 * Resets the duration of the Buff
	 */
	public void reset() {
		this.setDuration(this.maxDuration);
	}
	/**
	 * Sets the duration of the Buff
	 * @param duration number of turns the buff should last.
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/**
	 * Expires the Buff 1 turn at a time.
	 * @return True if the buff expired.
	 */
	public boolean expire () {
		if (this.duration == -1) return false;
		this.duration--;
		if (this.duration == 0)
			return true;
		return false;
	}
	/**
	 * Checks if the Buff has expired.
	 * @return True if the Buff has Expired.
	 */
	public boolean expired() {
		if (this.duration == 0) {
			this.reset();
			return true;
		}
		return false;
	}
	/**
	 * Duration of the Buff
	 * @return duration of the buff
	 */
	public int duration() { return this.duration; }
	public boolean isBuff() { return this.isBuff; }
	public boolean isDebuff() { return this.isDebuff; }
	public int chance() { return this.chance; }
	public boolean removeOnFail() { return this.removeOnFail; }
	public boolean removeOnSuccess() { return this.removeOnSuccess; }
	
	@Override
	public String toString() {
		return this.name + "(T:" + this.duration + ")";
	}
}
