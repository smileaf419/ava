package org.smileaf.game;

import java.io.Serializable;

import org.w3c.dom.Element;

/**	
 * Standard Interface for all items and creatures.
 * 
 * @author smileaf (smileaf@me.com)
 */
public interface Itemizer extends Serializable {
	static final long serialVersionUID = 1L;
	
	/**
	 * Open the file and populate variables specific to the Object
	 * @param node Node to load
	 * @param store ResourceStore to load from.
	 * @throws ResourceLoadException if the document fails to load.
	 */
	public void load(Element node, ResourceStore store) throws ResourceLoadException;

	/**
	 * As everything has a name, it should be easy to display it, but leave the implementation to the Object.
	 */
	@Override
	public String toString();
}
