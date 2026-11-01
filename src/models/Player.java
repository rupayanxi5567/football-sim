package models;

import java.util.Random;

public class Player {
    private final String name;
    private final Position position;
    private int pace;
    private int passing;
    private int shooting;
    private int defending;
    private int stamina;

    public Player(String name, Position position, int pace, int passing, int shooting, int defending, int stamina) {
        this.name = name;
        this.position = position;
        this.pace = clamps(pace);
        this.passing = clamps(passing);
        this.shooting = clamps(shooting);
        this.defending = clamps(defending);
        this.stamina = clamps(stamina);
    }

    public static int clamps(int x){
        return Math.clamp(x, 1, 99);
    }

    public static Player randomPlayer(String pName, Position pos, int level){
        Random rand = new Random((pName+pos).hashCode());
        level = Math.max(1, Math.min(5, level));
        int base = 40 + level * 10;
        int pace = roll(base,rand);
        int pass = roll(base,rand);
        int shooting = roll(base,rand);
        int defending = roll(base,rand);
        int stamina = roll(base,rand);

        if(pos.isDefender()){
            defending+=8;
            shooting=cap(shooting,20,5,rand);
            pass=cap(pass,60,5,rand);
        }else if(pos.isMidFielder()){
            pass+=8;
            stamina+=2;
            shooting=cap(shooting,90,5,rand);
        }else if(pos.isForward()){
            defending=cap(defending,40,5,rand);
            pass=cap(pass,70,5,rand);
            shooting+=8;
        }else if(pos== Position.GK){
            defending+=8;
            shooting=cap(shooting,50,5,rand);
            pass=cap(pass,70,5,rand);

        }

        return new Player(pName,pos,clamps(pace),clamps(pass),clamps(shooting),clamps(defending),clamps(stamina));
    }

    private static int cap(int value, int max, int spread, Random rand) {
        return Math.min(value, max - rand.nextInt(spread + 1));
    }

    private static int roll(int base, Random rand) {
        return base + rand.nextInt(11) - 5;
    }

    public int getOverall() {
        switch (position) {
            case CF: case SS:
                return (pace * 4 + passing * 3 + shooting * 5 + stamina * 3) / 15;
            case CMF: case LMF: case RMF: case AMF:
            case LWF: case RWF: case DMF:
                return (pace * 3 + passing * 4 + shooting * 3 + stamina * 3) / 13;
            case LB: case RB: case CB: case LWB: case RWB:
                return (passing * 3 + defending * 5 + stamina * 3) / 11;
            default:   // GK
                return (passing * 4 + defending * 5) / 9;
        }
    }

    public String getRatingsText() {
        return name + " (" + position + ", OVR " + getOverall() + ")\n"
                + "  Pace      : " + pace + "\n"
                + "  Passing   : " + passing + "\n"
                + "  Shooting  : " + shooting + "\n"
                + "  Defending : " + defending + "\n"
                + "  Stamina   : " + stamina;
    }

// ****************************************************** getter setter below ******************************************************

    public int getDefending() { return defending; }
    public void setDefending(int defending) { this.defending = clamps(defending); }
    public int getStamina() { return stamina; }
    public void setStamina(int stamina) { this.stamina = clamps(stamina); }

    @Override
    public String toString() {
        return name + " (" + position + ", OVR " + getOverall() + ")";
    }
    public String getName() {
        return name;
    }

    public Position getPosition() {
        return position;
    }

    public int getPace() {
        return pace;
    }

    public void setPace(int pace) {
        this.pace = clamps(pace);
    }

    public int getPassing() {
        return passing;
    }

    public void setPassing(int passing) {
        this.passing = clamps(passing);
    }

    public int getShooting() {
        return shooting;
    }

    public void setShooting(int shooting) {
        this.shooting = clamps(shooting);
    }
}
