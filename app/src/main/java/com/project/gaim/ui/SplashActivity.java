package com.project.gaim.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Thread.sleep(1500);
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}



//import androidx.appcompat.app.AppCompatActivity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//
//public class SplashActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        moveMain(3);	//1초 후 main activity 로 넘어감
//    }
//
//    private void moveMain(int sec) {
//        new Handler().postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                //new Intent(현재 context, 이동할 activity)
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//
//                startActivity(intent);	//intent 에 명시된 액티비티로 이동
//
//                finish();	//현재 액티비티 종료
//            }
//        }, 1000 * sec); // sec초 정도 딜레이를 준 후 시작
//    }
//}