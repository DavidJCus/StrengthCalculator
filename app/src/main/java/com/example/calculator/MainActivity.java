package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static int userAge;
    public static int userGender;
    public static int userWeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("userStats", Context.MODE_PRIVATE);
        userAge = sp.getInt("age",userAge);
        userGender = sp.getInt("gender", userGender);
        userWeight = sp.getInt("weight", userWeight);
    }

    public void editProfile(View v){
        startActivity(new Intent(MainActivity.this, profilePage.class));
    }
}