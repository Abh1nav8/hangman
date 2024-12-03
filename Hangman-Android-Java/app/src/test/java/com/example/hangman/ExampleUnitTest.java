package com.example.hangman;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class ExampleUnitTest {
    @Mock
    Context mMockContext;

    @Test
    public void apiTest() throws IOException, JSONException {
        String APIURL = "http://192.168.1.175:2500/api/test.csv";
        URL url = new URL(APIURL);

        InputStream inputStream = url.openStream();
        Files.copy(inputStream, Paths.get("/test1.csv"), StandardCopyOption.REPLACE_EXISTING);
        //Log.d("URL", String.format(APIURL, 3));
        /*HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.181 Safari/537.36");

        con.connect();

        InputStreamReader streamreader = new InputStreamReader(con.getInputStream());
        BufferedReader reader = new BufferedReader(
                streamreader);

        StringBuffer json = new StringBuffer(1024);
        String tmp = "";
        while ((tmp = reader.readLine()) != null)
            json.append(tmp).append("\n");
        reader.close();
*/
        //JSONObject data = new JSONObject(json.toString());

        //assertTrue(data != null);


    }
}


