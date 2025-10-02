package org.smileaf.game;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Basic Map Abstract class.
 * @author smileaf (smileaf@me.com)
 */
public abstract class Map implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// Room Directions
	public static final byte NORTH = (byte)0b00000001;
	public static final byte SOUTH = (byte)0b00000010;
	public static final byte EAST  = (byte)0b00000100;
	public static final byte WEST  = (byte)0b00001000;
	public static final byte UP    = (byte)0b00010000;
	public static final byte DOWN  = (byte)0b00100000;
	
	public static final byte EMPTY = (byte)0b00000000;
	public static final byte FULL  = (byte)0b00001111;
	
	// default Player start locations
	protected int x = 5;
	protected int y = 10;
	protected int z = 1;
	// Dungeon dimensions.
	protected int dx = 10;
	protected int dy = 20;
	protected int dz = 1;
	// Maximum Dimensions.
	public static final int maxX = 20;
	public static final int maxY = 50;
	public static final int maxZ = 50;
	// Map matrix of directions.
	private byte[][] map;
	
	// Number of spaces to all sides of our current location.
	protected int viewportX = 5;
	protected int viewportY = 10;

	public Map() {
		this(10,20,1);
	}

	public Map(int dx, int dy, int dz) {
		if (dx>maxX) dx=maxX;
		if (dy>maxY) dy=maxY;
		this.x = Math.round(dx / 2);
		this.y = Math.round(dy / 2);
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		this.genMap(dx, dy);
		if (this.z > 1)
			map[this.x][this.y] |= Map.UP; 
	}
	
	public int[] findUp() {
		int[] up = new int[2];
		for (int x=0;x<this.dx;x++) {
			for (int y=0;y<this.dy;y++) {
				if (canUp(x,y)) {
					up[0] = x; up[1] = y;
					return up;
				}
			}
		}
		return up;
	}
	public int[] findDown() {
		int[] d = new int[2];
		for (int x=0;x<this.dx;x++) {
			for (int y=0;y<this.dy;y++) {
				if (canDown(x,y)) {
					d[0] = x; d[1] = y;
					return d;
				}
			}
		}
		return d;
	}
	
	public void genMap(int gx, int gy) {
		if (gx>this.maxX) this.dx=this.maxX; else this.dx = gx;
		if (gy>this.maxY) this.dy=this.maxY; else this.dy = gy;
		map = new byte[gx][gy];
		for (int x = 0; x <  gx; x++) {
			for (int y = 0; y < gy; y++) {
				map[x][y] = (byte)ThreadLocalRandom.current().nextInt(0, 15 + 1);
			}
		}
		this.fixBorders();
	}
	
	public byte[][] getViewport() {
		byte[][] view = new byte[viewportX*2+1][viewportY*2+1];
		for (int a=this.x-viewportX-1;a<this.x+viewportX;a++) {
			for (int b=this.y-viewportY-1;b<this.y+viewportY;b++) {
				if (a>this.dx || a<0 || b>this.dy || b<0) {
					view[a][b] = Map.EMPTY;
				} else {
					view[a][b] = this.map[a][b];
				}
			}
		}
		return view;
	}
	
	public void fixBorders() {
		for (int x=this.dx-1; x>=0; x--) {
			for (int y=0; y<this.dy; y++) {
				if (y<this.dy-1 &&canEast(x,y)) this.map[x][y+1] |= Map.WEST;
				if (x>0 && canSouth(x,y)) this.map[x-1][y] |= Map.NORTH;
				if (x<this.dx-1 && canNorth(x,y)) this.map[x+1][y] |= Map.SOUTH;
				if (y>0 && canWest(x,y)) this.map[x][y-1] |= Map.EAST;
			}
		}
		int lx[] = { 0, this.dx -1};
		int ly[] = { 0, this.dy -1};
		for (int x : lx ) {
			for (int y = 0; y<this.dy; y++) {
				if (x == 0) {
					this.map[x][y] &= ~Map.SOUTH;
				} else {
					this.map[x][y] &= ~Map.NORTH;
				}
			}
		}
		for (int y : ly ) {
			for (int x = 0; x<this.dx; x++) {
				if (y == 0) {
					this.map[x][y] &= ~Map.WEST;
				} else {
					this.map[x][y] &= ~Map.EAST;
				}
			}
		}
	}
	
	public void setPos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(int x, int y, int direction) {
		if (direction == Map.DOWN && this.z == Map.maxZ) return;
		if (direction == Map.UP && this.z == 0) return;
		this.map[x][y] |= direction;
	}
	
	public boolean go(int direction) {
		if (direction == Map.NORTH && this.canNorth()) {
			this.x++;
		} else if (direction == Map.SOUTH && this.canSouth()) {
			this.x--;
		} else if (direction == Map.WEST && this.canWest()) {
			this.y--;
		} else if (direction == Map.EAST && this.canEast()) {
			this.y++;
		} else if (direction == Map.UP && this.canUp()) {
			this.z--;
		} else if (direction == Map.DOWN && this.canDown()) {
			this.z++;
		} else {
			System.out.println("Can't go that direction!");
			return false;
		}
		return true;
	}
	
	public boolean canNorth () {
		return canNorth(this.x,this.y);
	}
	public boolean canSouth () {
		return canSouth(this.x,this.y);
	}
	public boolean canWest () {
		return canWest(this.x,this.y);
	}
	public boolean canEast () {
		return canEast(this.x,this.y);
	}
	public boolean canUp() {
		return canUp(this.x,this.y);
	}
	public boolean canDown() {
		return canDown(this.x,this.y);
	}
	public boolean canNorth(int x, int y) {
		return ((map[x][y] & Map.NORTH) == Map.NORTH);
	}
	public boolean canSouth(int x, int y) {
		return ((map[x][y] & Map.SOUTH) == Map.SOUTH);
	}
	public boolean canWest(int x, int y) {
		return ((map[x][y] & Map.WEST) == Map.WEST);
	}
	public boolean canEast(int x, int y) {
		return ((map[x][y] & Map.EAST) == Map.EAST);
	}
	public boolean canUp(int x, int y) {
		return ((map[x][y] & Map.UP) == Map.UP);
	}
	public boolean canDown(int x, int y) {
		return ((map[x][y] & Map.DOWN) == Map.DOWN);
	}
	public boolean can(byte[][] map, int direction, int x, int y) {
		return ((map[x][y] & direction) == direction);
	}
	public int dx() { return this.dx; }
	public int dy() { return this.dy; }
	public int dz() { return this.dz; }
	public int x() { return this.x; }
	public int y() { return this.y; }
	public int z() { return this.z; }
	public int viewportX() { return this.viewportX; }
	public int viewportY() { return this.viewportY; }
	public void setViewX(int x) { this.viewportX = x; }
	public void setViewY(int y) { this.viewportY = y; }
}
