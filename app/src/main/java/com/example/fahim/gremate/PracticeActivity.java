package com.example.fahim.gremate;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.Word;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static android.view.View.GONE;

public class PracticeActivity extends NavDrawerActivity {

    private String wsId;
    private String listId;

    private Spinner wsSpinner;
    private Spinner listSpinner;

    private ProgressBar wsPB;
    private ProgressBar listPB;

    private DatabaseReference ref1;
    private DatabaseReference ref2;
    private DatabaseReference ref3;

    private ValueEventListener listener1;
    private ValueEventListener listener2;
    private ValueEventListener listener3;

    private ArrayList<NameIdPair> wordSets;
    private ArrayList<NameIdPair> wordLists;

    private ArrayList<Word> words;
    private ArrayList<Word> tempWords;

    private AppCompatButton loadButton;

    private HashMap<String, ArrayList<NameIdPair>> wordSetListMap;
    private HashMap<String, ArrayList<Word>> listWordMap;

    private ProgressBar loadPracPB;

    private LinearLayout ll3;

    private boolean []selectedFlags;

    private boolean reload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        selectedFlags =  new boolean[]{true, true, true, true};

        setupNavDrawerClick();

        setTitle(Html.fromHtml("<font color='#BDCBDA'>Practice</font>"));

        Bundle b = getIntent().getExtras();
        if (b != null) {
            wsId = b.getString("wsId");
            listId = b.getString("listId");
        }

        ll3 = (LinearLayout) findViewById(R.id.ll3);

        wsSpinner = (Spinner) findViewById(R.id.wsSpinner);
        listSpinner = (Spinner) findViewById(R.id.listSpinner);

        wsPB = (ProgressBar) findViewById(R.id.wsLoading);
        listPB = (ProgressBar) findViewById(R.id.listLoading);

