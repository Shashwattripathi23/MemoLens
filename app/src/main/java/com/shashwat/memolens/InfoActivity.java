package com.shashwat.memolens;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class InfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        TextView privacyNote = findViewById(R.id.privacy_note);

        // Apply gradient to the text
        Shader textShader = new LinearGradient(
                0, 0, 0, privacyNote.getTextSize() * 10,  // Adjust this for more dramatic effect
                new int[]{
                        getResources().getColor(R.color.gradientStart),
                        getResources().getColor(R.color.gradientEnd)
                },
                null,
                Shader.TileMode.CLAMP
        );


        privacyNote.getPaint().setShader(textShader);


        ImageView backIcon = findViewById(R.id.back_icon);
        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to MainActivity
                Intent intent = new Intent(InfoActivity.this, MainActivity.class);
                startActivity(intent);
                finish(); // Optionally finish this activity if you don't want to keep it in the back stack
            }
        });
    }
}

