package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class Team {


    public record LineupSlot(Position slot, Player player) {
        public int penalty() {
            return player.getPosition().penaltyFor(slot);
        }

        public int effectiveRating() {
            return player.getOverall() - penalty();
        }
    }

    private final String name;
    private final List<Player> squad = new ArrayList<>();
    private Formation formation = Formation.F_4213;
    private List<LineupSlot> cachedLineup;

    public Team(String name) {
        this.name = name;
    }



    public List<LineupSlot> getLineup() {
        if (cachedLineup == null) {
            cachedLineup = buildLineup();
        }
        return cachedLineup;
    }


    public void refreshLineup() {
        cachedLineup = null;
    }

    private List<LineupSlot> buildLineup() {
        List<Position> slots = new ArrayList<>();
        slots.add(Position.GK);
        slots.addAll(formation.getSlots());

        Player[] chosen = new Player[slots.size()];
        List<Player> used = new ArrayList<>();


        for (int round = 0; round < slots.size(); round++) {
            int bestSlot = -1;
            Player bestPlayer = null;
            int bestScore = Integer.MIN_VALUE;

            for (int s = 0; s < slots.size(); s++) {
                if (chosen[s] != null) continue;
                for (Player p : squad) {
                    if (used.contains(p)) continue;
                    int score = p.getOverall() - p.getPosition().penaltyFor(slots.get(s));
                    if (score > bestScore) {
                        bestScore = score;
                        bestSlot = s;
                        bestPlayer = p;
                    }
                }
            }
            if (bestPlayer == null) break;
            chosen[bestSlot] = bestPlayer;
            used.add(bestPlayer);
        }

        List<LineupSlot> lineup = new ArrayList<>();
        for (int s = 0; s < slots.size(); s++) {
            if (chosen[s] != null) {
                lineup.add(new LineupSlot(slots.get(s), chosen[s]));
            }
        }
        return Collections.unmodifiableList(lineup);
    }

    public List<Player> getStartingEleven() {
        List<Player> xi = new ArrayList<>();
        for (LineupSlot ls : getLineup()) {
            xi.add(ls.player());
        }
        return xi;
    }



    private int groupAverage(Predicate<Position> slotFilter) {
        int sum = 0, count = 0;
        for (LineupSlot ls : getLineup()) {
            if (slotFilter.test(ls.slot())) {
                sum += ls.effectiveRating();
                count++;
            }
        }
        return count == 0 ? 0 : sum / count;
    }

    public int getAttack() {
        return groupAverage(Position::isForward);
    }

    public int getMidField() {
        return groupAverage(p -> p.isMidFielder() || p.isAttackingMidFielder());
    }

    public int getDefense() {
        return groupAverage(p -> p.isDefender() || p.isGoalkeeper());
    }

    public int getGoalKeeperRating() {
        List<LineupSlot> lineup = getLineup();
        return lineup.isEmpty() ? 0 : lineup.get(0).effectiveRating();
    }

    public int getAverageStamina() {
        List<Player> xi = getStartingEleven();
        if (xi.isEmpty()) {
            return 0;
        }
        int sum = 0;
        for (Player p : xi) {
            sum += p.getStamina();
        }
        return sum / xi.size();
    }

    public List<Player> getPenaltyTakers() {
        List<Player> takers = new ArrayList<>(getStartingEleven());
        takers.removeIf(p -> p.getPosition().isGoalkeeper());
        takers.sort((a, b) -> b.getShooting() - a.getShooting());
        return takers;
    }



    public static Team random(String name, int level) {
        Team t = new Team(name);
        Position[] layout = {
                Position.GK, Position.GK,
                Position.CB, Position.CB, Position.CB, Position.LB, Position.RB, Position.CB,
                Position.DMF, Position.CMF, Position.CMF,
                Position.AMF, Position.AMF, Position.AMF,
                Position.LMF, Position.RMF,
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
        sb.append(name).append(" | ").append(formation.getLabel()).append("
");
        for (LineupSlot ls : getLineup()) {
            sb.append(String.format("  %-4s %s", ls.slot(), ls.player()));
            if (ls.penalty() > 0) {
                sb.append("  (out of position, -").append(ls.penalty()).append(")");
            }
            sb.append("
");
        }
        sb.append("ATT ").append(getAttack())
                .append("  MID ").append(getMidField())
                .append("  DEF ").append(getDefense());
        return sb.toString();
    }

    // ---------- getter / setter ----------

    public String getName() {
        return name;
    }

    public List<Player> getSquad() {
        return Collections.unmodifiableList(squad);
    }

    public Formation getFormation() {
        return formation;
    }

    public void setFormation(Formation formation) {
        this.formation = formation;
        cachedLineup = null;
    }

    public void addPlayer(Player p) {
        squad.add(p);
        cachedLineup = null;
    }
}
