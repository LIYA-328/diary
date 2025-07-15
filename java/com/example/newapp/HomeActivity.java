package com.example.newapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private Button btnTimer, btnNotes;
    private String username;
    private Button btnAccountBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // 获取用户名
        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Log.w(TAG, "用户名未传递，使用默认值");
            username = "默认用户";
        }

        // 初始化按钮 - 添加空检查
        btnTimer = findViewById(R.id.btnTimer);
        btnNotes = findViewById(R.id.btnNotes);
        // 初始化记账本按钮
        btnAccountBook = findViewById(R.id.btnAccountBook);
        btnAccountBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, AccountBookActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        if (btnTimer == null) {
            Log.e(TAG, "番茄钟按钮初始化失败");
            Toast.makeText(this, "界面初始化错误", Toast.LENGTH_SHORT).show();
        } else {
            // 设置番茄钟按钮点击事件
            btnTimer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "番茄钟按钮被点击");
                    try {
                        Intent intent = new Intent(HomeActivity.this, TomatoActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "启动番茄钟失败", e);
                        Toast.makeText(HomeActivity.this,
                                "无法打开番茄钟: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        if (btnNotes == null) {
            Log.e(TAG, "日记本按钮初始化失败");
        } else {
            // 设置日记本按钮点击事件
            btnNotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "日记本按钮被点击");
                    try {
                        Intent intent = new Intent(HomeActivity.this, NotesActivity.class);
                        intent.putExtra("USERNAME", username);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "启动日记本失败", e);
                        Toast.makeText(HomeActivity.this,
                                "无法打开日记本: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}