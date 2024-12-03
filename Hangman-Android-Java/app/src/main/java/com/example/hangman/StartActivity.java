package com.example.hangman;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StartActivity extends AppCompatActivity {
    Handler fetchHandler;
    Handler sendHandler;
    Gson gson = new Gson();
    List<ScoreboardRow> objList;
    List<ScoreboardRow> combinedDBandServer;
    SharedPreferences sharedPref;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        sharedPref = getSharedPreferences("MyPref", 0);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        this.getSupportActionBar().setCustomView(R.layout.customactionbar);
        mButton = (Button) findViewById(R.id.loginButton);
        mButton.setText(sharedPref.getString("username", "login"));

        fetchHandler = new Handler();
        new Thread() {
            public void run() {
                JSONArray threadTemp = new JSONArray();
                objList = new ArrayList<>();

                try {
                    threadTemp = APIUsage.apiScoreboardFetch();
                    for (int i = 0; i < threadTemp.length(); i++) {
                        ScoreboardRow scoreboardRow = gson.fromJson(String.valueOf(threadTemp.getJSONObject(i)), ScoreboardRow.class);
                        scoreboardRow.setDatetime(); // converts unix timestamp to more human readable
                        objList.add(scoreboardRow);
                        String datetime = scoreboardRow.getDatetime();
                    }

                    // with the latest copy of the online db, store in local db
                    AppDatabase database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "mydb")
                            .fallbackToDestructiveMigration()
                            .build();
                    database.scoreboardDao().insertMultiple(objList);

                    combinedDBandServer = database.scoreboardDao().getAllRowsDB();
                    for (ScoreboardRow scoreboardRow : combinedDBandServer) {
                        APIUsage.apiSendScores(scoreboardRow);
                    }
                    Log.d("db", "successfully stored");
                } catch (Exception e) {
                    e.printStackTrace();
                    fetchHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    String.format("Failed to fetch latest scoreboard"),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();

    }

    public void onClickPlay(View view) {
        Intent myIntent = new Intent(getBaseContext(), ListSelectActivity.class);
        startActivity(myIntent);
    }

    public void onClickInstructions(View view) {
        //todo add popup with instructions, currently using for testing
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Instructions");
        alertDialogBuilder.setMessage("Welcome to Hangman! Guess letter by letter for 1 point, or guess the whole phrase and get more points the less letters you revealed - though be careful, get it wrong and its game over! Choose a list from the defaults, or download one from online. Get as many points as you can, and climb the leaderboard of that list!\n" +
                "\n" +
                "Go to playhangman.co.uk to upload your own, and learn about the project.");

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();


        //todo move this
        sendHandler = new Handler();
        new Thread() {
            public void run() {
                try {
                    for (ScoreboardRow scoreboardRow : combinedDBandServer) {
                        APIUsage.apiSendScores(scoreboardRow);
                    }
                    sendHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    String.format("Sent to server"),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    sendHandler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    String.format("Failed to send latest scoreboard"),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }

    public void login(View view) {
        ScoreKeeper.login(view, this, sharedPref);
    }
}
