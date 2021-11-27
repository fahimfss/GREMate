package com.example.fahim.gremate.DataClasses;

/**
 * Created by fahim on 12/16/16.
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public abstract class FetchDataAsync extends AsyncTask<String, Void, String> {

    protected WeakReference<Activity> activityWeakReference;

    protected WordAllData wordAllData;

    protected StringBuilder allDefs, allBoldSyns;

    protected WordPractice wordPractice;

    private boolean error = true;

    public FetchDataAsync(Activity activity) {
        activityWeakReference = new WeakReference<>(activity);
    }

    @Override
    protected String doInBackground(String... strings) {

        String word = strings[0].toLowerCase();

        try {

            String url1 = "https://www.vocabulary.com/dictionary/";
            Document doc1 = Jsoup.connect(url1 + word).timeout(10000).get();

            String url2 = "https://en.oxforddictionaries.com/definition/us/";
            Document doc2 = Jsoup.connect(url2 + word).timeout(10000).get();

            Elements grambs = doc2.select("section.gramb");
            if(grambs.hasText()) error = false;

            if(!error){

                String shortds = ""; doc1.select("p.short").text();
                String longds = ""; doc1.select("p.long").text();

                // Set it from the calling class
                // Word word1 = new Word(wordSetId, wordListId, word, true, false, false, 0, 0);

                String pronunciationLink = "";
                try {
                    shortds =  doc1.select("p.short").text();
                    longds = doc1.select("p.long").text();

                    String pl = doc1.select(".audio").first().attr("data-audio");
                    if (pl.length() > 0) {
                        String LINK_VOC = "https://audio.vocab.com/1.0/us/";
                        pronunciationLink = LINK_VOC + pl + ".mp3";
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                allDefs = new StringBuilder();
                allBoldSyns = new StringBuilder();

                ArrayList<WordDef> wordDefs = new ArrayList<>();

                for(Element e1: grambs){
                    String pos = posEdit(e1.select("span.pos").text());

                    Elements lis = e1.select("ul.semb > li");
                    for(Element e2: lis){
                        StringBuilder sb = new StringBuilder();

                        Elements children = e2.child(0).children();
                        sb.append("<div class=\"trg\">");
                        for(Element e3: children){
                            if(e3.is("ol.subSenses")){
                                sb.append("</div>");
                                WordDef wf = parseDef(Jsoup.parse(sb.toString()), word + " (" + pos + ")" );
                                if(wf!=null) wordDefs.add(wf);
                                sb.setLength(0);

                                Elements trgs = e3.select("li.subSense");

                                for(Element e4: trgs){
                                    WordDef wd = parseDef(e4, word + " (" + pos + ")");
                                    if(wd!=null) wordDefs.add(wd);
                                }
                            }
                            else {
                                sb.append(e3.toString());
                            }
                        }
                        if(sb.length() != 0) {
                            WordDef wd = parseDef(Jsoup.parse(sb.toString()), word + " (" + pos + ")" );
                            if(wd!=null)wordDefs.add(wd);
                        }
                    }
                }
                wordAllData = new WordAllData();

                if (shortds.length() < 1) wordAllData.setWordData(new WordData("", "", pronunciationLink));
                else wordAllData.setWordData(new WordData(shortds + "\n\n" + longds, "", pronunciationLink));

                wordAllData.setWordDefs(wordDefs);
                wordPractice = new WordPractice(word, allBoldSyns.toString(), allDefs.toString(), pronunciationLink);
            }
        } catch (Throwable t) {
            error = true;
            t.printStackTrace();
        }
        return "";
    }

    private String posEdit(String pos){
        if(pos.equals("adjective") || pos.equals("adverb")){
            return pos.substring(0, 3) + ".";
        }
        else return pos.substring(0, 1) + ".";
    }

    private WordDef parseDef(Element e2, String title){

        WordDef def = new WordDef();
        def.setTitle(title);
        String defStr = "";
        try {
            defStr = e2.select("span.ind").first().text();
            def.setDef(defStr);
            if(def.getDef() == null || def.getDef().length()<1) return  null;
        }
        catch (Exception e){
            return null;
        }

        if(allDefs.length() > 0) allDefs.append(DB.DELIM);
        allDefs.append(defStr);

        //******SENTENCE*********//
        StringBuilder sentences = new StringBuilder();
        int size = 0;
        Elements exs = e2.select("div.trg > div.exg > div.ex");
        for(Element e3: exs){
            if(size<4){
                if(size>0) sentences.append(DB.DELIM);
                sentences.append(e3.select("em").first().text());
                size++;
            }
            else break;
        }
        exs = e2.select("div.trg > div.examples > div.exg > ul > li.ex");
        for(Element e4: exs){
            if(size<4){
                if(size>0) sentences.append(DB.DELIM);
                sentences.append(e4.select("em").first().text());
                size++;
            }
            else break;
        }
        def.setSentences(sentences.toString());
        //******SENTENCE*********//


        //******SYNONYMS*********//
        StringBuilder bold = new StringBuilder();
        StringBuilder syn = new StringBuilder();
        int boldSize = 0, synSize = 0;

        Elements synexs = e2.select("div.trg > div.synonyms > div.exg > div.exs");
        for(Element e4: synexs){
            String syns = e4.text();
            String [] ss = syns.split(", ");
            if (ss.length >= 1){

                if(boldSize>0)bold.append(DB.DELIM);
                bold.append(ss[0]);
                boldSize++;

                if(allBoldSyns.length() > 0) allBoldSyns.append(DB.DELIM);
                allBoldSyns.append(ss[0]);

                for(int i = 1; i < ss.length; i++){
                    if(synSize>0)syn.append(DB.DELIM);
                    syn.append(ss[i]);
                    synSize++;
                }
            }
        }
        def.setBoldSyns(bold.toString());
        def.setSyns(syn.toString());
        //******SYNONYMS*********//

        return def;
    }

}
