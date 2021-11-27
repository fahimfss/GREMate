package com.example.fahim.gremate.DataClasses;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class DB {

    public static final String DELIM = ";;,;;";

    private static SparseArray<ValueEventListener> listenerMap = new SparseArray<>();

    private static int listenerCounter = 0;

    private static ArrayList<String> listIds1;

    public static Pair<String, String> newWordSet(final String wsName) {
        DBRef db = new DBRef();
        String wsId = db.getWordSetKey();
        String mainListId = db.getListKey(wsId);
        db.setListData(wsId, mainListId, List.getAllList());
        db.setWordSetData(wsId, WordSet.newWordSet(wsName, mainListId));
        return new Pair<>(wsId, mainListId);
    }

    public static String newList(final String wsId, final String listName) {
        DBRef db = new DBRef();
        String listId = db.getListKey(wsId);
        db.setListData(wsId, listId, List.getNewList(listName));
        return listId;
    }

    public static String newWord(final String wsId, final String listId, final String listName,
                               final String mainListId, final String wordValue) {

        DBRef db = new DBRef();
        String wordId = db.getWordId(listId);
        Word w = Word.newWord(listName, wordId, wordValue);
        db.setWordData(listId, wordId, w);
        db.setWordClone(listId, wordId, wordId);

        if (!mainListId.equals(listId)) {
            addWordToAnotherList(wsId, mainListId, w);
        }

        final int listenerKey1 = ++listenerCounter;
        final DatabaseReference mRef1 = db.listCountRef(wsId, listId);
        ValueEventListener listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef1.setValue(count + 1);
                }
                if(listenerMap.get(listenerKey1)!=null) {
                    mRef1.removeEventListener(listenerMap.get(listenerKey1));
                    listenerMap.remove(listenerKey1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        mRef1.addValueEventListener(listener1);
        listenerMap.put(listenerKey1, listener1);

        final int listenerKey2 = ++listenerCounter;
        final DatabaseReference mRef2 = db.wordSetCountRef(wsId);
        ValueEventListener listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef2.setValue(count + 1);
                }
                if(listenerMap.get(listenerKey2)!=null) {
                    mRef2.removeEventListener(listenerMap.get(listenerKey2));
                    listenerMap.remove(listenerKey2);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef2.addValueEventListener(listener2);
        listenerMap.put(listenerKey2, listener2);

        return wordId;
    }

    public static void addWordToAnotherList(String wsId, String toListId, Word _word) {
        DBRef db = new DBRef();
        String wordId = db.getWordId(toListId);
        db.setWordData(toListId, wordId, _word);
        db.setWordClone(toListId, _word.getCloneOf(), wordId);

        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = db.listCountRef(wsId, toListId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    long count = (long) dataSnapshot.getValue();
                    mRef.setValue(count + 1);
                }
                if(listenerMap.get(listenerKey)!=null) {
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void setWordData(WordAllData wordAllData, WordPractice practice, String wordId) {
        DBRef db = new DBRef();

        boolean isPracticable = wordAllData.getWord().isPracticable();
        setWordPracticable(wordId, isPracticable);
        setWordValidity(wordId, wordAllData.getWord().getValidity());
        setWordLevel(wordId, wordAllData.getWord().getLevel());

        if (wordAllData.getWordData() != null) {
            db.setWordDataData(wordId, wordAllData.getWordData());
        }

        if (wordAllData.getWordDefs() != null) {
            for (WordDef def : wordAllData.getWordDefs()) {
                db.setWordDefData(wordId, def);
            }
        }

        if(isPracticable){
            db.setWordPracticeData(wordId, practice);
        }

        ShortData shortData = new ShortData(wordAllData);
        db.setShortData(shortData, wordId);
    }

    public static void setWordLevel(final String wordId, final int level) {
        final DBRef db = new DBRef();

        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = db.wordCloneRef(wordId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.setWordLevel(wcl.getListId(), wcl.getCloneId(), level);
                }
                if(listenerMap.get(listenerKey)!=null) {
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void setWordPracticable(String wordId, final boolean practicable) {
        final DBRef db = new DBRef();
        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = db.wordCloneRef(wordId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.setWordPracticable(wcl.getListId(), wcl.getCloneId(), practicable);
                }
                if(listenerMap.get(listenerKey)!=null) {
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void setWordValidity(String wordId, final int validity) {
        final DBRef db = new DBRef();
        final int listenerKey = ++listenerCounter;
        final DatabaseReference mRef = db.wordCloneRef(wordId);
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.setWordValidity(wcl.getListId(), wcl.getCloneId(), validity);
                }
                if(listenerMap.get(listenerKey)!=null){
                    mRef.removeEventListener(listenerMap.get(listenerKey));
                    listenerMap.remove(listenerKey);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        mRef.addValueEventListener(listener);
        listenerMap.put(listenerKey, listener);
    }

    public static void deleteWord(final String wordId, boolean isEdit) throws Exception {

        if(wordId.length()<5){
            throw new Exception("Word Length Invalid!");
        }

        final DBRef db = new DBRef();
        db.deleteWordData(wordId);
        db.deleteShortData(wordId);
        if (isEdit) return;

        listIds1 = new ArrayList<>();
        final int listenerKey1 = ++listenerCounter;
        final DatabaseReference mRef1 = db.wordCloneRef(wordId);
        ValueEventListener listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordClones wcl = ds.getValue(WordClones.class);
                    db.deleteWord(wcl.getListId(), wcl.getCloneId());
                    listIds1.add(wcl.getListId());
                }
                db.deleteWordClones(wordId);
                if(listenerMap.get(listenerKey1)!=null){
                    mRef1.removeEventListener(listenerMap.get(listenerKey1));
                    listenerMap.remove(listenerKey1);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        };
        mRef1.addValueEventListener(listener1);
        listenerMap.put(listenerKey1, listener1);
    }

    public static void removeWordClone(final String listId, final String wordId, final String cloneId) {

        final DBRef db = new DBRef();
        db.deleteWord(listId, cloneId);

        db.getClonesQuery(wordId, cloneId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    db.deleteWordSingleClone(wordId, ds.getKey());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public static void deleteList(final String wsId, final String listId, final boolean isMainList) {

        final DBRef db = new DBRef();

        if (isMainList) {
            db.deleteWordSet(wsId);
            db.deleteWordList(wsId, "", true);

            final int listenerKey1 = ++listenerCounter;
            final DatabaseReference mRef1 = db.wordListWordsRef(listId);
            ValueEventListener listener1 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        try {
                            deleteWord(ds.child("cloneOf").getValue().toString(), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if(listenerMap.get(listenerKey1)!=null){
                        mRef1.removeEventListener(listenerMap.get(listenerKey1));
                        listenerMap.remove(listenerKey1);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError){}
            };
            mRef1.addValueEventListener(listener1);
            listenerMap.put(listenerKey1, listener1);

        } else {
            db.deleteWordList(wsId, listId, false);
            final int listenerKey2 = ++listenerCounter;
            final DatabaseReference mRef2 = db.wordListWordsRef(listId);
            ValueEventListener listener2 = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String wordId = ds.getKey();
                        String cloneOf = ds.child("cloneOf").getValue().toString();

                        if (wordId.equals(cloneOf)){
                            try {
                                deleteWord(ds.child("cloneOf").getValue().toString(), false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else removeWordClone(listId, wordId, cloneOf);
                    }
                    if(listenerMap.get(listenerKey2)!=null){
                        mRef2.removeEventListener(listenerMap.get(listenerKey2));
                        listenerMap.remove(listenerKey2);
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError){}
            };
            mRef2.addValueEventListener(listener2);
            listenerMap.put(listenerKey2, listener2);
        }
    }

    public static void setLastListId(String listId){
        DBRef db = new DBRef();
        db.setLastList(listId);
    }

    public static void setLastWordSetId(String wsId){
        DBRef db = new DBRef();
        db.setLastWordSet(wsId);
    }

    public static void setShortData(ShortData shortData, String wordId){
        DBRef db = new DBRef();
        db.setShortData(shortData, wordId);
    }

    public static void initNewUser(String uId, Context context){

        DBRef db = new DBRef(uId);
        final String mainListName = "All Words";
        ArrayList<String> initWords = new FeedTestData().getWords(context);
        int index = initWords.size()-1;

        int wordsetCount = 5;
        int listInWordset = 10;
        int wordsInList = 40;

        for (int wsi = wordsetCount; wsi >= 1; wsi--) {

            String wsId = db.getWordSetKey();
            String mainListId = db.getListKey(wsId);

            db.setWordSetData(wsId, new WordSet("GRE: Word Set "+wsi, mainListId));
            db.setListData(wsId, mainListId, new List(mainListName));

            for (int ls = listInWordset; ls >= 1; ls--) {
                if(wsi == 5 && ls > 8 )continue;
                String listId = db.getListKey(wsId);
                db.setListData(wsId, listId, new List("List "+ls));
                for (int i = 0; i < wordsInList; i++) {
                    String wordValue = initWords.get(index-39+i);
                    String wordId = db.getWordId(mainListId);
                    String cloneId = db.getWordId(listId);
                    db.setWordData(mainListId, wordId, Word.newWord(mainListName, wordId, wordValue));
                    db.setWordData(listId, cloneId, Word.newWord(mainListName, wordId, wordValue));
                    db.setWordClone(listId, wordId, cloneId);
                    db.setWordClone(mainListId, wordId, wordId);

                    try {
                        Log.d("INIT", ""+index + " " + wordValue);
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                index -= 40;
            }
        }
    }

    public static void tempAddWords(String uId, Context context){

        DBRef db = new DBRef(uId);
        final String mainListName = "All Words";
        ArrayList<String> initWords = new FeedTestData().getNewWords(context);
        int index = initWords.size()-1;

        int wordsetCount = 1;
        int listInWordset = 9;
        int wordsInList = 40;

        for (int wsi = wordsetCount; wsi >= 1; wsi--) {

            String wsId = db.getWordSetKey();
            String mainListId = db.getListKey(wsId);

            db.setWordSetData(wsId, new WordSet("GRE: Word Set 6", mainListId));
            db.setListData(wsId, mainListId, new List(mainListName));

            for (int ls = listInWordset; ls >= 1; ls--) {
                String listId = db.getListKey(wsId);
                db.setListData(wsId, listId, new List("List "+ls));
                for (int i = 0; i < wordsInList; i++) {
                    String wordValue = initWords.get(index-39+i);
                    String wordId = db.getWordId(mainListId);
                    String cloneId = db.getWordId(listId);
                    db.setWordData(mainListId, wordId, Word.newWord(mainListName, wordId, wordValue));
                    db.setWordData(listId, cloneId, Word.newWord(mainListName, wordId, wordValue));
                    db.setWordClone(listId, wordId, cloneId);
                    db.setWordClone(mainListId, wordId, wordId);

                    if(index == 0) return;
                    try {
                        Log.d("INIT", ""+index + " " + wordValue);
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                index -= 40;
            }
        }
    }

    public static void initShortData(){

//        final DBRef dbRef = new DBRef();
//        DatabaseReference ref = dbRef.sdRef();
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot sn1: dataSnapshot.getChildren()){
//                    StringBuilder sbSyn = new StringBuilder();
//                    StringBuilder sbSen = new StringBuilder();
//                    String key = sn1.getKey();
//                    Log.d("Short Data", key);
//
//                    for(DataSnapshot sn2: sn1.getChildren()){
//                        WordDef def = sn2.getValue(WordDef.class);
//                        String syn = def.getBoldSyns();
//                        String[] sen = def.getSentencesArray();
//
//                        if(syn!=null && syn.length()>0) {
//                            if(sbSyn.length()>0)sbSyn.append(DELIM);
//                            sbSyn.append(syn);
//                        }
//
//                        if(sen!=null && sen[0]!=null && sen[0].length()>0) {
//                            if(sbSen.length()>0)sbSen.append(DELIM);
//                            sbSen.append(sen[0]);
//                        }
//                    }
//                    DatabaseReference rf = dbRef.shortDataRef(key);
//                    rf.child("syns").setValue(sbSyn.toString());
//                    rf.child("sentences").setValue(sbSen.toString());
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//        DatabaseReference ref1 = dbRef.wordDataRef();
//        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                for(DataSnapshot sn1: dataSnapshot.getChildren()){
//                    String shortDes="";
//                    String key = sn1.getKey();
//                    Log.d("Short Data", key);
//                    for(DataSnapshot sn2: sn1.getChildren()){
//                        WordData dt = sn2.getValue(WordData.class);
//                        shortDes = dt.getDes().split("[\\r\\n]+")[0];
//                    }
//                    DatabaseReference rf = dbRef.shortDataRef(key);
//                    rf.child("des").setValue(shortDes);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
    }

    public static void move_temp(){
        DBRef db = new DBRef();

        /*

        Set 1= -L3hMKkDqlPAEOF33V6a
        Set 1 list = -L3hMKkDqlPAEOF33V6b

        Set 2 = -L3hMFiEirhMr3iVJ4aF
        Set 2 list = -L3hMFiEirhMr3iVJ4aG

        Set 6 = -L94OXaaUBHRuoqVdMGo
        Set 6 list = -L94OXaaUBHRuoqVdMGp

        Set All = -LA6rpF9LX0vwOYCO74n
        Set All list = -LA6rpF9LX0vwOYCO74o

         */

        final DatabaseReference ref1 =  db.listWordsRef("-L3hMKkDqlPAEOF33V6b");
        ValueEventListener listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Word word = ds.getValue(Word.class);
                    addWordToAnotherList("-LA6rpF9LX0vwOYCO74n", "-LA6rpF9LX0vwOYCO74o", word);
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("DONE", "DONE 1");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref1.addValueEventListener(listener1);

        final DatabaseReference ref2 =  db.listWordsRef("-L3hMFiEirhMr3iVJ4aG");
        ValueEventListener listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Word word = ds.getValue(Word.class);
                    addWordToAnotherList("-LA6rpF9LX0vwOYCO74n", "-LA6rpF9LX0vwOYCO74o", word);
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("DONE", "DONE 2");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref2.addValueEventListener(listener2);

        final DatabaseReference ref3 =  db.listWordsRef("-L94OXaaUBHRuoqVdMGp");
        ValueEventListener listener3 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Word word = ds.getValue(Word.class);
                    addWordToAnotherList("-LA6rpF9LX0vwOYCO74n", "-LA6rpF9LX0vwOYCO74o", word);
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                Log.d("DONE", "DONE 3");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ref3.addValueEventListener(listener3);

    }


    public static void pronunciationInPractice(){
        final DBRef db = new DBRef();
        final DatabaseReference ref = db.wordDataHeadRef();
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    try {
                        String key = ds.getKey();
                        String pronunciation = ds.getChildren().iterator()
                                .next().child("pronunciation").getValue().toString();

                        if(pronunciation!=null && pronunciation.length()>0){
                            db.wordPracticeRef(key).child("pronunciation").setValue(pronunciation);
                        }

                        Log.d("pronunciationInPractice", pronunciation);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }

                ref.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
