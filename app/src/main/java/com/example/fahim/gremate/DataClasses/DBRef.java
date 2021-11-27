package com.example.fahim.gremate.DataClasses;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

/**
 * Created by Fahim on 31-Aug-17.
 */

//        DatabaseReference UW_USER = FirebaseDatabase.getInstance().getReference().child("UserWords").child(uId);
//        USER_ID = uId;
//        USER_DATA = FirebaseDatabase.getInstance().getReference().child("UserData");
//        USER_WORD = FirebaseDatabase.getInstance().getReference().child("UserWords");
//        WORD_SET = UW_USER.child("WordSet");
//        WORD_LIST = UW_USER.child("List");
//        WORD = UW_USER.child("Word");
//        WORD_DATA = UW_USER.child("WordData");
//        WORD_DEF = UW_USER.child("WordDef");
//        SENTENCE = UW_USER.child("Sentence");
//        IMAGE = UW_USER.child("Image");
//        WORD_CLONES = UW_USER.child("WordClones");
//        LAST_LIST = UW_USER.child("LastListId");
//        LAST_SET = UW_USER.child("LastWordSetId");

//        public static DatabaseReference USER_DATA;
//        public static DatabaseReference USER_WORD;
//        public static DatabaseReference WORD_SET;
//        public static DatabaseReference WORD_LIST;
//        public static DatabaseReference WORD;
//        public static DatabaseReference WORD_DATA;
//        public static DatabaseReference WORD_DEF;
//        public static DatabaseReference SENTENCE;
//        public static DatabaseReference IMAGE;
//        public static DatabaseReference WORD_CLONES;
//        public static DatabaseReference LAST_LIST;
//        public static DatabaseReference LAST_SET;


//        USER_DATA = db.getReference().child("UserData");
//        USER_WORD = db.getReference().child("UserWords");
//        WORD_SET = USER_WORD.child(USER_ID).child("WordSet");
//        WORD_LIST = USER_WORD.child(USER_ID).child("List");
//        WORD = USER_WORD.child(USER_ID).child("Word");
//        WORD_DATA = USER_WORD.child(USER_ID).child("WordData");
//        WORD_DEF = USER_WORD.child(USER_ID).child("WordDef");
//        SENTENCE = USER_WORD.child(USER_ID).child("Sentence");
//        IMAGE = USER_WORD.child(USER_ID).child("Image");
//        WORD_CLONES = USER_WORD.child(USER_ID).child("WordClones");
//        LAST_LIST = USER_WORD.child(USER_ID).child("LastListId");
//        LAST_SET = USER_WORD.child(USER_ID).child("LastWordSetId");
//        FirebaseAuth auth = FirebaseAuth.getInstance();
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//        USER_ID = auth.getCurrentUser().getUid();


public class DBRef {
    private final String USER_DATA = "UserData";
    private final String USER_WORD = "UserWords";
    private final String WORD_SET = "WordSet";
    private final String WORD_LIST = "List";
    private final String WORD = "Word";
    private final String WORD_DATA = "WordData";
    private final String WORD_DEF = "WordDef";
    private final String PRACTICE = "Practice";
    private final String WORD_CLONES = "WordClones";
    private final String SHORT_DATA = "ShortData";
    private final String LAST_LIST = "LastListId";
    private final String LAST_SET = "LastWordSetId";

    private String uId;
    private DatabaseReference userWord;

    public DBRef() {
        uId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userWord = FirebaseDatabase.getInstance().getReference().child(USER_WORD).child(uId);
    }

    public DBRef(String uId) {
        this.uId = uId;
        userWord = FirebaseDatabase.getInstance().getReference().child(USER_WORD).child(uId);
    }

    public DatabaseReference head(){
        return userWord;
    }

    String getWordSetKey() {
        return userWord.child(WORD_SET).push().getKey();
    }

    String getListKey(String wsId) {
        return userWord.child(WORD_LIST).child(wsId).push().getKey();
    }

    void setWordSetData(String wsId, WordSet data) {
        userWord.child(WORD_SET).child(wsId).setValue(data);
    }

    void setListData(String wsId, String listId, List data) {
        userWord.child(WORD_LIST).child(wsId).child(listId).setValue(data);
    }

    String getWordId(String listId) {
        return userWord.child(WORD).child(listId).push().getKey();
    }

    void setWordData(String listId, String wordId, Word data) {
        userWord.child(WORD).child(listId).child(wordId).setValue(data);
    }

    void setWordClone(String listId, String wordId, String cloneId) {
        userWord.child(WORD_CLONES).child(wordId).push().setValue(new WordClones(listId, cloneId));
    }

