package com.example.fahim.gremate;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.FeedTestData;
import com.example.fahim.gremate.DataClasses.Highlighter;
import com.example.fahim.gremate.DataClasses.PracticePreviousQuestions;
import com.example.fahim.gremate.DataClasses.ShortData;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.example.fahim.gremate.DataClasses.WordPractice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import static android.view.View.GONE;

public class PracticingActivity extends AppCompatActivity {

    private ArrayList<Word> words;
    private int index;
    private String ans;
    private String[][] OD;

    private String type;

    private TextView[] ansTVs;
    private TextView questionTV;
    private TextView postAnswerInfo;

    private int ansIndex;
    private int noQuestions;
    private int noCorrect;

    private WordPractice wordPractice;

    private RadioGroup diffRadioGroup;
    private int wordLevel;

    private Word word;

    private boolean thisJudged;

    private AppCompatButton nextButton;
    private AppCompatButton viewButton;

    private ScrollView practicingSV;

    private ProgressBar practicingLoading;

    private ArrayList<PracticePreviousQuestions> previousQuestions;

    private HashMap<String, ArrayList<Integer>> levelMap;
    private HashMap<String, HashSet<String>> answerMap;

    private PlaybackPronunciation playbackPronunciation;
    private boolean pronunciationPlaying;
    MediaPlayer mediaPlayer;

    private int autoPronounce;

    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private Random random;

    private PracticePreviousQuestions prevQues;

    FileOutputStream fileQuestions;

    DatabaseReference ref;
    ValueEventListener listener;

    private Typeface romanType;

