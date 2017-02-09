package com.boffinapes.geekcard;

public class GCInfo {
    public String[] cols = new String[24];
    public int id;

    public void fillGCI(int i, String[] stuffer){
        id = i;
        for(int j = 0; j<24; j++){
            cols[j] = stuffer[j];
        }
    }
}
