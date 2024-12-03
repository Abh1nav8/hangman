package com.example.hangman;

import androidx.room.RoomDatabase;
import androidx.room.Database;

@Database(entities = {ScoreboardRow.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ScoreboardDAO scoreboardDao();
}
