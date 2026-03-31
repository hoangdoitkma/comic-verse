package com.example.comicversev1.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AutoScrollManager {

    public interface AutoScrollListener {
        void onScrollStateChanged(boolean isScrolling);
    }

    private final RecyclerView recyclerView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SharedPreferences prefs;
    private AutoScrollListener listener;

    private boolean isAutoScrolling = false;
    private boolean isPausedByNetwork = false;
    private boolean isPausedByTouch = false;

    private int scrollSpeed = 2; // pixels per 16ms
    private int conflictMode = 1; // 0: Dừng hẳn (A), 1: Tạm dừng (B)

    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoScrolling && !isPausedByNetwork && !isPausedByTouch && recyclerView != null) {
                recyclerView.scrollBy(0, scrollSpeed);
                handler.postDelayed(this, 16); // ~60 FPS
            }
        }
    };

    private final Runnable resumeRunnable = () -> {
        if (isAutoScrolling) {
            isPausedByTouch = false;
            startHandler();
        }
    };

    public AutoScrollManager(Context context, RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        this.prefs = context.getSharedPreferences("NovelReaderPrefs", Context.MODE_PRIVATE);
        loadSettings();
        setupTouchListener();
    }

    public void setListener(AutoScrollListener listener) {
        this.listener = listener;
    }

    public void loadSettings() {
        this.scrollSpeed = prefs.getInt("scroll_speed", 2);
        this.conflictMode = prefs.getInt("scroll_conflict_mode", 1);
    }

    public void applySettings(int speed, int conflictMode) {
        this.scrollSpeed = speed;
        this.conflictMode = conflictMode;
        prefs.edit()
             .putInt("scroll_speed", speed)
             .putInt("scroll_conflict_mode", conflictMode)
             .apply();
    }

    public void toggle(boolean enable) {
        this.isAutoScrolling = enable;
        if (enable) {
            isPausedByNetwork = false;
            isPausedByTouch = false;
            startHandler();
        } else {
            stopHandler();
        }
        if (listener != null) {
            listener.onScrollStateChanged(enable);
        }
    }

    private void startHandler() {
        handler.removeCallbacks(scrollRunnable);
        handler.post(scrollRunnable);
    }

    private void stopHandler() {
        handler.removeCallbacks(scrollRunnable);
        handler.removeCallbacks(resumeRunnable);
    }

    public void pauseForNetwork() {
        if (isAutoScrolling) {
            isPausedByNetwork = true;
        }
    }

    public void resumeFromNetwork() {
        if (isAutoScrolling && isPausedByNetwork) {
            isPausedByNetwork = false;
            startHandler();
        }
    }

    private void setupTouchListener() {
        recyclerView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                if (!isAutoScrolling) return false;

                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (conflictMode == 0) {
                            // Cách A: Dừng hẳn
                            toggle(false);
                            // Cần update UI nên đã gọi listener.onScrollStateChanged.
                        } else {
                            // Cách B: Tạm dừng thông minh (Smart pause)
                            isPausedByTouch = true;
                            stopHandler();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        if (isAutoScrolling && conflictMode == 1) {
                            // Schedule resume sau 2s
                            handler.removeCallbacks(resumeRunnable);
                            handler.postDelayed(resumeRunnable, 2000);
                        }
                        break;
                }
                return false; // Trả về false để các View con vẫn có thể bắt chạm (ví dụ nút bấn, hoặc vuốt)
            }
        });
    }

    public void destroy() {
        stopHandler();
        this.listener = null;
    }
    
    public boolean isAutoScrolling() {
        return isAutoScrolling;
    }
}
