package org.smileaf.ava;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.io.IOException;

import org.smileaf.Utils;
import org.smileaf.Color;
import org.smileaf.game.Creature;
import org.smileaf.game.Dialog;
import org.smileaf.game.Map;
import org.smileaf.game.MonsterLoadException;
import org.smileaf.game.NoMonstersException;
import org.smileaf.game.Participant;
import org.smileaf.game.Resource;

//import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.TextColor.Indexed;
import com.googlecode.lanterna.TextCharacter;

/**
 * @author smileaf (smileaf@me.com)
 */
public class CreatureMap extends Map implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 
	 */
	private ArrayList<Resource> storage = new ArrayList<Resource>();
	
	/**
	 * Population of the given level
	 */
	protected int population = 1;
	/**
	 * Max Capacity %
	 */
	protected float maxCapacity = 0.35f;
	/**
	 * Indicates we don't have a Monster at the location.
	 */
	public static final Creature NOMOB = null;
	/**
	 * Creature Map
	 */
	private Creature[][] map;
	/**
	 * Filtered list of Monsters for the level we're on.
	 */
	private ArrayList<Creature> filteredMonsterList = new ArrayList<Creature>();
	
	private boolean returnTrip = false;
	
	protected Color mapColor;
	public static int[] MAPCOLORS = {Color.GREEN, Color.WHITE, Color.GRAY, 93, Color.RED, 94 };
	public static String[] LAYERNAMES = {"Top Dwellers", "Undead Layer", "Giants Layer", "Cursed Layer", "Cursed Caverns", "Nature's Army Waves"};
	
	public CreatureMap(int x, int y, int z, int population) {
		super(x, y, z); // Gen our map before we add in monsters.
		
		// Demo our Floor Colors
		Dialog.pln("Floor Layer Colors: ");
		for (int i=0;i<MAPCOLORS.length;i++) {
			Dialog.p(new Color(LAYERNAMES[i]).bg(MAPCOLORS[i]));
			if (i == 2) Dialog.newLine();
		}
		Dialog.newLine();
		
		try {
			this.filterList();
		} catch (NoMonstersException e) {
			e.printStackTrace();
		}
		this.map = new Creature[this.dx][this.dy];
		
		this.populate(this.population);
		this.addBoss();
		ArrayList<Resource> champions = this.filter(storage, c -> ((Creature) c).type == Creature.CHAMPION);
		if (champions.size() > 0) {
			for (Resource champion : champions) {
				this.addChampion((Creature) champion);
			}
		}
	}
	public void populate(int pop) {
		// if we end up with a population over the capacity, set it to the maxCapacity.
		if (pop > this.dx * this.dy * this.maxCapacity)
			pop = (int) (this.dx * this.dy * this.maxCapacity);
		else if (pop < 0) {
			this.population = 0;
			return;
		}

		int mx = 0;
		int my = 0;
		int fails = 0;
		System.out.println("Populating a " + this.dx + "/" + this.dy + " grid with " + pop + " Monsters.");
		Utils.progressPercentage("Populating", 0, pop);
		
		// Add in # of Monsters
		int popAdded = 0;
		for (int m = 0; m < pop - this.population; m++) {
			mx = (byte)Utils.random(0, this.dx);
			my = (byte)Utils.random(0, this.dy);
			if (mx == this.x && my == this.y ||
					map[mx][my] != CreatureMap.NOMOB) {
				// Found Duplicate or found in Starting zone.
				m--;
				fails++;
			} else {
				popAdded++;
				fails = 0;
				try {
					map[mx][my] = this.randomCreature();
				} catch (MonsterLoadException e) {
					e.printStackTrace();
				}
				Utils.progressPercentage("Populating", m, pop);
			}
			if (fails > 10) {
				m++;
				fails=0;
				this.population = this.population + popAdded;
			}
		}
		Utils.progressPercentage("Populating", this.population, this.population);
	}
	public void addChampion (Creature champion) {
		int mx = Utils.random(0, dx);
		int my = Utils.random(0, dy);
		map[mx][my] = champion;
	}
	public void addBoss() {
		// Add a boss for the level
		int mx = Utils.random(0, dx);
		int my = Utils.random(0, dy);
		if (map[mx][my] == CreatureMap.NOMOB) {
			try {
				map[mx][my] = this.randomCreature();
			} catch (MonsterLoadException e) {
				e.printStackTrace();
			}
			this.population++; // Add the Boss to the population.
		}
		map[mx][my].setBoss(true);
		//System.out.println("Setting Monster (boss): "+map[mx][my].name+" at: " + mx + "x" + my);
	}
	public void regenMap(int x, int y, int pop) {
		this.map = new Creature[x][y];	// New Monster Array
		this.setPopulation(pop);		// Set a new Population
		int[] up, down;
		up = findUp();
		down = findDown();
		this.genMap(x, y);				// Generate a new set of directions
		if (up != null) set(up[0],up[1],Map.UP);
		if (down != null) set(down[0],down[1],Map.DOWN);
		try {
			this.filterList();			// filter them to the level
		} catch (NoMonstersException e) {
			System.out.println(e.getMessage());
			System.exit(1);	// Exit
		}
		this.populate(pop);				// Add Monsters to our array
		this.addBoss();					// Add a Boss monster.
	}
	
	public void setPopulation (int pop) {
		this.population = pop;
	}
	
	public void monsterDies(int x, int y) {
		this.population--;
		if (this.population < 0) this.population = 0;
		this.map[x][y] = CreatureMap.NOMOB;
	}
	
	public boolean hasMonster(int x, int y) {
		return (this.map[x][y] != CreatureMap.NOMOB);
	}
	
	public Creature getMonster(int x, int y) {
		return this.map[x][y];
	}
	
	public Creature randomCreature() throws MonsterLoadException {
		return (Creature)(this.getFiltered(Utils.random(0, this.filteredMonsterList.size())).clone());

	}
	
	public void filterList() throws NoMonstersException {
		System.out.print("Filtering Monsters to level: " + this.z + " ");
		ArrayList<Resource>mFList = filter(App.resource.getStore(), c -> c.resourceType() == Resource.CREATURE &&
		 												 ((Creature) c).levels()[0] <= this.z && 
														 ((Creature) c).levels()[1] >= this.z &&
														 ((Creature) c).returnTrip == this.isReturnTrip());
		this.set(mFList);
		if (mFList.size() == 0) throw new NoMonstersException("No Monsters Found while Filtering to level: "+this.z);
		System.out.println(this.filteredMonsterList);
	}

	public ArrayList<Resource> filter(List<Resource> list, Predicate<Resource> predicate) {
		ArrayList<Resource> accumulator = new ArrayList<>();
	     for (Resource item : list) 
	          if(predicate.test(item))
	              accumulator.add(item);
	     return accumulator;
	}
	
	public void set (ArrayList<Resource> list) {
		this.filteredMonsterList.clear();
		for (Resource item : list) {
			this.filteredMonsterList.add((Creature) item);
		}
	}

	public Creature getFiltered(String name) {
		for (Participant monster : this.filteredMonsterList) {
			if (monster.name() == name) {
				return (Creature) monster;
			}
		}
		return CreatureMap.NOMOB;
	}
	public Creature getFiltered(int pos) {
		return this.filteredMonsterList.get(pos);
	}

	public void add (Participant creature) {
		storage.add(creature);
	}
	
	public void add (ArrayList<Participant> creatures) {
		storage.addAll(creatures);
	}
	
	public Creature get(String name) {
		for (Resource monster : this.storage) {
			if (monster.name() == name) {
				return (Creature) monster;
			}
		}
		return CreatureMap.NOMOB;
	}

	public Creature get(int pos) {
		return (Creature) storage.get(pos);
	}
	
	/*screen.setCharacter(column, row, new TextCharacter(
						' ',
						TextColor.ANSI.DEFAULT,
						// This will pick a random background color
						TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)]));
	 */
						
	public void dspLMap() {
		int c = MAPCOLORS[Math.round(this.dz / 15)];
		Color wall = new Color("0").bg(c).fg(c);
		String[] d = new String[3];
		Color tile = new Color();
		d[0] = new Color(Utils.getChar((this.viewportY*2+1)*2+1, "X")).bg(c).fg(c)+"\n";
		d[1] = wall.toString();
		d[2] = wall.toString();
		for (int x=this.x+viewportX+1;x>=this.x-viewportX;x--) {
			for (int y=this.y-viewportY;y<this.y+viewportY+1;y++) {
				if (x>=this.dx || x<0 || y>=this.dy || y<0) {
					d[1] += wall+""+wall;
					d[2] += wall+""+wall;
				} else {
					if (this.x==x && this.y == y) {
						tile.text("■").fg(Color.CYAN).bg(Color.BLACK);
					}
					if (canUp(x,y) || canDown(x,y)) {
						tile.text("S").fg(Color.WHITE).bg(Color.BLACK);
						if (this.x==x && this.y==y)
							tile.bg(Color.CYAN);
					} else if (this.hasMonster(x,y)) {
						if (this.map[x][y].type == Creature.BOSS)
							tile.text("B").fg(12).bg(Color.BLACK);
						else if (this.map[x][y].type == Creature.CHAMPION)
							tile.text("C").fg(13).bg(Color.BLACK);
						else
							tile.text("M").fg(9).bg(Color.BLACK);
						if (this.x==x && this.y==y)
							tile.bg(Color.CYAN);
					} else if (!(this.x == x && this.y == y)) {
						if (!canNorth(x,y) && !canSouth(x,y) && !canWest(x,y) && !canEast(x,y))
							tile.text(" ").bg(c).fg(c);
						else
							tile.text(" ").bg(Color.BLACK);
							
					}
					d[1] += tile;
					if (canEast(x,y)) d[1]+=" "; else d[1] += wall;
					if (canSouth(x,y)) d[2]+=" " + wall; else d[2] += wall + "" + wall;
				}
			}
			d[0] += d[1]+"\n"+d[2]+"\n";
			d[1] = wall.toString(); d[2] = wall.toString();
		}
		System.out.println(d[0]);
	}
	
	public void dspMap(TextGraphics textGraphics) {
		int oX=0, oY=2;	// Offset Coords
		//dspLMap();
		int x=0, y=0;
//		System.out.println(MAPCOLORS[Math.round(this.dz / 15)]);
		TextColor.Indexed c = new TextColor.Indexed(MAPCOLORS[Math.round(this.dz / 15)]);
		TextCharacter wall = new TextCharacter('0', c, c);
		TextCharacter blank = new TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
		TextColor.Indexed tileColor = new TextColor.Indexed(0);
		TextCharacter tile = new TextCharacter(' ', TextColor.ANSI.DEFAULT, TextColor.ANSI.DEFAULT);
		textGraphics.setCharacter(oY-1,oX+1,wall);
		for (x=0;x<this.dy;x++) {
			textGraphics.setCharacter(oY+x*2+1,oX+1,wall);
			textGraphics.setCharacter(oY+x*2,oX+1,wall);
		}
		for (y=1;y<=this.dx;y++) {
			textGraphics.setCharacter(oY-1,oX+y*2,wall);
			textGraphics.setCharacter(oY-1,oX+y*2+1,wall);
		}
		/* Traditionally X<> Y^v (x/y)
		 * in a Console however its (Rows (y)/Columns (x))
		 * 
		 * in standard Geometry the positive Axes are in the top right corner 0/0 being in the lower left.
		 * on the console however the only absolute coord is the top left.
		 * Plotting the 2 results in a map being upside down.
		 */
		/*
		 * ##############
		 * #121212121212
		 * #343434343434
		 * #121212121212
		 * #343434343434
		 *
		 * X+>-< Y+V-^
		 * 1 = Center block
		 * 2 = East
		 * 3 = South
		 * 4 = Alway a wall
		 */
		int tX=0, tY=0;		// temp Coords
		//Dialog.pln("X: "+this.x+" Y: "+this.y);
		for (x=0;x<this.dx;x++) {
			tX=this.dx-x;
//			textGraphics.setCharacter(oY,x*2+oX, wall);
			for (y=0;y<this.dy;y++) {
				if (this.x==x && this.y==y) {
					tile = tile.withCharacter('■').withForegroundColor(TextColor.ANSI.CYAN);
				} 
				if (canUp(x,y) || canDown(x,y)) {
					tile = tile.withCharacter('S').withForegroundColor(TextColor.ANSI.WHITE)
												  .withBackgroundColor(TextColor.ANSI.BLACK);
					if (this.x==x && this.y==y)
						tile = tile.withBackgroundColor(TextColor.ANSI.CYAN);
				} else if (this.hasMonster(x,y)) {
					if (this.map[x][y].type == Creature.BOSS)
						tile = tile.withCharacter('B').withForegroundColor(new TextColor.Indexed(12));
					else if (this.map[x][y].type == Creature.CHAMPION)
						tile = tile.withCharacter('C').withForegroundColor(new TextColor.Indexed(13));
					else
						tile = tile.withCharacter('M').withForegroundColor(new TextColor.Indexed(9));
					if (this.x==x && this.y==y)
						tile = tile.withBackgroundColor(TextColor.ANSI.CYAN);
				} else if (!(this.x == x && this.y == y)) {
					if (!canNorth(x,y) && !canSouth(x,y) && !canWest(x,y) && !canEast(x,y))
						tile = tile.withCharacter(' ').withForegroundColor(c).withBackgroundColor(c);
					else
						tile = tile.withCharacter(' ').withBackgroundColor(TextColor.ANSI.BLACK);
				}
				if (canEast(x,y)) {
					textGraphics.setCharacter(y*2+1+oY,tX*2+oX, blank);
				} else {
					textGraphics.setCharacter(y*2+1+oY,tX*2+oX, wall);
				}
				if (canSouth(x,y)) {
					textGraphics.setCharacter(y*2+oY,tX*2+1+oX, blank);
				} else {
					textGraphics.setCharacter(y*2+oY,tX*2+1+oX, wall);
				}
				textGraphics.setCharacter(y*2+1+oY,tX*2+1+oX, wall);
				textGraphics.setCharacter(y*2+oY,tX*2+oX, tile);
			}
		}
//		screen.setCharacter(0, 0, wall);
/*		for (int z=1;z<=255;z++) {
			if (x>=16) { y++; x=0; }
			TextColor.Indexed c = new TextColor.Indexed(z);
			TextCharacter wall = new TextCharacter(Integer.toString(z).charAt(0), c, c);
			textGraphics.setCharacter(x, y, wall);
			//textGraphics.putString(x,y,"0");
			//screen.refresh();
			x++;
		}*/
		return;
	}
	public int population() { return this.population; }
	public boolean isReturnTrip() {
		return returnTrip;
	}
	public void setReturnTrip(boolean returnTrip) {
		this.returnTrip = returnTrip;
	}
}
