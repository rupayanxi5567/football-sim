package league;

public class TableEntry {
    public final String teamName;
    public int played, won, drawn, lost, goalsFor, goalsAgainst;

    public TableEntry(String teamName) {
        this.teamName = teamName;
    }

    public int getPoints() {
        return won * 3 + drawn;
    }

    public int getGoalDiff() {
        return goalsFor - goalsAgainst;
    }

    public void record(int scored, int conceded) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        if (scored > conceded) won++;
        else if (scored == conceded) drawn++;
        else lost++;
    }
}
