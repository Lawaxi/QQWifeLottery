package net.lawaxi.lottery.models;

import cn.hutool.json.JSONObject;

import java.util.HashMap;

public class Wife {
    private HashMap<Long, Long[]> sense;

    public Wife(HashMap<Long, Long[]> sense) {
        this.sense = sense;
    }

    public int getSenseInGroup(Long group){
        try {
            return Integer.valueOf(String.valueOf(sense.get(group)[1]));
        }catch (Exception e){
            return 0;
        }
    }

    public long getSenseFromInGroup(Long group){
        return sense.get(group)[0];
    }

    public void putSense(Long group, long qqID, int sense){
        this.sense.put(group, new Long[]{qqID, Long.valueOf(sense)});
    }

    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        for(Long key : sense.keySet()){
            o.set(String.valueOf(key),sense.get(key));
        }
        return o.toString();
    }

}
