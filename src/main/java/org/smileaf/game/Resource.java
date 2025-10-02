package org.smileaf.game;

/**
 * Basic Container class for all Resources
 * @author smileaf
 *
 */
public abstract class Resource implements Itemizer, Cloneable {
	private static final long serialVersionUID = 1L;
	/**
	 * Resource Type
	 */
	public static final int NONE = 0;
	/**
	 * Resource Type
	 */
	public static final int JOB = 1;
	/**
	 * Resource Type
	 */
	public static final int CREATURE = 2;
	/**
	 * Resource Type
	 */
	public static final int ABILITY = 3;
	/**
	 * Resource Type
	 */
	public static final int BUFF = 4;
	/**
	 * Resource Type
	 */
	public static final int ITEM = 5;
	/**
	 * Type of resource
	 */
	protected int resourceType = NONE;
	/**
	 * Name of the Resource
	 */
	protected String name = "";
	/**
	 * Description of the Resource
	 */
	protected String description = "";
	/**
	 * Lore of the Resource
	 */
	protected String lore = "";
	/**
	 * Filename where the Resource resided.
	 */
	protected String filename = "";

	public String name() { return this.name; }
	public String description() { return this.description; }
	public String lore() { return this.lore; }
	public String filename() { return this.filename; }
	public int resourceType() { return this.resourceType; }
	
	public void setName(String name) { this.name = name; }
	public void setDescription(String description) { this.description = description; }
	public void setLore(String lore) { this.lore = lore; }
	public void setFilename(String filename) { this.filename = filename; }
	
	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
