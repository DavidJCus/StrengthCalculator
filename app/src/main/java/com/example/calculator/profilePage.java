package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class profilePage extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    String[] ageChoices = { "14 – 17", "18 – 23", "24 – 39", "40 – 49", "50 – 59", "60 – 69", "70 – 79", "80 – 89"
        };

    String[] genderChoices = {"Male", "Female"};
    SharedPreferences sp;
    int ageChange = 0;
    static int genChange = 0;
    static int weightChange = 0;
    public static int userAge;
    public static int userGender;
    public static int userWeight;
    public static String userWeightInput;
    public EditText userInput;
    Spinner ageSpinner;
    Spinner genSpinner;
    EditText weightInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_page);

        ageChange = 0;
        weightChange = 0;
        genChange = 0;

        sp = getSharedPreferences("UserStats", Context.MODE_PRIVATE);

        ageSpinner = findViewById(R.id.age);
        genSpinner = findViewById(R.id.gender);
        weightInput = (EditText)findViewById(R.id.bodyweight);

        ageSpinner.setOnItemSelectedListener(this);
        genSpinner.setOnItemSelectedListener(new genderSelection());
        weightInput.setOnEditorActionListener(new weightChangeFunc());
        weightInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                clearWeightInput(v); //clear weight input when clicked, then OnClick works
            }
        });

        ArrayAdapter age = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ageChoices);
        ArrayAdapter gen = new ArrayAdapter(this, android.R.layout.simple_spinner_item, genderChoices);

        age.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        ageSpinner.setAdapter(age);
        genSpinner.setAdapter(gen);

        int loadAge = sp.getInt("age", -1);
        int loadGender = sp.getInt("gender", -1);
        int loadWeight = sp.getInt("weight", -1);

        ageSpinner.setSelection(loadAge);
        genSpinner.setSelection(loadGender);
        weightInput.setHint("" + loadWeight);
    }

    public void saveProfile(View view) {
        SharedPreferences.Editor editor = sp.edit();

        if(com.example.calculator.profilePage.weightChange == 1){
            userInput = (EditText)findViewById(R.id.bodyweight);
            userWeightInput = userInput.getText().toString();

            if (userWeightInput.equals("")){
                Context context = getApplicationContext();
                CharSequence text = "Please enter weight";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            userWeight = Integer.parseInt(userWeightInput);
            weightInput.setHint("" + userWeight);
            editor.putInt("weight", userWeight);
            editor.apply();
        }

        if(genChange == 1){
            genSpinner.setSelection(userGender);
            editor.putInt("gender", userGender);
            editor.apply();
        }

        if(ageChange == 1){
            editor.putInt("age", userAge);
            editor.apply();
            ageSpinner.setSelection(userAge);
        }

        Context context = getApplicationContext();
        CharSequence text = "Profile saved";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        int loadWeight = sp.getInt("weight", -1);
        weightInput.getText().clear();
        weightInput.setHint("" + loadWeight);

        ageChange = 0;
        weightChange = 0;
        genChange = 0;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        userAge = position;
        ageChange = 1;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        ageChange = 0;
    }

    static class genderSelection implements AdapterView.OnItemSelectedListener{
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            userGender = position;
            genChange = 1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            genChange = 0;
        }
    }


    static class weightChangeFunc implements TextView.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event){
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                com.example.calculator.profilePage.weightChange = 1;
            }
            return false;
        }
    }

    public void clearWeightInput(View v){
        weightInput.setHint("");
        weightInput.getText().clear();
    }

}