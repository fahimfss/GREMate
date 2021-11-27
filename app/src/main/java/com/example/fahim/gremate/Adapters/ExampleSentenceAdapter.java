package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.fahim.gremate.DataClasses.PracticePreviousQuestions;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.SearchActivity;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Fahim on 02-Mar-18.
 */

public class ExampleSentenceAdapter extends RecyclerView.Adapter<ExampleSentenceAdapter.ExampleSentenceViewHolder> {

    private ArrayList<Pair<String, String>> sentences;
    private float textSize;

    public ExampleSentenceAdapter(ArrayList<Pair<String, String>> sentences, float textSize){
        this.sentences = sentences;
        this.textSize = textSize;
    }

    @Override
    public ExampleSentenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.exmp_sent, parent, false);
        return new ExampleSentenceViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ExampleSentenceViewHolder holder, int position) {

        String sentence = sentences.get(position).first;
        String source = sentences.get(position).second;

        TextView sentTV = holder.sentence;
        TextView srcTV = holder.source;

        sentTV.setTextSize(textSize);
        srcTV.setTextSize(textSize-5);

        sentTV.setText(fromHtml(sentence));
        srcTV.setText(fromHtml(source));
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
        return sentences.size();
    }

    public static class ExampleSentenceViewHolder extends RecyclerView.ViewHolder {
        TextView sentence;
        TextView source;

        public ExampleSentenceViewHolder(View itemView) {
            super(itemView);
            sentence = (TextView) itemView.findViewById(R.id.sentence);
            source = (TextView) itemView.findViewById(R.id.source);
        }
    }
}
