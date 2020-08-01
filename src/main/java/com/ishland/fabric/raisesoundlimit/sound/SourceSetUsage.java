package com.ishland.fabric.raisesoundlimit.sound;

public class SourceSetUsage {

    private final int used;
    private final int max;

    public SourceSetUsage(int used, int max){
        this.used = used;
        this.max = max;
    }

    public int getUsed() {
        return used;
    }

    public int getMax() {
        return max;
    }
}
