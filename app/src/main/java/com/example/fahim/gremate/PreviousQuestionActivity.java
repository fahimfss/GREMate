package com.example.fahim.gremate;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.example.fahim.gremate.Adapters.QuestionAdapter;
import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.PracticePreviousQuestions;
import com.example.fahim.gremate.DataClasses.Word;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class PreviousQuestionActivity extends AppCompatActivity {

    private ArrayList<PracticePreviousQuestions> previousQuestions;
    private ArrayList<PracticePreviousQuestions> all;
    private ArrayList<PracticePreviousQuestions> mistakes;

    private RecyclerView previousQuestionsRV;

    private HashMap<String, Word> words;
    private  float textSize;
    QuestionAdapter questionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_previous_question);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Bundle extras = getIntent().getExtras();
        if(extras!=null) {
            all = extras.getParcelableArrayList("previousQuestions");
            Collections.reverse(all);
            previousQuestions = all;
            ArrayList<Word> tempWords = extras.getParcelableArrayList("words");
            words = new HashMap<>();
            for(Word w: tempWords){
                words.put(w.getCloneOf(), w);
            }
            mistakes = new ArrayList<>();
            for(PracticePreviousQuestions pq: all){
                if(!pq.getWasCorrect()) mistakes.add(pq);
            }
        }
        else
            finish();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle(Html.fromHtml("<font color='#BDCBDA'>Previous Questions</font>"));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = prefs.getFloat("textSize", 16);

        Log.d("PrvQsActv", ""+ previousQuestions.size()+"  "+words.size());

        previousQuestionsRV = (RecyclerView) findViewById(R.id.questionsRV);
        previousQuestionsRV.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        previousQuestionsRV.setLayoutManager(llm);
        questionAdapter = new QuestionAdapter(this, words, previousQuestions, textSize);
        previousQuestionsRV.setAdapter(questionAdapter);
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
                        words.get(st[0]).setLevel(Integer.valueOf(st[1]));
                        questionAdapter.notifyDataSetChanged();
                    }
                }
                br.close();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.previous_question_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.all:
                if(previousQuestions != all){
                    previousQuestions = all;
                    questionAdapter = new QuestionAdapter(this, words, previousQuestions, textSize);
                    previousQuestionsRV.setAdapter(questionAdapter);
                }
                break;
            case R.id.mistake:
                if(previousQuestions != mistakes){
                    previousQuestions = mistakes;
                    questionAdapter = new QuestionAdapter(this, words, previousQuestions, textSize);
                    previousQuestionsRV.setAdapter(questionAdapter);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
