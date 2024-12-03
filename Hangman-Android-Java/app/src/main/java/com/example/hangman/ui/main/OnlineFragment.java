package com.example.hangman.ui.main;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hangman.APIUsage;
import com.example.hangman.OnlineListRVAdapter;
import com.example.hangman.R;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class OnlineFragment extends Fragment {
    List<String> filenames = new ArrayList<>();
    List<String> localFilenames = new ArrayList<>();
    OnlineListRVAdapter adapter;
    Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_local, viewGroup, false);

        filenames = listOnlineFiles(); //gets list of filenames from /res/raw directory
        localFilenames = listRaw();

        // Lookup the recyclerview in activity layout
        RecyclerView recyclerViewLocal = RootView.findViewById(R.id.recyclerViewId);

        // Create adapter passing in the sample user data
        adapter = new OnlineListRVAdapter(filenames, getActivity());
        // Attach the adapter to the recyclerview to populate items
        recyclerViewLocal.setAdapter(adapter);
        // Set layout manager to position the items
        recyclerViewLocal.setLayoutManager(new LinearLayoutManager(getActivity()));


        return RootView;
    }

    public List<String> listOnlineFiles() {
        final List<String> temp = new ArrayList<>();
        handler = new Handler();
        new Thread() {
            public void run() {
                List<String> threadTemp = new ArrayList<>();
                try {
                    threadTemp = APIUsage.apiListsFetch();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(getContext(),
                                    String.format("Failed to fetch lists"),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                final List<String> finalThreadTemp = threadTemp;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < finalThreadTemp.size(); i++) {
                            if (!localFilenames.contains(finalThreadTemp.get(i))) {
                                temp.add(finalThreadTemp.get(i));
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }.start();
        return temp;
    }

    @Override
    public void onResume() { //refreshes local storage list, but not online todo add refresh button?
        super.onResume();
        localFilenames.clear();
        List<String> newList;
        newList = listRaw();
        localFilenames.addAll(newList);
        adapter.notifyDataSetChanged();
    }


    public List<String> listRaw() {
        List<String> temp = new ArrayList<>();
        File directory = getActivity().getFilesDir();
        File[] files = directory.listFiles();
        for (int count = 0; count < files.length; count++) {
            temp.add(files[count].getName().replace(".json", ""));
            Log.i("Downloaded list: ", files[count].getName());
        }
        if (temp.isEmpty()) {
            int listId = getResources().getIdentifier("raw/" + "defaultlist", null, getActivity().getPackageName());
            InputStream inputStream = getResources().openRawResource(listId);

            Path check = Paths.get(String.valueOf(getActivity().getFilesDir()) + String.format("/%s.json", "defaultlist"));
            Log.d("inthread", String.valueOf(check));
            try {
                Files.copy(inputStream, check, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return temp;
    }
}