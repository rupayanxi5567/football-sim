package models;

public enum Position {
    GK, CB, LB, RB, LWB, RWB, CMF, DMF, AMF, LMF, RMF, LWF, RWF, SS, CF;

    public boolean isForward() {
        return this == CF || this == SS || this == RWF || this == LWF;
    }

    public boolean isMidFielder() {
        return this == CMF || this == DMF || this == LMF || this == RMF;
    }

    public boolean isAttackingMidFielder() {
        return this == AMF;
    }

    public boolean isDefender() {
        return this == CB || this == LB || this == RB || this == LWB || this == RWB;
    }

    public boolean isGoalkeeper() {
        return this == GK;
    }

    public double attackWeight() {
        switch (this) {
            case CF: return 1.0;
            case SS: return 0.9;
            case LWF: case RWF: return 0.7;
            case AMF: return 0.6;
            case LMF: case RMF: return 0.5;
            case CMF: return 0.35;
            case DMF: return 0.2;
            case GK: return 0.0;
            default: return 0.1;
        }
    }




    private int line() {
        if (isDefender()) return 0;
        if (isAttackingMidFielder()) return 2;
        if (isMidFielder()) return 1;
        return 3;
    }

    private static boolean pair(Position a, Position b, Position x, Position y) {
        return (a == x && b == y) || (a == y && b == x);
    }


    private static boolean close(Position a, Position b) {
        return pair(a, b, LB, LWB) || pair(a, b, RB, RWB)
                || pair(a, b, LWB, LMF) || pair(a, b, RWB, RMF)
                || pair(a, b, CMF, DMF) || pair(a, b, CMF, AMF)
                || pair(a, b, SS, CF) || pair(a, b, SS, AMF);
    }


    public int penaltyFor(Position slot) {
        if (this == slot) return 0;
        if (this == GK || slot == GK) return 45;
        if (close(this, slot)) return 3;
        if (pair(this, slot, LMF, LWF) || pair(this, slot, RMF, RWF)) return 7;
        int diff = Math.abs(this.line() - slot.line());
        return diff == 0 ? 6 : diff * 8;
    }
}
