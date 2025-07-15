package com.example.newapp;

// TomatoActivity.java

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class TomatoActivity extends AppCompatActivity {

    private TextView tvTimer, tvStatus;
    private Button btnStart, btnPause, btnStop;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 1500000; // 25分钟
    private boolean timerRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tomato);

        tvTimer = findViewById(R.id.tvTimer);
        tvStatus = findViewById(R.id.tvStatus);
        btnStart = findViewById(R.id.btnStart);
        btnPause = findViewById(R.id.btnPause);
        btnStop = findViewById(R.id.btnStop);

        updateTimer();

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimer();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pauseTimer();
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });
    }

    private void startTimer() {
        if (!timerRunning) {
            countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftInMillis = millisUntilFinished;
                    updateTimer();
                }

                @Override
                public void onFinish() {
                    timerRunning = false;
                    tvStatus.setText("完成！");
                    btnStart.setText("开始");
                    btnStart.setVisibility(View.VISIBLE);
                    btnPause.setVisibility(View.GONE);
                }
            }.start();

            timerRunning = true;
            tvStatus.setText("正在学习...");
            btnStart.setVisibility(View.GONE);
            btnPause.setVisibility(View.VISIBLE);
        }
    }

    private void pauseTimer() {
        if (timerRunning) {
            countDownTimer.cancel();
            timerRunning = false;
            tvStatus.setText("已暂停");
            btnStart.setVisibility(View.VISIBLE);
            btnPause.setVisibility(View.GONE);
        }
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        timerRunning = false;
        timeLeftInMillis = 1500000;
        updateTimer();
        tvStatus.setText("准备开始");
        btnStart.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.GONE);
    }

    private void updateTimer() {
        int minutes = (int) (timeLeftInMillis / 1000) / 60;
        int seconds = (int) (timeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format("%02d:%02d", minutes, seconds);
        tvTimer.setText(timeLeftFormatted);
    }
}