package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.PracticePreviousQuestions;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.SearchActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Fahim on 02-Mar-18.
 */

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {


    private Context context;
    private HashMap<String, Word> words;
    private ArrayList<PracticePreviousQuestions> previousQuestions;
    private float textSize;

    public QuestionAdapter(Context context, HashMap<String, Word> words,
                           ArrayList<PracticePreviousQuestions> previousQuestions, float textSize){

        this.context = context;
        this.words = words;
        this.previousQuestions = previousQuestions;
        this.textSize = textSize;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.previous_question_entry, parent, false);
        return new QuestionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final QuestionViewHolder holder, int position) {
        PracticePreviousQuestions pq = previousQuestions.get(position);
        holder.word.setText("" + (previousQuestions.size() - position) + ". " + pq.getValue().toUpperCase());
        holder.word.setTextSize(textSize+2);
        if(!pq.getWasCorrect()) holder.word.setTextColor(Color.rgb(140, 0, 1));
        else holder.word.setTextColor(Color.rgb(0, 128, 0));

        StringBuilder sb = new StringBuilder();
        String [] ans = new String[5];
        ans[0] = pq.getAns1();
        ans[1] = pq.getAns2();
        ans[2] = pq.getAns3();
        ans[3] = pq.getAns4();
        ans[4] = pq.getAns5();

        ans[pq.getCorrectIndex()] = "<b>" + ans[pq.getCorrectIndex()] + "</b>";

        for(int i = 0; i < 5; i++){
            if(i > 0) sb.append("<br>");
            sb.append("<b>");
            sb.append(i+1);
            sb.append(". </b>");
            sb.append(ans[i]);
        }

        holder.answer.setText(fromHtml(sb.toString()));
        holder.answer.setTextSize(textSize);

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, SearchActivity.class);

                ArrayList<Word> temp = new ArrayList<>();
                temp.add(words.get(previousQuestions.get(holder.getAdapterPosition()).getId()));
                intent.putParcelableArrayListExtra("words", temp);
                context.startActivity(intent);
            }
        });
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
    public int getItemCount() {
        return previousQuestions.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView word;
        TextView answer;
        ImageButton editBtn;

        public QuestionViewHolder(View itemView) {
            super(itemView);
            word = (TextView) itemView.findViewById(R.id.word);
            answer = (TextView) itemView.findViewById(R.id.answer);
            editBtn = (ImageButton) itemView.findViewById(R.id.edit);
        }
    }
}
