package org.smileaf.game;

import java.io.Serializable;

import org.smileaf.Utils;

/**
 * Object obtained after Battle
 * Contains an Item a set amount and the chance for the player to obtain it.
 * @author smileaf
 *
 */
public class LootItem implements Serializable,Cloneable {
	private static final long serialVersionUID = 1L;
	/**
	 * Item to be Obtained
	 */
	protected Item item;
	/**
	 * Amount of Items to obtain
	 */
	protected int amount = 0;
	/**
	 * Chance to obtain the Item.
	 */
	protected int chance = 0;
	
	public LootItem() {}
	public LootItem(Item item, int min, int max, int chance) {
		this.item = item;
		if (min > max) {
			// Well this is awkward....
			System.err.println("Error Min > Max .. Correcting, please fix! " + item.filename);
			int a = min;
			min = max;
			max = a;
		}
		if (min != max) {
			this.amount = Utils.random(min,  max);
		} else {
			this.amount = max;
		}
		this.chance = chance;
	}
	
	/**
	 * Returns the Item of the Loot
	 * @return item
	 */
	public Item item() { return this.item; }
	public int amount() { return this.amount; }
	public int chance() { return this.chance; }
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		LootItem li = (LootItem) super.clone();
		li.item = (Item) this.item.clone();
    	return li;
	}
	@Override
	public String toString() {
		return item.name + ": " + amount;
	}
	/**
	 * Adds to the amount of items.
	 * @param amount number of items to add
	 * @return Total number of Items.
	 */
	public int addAmount(int amount) {
		this.amount += amount;
		return this.amount;
	}
	/**
	 * Subtracts from the amount of Items.
	 * @param amount number of items to remove
	 * @return Total number of Items.
	 */
	public int removeAmount(int amount) {
		this.amount -= amount;
		return this.amount;
	}
}
