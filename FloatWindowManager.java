package com.example.touchrecorder;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class FloatWindowManager {
    private WindowManager windowManager;
    private View floatView;
    private Context context;
    private boolean isShowing = false;

    private Button btnRecord;
    private Button btnPlay;
    private TextView tvStatus;

    public FloatWindowManager(Context ctx) {
        context = ctx.getApplicationContext();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    public void showFloatWindow() {
        if (isShowing) return;

        floatView = LayoutInflater.from(context).inflate(R.layout.float_window, null);
        btnRecord = floatView.findViewById(R.id.btn_record_float);
        btnPlay = floatView.findViewById(R.id.btn_play_float);
        tvStatus = floatView.findViewById(R.id.tv_status_float);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 100;
        params.y = 200;

        floatView.setOnTouchListener(new View.OnTouchListener() {
            private int lastX, lastY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        params.x += dx;
                        params.y += dy;
                        windowManager.updateViewLayout(floatView, params);
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                        return true;
                }
                return false;
            }
        });

        btnRecord.setOnClickListener(v -> handleRecordClick());
        btnPlay.setOnClickListener(v -> handlePlayClick());

        windowManager.addView(floatView, params);
        isShowing = true;
        updateUI(StateManager.getInstance().getCurrentState());
    }

    private void handleRecordClick() {
        TouchAccessibilityService service = TouchAccessibilityService.getInstance();
        if (service == null) return;

        if (StateManager.getInstance().getCurrentState() == StateManager.STATE_RECORDING) {
            service.stopRecording();
        } else {
            service.startRecording();
        }
    }

    private void handlePlayClick() {
        TouchAccessibilityService service = TouchAccessibilityService.getInstance();
        if (service == null) return;

        if (StateManager.getInstance().getCurrentState() == StateManager.STATE_PLAYING) {
            service.stopPlayback();
        } else {
            service.startPlayback();
        }
    }

    public void updateUI(int state) {
        if (!isShowing || floatView == null) return;

        boolean isLoop = StateManager.getInstance().isLoopPlayback();
        switch (state) {
            case StateManager.STATE_IDLE:
                btnRecord.setText("开始录制");
                btnPlay.setText("模拟执行");
                tvStatus.setText(isLoop ? "空闲(循环)" : "空闲中");
                btnPlay.setEnabled(true);
                btnRecord.setEnabled(true);
                break;
            case StateManager.STATE_RECORDING:
                btnRecord.setText("结束录制");
                btnPlay.setText("模拟执行");
                tvStatus.setText("录制中...");
                btnPlay.setEnabled(false);
                break;
            case StateManager.STATE_PLAYING:
                btnRecord.setText("开始录制");
                btnPlay.setText("结束模拟");
                tvStatus.setText(isLoop ? "循环操作中..." : "正在操作...");
                btnRecord.setEnabled(false);
                break;
        }
    }

    public void hideFloatWindow() {
        if (isShowing && floatView != null) {
            windowManager.removeView(floatView);
            isShowing = false;
        }
    }
}
