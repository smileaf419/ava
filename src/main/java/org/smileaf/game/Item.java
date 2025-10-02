package org.smileaf.game;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Obtained Item information
 * @author smileaf (smileaf@me.com)
 */
public class Item extends Resource {
	private static final long serialVersionUID = 1L;
	/**
	 * Stats item yields
	 */
	protected Stats stats = new Stats();
	/**
	 * Ability list
	 */
	protected ArrayList<Ability> ability = new ArrayList<Ability>();
	
	/**
	 * Slot Values
	 */
	public static final int NONE = 0;
	/**
	 * Defensive Equipment
	 */
	public static final int HEAD = 1;
	/**
	 * Defensive Equipment
	 */
	public static final int CHEST = 2;
	/**
	 * Defensive Equipment
	 */
	public static final int LEGS = 3;
	/**
	 * Defensive Equipment
	 */
	public static final int BOOTS = 4;
	/**
	 * Primary Weapon
	 */
	public static final int MAINHAND = 5;
	/**
	 * Secondary Weapon, Stat stick or Defense
	 */
	public static final int OFFHAND = 6;
	/**
	 * Stat Booster
	 */
	public static final int NECK = 7;
	/**
	 * Special Effect: Booster or added effects.
	 */
	public static final int ACCESSORY = 8;
	/**
	 * Special Effect: Nullifier
	 */
	public static final int TRINKET = 9;
	/**
	 * Slot Item to be equipped in.
	 */
	protected int slot = 0;
	/**
	 * Effect values:
	 * Status Effects (Healing or Damaging)
	 * Key Items
	 */
	private int effects = 0;
	
	/**
	 * Equipment Type: Weapon
	 */
	public static final int WEAPON = 1;
	/**
	 * Equipment Type: Armor
	 */
	public static final int ARMOR = 2;
	/**
	 * Equipment Type: Ability Item
	 */
	public static final int ABILITY = 3;
	/**
	 * Equipment Type: Item
	 */
	public static final int ITEM = 4;
	/**
	 * Equipment Type
	 */
	private int itemType = 0;
	/**
	 * True if the Item has Abilities
	 */
	protected boolean hasAbility = false;
	
	public Item() {}
	
	/**
	 * Loads Item from XML
	 * @param node XML Node to load the Item from.
	 * @param store ResourceStore to load any abilities from.
	 */
	@Override
	public void load (Element node, ResourceStore store) throws ResourceLoadException {
		this.name = ResourceStore.getValueFrom(node, "name", this.name, false);
		switch (node.getAttribute("type").toLowerCase()) {
		case "item": this.itemType = Item.ITEM; break;
		case "armor": this.itemType = Item.ARMOR; break;
		case "weapon": this.itemType = Item.WEAPON; break;
		case "ability": this.itemType = Item.ABILITY; break;
		}
		this.slot = ResourceStore.getValueFrom(node, "slot", this.slot, false);
		Element statEffects = (Element)ResourceStore.getChildNodeByName(node, "StatEffects");
		if (statEffects != null && statEffects.hasChildNodes()) {
			this.stats.stamina = ResourceStore.getValueFrom(statEffects, "Stamina", 0, true);
			this.stats.attack = ResourceStore.getValueFrom(statEffects, "Attack", 0, true);
			this.stats.defense = ResourceStore.getValueFrom(statEffects, "Defense", 0, true);
			this.stats.speed = ResourceStore.getValueFrom(statEffects, "Speed", 0, true);
			this.stats.magic = ResourceStore.getValueFrom(statEffects, "Magic", 0, true);
			this.stats.magicDefense = ResourceStore.getValueFrom(statEffects, "MagicDefense", 0, true);
			this.stats.evasion = ResourceStore.getValueFrom(statEffects, "Evasion", 0, true);
		}

		Element effects = (Element)ResourceStore.getChildNodeByName(node, "Effects");
		if (effects != null && effects.hasChildNodes()) {
			//if (effects == null) throw new ResourceLoadException("Failed to load: Effects");
			this.effects  = ResourceStore.getValueFrom(effects, "Damage", true, true)?Ability.DAMAGE:0;
			this.effects += ResourceStore.getValueFrom(effects, "Healing", true, true)?Ability.HEAL:0;
		}
		
		// Add Abilities if any.
		Element abilities = (Element) ResourceStore.getChildNodeByName(node, "Abilities");
		if (abilities != null && abilities.hasChildNodes()) {
			ArrayList<Node> ability = ResourceStore.getChildNodesByName(abilities, "Ability");
			for (Node na : ability) {
				Ability a = new Ability();
				a.load((Element)na, store);
				this.ability.add(a);
			}
		}
	}

	@Override
	public String toString () {
		return this.name;
	}
	/**
	 * Item Type
	 * @return Item Type
	 */
	public int itemType() { return this.itemType; }
	/**
	 * Returns the Type of Item as a readable value.
	 * @return Type as a String
	 */
	public String typeToString() {
		switch (this.slot) {
		case Item.HEAD:      return "Helm";
		case Item.CHEST:     return "Chest";
		case Item.LEGS:      return "Legs";
		case Item.BOOTS:     return "Boots";
		case Item.MAINHAND:  return "Main hand";
		case Item.OFFHAND:   return "Off hand";
		case Item.NECK:      return "Neck"; 
		case Item.ACCESSORY: return "Accessory"; 
		case Item.TRINKET:   return "Trinket"; 
		case Item.NONE:      return "None"; 
		}
		return "";
	}

	public boolean hasEffect(int effect) {
		return (effect & this.effects) == effect;
	}
}
