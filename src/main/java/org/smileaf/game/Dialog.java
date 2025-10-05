package org.smileaf.game;

import org.smileaf.Color;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;

import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.*;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.input.KeyStroke;

/**
 * The way to display all display-able dialogs
 * @author smileaf (smileaf@me.com)
 * @version 2.0
 * @since 0.1
 */
/**
 * TODO:
 * Remove hard coded replacements and replace it with a loadable config file.
 * Remove references to the class Color and replace it with Lanterna.
 * Implement ability to specify a location on the terminal to contain dialog.
 * Implement ability to use this class to display graphical dialogs if available.
 * Fix Storyline displaying.
 */
public class Dialog implements Serializable {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Enables debug output.
	 */
	public static final byte DEBUG  = (byte)0b00000001;
	/**
	 * Enables a silent mode, disabling all output even when output is called.
	 */
	public static final byte SILENT = (byte)0b00000010;
	public static final byte NONE   = (byte)0b00000000;
	
	private static byte state = Dialog.NONE;
	
	/**
	 * Dialog to output
	 */
	public String dialog = "";
	/**
	 * Battle values
	 */
	public int damage = 0;
	/**
	 * Battle values
	 */
	public  int healing = 0;
	
	private ArrayList<Dialog.ReplaceWords> replacements = new ArrayList<Dialog.ReplaceWords>();

	private Player mc;
	private Creature mon;
	private Terminal terminal = null;
	
	public Dialog () {}
	public Dialog(Terminal terminal, String dialog) {
		input(terminal);
		text(dialog);
	}
	public Dialog (String dialog) {
		text(dialog);
	}
	
	/**
	 * Required before using any method involving an input.
	 */
	public Dialog input(Terminal terminal) {
		this.terminal = terminal;
		return this;
	}
	
	/**
	 *
	 */
	public void load() {
		Document doc = ResourceStore.getDoc("storyline/dialogReplacements.xml");
		
		NodeList nList = doc.getElementsByTagName("word");
		for (int x = 0; x < nList.getLength(); x++) {
			Element node = (Element) nList.item(x);
	 		String replace = ResourceStore.getValueFrom(node, "replace", "", false);
	 		String with = ResourceStore.getValueFrom(node, "with", "", false);
			replacements.add(new Dialog.ReplaceWords(replace, with));
		}
	}

	/**
	 * Sets the dialog and returns itself.
	 */
	public Dialog text(String dialog) {
		this.dialog = dialog;
		return this;
	}
	
	public Dialog healing(int healing) {
		this.healing = healing;
		return this;
	}
	public Dialog damage(int damage) {
		this.damage = damage;
		return this;
	}
	public Dialog setPlayer(Player mc) {
		this.mc = mc;
		return this;
	}
	public Dialog setMonster(Creature mon) {
		this.mon = mon;
		return this;
	}

	private String format(String dialog) {
		if (mon != null)
			dialog = dialog.replaceAll("%M", mon.name);
		if (mc != null) {
			if (mc.isMale)
				dialog = dialog.replaceAll("%G\\[(.*?)\\|.*?\\]", "$1");
			else if (mc.isFemale)
				dialog = dialog.replaceAll("%G\\[.*?\\|(.*?)\\|.*?\\]", "$1");
			else
				dialog = dialog.replaceAll("%G\\[.*?\\|.*?\\|(.*?)\\]", "$1");

			dialog = dialog.replaceAll("%Player", mc.name);
			// TODO: Genderize Dialog relating to the player.
		}
		//return dialog;
		return dialog.replaceAll("%Damage", new Color(Integer.toString(damage)).fg(Color.RED).toString())
					 .replaceAll("%Healing", new Color(Integer.toString(healing)).fg(Color.GREEN).toString())
		//			 .replaceAll("%Goddess", Dialog.GODDESS)
		//			 .replaceAll("%World", Dialog.WORLD)
					 .replaceAll("\\((..?)\\)", "\\("+ new Color("$1").fg(Color.GREEN)+"\\)");
	}

	public void println() {
		if (isSet(Dialog.SILENT)) return;
		Dialog.pln(this.format(dialog)); 
	}
	public void print() {
		if (isSet(Dialog.SILENT)) return;
		Dialog.p(this.format(dialog));
	}
	public void println(String s) {
		if (isSet(Dialog.SILENT)) return;
		Dialog.pln(this.format(s));
	}
	public void println(Object o) {
		if (isSet(Dialog.SILENT)) return;
		Dialog.pln(this.format(o.toString()));
	}
	public void print(String s) {
		if (isSet(Dialog.SILENT)) return;
		Dialog.p(this.format(s));
	}
	public void print(Object o) {
		if (isSet(Dialog.SILENT)) return;
		Dialog.p(this.format(o.toString()));
	}
	
