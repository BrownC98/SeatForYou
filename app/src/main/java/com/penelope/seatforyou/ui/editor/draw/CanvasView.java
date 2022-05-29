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

    private Canvas canvas;

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
    }

    // 최근에 터치한 도형의 번호, 맨바닥은 -1 로 표시
    private int curTouchedIdx = -1;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curTouchedIdx = touchedId(x, y);
                if (curTouchedIdx == -1){
                    createFigure(x, y);
                    Paint paint = figureList.get(figureList.size()-1).getPaint();
                    paint.setColor(Color.GREEN);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (curTouchedIdx != -1){
                    movePolygon(x, y, curTouchedIdx);
                    Paint paint = figureList.get(curTouchedIdx).getPaint();
                    paint.setColor(Color.GREEN);
                }
                break;
            case MotionEvent.ACTION_UP:
                if(curTouchedIdx != -1){
                    Paint paint = figureList.get(curTouchedIdx).getPaint();
                    paint.setColor(Color.BLACK);
                }
                curTouchedIdx = -1;
                break;
        }
        return true;
    }

    /**
     * 주어진 x, y 좌표가 현재 도형들의 내부인지 판별하는 함수
     *
     * @param x - 터치한 x 좌표
     * @param y - 터치한 y 좌표
     * @return - 도형 외부라면 -1 반환
     * - 도형 내부라면 해당 도형의 id를 반환
     */
    public int touchedId(float x, float y) {
        for (int i = figureList.size() - 1; i >= 0; i--) {
            Region r = figureList.get(i).getRegion();
            if (r.contains((int) x, (int) y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 터치한 위치에 도형을 추가해주는 함수
     *
     * @param x 생성점의 x좌표
     * @param y 생성점의 y좌표
     */
    public void createFigure(float x, float y) {
        // 사이드 탭에서 선택한 도형을 추가함
        List<SideTabData> dataList = EditorSideTabAdapter.getLocalDataSet();
        int i;
        Figure figure = null;

        for (i = 0; i < dataList.size(); i++) {
            if ((dataList.get(i)).isSelected()) {
                switch (dataList.get(i).getImageId()) {
                    case R.drawable.ic_baseline_crop_square_24:
                        figure = new Squre(new PointF(x, y), 500, 500);
                        break;
                    case R.drawable.ic_baseline_panorama_fish_eye_24_circle:
                        figure = new Circle(new PointF(x, y), 50);
                        break;
                    default:
                        break;
                }
                figureList.add(figure);
                curTouchedIdx = figureList.size() - 1;
                break;
            }
        }
        this.invalidate();
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
     * @param x         - 주어진 좌표
     * @param y         - 주어진 좌표
     * @param listIndex 물체 식별자
     */
    private void movePolygon(float x, float y, int listIndex) {
        Figure figure = figureList.get(listIndex);
        figure.setSpawnPoint(new PointF(x, y));
        this.invalidate();
    }
}
