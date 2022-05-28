package com.penelope.seatforyou.ui.editor.draw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Region;
import android.view.MotionEvent;
import android.view.View;

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
    private ArrayList<Figure> figureList = new ArrayList<>();

    private Canvas canvas;

    public CanvasView(Context context) {
        super(context);

        init();
    }

    @Override
    protected void onDraw(Canvas canvas) {
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
        Figure.setClip(new Region(0, 0, getWidth(), getHeight()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                // 기존 생성물을 터치한건지 확인
//                for (int i = 0; i < figureList.size(); i++) {
//                    Region r = figureList.get(i).getRegion();
//                    if (r.contains((int) event.getX(), (int) event.getY())) {
//                        Toast.makeText(getContext(), "도형 터치 감지", Toast.LENGTH_SHORT).show();
//                        return super.onTouchEvent(event);
//                    }
//                }
                // 아니라면 새로 생성
                createFigure(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                break;
        }
        return super.onTouchEvent(event);
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
                break;
            }
        }
        this.invalidate();
    }

    public int getTotalArea(){
        float totalArea = 0;
        for(Figure f : figureList){
            totalArea += f.getArea();
        }
        return (int) totalArea;
    }

    /**
     * 물체를 드래그 해서 움직이면 동작하는 함수
     * 물체의 위치정보를 변경시키고 다시 화면을 그림
     *
     * @param x
     * @param y
     * @param listIndex 물체 식별자
     */
    private void movePolygon(float x, float y, int listIndex) {

    }
}
