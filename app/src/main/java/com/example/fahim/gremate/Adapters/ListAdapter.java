package com.example.fahim.gremate.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.ListWithId;
import com.example.fahim.gremate.ListWordsActivity;
import com.example.fahim.gremate.R;

import java.util.ArrayList;

/**
 * Created by Fahim on 24-Dec-16.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder> {


    private ArrayList<ListWithId> lists;
    private Context context;
    private String wsId, mainListId, lastListId;

    public ListAdapter(ArrayList<ListWithId> lists, Context context, String wsId, String mainListId, String lastListId) {
        this.lists = lists;
        this.context = context;
        this.wsId = wsId;
        this.mainListId = mainListId;
        this.lastListId = lastListId;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card, parent, false);
        return new ListViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(ListViewHolder holder, final int position) {
        ListWithId item = lists.get(position);
        holder.listName.setText(item.getName());
        if(lists.get(position).getId().equals(lastListId)){
            holder.cv.setCardBackgroundColor(context.getResources().getColor(R.color.darkBack4));
            holder.delBtn.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBack4)));
        }
        else {
            holder.cv.setCardBackgroundColor(context.getResources().getColor(R.color.darkBack5));
            holder.delBtn.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBack5)));
        }
        holder.delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String listId = lists.get(position).getId();
                final boolean isMainList = (mainListId.equals(listId));
                String showMessage;

                if(isMainList) showMessage = "Are you sure you want to delete this word this list? " +
                        "Note that deleting this list will delete this word set!";
                else
                    showMessage = "Are you sure you want to delete this word list?";

                final AlertDialog dialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                        .setTitle("Confirm Delete")
                        .setMessage(showMessage)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = lists.get(position).getName();

                                DB.deleteList(wsId, listId, isMainList);
                                Toast.makeText(context,
                                        "List " + name + " deleted", Toast.LENGTH_LONG).show();

                                if(isMainList) ((Activity)context).finish();
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
                DB.setLastListId(lists.get(position).getId());

                ArrayList<ListWithId> otherLists = new ArrayList<ListWithId>();
                for(int i=0; i< lists.size(); i++){
                    if(i==position)continue;
                    if(lists.get(i).getId().equals(mainListId))continue;
                    otherLists.add(lists.get(i));
                }
                Intent intent = new Intent(context, ListWordsActivity.class);
                intent.putParcelableArrayListExtra("otherLists", otherLists);
                intent.putExtra("listId", lists.get(position).getId());
                intent.putExtra("wsId", wsId);
                intent.putExtra("mainListId", mainListId);
                intent.putExtra("listTitle", lists.get(position).getName());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class ListViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView listName;
        AppCompatImageButton delBtn;

        public ListViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardItem);
            listName = (TextView) itemView.findViewById(R.id.name);
            delBtn = (AppCompatImageButton) itemView.findViewById(R.id.delete);
        }
    }
}