    private static final String FILENAME_QUESTIONS = "previousQuestionsDescription";
    private static final String FILENAME_WORDS = "practiceWords";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practicing);

        romanType = Typeface.createFromAsset(getAssets(),"fonts/times.ttf");

        practicingSV = findViewById(R.id.practicingSV);
        practicingSV.setVisibility(GONE);

        postAnswerInfo = findViewById(R.id.postAnswerInfo);
        postAnswerInfo.setVisibility(GONE);
        postAnswerInfo.setLineSpacing(0, 1.133f);
        postAnswerInfo.setTypeface(romanType);

        practicingLoading = findViewById(R.id.practicingLoading);
        practicingLoading.setVisibility(View.VISIBLE);

        wordPractice = null;

        Bundle b = getIntent().getExtras();

        setTitle(Html.fromHtml("<font color='#BDCBDA'>0 </font>"));
        random = new Random();
        diffRadioGroup = findViewById(R.id.diffRadioGroup);

        index = 0;
        noQuestions = 0;
        noCorrect = 0;

        OD = new FeedTestData().getPracticeWords(this);

        deleteFile("levelChange");

        type = b.getString("type");
        if(type.equals("resume") || savedInstanceState != null){
            restoreStateFromFile();
        }
        else {
            words = b.getParcelableArrayList("words");
            ArrayList<Word> temp = new ArrayList<>();
            for(int lvl=0; lvl<=Word.LVL_VHARD; lvl++) {
                for (int i = 0; i < words.size(); i++) {
                    if(words.get(i).getLevel()>=lvl) {
                        temp.add(words.get(i));
                    }
                }
                if(type.equals("short"))break;
            }
            words = temp;

            previousQuestions = new ArrayList<>();

            try {
                deleteFile(FILENAME_QUESTIONS);
            }catch (Exception e){
                e.printStackTrace();
            }
            randomizeWords();
        }

        loadWordPracticeDataPreFetch(words.get(index).getCloneOf());

        try {
            fileQuestions = openFileOutput(FILENAME_QUESTIONS, MODE_APPEND);

            if(savedInstanceState == null) {

                FileOutputStream fileWords = openFileOutput(FILENAME_WORDS, MODE_PRIVATE);
                for (int i = 0; i < words.size(); i++) {
                    fileWords.write(words.get(i).getStringForm().getBytes());
                }
                fileWords.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        autoPronounce = prefs.getInt("pracAutoPronounce", 0);

        ansTVs = new TextView[5];

        ansTVs[0] = findViewById(R.id.ansTV1);
        ansTVs[1] = findViewById(R.id.ansTV2);
        ansTVs[2] = findViewById(R.id.ansTV3);
        ansTVs[3] = findViewById(R.id.ansTV4);
        ansTVs[4] = findViewById(R.id.ansTV5);

        for(int i=0; i<5; i++){
            ansTVs[i].setTypeface(romanType);
        }

        questionTV = findViewById(R.id.questionTv);
        questionTV.setTypeface(romanType);

        nextButton = findViewById(R.id.nextBtn);
        nextButton.setVisibility(GONE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nextButton.setVisibility(GONE);
                viewButton.setVisibility(GONE);
                postAnswerInfo.setVisibility(GONE);

                practicingSV.setVisibility(GONE);
                diffRadioGroup.setVisibility(GONE);
                practicingLoading.setVisibility(View.VISIBLE);

                previousQuestions.add(prevQues);

                index++;
                if (index == words.size()) {
                    index = 0;
                    randomizeWords();
                    createLevelMap();

                    try {
                        FileOutputStream fileWords = openFileOutput(FILENAME_WORDS, MODE_PRIVATE);
                        for (int i = 0; i < words.size(); i++) {
                            fileWords.write(words.get(i).getStringForm().getBytes());
                        }
                        fileWords.close();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                loadWordPracticeDataPreFetch(words.get(index).getCloneOf());

                try{
                    String toWrite = "" + index + " " + noQuestions + " " + noCorrect + "\n";
                    fileQuestions.write(prevQues.getStringForm().getBytes());
                    fileQuestions.write(toWrite.getBytes());
                    fileQuestions.flush();
                }catch (Exception e){
                    e.printStackTrace();
                }

                wordPractice = null;

                if (wordLevel != word.getLevel()) {
                    DB.setWordLevel(word.getCloneOf(), wordLevel);
                    if(levelMap == null || !levelMap.containsKey(word.getCloneOf())){
                        createLevelMap();
                    }
                    ArrayList<Integer> al = levelMap.get(word.getCloneOf());
                    for(int i: al) words.get(i).setLevel(wordLevel);
                }

                for (int i = 0; i < 5; i++) {
                    ansTVs[i].setTextColor(getResources().getColor(R.color.darkFore1));
                    ansTVs[i].setTypeface(romanType);
                }

                loadWordPracticeData(words.get(index).getCloneOf());

            }
        });

        viewButton = (AppCompatButton) findViewById(R.id.viewBtn);
        viewButton.setVisibility(GONE);
        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PracticingActivity.this, SearchActivity.class);

                ArrayList<Word> temp = new ArrayList<>();
                temp.add(words.get(index));
                intent.putParcelableArrayListExtra("words", temp);
                startActivity(intent);

            }
        });

        diffRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.radio1:
                        wordLevel = 0;
                        break;
                    case R.id.radio2:
                        wordLevel = 1;
                        break;
                    case R.id.radio3:
                        wordLevel = 2;
                        break;
                    case R.id.radio4:
                        wordLevel = 3;
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        answerMap = new HashMap<>();

        pronunciationPlaying = false;
        mediaPlayer = new MediaPlayer();

        createLevelMap();
        loadWordPracticeData(words.get(index).getCloneOf());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreStateFromFile();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putString("type", "restore");
    }

    @Override
    protected void onResume() {
        super.onResume();

        String []files = fileList();
        int flag = 0;
        for (String file : files) {
            if (file.equals("levelChange")) {
                flag = 1;
                break;
            }
        }

        if(flag == 1){
            String filename = "levelChange";
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader( openFileInput(filename) ) );
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.length()>0){
                        String [] st = line.split(DB.DELIM);
                        int level = Integer.valueOf(st[1]);
                        if(levelMap == null || !levelMap.containsKey(st[0])){
                            createLevelMap();
                        }
                        ArrayList<Integer> al = levelMap.get(st[0]);
                        for(int i: al) words.get(i).setLevel(level);

                        if(word != null && word.getCloneOf().equals(st[0])){
                            word.setLevel(level);
                            ((RadioButton)diffRadioGroup.getChildAt(level)).setChecked(true);
                            wordLevel = level;
                        }
                    }
                }
                br.close();
                deleteFile(filename);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.practicing_menu, menu);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        autoPronounce = prefs.getInt("pracAutoPronounce", 0);
        if(autoPronounce == 0){
            menu.findItem(R.id.auto_pronounce).setTitle("Auto pronounce");
        }
        else {
            menu.findItem(R.id.auto_pronounce).setTitle("Stop auto pronounce");
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (wordLevel != word.getLevel()) {
            DB.setWordLevel(word.getCloneOf(), wordLevel);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            mediaPlayer.release();
        }
        try {
            fileQuestions.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        if(playbackPronunciation!=null){
            playbackPronunciation.cancel(true);
            playbackPronunciation = null;
        }

        if(ref!=null && listener!=null){
            ref.removeEventListener(listener);
        }
    }

    private void loadWordPracticeData(final String id) {
        DBRef db = new DBRef();

        word = words.get(index);
        wordLevel = word.getLevel();
        diffRadioGroup.setVisibility(GONE);
        ((RadioButton)diffRadioGroup.getChildAt(wordLevel)).setChecked(true);

        final DatabaseReference ref = db.wordPracticeRef(id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                wordPractice = dataSnapshot.getValue(WordPractice.class);
                setupQuestion();
                setupPostAnswerInfo(word.getCloneOf());
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setupPostAnswerInfo(String wordId) {
        if(ref!=null && listener!=null){
            ref.removeEventListener(listener);
        }

        practicingSV.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        DBRef db = new DBRef();
        ref = db.shortDataRef(wordId);
        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<b>Short Review: </b>");

                ShortData shortData = dataSnapshot.getValue(ShortData.class);
                if(shortData == null) return;
                if(shortData.getDes()!=null && shortData.getDes().length()>0){
                    stringBuilder.append("<br>");
                    stringBuilder.append(Highlighter.highlight(shortData.getDes(), word.getValue()));
                }

                if(shortData.getSyns()!=null && shortData.getSyns().length()>0){
                    stringBuilder.append("<br><b>Synonyms: ");
                    String[] syns = shortData.getSyns().split(DB.DELIM);
                    for (int i = 0; i < syns.length; i++) {
                        if(syns[i].length()<1)continue;
                        if (i > 0) stringBuilder.append(", ");
                        stringBuilder.append(syns[i]);
                    }
                    stringBuilder.append("</b>");
                }

                if(shortData.getSentences()!=null && shortData.getSentences().length()>0){
                    String[] sents = shortData.getSentences().split(DB.DELIM);
                    for (int i = 0; i < sents.length; i++) {
                        if(sents[i].length()<1)continue;
                        stringBuilder.append("<br><b>");
                        stringBuilder.append(i+1);
                        stringBuilder.append(". </b>");
                        if (sents[i].length() > 2 && sents[i].charAt(0) == '‘' && sents[i].charAt(sents[i].length() - 1) == '’') {
                            sents[i] = sents[i].substring(1, sents[i].length() - 1);
                        }
                        sents[i] = sents[i].substring(0, 1).toUpperCase() + sents[i].substring(1, sents[i].length());
                        stringBuilder.append(sents[i]);
                    }
                }
                postAnswerInfo.setText(fromHtml(stringBuilder.toString()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        ref.addValueEventListener(listener);
    }


    private void loadWordPracticeDataPreFetch(final String id) {
        DBRef db = new DBRef();

        final DatabaseReference ref = db.wordPracticeRef(id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void setupQuestion() {

        prevQues = new PracticePreviousQuestions();
        prevQues.setValue(word.getValue());
        prevQues.setId(word.getCloneOf());

        if(answerMap == null) answerMap = new HashMap<>();
        if(!answerMap.containsKey(wordPractice.getWord())){
            answerMap.put(wordPractice.getWord(), new HashSet<String>());
        }
        HashSet<String> ansSet = answerMap.get(wordPractice.getWord());

        String [] defs = wordPractice.getDefinitions().split(DB.DELIM);
        String [] syns = wordPractice.getSynonyms().split(DB.DELIM);

        ArrayList<Pair<String, Integer>> oldAns = new ArrayList<>();
        ArrayList<Pair<String, Integer>> newAns = new ArrayList<>();

        for(String s: syns) {
            if(s!=null && s.length()>0){
                s = s.toLowerCase();
                if(ansSet.contains(s)) oldAns.add(new Pair<>(s, 0));
                else newAns.add(new Pair<>(s, 0));
            }
        }
        for(String s: defs) {
            if(s!=null && s.length()>0){
                s = s.toLowerCase();
                if(ansSet.contains(s)) oldAns.add(new Pair<>(s, 1));
                else newAns.add(new Pair<>(s, 1));
            }
        }

        if(newAns.size() == 0){
            ansSet.clear();
            newAns = oldAns;
        }

        Pair<String, Integer> ansPair;

        int rv = random.nextInt(newAns.size());
        ansPair = newAns.get(rv);
        ansSet.add(ansPair.first);

        ans = ansPair.first;
        if(ans.charAt(ans.length()-1)=='.') ans = ans.substring(0, ans.length()-1);
        int type = ansPair.second;

        if(type == 0) questionTV.setText("Synonym of the word " + wordPractice.getWord().toLowerCase() + " is?");
        else questionTV.setText("Meaning of the word " + wordPractice.getWord().toLowerCase() + " is?");

        ArrayList<String> otDefs = new ArrayList<>();
        String currentWord = words.get(index).getValue().toLowerCase();

        HashSet<String> mp = new HashSet<>();
        mp.add(currentWord);
        while (otDefs.size() < 4) {
            int ind = Math.abs(random.nextInt()) % OD.length;
            if(mp.contains(OD[ind][0].toLowerCase()) || mp.contains(OD[ind][1].toLowerCase())) continue;

            otDefs.add(OD[ind][type].toLowerCase());
            mp.add(OD[ind][0].toLowerCase());
            mp.add(OD[ind][1].toLowerCase());
        }

        ansIndex = random.nextInt(5);
        prevQues.setCorrectIndex(ansIndex);
        ansTVs[ansIndex].setText(fromHtml("<b>" + (ansIndex + 1) + ".</b> " + ans));

        int j = 0;
        for (int i = 0; i < 5; i++) {
            if (i == ansIndex) continue;
            ansTVs[i].setText(fromHtml("<b>" + (i + 1) + ".</b> " + otDefs.get(j++)));
        }

        prevQues.setWasCorrect(false);
        prevQues.setAns1(ansTVs[0].getText().toString().substring(3));
        prevQues.setAns2(ansTVs[1].getText().toString().substring(3));
        prevQues.setAns3(ansTVs[2].getText().toString().substring(3));
        prevQues.setAns4(ansTVs[3].getText().toString().substring(3));
        prevQues.setAns5(ansTVs[4].getText().toString().substring(3));

        practicingLoading.setVisibility(GONE);
        practicingSV.setVisibility(View.VISIBLE);

        thisJudged = false;

        setTitle(Html.fromHtml("<font color='#BDCBDA'>" + noCorrect + "/" + noQuestions +  " (" + words.size() + ") </font>"));
        noQuestions++;

        if(autoPronounce == 1) {
            mediaPlayer.reset();
            pronunciationPlaying = false;
            pronunciationInit(word.getValue().toLowerCase());
        }
    }

    private boolean checkPlacement(int prevPos, int newPos){

        String wordId = words.get(prevPos).getCloneOf();
        for(int i=1; i<5; i++){
            int rv = newPos + i;
            int lv = newPos - i;
            if(rv<words.size() && words.get(rv).getCloneOf().equals(wordId)) return false;
            if(lv>=0 && words.get(lv).getCloneOf().equals(wordId)) return false;
        }

        wordId = words.get(newPos).getCloneOf();
        for(int i=1; i<5; i++){
            int rv = prevPos + i;
            int lv = prevPos - i;
            if(rv<words.size() && words.get(rv).getCloneOf().equals(wordId)) return false;
            if(lv>=0 && words.get(lv).getCloneOf().equals(wordId)) return false;
        }

        return true;
    }

    private void randomizeWords() {
        HashMap<String, Integer> map = new HashMap<>();
        for(Word w: words) map.put(w.getCloneOf(), -1);

        int index;
        Word temp;
        for (int i = words.size() - 1; i > 0; i--) {
            for(int j=0; j<5; j++) {
                index = random.nextInt(i + 1);
                temp = words.get(index);

                int lastIndex = map.get(temp.getCloneOf());
                if(j<4 && lastIndex != -1 && lastIndex - i < 5) continue;

                words.set(index, words.get(i));
                words.set(i, temp);
                map.put(temp.getCloneOf(), i);
                break;
            }
        }

        for(int i=0; i<5 && i<words.size(); i++){
            if(!checkPlacement(i, i)){
                boolean flag = false;
                for(int j=0; j<5; j++) {
                    index = random.nextInt(words.size());
                    if(checkPlacement(i, index)){
                        Word tm = words.get(i);
                        words.set(i, words.get(index));
                        words.set(index, tm);
                        flag = true;
                        break;
                    }
                }
                if(flag) continue;

                for(int j = 0; j<words.size(); j++){
                    if(checkPlacement(i, j)){
                        Word tm = words.get(i);
                        words.set(i, words.get(j));
                        words.set(j, tm);
                        break;
                    }
                }
            }
        }
    }

    public void validateResult(View v) {
        int ind = Integer.valueOf(v.getTag().toString());
        if (ind == ansIndex) {
            ansTVs[ind].setTextColor(getResources().getColor(R.color.easy));
            ansTVs[ind].setTypeface(romanType, Typeface.BOLD);
            if (!thisJudged) {
                prevQues.setWasCorrect(true);
                noCorrect++;
            }
        } else {
            ansTVs[ind].setTextColor(getResources().getColor(R.color.vhard));
            ansTVs[ind].setTypeface(romanType, Typeface.BOLD);
        }
        if(!thisJudged){
            setTitle(Html.fromHtml("<font color='#BDCBDA'>" + noCorrect + "/" + noQuestions +  " (" + words.size() + ") </font>"));
        }
        thisJudged = true;

        nextButton.setVisibility(View.VISIBLE);
        viewButton.setVisibility(View.VISIBLE);
        postAnswerInfo.setVisibility(View.VISIBLE);
        diffRadioGroup.setVisibility(View.VISIBLE);
        practicingSV.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        postAnswerInfo.setTextIsSelectable(true);
    }

    public void createLevelMap(){
        levelMap = new HashMap<>();
        if(words != null){
            int i = 0;
            for(Word w: words){
                if(levelMap.containsKey(w.getCloneOf())){
                    ArrayList<Integer> al = levelMap.get(w.getCloneOf());
                    al.add(i++);
                }
                else {
                    ArrayList<Integer> al = new ArrayList<>();
                    al.add(i++);
                    levelMap.put(w.getCloneOf(), al);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.pronounce:
                mediaPlayer.reset();
                pronunciationPlaying = false;

                pronunciationInit(word.getValue().toLowerCase());
                break;

            case R.id.prevQues:
                Intent intent = new Intent(this, PreviousQuestionActivity.class);
                intent.putParcelableArrayListExtra("previousQuestions", previousQuestions);
                intent.putParcelableArrayListExtra("words", words);

                startActivity(intent);
                break;

            case R.id.auto_pronounce:
                if(autoPronounce == 0){
                    autoPronounce = 1;
                    item.setTitle("Stop auto pronounce");
                }
                else {
                    autoPronounce = 0;
                    item.setTitle("Auto pronounce");
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("pracAutoPronounce", autoPronounce);
                editor.apply();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String source) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(source, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(source);
        }
    }

    @Override
    public void onBackPressed() {
        StringBuilder msg = new StringBuilder();

        ArrayList<String> wrongAns = new ArrayList<>();

        for(PracticePreviousQuestions pq: previousQuestions){
            if(!pq.getWasCorrect()){
                wrongAns.add("<b>"+pq.getValue().toUpperCase()+": </b>" + pq.getCorrectAns());
            }
        }

        if(wrongAns.size()>0) {
            msg.append("<b><u>REVIEW</u></b>");
            for(String s: wrongAns){
                msg.append("<br>");
                msg.append(s);
            }
        }
        if(msg.length()>0)msg.append("<br><br>");
        msg.append( "You correctly answered " );
        msg.append(noCorrect);
        msg.append(" out of ");
        msg.append(noQuestions);
        msg.append(". Do you want to stop?");

        final AlertDialog dialog = new AlertDialog.Builder(PracticingActivity.this, R.style.AlertDialogTheme)
                .setTitle("End practice?")
                .setMessage(fromHtml(msg.toString()))
                .setPositiveButton("STOP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
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

    private void pronunciationInit(String word){

        int permission = ActivityCompat.checkSelfPermission(PracticingActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    PracticingActivity.this,
                    PERMISSIONS_STORAGE, 0
            );
        } else{
            pronunciationPlay(word);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        pronunciationPlay(word.getValue().toLowerCase());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void pronunciationPlay(String word){
        if(!pronunciationPlaying) {
            pronunciationPlaying = true;
            File dir = new File(Environment.getExternalStorageDirectory(), "GREMate" + File.separator + "Word Pronunciations");
            if(!dir.exists()) dir.mkdirs();

            String mp3Dir = "GREMate" + File.separator + "Word Pronunciations" + File.separator + word + ".mp3";

            File mp3File = new File(Environment.getExternalStorageDirectory(), mp3Dir);
            if (mp3File.exists()) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(mp3File.getPath());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            pronunciationPlaying = false;
                        }
                    });
                } catch (IOException e) {
                    if(playbackPronunciation != null) playbackPronunciation.cancel(true);
                    playbackPronunciation = new PlaybackPronunciation(this);
                    playbackPronunciation.execute(wordPractice.getPronunciation(), wordPractice.getWord());
                }
            } else {
                if(playbackPronunciation != null) playbackPronunciation.cancel(true);
                playbackPronunciation = new PlaybackPronunciation(this);
                playbackPronunciation.execute(wordPractice.getPronunciation(), wordPractice.getWord());
            }
        }
    }

    private static class PlaybackPronunciation extends AsyncTask<String, Void, String> {

        protected WeakReference<PracticingActivity> activityWeakReference;

        public PlaybackPronunciation(PracticingActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... strings) {
            if(activityWeakReference != null) {
                MediaPlayer player = activityWeakReference.get().mediaPlayer;
                if (activityWeakReference.get().isNetworkConnected()) {
                    try {
                        String link = strings[0];
                        String word = strings[1].toLowerCase();
                        if (link.length() < 1) {
                            activityWeakReference.get().runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activityWeakReference.get(), "Pronunciation not found!", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return "";
                        }

                        URLConnection conn = new URL(link).openConnection();
                        InputStream is = conn.getInputStream();
                        File dir = new File(Environment.getExternalStorageDirectory(), "GREMate" + File.separator + "Word Pronunciations");
                        if(!dir.exists()) dir.mkdirs();

                        String mp3Dir = "GREMate" + File.separator + "Word Pronunciations" + File.separator + word + ".mp3";

                        File mp3File = new File(Environment.getExternalStorageDirectory(), mp3Dir);
                        OutputStream outStream = new FileOutputStream(mp3File);
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = is.read(buffer)) > 0) {
                            outStream.write(buffer, 0, len);
                        }
                        outStream.close();

                        player.reset();
                        player.setDataSource(mp3File.getPath());
                        player.prepare();
                        player.start();
                        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                if(activityWeakReference != null) activityWeakReference.get().pronunciationPlaying = false;
                            }
                        });

                        return "";

                    } catch (Exception e) {
                        e.printStackTrace();
                        if(activityWeakReference != null) activityWeakReference.get().pronunciationPlaying = false;
                        activityWeakReference.get().runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activityWeakReference.get(), "Error...", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return "";
                    }
                } else {
                    if(activityWeakReference != null) activityWeakReference.get().pronunciationPlaying = false;
                    activityWeakReference.get().runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activityWeakReference.get(), "Internet connection required!", Toast.LENGTH_SHORT).show();
                        }
                    });
                    return "";
                }
            }
            return "";
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

//    private void writePreviousQuestionsToFile(){
//
//        if(previousQuestions == null) return;
//
//        String filename = "prevQuestions";
//        FileOutputStream outputStream;
//
//        try {
//            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//            for(int i = 0; i < previousQuestions.size(); i++){
//                outputStream.write(previousQuestions.get(i).getStringForm().getBytes());
//            }
//            outputStream.close();
//        } catch (Exception e) {
//            Log.d("PracticingActivityFile", "write fail 1 ");
//            e.printStackTrace();
//        }
//
//        filename = "prevQuestionsWords";
//        try {
//            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//            for(int i = 0; i < words.size(); i++){
//                outputStream.write(words.get(i).getStringForm().getBytes());
//            }
//            outputStream.close();
//        } catch (Exception e) {
//            Log.d("PracticingActivityFile", "write fail 2 ");
//            e.printStackTrace();
//        }
//
//        filename = "prevQuestionsData";
//        try {
//            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//            outputStream.write(String.valueOf(index).getBytes()); outputStream.write('\n');
//            outputStream.write(String.valueOf(noQuestions-1).getBytes()); outputStream.write('\n');
//            outputStream.write(String.valueOf(prvCorrect).getBytes());
//
//            outputStream.close();
//        } catch (Exception e) {
//            Log.d("PracticingActivityFile", "write fail 3 ");
//            e.printStackTrace();
//        }
//
//        Log.d("PracticingActivityFile", "write end");
//    }

    private void restoreStateFromFile(){
        previousQuestions = new ArrayList<>();
        words = new ArrayList<>();
        answerMap = new HashMap<>();
        String line1, line2="";
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader( openFileInput(FILENAME_QUESTIONS) ) );
            while ((line1 = br.readLine()) != null) {
                if(line1.length()>0){
                    PracticePreviousQuestions previousQuestion = new PracticePreviousQuestions(line1);
                    previousQuestions.add(previousQuestion);

                    if(!answerMap.containsKey(previousQuestion.getValue())){
                        answerMap.put(previousQuestion.getValue(), new HashSet<String>());
                    }
                    HashSet<String> ansSet = answerMap.get(previousQuestion.getValue());

                    ansSet.add(previousQuestion.getCorrectAns());

                    line2 = br.readLine();
                }
            }
            if(line2 != null && line2.length()>0) {
                String[] states = line2.split(" ");
                index = Integer.valueOf(states[0]);
                noQuestions = Integer.valueOf(states[1]);
                noCorrect = Integer.valueOf(states[2]);
            }
            br.close();
        }
        catch (IOException e) {
            Log.d("PracticingActivityFile", "read fail 1 ");
            e.printStackTrace();
        }

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader( openFileInput(FILENAME_WORDS) ) );
            String line;
            while ((line = br.readLine()) != null) {
                if(line.length()>0){
                    words.add(new Word(line));
                }
            }
            br.close();
        }
        catch (IOException e) {
            Log.d("PracticingActivityFile", "read fail 3 ");
            e.printStackTrace();
        }

        Log.d("PracticingActivityFile", "read end");
    }

}
