package models;

public class Formation {
    private String label;
    private final int defenders;
    private final int forwards;
    private final int midfielders;
    private final int attackingMidfielders;

    public Formation(int defenders, int midfielders, int attackingMidfielders, int forwards) {
        this.defenders = defenders;
        this.forwards = forwards;
        this.midfielders = midfielders;
        this.attackingMidfielders = attackingMidfielders;

        this.label = attackingMidfielders > 0
                ? defenders + "-" + midfielders + "-" + attackingMidfielders + "-" + forwards
                : defenders + "-" + midfielders + "-" + forwards;
    }

    public static final Formation F_4312 = new Formation(4,3,1,2);
    public static final Formation F_4321 = new Formation(4,3,2,1);
    public static final Formation F_3421 = new Formation(3,4,2,1);
    public static final Formation F_3412 = new Formation(3,4,1,2);
    public static final Formation F_4231 = new Formation(4,2,3,1);



    @Override
    public String toString() { return label; }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getDefenders() {
        return defenders;
    }

    public int getForwards() {
        return forwards;
    }

    public int getAttackingMidfielders() {
        return attackingMidfielders;
    }

    public int getMidfielders() {
        return midfielders;
    }
}
