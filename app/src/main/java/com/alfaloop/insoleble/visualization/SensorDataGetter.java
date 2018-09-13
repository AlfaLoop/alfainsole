package com.alfaloop.insoleble.visualization;

import android.graphics.Bitmap;
import android.util.Log;

public class SensorDataGetter {

	public enum Direction {
		RIGHT, LEFT
	}
	
	protected Direction direction;

	private SensorPoints sensorPoints = null;

	protected float pa, pb, pc, pd;

	protected float x, y, z;

	private boolean keepShowData = false;

	private byte[][] shoeBaseMask;

	private Bitmap shoeBaseCover;

	public SensorDataGetter(Direction direction) {
		this(direction, false);
	}
	
	public SensorDataGetter(Direction direction, boolean showData) {
		this.direction = direction;
		ShoePoint[] points = InsoleSensor.getSensorPoints(direction);
		this.sensorPoints = new SensorPoints(InsoleSensor.getShoeBaseWidth(),
											 InsoleSensor.getShoeBaseHeight(),
											 points);
		shoeBaseMask = InsoleSensor.getShoeBaseMask(direction);
		shoeBaseCover = InsoleSensor.getShoeBaseCover(direction);
		this.keepShowData = showData;
	}

	public void addSensorData(float[] signals) {
		x = signals[0];
		y = signals[1];
		z = signals[2];
		pa = signals[3];
		pb = signals[4];
		pc = signals[5];
		pd = signals[6];
		if (keepShowData)
			showData(signals);
	}
	
	private void showData(float[] signals) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%.2f   ",
				Math.sqrt(signals[0] * signals[0] +
						signals[1] * signals[1] +
						signals[2] * signals[2])));
		for (int i = 0; i <= 2; i++)
			if (signals[i] < 0)
				sb.append(String.format("%.1f  ", signals[i]));
			else
				sb.append(String.format(" %.1f  ", signals[i]));
		for (int i = 3; i <= 6; i++)
			if (signals[i] < 0)
				sb.append(String.format("%8.0f", signals[i]));
			else
				sb.append(String.format(" %8.0f", signals[i]));
		sb.append("\n");
		Log.e("DATA", new String(sb));
	}
	
	synchronized public float getX() {
		return x;
	}
	
	synchronized public float getY() {
		return y;
	}
	
	synchronized public float getZ() {
		return z;
	}
	
	synchronized public float getA() {
		return pa;
	}
	
	synchronized public float getB() {
		return pb;
	}
	
	synchronized public float getC() {
		return pc;
	}
	
	synchronized public float getD() {
		return pd;
	}
	
	synchronized public float getG() {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}
	
	public ShoePoint getPointA() {
		return sensorPoints.getPointA();
	}
	
	public ShoePoint getPointB() {
		return sensorPoints.getPointB();
	}
	
	public ShoePoint getPointC() {
		return sensorPoints.getPointC();
	}
	
	public ShoePoint getPointD() {
		return sensorPoints.getPointD();
	}

	public byte[][] getShoeBaseMask() {
		return shoeBaseMask;
	}

	public Bitmap getShoeBaseCover() {
		return shoeBaseCover;
	}

	public int getWidth() {
		return sensorPoints.getWidth();
	}
	
	public int getHeight() {
		return sensorPoints.getHeight();
	}
	
	synchronized public ShoePoint getCenterOfPressurePoint() {
		return InsoleSensor.getCenterOfPressurePoint(direction, pa, pb, pc, pd);
	}

	public void close() {
	}

}
