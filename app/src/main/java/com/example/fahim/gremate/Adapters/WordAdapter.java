package com.example.fahim.gremate.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fahim.gremate.DataClasses.DB;
import com.example.fahim.gremate.DataClasses.ListWithId;
import com.example.fahim.gremate.DataClasses.Word;
import com.example.fahim.gremate.DataClasses.WordWithId;
import com.example.fahim.gremate.EditActivity;
import com.example.fahim.gremate.R;
import com.example.fahim.gremate.ShowWordActivity;

import java.util.ArrayList;


public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {


    private ArrayList<WordWithId> wordList;
    private ArrayList<ListWithId> otherLists;

    private Context context;
    private String wsId;
    private String mainListId;
    private String currentListId;

    public WordAdapter(ArrayList<WordWithId> wordList, ArrayList<ListWithId>otherLists, Context context, String wsId, String mainListId, String currentListId) {
        this.wordList = wordList;
        this.otherLists = otherLists;
        this.context = context;
        this.wsId = wsId;
        this.mainListId = mainListId;
        this.currentListId = currentListId;
    }

    @Override
    public WordViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wordcard, parent, false);
        WordViewHolder wv = new WordViewHolder(v);
        return wv;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(WordViewHolder holder, final int position) {
        holder.wordValue.setText(wordList.get(position).getValue().toUpperCase());
        String text = ""+(position+1) + " - Source List: ";
        if (!wordList.get(position).isClone() || currentListId.equals(mainListId))
            text += wordList.get(position).getSourceListName();
        else text += wordList.get(position).getSourceListName() + " (c)";

        holder.sourceListName.setText(text);

        holder.cv.setCardBackgroundColor(context.getResources().getColor(R.color.darkBack4));
        holder.moreBtn.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.darkBack4)));

        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, ShowWordActivity.class);

                ArrayList<Word> words = new ArrayList<>();
                for (int i = 0; i < wordList.size(); i++) {
                    Word w = wordList.get(i).toWord();
                    words.add(w);
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("index", position);
                editor.apply();
                intent.putParcelableArrayListExtra("words", words);
                intent.putExtra("listId", currentListId);
                intent.putExtra("wsId", wsId);
                intent.putExtra("mainListId", mainListId);
                context.startActivity(intent);
            }
        });

        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                final WordWithId _word = wordList.get(position);
                builder.setTitle(_word.getValue().toUpperCase());

                CharSequence[] listNames;
                if (!_word.isClone() || currentListId.equals(mainListId))
                    listNames = new CharSequence[]{"Add to another list", "Edit", "Delete"};
                else
                    listNames = new CharSequence[]{"Add to another list", "Edit", "Remove from list", "Delete"};

                builder.setItems(listNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) {
                            if (otherLists == null || otherLists.size() == 0) {
                                Toast.makeText(context,
                                        "Please create a new list first!", Toast.LENGTH_LONG).show();
                                return;
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                            builder.setTitle("Select a list");
                            CharSequence[] listNames = new CharSequence[otherLists.size()];
                            for (int j = 0; j < otherLists.size(); j++) {
                                listNames[j] = otherLists.get(j).getName();
                            }
                            builder.setItems(listNames, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    WordWithId wid = wordList.get(position);
                                    Word nWord = wid.toWord();

                                    Toast.makeText(context,
                                            _word.getValue() + " added to " + otherLists.get(i).getName(), Toast.LENGTH_SHORT).show();
                                    DB.addWordToAnotherList(wsId, otherLists.get(i).getId(), nWord);
                                }
                            });
                            final AlertDialog alert = builder.create();
                            alert.setOnShowListener( new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface arg0) {
                                    if ((alert.findViewById(android.R.id.message)) != null) {
                                        ((TextView)alert.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                                    }
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                                }
                            });
                            alert.show();
                        } else if (i == 1) {
                            Intent intent = new Intent(context, EditActivity.class);

                            String id = wordList.get(position).getCloneOf();
                            intent.putExtra("word", wordList.get(position).toWord());
                            intent.putExtra("wsId", wsId);
                            intent.putExtra("wordId", id);
                            context.startActivity(intent);

                            return;
                        } else {
                            if (!_word.isClone() || currentListId.equals(mainListId)) {
                                final AlertDialog alert = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                                        .setTitle("Confirm Delete")
                                        .setMessage("Are you sure you want to delete this word?")
                                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                try {
                                                    String wid = _word.getCloneOf();
                                                    DB.deleteWord(wid, false);
                                                    Toast.makeText(context,
                                                            "Deleted " + _word.getValue(), Toast.LENGTH_LONG).show();
                                                } catch (Exception e) {
                                                    Toast.makeText(context,
                                                            "Error deleting... " + _word.getValue(), Toast.LENGTH_LONG).show();
                                                    e.printStackTrace();
                                                }

                                            }
                                        })
                                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                            }
                                        }).create();
                                alert.setOnShowListener( new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface arg0) {
                                        if ((alert.findViewById(android.R.id.message)) != null) {
                                            ((TextView)alert.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                                        }
                                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                                    }
                                });

                                alert.show();

                            } else {
                                if (i == 2) {
                                    final AlertDialog alert = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                                            .setTitle("Confirm Remove")
                                            .setMessage("Are you sure you want to remove this word from this list?")
                                            .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    DB.removeWordClone(currentListId, _word.getCloneOf(), _word.getId());
                                                    Toast.makeText(context,
                                                            "Removed " + _word.getValue() + " from this list", Toast.LENGTH_LONG).show();
                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            }).create();

                                    alert.setOnShowListener( new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface arg0) {
                                            if ((alert.findViewById(android.R.id.message)) != null) {
                                                ((TextView)alert.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                                            }
                                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                                        }
                                    });

                                    alert.show();

                                } else {
                                     final AlertDialog alert = new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                                            .setTitle("Confirm Delete")
                                            .setMessage("Are you sure you want to delete this word?")
                                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    try {
                                                        DB.deleteWord(_word.getCloneOf(), false);
                                                        Toast.makeText(context,
                                                                "Deleted " + _word.getValue(), Toast.LENGTH_LONG).show();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        Toast.makeText(context,
                                                                "Error deleting... " + _word.getValue(), Toast.LENGTH_LONG).show();
                                                    }

                                                }
                                            })
                                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                }
                                            }).create();

                                    alert.setOnShowListener( new DialogInterface.OnShowListener() {
                                        @Override
                                        public void onShow(DialogInterface arg0) {
                                            if ((alert.findViewById(android.R.id.message)) != null) {
                                                ((TextView)alert.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                                            }
                                            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                                            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                                        }
                                    });

                                    alert.show();
                                }
                            }
                        }
                    }
                });
                final AlertDialog alert = builder.create();
                alert.setOnShowListener( new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface arg0) {
                        if ((alert.findViewById(android.R.id.message)) != null) {
                            ((TextView)alert.findViewById(android.R.id.message)).setLineSpacing(0.0f, 1.15f);
                        }
                        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTypeface(null, Typeface.BOLD);
                        alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTypeface(null, Typeface.BOLD);
                    }
                });

                alert.show();
            }
        });


        int lvl = wordList.get(position).getLevel();
        if(wordList.get(position).isPracticable()) {
            switch (lvl) {
                case Word.LVL_EASY:
                    holder.img.setImageResource(R.drawable.easy);
                    break;
                case Word.LVL_NORMAL:
                    holder.img.setImageResource(R.drawable.normal);
                    break;
                case Word.LVL_HARD:
                    holder.img.setImageResource(R.drawable.hard);
                    break;
                case Word.LVL_VHARD:
                    holder.img.setImageResource(R.drawable.vhard);
            }
        }
        else{
            holder.img.setImageResource(R.drawable.grey);
        }
    }

    @Override
    public int getItemCount() {
        return wordList.size();
    }


    public static class WordViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView wordValue;
        TextView sourceListName;
        AppCompatImageButton moreBtn;
        ImageView img;

        public WordViewHolder(View itemView) {
            super(itemView);

            cv = (CardView) itemView.findViewById(R.id.wordCV);
            wordValue = (TextView) itemView.findViewById(R.id.wordValue);
            sourceListName = (TextView) itemView.findViewById(R.id.sourceListName);
            moreBtn = (AppCompatImageButton) itemView.findViewById(R.id.moreBtn);
            img = (ImageView) itemView.findViewById(R.id.img);
        }
    }
}
