package com.tiktok.paneli;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_login);
        Button hesapOlustur = findViewById(R.id.button);
        TextView girisYap = findViewById(R.id.girisYap);
        TextView nasilKullanilir = findViewById(R.id.nasilKullanilir);
        hesapOlustur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
                intent.putExtra("link","https://tiktokpaneli.com/signup");
                startActivity(intent);
                finish();
            }
        });

        girisYap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
                intent.putExtra("link","https://tiktokpaneli.com/");
                startActivity(intent);
                finish();
            }
        });

        nasilKullanilir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
                intent.putExtra("link","https://tiktokpaneli.com/help-me/");
                startActivity(intent);
                finish();
            }
        });
    }
}