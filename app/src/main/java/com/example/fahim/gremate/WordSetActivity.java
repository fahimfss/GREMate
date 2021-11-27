package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.Adapters.WordSetAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.UserData;
import com.example.fahim.gremate.DataClasses.WordSet;
import com.example.fahim.gremate.DataClasses.WordSetWithId;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WordSetActivity extends NavDrawerActivity {

    private ArrayList<WordSetWithId> wordSets;
    private RecyclerView wsRecyclerView;
    private LinearLayoutManager llm;

    private ProgressBar loadWordSet;

    String lastSetId = "";

    private TextView wordSetTitle;

    DatabaseReference ref1;
    DatabaseReference ref2;
    ValueEventListener listener1;
    ValueEventListener listener2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_set);

        setupNavDrawerClick();

        wordSetTitle = (TextView) findViewById(R.id.wordSetTitle);

        setWsTitle();
        setTitle(Html.fromHtml("<font color='#BDCBDA'>GREMate</font>"));

        wsRecyclerView = (RecyclerView)findViewById(R.id.rvWordSet);
        wsRecyclerView.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        wsRecyclerView.setLayoutManager(llm);

        loadWordSet = (ProgressBar) findViewById(R.id.loadWordSetRV);

        getLastSetId();

        wsRecyclerView.setVisibility(View.GONE);
        loadWordSet.setVisibility(View.VISIBLE);

        setWordSet();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener1!=null) ref1.removeEventListener(listener1);
        if(listener2!=null) ref2.removeEventListener(listener2);

    }

    // For highlighting last opened word set
    public void getLastSetId(){
        DBRef db = new DBRef();
        ref2 = db.lastWordSetRef();
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    lastSetId = (String) dataSnapshot.getValue();
                    if(wordSets!=null){
                        wsRecyclerView.setAdapter(new WordSetAdapter(wordSets, WordSetActivity.this, lastSetId));
                        scrollToLastId();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError){ }
        };
        ref2.addValueEventListener(listener2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_word_set, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addSet:
                AlertDialog.Builder builder = new AlertDialog.Builder(WordSetActivity.this, R.style.AlertDialogTheme);
                builder.setTitle("WORD SET NAME");

                final EditText input = new EditText(WordSetActivity.this);

                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String wsName = input.getText().toString();
                        if(wsName.length()<1){
                            Toast.makeText(WordSetActivity.this, "Failed! Name must be at least 1 character long.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            DB.newWordSet(wsName);
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                final AlertDialog dialog = builder.create();

                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if ((dialog.findViewById(android.R.id.message)) != null) {
                            ((TextView)dialog.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                    }
                });

                dialog.show();


                return true;

            case R.id.sync:
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("UserWords").child(userId);
                ref.keepSynced(true);

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setWsTitle(){
        DBRef db = new DBRef();
        DatabaseReference mRef = db.userDataRef();
        mRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserData user = dataSnapshot.getValue(UserData.class);
                try {
                    wordSetTitle.setText(user.getUserName() + "'s Word Sets");
                }
                catch(NullPointerException e){
                    wordSetTitle.setText("User Word Sets");
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void setWordSet(){
        if(listener1 != null){
            ref1.removeEventListener(listener1);
        }
        DBRef db = new DBRef();
        ref1 = db.wordSetRef(); 
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordSets = new ArrayList<>();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    WordSet wordSet = ds.getValue(WordSet.class);
                    String id = ds.getKey();
                    WordSetWithId wordSetWithId = new WordSetWithId(wordSet, id);
                    wordSets.add(wordSetWithId);
                }
                Log.d("WordSetActivity ", "setWordSet(), wordSets size = " + wordSets.size());

                Collections.sort(wordSets, new Comparator<WordSetWithId>() {
                    @Override
                    public int compare(WordSetWithId wordSetWithId, WordSetWithId t1) {
                        return wordSetWithId.getName().compareTo(t1.getName());
                    }
                });
                wsRecyclerView.setAdapter(new WordSetAdapter(wordSets, WordSetActivity.this, lastSetId));
                scrollToLastId();
                wsRecyclerView.setVisibility(View.VISIBLE);
                loadWordSet.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError){ }
        };
        ref1.addValueEventListener(listener1);
    }

    public void scrollToLastId(){
        if(wordSets!=null && lastSetId!=null){
            int i = 0;
            for(WordSetWithId ws: wordSets){
                if(ws.getId().equals(lastSetId)){
                    llm.scrollToPositionWithOffset((i==0)?i:i-1, 0);
                    return;
                }
                i++;
            }
        }
    }

    private void setupNavDrawerClick(){
        NavigationView mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch(menuItem.getItemId()){
                    case R.id.nav_learn:
                        intent = new Intent(WordSetActivity.this, WordSetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        final AlertDialog dialog = new AlertDialog.Builder(WordSetActivity.this, R.style.AlertDialogTheme)
                                .setTitle("Confirm Sign Out")
                                .setMessage( "Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(WordSetActivity.this, LoginActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                }).create();

                        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                            @Override
                            public void onShow(DialogInterface arg0) {
                                if ((dialog.findViewById(android.R.id.message)) != null) {
                                    ((TextView)dialog.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                                }
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                            }
                        });

                        dialog.show();

                        break;
                    case R.id.nav_search:
                        intent = new Intent(WordSetActivity.this, SearchActivity.class);
                        WordSetActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(WordSetActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawerLayout.isDrawerOpen(Gravity.START))
                mDrawerLayout.closeDrawer(Gravity.START);
            else {
                final AlertDialog dialog = new AlertDialog.Builder(WordSetActivity.this, R.style.AlertDialogTheme)
                        .setTitle("Close")
                        .setMessage("Are you sure you want to close GREMate?")
                        .setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finishAffinity();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).create();

                dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if ((dialog.findViewById(android.R.id.message)) != null) {
                            ((TextView)dialog.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                        }
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                    }
                });

                dialog.show();
            }
        }
        return true;
    }
}
