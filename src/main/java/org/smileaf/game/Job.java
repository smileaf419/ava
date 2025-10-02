package org.smileaf.game;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * The Job Class: Describes a player's Job. Abilities and Stat changes.
 * @author smileaf (smileaf@me.com)
 */
public class Job extends Resource {
	private static final long serialVersionUID = 1L;
	/**
	 * List of Abilities learned within the Job.
	 */
	protected ArrayList<Ability> ability = new ArrayList<Ability>();

	/**
	 * Job Stats: gained on level up.
	 */
	protected Stats stats = new Stats();
	
	public Job() {
	}
	@Override
	public void load(Element node, ResourceStore store) throws ResourceLoadException {
		this.name = ResourceStore.getValueFrom(node, "name", this.name, false);
		this.lore = ResourceStore.getValueFrom(node, "Lore", this.lore, true);
		
		Element stats = (Element)ResourceStore.getChildNodeByName(node, "Stats");
		if (stats == null) throw new ResourceLoadException("Error Loading stats of: " + this.name);
		this.stats.stamina = ResourceStore.getValueFrom(stats, "Stamina", 0, true);
		this.stats.attack = ResourceStore.getValueFrom(stats, "Attack", 0, true);
		this.stats.defense = ResourceStore.getValueFrom(stats, "Defense", 0, true);
		this.stats.speed = ResourceStore.getValueFrom(stats, "Speed", 0, true);
		this.stats.magic = ResourceStore.getValueFrom(stats, "Magic", 0, true);
		this.stats.magicDefense = ResourceStore.getValueFrom(stats, "MagicDefense", 0, true);
		
		NodeList abilities = ResourceStore.getChildNodeByName(node, "Abilities").getChildNodes();
		for (int i = 0; i < abilities.getLength(); i++) {
			if (abilities.item(i).getNodeName().equals("Ability")) {
				String name = ResourceStore.getValueFrom((Element)abilities.item(i), "name", "", false);
				Ability ability = (Ability) store.getResourcesByNameAndType(name, Resource.ABILITY).get(0);
				this.ability.add(ability);
			}
		}
	}
	
	@Override
	public String toString() {
		return this.name;
	}
}
