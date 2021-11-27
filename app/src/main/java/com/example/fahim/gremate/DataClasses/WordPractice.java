package com.example.fahim.gremate.DataClasses;

import com.google.firebase.database.Exclude;

import java.util.Random;

/**
 * Created by Fahim on 24-Jan-18.
 */

public class WordPractice {
    private String word, synonyms, definitions, pronunciation;

    public WordPractice() {
        this.word = "";
        this.synonyms = "";
        this.definitions = "";
        this.pronunciation = "";
    }

    public WordPractice(String word, String synonyms, String definitions, String pronunciation) {
        this.word = word;
        this.synonyms = synonyms;
        this.definitions = definitions;
        this.pronunciation = pronunciation;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(String synonyms) {
        this.synonyms = synonyms;
    }

    public String getDefinitions() {
        return definitions;
    }

    public void setDefinitions(String definitions) {
        this.definitions = definitions;
    }

    public boolean hasSynonyms() {
        return (synonyms != null && synonyms.length() > 0);
    }

    public boolean hasDefinitions(){
        return ( definitions != null && definitions.length()>0);
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }
}