        loadButton = (AppCompatButton) findViewById(R.id.loadPracBtn);
        loadPracPB = (ProgressBar) findViewById(R.id.pracLoading);

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wsSpinner.setEnabled(false);
                listSpinner.setEnabled(false);
                loadPracPB.setVisibility(View.VISIBLE);
                ll3.setVisibility(GONE);
                getWords();
            }
        });

        findViewById(R.id.startCompPracBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (tempWords.size() == 0) {
                            Toast.makeText(PracticeActivity.this, "No words selected!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        reload = true;

                        Intent intent = new Intent(PracticeActivity.this, PracticingActivity.class);
                        intent.putParcelableArrayListExtra("words", tempWords);
                        intent.putExtra("type", "complete");
                        startActivity(intent);
                    }
                });

        findViewById(R.id.startShortPracBtn)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (words.size() == 0) {
                            Toast.makeText(PracticeActivity.this, "No words selected!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        reload = true;

                        Intent intent = new Intent(PracticeActivity.this, PracticingActivity.class);
                        intent.putParcelableArrayListExtra("words", tempWords);
                        intent.putExtra("type", "short");
                        startActivity(intent);
                    }
                });

        wsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                wsId = wordSets.get(i).getId();
                listSpinner.setVisibility(GONE);
                listPB.setVisibility(View.VISIBLE);
                getWordList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        listSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                listId = wordLists.get(i).getId();
                ll3.setVisibility(GONE);
                loadPracPB.setVisibility(View.VISIBLE);
                getWords();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        reload = true;

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(reload) {

            ll3.setVisibility(GONE);

            wsSpinner.setVisibility(GONE);
            listSpinner.setVisibility(GONE);

            wsPB.setVisibility(View.VISIBLE);
            listPB.setVisibility(View.VISIBLE);

            loadButton.setVisibility(GONE);
            loadPracPB.setVisibility(GONE);

            wordSetListMap = new HashMap<>();
            listWordMap = new HashMap<>();

            getWordSet();

            reload = false;
        }

        if(checkPreviousPractice()){
            findViewById(R.id.startResumePracBtn)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (words.size() == 0) {
                                Toast.makeText(PracticeActivity.this, "No words selected!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            reload = true;

                            Intent intent = new Intent(PracticeActivity.this, PracticingActivity.class);
                            intent.putExtra("type", "resume");
                            startActivity(intent);
                        }
                    });

            findViewById(R.id.startResumePracBtn).setVisibility(View.VISIBLE);
        }
        else {
            findViewById(R.id.startResumePracBtn).setVisibility(GONE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(listener1!=null) ref1.removeEventListener(listener1);
        if(listener2!=null) ref2.removeEventListener(listener2);
        if(listener3!=null) ref3.removeEventListener(listener3);
    }

    private void getWordSet() {
        if (listener1 != null) ref1.removeEventListener(listener1);

        DBRef db = new DBRef();
        ref1 = db.wordSetRef();
        listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordSets = new ArrayList<>();
                int i = 0, wsIndex = -1;
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String wsName = ds.child("name").getValue().toString();
                    String id = ds.getKey();
                    wordSets.add(new NameIdPair(wsName, id));
                    if (wsId != null && id.equals(wsId)) wsIndex = i;
                    i++;
                }
                Collections.reverse(wordSets);

                ArrayAdapter<String> wsArrayAdapter = new ArrayAdapter<>(
                        PracticeActivity.this, R.layout.spinner_item, getNames(wordSets));
                wsArrayAdapter.setDropDownViewResource(R.layout.spinner_item);

                wsSpinner.setAdapter(wsArrayAdapter);
                if (wsIndex != -1) wsSpinner.setSelection(wordSets.size() - 1 - wsIndex, false);
                else if(wordSets.size()>0)wsId = wordSets.get(0).getId();

                wsSpinner.setVisibility(View.VISIBLE);
                wsPB.setVisibility(GONE);

                ref1.removeEventListener(listener1);
                getWordList();
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        ref1.addValueEventListener(listener1);
    }

    private void getWordList() {
        if (listener2 != null) {
            ref2.removeEventListener(listener2);
        }
        DBRef db = new DBRef();
        if (wordSetListMap.containsKey(wsId)) {
            wordLists = wordSetListMap.get(wsId);
            wordListPostProcess();
        } else {
            ref2 = db.wordSetListsRef(wsId);
            listener2 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<NameIdPair> _wordLists = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String lsName = ds.child("name").getValue().toString();
                        String id = ds.getKey();
                        _wordLists.add(new NameIdPair(lsName, id));
                    }
                    wordSetListMap.put(wsId, _wordLists);
                    wordLists = _wordLists;

                    ref2.removeEventListener(listener2);
                    wordListPostProcess();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            };
            ref2.addValueEventListener(listener2);
        }
    }

    private void wordListPostProcess() {
        ArrayAdapter<String> listArrayAdapter = new ArrayAdapter<>(
                PracticeActivity.this, R.layout.spinner_item, getNames(wordLists));
        listArrayAdapter.setDropDownViewResource(R.layout.spinner_item);
        listSpinner.setAdapter(listArrayAdapter);

        int lsIndex = -1, i = 0;
        for (NameIdPair nameIdPair : wordLists) {
            if (nameIdPair.getId().equals(listId)) lsIndex = i;
            i++;
        }

        if (lsIndex != -1) listSpinner.setSelection(lsIndex, false);
        else listId = wordLists.get(0).getId();

        listPB.setVisibility(View.GONE);
        listSpinner.setVisibility(View.VISIBLE);

        if(listId != null && wsId != null)loadButton.performClick();
    }

    private void getWords() {
        if (listId == null) {
            Toast.makeText(PracticeActivity.this, "No list selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ref3 != null) {ref3.removeEventListener(listener3);}

        if(listWordMap.containsKey(listId)){
            words = listWordMap.get(listId);
            wordsPostProcess();
        }
        else {
            DBRef db = new DBRef();
            ref3 = db.listWordsRef(listId);
            listener3 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ArrayList<Word> _words = new ArrayList<>();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Word word = ds.getValue(Word.class);
                        if (word.isPracticable()) {
                            _words.add(word);
                        }
                    }
                    listWordMap.put(listId, _words);
                    words = _words;

                    ref3.removeEventListener(listener3);
                    wordsPostProcess();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
            ref3.addValueEventListener(listener3);
        }
    }

    private void wordsPostProcess(){

        tempWords = new ArrayList<>();
        for(Word w: words){
            int level = w.getLevel();
            if(selectedFlags[level]) tempWords.add(w);
        }

        String s = "";
        int i = 1;
        for(Word w: tempWords){
            s += ""+i+". "+w.getValue()+"\n";
            i++;
        }
        wsSpinner.setEnabled(true);
        listSpinner.setEnabled(true);

        ll3.setVisibility(View.VISIBLE);
        loadPracPB.setVisibility(GONE);
        ((TextView) findViewById(R.id.practicableWordsTV)).setText(s);
    }

    private void setupNavDrawerClick() {
        NavigationView mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Intent intent;
                switch (menuItem.getItemId()) {
                    case R.id.nav_learn:
                        intent = new Intent(PracticeActivity.this, WordSetActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case R.id.nav_signout:
                        final AlertDialog dialog = new AlertDialog.Builder(PracticeActivity.this, R.style.AlertDialogTheme)
                                .setTitle("Confirm Sign Out")
                                .setMessage("Are you sure you want to sign out?")
                                .setPositiveButton("Sign Out", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        FirebaseAuth.getInstance().signOut();
                                        Intent intent = new Intent(PracticeActivity.this, LoginActivity.class);
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
                                ((TextView)dialog.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                                dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                            }
                        });

                        dialog.show();

                        break;
                    case R.id.nav_search:
                        intent = new Intent(PracticeActivity.this, SearchActivity.class);
                        PracticeActivity.this.startActivity(intent);
                        break;
                    case R.id.nav_exercise:
                        intent = new Intent(PracticeActivity.this, PracticeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                }
                return true;
            }
        });
    }

    public void onCheckboxClicked(View view) {
        boolean checked = ((CheckBox) view).isChecked();

        switch(view.getId()) {
            case R.id.easy:
                selectedFlags[0] = checked;
                break;

            case R.id.normal:
                selectedFlags[1] = checked;
                break;

            case R.id.hard:
                selectedFlags[2] = checked;
                break;

            case R.id.vhard:
                selectedFlags[3] = checked;
                break;
        }

        tempWords = new ArrayList<>();
        for(Word w: words){
            int level = w.getLevel();
            if(selectedFlags[level]) tempWords.add(w);
        }

        wordsPostProcess();
    }

    boolean checkPreviousPractice(){
        String FILENAME_QUESTIONS = "previousQuestionsDescription";
        String FILENAME_WORDS = "practiceWords";

        String []files = fileList();
        for (String file : files) {
            if (file.equals(FILENAME_QUESTIONS) || file.equals(FILENAME_WORDS)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mDrawerLayout.isDrawerOpen(Gravity.START))
                mDrawerLayout.closeDrawer(Gravity.START);
            else {
                onBackPressed();
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.practice_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public ArrayList<String> getNames(ArrayList<NameIdPair> nameIdPairs) {
        ArrayList<String> ret = new ArrayList<>();
        for (NameIdPair p : nameIdPairs) {
            ret.add(p.getName());
        }
        return ret;
    }

    public class NameIdPair {
        String name, id;

        NameIdPair(String name, String id) {
            this.name = name;
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
