package com.project.gaim.ui;


import android.os.Bundle;
import android.view.View;
import android.widget.Button;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.project.gaim.R;

public class SubActivity extends AppCompatActivity {
    Button btn_LLH;
    Button btn_XYZ;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_notifications2);

        btn_LLH = findViewById(R.id.btn_XYZ);

        btn_LLH.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_XYZ = findViewById(R.id.btn_LLH);



    }
}


