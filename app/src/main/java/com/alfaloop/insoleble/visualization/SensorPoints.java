package com.alfaloop.insoleble.visualization;

public class SensorPoints {

	private int width;
	private int height;
	
	private ShoePoint pointA;
	private ShoePoint pointB;
	private ShoePoint pointC;
	private ShoePoint pointD;
	
	public SensorPoints(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public SensorPoints(int width, int height, ShoePoint... points) {
		this(width, height);
		this.pointA = points[0];
		this.pointB = points[1];
		this.pointC = points[2];
		this.pointD = points[3];
	}
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public void setPointA(ShoePoint point) {
		this.pointA = point;
	}

	public void setPointB(ShoePoint point) {
		this.pointB = point;
	}
	
	public void setPointC(ShoePoint point) {
		this.pointC = point;
	}
	
	public void setPointD(ShoePoint point) {
		this.pointD = point;
	}
	
	public ShoePoint getPointA() {
		return this.pointA;
	}
	
	public ShoePoint getPointB() {
		return this.pointB;
	}
	
	public ShoePoint getPointC() {
		return this.pointC;
	}
	
	public ShoePoint getPointD() {
		return this.pointD;
	}
}
