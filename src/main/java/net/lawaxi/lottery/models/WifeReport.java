package net.lawaxi.lottery.models;

public class WifeReport {
    public final String name;
    public int count = 1;
    public int sense = 0;

    public WifeReport(String name, int sense) {
        this.name = name;
        this.sense = sense;
    }

    public void addRecord(int sense) {
        count++;
        if (sense > this.sense) {
            this.sense = sense;
        }
    }
}
