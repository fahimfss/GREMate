package com.example.fahim.gremate.DataClasses;

public class ShortData {
    String des, syns, sentences;

    public ShortData(String des, String syns, String sentences) {
        this.des = des;
        this.syns = syns;
        this.sentences = sentences;
    }

    public ShortData() {
        this.des = "";
        this.syns = "";
        this.sentences = "";
    }

    public ShortData(WordAllData wordAllData) {
        this.des = wordAllData.getWordData().getDes().split("[\\r\\n]+")[0];;
        StringBuilder sbSyn = new StringBuilder();
        StringBuilder sbSen = new StringBuilder();

        for(WordDef def: wordAllData.getWordDefs()){
            String syn = def.getBoldSyns();
            String[] sen = def.getSentencesArray();

            if(syn!=null && syn.length()>0) {
                if(sbSyn.length()>0)sbSyn.append(DB.DELIM);
                sbSyn.append(syn);
            }

            if(sen!=null && sen[0]!=null && sen[0].length()>0) {
                if(sbSen.length()>0)sbSen.append(DB.DELIM);
                sbSen.append(sen[0]);
            }
        }

        this.syns = sbSyn.toString();
        this.sentences = sbSen.toString();
    }


    public String getDes() {
        return des;
    }

    public void setDes(String des) {
        this.des = des;
    }

    public String getSyns() {
        return syns;
    }

    public void setSyns(String syns) {
        this.syns = syns;
    }

    public String getSentences() {
        return sentences;
    }

    public void setSentences(String sentences) {
        this.sentences = sentences;
    }
}
