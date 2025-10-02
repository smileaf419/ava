package org.smileaf.game;

/**
 * Thrown during any Resource's load()
 * @author smileaf
 *
 */
public class ResourceLoadException extends Exception {
	private static final long serialVersionUID = 1L;

	public ResourceLoadException (String errorMessage) {
		super(errorMessage);
	}
}
