package com.alfaloop.insoleble.visualization;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.alfaloop.insoleble.R;


public class InsoleSensor {

	private static float xScale;

	private static float yScale;

	private static byte[][] leftShoeBaseMask;

	private static byte[][] rightShoeBaseMask;

	private static int shoeBaseWidth;

	private static int shoeBaseHeight;

	private static ShoePoint[] leftSensorPoints;

	private static ShoePoint[] rightSensorPoints;

	private static Bitmap leftShoeBaseCover;

	private static Bitmap rightShoeBaseCover;

	public static void loadData(Resources res, float xScale, float yScale) {
		InsoleSensor.xScale = xScale;
		InsoleSensor.yScale = yScale;
		shoeBaseWidth = (int) (res.getInteger(R.integer.shoe_base_width) * xScale);
		shoeBaseHeight = (int) (res.getInteger(R.integer.shoe_base_height) * yScale);
		leftShoeBaseMask = loadShoeBaseData(res, R.array.left_shoe_base);
		rightShoeBaseMask = loadShoeBaseData(res, R.array.right_shoe_base);
		leftSensorPoints = loadShoePoints(res,
				R.array.left_shoe_points_x,
				R.array.left_shoe_points_y);
		rightSensorPoints = loadShoePoints(res,
				R.array.right_shoe_points_x,
				R.array.right_shoe_points_y);

		Bitmap shoeBaseCover = BitmapFactory.decodeResource(res, R.mipmap.cover);
		leftShoeBaseCover = Bitmap.createBitmap(shoeBaseCover,
				0, 0,
				shoeBaseCover.getWidth() / 2, shoeBaseCover.getHeight());
		rightShoeBaseCover = Bitmap.createBitmap(shoeBaseCover,
				shoeBaseCover.getWidth() / 2, 0,
				shoeBaseCover.getWidth() / 2, shoeBaseCover.getHeight());
	}

	private static byte[][] loadShoeBaseData(Resources res, int id) {

		byte[][] mask = new byte[shoeBaseHeight][shoeBaseWidth];
		String[] data = res.getStringArray(id);
		for (int h = 0; h < shoeBaseHeight; h++) {
			for (int w = 0; w < shoeBaseWidth; w++) {
				byte n = (byte) (data[(int) (h / yScale)].charAt((int) (w / xScale)) - '0');
				mask[h][w] = n;
			}
		}

		return mask;
	}

	private static ShoePoint[] loadShoePoints(Resources res, int xPointsId, int yPointsId) {
		ShoePoint[] points = new ShoePoint[4];
		int[] xPoints = res.getIntArray(xPointsId);
		int[] yPoints = res.getIntArray(yPointsId);
		for (int i = 0; i < 4; i++) {
			points[i] = new ShoePoint((int) (xPoints[i] * xScale), (int) (yPoints[i] * yScale));
		}
		return points;
	}

	public static byte[][] getShoeBaseMask(SensorDataGetter.Direction direction) {
		return direction == SensorDataGetter.Direction.LEFT ? leftShoeBaseMask : rightShoeBaseMask;
	}

	public static int getShoeBaseWidth() {
		return shoeBaseWidth;
	}

	public static int getShoeBaseHeight() {
		return shoeBaseHeight;
	}

	public static ShoePoint[] getSensorPoints(SensorDataGetter.Direction direction) {
		return direction == SensorDataGetter.Direction.LEFT ? leftSensorPoints : rightSensorPoints;
	}

	public static Bitmap getShoeBaseCover(SensorDataGetter.Direction direction) {
		return direction == SensorDataGetter.Direction.LEFT ? leftShoeBaseCover : rightShoeBaseCover;
	}

	synchronized public static ShoePoint getCenterOfPressurePoint(
            SensorDataGetter.Direction direction, float a, float b, float c, float d) {

		double xp = direction == SensorDataGetter.Direction.LEFT ?
								leftSensorPoints[0].x * a +
								leftSensorPoints[1].x * b +
								leftSensorPoints[2].x * c +
								leftSensorPoints[3].x * d :
								rightSensorPoints[0].x * a +
								rightSensorPoints[1].x * b +
								rightSensorPoints[2].x * c +
								rightSensorPoints[3].x * d;
		double yp = direction == SensorDataGetter.Direction.LEFT ?
								leftSensorPoints[0].y * a +
								leftSensorPoints[1].y * b +
								leftSensorPoints[2].y * c +
								leftSensorPoints[3].y * d :
								rightSensorPoints[0].y * a +
								rightSensorPoints[1].y * b +
								rightSensorPoints[2].y * c +
								rightSensorPoints[3].y * d;
		double p = a + b + c + d;
		return new ShoePoint((int) (xp / p), (int) (yp / p));
		
	}

}
