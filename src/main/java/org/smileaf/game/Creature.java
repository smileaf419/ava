package org.smileaf.game;

import java.io.Serializable;
import java.util.ArrayList;

import org.smileaf.Utils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Creature class
 * @author smileaf (smileaf@me.com)
 */
public class Creature extends Participant implements Itemizer, Serializable  {
	private static final long serialVersionUID = 1L;
	/**
	 * Creature type
	 */
	public static final int NORMAL = 0;
	/**
	 * Creature type
	 */
	public static final int BOSS = 1;
	/**
	 * Creature type
	 */
	public static final int CHAMPION = 2;
	/**
	 * Type of Creature
	 */
	public int type = NORMAL;
	
	private ArrayList<LootItem> loot;
	private int[] gold = {0,0};
	private int exp = 0;
	private int[] levels = {0,0};
	private float bossMultiplier = 1.2f;
	/**
	 * Set when the levels the monster spawns on are &gt; Map.maxZ
	 */
	public boolean returnTrip = false;
	/**
	 * Level Stats, Unlike stats which are the base Stats
	 * and bStats which are the Buffed Stats.
	 * These are unique to the Creature and take the place of the Job.stats
	 */
	public Stats lStats = new Stats();
	
	/**
	 * Battle variable, Accumulator during battle.
	 * Creature gets their turn when this is higher than the Battle.maxTurn private variable.
	 */
	public float turnCount = 0;

	public Creature() {
		this.inBattle = true;
	}

	/**
	 * Loads a creature and its Ability's and Items from an XML Node.
	 */
	@Override
	public void load (Element node, ResourceStore store) throws ResourceLoadException {
		this.name = ResourceStore.getValueFrom(node, "name", this.name, false);
		this.exp = ResourceStore.getValueFrom(node, "exp", this.exp, false);
		this.type = (ResourceStore.getValueFrom(node, "isChampion", false, false)?CHAMPION:NORMAL);
		this.lore = ResourceStore.getValueFrom(node, "Lore", this.lore, true);
		
		Element stats = (Element)ResourceStore.getChildNodeByName(node, "Stats");
		if (stats == null) throw new ResourceLoadException("Error Loading stats of: " + this.name);
		this.lStats.stamina = ResourceStore.getValueFrom(stats, "Stamina", 0f, true);
		this.lStats.attack = ResourceStore.getValueFrom(stats, "Attack", 0f, true);
		this.lStats.defense = ResourceStore.getValueFrom(stats, "Defense", 0f, true);
		this.lStats.speed = ResourceStore.getValueFrom(stats, "Speed", 0f, true);
		this.lStats.magic = ResourceStore.getValueFrom(stats, "Magic", 0f, true);
		
		stats = (Element)ResourceStore.getChildNodeByName(node, "BaseStats");
		if (stats == null) throw new ResourceLoadException("Error Loading base stats of: " + this.name);
		this.stats.stamina = ResourceStore.getValueFrom(stats, "Stamina", 0f, true);
		this.stats.attack = ResourceStore.getValueFrom(stats, "Attack", 0f, true);
		this.stats.defense = ResourceStore.getValueFrom(stats, "Defense", 0f, true);
		this.stats.speed = ResourceStore.getValueFrom(stats, "Speed", 0f, true);
		this.stats.magic = ResourceStore.getValueFrom(stats, "Magic", 0f, true);
		
		Element floor = (Element)ResourceStore.getChildNodeByName(node, "Floors");
		if (floor == null) throw new ResourceLoadException("Error Loading floors of: " + this.name);
		this.levels[0] = ResourceStore.getValueFrom(floor, "low", 0, false);
		this.levels[1] = ResourceStore.getValueFrom(floor, "high", 0, false);
		this.returnTrip = (this.levels[0] > Map.maxZ || this.levels[1] > Map.maxZ);
		if (this.levels[0] > Map.maxZ) 
			this.levels[0] -= Map.maxZ;
		if (this.levels[1] > Map.maxZ) 
			this.levels[1] -= Map.maxZ;
		
		Node loot = ResourceStore.getChildNodeByName(node, "Loot");
		this.loot = new ArrayList<LootItem>();
		
		NodeList item = loot.getChildNodes();
		for (int x = 0; x < item.getLength(); x++) {
			if (item.item(x).getNodeName().equals("Item")) {
				String name = ResourceStore.getValueFrom((Element)item.item(x), "name", "", false);
				if ((name.toLowerCase().equals("gold"))) {
					Element gold = (Element)ResourceStore.getChildNodeByName((Element)item.item(x), "Drop");
					if (gold != null) {
						this.gold[0] = ResourceStore.getValueFrom(gold, "low", 0, false);
						this.gold[1] = ResourceStore.getValueFrom(gold, "high", 0, false);
					}
				} else {
					Element drop = (Element)ResourceStore.getChildNodeByName((Element)item.item(x), "Drop");
					if (drop != null) {
						int min = ResourceStore.getValueFrom(drop, "low", 0, false);
						int max = ResourceStore.getValueFrom(drop, "high", 0, false);
						int chance = ResourceStore.getValueFrom(drop, "chance", 0, false);
						Item lItem = (Item) store.getResourcesByNameAndType(name, Resource.ITEM).get(0);
						this.loot.add(new LootItem(lItem, min, max, chance));
					}
				}
			}
		}
		
		// Add in Monster Abilities
		NodeList abilities = ResourceStore.getChildNodeByName(node, "Abilities").getChildNodes();
		for (int i = 0; i < abilities.getLength(); i++) {
			if (abilities.item(i).getNodeName().equals("Ability")) {
				String name = ResourceStore.getValueFrom((Element)abilities.item(i), "name", "", false);
				Ability ability = (Ability) store.getResourcesByNameAndType(name, Resource.ABILITY).get(0);
				this.ability.add(ability);
			}
		}
	}
	/**
	 * Sets the level and filters Abilities
	 * @param floor The current floor which the creature spawned on.
	 */
	public void startBattle(int floor) {
		int level = Utils.random(floor * 2 - 1, floor * 2 + 1);
		if (level <= 0) level = 1;
		this.setLevel(level);
		this.filterAbilityByLevel();
	}

