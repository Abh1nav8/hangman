package com.example.hangman.ui.main;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hangman.ListRVAdapter;
import com.example.hangman.ListSelectActivity;
import com.example.hangman.R;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class LocalFragment extends Fragment {
    List<String> filenames = new ArrayList<>();
    ListRVAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View RootView = inflater.inflate(R.layout.fragment_local, viewGroup, false);
        filenames = listRaw(); //gets list of filenames from /res/raw directory

        // Lookup the recyclerview in activity layout
        RecyclerView recyclerViewLocal = RootView.findViewById(R.id.recyclerViewId);

        // Create adapter passing in the sample user data
        adapter = new ListRVAdapter(filenames, getActivity());
        // Attach the adapter to the recyclerview to populate items
        recyclerViewLocal.setAdapter(adapter);
        // Set layout manager to position the items
        recyclerViewLocal.setLayoutManager(new LinearLayoutManager(getActivity()));

        return RootView;
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

    @Override
    public void onResume() {
        super.onResume();
        filenames.clear();
        List<String> newList;
        newList = listRaw();
        filenames.addAll(newList);
        adapter.notifyDataSetChanged();
    }
}