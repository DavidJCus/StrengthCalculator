package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class profilePage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] ageChoices = { "14 – 17", "18 – 23", "24 – 39", "40 – 49", "50 – 59", "60 – 69", "70 – 79", "80 – 89"
        };

    String[] genderChoices = {"Male", "Female"};
    SharedPreferences sp;
    public static int userAge;
    public static int userGender;
    public static int userWeight;
    Spinner ageSpinner;
    Spinner genSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        sp = getSharedPreferences("UserStats", Context.MODE_PRIVATE);

        ageSpinner = findViewById(R.id.age);
        genSpinner = findViewById(R.id.gender);

        ageSpinner.setOnItemSelectedListener(this);
        genSpinner.setOnItemSelectedListener(new genderSelection());

        ArrayAdapter age = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ageChoices);
        ArrayAdapter gen = new ArrayAdapter(this, android.R.layout.simple_spinner_item, genderChoices);

        age.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ageSpinner.setAdapter(age);
        genSpinner.setAdapter(gen);
        int loadAge = sp.getInt("age", -1);
        int loadGender = sp.getInt("gender", -1);
        ageSpinner.setSelection(loadAge);
        genSpinner.setSelection(loadGender);
    }

    public void saveProfile(View v) {
        Context context = getApplicationContext();
        CharSequence text = "Profile saved";
        int duration = Toast.LENGTH_SHORT;

        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("gender", userGender);
        editor.apply();

        editor.putInt("age", userAge);
        editor.apply();

        ageSpinner.setSelection(userAge);
        genSpinner.setSelection(userGender);

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        userAge = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class genderSelection implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            userGender = position;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public static int returnAge(){
        return userAge;
    }
    public static int returnGender() {
        return userGender;
    }

}