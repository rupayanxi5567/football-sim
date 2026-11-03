package models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Team {
    private String name;
    List<Player> squad = new ArrayList<>();
    private Formation formation = Formation.F_4312;

    private List<Player> best(Predicate<Position> filters, int n) {
        List<Player> myList = new ArrayList<>();
        for (Player p : squad) {
            if (filters.test(p.getPosition())) {
                myList.add(p);
            }
        }
        myList.sort((a, b) -> b.getOverall() - a.getOverall());
        return new ArrayList<>(myList.subList(0, Math.min(n, myList.size())));
    }

    public List<Player> getStartingEleven(){
        List<Player> xiPlayers = new ArrayList<>();
        xiPlayers.addAll(best(Position::isGoalkeeper,1));
        xiPlayers.addAll(best(Position::isDefender,formation.getDefenders()));
        xiPlayers.addAll(getStartingMidfielders());
        xiPlayers.addAll(best(Position::isForward,formation.getForwards()));
        return xiPlayers;
    }

    private List<Player> getStartingMidfielders() {
        List<Player> amf = best(Position::isAttackingMidFielder, formation.getAttackingMidfielders());
        int missing = formation.getAttackingMidfielders() - amf.size();
        List<Player> mids = best(p -> p.isMidFielder() && !p.isAttackingMidFielder(),
                formation.getMidfielders() + missing);
        List<Player> all = new ArrayList<>(mids);
        all.addAll(amf);
        return all;
    }

    private int calculateAverage(List<Player> xiPlayers){
        if(xiPlayers.size()==0){
            return 0;
        }
        int sum=0;
        for(Player p:xiPlayers){
            sum=sum+p.getOverall();
        }
        return sum/xiPlayers.size();
    }

    public int getAttack(){
        return calculateAverage(best(Position::isForward, formation.getForwards()));
    }

    public int getMidField() {
        return calculateAverage(getStartingMidfielders());
    }

    public int getDefense(){
        List<Player> backs = best(Position::isDefender, formation.getDefenders());
        backs.addAll(best(Position::isGoalkeeper,1));
        return calculateAverage(backs);
    }

    public static Team random(String name, int level) {
        Team t = new Team(name);
        Position[] layout = {
                Position.GK, Position.GK,
                Position.CB, Position.CB, Position.CB, Position.LB, Position.RB, Position.CB,
                Position.DMF, Position.CMF, Position.CMF, Position.AMF, Position.LMF, Position.RMF,
                Position.CF, Position.CF, Position.LWF, Position.RWF
        };
        int n = 1;
        for (Position pos : layout) {
            t.addPlayer(Player.randomPlayer(name + " " + pos + n++, pos, level));
        }
        return t;
    }

    public String getLineupText() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" | ").append(formation.getLabel()).append("\n");
        for (Player p : getStartingEleven()) {
            sb.append("  ").append(p).append("\n");
        }
        sb.append("ATT ").append(getAttack())
                .append("  MID ").append(getMidField())
                .append("  DEF ").append(getDefense());
        return sb.toString();
    }











    public String getName() {
        return name;
    }

    public List<Player> getSquad() {
        return squad;
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
    }

    public Team(String name) {
        this.name = name;
    }
    public void addPlayer(Player p) {
        squad.add(p);
    }

    public int getGoalKeeperRating(){
        List<Player> gk = best(Position::isGoalkeeper,1);
        return gk.isEmpty() ? 0 : gk.getFirst().getOverall();
    }

    public int getAverageStamina(){
        List<Player> xi = getStartingEleven();
        if(xi.isEmpty()){
            return 0;
        }
        int sum = 0;
        for(Player p:xi){
            sum = sum+p.getStamina();
        }
        return sum/xi.size();
    }

    public List<Player> getPenaltyTakers(){
        List<Player> takers = new ArrayList<>(getStartingEleven());
        takers.removeIf(p -> p.getPosition().isGoalkeeper());
        takers.sort((a,b) -> b.getShooting() - a.getShooting());
        return takers;
    }

}
