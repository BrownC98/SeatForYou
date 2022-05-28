package com.penelope.seatforyou.data.editor.assets;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;

/**
 * 다각형, 원형을 포함한 모든 도형을 추상화한 클래스
 */
public abstract class Figure {

    public PointF spawnPoint;
    private Paint paint = new Paint();
    private Path path = new Path();
    private Region region = new Region();
    private static Region clip;

    public Figure(PointF spawnPoint) {
        this.spawnPoint = spawnPoint;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(12);
        paint.setColor(Color.BLACK);
        paint.setAntiAlias(true);
    }

    public PointF getSpawnPoint() {
        return spawnPoint;
    }

    public void setSpawnPoint(PointF spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;

        region.setPath(path, clip);
    }

    public static Region getClip() {
        return clip;
    }

    public static void setClip(Region clip) {
        Figure.clip = clip;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public abstract float getArea();
}
