package com.example.fahim.gremate.DataClasses;

import android.content.Context;

import com.example.fahim.gremate.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Fahim on 06-Jan-17.
 */

public class FeedTestData {

    public ArrayList<String> getWords(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.words2);
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);

        ArrayList<String> ret = new ArrayList<>();
        try {
            String line;
            while (( line = br.readLine()) != null) {
                ret.add(line);
            }

            br.close();
        } catch (IOException e) {
            return null;
        }
        return ret;
    }

    public ArrayList<String> getNewWords(Context context) {
        InputStream is = context.getResources().openRawResource(R.raw.new_words);
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);

        ArrayList<String> ret = new ArrayList<>();
        try {
            String line;
            while (( line = br.readLine()) != null) {
                ret.add(line);
            }

            br.close();
        } catch (IOException e) {
            return null;
        }
        return ret;
    }

    public String[][] getPracticeWords(Context context) {
        String[][] s = new String[1535][2];
        InputStream is = context.getResources().openRawResource(R.raw.practice_words);
        InputStreamReader ir = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(ir);
        try {
            String line;
            int i = 0;
            while (( line = br.readLine()) != null) {
                String[] st = line.split(DB.DELIM);
                s[i][0] = st[0];
                s[i++][1] = st[1];
            }
            br.close();
        } catch (IOException e) {
            return null;
        }

        return s;
    }

}
