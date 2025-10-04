package org.smileaf.ava;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.smileaf.Color;
import org.smileaf.Input;
import org.smileaf.game.Dialog;
import org.smileaf.game.LootItem;
import org.smileaf.game.Map;
import org.smileaf.game.ResourceStore;
import org.smileaf.game.Creature;

import com.googlecode.lanterna.terminal.*;
import com.googlecode.lanterna.screen.*;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.graphics.TextGraphics;

/**
 * Ava Dungeon Crawler Text based RPG.
 * Date: 2019-01-10
 * @author smileaf (smileaf@me.com)
 * @version 0.3
 */
public class App {
	public static final String AUTHOR = "smileaf";
	public static final float VERSION = 0.3f;
	public static final String COPYRIGHT = "2019-2025";

	public static final String gameDir = System.getProperty("user.home") + "/.ava/";

	public static File gameSave;
	public static Game game;
	public static Dialog dialog;
	public static ResourceStore resource;
	public static Terminal terminal = null;
	public static TextGraphics textGraphics;
	public static TerminalScreen screen = null;
	

	public App() {
		DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
		try {
			screen = defaultTerminalFactory.createScreen();
			screen.startScreen();
			terminal = screen.getTerminal();
			screen.setCursorPosition(null);
			
			textGraphics = terminal.newTextGraphics();
			int x = terminal.getTerminalSize().getColumns();
			int y = terminal.getTerminalSize().getRows();
			String s = "Terminal Size: " + x + " / " + y;
			textGraphics.putString(x-s.length(), 0, s);
			textGraphics.putString(0, 0, "Ava " + VERSION + " by " + AUTHOR + " c" + COPYRIGHT);
			
			game = new Game();
			gameSave = new File(gameDir + "game.sav");
			dialog = new Dialog(terminal, "");
			resource = new ResourceStore();
			ArrayList<String> paths = new ArrayList<String>();
			paths.add("creatures");
			paths.add("jobs");
			
			// Check game file.
			boolean gameExists = gameSave.exists();

			dspMenuItem(x/2, y/2-2 , "New Game");
			if (gameExists) {
				dspMenuItem(x/2, y/2-1 , "Continue");
			}
			dspMenuItem(x/2, y/2 , "Test");
			dspMenuItem(x/2, y/2+1 , "Quit");
			
			// Set the cursor for the resource loading.
			setCursor(0, 1);
			if (!resource.load(paths, game.getClass())) System.exit(1);

			// Reset Cursor
			screen.setCursorPosition(null);
			
			// Start the main loop
			do {
				Character loadGame = Character.toLowerCase(terminal.readInput().getCharacter());
				if (loadGame == 'c') {
					try {
						Dialog.p("Loading...");
						game = (Game) Game.loadGame();
						game.player.reloadAbilties();
						App.dialog.setPlayer(game.player);
					} catch (Exception ex) {
						Dialog.pln("Failed!");
						ex.printStackTrace();
						System.exit(1);
					}
					break;
				} else if (loadGame == 'n') {
					File gameDirF = new File(gameDir);
					if (!gameDirF.exists()) {
						try {
							gameDirF.mkdir();
						} catch (SecurityException se) {
							App.dialog.e("Error Creating directory: "+gameDir+" We don't have permission!");
							System.exit(1);
						}
					}
					game.newGame();
					break;
				} else if (loadGame == 't') {
					terminal.clearScreen();
					Test test = new Test();
					test.test();
					System.exit(0);
				} else {
					Dialog.pln("Exiting.");
					System.exit(0);
				}
				Dialog.pln(game.player);
			} while (true);
			terminal.clearScreen();
			gameLoop();
		} catch(IOException e) {
			e.printStackTrace();
		} finally {
			if(terminal != null) {
				try {
					terminal.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static void setCursor(int x, int y) throws IOException {
		textGraphics.putString(x, y, " ");
	}
	public static void resetCursor() throws IOException {
		int x = terminal.getTerminalSize().getColumns();
		int y = terminal.getTerminalSize().getRows();
		textGraphics.putString(x, y, " ");
	}
	public static void dspMenuItem(int x, int y, String name) throws IOException {
		textGraphics.putString(x, y, name);
		textGraphics.putString(x, y, name.substring(0, 1).toUpperCase(), SGR.BOLD);
	}
	public static void drawBox(TextGraphics textGraphics, int x1, int y1, int x2, int y2) {
		//┏┓┗┛━┃┣┫┳╋┻
		textGraphics.setCharacter(x1*2, y1*2, '┏');
		textGraphics.setCharacter(x1*2, y2*2+2, '┗');
		textGraphics.setCharacter(x2*2+2, y1*2, '┓');
		textGraphics.setCharacter(x2*2+2, y2*2+2, '┛');
		for (int x=x1+1;x<x2*2+2;x++) {
			textGraphics.setCharacter(x, y1*2, '━');
			textGraphics.setCharacter(x, y2*2+2, '━');
		}
		for (int y=y1+1;y<y2*2+2;y++) {
			textGraphics.setCharacter(x1*2, y, '┃');
			textGraphics.setCharacter(x2*2+2, y, '┃');
		}
	}

	public static void gameLoop() {
		boolean monster;
		Test test = new Test();
		try {
			do {
				drawBox(textGraphics, 0, 0, game.map.maxY, game.map.maxX);
				game.map.setViewX((terminal.getTerminalSize().getRows()) / 4 - 2);;
				game.map.setViewY(terminal.getTerminalSize().getColumns() / 4 - 2);
				monster = false;
				game.map.dspMap(textGraphics);
				setCursor(0,game.map.maxX*2+3);
				Dialog.p(new Color(CreatureMap.LAYERNAMES[Math.round(game.map.z() / 15)]).bg(CreatureMap.MAPCOLORS[Math.round(game.map.z() / 15)]));
				Dialog.pln(" Floor: "+game.map.z()+" Monsters Remaining: " + game.map.population());
				App.dialog.print("(C)haracter, (I)nventory, (S)ave, (R)est or (Q)uit");
				if (game.map.canDown() || game.map.canUp()) {
					App.dialog.print("; Stairs available: ");
					if (game.map.canDown())
						App.dialog.print("(D)own");
					else
						App.dialog.print("(U)p");
				}
				App.dialog.println(  "                            ");
				//App.dialog.print("What would you like to do? ");
				input: while (true) {
					KeyStroke key = terminal.readInput();
					if (key.getKeyType() == KeyType.ArrowUp) {
						game.map.go(Map.NORTH);
						monster = game.map.hasMonster(game.map.x(), game.map.y());
						break input;
					} else if (key.getKeyType() == KeyType.ArrowDown) {
						game.map.go(Map.SOUTH);
						monster = game.map.hasMonster(game.map.x(), game.map.y());
						break input;
					} else if (key.getKeyType() == KeyType.ArrowRight) {
						game.map.go(Map.EAST);
						monster = game.map.hasMonster(game.map.x(), game.map.y());
						break input;
					} else if (key.getKeyType() == KeyType.ArrowLeft) {
						game.map.go(Map.WEST);
						monster = game.map.hasMonster(game.map.x(), game.map.y());
						break input;
					} else if (key.getKeyType() == KeyType.Character) {
							switch (Character.toLowerCase(key.getCharacter())) {
								case 'c':
									Dialog.pln(game.player); 
									dialog.pause();
									break input;
								case 'd':
									if (game.map.go(Map.DOWN)) {
										game.genMap(game.map.z());
										game.map.set(game.map.x(), game.map.y(), Map.UP);
									}
									break;
								case 'u':
									if (game.map.go(Map.UP)) {
										game.genMap(game.map.z());
										game.map.set(game.map.x(), game.map.y(), Map.UP);
									}
									break;
								case 'q':
									game.save();
									System.exit(0); 
									break;
								case 'r':
									game.sleep(); 
									dialog.pause();
									break input;
								case 's':
									game.save();
									dialog.pause();
									break input;
								case 'i':
									// Shows Character Inventory
									Dialog.pln("Inventory:");
									if (game.player.inventory().size() == 0) {
										Dialog.pln("Nothing!");
									} else {
										for (LootItem li : game.player.inventory()) {
											Dialog.pln(li.item().name() + " x" + li.amount());
										}
									}
									dialog.pause();
									break input;
								// Cheat Codes
								case 'p':
									game.player.levelUp();
									dialog.pause();
									break input;
								case 'x':
									game.map.set(game.map.x(), game.map.y(), Map.DOWN);
									break input;
								case 'e':
									game.player.equip(test.loadEquipment());
									break input;
								case 'j':
									game.player.setJob(test.loadJob());
									break input;
								default:
									break input;
							}
					}
				}
				Dialog.newLine();
				
				if (monster) {
					terminal.clearScreen();
					// Initiate a battle!
					Creature mon = game.map.getMonster(game.map.x(), game.map.y());
					// Do battle stuff here
					Battle battle = new Battle(dialog, game.player, mon);
					battle.setDialog(dialog);
					switch (battle.battleStart()) {
					case Battle.WIN:
						// Clear buffs and fix our HP if we had a stamina buff.
						game.player.endBattle();
						Dialog.newLine();
						// if we we win
						ArrayList<LootItem> loot = mon.loot();
						if (loot.size() > 0) {
							Dialog.p("Loot Obtained: ");
							Dialog.pln(loot);
							game.player.gainLoot(loot);
						}
						game.map.monsterDies(game.map.x(), game.map.y());
						game.player.gainGold(mon.gold() * mon.level());
						game.player.gainExp(mon.exp() * mon.level());
						if (mon.type == Creature.BOSS && game.map.z() != Map.maxZ) {
							// Congrats we cleared the level!
							game.map.set(game.map.x(), game.map.y(), Map.DOWN);
							Dialog.pln("As you defeat the monster a staircase appears allowing you to go down.");
						}
						dialog.pause();
						break;
					case Battle.RAN:
						Dialog.pln("Ran Away!");
						// Clear buffs and fix our HP if we had a stamina buff.
						game.player.endBattle();
						mon.endBattle();
						break;
					case Battle.LOSE:
						// Game over.
						Dialog.pln("Game Over: You have died!");
						dialog.pause();
						System.exit(0);
					}
					terminal.clearScreen();
				}
			} while (true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
