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
import android.os.Handler;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
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
    public static int SQLuserWeight;

    public static int reps = 0;
    public static int liftWeight = 0;
    public static double userRepMax;

    public static String userReps;
    public static String userLiftWeight;
    public static int userExercise;
    public double[] percent = {0,1,.97,.94,.92,.89,.86,.83,.81,.78,.75,.73,.71,.70,.68,.67,
            .65,.64,.63,.61,.60,.59,.58,.57,.56,.55,.54,.53,.52,.51,.50};
    public double[] agePercent = {0.87,0.98,1,0.95,0.17,0.69,0.55,0.44};
    String[] exerciseChoices= {"Bench Press", "Deadlift", "Squat"};

    static int liftWeightChange; //removed initialization to zero
    static int liftRepChange;
    static int exerciseChange;

    static double beginner;
    static double novice;
    static double intermediate;
    static double advanced;
    static double elite;
    double strength;

    private ProgressBar progressBar;
    private int progressStatus = 0;
    private final Handler progressHandler = new Handler();

    EditText liftWeightInput;
    EditText repsInput;
    EditText userRepsInput;
    EditText userLiftWeightInput;
    TextView repMaxStatement;
    TextView strongerThan;
    ConstraintLayout maxRepOutput;
    ConstraintLayout strongerThanOutput;
    Spinner exerciseSpinner;
    ArrayAdapter<CharSequence> exercise;

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

        //connecting variables to the UI elements
        liftWeightInput = findViewById(R.id.exerciseWeightInput);
        repsInput = findViewById(R.id.repsInput);
        repMaxStatement = findViewById(R.id.repMaxStatement);
        maxRepOutput = findViewById(R.id.repMaxContainer);
        strongerThanOutput = findViewById(R.id.strongerThanContainer);
        strongerThan = findViewById(R.id.strongerThan);
        exerciseSpinner = findViewById(R.id.exercise);

        //adding listeners for the lift weight, reps, and exercise inputs
        liftWeightInput.setOnEditorActionListener(new MainActivity.exerciseWeightInput());
        repsInput.setOnEditorActionListener(new MainActivity.exerciseRepsInput());
        exerciseSpinner.setOnItemSelectedListener(new exerciseSelection());

        //focus change listeners so keyboard shows up on first tap of weight and rep inputs
        liftWeightInput.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                liftWeightInput.requestFocus();
                InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(liftWeightInput, 0);
                clearLiftWeightInput(v);
           }
        });

            repsInput.setOnFocusChangeListener((v, hasFocus) -> {
                if(hasFocus){
                    repsInput.requestFocus();
                    InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(repsInput, 0);
                    clearRepsInput(v);
                }
            });

        //exercise options
        exercise = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, exerciseChoices);
        exercise.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        exerciseSpinner.setAdapter(exercise);

    }

    public void editProfile(View v){ //accessed via Android:onClick in XML for Edit Stats button
        startActivity(new Intent(MainActivity.this, profilePage.class));
    }

    public void calculate(View v){ //called on when calculate button is clicked
        SharedPreferences.Editor editor = sp.edit();
        String gender;

        //loading values from stored shared preferences
        userAge = sp.getInt("age",1);
        userGender = sp.getInt("gender", 1);
        userWeight = sp.getInt("weight", 1);

        if (liftWeightChange == 0 || liftRepChange == 0){ //handling if nothing is initially given
            Context context = getApplicationContext();
            CharSequence text = "Please enter weight and reps";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        //getting user gender based on profile choice
        if (userGender == 0 ){
            gender = "Male";
        } else {
            gender = "Female";
        }

        String exercise = exerciseChoices[userExercise];

        if (exerciseChange == 1){
            exerciseSpinner.setSelection(userExercise);
        }

        if (liftWeightChange == 1){
            userLiftWeightInput = findViewById(R.id.exerciseWeightInput);
            userLiftWeight = userLiftWeightInput.getText().toString();

            if (userLiftWeight.equals("")){
                Context context = getApplicationContext();
                CharSequence text = "Please enter weight and reps";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            liftWeight = Integer.parseInt(userLiftWeight);

            liftWeightInput.setHint(""+ liftWeight);
            editor.putInt("liftWeight", liftWeight);
            editor.apply();
        }

        if (liftRepChange == 1){
            userRepsInput = findViewById(R.id.repsInput);
            userReps = userRepsInput.getText().toString();

            if (userReps.equals("")){
                Context context = getApplicationContext();
                CharSequence text = "Please enter weight and reps";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                return;
            }
            reps = Integer.parseInt(userReps);
            repsInput.setHint("" + reps);

            editor.putInt("liftReps", reps);
            editor.apply();
        }

        if (reps > 30){
            Context context = getApplicationContext();
            CharSequence text = "Reps must be 30 or less";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        //one rep max calculation container and stronger than output container reveled when calculate is first pressed
        if (click == 0 && liftWeightChange == 1 && liftRepChange == 1){
            maxRepOutput.setVisibility(View.VISIBLE);
            maxRepOutput.setAlpha(0.0f);
            maxRepOutput.animate()
                    .alpha(1.0f);
            click = 1;

            strongerThanOutput.setVisibility(View.VISIBLE);
            strongerThanOutput.setAlpha(0.0f);
            strongerThanOutput.animate()
                    .alpha(1.0f);
            click = 1;
        }

        userRepMax = Math.round(liftWeight / percent[reps]);
        String statement = "Your estimated one rep max is " + Math.round(userRepMax) +" lbs";
        repMaxStatement.setText(statement);
        SQLuserWeight = (userWeight / 10);

        //getting values for beginner to elite via SQL query
        if(connection!=null){
            Statement statement2;
            try{
                statement2 = connection.createStatement();
                String query = "SELECT * FROM " + gender + exercise + " WHERE Bodyweight LIKE '" + SQLuserWeight + "%' ";
                ResultSet resultSet = statement2.executeQuery(query);
                while(resultSet.next()) {
                    beginner = resultSet.getInt(2);
                    novice = resultSet.getInt(3);
                    intermediate = resultSet.getInt(4);
                    advanced = resultSet.getInt(5);
                    elite = resultSet.getInt(6);
                }
                //reduce standards based on age
                beginner = beginner * agePercent[userAge];
                novice = novice * agePercent[userAge];
                intermediate = intermediate * agePercent[userAge];
                advanced = advanced * agePercent[userAge];
                elite = elite * agePercent[userAge];

                double above;
                double below;
                double multiplier;
                int base;

                if (beginner <= userRepMax && userRepMax <= novice){
                    below = beginner;
                    above = novice;
                    base = 5;
                    multiplier = 15;
                } else if (novice < userRepMax && userRepMax <= intermediate){
                    below = novice;
                    above = intermediate;
                    base = 20;
                    multiplier = 30;
                } else if (intermediate < userRepMax && userRepMax <= advanced){
                    below = intermediate;
                    above = advanced;
                    base = 50;
                    multiplier = 30;
                } else if (advanced < userRepMax && userRepMax <= elite){
                    below = advanced;
                    above = elite;
                    base = 80;
                    multiplier = 15;
                } else {
                    strongerThan.setText(R.string.OnerepmaxTooLowText); //placeholder
                    CharSequence text = "error";
                    Context context = getApplicationContext();
                    if (userRepMax < beginner){
                        text = "One rep max too low";
                    } else if (userRepMax > elite){
                        text = "One rep max too high";
                        strongerThan.setText(R.string.userRepMaxTooHigh);
                    }
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }

                double range = above - below;
                double num = userRepMax - below;
                double percent = num / range;
                strength = (percent * multiplier) + base;
                strength = Math.round(strength);

                String output = "You are stronger than " + (int)strength + "% of lifters in your age and weight group";

                strongerThan.setText(output);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } else {
            strongerThan.setText(R.string.nullConnection);
        }

        progressBar = findViewById(R.id.progressBar);
        progressBar.setScaleY(4f);

        //progress bar animation
        new Thread(() -> {
            while (progressStatus != strength){
                if (progressStatus < strength){
                    progressStatus++;
                } else if (progressStatus > strength){
                    progressStatus--;
                }
                android.os.SystemClock.sleep(25);
                progressHandler.post(() -> progressBar.setProgress(progressStatus));
            }
        }).start();

    }

    //clearing the text inside weight input when it is clicked after the first time
    public void clearLiftWeightInput(View v){
        liftWeightInput.setHint("");
        liftWeightInput.getText().clear();
    }

    //clearing the text inside reps input when it is clicked after the first time
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

    static class exerciseRepsInput implements TextView.OnEditorActionListener{
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                MainActivity.liftRepChange = 1;
            }
            return false;
        }
    }

    static class exerciseSelection implements AdapterView.OnItemSelectedListener {

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