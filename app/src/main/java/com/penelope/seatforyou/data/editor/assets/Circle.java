package com.penelope.seatforyou.data.editor.assets;

import android.graphics.Path;
import android.graphics.PointF;

public class Circle extends Figure{

    private float radius;

    public Circle(PointF spawnPoint, float radius) {
        super(spawnPoint);
        this.radius = radius;
        Path path = getPath();
        path.addCircle(spawnPoint.x, spawnPoint.y, radius, Path.Direction.CW);
        setPath(path);
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public float getArea(){
        return (float) (radius * radius * Math.PI);
    }

}
