package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.example.fahim.gremate.Adapters.WordAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.ListWithId;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordWithId;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;

public class ListWordsActivity extends NavDrawerActivity {

    private String wsId;
    private String mainListId;
    private String currentListId;
    private String currentListName;

    private ArrayList<ListWithId> otherLists;
    private ArrayList<WordWithId> words;

    private RecyclerView wordsInListRV;
    private LinearLayoutManager llm;

    private DatabaseReference ref1;
    private ValueEventListener listener1;

    private WordAdapter rvAdapter;
    private ProgressBar loadWordRV;

    private int sortOrder;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_words);

        setupNavDrawerClick();
        getListSortOrder();

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();

        otherLists = extras.getParcelableArrayList("otherLists");
        currentListId = extras.getString("listId");
        wsId = extras.getString("wsId");
        mainListId = extras.getString("mainListId");
        currentListName = extras.getString("listTitle");

        setTitle(Html.fromHtml("<font color='#BDCBDA'>"+currentListName.toUpperCase()+"</font>"));

        wordsInListRV = (RecyclerView) findViewById(R.id.wordInListRV);
        wordsInListRV.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        wordsInListRV.setLayoutManager(llm);

        loadWordRV = (ProgressBar) findViewById(R.id.loadWordRV);

        hideWordRv();
        Log.d("ListWordsActivityLOG ", " onCreate");
        getListWords();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(listener1!=null && ref1!=null) ref1.addValueEventListener(listener1);
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeListeners();
        setListState();
    }

    public void addButtonClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Add Word");

        final EditText input = new EditText(ListWordsActivity.this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Word");
        builder.setView(input);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String word = input.getText().toString();
                if (word.length() < 1) {
                    Toast.makeText(ListWordsActivity.this,
                            "Failed! Word must be at least 1 character long.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(ListWordsActivity.this,
                            word + " added", Toast.LENGTH_SHORT).show();
                    DB.newWord(wsId, currentListId, currentListName, mainListId, word);
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
    }

    public void searchButtonClick(View v) {
        if (words == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Search Word");

        final EditText input = new EditText(ListWordsActivity.this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Word");
        builder.setView(input);

        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                char[] word = input.getText().toString().toLowerCase().toCharArray();
                for (int i = 0; i < words.size(); i++) {
                    char[] listWord = words.get(i).getValue().toLowerCase().toCharArray();
                    if (listWord.length < word.length) continue;
                    boolean match = true;
                    for (int j = 0; j < word.length; j++) {
                        if (word[j] != listWord[j]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        llm.scrollToPositionWithOffset(i, 0);
                        return;
                    }
                }
                Toast.makeText(ListWordsActivity.this,
                        input.getText().toString() + " was not found in this list!", Toast.LENGTH_SHORT).show();
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
    }

    public void sortButtonClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this, R.style.AlertDialogTheme);
        LayoutInflater inflater = (ListWordsActivity.this).getLayoutInflater();

        builder.setTitle("Sort Words");
        final View layout = inflater.inflate(R.layout.ws_sort, null);
        final RadioButton alph = (RadioButton) layout.findViewById(R.id.alphabetical);
        final RadioButton time = (RadioButton) layout.findViewById(R.id.timeAdded);
        final RadioButton diff = (RadioButton) layout.findViewById(R.id.difficulty);

        final RadioButton asc = (RadioButton) layout.findViewById(R.id.ascending);
        final RadioButton dsc = (RadioButton) layout.findViewById(R.id.descending);

        if (sortOrder == 21) {
            time.setChecked(true);
            asc.setChecked(true);
        } else if (sortOrder == 22) {
            time.setChecked(true);
            dsc.setChecked(true);
        } else if (sortOrder == 31) {
            alph.setChecked(true);
            asc.setChecked(true);
        } else if (sortOrder == 32) {
            alph.setChecked(true);
            dsc.setChecked(true);
        } else if (sortOrder == 41) {
            diff.setChecked(true);
            asc.setChecked(true);
        } else if (sortOrder == 42) {
            diff.setChecked(true);
            dsc.setChecked(true);
        }

        final int prevSortOrder = sortOrder;

        builder.setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (time.isChecked() && asc.isChecked()) {
                            sortOrder = 21;
                        } else if (time.isChecked() && dsc.isChecked()) {
                            sortOrder = 22;
                        } else if (alph.isChecked() && asc.isChecked()) {
                            sortOrder = 31;
                        } else if (alph.isChecked() && dsc.isChecked()) {
                            sortOrder = 32;
                        } else if (diff.isChecked() && asc.isChecked()) {
                            sortOrder = 41;
                        } else if (diff.isChecked() && dsc.isChecked()) {
                            sortOrder = 42;
                        }
                        if (prevSortOrder == sortOrder) return;
                        setListSortOrder();
                        sortWords(true);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

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
    }

    public void practiceButtonClick(View v) {
        Intent intent = new Intent(ListWordsActivity.this, PracticeActivity.class);
        Bundle b = new Bundle();
        b.putString("wsId", wsId);
        b.putString("listId", currentListId);
        intent.putExtras(b);
        startActivity(intent);
    }

    public void detailsButtonClick(View v) {
        if (words == null) return;
        int total = 0, practicable = 0, easy = 0, normal = 0, hard = 0, veryHard = 0;
        for (WordWithId word : words) {
            total++;
            if (word.isPracticable()) {
                practicable++;
                switch (word.getLevel()) {
                    case Word.LVL_EASY:
                        easy++;
                        break;
                    case Word.LVL_NORMAL:
                        normal++;
                        break;
                    case Word.LVL_HARD:
                        hard++;
                        break;
                    case Word.LVL_VHARD:
                        veryHard++;
                }
            }
        }
        String text = "Total words: " + total + "\n" +
                "Practicable: " + practicable + "\n" +
                "Easy: " + easy + "\n" +
                "Normal: " + normal + "\n" +
                "Hard: " + hard + "\n" +
                "Very Hard: " + veryHard + "\n";

        AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Details");

        final TextView dataTV = new TextView(ListWordsActivity.this);
        dataTV.setTextSize(20);
        dataTV.setText(text);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dataTV.setLayoutParams(llp);
        dataTV.setPadding(30, 30, 30, 30);
        builder.setView(dataTV);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
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
    }

    public void saveButtonClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ListWordsActivity.this, R.style.AlertDialogTheme);
        builder.setTitle("Save List");

        final TextView dataTV = new TextView(ListWordsActivity.this);
        dataTV.setTextSize(20);
        dataTV.setText("This will save the current list in GREMate folder of your device.");
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dataTV.setLayoutParams(llp);
        dataTV.setPadding(30, 30, 30, 30);
        builder.setView(dataTV);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    int permission = ActivityCompat.checkSelfPermission(ListWordsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (permission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                ListWordsActivity.this,
                                PERMISSIONS_STORAGE,
                                REQUEST_EXTERNAL_STORAGE
                        );
                    } else{
                        saveToStorage();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(ListWordsActivity.this, "Error...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

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
    }

    public void saveToStorage() throws Exception {
        if(words==null)throw new Exception("words is null");
        File root = new File(Environment.getExternalStorageDirectory(), "GREMate");
        if (!root.exists()) {
            root.mkdirs();
        }
        File listFile = new File(root, currentListName + ".txt");
        FileWriter writer = new FileWriter(listFile);
        StringBuilder output = new StringBuilder();
        for(WordWithId word: words){
            output.append(word.getValue()).append("\n");
        }
        writer.append(output);
        writer.flush();
        writer.close();
        Toast.makeText(ListWordsActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        saveToStorage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    }

    private void removeListeners() {
        Log.d("ListWordsActivityLOG ", "REF REMOVED");
        if (listener1 != null) ref1.removeEventListener(listener1);
    }

    public void getListWords() {
        DBRef db = new DBRef();
        ref1 =  db.listWordsRef(currentListId);
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                words = new ArrayList<>();
                int serial = 0;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Word word = ds.getValue(Word.class);
                    WordWithId wordWithId = new WordWithId(word, ds.getKey(), ++serial);
                    words.add(wordWithId);
                }
                sortWords(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);
    }

    private void sortWords(boolean resetList) {

        getListSortOrder();

        if (words == null) return;
        else if (sortOrder == 21){
            Collections.sort(words, WordWithId.addedTime_Asc);
        }
        else if (sortOrder == 22) {
            Collections.sort(words, WordWithId.addedTime_Dsc);
        } else if (sortOrder == 31) {
            Collections.sort(words, WordWithId.alphabetical_Asc);
        } else if (sortOrder == 32) {
            Collections.sort(words, WordWithId.alphabetical_Dsc);
        } else if (sortOrder == 41) {
            Collections.sort(words, WordWithId.difficulty_Asc);
        } else if (sortOrder == 42) {
            Collections.sort(words, WordWithId.difficulty_Dsc);
        }

        if (resetList) resetListState();
        else if (rvAdapter != null) setListState();

        rvAdapter = new WordAdapter(words, otherLists, ListWordsActivity.this, wsId, mainListId, currentListId);
        wordsInListRV.setAdapter(rvAdapter);
        restoreListState();

        showWordRv();
    }

    private void setListSortOrder() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferences.edit().putInt(currentListId + "`~", sortOrder).apply();
    }

    private void getListSortOrder() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortOrder = prefs.getInt(currentListId + "`~", 21);
    }

    private void resetListState() {
        if (wordsInListRV == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        preferences.edit().putInt(currentListId, 0).apply();
        preferences.edit().putInt(currentListId + "~~", 0).apply();
    }

    private void setListState() {
        if (wordsInListRV == null) return;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        View firstChild = wordsInListRV.getChildAt(0);
        if (firstChild == null) return;
        int firstVisiblePosition = wordsInListRV.getChildAdapterPosition(firstChild);
        int offset = firstChild.getTop();

        preferences.edit().putInt(currentListId, firstVisiblePosition).apply();
        preferences.edit().putInt(currentListId + "~~", offset).apply();
        setListSortOrder();

    }

    private void restoreListState() {
        if (wordsInListRV == null) return;
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        wordsInListRV.scrollToPosition(preferences.getInt(currentListId, 0));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                wordsInListRV.scrollBy(0, -preferences.getInt(currentListId + "~~", 0));
            }
        }, 5);
    }

    private void hideWordRv() {
        wordsInListRV.setVisibility(View.GONE);
        loadWordRV.setVisibility(View.VISIBLE);
    }

    private void showWordRv() {
        loadWordRV.setVisibility(View.GONE);
        wordsInListRV.setVisibility(View.VISIBLE);
    }

    private void setupNavDrawerClick() {
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.nav_learn:
                        intent = new Intent(ListWordsActivity.this, WordSetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        final AlertDialog dialog = new AlertDialog.Builder(ListWordsActivity.this, R.style.AlertDialogTheme)
                                .setTitle("Confirm Sign Out")
                                .setMessage("Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(ListWordsActivity.this, LoginActivity.class);
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
                        intent = new Intent(ListWordsActivity.this, SearchActivity.class);
                        ListWordsActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(ListWordsActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });
    }
}