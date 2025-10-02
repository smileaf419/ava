package org.smileaf.game;

import java.io.Serializable;
import java.util.ArrayList;

import org.smileaf.Utils;
import org.smileaf.Color;

import org.w3c.dom.Element;
/**
 * Player Character class.
 * @author smileaf (smileaf@me.com)
 */
public class Player extends Participant implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Controls the level of output.
	 */
	public boolean silent = false;
	/**
	 * Amount of Experience needed to reach next level
	 */
	private int expToLevel = 10;
	/**
	 * Upon level up increase the expToLevel
	 */
	private float expModifier = 1.2f;
	private Job job;
	// Helm, Chest, Pants, Boots
	// Main Hand, Off-hand, Neck, Accessory 1 & 2
	private Item equipment[] = new Item[8];	
	private ArrayList<LootItem> loot;
	
	/**
	 * True: Male; False: Female
	 */
	public boolean isMale = true;
	/**
	 * Gold amount.
	 */
	protected int gold = 0;
	
	private ResourceStore store;
	/**
	 * Player Character
	 * @param name Our Name
	 * @param store ResourceStore to load abilities from.
	 */
	public Player(String name, ResourceStore store) {
		this.name = name;
		this.store = store;
		this.loot = new ArrayList<LootItem>();
		this.isPlayer(true);
		this.level = 1;
	}
	/**
	 * Sets the level of our player.
	 * @param level Level to set.
	 */
	public void setLevel (int level) {
		this.level = level;
		// Reset the Stats
		this.stats.set(Participant.baseStat);
		// Reset the Job
		this.setJob(this.job);
		// reload all Equipment
		Item e[] = this.equipment.clone();
		this.equipment = new Item[8];
		for (Item equip : e) {
			this.equip(equip);
		}
		for (int i=1; i<level; i++) {
			this.levelUp();
		}
	}
	/**
	 * Adds gold to our wallet.
	 * @param gold Gold to add.
	 */
	public void gainGold(int gold) {
		if (gold > 0) {
			Dialog.pln("Gained " + gold + " Gold!");
			this.gold += gold;
		}
	}
	/**
	 * Subtracts gold from user's account, and returns the remaining amount.
	 * @param gold Gold amount to payout.
	 * @return int gold remaining
	 */
	public int pay (int gold) {
		this.gold -= gold;
		// Can't go negative
		if (this.gold < 0) this.gold = 0;
		return this.gold;
	}
	/**
	 * Adds Experience, if enough has been earned a level up is done.
	 * @param exp Experience to add.
	 */
	public void gainExp(int exp) {
		Dialog.pln("Gained " + exp + " Experience!");
		this.exp += exp;
		while (this.exp >= this.expToLevel) {
			// Reset the Exp accumulator.
			this.exp -= this.expToLevel;
			this.levelUp();
		}
		Dialog.pln((this.expToLevel - this.exp) + " experience to the next level.");
	}
	/**
	 * Levels up our player and restores both HP and MP.
	 */
	public void levelUp() {
		this.expToLevel = this.expToLevel + (int)Math.round(this.expToLevel * this.expModifier);
		// TODO Randomize a modifier to add to each stat based on the Job.
		//      Add a bonus modifier for items?
		this.stats.add(this.job.stats);
		this.level++;
		heal();
		if (!silent) {
			Dialog.pln("Congradulations! You've reached level " + this.level() + "!");
			Dialog.pln(this);
			this.checkAbilities();
		}
	}
	/**
	 * Gains loot Items.
	 * @param loot Loot to be added.
	 */
	public void gainLoot(ArrayList<LootItem> loot) {
		for (int i = 0; i < loot.size(); i++) {
			Item item;
			item = (Item)loot.get(i).item.clone();
			switch (item.itemType()) {
			case Item.WEAPON:
				if (this.getSlot(item.slot) == null || item.stats.attack > this.getSlot(item.slot).stats.attack) {
					this.equip(item);
				}
				break;
			case Item.ARMOR:
				if (this.getSlot(item.slot) == null || item.stats.defense > this.getSlot(item.slot).stats.defense) {
					this.equip(item);
				}
				break;
			case Item.ITEM:
				boolean found = false;
				for (int t = 0; t < this.loot.size(); t++) {
					if (this.loot.get(t).item.name.equals(loot.get(i).item.name)) {
						this.loot.get(t).addAmount(loot.get(i).amount);
						found = true; continue;
					}
				}
				if (!found) this.loot.add(loot.get(i));
				break;
			}
		}
	}
	/**
	 * Get the Equipment Item at the provided slot.
	 * @param slot Slot to be returned.
	 * @return Equipment Item at the given slot.
	 */
	public Item getSlot (int slot) {
		return this.equipment[slot];
	}
	/**
	 * Checks if a slot has an Item set.
	 * @param slot Slot to check
	 * @return True if the slot has a set Item.
	 */
	public boolean isEquipped(int slot) {
		return (this.equipment[slot] != null);
	}
	/**
	 * Unequips an Item
	 * @param equip Item to be unequipped.
	 */
	public void unEquip(Item equip) {
		if (equip == null) return;
		if (equip.slot <= 0) {
			Dialog.important(equip.name + " is not equipable!");
			return;
		}
		// Remove the stat bonus' before equipping the new gear
		if (this.equipment[equip.slot] != null) {
			this.stats.sub(this.equipment[equip.slot].stats);
			// Refresh container stats
			refreshPoints();
		}
		this.equipment[equip.slot] = null;
	}
	/**
	 * Equips an Item
	 * @param equip Item to be equipped.
	 */
	public void equip(Item equip) {
		if (equip == null) return;
		if (equip.slot <= 0) {
			Dialog.important(equip.name + " is not equipable!");
			return;
		}
		this.unEquip(equip);
		// Add the stat bonus' of the new equipment.
		this.equipment[equip.slot] = equip;
		this.stats.add(this.equipment[equip.slot].stats);
		refreshPoints();
		
		if (!silent)
			Dialog.pln("Equipped: " + this.equipment[equip.slot].name + "\n  "+
							this.equipment[equip.slot].stats.toString() );
		if (this.equipment[equip.slot].hasAbility) {
			for (Ability ab : this.equipment[equip.slot].ability) {
				this.addAbility(ab);
			}
		}
	}
	
	@Override
	public String toString() {
		String a = "";
		// Level, HP, Attack, Defense, Speed, Magic
		if (this.inBattle) {
			a = this.name +
			": "+(int)this.hp()+"/"+(int)this.stamina() + 
			" MP:"+(int)this.mp()+"/"+(int)this.magic() + 
			" D:" + this.defense();
		} else {
		a =      "Character:  " + this.name +
			   "\nLevel:      " + this.level() +
			   "\nHP:         " + Color.colorizeValues((int)this.hp(), (int)this.stamina()) +
			   "\nAttack:     " + (int)this.attack() +
			   "\nDefense:    " + (int)this.defense() +
			   "\nSpeed:      " + (int)this.speed() +
			   "\nMagic:      " + Color.colorizeValues((int)this.mp(), (int)this.magic()) +
			   "\nGold:       " + this.gold +
				   "\nExperience: " + Color.colorizeValues(this.exp, this.expToLevel) + 
				   "\nEquipment:";
			for (int i = 0; i < this.equipment.length; i++) {
				if (this.equipment[i] != null)
				a += "\n" + this.equipment[i].typeToString() + ": " + Utils.getSpaces(9-this.equipment[i].typeToString().length()) + this.equipment[i]; 
			}
		}
		return a;
	}
	/**
	 * Sets a job.
	 * @param job Job to be acquired.
	 */
	public void setJob(Job job) {
		if (!silent) 
			Dialog.pln("Aquired job: " + job.name);
		this.job = job;
		this.stats.add(this.job.stats);
		this.reloadAbilties();
	}
	/**
	 * Adds an Ability
	 * @param ability Ability to add.
	 */
	public void addAbility(Ability ability) {
		if (ability == null) return;
		if (!this.ability.get(ability.slot).name.equals(ability.name)) {
			if (!silent) Dialog.pln(" Gained skill: " + ability.name + " (" + ability.slot + ")");
			this.ability.set(ability.slot, ability);
		}
	}
	/**
	 * Reloads all Abilities
	 */
	public void reloadAbilties() {
		ArrayList<Ability> ab = this.ability;
		// Clear out old data.
		this.ability.clear();
		// Refresh our array
		for (int i = 0; i< 12; i++) { this.ability.add(new Ability()); }
		for (Ability ability : ab) {
			ArrayList<Resource> r = store.getResourcesByNameAndType(ability.name, Resource.ABILITY);
			if (r.size() > 0)
				this.addAbility((Ability)r.get(0));
		}
		this.checkAbilities();
	}
	/**
	 * Checks if any new Abilities are available.
	 */
	public void checkAbilities() {
		for (int i = 0; i < this.job.ability.size(); i++) {
			if (this.job.ability.get(i).level <= this.level) {
				this.addAbility(this.job.ability.get(i));
			}
		}
		// Add in Equipment abilities
		for (int i = 0; i < this.equipment.length; i++) {
			if (this.equipment[i] != null && this.equipment[i].ability.size() > 0) {
				for (int a = 0; a < this.equipment[i].ability.size(); a++) {
					if (this.equipment[i].ability.get(a).level <= this.level) {
						this.addAbility(this.equipment[i].ability.get(a));
					}
				}
			}
		}
	}
	/**
	 * Current Job
	 * @return Current Job
	 */
	public Job job() { return this.job; }
	/**
	 * Current Inventory list.
	 * @return Inventory List.
	 */
	public ArrayList<LootItem> inventory() { return this.loot; }
	@Override
	public void load(Element node, ResourceStore store) throws ResourceLoadException {}
}
