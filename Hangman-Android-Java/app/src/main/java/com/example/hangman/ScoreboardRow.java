package com.example.hangman;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;

@Entity(tableName = "scoreboard")
public class ScoreboardRow {
    @PrimaryKey
    @NonNull
    private String datetime;
    private long unixDatetime;
    private String list;
    private String user;
    private int score;

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setUnixDatetime(long unixDatetime) {
        this.unixDatetime = unixDatetime;
    }

    public void setList(String list) {
        this.list = list;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public ScoreboardRow(Long unixDatetime, String list, String user, int score) {
        this.unixDatetime = unixDatetime;
        this.list = list;
        this.user = user;
        this.score = score;

        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.datetime = jdf.format(unixDatetime * 1000L);
    }

    public void setDatetime() {
        SimpleDateFormat jdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        datetime = jdf.format(unixDatetime * 1000L);
    }

    public String getDatetime() {
        return datetime;
    }

    public long getUnixDatetime() {
        return unixDatetime;
    }

    public String getList() {
        return list;
    }

    public String getUser() {
        return user;
    }

    public int getScore() {
        return score;
    }
}
