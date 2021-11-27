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

import com.example.fahim.gremate.Adapters.ListAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.List;
import com.example.fahim.gremate.DataClasses.ListWithId;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class ListActivity extends NavDrawerActivity {

    String wsId = "";
    String mainListId = "";
    String wsTitle = "";
    String lastListId = "";

    RecyclerView listRecyclerView;
    LinearLayoutManager llm;
    ProgressBar loadList;

    ValueEventListener listener1;
    ValueEventListener listener2;
    DatabaseReference ref1;
    DatabaseReference ref2;

    ArrayList<ListWithId> lists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        setupNavDrawerClick();

        Bundle extras = getIntent().getExtras();

        if(extras == null) finish();


        wsId = extras.getString("wsId");
        mainListId = extras.getString("mainListId");
        wsTitle = extras.getString("wordSetTitle");

        ((TextView)findViewById(R.id.wordListTitle)).setText(wsTitle+ " lists: ");

        setTitle(Html.fromHtml("<font color='#BDCBDA'>GREMate</font>"));

        listRecyclerView = (RecyclerView)findViewById(R.id.rvWordList);
        listRecyclerView.setHasFixedSize(true);

        llm = new LinearLayoutManager(this);
        listRecyclerView.setLayoutManager(llm);

        loadList = (ProgressBar) findViewById(R.id.loadWordListRV);
        lists = new ArrayList<>();

        getLastListId();

        listRecyclerView.setVisibility(View.GONE);
        loadList.setVisibility(View.VISIBLE);

        setWordList();
        Log.d("ListActivity ", " onCreate");
    }

    public void getLastListId(){
        DBRef db = new DBRef();
        ref2 = db.lastListRef();
        listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    lastListId = (String) dataSnapshot.getValue();
                    if(lists!=null){
                        scrollToLastId();
                        listRecyclerView.setAdapter(new ListAdapter(lists, ListActivity.this, wsId, mainListId, lastListId));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError){ }
        };
        ref2.addValueEventListener(listener2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener1!=null) ref1.removeEventListener(listener1);
        if(listener2!=null) ref2.removeEventListener(listener2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.addList:

                final AlertDialog.Builder builder = new AlertDialog.Builder(ListActivity.this, R.style.AlertDialogTheme);
                builder.setTitle("Add list");

                final EditText input = new EditText(ListActivity.this);
                input.setHint("List name");
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listName = input.getText().toString();
                        if(listName.length()<2){
                            Toast.makeText(ListActivity.this,
                                    "Failed! Name must be at least 2 characters long.", Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(ListActivity.this,
                                    "List "+listName+" created", Toast.LENGTH_LONG).show();
                            DB.newList(wsId, listName);
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
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void setWordList(){
        DBRef db = new DBRef();

        ref1 = db.wordSetListsRef(wsId);

        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lists = new ArrayList<>();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    List wl = ds.getValue(List.class);
                    ListWithId wlId = new ListWithId(wl, ds.getKey());
                    lists.add(wlId);
                }
                Log.d("ListActivity ", "setWordList(), lists size = " + lists.size());
                Collections.reverse(lists);
                listRecyclerView.setAdapter(new ListAdapter(lists, ListActivity.this, wsId, mainListId, lastListId));
                scrollToLastId();
                listRecyclerView.setVisibility(View.VISIBLE);
                loadList.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);
    }

    private void setupNavDrawerClick(){
        NavigationView mNavigationView = (NavigationView)findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch(menuItem.getItemId()){
                    case R.id.nav_learn:
                        intent = new Intent(ListActivity.this, WordSetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        final AlertDialog dialog = new AlertDialog.Builder(ListActivity.this, R.style.AlertDialogTheme)
                                .setTitle("Confirm Sign Out")
                                .setMessage( "Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(ListActivity.this, LoginActivity.class);
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
                        intent = new Intent(ListActivity.this, SearchActivity.class);
                        ListActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(ListActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });
    }

    public void scrollToLastId(){
        if(lists!=null && lastListId!=null){
            int i = 0;
            for(ListWithId list: lists){
                if(list.getId().equals(lastListId)){
                    llm.scrollToPositionWithOffset((i==0)?i:i-1, 0);
                    return;
                }
                i++;
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(mDrawerLayout.isDrawerOpen(Gravity.START))
                mDrawerLayout.closeDrawer(Gravity.START);
            else {
               finish();
            }
        }
        return true;
    }
}
