package net.lawaxi.lottery.models;

import cn.hutool.json.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class UserWives {
    private final HashMap<String,Integer> a;

    public UserWives() {
        this.a = new HashMap<>();
    }

    public void put(String key, int w){
        a.put(key,w);
    }

    public void add(String key){
        if(!a.containsKey(key))
            a.put(key,1);

        else
            a.put(key,a.get(key)+1);
    }

    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        for(String key : a.keySet()){
            o.set(String.valueOf(key),a.get(key));
        }
        return o.toString();
    }

    public Set<String> keySet(){
        return a.keySet();
    }

    public int get(String key){
        return a.get(key);
    }
}
