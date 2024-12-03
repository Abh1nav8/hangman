package com.example.hangman;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.util.Log;
import android.util.Property;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.room.Room;

import java.util.List;

public class ScoreKeeper implements Parcelable {
    private String user;
    private String list;
    private int currentScore;
    private int highScore;

    public ScoreKeeper(String user, String list) {
        this.user = user;
        this.list = list;
        this.currentScore = 0;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setList(String list) {
        this.list = list;
    }

    public String getList() {
        return list;
    }

    public int getCurrentScore() {
        return currentScore;
    }

    public void setCurrentScore(int currentScore) {
        this.currentScore = currentScore;
    }

    public void increaseScore(int points) {
        currentScore = currentScore + points;
    }

    public void updateHighScore(final Context context) {
        if (currentScore >= highScore) {
            highScore = currentScore;
            final ScoreboardRow scoreboardRow = new ScoreboardRow(System.currentTimeMillis() / 1000L, list, user, highScore);
            new Thread() {
                public void run() {
                    try {
                        // with the latest copy of the online db, store in local db
                        AppDatabase database = Room.databaseBuilder(context, AppDatabase.class, "mydb")
                                .fallbackToDestructiveMigration()
                                .build();
                        database.scoreboardDao().insertSingle(scoreboardRow);

                        List<ScoreboardRow> tester = database.scoreboardDao().getAllRowsDB();
                        Log.d("db", "successfully stored");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    // Parcelable stuff
    //write object values to parcel for storage
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user);
        dest.writeString(list);
        dest.writeInt(currentScore);
        dest.writeInt(highScore);
    }

    //constructor used for parcel
    public ScoreKeeper(Parcel parcel) {
        user = parcel.readString();
        list = parcel.readString();
        currentScore = parcel.readInt();
        highScore = parcel.readInt();
    }

    //creator - used when un-parceling our parcel (creating the object)
    public static final Parcelable.Creator<ScoreKeeper> CREATOR = new Parcelable.Creator<ScoreKeeper>() {

        @Override
        public ScoreKeeper createFromParcel(Parcel parcel) {
            return new ScoreKeeper(parcel);
        }

        @Override
        public ScoreKeeper[] newArray(int size) {
            return new ScoreKeeper[0];
        }
    };

    //return hashcode of object
    public int describeContents() {
        return hashCode();
    }

    public static void login(final View view, final Context context, final SharedPreferences sharedPref) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Login");

        LinearLayout dialog = new LinearLayout(context);
        dialog.setOrientation(LinearLayout.VERTICAL);

        final EditText username = new EditText(context);
        username.setInputType(InputType.TYPE_CLASS_TEXT);
        username.setHint("Username");
        dialog.addView(username);

        builder.setView(dialog);


        builder.setPositiveButton("Login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("username", username.getText().toString().trim());
                editor.commit();
                Button mButton = (Button) view.findViewById(R.id.loginButton);
                mButton.setText(username.getText().toString().trim());
            }
        });
        builder.show();
    }
}
