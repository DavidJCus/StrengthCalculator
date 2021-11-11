package com.example.calculator;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.solver.state.State;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Layout;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sp;
    private int click = 0;
    public static int userAge;
    public static int userGender;
    public static int userWeight;

    public static int reps;
    public static int liftWeight;
    public static double userRepMax;

    public static String userReps;
    public static String userLiftWeight;
    public double[] percent = {0,1,.97,.94,.92,.89,.86,.83,.81,.78,.75,.73,.71,.70,.68,.67,
            .65,.64,.63,.61,.60,.59,.58,.57,.56,.55,.54,.53,.52,.51,.50};

    static int liftWeightChange = 0;
    static int liftRepChange = 0;

    EditText liftWeightInput;
    EditText repsInput;
    EditText userRepsInput;
    EditText userLiftWeightInput;
    TextView repMaxStatement;
    ConstraintLayout maxRepOutput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences("UserStats", Context.MODE_PRIVATE);

        liftWeightInput = (EditText)findViewById(R.id.exerciseWeightInput);
        repsInput = (EditText)findViewById(R.id.repsInput);
        repMaxStatement = (TextView)findViewById(R.id.repMaxStatement);
        maxRepOutput = (ConstraintLayout)findViewById(R.id.repMaxContainer);

        liftWeightInput.setOnEditorActionListener(new MainActivity.exerciseWeightInput());
        repsInput.setOnEditorActionListener(new MainActivity.exerciseRepsInput());

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
}