package com.example.fahim.gremate.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.WordSetWithId;
import com.example.fahim.gremate.ListActivity;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.WordSetActivity;

import java.util.ArrayList;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class WordSetAdapter extends RecyclerView.Adapter<WordSetAdapter.WSViewHolder> {


    private ArrayList<WordSetWithId> wsList;
    private Context context;
    private String lastSetId;

    public WordSetAdapter(ArrayList<WordSetWithId> wordSetList, Context context, String lastSetId) {
        this.wsList = wordSetList;
        this.context = context;
        this.lastSetId = lastSetId;
    }

    @Override
    public WSViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new WSViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(WSViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        WordSetWithId ws = wsList.get(position);
        holder.wordSet.setText(ws.getName());
        holder.wordSet.setTextColor(context.getResources().getColor(R.color.darkFore1));
        if (wsList.get(position).getId().equals(lastSetId)) {
            holder.cv.setCardBackgroundColor(context.getResources().getColor(R.color.darkBack4));
            holder.delBtn.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBack4)));
        } else {
            holder.cv.setCardBackgroundColor(context.getResources().getColor(R.color.darkBack5));
            holder.delBtn.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBack5)));
        }
        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete this word set?")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = wsList.get(position).getName();
                                DB.deleteList(wsList.get(position).getId(), wsList.get(position).getMainList(), true);
                                Toast.makeText(context,
                                        "Word set " + name + " deleted", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
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
        });
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DB.setLastWordSetId(wsList.get(position).getId());
                Intent intent = new Intent(context, ListActivity.class);
                intent.putExtra("wsId", wsList.get(position).getId());
                intent.putExtra("mainListId", wsList.get(position).getMainList());
                intent.putExtra("wordSetTitle", wsList.get(position).getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return wsList.size();
    }


    static class WSViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView wordSet;
        ImageButton delBtn;

        WSViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardItem);
            wordSet = (TextView) itemView.findViewById(R.id.name);
            delBtn = (ImageButton) itemView.findViewById(R.id.delete);
        }
    }
}
