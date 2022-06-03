package com.penelope.seatforyou.data.editor.assets;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;
import java.util.List;

/**
 * 임의 개수의 꼭지점을 가진 다각형을 표현하는 클래스
 * 생성위치(중심점)과 다각형을 이루는 꼭지점들의 리스트를 받아서 생성됨
 */
public class Polygon extends Figure{

    protected List<PointF> points = new ArrayList<>();
    protected double rotationAngle = 0;

    public Polygon(PointF spawnPoint) {
        super(spawnPoint);
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle % 360;
    }

    public float getArea(){
        float total = 0;
        if(points.size() >= 3){
            for (int i = 0; i < points.size(); i++) {
                float addX = points.get(i).x;
                float addY = points.get(i == points.size() - 1 ? 0 : i + 1).y;
                float subX = points.get(i == points.size() - 1 ? 0 : i + 1).x;
                float subY = points.get(i).y;
                total += (addX * addY * 0.5);
                total -= (subX * subY * 0.5);
            }
            return Math.abs(total);
        }else return 0;
    }

    public List<PointF> getPoints() {
        return points;
    }

    // 점들을 지정하면 자동으로 path 객체 생성후 저장
    public void setPoints(List<PointF> points) {
        this.points = points;
        Path path = getPath();
        path.reset();
        path.moveTo(points.get(0).x, points.get(0).y);
        // 모든 점들을 선으로 잇는다.
        for (int i = 1; i < points.size(); i++) {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        path.lineTo(points.get(0).x, points.get(0).y);
        setPath(path);
    }

    @Override
    public void setSpawnPoint(PointF spawnPoint) {
        float dx = spawnPoint.x - this.spawnPoint.x;
        float dy = spawnPoint.y - this.spawnPoint.y;

        for (PointF p : points){
            p.x += dx;
            p.y += dy;
        }

        super.setSpawnPoint(spawnPoint);
    }
}
