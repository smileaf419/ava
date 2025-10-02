package org.smileaf;
/**
 * For use of Coloring the console using ANSI standards.
 * 
 * <h3>Examples:</h3>
 * <p>Blue text, Green background
 * <br>{@code Color text = new Color("Testing").fg(Color.BLUE).bg(Color.GREEN);}
 * <br>{@code System.out.println(text);}
 * </p>
 * <p>White text
 * <br>{@code Color text = new Color();}
 * <br>{@code text.text("Testing");}
 * <br>{@code text.fg(Color.WHITE);}
 * <br>{@code System.out.println(text);}
 * </p>
 * <p>Sets all font color to green.
 * <br>If no text is set the console colors are not reset.
 * <br>However if we again use Color to output text, printing that to the console will result in it being reset.
 * <br>{@code Color text = new Color().fg(Color.GREEN);}
 * <br>{@code System.out.print(text);}
 * <br>{@code System.out.println("This text is Green!");}
 * </p>
 * @author smileaf (smileaf@me.com)
 */
public class Color {
	/**
	 * Resets the Console colors to default
	 * All toString calls automatically reset the console colors if text is supplied.
	 */
	public static final String RESET = "\u001b[0m";
	
	public static final int BLACK = 0;
	public static final int RED = 1;
	public static final int GREEN = 2;
	public static final int YELLOW = 3;
	public static final int BLUE = 4;
	public static final int MAGENTA = 5;
	public static final int CYAN = 6;
	public static final int WHITE = 7;
	public static final int GRAY = 8;
	
	
	@SuppressWarnings("unused")
	private static final String BRIGHT = ";1";
	private static final String UNDERLINE = "\u001b[1m";
	private static final String BOLD = "\u001b[4m";
	private static final String REVERSED = "\u001b[7m";
	
	private boolean change_fg = false;
	private int fg = Color.WHITE;
	private boolean change_bg = false;
	private int bg = Color.BLACK;
	private boolean underline = false;
	private boolean bold = false;
	private boolean reversed = false;
	private String text = null;
	
	/**
	 * 
	 */
	public Color() {}
	/**
	 * Constructs a Color object with text.
	 * Example to output a red "Hi"
	 * {@code Color text = new Color("Hi").fg(Color.RED);
	 * System.out.println(text);}
	 * 
	 * @param text Text to be colorized.
	 */
	public Color (String text) {
		this.text = text;
	}
	/**
	 * Sets the Text to be colorized
	 * @param text Text to be modified.
	 * @return self
	 */
	public Color text (String text) { 
		this.text = text; 
		return this; 
	}
	/**
	 * Sets the foreground/font color
	 * @param fg value of the color for the foreground.
	 * @return self
	 */
	public Color fg(int fg) {
		this.change_fg = true;
		this.fg = fg;
		return this;
	}
	/**
	 * Sets the background color
	 * @param bg value of the color for the background
	 * @return self
	 */
	public Color bg(int bg) {
		this.change_bg = true;
		this.bg = bg;
		return this;
	}
	/**
	 * Underlines the text
	 * @deprecated Needs testing
	 * @return self
	 */
	public Color underline() { this.underline = true; return this; }
	/**
	 * Bolds the text
	 * @deprecated Needs testing
	 * @return self
	 */
	public Color bold() { this.bold = true; return this; }
	/**
	 * Reverses... something?
	 * @deprecated
	 * @return self
	 */
	public Color reversed() { this.reversed = true; return this; }

	/**
	 * Helper method to colorize values into a more readable form.
	 * Green if x / y &gt; 50
	 * Yellow if x / y &gt; 25 and &lt;= 50
	 * Red if x / y &lt;= 25
	 * @param x Low value
	 * @param y High value
	 * @return x / y (x/y)%
	 */
	public static String colorizeValues(float x, float y) {
		int valueP = (int)((x / y) * 100);
		Color valueC = new Color((int)x+" / "+(int)y +  " " + valueP + "%");
		if (valueP <= 25) valueC.fg(Color.RED);
		else if (valueP <= 50) valueC.fg(Color.YELLOW);
		else valueC.fg(Color.GREEN);
		return valueC.toString();
	}
	
	/**
	 * @return properly formated text.
	 */
	@Override 
	public String toString() {
		String s = "";
		if (bold) s += Color.BOLD;
		if (underline) s += Color.UNDERLINE;
		if (reversed) s += Color.REVERSED;
		
		if (change_fg) {
			s = "\u001b[38;5;";
			s += fg + "m" ;
		}
		if (change_bg) {
			s += "\u001b[48;5;";
			s += bg + "m" ;
		}
		if (text != null)
			return s + text + Color.RESET;
		return s;
	}
}
