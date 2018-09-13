package com.alfaloop.insoleble.visualization;
public class ShoePoint {
	public int x;
	public int y;
	public ShoePoint(int x, int y) {
		this.x = x;
		this.y = y;

	}

	public boolean equals(ShoePoint sp) {
		return x == sp.x && y == sp.y;
	}
}