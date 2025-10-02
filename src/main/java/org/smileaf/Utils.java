package org.smileaf;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.concurrent.ThreadLocalRandom;


/**
 * This was made to hold all Utilities commonly used not provided by the Java API.
 * Start date: 2005-04-05
 *
 @author Stephen Leaf (smileaf@me.com)
 @version 2.0
 */
public class Utils {
    // http://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[1;30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    
    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";
	/**
	 * getSpaces is designed to be used to format text either on a screen or in a file in terms of Position using spaces.
	 @param spaces Number of spaces you want returned
	 @return String however many spaces you asked for.
	 @since 1.0
	 */
	public static String getSpaces(int spaces) {
		return Utils.getChar(spaces, " ");
	}
	/**
	 * getChar returns a number of characters specified
	 * @param amount number of characters to be returned.
	 * @param ch The character to be returned.
	 * @return String the amount of characters you asked for.
	 * @since 2.0
	 */
	public static String getChar(int amount, String ch) {
		StringBuffer sb = new StringBuffer(amount);
		for (int x=0;x<amount;x++)
			sb.append(ch);
		return sb.toString();
	}
	/**
	 * readFile reads a file and returns the result as a String
	 @param filename Filename to be read
	 @return String the file's contents
	 @since 2.0
	 */
	public static String readFile(String filename) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();
	        while (line != null) {
	            sb.append(line);
	            line = br.readLine();
	        }
	        result = sb.toString();
	        br.close();
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	/**
	 * Capitalize 1st letter in a string.
	 * @param original Text to modficy
	 * @return modified text
	 * @since 2.0
	 */
	public static String capitalizeFirst(String original) {
	    if (original == null || original.length() == 0) {
	        return original;
	    }
	    return original.substring(0, 1).toUpperCase() + original.substring(1);
	}
	/**
	 * Random number helper
	 * @param a Low
	 * @param b High
	 * @return random number
	 * @since 2.0
	 */
	public static int random(int a, int b) {
		return ThreadLocalRandom.current().nextInt(a, b);
	}
	/**
	 * https://stackoverflow.com/questions/852665/command-line-progress-bar-in-java
	 * @author maytham-ɯɐɥʇʎɐɯ
	 * @param dialog Dialog to display in front of display
	 * @param remain current value
	 * @param total total value
	 */
	public static void progressPercentage(String dialog, int remain, int total) {
	    if (remain > total) {
	        //throw new IllegalArgumentException();
	    	remain = total;
	    }
	    int maxBareSize = 10; // 10unit for 100%
	    int remainProcent = ((100 * remain) / total) / maxBareSize;
	    char defaultChar = '-';
	    String icon = "*";
	    String bare = new String(new char[maxBareSize]).replace('\0', defaultChar) + "]";
	    StringBuilder bareDone = new StringBuilder();
	    bareDone.append(dialog + " [");
	    for (int i = 0; i < remainProcent; i++) {
	        bareDone.append(icon);
	    }
	    String bareRemain = bare.substring(remainProcent, bare.length());
	    System.out.print("\r" + bareDone + bareRemain + " " + remainProcent * 10 + "%");
	    if (remain == total) {
	        System.out.print("\n");
	    }
	}
}