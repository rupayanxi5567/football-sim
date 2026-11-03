package engine;

import models.Player;
import models.Position;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Commentary {

    // কমেন্ট্রির নিজস্ব Random: ইঞ্জিনের rand-এর সাথে মেশানো চলবে না,
    // নইলে verbose চালু/বন্ধ করলে ম্যাচের ফলই বদলে যেত
    private static final Random RNG = new Random();

    // প্রতিটা বাক্যের তালিকার জন্য শেষবার কোনটা বাছা হয়েছিল (পরপর একই বাক্য এড়াতে)
    private static final Map<String, Integer> LAST_PICK = new HashMap<>();

    // =====================================================================
    //  বাক্যের ভাণ্ডার ({p} = খেলোয়াড়ের নাম, {t} = দলের নাম)
    // =====================================================================



    private static final String[] KICKOFF_LINES = {
            "Kick-off! The match is underway.",
            "And we're off! The referee blows the whistle to start the game.",
            "The players are ready, the crowd is buzzing. Here we go!",
            "Peep! The match begins.",
            "The ball is rolling. Let the game begin!",
            "Kick-off, and the first pass of the match is played."
    };

    private static final String[] HALF_TIME_LINES = {
            "Half-time. Time for the players to catch their breath.",
            "The referee blows for half-time.",
            "The whistle goes for the break. Time to regroup.",
            "Half-time. The managers will have plenty to say in the dressing room.",
            "And that's the first half done. The teams head down the tunnel."
    };

    private static final String[] FULL_TIME_LINES = {
            "Full-time whistle.",
            "The referee brings the match to an end. That's full-time!",
            "It's all over! The final whistle blows.",
            "And that's it. The referee ends the game.",
            "Full-time. The players are exhausted."
    };

    private static final String[] EXTRA_TIME_START_LINES = {
            "The match goes into extra time. Thirty more minutes to find a winner!",
            "Nothing to separate them after 90 minutes. Extra time it is.",
            "We're heading to extra time! The players will need to dig deep.",
            "Still level, and now the players face another half-hour."
    };

    private static final String[] EXTRA_TIME_END_LINES = {
            "Extra time is over.",
            "The referee blows to end extra time.",
            "That's the end of extra time. 120 minutes played.",
            "The whistle goes. Extra time is done."
    };

    private static final String[] SHOOTOUT_START_LINES = {
            "It's going to penalties! Nerves of steel needed now.",
            "Penalty shoot-out time. Who will hold their nerve?",
            "After 120 minutes, it all comes down to the spot.",
            "The shoot-out begins. Hearts are racing all around the stadium."
    };

    private static final String[] PENALTY_SCORED_LINES = {
            "{p} steps up and scores for {t}!",
            "{p} sends the keeper the wrong way. Cool as you like!",
            "{p} hammers it into the top corner. No chance for the keeper!",
            "Calm and composed. {p} rolls it into the corner.",
            "{p} places it down the middle as the keeper dives away. Scored!",
            "{p} puts it away emphatically, and {t} keep the pressure on.",
            "Goal! {p} converts the penalty."
    };

    private static final String[] PENALTY_MISSED_LINES = {
            "{p} misses! The keeper guesses right and saves it!",
            "Off the post! {p} cannot believe it.",
            "{p} blasts it over the bar! Disaster for {t}.",
            "Saved! The goalkeeper gets down to keep out {p}'s penalty.",
            "{p} drags it wide. The pressure got to him.",
            "{p} sees the penalty saved, and the nerves show on the {t} bench."
    };

    private static final String[] WINNER_LINES = {
            "{t} win the match!",
            "It's all over, and {t} are the winners!",
            "{t} take the victory! What a result.",
            "Victory belongs to {t}!",
            "{t} come out on top in the end."
    };

    // ---- গোল ----
    private static final String[] GOAL_FWD = {
            "GOAL! {p} finds the net for {t}!",
            "GOOOAL! {p} makes no mistake from close range!",
            "{p} gets across the defender and slots it past the keeper. {t} score!",
            "What a finish from {p}! The keeper had no chance.",
            "{p} is clinical! {t} have the ball in the back of the net.",
            "Unstoppable! {p} smashes it home for {t}!",
            "{p} wriggles free in the box and finishes coolly.",
            "A predator's goal. {p} was in the right place at the right time.",
            "Curled beautifully into the corner by {p}!",
            "Low, hard and into the bottom corner. {p} for {t}!",
            "{p} races clear and calmly rounds the keeper. Goal!"
    };

    private static final String[] GOAL_MID = {
            "GOAL! {p} arrives late in the box and finishes for {t}!",
            "A wonderful strike from distance by {p}! The keeper is beaten all ends up.",
            "{p} pops up with a goal from midfield! {t} are celebrating.",
            "Superb! {p} picks his spot and {t} score.",
            "{p} drills it low into the corner. Great composure from the midfielder.",
            "{p} steps forward, takes aim and fires it home!",
            "A lovely one-two, and {p} sweeps it into the net."
    };

    private static final String[] GOAL_DEF = {
            "Defender {p} goes forward and scores! Unexpected, but {t} will take it!",
            "A rare goal from the back! {p} rises highest and heads it in.",
            "{p} smashes it in! Not what you'd expect from a defender, but it counts for {t}.",
            "{p} bundles it over the line from a scramble in the box!",
            "The defender {p} is on the scoresheet! What a moment for him."
    };

    private static final String[] GOAL_ANY = {
            "GOAL for {t}! {p} puts it in the net!",
            "It's in! {p} scores for {t}!",
            "{t} have scored! {p} with the finish.",
            "The ball is in the back of the net, and it's {p} who put it there!"
    };

    private static final String[] GOAL_STAR = {
            "Pure class from the star man.",
            "That's why he's one of the best in the business.",
            "Another moment of magic from the big name.",
            "The superstar delivers when it matters."
    };

    // ---- গোলের সময়ের স্বাদ ----
    private static final String[] FLAVOR_EARLY = {
            "An early breakthrough!",
            "What a start to the game!",
            "They've wasted no time at all.",
            "Barely any time on the clock, and already a goal!"
    };

    private static final String[] FLAVOR_BEFORE_BREAK = {
            "Just before the break!",
            "A crucial goal right before half-time.",
            "Perfect timing, just ahead of the interval."
    };

    private static final String[] FLAVOR_AFTER_BREAK = {
            "Straight after the restart!",
            "Right out of the blocks after half-time.",
            "The break did them good, it seems."
    };

    private static final String[] FLAVOR_LATE = {
            "Late drama!",
            "A huge goal at a crucial moment!",
            "Can they hold on now?",
            "The clock is ticking, and this could be decisive.",
            "What a time to score!"
    };

    private static final String[] FLAVOR_EXTRA_TIME = {
            "In extra time!",
            "Extra-time drama!",
            "Can they hang on for the remaining minutes?",
            "That could be the decisive goal!"
    };

    // ---- শট ----
    private static final String[] SAVED_ANY = {
            "{p} tests the keeper, but he gets down well to save.",
            "Good save! {p}'s effort is turned away.",
            "{p} shoots, but the goalkeeper is equal to it.",
            "The keeper stands tall and denies {p}!",
            "A strong hand from the keeper keeps out {p}'s shot.",
            "Superb reflexes! The keeper tips {p}'s effort away.",
            "{p} places it well, but the goalkeeper gets across to stop it.",
            "Parried! {p} can't believe it."
    };

    private static final String[] SAVED_FWD = {
            "{p} was clean through, but the keeper smothers it at his feet.",
            "{p} goes for power, but the keeper holds on.",
            "The keeper makes himself big and blocks {p}'s close-range shot."
    };

    private static final String[] SAVED_LONG = {
            "{p} tries his luck from distance, and the keeper saves comfortably.",
            "A long-range effort from {p} is gathered by the goalkeeper.",
            "{p} lets fly from outside the box, but the keeper is behind it."
    };

    private static final String[] MISSED_ANY = {
            "{p} fires wide of the post.",
            "{p} drags his shot wide.",
            "Over the bar! {p} should have done better.",
            "{p} blazes it over.",
            "Just wide! {p} was inches away.",
            "{p} slices his effort and it goes harmlessly out.",
            "A glorious chance goes begging as {p} misses the target.",
            "That one clipped the outside of the post. {p} can hardly believe it.",
            "{p} snatches at it and sends it into the stands."
    };

    private static final String[] MISSED_LONG = {
            "{p} tries a speculative effort from distance, and it's well off target.",
            "A hopeful long-range shot from {p} sails high and wide."
    };

    // ---- ফাউল, কার্ড, ইনজুরি ----
    private static final String[] FOUL_ANY = {
            "Foul by {p}.",
            "{p} clatters into the back of an opponent. Free-kick.",
            "The referee blows up for a foul by {p}.",
            "{p} mistimes the challenge and gives away a free-kick.",
            "A bit late from {p}. The referee has seen enough.",
            "{p} pulls his man back. Free-kick awarded.",
            "Cynical foul by {p} to stop the break.",
            "{p} goes in hard and the referee whistles."
    };

    private static final String[] FOUL_DEF = {
            "{p} commits to the tackle and takes the man instead of the ball.",
            "{p} brings down the attacker on the edge of the box."
    };

    private static final String[] YELLOW_LINES = {
            "Yellow card for {p}.",
            "The referee reaches for his pocket. {p} is booked.",
            "{p} goes into the book for that challenge.",
            "A caution for {p}, and he'll need to be careful now.",
            "{p} can have no complaints. Yellow card.",
            "That's a clear yellow for {p}.",
            "{p} is booked after a lengthy discussion with the referee."
    };

    private static final String[] RED_LINES = {
            "RED CARD! {p} is sent off!",
            "Straight red! {p} has to go, and {t} are down to ten men.",
            "The referee shows {p} a red card! {t} will be furious.",
            "{p} is off! A moment of madness, and {t} are a man down.",
            "Disaster for {t}! {p} sees red.",
            "Off he goes! {p} walks, and {t} must hold on with ten men."
    };

    private static final String[] EXTRA_TIME_HALF_LINES = {
            "End of the first period of extra time. Fifteen minutes to go.",
            "The players switch ends. Another fifteen minutes to find a winner.",
            "A short break, and then the final fifteen minutes of extra time.",
            "Halfway through extra time, and the legs are heavy."
    };

    private static final String[] INJURY_LINES = {
            "{p} is down injured.",
            "{p} stays down and the physios are on.",
            "{p} pulls up holding his hamstring.",
            "Concern for {t} as {p} receives treatment.",
            "{p} looks in real discomfort. This could be serious.",
            "{p} grimaces and signals to the bench.",
            "A worrying moment as {p} limps."
    };

    private static final String[] GOAL_ASSIST = {
            "{p} finishes after a lovely pass from {a}!",
            "{a} picks him out, and {p} makes no mistake for {t}!",
            "Great work from {a}, and {p} gets the goal.",
            "{a} with the assist, {p} with the finish. Wonderful for {t}!",
            "{a} slides it through, and {p} slots it home!",
            "A perfect cross from {a}, and {p} converts it!"
    };

    private static final String[] FOUL_VICTIM = {
            "{p} brings down {a}.",
            "{p} catches {a} late. Free-kick.",
            "{a} goes down under a challenge from {p}.",
            "{p} clips the heels of {a}. The referee whistles."
    };

    // =====================================================================
    //  হেল্পার মেথড
    // =====================================================================

    // তালিকা থেকে একটা বাক্য বাছো, কিন্তু আগেরবারের একই বাক্য পরপর নয়
    private static String pick(String key, String... options) {
        int idx = RNG.nextInt(options.length);
        Integer last = LAST_PICK.get(key);
        if (options.length > 1 && last != null && idx == last) {
            idx = (idx + 1 + RNG.nextInt(options.length - 1)) % options.length;
        }
        LAST_PICK.put(key, idx);
        return options[idx];
    }

    private static String[] concat(String[] a, String[] b) {
        String[] out = new String[a.length + b.length];
        System.arraycopy(a, 0, out, 0, a.length);
        System.arraycopy(b, 0, out, a.length, b.length);
        return out;
    }

    // {p} আর {t} বসানো, নাম না থাকলে নিরাপদ বিকল্প
    private static String fill(String template, MatchEvent e) {
        String who = (e.getPlayer() != null) ? e.getPlayer().getName() : "a player";
        String team = (e.getTeam() != null) ? e.getTeam().getName() : "the team";
        String other = (e.getSecondPlayer() != null) ? e.getSecondPlayer().getName() : "a teammate";
        return template.replace("{p}", who).replace("{t}", team).replace("{a}", other);
    }

    private static String score(MatchEvent e) {
        return " (" + e.getHomeScore() + "-" + e.getAwayScore() + ")";
    }

    private static String shootoutScore(MatchEvent e) {
        return "  [Shoot-out: " + e.getHomeScore() + "-" + e.getAwayScore() + "]";
    }

    // মিনিটের লেবেল। যোগ করা সময় ("45+2") দেখাতে চাইলে শুধু এই মেথড বদলালেই হবে
    private static String time(MatchEvent e) {
        String l = e.getLabel();
        return (l != null && !l.isEmpty() ? l : String.valueOf(e.getMinute())) + "' ";
    }

    private static String role(MatchEvent e) {
        Player p = e.getPlayer();
        if (p == null) return "any";
        Position pos = p.getPosition();
        if (pos.isGoalkeeper()) return "gk";
        if (pos.isDefender()) return "def";
        if (pos.isMidFielder() || pos.isAttackingMidFielder()) return "mid";
        return "fwd";
    }

    private static boolean isStar(MatchEvent e) {
        return e.getPlayer() != null && e.getPlayer().getOverall() >= 90;
    }

    // গোলের মিনিট দেখে একটা অতিরিক্ত বাক্য (সবসময় নয়, ~৭০% ক্ষেত্রে)
    private static String timingFlavor(int minute) {
        if (RNG.nextInt(100) >= 70) return "";
        if (minute <= 5) return pick("flavor.early", FLAVOR_EARLY);
        if (minute >= 41 && minute <= 45) return pick("flavor.break", FLAVOR_BEFORE_BREAK);
        if (minute >= 46 && minute <= 50) return pick("flavor.restart", FLAVOR_AFTER_BREAK);
        if (minute > 90) return pick("flavor.et", FLAVOR_EXTRA_TIME);
        if (minute >= 85) return pick("flavor.late", FLAVOR_LATE);
        return "";
    }

    private static String goal(MatchEvent e) {
        String r = role(e);
        String[] pool = switch (r) {
            case "fwd" -> concat(GOAL_FWD, GOAL_ANY);
            case "mid" -> concat(GOAL_MID, GOAL_ANY);
            case "def" -> concat(GOAL_DEF, GOAL_ANY);
            default -> GOAL_ANY;
        };

        String line;
        if( RNG.nextInt(100)<65 && e.getSecondPlayer() != null){
            line = fill(pick("goal.assist", GOAL_ASSIST), e);
        }else{
            line = fill(pick("goal." + r, pool), e);
        }

        String flavor = timingFlavor(e.getMinute());
        String star = (isStar(e) && RNG.nextInt(100) < 35)
                ? " " + pick("goal.star", GOAL_STAR) : "";

        return time(e) + line + (flavor.isEmpty() ? "" : " " + flavor) + star + score(e);
    }

    private static String shotSaved(MatchEvent e) {
        String r = role(e);
        String[] extra = r.equals("fwd") ? SAVED_FWD : SAVED_LONG;
        return time(e) + fill(pick("saved." + r, concat(SAVED_ANY, extra)), e);
    }

    private static String shotMissed(MatchEvent e) {
        String r = role(e);
        String[] pool = r.equals("fwd") ? MISSED_ANY : concat(MISSED_ANY, MISSED_LONG);
        return time(e) + fill(pick("missed." + r, pool), e);
    }

    private static String foul(MatchEvent e) {
        String r = role(e);
        // 6 no. todo
        String[] pool = r.equals("def") ? concat(FOUL_ANY, FOUL_DEF) : FOUL_ANY;
        return time(e) + fill(pick("foul." + r, pool), e);
    }

    public static String describe(MatchEvent e) {
        return switch (e.getType()) {
            case KICKOFF -> pick("kickoff", KICKOFF_LINES);
            case GOAL -> goal(e);
            case SHOT_SAVED -> shotSaved(e);
            case SHOT_MISSED -> shotMissed(e);
            case FOUL -> foul(e);
            case YELLOW_CARD -> time(e) + fill(pick("yellow", YELLOW_LINES), e);
            case RED_CARD -> time(e) + fill(pick("red", RED_LINES), e);
            case INJURY -> time(e) + fill(pick("injury", INJURY_LINES), e);
            case HALF_TIME -> pick("halftime", HALF_TIME_LINES) + " Score: " + e.getHomeScore() + "-" + e.getAwayScore();
            case FULL_TIME -> pick("fulltime", FULL_TIME_LINES) + " Score: " + e.getHomeScore() + "-" + e.getAwayScore();
            case EXTRA_TIME_START -> pick("et.start", EXTRA_TIME_START_LINES);
            case EXTRA_TIME_END -> pick("et.end", EXTRA_TIME_END_LINES);
            case PENALTY_SHOOTOUT_START -> pick("shootout.start", SHOOTOUT_START_LINES);
            case PENALTY_SCORED -> fill(pick("pen.scored", PENALTY_SCORED_LINES), e) + shootoutScore(e);
            case PENALTY_MISSED -> fill(pick("pen.missed", PENALTY_MISSED_LINES), e) + shootoutScore(e);
            case WINNER -> fill(pick("winner", WINNER_LINES), e) + (e.getLabel().isEmpty() ? "" : " (" + e.getLabel() + ")");
            case EXTRA_TIME_HALF -> pick("et.half", EXTRA_TIME_HALF_LINES) + " Score: " + e.getHomeScore() + "-" + e.getAwayScore();
        };
    }
}