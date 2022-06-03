package com.penelope.seatforyou.data.editor.assets;

import android.graphics.PointF;

public class Table extends Squre{

    private int member; // 테이블의 최대 인원수

    public Table(PointF spawnPoint, float width, float height, int member) {
        super(spawnPoint, width, height);
    }

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }
}
