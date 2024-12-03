package com.example.hangman;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineListRVAdapter extends RecyclerView.Adapter<OnlineListRVAdapter.ViewHolder> {
    Handler handler;
    Map<String, Integer> tableMap = new HashMap<>();

    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView listnameTextView;
        public Button downloadListButton;
        public Button scoreboardButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            listnameTextView = (TextView) itemView.findViewById(R.id.listNameRow);
            downloadListButton = (Button) itemView.findViewById(R.id.downloadListButton);
            scoreboardButton = (Button) itemView.findViewById(R.id.scoreboardButton);


        }
    }

    // Store a member variable for the contacts
    private List<String> mGoalStringNameList;
    private Context mContext;

    // Pass in the list array into the constructor
    public OnlineListRVAdapter(List<String> goalStringNameList, Context context) {
        mGoalStringNameList = goalStringNameList;
        mContext = context;
    }

    //Store list of name selected
    String goalStringListName;

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public OnlineListRVAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        tableMap.put("user1", R.id.user1);
        tableMap.put("user2", R.id.user2);
        tableMap.put("user3", R.id.user3);
        tableMap.put("user4", R.id.user4);
        tableMap.put("user5", R.id.user5);
        tableMap.put("score1", R.id.score1);
        tableMap.put("score2", R.id.score2);
        tableMap.put("score3", R.id.score3);
        tableMap.put("score4", R.id.score4);
        tableMap.put("score5", R.id.score5);
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.online_rv_row, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(contactView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(OnlineListRVAdapter.ViewHolder viewHolder, final int position) {
        // Get the data model based on position
        goalStringListName = mGoalStringNameList.get(position);

        // Set item views based on your views and data model
        TextView textView = viewHolder.listnameTextView;
        Button downloadListButton = viewHolder.downloadListButton;
        Button scoreboardButton = viewHolder.scoreboardButton;

        //Set properties of those views
        textView.setText(goalStringListName);

        //Creates onclicklistener for each list row, taking the list name and using to fetch and download data
        View.OnClickListener downloadListOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String selectedList = mGoalStringNameList.get(position);
                handler = new Handler();
                new Thread() {
                    public void run() {
                        try {
                            APIUsage.apiFetch(selectedList, mContext);
                            handler.post(new Runnable() {
                                public void run() {
                                    mGoalStringNameList.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mGoalStringNameList.size());
                                    Toast.makeText(mContext,
                                            String.format("%s downloaded", selectedList),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(mContext,
                                            String.format("Failed to download %s", selectedList),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }.start();
            }
        };

        View.OnClickListener scoreboardOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                //todo intent to scoreboard page
                final String selectedList = mGoalStringNameList.get(position);


                final Handler handler = new Handler();
                new Thread() {
                    public void run() {
                        try {
                            AppDatabase database = Room.databaseBuilder(mContext, AppDatabase.class, "mydb")
                                    .fallbackToDestructiveMigration()
                                    .build();
                            final List<ScoreboardDAO.scoreboardTuple> scoreboardTupleList = database.scoreboardDao().getListScoreboard(selectedList);

                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(mContext,
                                            String.format("Fetched scoreboard"),
                                            Toast.LENGTH_LONG).show();
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                                    ViewGroup viewGroup = (ViewGroup) v.findViewById(android.R.id.content);
                                    View dialogView = LayoutInflater.from(v.getContext()).inflate(R.layout.scoreboard_dialog, viewGroup, false);


                                    for (int i = 1; i <= 5; i++) {
                                        int check = tableMap.get(String.format("user%d", i));
                                        TextView user = dialogView.findViewById(tableMap.get(String.format("user%d", i)));
                                        user.setText("");
                                        TextView score = dialogView.findViewById(tableMap.get(String.format("score%d", i)));
                                        score.setText("");
                                        if (scoreboardTupleList.size() == 0) {
                                            user.setText("No scores");
                                            break;
                                        }
                                        if (i <= scoreboardTupleList.size()) {
                                            user.setText(scoreboardTupleList.get(i - 1).user);
                                            score.setText(Integer.toString(scoreboardTupleList.get(i - 1).highscore));
                                        }
                                    }

                                    alertDialogBuilder.setView(dialogView);

                                    AlertDialog alertDialog = alertDialogBuilder.create();
                                    alertDialog.show();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(mContext,
                                            String.format("Failed to fetch latest scoreboard"),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                }.start();


            }
        };

        downloadListButton.setOnClickListener(downloadListOnClickListener);
        scoreboardButton.setOnClickListener(scoreboardOnClickListener);
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mGoalStringNameList.size();
    }


}
