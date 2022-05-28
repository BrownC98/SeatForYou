package com.penelope.seatforyou.ui.editor.draw;

import static java.lang.Math.abs;

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
    private double touch_interval_X = 0; // X 터치 간격
    private double touch_interval_Y = 0; // Y 터치 간격
    private int zoom_in_count = 0; // 줌 인 카운트
    private int zoom_out_count = 0; // 줌 아웃 카운트
    private int touch_zoom = 0; // 줌 크기

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: // 싱글터치
                createFigure(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE: // 터치 후 이동 시
                float x = event.getX();
                float y = event.getY();
                if(event.getPointerCount() == 2) {
                    double now_interval_X = (double) abs(event.getX(0) - event.getX(1)); // 두 손가락 X좌표 차이 절대값
                    double now_interval_Y = (double) abs(event.getY(0) - event.getY(1)); // 두 손가락 Y좌표 차이 절대값
                    if (touch_interval_X < now_interval_X && touch_interval_Y < now_interval_Y) {
                        // 이전 값과 비교, 확대기능에 대한 코드 정의 TODO: 김태환
                        zoom_in_count++;
                        if (zoom_in_count > 5) {
                            // 카운트를 세는 이유 : 너무 많은 호출을 줄이기 위해
                            zoom_in_count = 0;
                            touch_zoom += 5;
                        }
                    }
                    if (touch_interval_X > now_interval_X && touch_interval_Y > now_interval_Y) {
                        // 축소 기능에 대한 코드 정의
                        zoom_out_count++;
                        if (zoom_out_count > 5) {
                            zoom_out_count = 0;

                            touch_zoom -= 10;
                            // TODO: 김태환
                        }
                    }
                    touch_interval_X = (double) abs(event.getX(0) - event.getX(1));
                    touch_interval_Y = (double) abs(event.getY(0) - event.getY(1));

                }
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
