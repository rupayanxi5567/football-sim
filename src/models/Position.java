package models;

public enum Positions {
    GK, CB, LB,RB,LWB,RWB,CMF,DMF,AMF,LMF,RMF,LWF,RWF,SS,CF;

    public boolean isForward(){
        return this==CF || this==SS || this==RWF || this==LWF;
    }

    public boolean isMidFielder(){
        return this == AMF || this == CMF || this == DMF || this == LMF || this == RMF;
    }

    public boolean isDefender(){
        return this == CB || this == LB || this == RB || this == LWB || this == RWB;
    }
}