	/**
	 * Clears all buffs
	 */
	@Override
	public void endBattle() {
		this.buffs.clear();
	}
	/**
	 * Makes a creature a Boss
	 * @param isBoss Is a Boss?
	 */
	public void setBoss(boolean isBoss) {
		if (isBoss)
			this.type = Creature.BOSS;
		else
			this.type = Creature.NORMAL;
	}
	/**
	 * Multiply stats, gold and experience by an amount
	 * @param level Level of the boss.
	 */
	public void makeBoss(int level) {
		if (this.type == Creature.BOSS) level++;
		this.level = level;
		this.getbStats().add(lStats.multiply(this.bossMultiplier));
		heal();
		this.exp = Math.round(this.exp * this.bossMultiplier);
		this.setBoss(true);
	}
	/**
	 * Experience yield.
	 * @return experience the monster gives upon defeat.
	 */
	public int exp() { return this.exp; }
	/**
	 * Gold yield
	 * @return gold reward upon defeat
	 */
	public int gold() {
		int gold = Utils.random(this.gold[0], this.gold[1]);
		if (this.type == Creature.BOSS) {
			return (int)(gold * this.bossMultiplier);
		}
		return gold; 
	}
	/**
	 * Loot dropped
	 * @return a list of loot to be gained by the player.
	 */
	public ArrayList<LootItem> loot() {
		ArrayList<LootItem> li = new ArrayList<LootItem>();
		for (int i = 0; i < this.loot.size(); i++) {
			int chance = Utils.random(0, 100);
			if (this.loot.get(i).chance > chance)
				try {
					li.add((LootItem)this.loot.get(i).clone());
				} catch (CloneNotSupportedException e) {
					e.printStackTrace();
				}
		}
		return li;
	}
	/**
	 * Floor levels the creature spawns on.
	 * @return Low, High
	 */
	public int[] levels() { return this.levels; }
	
	@Override
	public Object clone() {
		// Reset the level so when we startBattle it'll set the stats correctly.
		Creature c = (Creature) super.clone();
		c.level = 0;
		return c;
	}
	
	@Override
	public String toString() {
		return this.name;
	}
	/**
	 * Sets the Creature's level.
	 */
	@Override
	public void setLevel(int level) {
		this.level = level;
		this.setbStats(new Stats());
		this.getbStats().add(lStats.multiply(level));
		heal();
	}
}
