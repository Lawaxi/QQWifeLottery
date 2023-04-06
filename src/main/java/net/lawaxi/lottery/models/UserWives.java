package net.lawaxi.lottery.models;

import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class UserWives {
    private final HashMap<Long,Integer> a;

    public UserWives() {
        this.a = new HashMap<>();
    }

    public void put(long key, int w){
        a.put(key,w);
    }

    public void add(long key){
        if(!a.containsKey(key))
            a.put(key,1);

        else
            a.put(key,a.get(key)+1);
    }

    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        for(Long key : a.keySet()){
            o.set(String.valueOf(key),a.get(key));
        }
        return o.toString();
    }

    public Set<Long> keySet(){
        return a.keySet();
    }

    public int get(long key){
        return a.get(key);
    }
}
