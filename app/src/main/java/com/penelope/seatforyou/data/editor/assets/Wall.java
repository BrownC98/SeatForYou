package com.penelope.seatforyou.data.editor.assets;

import android.graphics.Path;
import android.graphics.PointF;

import java.util.ArrayList;

/**
 * 벽은 2차원 평면상에서 하나의 직선으로 표현됨
 */
public class Wall extends Polygon{

    PointF startPt;
    PointF endPt;
    Path path;

    public Wall(PointF spawnPoint) {
        super(spawnPoint);
        setStartPoint(spawnPoint);
    }

    public void setEndPoint(PointF point){
        endPt = point;

        points.clear();
        points.add(new PointF(startPt.x, startPt.y));
        points.add(new PointF(endPt.x, endPt.y));

        Path path = getPath();
        path.reset();
        path.moveTo(points.get(0).x, points.get(0).y);
        path.lineTo(points.get(1).x, points.get(1).y);
        setPath(path);
    }

    public void setStartPoint(PointF point){
        this.startPt = point;
        setSpawnPoint(point);
    }

   public PointF getStartPoint(){
        return startPt;
   }

   public PointF getEndPt(){
        return endPt;
   }

}
