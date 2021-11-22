package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Spinner;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MainActivity extends AppCompatActivity {
    private static final String ip = "192.168.1.231";
    private static final String port = "1433";
    private static final String database = "LiftData";
    private static final String JClass = "net.sourceforge.jtds.jdbc.Driver";
    private static final String username = "test1";
    private static final String password = "test1";
    private static final String url ="jdbc:jtds:sqlserver://"+ip+":"+port+"/"+database;
    private Connection connection = null;

    SharedPreferences sp;
    private int click = 0;
    public static int userAge;
    public static int userGender;
    public static int userWeight;

    public static int reps;
    public static int liftWeight;
    public static double userRepMax;
    public static double userRepMaxWithAge;

    public static String userReps;
    public static String userLiftWeight;
    public static int userExercise;
    public double[] percent = {0,1,.97,.94,.92,.89,.86,.83,.81,.78,.75,.73,.71,.70,.68,.67,
            .65,.64,.63,.61,.60,.59,.58,.57,.56,.55,.54,.53,.52,.51,.50};
    public double[] agePercent = {0.87,0.98,1,0.95,0.17,0.69,0.55,0.44};
    String[] exerciseChoices= {"Benchpress", "Deadlift", "Squat"};

    static int liftWeightChange = 0;
    static int liftRepChange = 0;
    static int exerciseChange = 0;

    EditText liftWeightInput;
    EditText repsInput;
    EditText userRepsInput;
    EditText userLiftWeightInput;
    TextView repMaxStatement;
    TextView strongerThan;
    ConstraintLayout maxRepOutput;
    Spinner exerciseSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            Class.forName(JClass);
            connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        sp = getSharedPreferences("UserStats", Context.MODE_PRIVATE);

        liftWeightInput = (EditText)findViewById(R.id.exerciseWeightInput);
        repsInput = (EditText)findViewById(R.id.repsInput);
        repMaxStatement = (TextView)findViewById(R.id.repMaxStatement);
        maxRepOutput = (ConstraintLayout)findViewById(R.id.repMaxContainer);
        strongerThan = (TextView)findViewById(R.id.strongerThan);
        exerciseSpinner = findViewById(R.id.exercise);


        liftWeightInput.setOnEditorActionListener(new MainActivity.exerciseWeightInput());
        repsInput.setOnEditorActionListener(new MainActivity.exerciseRepsInput());
        exerciseSpinner.setOnItemSelectedListener(new exerciseSelection());

        ArrayAdapter exercise = new ArrayAdapter(this, android.R.layout.simple_spinner_item, exerciseChoices);
        exercise.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(exercise);

        SharedPreferences sp = getApplicationContext().getSharedPreferences("userStats", Context.MODE_PRIVATE);
        userAge = sp.getInt("age",userAge);
        userGender = sp.getInt("gender", userGender);
        userWeight = sp.getInt("weight", userWeight);

    }

    public void editProfile(View v){
        startActivity(new Intent(MainActivity.this, profilePage.class));
    }

    public void calculate(View v){
        SharedPreferences.Editor editor = sp.edit();

        if (exerciseChange == 1){
            exerciseSpinner.setSelection(userExercise);
        }

        if (liftWeightChange == 1){
            userLiftWeightInput = (EditText)findViewById(R.id.exerciseWeightInput);
            userLiftWeight = userLiftWeightInput.getText().toString();
            liftWeight = Integer.parseInt(userLiftWeight);

            liftWeightInput.setHint(""+ liftWeight);
            editor.putInt("liftWeight", liftWeight);
            editor.apply();
            liftWeightChange = 0;
        }

        if (liftRepChange == 1){
            userRepsInput = (EditText)findViewById(R.id.repsInput);
            userReps = userRepsInput.getText().toString();
            reps = Integer.parseInt(userReps);

            repsInput.setHint("" + liftWeight);
            editor.putInt("liftReps", reps);
            editor.apply();
            liftRepChange = 0;
        }

        if (reps > 30){
            Context context = getApplicationContext();
            CharSequence text = "Reps must be 30 or less";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        if (click == 0){
            maxRepOutput.setVisibility(View.VISIBLE);
            maxRepOutput.setAlpha(0.0f);
            maxRepOutput.animate()
                    .alpha(1.0f);
            click = 1;
        }

        userRepMax = Math.round(liftWeight / percent[reps]);
        String statement = "Your estimated one rep max is " + Math.round(userRepMax) +" lbs";
        repMaxStatement.setText(statement);

        if(connection!=null){
            Statement statement2;
            try{
                statement2 = connection.createStatement();
                ResultSet resultSet = statement2.executeQuery("SELECT Beginner FROM MaleSquat WHERE BodyWeight = 150");//placeholder SQL statements, will use vars
                while(resultSet.next()) {
                    int result = resultSet.getInt(1);
                    strongerThan.setText("SQL RESULT: " + result); //placeholder
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            strongerThan.setText("Null Connection");
        }

        userRepMaxWithAge = userRepMax * agePercent[userAge];

    }

    public void clearLiftWeightInput(View v){
        liftWeightInput.setHint("");
        liftWeightInput.getText().clear();
    }

    public void clearRepsInput(View v){
        repsInput.setHint("");
        repsInput.getText().clear();
    }

    static class exerciseWeightInput implements TextView.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                MainActivity.liftWeightChange = 1;
            }
            return false;
        }
    }

    static class exerciseRepsInput implements  TextView.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                MainActivity.liftRepChange = 1;
            }
            return false;
        }
    }

    static class exerciseSelection implements AdapterView.OnItemSelectedListener{

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            userExercise = position;
            exerciseChange = 1;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}