    DatabaseReference listCountRef(String wsId, String listId) {
        return userWord.child(WORD_LIST).child(wsId).child(listId).child("wordCount");
    }

    DatabaseReference wordSetCountRef(String wsId) {
        return userWord.child(WORD_SET).child(wsId).child("wordCount");
    }

    void setWordDataData(String wordId, WordData data) {
        userWord.child(WORD_DATA).child(wordId).push().setValue(data);
    }

    void setWordDefData(String wordId, WordDef data) {
        userWord.child(WORD_DEF).child(wordId).push().setValue(data);
    }

    void setWordPracticeData(String wordId, WordPractice data){
        userWord.child(PRACTICE).child(wordId).setValue(data);
    }

    DatabaseReference wordCloneRef(String wordId) {
        return userWord.child(WORD_CLONES).child(wordId);
    }

    void setWordLevel(String listId, String wordId, int level) {
        userWord.child(WORD).child(listId).child(wordId).child("level").setValue(level);
    }

    void setWordPracticable(String listId, String wordId, boolean practicable) {
        userWord.child(WORD).child(listId).child(wordId).child("practicable").setValue(practicable);
    }

    void setWordValidity(String listId, String wordId, int validity) {
        userWord.child(WORD).child(listId).child(wordId).child("validity").setValue(validity);
    }

    void deleteWordData(String wordId) {
        userWord.child(WORD_DEF).child(wordId).setValue(null);
        userWord.child(WORD_DATA).child(wordId).setValue(null);
    }

    void deleteWord(String listId, String wordId) {
        userWord.child(WORD).child(listId).child(wordId).setValue(null);
    }

    void deleteWordClones(String wordId) {
        userWord.child(WORD_CLONES).child(wordId).setValue(null);
    }

    void setShortData(ShortData shortData, String wordId) {
        userWord.child(SHORT_DATA).child(wordId).setValue(shortData);
    }

    public DatabaseReference shortDataRef(String wordId) {
        return userWord.child(SHORT_DATA).child(wordId);
    }

    void deleteShortData(String wordId) {
        userWord.child(SHORT_DATA).child(wordId).setValue(null);
    }

    void deleteWordSingleClone(String wordId, String cloneKey) {
        userWord.child(WORD_CLONES).child(wordId).child(cloneKey).setValue(null);
    }

    Query getClonesQuery(String wordId, String cloneId) {
        return userWord.child(WORD_CLONES).child(wordId).orderByChild("cloneId").equalTo(cloneId);
    }

    void setLastList(String listId) {
        userWord.child(LAST_LIST).setValue(listId);
    }

    public DatabaseReference lastListRef() {
        return userWord.child(LAST_LIST);
    }

    void setLastWordSet(String wsId) {
        userWord.child(LAST_SET).setValue(wsId);
    }

    public DatabaseReference lastWordSetRef() {
        return userWord.child(LAST_SET);
    }

    void deleteWordSet(String wsId) {
        userWord.child(WORD_SET).child(wsId).setValue(null);
    }

    void deleteWordList(String wsId, String listId, boolean isMainList) {
        if (!isMainList) userWord.child(WORD_LIST).child(wsId).child(listId).setValue(null);
        else userWord.child(WORD_LIST).child(wsId).setValue(null);
    }

    DatabaseReference wordListWordsRef(String listId) {
        return userWord.child(WORD).child(listId);
    }

    public DatabaseReference wordDataRef(String wordId) {
        return userWord.child(WORD_DATA).child(wordId);
    }

    public DatabaseReference wordDataHeadRef() {
        return userWord.child(WORD_DATA);
    }

    public DatabaseReference wordDefinitionRef(String wordId) {
        return userWord.child(WORD_DEF).child(wordId);
    }

    public DatabaseReference wordSetListsRef(String wsId) {
        return userWord.child(WORD_LIST).child(wsId);
    }

    public DatabaseReference listWordsRef(String listId) {
        return userWord.child(WORD).child(listId);
    }

    public DatabaseReference userDataRef() {
        return FirebaseDatabase.getInstance().getReference().child(USER_DATA).child(uId);
    }

    public DatabaseReference wordPracticeRef(String wordId){
        return userWord.child(PRACTICE).child(wordId);
    }

    public DatabaseReference wordSetRef() {
        return userWord.child(WORD_SET);
    }

//    public DatabaseReference wordRef(){
//        return userWord.child(WORD);
//    }
//
//    public DatabaseReference wordDataRef(){
//        return userWord.child(WORD_DATA);
//    }
//
//    public DatabaseReference sdRef(){
//        return userWord.child(WORD_DEF);
//    }

}
