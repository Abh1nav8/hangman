package com.example.hangman;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class APIUsage {
    public static void apiFetch(String listName, Context context) throws IOException {

        //todo build function for testing on home wifi
        String APIURL = "http://185.108.171.164:2500/api/%s.json";
        URL url = new URL(String.format(APIURL, listName.toLowerCase()));

        InputStream inputStream = url.openStream();
        Path check = Paths.get(String.valueOf(context.getFilesDir()) + String.format("/%s.json", listName));
        Log.d("inthread", String.valueOf(check));
        Files.copy(inputStream, check, StandardCopyOption.REPLACE_EXISTING);
        //todo add toast message for success/fail


    }

    public static List<String> apiListsFetch() throws JSONException, IOException {

        //todo build function for testing on home wifi
        String APIURL = "http://185.108.171.44:2500/api";
        URL url = new URL(APIURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        InputStreamReader streamreader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(
                streamreader);

        StringBuffer json = new StringBuffer(1024);
        String tmp = "";
        while ((tmp = reader.readLine()) != null)
            json.append(tmp).append("\n");
        reader.close();

        JSONArray data = new JSONArray(json.toString());

        List<String> temp = new ArrayList<>();
        for (int i = 0; i < data.length(); i++) {
            temp.add((String) data.get(i));
        }

        return temp;
        //todo add toast message for success/fail

    }

    public static JSONArray apiScoreboardFetch() throws JSONException, IOException {

        //todo build function for testing on home wifi
        String APIURL = "http://185.108.171.164:2500/api/scoreboarddb";
        URL url = new URL(APIURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();

        InputStreamReader streamreader = new InputStreamReader(connection.getInputStream());
        BufferedReader reader = new BufferedReader(
                streamreader);

        StringBuffer json = new StringBuffer(1024);
        String tmp = "";
        while ((tmp = reader.readLine()) != null)
            json.append(tmp).append("\n");
        reader.close();

        JSONObject tempJSON = new JSONObject(json.toString());
        JSONArray data = new JSONArray(tempJSON.getJSONArray("result").toString());


        return data;
        //todo add toast message for success/fail

    }

    public static void apiSendScores(ScoreboardRow scoreboardRow) throws JSONException, IOException {
        String params = String.format("list=%s&user=%s&score=%d&datetime=%s", scoreboardRow.getList(), scoreboardRow.getUser(), scoreboardRow.getScore(), scoreboardRow.getDatetime());

        String APIURL = "http://185.108.171.164:2500/api/scoreupload";
        URL url = new URL(APIURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset", "UTF-8");
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(15000);

        connection.connect();
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(params);
        wr.flush();
        wr.close();

        try { //get response from server
            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            Log.d("test", "result from server: " + result.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
