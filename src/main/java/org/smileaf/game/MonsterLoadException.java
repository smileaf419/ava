package org.smileaf.game;

/**
 * Thrown if an Error occurs during Creature.load()
 * @author smileaf
 *
 */
public class MonsterLoadException extends Exception {
	private static final long serialVersionUID = 1L;

	public MonsterLoadException (String errorMessage) {
		super(errorMessage);
	}
}
