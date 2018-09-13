package com.alfaloop.insoleble.visualization;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class FootPressureView extends View {

    private static final int MAX_PRESSURE = 80;

    private SensorDataGetter getter = null;
    private byte[][] shoeBaseMask;
    private Bitmap shoeBaseCover;

    private int shoeWidth;
    private int shoeHeight;

    private int gridWidth;
    private int gridHeight;
    private float a, b, c, d;
    private ShoePoint A, B, C, D;
    private double accX, accY, accZ;
    private double[][] distanceA;
    private double[][] distanceB;
    private double[][] distanceC;
    private double[][] distanceD;

    private Paint paint = new Paint();

    public FootPressureView(Context context, SensorDataGetter getter) {

        super(context);
        this.setBackgroundColor(Color.BLACK);
        this.getter = getter;
        this.A = getter.getPointA();
        this.B = getter.getPointB();
        this.C = getter.getPointC();
        this.D = getter.getPointD();

        this.shoeWidth = getter.getWidth();
        this.shoeHeight = getter.getHeight();
        this.shoeBaseMask = getter.getShoeBaseMask();

        setDistances();

    }

    private void setDistances() {
        distanceA = getDistanceArray(getSensorAreaList(2));
        distanceB = getDistanceArray(getSensorAreaList(3));
        distanceC = getDistanceArray(getSensorAreaList(4));
        distanceD = getDistanceArray(getSensorAreaList(5));
    }

    private List<ShoePoint> getSensorAreaList(int n) {
        List<ShoePoint> sensorAreaList = new ArrayList<ShoePoint>();
        for (int x = 0; x < shoeWidth; x++)
            for (int y = 0; y < shoeHeight; y++)
                if (shoeBaseMask[y][x] == n)
                    sensorAreaList.add(new ShoePoint(x, y));
        return sensorAreaList;
    }

    private double[][] getDistanceArray(List<ShoePoint> sensorAreaList) {
        double[][] distances = new double[shoeWidth][shoeHeight];
        for (int x = 0; x < shoeWidth; x++) {
            for (int y = 0; y < shoeHeight; y++) {
                distances[x][y] = Double.MAX_VALUE;
                for (ShoePoint sp : sensorAreaList) {
                    if (getPointsDistance(x, y, sp.x, sp.y) < distances[x][y]) {
                        distances[x][y] = getPointsDistance(x, y, sp.x, sp.y);
                    }
                }
            }
        }
        return distances;
    }

    @Override
    public void onDraw(Canvas canvas) {

        this.accX = getter.getX();
        this.accY = getter.getY();
        this.accZ = getter.getZ();

        if (shoeBaseCover == null) {
            shoeBaseCover = Bitmap.createScaledBitmap(
                    getter.getShoeBaseCover(), this.getWidth(), this.getHeight(), false);
        }

        gridWidth = this.getWidth()/shoeWidth + 1;
        gridHeight = this.getHeight()/shoeHeight + 1;

        super.onDraw(canvas);
        getPressureData();

        drawGridColor(canvas);
        drawPressureCenterPoint(canvas);

        canvas.drawBitmap(shoeBaseCover, 0, 0, paint);
        drawSensorData(canvas);
        invalidate();

    }

    private void drawGridColor(Canvas canvas) {

        for (int x = 0; x < shoeWidth; x++)
            for (int y = 0; y < shoeHeight; y++)
                if (shoeBaseMask[y][x] > 0) {
                    int pressure = getGridPressure(x, y);
                    drawGrid(canvas, x, y, pressureToColor(pressure));
                }
    }

    private int getGridPressure(int x, int y) {

        int pa = (int) (a / (distanceA[x][y] / 2 + 1));
        int pb = (int) (b / (distanceB[x][y] / 2 + 1));
        int pc = (int) (c / (distanceC[x][y] / 2 + 1));
        int pd = (int) (d / (distanceD[x][y] / 2 + 1));

        double sum = Math.min(pa + pb + pc + pd, MAX_PRESSURE);

        return (int) (Math.sin(sum / MAX_PRESSURE * Math.PI / 2) * MAX_PRESSURE);
    }

    private static double getPointsDistance(int x1, int y1, int x2, int y2) {
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }

    private void drawPressureCenterPoint(Canvas canvas) {
        int pointSize = (this.getWidth() + this.getHeight()) / 80;
        ShoePoint c = getter.getCenterOfPressurePoint();
        paint.setColor(Color.WHITE);
        if (c.x > 0 && c.y > 0) {
            int x = toRealX(c.x);
            int y = toRealY(c.y);
            canvas.drawRect(x - pointSize, y - 3, x + pointSize, y + 3, paint);
            canvas.drawRect(x - 3, y - pointSize, x + 3, y + pointSize, paint);
        }

    }

    private void getPressureData() {
        a = getter.getA()+1;
        d = getter.getD()+1;
        b = getter.getB()+1;
        c = getter.getC()+1;
    }

    private int pressureToColor(double pressure) {
        pressure = Math.min(pressure, MAX_PRESSURE);
        int c = Math.max(0, (int) (pressure * 450 / MAX_PRESSURE));

        if (c < 20)
            return Color.rgb(60, 60, 60);
        else if (c < 215)
            return Color.rgb(c + 40, 60, 60);
        else
            return Color.rgb(255, c - 195, 60);
    }

    private void drawGrid(Canvas canvas, float x, float y, int color) {
        paint.setColor(color);
        canvas.drawCircle(toRealX(x), toRealY(y), gridWidth / 2, paint);

    }

    private void drawSensorData(Canvas canvas) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(50.0f);

        canvas.drawText(String.format("X : %.2f", accX), this.getWidth() * 0.30f, this.getHeight() * 0.85f, paint);
        canvas.drawText(String.format("Y : %.2f", accY), this.getWidth() * 0.30f, this.getHeight() * 0.90f, paint);
        canvas.drawText(String.format("Z : %.2f", accZ), this.getWidth() * 0.30f, this.getHeight() * 0.95f, paint);
    }

    private void drawDataText(Canvas canvas, String name, double data, float x, float y) {
        canvas.drawText(name + " : " + data, x, y, paint);
    }

    private int toRealX(float x) {
        return (int) (x * this.getWidth() / shoeWidth);
    }

    private int toRealY(float y) {
        return (int) (y * this.getHeight() / shoeHeight);
    }

}