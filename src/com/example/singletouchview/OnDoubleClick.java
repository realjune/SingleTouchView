package com.example.singletouchview;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by jx on 2016/5/27.
 */
public class OnDoubleClick implements View.OnTouchListener {
    private int count;
    private long firClick;
    private long secClick;

    public OnDoubleClick(OnDoubleClicked mOnDoubleClicked) {
        setOnDoubleClicked(mOnDoubleClicked);
    }

    private OnDoubleClicked mOnDoubleClicked;

    public void setOnDoubleClicked(OnDoubleClicked mOnDoubleClicked) {
        this.mOnDoubleClicked = mOnDoubleClicked;
    }

    public static interface OnDoubleClicked {
        void onDoubleClicked(View v);
    }

    private long pressTime;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            pressTime = System.currentTimeMillis();
        } else if (MotionEvent.ACTION_UP == action) {
            if (System.currentTimeMillis() - pressTime > 500) {
                //not click
                return false;
            }
            if (System.currentTimeMillis() - firClick > 1000) {
                firClick = System.currentTimeMillis();
            } else {
                //double
                firClick = 0;
                if (mOnDoubleClicked != null) {
                    mOnDoubleClicked.onDoubleClicked(v);
                }
            }
        }
        return true;
    }

}
