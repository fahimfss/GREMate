package com.example.fahim.gremate.DataClasses;

import android.util.Log;

import com.google.firebase.database.Exclude;

/**
 * Created by fahim on 12/23/16.
 */

public class WordDef {
    private String title, def, boldSyns, syns, sentences;

    public WordDef() {
        this.title = "";
        this.def = "";
        this.boldSyns = "";
        this.syns = "";
        this.sentences = "";
    }

    public WordDef(String title, String def, String boldSyns, String syns, String sentences) {
        setTitle(title);
        this.def = def;
        this.boldSyns = boldSyns;
        this.syns = syns;
        this.sentences = sentences;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        title = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
        this.title = title;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public String getBoldSyns() {
        return boldSyns;
    }

    public void setBoldSyns(String boldSyns) {
        this.boldSyns = boldSyns;
    }

    public String getSyns() {
        return syns;
    }

    public void setSyns(String syns) {
        this.syns = syns;
    }

    public String getSentences() {
        return sentences;
    }

    public void setSentences(String sentences) {
        this.sentences = sentences;
    }

    @Exclude
    public String[] getSentencesArray(){
        if(sentences == null || sentences.length()==0) return null;
        return sentences.split(DB.DELIM);
    }

    @Exclude
    public String[] getBoldSynsArray(){
        return boldSyns.split(DB.DELIM);
    }

    @Exclude
    public String[] getSynsArray(){
        return syns.split(DB.DELIM);
    }

    @Exclude
    public String getDefinationHtml(int no){

        return "<b>" +
                String.valueOf(no) +
                ". " +
                getTitle() +
                "</b><br>" +
                "<i>" +
                getDef() +
                "</i>";
    }

    @Exclude
    public String getSynonymHtml(){
        StringBuilder sb = new StringBuilder();

        if(boldSyns.length()>0){
            sb.append("<b>Synonyms:<br>");
            String [] boldSynsArray = getBoldSynsArray();
            for(int i=0; i<boldSynsArray.length; i++){
                if(i>0) sb.append(", ");
                sb.append(boldSynsArray[i]);
            }
            sb.append("</b>");
        }

        if(syns.length() > 0){
            if(sb.length()>0) sb.append(", ");
            else sb.append("<b>Synonyms:</b><br> ");

            String [] synsArray = getSynsArray();
            for(int i=0; i<synsArray.length; i++){
                if(i>0) sb.append(", ");
                sb.append(synsArray[i]);
            }
        }

        return sb.toString();
    }

    @Exclude
    public String getSentenceHtml(){

        StringBuilder sb = new StringBuilder();
        String [] sents = getSentencesArray();
        if(sents != null && sents.length > 0) {
            sb.append("<b>Sentences:</b>");
            for (int i=0; i<sents.length; i++) {
                String sent = sents[i];
                if(sent.length()>0) {
                    sb.append("<br>");
                    sb.append(i+1);
                    sb.append(". ");
                    String st = sent;
                    if (st.length() > 2 && st.charAt(0) == '‘' && st.charAt(st.length() - 1) == '’') {
                        st = st.substring(1, st.length() - 1);
                    }
                    st = st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
                    sb.append(st);
                }
            }
        }
        return sb.toString();
    }

    @Exclude
    public String getHighlightedSentenceHtml(String highlightWord){
        StringBuilder sb = new StringBuilder();
        try {
            String[] sents = getSentencesArray();
            if(sents != null && sents.length > 0) {
                sb.append("<b>Sentences:</b>");
                for (int i=0; i<sents.length; i++) {
                    String sent = sents[i];
                    if(sent.length()>0) {
                        sb.append("<br><b>");
                        sb.append(i+1);
                        sb.append(".</b> ");
                        String st = sent;
                        if (st.length() > 2 && st.charAt(0) == '‘' && st.charAt(st.length() - 1) == '’') {
                            st = st.substring(1, st.length() - 1);
                        }
                        st = Highlighter.highlight(st, highlightWord);
                        st = st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
                        sb.append(st);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Exclude
    public String getHighlightedFirstSentence(String highlightWord, int index){
        StringBuilder sb = new StringBuilder();
        try {
            String[] sents = getSentencesArray();
            if(sents != null && sents.length > 0) {
                for (int i=0; i<1; i++) {
                    String sent = sents[i];
                    if(sent.length()>0) {
                        sb.append("<b>");
                        sb.append(index);
                        sb.append(".</b> ");
                        String st = sent;
                        if (st.length() > 2 && st.charAt(0) == '‘' && st.charAt(st.length() - 1) == '’') {
                            st = st.substring(1, st.length() - 1);
                        }
                        st = Highlighter.highlight(st, highlightWord);
                        st = st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
                        sb.append(st);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    @Exclude
    public String getFirstSentence(int index){
        StringBuilder sb = new StringBuilder();
        try {
            String[] sents = getSentencesArray();
            if(sents != null && sents.length > 0) {
                for (int i=0; i<1; i++) {
                    String sent = sents[i];
                    if(sent.length()>0) {
                        sb.append("<b>");
                        sb.append(index);
                        sb.append(".</b> ");
                        String st = sent;
                        if (st.length() > 2 && st.charAt(0) == '‘' && st.charAt(st.length() - 1) == '’') {
                            st = st.substring(1, st.length() - 1);
                        }
                        st = st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
                        sb.append(st);
                    }
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return sb.toString();
    }

    public boolean haveMoreData(){
        return (syns != null && syns.length() > 1) || (sentences != null && sentences.length() > 1);
    }
}
