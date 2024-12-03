package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScoreboardDAO {
    @Query("SELECT * FROM scoreboard")
    public List<ScoreboardRow> getAllRowsDB();

    @Query("SELECT user, MAX(score) as highscore FROM scoreboard WHERE list LIKE :list GROUP BY 1 ORDER BY MAX(score) desc LIMIT 10")
    public List<scoreboardTuple> getListScoreboard(String list);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertMultiple(List<ScoreboardRow> scoreboardList);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertSingle(ScoreboardRow scoreboardList);

    public class scoreboardTuple {
        @ColumnInfo(name = "user")
        public String user;

        @ColumnInfo(name = "highscore")
        @NonNull
        public int highscore;
    }
}
