package org.smileaf.ava;

import java.io.Serializable;

import org.smileaf.game.Dialog;
import org.smileaf.game.Item;
import org.smileaf.game.Job;
import org.smileaf.game.Resource;
import org.smileaf.game.Player;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;

/**
 * @author smileaf (smileaf@me.com)
 */
public class Game implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * Player~
	 */
	protected Player player;
	/**
	 * Map Grid containing Monsters.
	 */
	protected CreatureMap map;

	// Initial Starting point.
	private static final int X = 10;
	private static final int Y = 20;
	private static final int Z = 1;
	
	private float populationPercent = 0.04f; // Initial Population density: 4%+1% per z level.

	/**
	 * Starts a new Game
	 */
	public void newGame() {
		// Get MC's name
		String name = App.dialog.getInputString("Hero's Name?: ");
		player = new Player(name, App.resource);
		App.dialog.setPlayer(player);
		String gender = App.dialog.getInput("Are you (M)ale or (F)emale?: ");
		Dialog.newLine();
		player.isMale = gender.toLowerCase().equals("m");
		
		// Set Default Job
		player.setJob((Job)App.resource.getResourcesByNameAndType("Freelancer", Resource.JOB).get(0));
		
		// Set default gear
		player.equip((Item)App.resource.getResourcesByNameAndType("Wood Sword", Resource.ITEM).get(0));
		player.heal();

		this.map = new CreatureMap(Game.X, Game.Y, Game.Z, (int)Math.round(Game.X*Game.Y*(this.populationPercent+0.01)));
		
		// New game dialog
		App.dialog.storyDialog(App.dialog.loadStory("newgame"));
		App.dialog.pause();
	}
	/**
	 * Generates a map for a given level
	 * @param level level to generate.
	 */
	public void genMap(int level) {
		// Generate Monsters
		this.map.regenMap(map.dx()+level-1, map.dy()+level-1, (int)Math.round(map.dx()*map.dy()*(this.populationPercent+(level*0.01))));
	}
	/**
	 * Player Sleeps and regenerates HP and MP
	 */
	public void sleep() {
		Dialog.pln("You setup camp and fall asleep recovering HP & MP.");
		int pop = this.map.population();
		// Check if we're at our cap, if not, add to the population and regenerate.
		App.dialog.println("Monster cap: " + (int)(this.map.dx() * this.map.dy() * this.map.maxCapacity));
		if (pop * 1.2f < this.map.dx() * this.map.dy() * this.map.maxCapacity) {
			this.map.setPopulation((int)(pop*1.2f));
			this.map.populate();
		}
		this.regenMap();
		this.player.heal();
		Dialog.pln("Upon waking up, you notice the dungeon seems to look different.");
	}
	/**
	 * Regenerate the Current level keeping all monsters where they are.
	 */
	public void regenMap() {
		// save our details so we can restore them after regenerating the map
		int x = this.map.x();
		int y = this.map.y();
		int pop = this.map.population();

		map.genMap(this.map.dx(), this.map.dy());
		
		// restore our details
		this.map.setPopulation(pop);
		map.setPos(x, y);
	}
	/**
	 * Saves the Game.
	 */
	public void save() {
		try {
			Dialog.p("Saving ... ");
			FileOutputStream fileOut = new FileOutputStream(App.gameSave);
			ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(this);
			objectOut.close();
			Dialog.pln("Done!");
		} catch (Exception ex) {
			Dialog.pln("Failed!");
			ex.printStackTrace();
		}
	}
	/**
	 * Loads a saved Game
	 * @return game Object
	 * @throws Exception on read error
	 */
	public static Object loadGame() throws Exception {
		FileInputStream fileIn = new FileInputStream(App.gameSave);
		ObjectInputStream objectIn = new ObjectInputStream(fileIn);

		Object obj = objectIn.readObject();
		objectIn.close();
		return obj;
	}
}