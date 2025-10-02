package org.smileaf.game;

/**
 * Thrown if No monsters are found during Creature.load();
 * @author smileaf
 *
 */
public class NoMonstersException extends Exception {
	private static final long serialVersionUID = 1L;

	public NoMonstersException (String errorMessage) {
		super(errorMessage);
	}
}
