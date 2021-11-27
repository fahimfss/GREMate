package com.example.fahim.gremate;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fahim.gremate.Adapters.ExampleSentenceAdapter;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class ExampleSentencesActivity extends AppCompatActivity {

    private float textSize;
    private String word;

    private LinearLayout sentencesLL;
    private ProgressBar loadSentencePB;
    private TextView errorTV;

    private FetchSentencesAsync fetchSentences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_sentences);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        textSize = prefs.getFloat("textSize", 16);

        Bundle extras = getIntent().getExtras();
        if(extras!=null)
            word = extras.getString("word");

        else
            finish();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle(Html.fromHtml("<font color='#BDCBDA'>"+word.toUpperCase()+"</font>"));

        sentencesLL = (LinearLayout) findViewById(R.id.sentencesLL);
        loadSentencePB = (ProgressBar) findViewById(R.id.loadSentencePB);
        errorTV = (TextView) findViewById(R.id.errorTV);

        fetchSentences = new FetchSentencesAsync(this);
        fetchSentences.execute(word);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(fetchSentences!=null){
            fetchSentences.cancel(true);
            fetchSentences = null;
        }
    }

    void showData(ArrayList<Pair<String, String>> sentences){

        if(sentences == null || sentences.size() == 0){
            loadSentencePB.setVisibility(View.GONE);
            errorTV.setVisibility(View.VISIBLE);
            return;
        }

        RecyclerView sentencesRV = (RecyclerView)findViewById(R.id.sentencesRV);
        sentencesRV.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        sentencesRV.setLayoutManager(llm);
        sentencesRV.setAdapter(new ExampleSentenceAdapter(sentences, textSize));

        sentencesLL.setVisibility(View.VISIBLE);
        loadSentencePB.setVisibility(View.GONE);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class FetchSentencesAsync extends AsyncTask<String, Void, String> {

        protected WeakReference<ExampleSentencesActivity> activityWeakReference;

        public FetchSentencesAsync(ExampleSentencesActivity activity){
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected String doInBackground(String... strings) {
            String word = strings[0].toLowerCase();

            String url = "http://corpus.vocabulary.com/api/1.0/examples.json?query="+word+"&maxResults=25";

            try {
                Document doc = Jsoup.connect(url).ignoreContentType(true).timeout(10000).get();

                String jsn = doc.text();

                final ArrayList<Pair<String, String>> sentences = new ArrayList<>();

                String [] mnts = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

                JSONObject obj = new JSONObject(jsn);
                JSONArray sts = obj.getJSONObject("result").getJSONArray("sentences");
                for (int ii = 0; ii < sts.length(); ii++) {
                    JSONObject ith = (JSONObject) sts.get(ii);
                    String sentence = (String) ith.get("sentence");
                    JSONArray offset = (JSONArray) ith.get("offsets");
                    String finalSentence = "<b>" + (ii+1) + ".</b> ";
                    int pv = 0;
                    for (int j = 0; j < offset.length(); j += 2) {
                        int st = (int) offset.get(j);
                        int en = (int) offset.get(j + 1);
                        finalSentence += sentence.substring(pv, st);
                        finalSentence += "<b>";
                        finalSentence += sentence.substring(st, en);
                        finalSentence += "</b>";
                        pv = en;
                    }
                    if (pv < sentence.length()) {
                        finalSentence += sentence.substring(pv, sentence.length());
                    }

                    JSONObject src = (JSONObject) ith.get("volume");

                    String source = "", date = "", dateTmp, title = "";

                    try {
                        source = "<i>" + ((JSONObject)src.get("corpus")).get("name").toString() + "</i>";
                    } catch (Exception e){

                    }

                    try{
                        dateTmp = src.getString("datePublished");
                        dateTmp = dateTmp.substring(0, 10);   // 2001-03-16
                        int month = Integer.parseInt(dateTmp.substring(5, 7));

                        String year = dateTmp.substring(0, 4);
                        String mon = mnts[month-1];
                        String day = String.valueOf(Integer.parseInt(dateTmp.substring(8, 10)));

                        date = day + " " + mon+ ", " + year;
                    }catch (Exception e){

                    }

                    try{
                        title = src.getString("title");
                    } catch (Exception e){

                    }

                    Pair<String, String> mp;
                    if(source.length()>0 || date.length()>0 || title.length()>0) {
                        String tmp = "";
                        if(title.length()>0) tmp += title;
                        if(source.length()>0){
                            if(tmp.length()>0) tmp += " &mdash; ";
                            tmp += source;
                        }
                        if(date.length()>0){
                            if(tmp.length()>0) tmp+="&nbsp;&nbsp;&nbsp;&nbsp;";
                            tmp += date;
                        }

                        mp = new Pair<>(finalSentence, tmp);
                    }
                    else mp = new Pair<>(finalSentence, "");
                    sentences.add(mp);
                }

                if(activityWeakReference.get()!=null)  activityWeakReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        activityWeakReference.get().showData(sentences);
                    }
                });

            } catch (Exception e){
                e.printStackTrace();
                if(activityWeakReference.get()!=null)  activityWeakReference.get().runOnUiThread(new Runnable() {
                    public void run() {
                        activityWeakReference.get().showData(null);
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }
}
