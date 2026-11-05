# ⚽ Football Sim

**A Java football match and league simulator.** Build squads, watch the match engine turn player ratings into chances and goals, and run a complete home-and-away season with a live league table.

The project is designed to be easy to explore: start with generated teams and a seeded season, or connect it to MySQL to load real squads and save league progress.

## ✨ What it can do

- **Simulate matches** with possession, attacks, shots, saves, goals, stamina effects, and home advantage.
- **Play knockout matches** through extra time and penalty shootouts when the score is level.
- **Build a starting XI** from a squad, choosing players for formation slots and accounting for out-of-position penalties.
- **Run a full league** with home and away fixtures, points, goal difference, and a sorted table.
- **Generate repeatable seasons** with a random seed, so results can be reproduced.
- **Load squads from MySQL** and save or restore partially played leagues.
- **Follow match events** with generated play-by-play commentary.

## 🧰 Requirements

- **JDK 21 or newer** (the code uses `Math.clamp` and Java text blocks).
- **MySQL 8** and a populated `football_sim` database for database-backed examples.
- The MySQL Connector/J dependency is included in `lib/`.

There is no Maven or Gradle build file yet. Compile directly with `javac` or open the project in IntelliJ IDEA.

## 🚀 Run a season

The default `Main` program generates eight synthetic teams, plays a seeded double round-robin season, prints the final table, and reports a few consistency checks. It does not need a database.

### Windows PowerShell

```powershell
New-Item -ItemType Directory -Force out | Out-Null
javac -cp "lib/*" -d out (Get-ChildItem src -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp "out;lib/*" Main
```

### macOS / Linux

```bash
mkdir -p out
javac -cp 'lib/*' -d out $(find src -name '*.java')
java -cp 'out:lib/*' Main
```

The seed in `Main` is `42`. Change it to explore a different repeatable season.

## 🗄️ Connect MySQL

Database settings are read from `db.properties` in the project root. This file is intentionally ignored by Git, so add your local connection settings there:

```properties
db.url=jdbc:mysql://localhost:3306/football_sim?useUnicode=true&characterEncoding=UTF-8
db.user=your_mysql_user
db.password=your_mysql_password
```

The repository expects a database populated with the `teams`, `players`, `saves`, `save_teams`, and `fixtures` tables. Database schema and sample data are not included in this repository. The examples that load leagues or national teams need matching records in that database.

Database-backed entry points:

| Class | What it demonstrates |
| --- | --- |
| `LeagueTest` | Load a league, simulate its season, and print the table |
| `DbTest` | Load Portugal and Argentina, show lineups, and play a knockout match |
| `SaveLoadTest` | Save a partly played league, reload it, then finish the season |
| `MonteCarloTest` | Simulate repeated fixtures and summarize the outcomes |

Run one after compiling by replacing `Main` in the Java command. For example:

```powershell
java -cp "out;lib/*" LeagueTest
```

```bash
java -cp 'out:lib/*' LeagueTest
```

## 🧠 How the simulation is structured

```text
src/
├── engine/   Match flow, events, and commentary
├── league/   Fixtures, season simulation, and standings
├── models/   Players, positions, formations, and teams
└── storage/  MySQL access and league save/load
```

Team strength comes from the best available lineup for the selected formation. The match engine uses team and player ratings, stamina, and possession to shape events over regular time. A league match ends after regular time; knockout matches can continue into extra time and penalties.

## 🧪 Quick validation

Running `Main` prints checks for the expected fixture count, matches played per team, matching league-wide goals for and against, and completed fixtures. With eight teams, the season has **56 fixtures** and **14 matches per team**.

## 🛠️ Ideas to build on

Add a database schema and seed dataset, introduce substitutions and richer match events, or create a UI for managing squads and following a season. The current code keeps simulation, league rules, player models, and persistence in separate packages to make those additions straightforward.

---

*Made for the drama of football, one simulated minute at a time.*
