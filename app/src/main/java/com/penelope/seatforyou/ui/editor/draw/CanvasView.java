package com.penelope.seatforyou.ui.editor.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.penelope.seatforyou.R;
import com.penelope.seatforyou.data.editor.assets.Circle;
import com.penelope.seatforyou.data.editor.assets.Figure;
import com.penelope.seatforyou.data.editor.assets.Squre;
import com.penelope.seatforyou.data.editor.assets.Wall;
import com.penelope.seatforyou.ui.editor.adapter.EditorSideTabAdapter;
import com.penelope.seatforyou.ui.editor.adapter.SideTabData;

import java.util.ArrayList;
import java.util.List;

/**
 * 실제 그림이 그려지는 뷰 클래스
 */
public class CanvasView extends View {
    // 배치할 도형
    public ArrayList<Figure> figureList = new ArrayList<>();
    // 사이드 탭에서 선택한 도형을 추가함
    private List<SideTabData> sideTabDataList;
    private Canvas canvas;
    private int id; // 생성되는 도형에 부여할 id

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    public CanvasView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        this.canvas = canvas;
        if (Figure.getClip() == null)
            Figure.setClip(new Region(0, 0, canvas.getWidth(), canvas.getHeight()));

        // 모든 도형을 살펴봐서
        for (Figure figure : figureList) {
            Path path = figure.getPath();
            Paint paint = figure.getPaint();
            canvas.drawPath(path, paint);
        }
        super.onDraw(canvas);
    }

    // 뷰 초기 세팅
    private void init() {
        setId(View.generateViewId());
        sideTabDataList = EditorSideTabAdapter.getLocalDataSet();
    }

    // 지정모드가 활성화 된 생성물의 식별자(figure의 id)
    private int curSelected = -1;
    private boolean makeLine = false; // 지금 줄을 긋고 있는 중인지 확인하는 변수
    private PointF preTouchPt; // 직전 터치 좌표

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        int touchedIdx = getTouchedIdx(x, y); // figure 목록중 선택된 인덱스
        int sideTabIdx = getSelectedIdx(); // 사이드탭 선택 인덱스

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (sideTabIdx != -1 && touchedIdx == -1) { // 사이드 선택하고, 빈 땅을 터치하면 도형생성
                    curSelected = createFigure(x, y, sideTabDataList.get(sideTabIdx));
                } else if (touchedIdx != -1) { // 사이드탭 상관없이 기존 도형터치하면 도형지정
                    int newSelected = figureList.get(touchedIdx).getFigureId();
                    if (curSelected == newSelected) { // 이미 지정된 도형을 터치하면 지정이 풀림
                        Paint paint = figureList.get(touchedIdx).getPaint();
                        paint.setColor(Color.BLACK);
                        curSelected = -1;
                    } else {
                        curSelected = newSelected;
                    }
                } else if (curSelected != -1 && touchedIdx == -1) { // 지정된 도형이 있는 상태로 맨땅 클릭하면 지정 풀림
                    Paint paint = figureList.get(idToIdx(curSelected)).getPaint();
                    paint.setColor(Color.BLACK);
                    curSelected = -1;
                }
                if (curSelected != -1) { // 현재 지정된 도형의 색을 변경함
                    Paint paint;
                    for (Figure f : figureList) {
                        paint = f.getPaint();
                        paint.setColor(Color.BLACK);
                    }
                    paint = figureList.get(idToIdx(curSelected)).getPaint();
                    paint.setColor(Color.GREEN);
                    preTouchPt = new PointF(x, y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // 지정된 도형을 드래그 하는지 감지
                // 터치하고 있는 물체의 리스트 인덱스를 가져옴
                if (curSelected != -1) {
                    if (makeLine) { // 선을 만드는 중이라면
                        ((Wall) figureList.get(idToIdx(curSelected))).setEndPoint(new PointF(x, y));
                    } else {
                        movePolygon(x, y, curSelected);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (makeLine) makeLine = false;
                break;
        }
        invalidate();
        return true;
    }

    /**
     * 주어진 figure id가 figurelist의 몆번째 인덱스인지 반환하는 함수
     *
     * @return figurelist에 해당하는 값이 없으면 -1 반환
     */
    public int idToIdx(int figureId) {
        for (int i = 0; i < figureList.size(); i++) {
            if (figureId == figureList.get(i).getFigureId()) return i;
        }
        return -1;
    }

    /**
     * 주어진 좌표를 포함하는 도형이 도형리스트의 몇번째 도형인지 반환함
     * 최근에 생성된 순으로 먼저 감지함
     *
     * @param x - 터치한 x 좌표
     * @param y - 터치한 y 좌표
     * @return - 빈 공간이라면 -1 반환
     * - 도형 내부라면 해당 도형의 리스트 인덱스를 반환
     */
    public int getTouchedIdx(float x, float y) {
        for (int i = figureList.size() - 1; i >= 0; i--) {
            if (!makeLine && figureList.get(i) instanceof Wall) {
                if (isPointOnLine((Wall) figureList.get(i), new PointF(x, y))) {
                    return i;
                }
            } else {
                Region r = figureList.get(i).getRegion();
                if (r.contains((int) x, (int) y)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public boolean isPointOnLine(Wall wall, PointF point) {
        final float EPSILON = wall.getPaint().getStrokeWidth() + 100;
        PointF lineStaPt = wall.getStartPoint();
        PointF lineEndPt = wall.getEndPt();

        if (Math.abs(lineStaPt.x - lineEndPt.x) < EPSILON) {
            return (Math.abs(point.x - lineStaPt.x) < EPSILON);
        } else {
            // y = mx + b
            float m = (lineEndPt.y - lineStaPt.y) / (lineEndPt.x - lineStaPt.x);
            float b = lineStaPt.y - m * lineStaPt.x;
            return (Math.abs(point.y - (m * point.x + b)) < EPSILON);
        }
    }

    /**
     * 현재 사이드탭의 몇 번 인덱스가 선택되었는지 반환하는 함수
     *
     * @return - 선택된 항목의 인덱스 반환, 결과가 없으면 -1 반환
     */
    public int getSelectedIdx() {
        for (int i = 0; i < sideTabDataList.size(); i++) {
            if (sideTabDataList.get(i).isSelected()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 터치한 위치에 도형을 추가해주는 함수
     *
     * @param x    생성점의 x좌표
     * @param y    생성점의 y좌표
     * @param data 생성하기위해 지정된 데이터
     * @return 생성물의 id
     */
    public int createFigure(float x, float y, SideTabData data) {
        Figure figure = null;

        switch (data.getImageId()) {
            case R.drawable.ic_baseline_crop_square_24:
                figure = new Squre(new PointF(x, y), 150, 150);
                break;
            case R.drawable.ic_baseline_panorama_fish_eye_24_circle:
                figure = new Circle(new PointF(x, y), 50);
                break;
            case R.drawable.ic_baseline_border_color_24:
                figure = new Wall(new PointF(x, y));
                makeLine = true;
                break;
            default:
                break;
        }
        // 생성물에 id 부여
        if (figure != null) {
            figure.setFigureId(id++);
            curSelected = figure.getFigureId();
            figureList.add(figure);
            return figure.getFigureId();
        } else return -1;
    }

    public int getTotalArea() {
        float totalArea = 0;
        for (Figure f : figureList) {
            totalArea += f.getArea();
        }
        return (int) totalArea;
    }

    /**
     * 물체를 드래그 해서 움직이면 동작하는 함수
     * 물체의 위치정보를 변경시키고 다시 화면을 그림
     *
     * @param x        - 주어진 좌표
     * @param y        - 주어진 좌표
     * @param figureId 물체 식별자
     */
    private void movePolygon(float x, float y, int figureId) {
        float dx = x - preTouchPt.x;
        float dy = y - preTouchPt.y;
        Figure figure = figureList.stream().filter(f -> f.getFigureId() == figureId).findFirst().get();
        if(figure instanceof Wall){
            Wall wall = (Wall)figure;
            PointF preStart = wall.getStartPoint();
            PointF preEnd = wall.getEndPt();
            wall.setStartPoint(new PointF(preStart.x + dx, preStart.y + dy));
            wall.setEndPoint(new PointF(preEnd.x + dx, preEnd.y + dy));
        }
        else{
            PointF preSpawnPt = figure.getSpawnPoint();
            figure.setSpawnPoint(new PointF(preSpawnPt.x + dx, preSpawnPt.y + dy));
        }
        preTouchPt.x = x;
        preTouchPt.y = y;
    }

    // 현재 지정된 도형을 삭제함
    public void deleteFigure() {
        if (curSelected != -1) {
            figureList.remove(idToIdx(curSelected));
            curSelected = -1;
        }
        invalidate();
    }
}
