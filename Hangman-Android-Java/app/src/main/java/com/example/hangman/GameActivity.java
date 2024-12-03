package com.example.hangman;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.opencsv.CSVReader;

import org.json.JSONArray;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameActivity extends AppCompatActivity {
    public Map<String, Integer> imageMap = new HashMap<>();
    ImageView mainImage;
    int stage = 1;
    final int guessesAllowed = 11;
    float percentGuessed;
    NumberPicker letterPicker;
    String[] pickerLetters;
    String[] guessedLetters;
    TextView guessedLettersDisplay;
    TextView goalStringDisplay;
    TextView currentScoreDisplay;
    Button guessButton;
    Button restartButton;
    Button attemptButton;
    GoalString goalString;
    int points = 0;
    ListObject goalStringList;
    ScoreKeeper scoreKeeper;
    SharedPreferences sharedPref;
    Button mButton;
    Handler fetchHandler;
    Gson gson = new Gson();
    List<ScoreboardRow> objList;
    List<ScoreboardRow> combinedDBandServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("MyPref", 0);

        this.getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        this.getSupportActionBar().setCustomView(R.layout.customactionbar);
        mButton = (Button) findViewById(R.id.loginButton);
        mButton.setText(sharedPref.getString("username", "login"));


        mainImage = findViewById(R.id.mainImage);
        guessedLettersDisplay = findViewById(R.id.guessedLetters);
        goalStringDisplay = findViewById(R.id.goalStringDisplay);
        currentScoreDisplay = findViewById(R.id.currentScoreView);
        guessButton = findViewById(R.id.guessButton);
        restartButton = findViewById(R.id.restartButton);
        attemptButton = findViewById(R.id.attemptButton);
        for (int i = 1; i <= 11; i++) {
            String imageName = String.format("stage%d", i);
            int imageId = getResources().getIdentifier(imageName, "drawable", getApplicationInfo().packageName);
            imageMap.put(imageName, imageId);
        }
        // Get scorekeeper
        scoreKeeper = getIntent().getParcelableExtra("scoreKeeper");
        scoreKeeper.setUser(sharedPref.getString("username", "anonymous"));

        //Hardcoded list selection for now todo list selector
        String chosenList = getIntent().getStringExtra("listName");
        goalStringList = loadList(chosenList);
        initialiseGame(goalStringList);
    }

    public void initialiseGame(ListObject goalStringList) {
        // Set initial stage of game
        stage = 1;
        mainImage.setImageResource(imageMap.get(String.format("stage%d", stage)));

        guessButton.setVisibility(View.VISIBLE);
        restartButton.setVisibility(View.INVISIBLE);
        attemptButton.setEnabled(true);
        ArrayList<String> gameStrings = new ArrayList<>(Arrays.asList(goalStringList.list));
        goalString = selectFromList(gameStrings);
        pickerLetters = new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"};
        guessedLetters = new String[26];
        updateGuessedDisplay(guessedLetters);
        updatePicker(guessedLetters);

        percentGuessed = 0;
    }

    private GoalString selectFromList(ArrayList<String> gameStrings) {
        //Randomly selects goalString from within the loaded list
        if (gameStrings.isEmpty()) {
            //todo toast message, prompt for another list?
        }
        int random = new Random().nextInt(gameStrings.size());
        goalString = new GoalString(gameStrings.get(random));
        goalStringDisplay.setText(goalString.getClosedString());
        gameStrings.remove(random); //remove from session list so the word is not replayed
        return goalString;
    }

    private ListObject loadList(String chosenList) {
        ListObject object = null;
        Gson gson = new Gson();
        try {
            Log.d("openjson", String.format("%s/%s.json", getFilesDir(), chosenList));
            object = gson.fromJson(new FileReader(String.format("%s/%s.json", getFilesDir(), chosenList)), ListObject.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }


    public void onClickStep(View view) {
        String selectedLetter = pickerLetters[letterPicker.getValue()];
        boolean guessMatch = checkGuess(selectedLetter, goalString);
        if (guessMatch && goalString.getClosedString().equalsIgnoreCase(goalString.getOpenString())) {
            winRound(1);
        } else if (!guessMatch) {
            stage++;
            if (stage <= guessesAllowed) {
                mainImage.setImageResource(imageMap.get("stage" + stage));

            } else {
                gameOver();
                return;
            }
        }
        for (int i = 0; i < guessedLetters.length; i++) {
            if (guessedLetters[i] == null) {
                guessedLetters[i] = selectedLetter;
                break;
            }
        }
        updateGuessedDisplay(guessedLetters);
        updatePicker(guessedLetters);
    }

    private void winRound(int pointsWon) {
        scoreKeeper.increaseScore(pointsWon); //todo add more points for guesses
        scoreKeeper.updateHighScore(getApplicationContext());
        scoreKeeper.getCurrentScore();


        currentScoreDisplay.setText(Integer.toString(scoreKeeper.getCurrentScore()));

        mainImage.setImageResource(R.drawable.win);
        guessButton.setVisibility(View.INVISIBLE);
        restartButton.setText("Continue");
        restartButton.setVisibility(View.VISIBLE);
        attemptButton.setEnabled(false);

        fetchHandler = new Handler();
        new Thread() {
            public void run() {
                JSONArray threadTemp = new JSONArray();
                objList = new ArrayList<>();

                try {
                    /*threadTemp = APIUsage.apiScoreboardFetch();
                    for (int i = 0; i < threadTemp.length(); i++) {
                        ScoreboardRow scoreboardRow = gson.fromJson(String.valueOf(threadTemp.getJSONObject(i)), ScoreboardRow.class);
                        scoreboardRow.setDatetime(); // converts unix timestamp to more human readable
                        objList.add(scoreboardRow);
                        String datetime = scoreboardRow.getDatetime();
                    }*/

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

    private void gameOver() {
        scoreKeeper.updateHighScore(getApplicationContext());
        scoreKeeper.setCurrentScore(0);
        currentScoreDisplay.setText(Integer.toString(scoreKeeper.getCurrentScore()));

        goalStringDisplay.setText(goalString.getOpenString());
        mainImage.setImageResource(R.drawable.gameover);
        guessButton.setVisibility(View.INVISIBLE);
        restartButton.setText("Restart");
        restartButton.setVisibility(View.VISIBLE);
        attemptButton.setEnabled(false);
    }

    public void updatePicker(String[] guessedLetters) {
        List<String> temp = new LinkedList<String>(Arrays.asList(pickerLetters));
        int removedLetterCount = 0;
        for (int i = 0; i < pickerLetters.length; i++) {
            for (int j = 0; j < guessedLetters.length; j++) {
                if (guessedLetters[j] == null) {
                    break;
                } else if (guessedLetters[j].equalsIgnoreCase(pickerLetters[i])) {
                    temp.remove(i - removedLetterCount); //removes a letter from the pickerletters as it has been guessed, shifting index backwards to account for removed letters
                    removedLetterCount++;
                    break;
                }
            }
        }
        pickerLetters = new String[temp.size()];
        for (int i = 0; i < temp.size(); i++) {
            pickerLetters[i] = temp.get(i);
        }

        letterPicker = findViewById(R.id.letterWheel);
        letterPicker.setDisplayedValues(null);
        letterPicker.setMinValue(0);
        letterPicker.setMaxValue(pickerLetters.length - 1);
        letterPicker.setDisplayedValues(pickerLetters);
    }

    public void updateGuessedDisplay(String[] guessedLetters) {
        String display = "";
        for (String letter : guessedLetters) {
            if (letter != null) {
                display = display + letter + "\n";
            }
        }
        if (display.equals("")) {
            guessedLettersDisplay.setText(" ");
        } else {
            guessedLettersDisplay.setText(display);
        }
    }

    public boolean checkGuess(String guess, GoalString goalString) {
        char guessChar = guess.charAt(0);
        boolean match = false;
        String goalStringOpen = goalString.getOpenString();
        String goalStringClosed = goalString.getClosedString();
        String goalStringCurrent = "";

        for (int i = 0; i < goalStringOpen.length(); i++) {
            if (Character.toLowerCase(goalStringOpen.charAt(i)) == guessChar) {
                match = true;
                goalStringCurrent = goalStringCurrent + goalStringOpen.charAt(i);
            } else {
                goalStringCurrent = goalStringCurrent + goalStringClosed.charAt(i);
            }
        }
        goalString.setClosedString(goalStringCurrent); //updates with revealed string
        goalStringDisplay.setText(goalStringCurrent);

        percentGuessed = goalString.getPercentage(goalString);
        return match;
    }

    public void restartGame(View view) {
        initialiseGame(goalStringList);
    }

    public void login(View view) {
        ScoreKeeper.login(view, this, sharedPref);
    }

    public void attemptGuess(View view) {
        final int pointsToGain;
        if (percentGuessed < 0.33) {
            pointsToGain = 3;
        } else if (percentGuessed < 0.66) {
            pointsToGain = 2;
        } else {
            pointsToGain = 1;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Full Guess");
        if (pointsToGain == 0) {
            builder.setMessage("Get the full phrase for 1 point");
        } else {
            builder.setMessage(String.format("Get the full phrase for %d points", pointsToGain));
        }


        final EditText guessAttempt = new EditText(this);
        guessAttempt.setInputType(InputType.TYPE_CLASS_TEXT);
        guessAttempt.setHint("Phrase");
        builder.setView(guessAttempt);

        builder.setPositiveButton("Guess", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String answerLower = goalString.getOpenString().toLowerCase();
                String guessLower = guessAttempt.getText().toString().toLowerCase();
                if (answerLower.equals(guessLower)) {
                    winRound(pointsToGain);
                } else {
                    gameOver();
                }
            }
        });
        builder.show();
    }
}