	/*
	 * Story line Dialog
	 */
	public String loadStory(String s) {
		try {
			return ResourceStore.loadResource("/storyline/" + s + ".txt");
		} catch (IOException e) {
			Dialog.important("Error loading story chapter: " + s);
			e.printStackTrace();
			return "";
		}
	}
	
	public void storyDialog(String s) {
		/*newLine();
		char[] chars = this.format(s).toCharArray();
		try {
			Terminal terminal = TerminalBuilder.builder()
				    .jna(true)
				    .system(true)
				    .build();
			// raw mode means we get key presses rather than line buffered input
			terminal.enterRawMode();
			NonBlockingReader input = terminal.reader();
			Thread t1=new Thread() {
				public void run() {
					for (int i = 0; i < chars.length; i++) {
						System.out.print(chars[i]);
						try {
							if (chars[i] == '.')
								Thread.sleep(500);
							if (chars[i] == ',')
								Thread.sleep(200);
							Thread.sleep(5);
						} catch (InterruptedException e) {
							newLine();
							break;
						}
					}
				}  
			};
			t1.start();
			while (true) {
	        	if (input.read() == 27) {
	        		t1.interrupt();
	        		break;
	        	}
			}
			input.shutdown();
			terminal.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}*/
	}
	/**
	 * Gets input
	 * @param s Message to print
	 * @return inputed string
	 */
	public String getInput (String s) {
		print(s);
		try {
			return terminal.readInput().getCharacter().toString();
		} catch (IOException e) {}
		return "";
	}
	/**
	 * Get Input String
	 */
	public String getInputString (String s) {
		print(s);
		String str = "";
		try {
			KeyStroke key;
			do {
				key = terminal.readInput();
				if (key.getKeyType() == KeyType.Enter){
					print(key.getCharacter());
				} else if (key.getKeyType() == KeyType.Backspace) {
					if (str.length() > 0) {
						str = str.substring(0, str.length()-1); 
						print(key.getCharacter()+" "+key.getCharacter());
					}
				} else {
					str = str + key.getCharacter().toString(); 
					print(key.getCharacter());
				}
			} while (key.getKeyType() != KeyType.Enter);
		} catch (IOException e) {}
		return str;
	}
	/**
	 * prints a newline.
	 */
	public void nl () {
		if (isSet(Dialog.SILENT)) return;
		Dialog.newLine();
	}
	/**
	 * Error or Important
	 * @param s Dialog or message
	 */
	public void e (String s) {
		if (isSet(Dialog.SILENT)) return;
		Dialog.important(this.format(s));
	}
	/**
	 * Informational
	 * @param s Dialog or message
	 */
	public void i (String s) {
		if (isSet(Dialog.SILENT)) return;
		Dialog.info(this.format(s));
	}
	public void pause() {
		if (terminal == null) { e("Cannot pause terminal not defined."); return; }
		Dialog.pause(terminal);
	}
	
	public byte state() { return state; }
	public void setState(byte state) {
		this.state = state;
	}
	public void debug (boolean debug) {
		if (debug) this.state |= Dialog.DEBUG;
		else this.state &= ~Dialog.DEBUG;
	}
	public void silent (boolean debug) {
		if (debug) this.state |= Dialog.SILENT;
		else this.state &= ~Dialog.SILENT;
	}
	public static boolean isSet(byte state) {
		return ((Dialog.state & state) == state);
	}

	// Static Methods
	public static void pause(Terminal terminal) {
		p("Press any key to continue.");
		try {
			terminal.readInput();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		newLine();
	}
	
	/*
	 * Helper function: ! Message
	 */
	public static void important (String s) { Dialog.pln(new Color(" ! " + s).fg(Color.YELLOW)); }
	/*
	 * Helper function: * Message
	 */
	public static void info (String s) { Dialog.pln(new Color(" * " + s).fg(Color.BLUE)); }
	/*
	 * Prints a new line
	 */
	public static void newLine() { System.out.println(""); }
	/*
	 * Debug Methods.
	 */
	public static void debug (String s) { if (isSet(Dialog.DEBUG)) Dialog.p(new Color(s).fg(Color.RED)); }
	public static void debugln (String s) { if (isSet(Dialog.DEBUG)) Dialog.pln(new Color(s).fg(Color.RED)); }
	/*
	 * Helper Method to System.out.println
	 */
	public static void pln (String s) { System.out.println(s); }
	public static void pln (Object s) { System.out.println(s.toString()); }
	/*
	 * Helper Method to System.out.print
	 */
	public static void p (String s) { System.out.print(s); }
	public static void p (Object s) { System.out.print(s.toString()); }
	
	@Override
	public String toString() {
		return this.dialog;
	}
	
	private class ReplaceWords {
		public String replace = "";
		public String with = "";
		public ReplaceWords(String replace, String with) {
			this.replace = replace;
			this.with = with;
		}
	}
}
