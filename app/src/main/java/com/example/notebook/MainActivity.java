package com.example.notebook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Задержка в 2 секунды
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Переход на другую Activity
                startActivity(new Intent(MainActivity.this, SpisokActivity.class));
                finish(); // Закрыть текущую Activity
            }
        }, 2000); // 2000 миллисекунд = 2 секунды
    }
}