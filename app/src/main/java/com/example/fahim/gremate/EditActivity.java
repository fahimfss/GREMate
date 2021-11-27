package com.example.fahim.gremate;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.DBRef;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordAllData;
import com.example.fahim.gremate.DataClasses.WordData;
import com.example.fahim.gremate.DataClasses.WordDef;
import com.example.fahim.gremate.DataClasses.WordPractice;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    private String wordId;
    private String pronunciationLink;

    private Word WORD;

    private int dummyHeight;
    private int delbtnSize;

    private WordAllData wordAllData;

    private ArrayList<DefinitionView> definitionViews;

    private int definitionViewID;
    private EditText description;
    private EditText extraInfo;

    LinearLayout defsLL;
    LinearLayout desLL;
    LinearLayout eiLL;
    LinearLayout ll1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        wordAllData = new WordAllData();

        Bundle extras = getIntent().getExtras();
        if (extras == null) finish();

        WORD = extras.getParcelable("word");
        if(WORD == null) finish();

        wordAllData.setWord(WORD);
        wordId = extras.getString("wordId");

        final float scale = this.getResources().getDisplayMetrics().density;
        delbtnSize = (int) (45 * scale);

        definitionViews = new ArrayList<>();

        definitionViewID = 100000;
        dummyHeight = 65;

        defsLL = (LinearLayout) findViewById(R.id.WordOperationLLDef);
        desLL = (LinearLayout) findViewById(R.id.WordOperationLLDes);
        eiLL = (LinearLayout) findViewById(R.id.WordOperationLLExtraInfo);
        ll1 = (LinearLayout) findViewById(R.id.WordOperationLL);

        setTitle(Html.fromHtml("<font color='#BDCBDA'>GREMate</font>"));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        editWordSetup();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void editWordSetup() {
        DBRef db = new DBRef();
        TextView wrd = (TextView) findViewById(R.id.WordOperationWord);
        wrd.setText(wordAllData.getWord().getValue().toUpperCase());

        final DatabaseReference ref1 = db.wordDataRef(wordId);
        ref1.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) return;
                try{
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        WordData wd = ds.getValue(WordData.class);
                        wordAllData.setWordData(wd);
                        if (wordAllData.getWordData().getDes().length() > 0) {
                            desLL.addView(addDescLL(true, wordAllData.getWordData().getDes()));
                        }
                        if (wordAllData.getWordData().getExtraInfo().length() > 0) {
                            eiLL.addView(addEiLL(true, wordAllData.getWordData().getExtraInfo()));
                        }
                        pronunciationLink = "";
                        pronunciationLink = wd.getPronunciation();
                        ref1.removeEventListener(this);
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        });

        final DatabaseReference ref2 = db.wordDefinitionRef(wordId);
        ref2.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) return;
                ArrayList<WordDef> wordDefs = new ArrayList<WordDef>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    WordDef w = ds.getValue(WordDef.class);
                    wordDefs.add(w);
                }
                wordAllData.setWordDefs(wordDefs);
                if (wordDefs.size() > 0) {
                    for (int i = 0; i < wordDefs.size(); i++) {
                        LinearLayout ll = addDefiLL(true, wordDefs.get(i));
                        defsLL.addView(ll, defsLL.getChildCount() - 1);
                    }
                }
                ref2.removeEventListener(this);
            }
            @Override
            public void onCancelled(DatabaseError databaseError){}
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void addDesc(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addDescLL(false, null), llp.getChildCount() - 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private LinearLayout addDescLL(boolean flg, String des) {

        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvDesc = new TextView(EditActivity.this);
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDesc.setText("Description");
        EditText edDesc = new EditText(EditActivity.this);
        edDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) edDesc.setText(des);

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        delBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkFore4)));
        delBtn.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout) v.getParent()).setVisibility(View.GONE);
                description = null;
                Button btnDes = (Button) findViewById(R.id.WordOperationBtnDes);
                btnDes.setVisibility(View.VISIBLE);
            }
        });

        Button btnDes = (Button) findViewById(R.id.WordOperationBtnDes);
        btnDes.setVisibility(View.GONE);

        description = edDesc;

        ll.addView(tvDesc);
        ll.addView(edDesc);
        ll.addView(delBtn);

        return ll;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void addEi(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addEiLL(false, null), llp.getChildCount() - 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private LinearLayout addEiLL(boolean flg, String ei) {

        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView tvDesc = new TextView(EditActivity.this);
        tvDesc.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvDesc.setText("Extra Information");
        EditText etEi = new EditText(EditActivity.this);
        etEi.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        if (flg) etEi.setText(ei);

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        delBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkFore4)));
        delBtn.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout) v.getParent()).setVisibility(View.GONE);
                extraInfo = null;
                Button btnMN = (Button) findViewById(R.id.WordOperationBtnEI);
                btnMN.setVisibility(View.VISIBLE);
            }
        });

        Button btnMn = (Button) findViewById(R.id.WordOperationBtnEI);
        btnMn.setVisibility(View.GONE);

        extraInfo = etEi;

        ll.addView(tvDesc);
        ll.addView(etEi);
        ll.addView(delBtn);

        return ll;
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void addDefi(View v) {
        LinearLayout llp = (LinearLayout) v.getParent();
        llp.addView(addDefiLL(false, null), llp.getChildCount() - 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private LinearLayout addDefiLL(boolean flg, WordDef defi) {

        DefinitionView dv = new DefinitionView();

        LinearLayout ll = getLinearLayout();
        View v = new View(EditActivity.this);
        v.setLayoutParams(new ActionBar.LayoutParams(0, dummyHeight));

        TextView tvTitle = getTextView("Title"), tvDef = getTextView("Definition"), tvPrimSyn = getTextView("Primary Synonym"),
                tvSyn = getTextView("Synonyms"), tvSent = getTextView("Sentences");

        if(flg){
            dv.edTitle.setText(defi.getTitle());
            dv.edDef.setText(defi.getDef());
            dv.edPrimSyn.setText(defi.getBoldSyns().replaceAll(DB.DELIM, ", "));
            dv.edSyn.setText(defi.getSyns().replaceAll(DB.DELIM, ", "));
            String [] sents = defi.getSentencesArray();
            if(sents != null) for(int i=0; i<sents.length; i++)dv.edSents[i].setText(sents[i]);
        }

        ImageButton delBtn = new ImageButton(EditActivity.this);
        delBtn.setImageResource(R.drawable.deltbtn);
        delBtn.setScaleType(android.widget.ImageView.ScaleType.CENTER_INSIDE);
        delBtn.setLayoutParams(new LinearLayout.LayoutParams(delbtnSize, delbtnSize));
        delBtn.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.darkFore4)));
        delBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));

        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId(), i;
                LinearLayout llp = (LinearLayout) v.getParent();
                llp.setVisibility(View.GONE);

                for (i = 0; i < definitionViews.size(); i++) {
                    if (definitionViews.get(i).slNo == id) break;
                }
                if (i != definitionViews.size()) definitionViews.remove(i);

                if (definitionViews.size() == 0) {
                    Button b = (Button) findViewById(R.id.WordOperationBtnDef);
                    b.setText("ADD DEFINITION");
                }
            }
        });

        Button b = (Button) findViewById(R.id.WordOperationBtnDef);
        b.setText("ADD ANOTHER DEFINITION");

        delBtn.setId(definitionViewID);
        delBtn.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        dv.slNo = definitionViewID++;

        definitionViews.add(dv);

        ll.addView(v);
        ll.addView(tvTitle);
        ll.addView(dv.edTitle);
        ll.addView(tvDef);
        ll.addView(dv.edDef);
        ll.addView(tvPrimSyn);
        ll.addView(dv.edPrimSyn);
        ll.addView(tvSyn);
        ll.addView(dv.edSyn);
        ll.addView(tvSent);
        ll.addView(dv.edSents[0]);
        ll.addView(dv.edSents[1]);
        ll.addView(dv.edSents[2]);
        ll.addView(dv.edSents[3]);
        ll.addView(delBtn);

        return ll;
    }

    public LinearLayout getLinearLayout(){
        LinearLayout ll = new LinearLayout(EditActivity.this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return ll;
    }

    public TextView getTextView(String text){
        TextView t = new TextView(EditActivity.this);
        t.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        t.setText(text);
        return t;
    }

    public EditText getEditText(){
        EditText e = new EditText(EditActivity.this);
        e.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        return e;
    }

    public void save(View v) {
        boolean practicable = false;
        for (DefinitionView dv : definitionViews) {
            String df = dv.edDef.getText().toString().replaceAll("\\s", "");
            if (df.length() > 0) {
                practicable = true;
                break;
            }
        }
        if (practicable) saveData();
        else {

            final AlertDialog dialog = new AlertDialog.Builder(EditActivity.this,  R.style.AlertDialogTheme)
                    .setTitle("Word is not practicable!")
                    .setMessage("Please add a definition of this word to make it practicable.")
                    .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton("Skip", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveData();
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
    }

    public void saveData() {
        try {
            DB.deleteWord(wordId, true);

            boolean practicable = false;
            for (DefinitionView dv : definitionViews) {
                String df = dv.edDef.getText().toString().replaceAll("\\s", "");
                if (df.length() > 0) practicable = true;
            }

            ArrayList<WordDef> defs = new ArrayList<>();
            StringBuilder pSyns = new StringBuilder();
            StringBuilder pdefs = new StringBuilder();

            for (DefinitionView dv : definitionViews) {
                if (dv.edTitle.getText().length() > 0 ||
                        dv.edDef.getText().length() > 0 ||
                        dv.edPrimSyn.getText().length() > 0 ||
                        dv.edSyn.getText().length() > 0) {
                    WordDef d = new WordDef();
                    d.setTitle(dv.edTitle.getText().toString());

                    String def = dv.edDef.getText().toString();
                    if(def.length()>0){
                        if(pdefs.length()>0)pdefs.append(DB.DELIM);
                        pdefs.append(def);
                    }
                    d.setDef(def);


                    String bSyns = dv.edPrimSyn.getText().toString().replaceAll(", ", DB.DELIM);
                    if(bSyns.length()>0){
                        if(pSyns.length()>0)pSyns.append(DB.DELIM);
                        pSyns.append(bSyns);
                    }
                    d.setBoldSyns(bSyns);

                    d.setSyns(dv.edSyn.getText().toString().replaceAll(", ", DB.DELIM));

                    StringBuilder sb = new StringBuilder();
                    for(int i=0; i<4; i++){
                        String s = dv.edSents[i].getText().toString();
                        if(s.length()>0){
                            if(sb.length()>0)sb.append(DB.DELIM);
                            sb.append(s);
                        }
                    }
                    d.setSentences(sb.toString());
                    defs.add(d);
                }
            }
            wordAllData.setWordDefs(defs);
            wordAllData.getWord().setValidity(1);

            WordData wordData = new WordData();

            if (description == null)
                wordData.setDes("");
            else
                wordData.setDes(description.getText().toString());

            if (extraInfo == null)
                wordData.setExtraInfo("");
            else
                wordData.setExtraInfo(extraInfo.getText().toString());

            wordData.setPronunciation(pronunciationLink);


            wordAllData.setWordData(wordData);

            wordAllData.getWord().setPracticable(practicable);

            DB.setWordData(wordAllData,
                    new WordPractice(WORD.getValue(), pSyns.toString(), pdefs.toString(), pronunciationLink), wordId);

            Toast.makeText(EditActivity.this, "Saved", Toast.LENGTH_SHORT).show();

            finish();
        } catch (Exception e) {
            Toast.makeText(EditActivity.this, "Edit Failed!", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
    }

    private class DefinitionView {
        public EditText edTitle;
        public EditText edDef;
        public EditText edPrimSyn;
        public EditText edSyn;
        public EditText[] edSents;

        public DefinitionView(){
            edTitle = getEditText();
            edDef = getEditText();
            edPrimSyn = getEditText();
            edSyn = getEditText();
            edSents = new EditText[4];
            for(int i=0; i<4; i++){
                edSents[i] = getEditText();
                edSents[i].setHint("Sentence " + (i+1));
            }
        }

        public int slNo;
    }
}
