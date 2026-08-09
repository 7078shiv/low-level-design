# Chess

A playable chess app built on the LLD in `org.example.lld.chess`: the domain model is the
rules authority, Spring Boot exposes it over REST, and a static frontend draws the board.

## Running it

```bash
docker compose up -d --build     # http://localhost:8080
docker compose down
```

Or without Docker:

```bash
mvn spring-boot:run              # http://localhost:8080
mvn package && java -jar target/low-level-design-1.0-SNAPSHOT.jar
```

`PORT` overrides the port. Games live in memory, so a restart clears them and running more
than one replica needs a shared store first.

## Deploying to Google Cloud Run

```bash
gcloud auth login
gcloud projects list                       # pick one deliberately
./deploy/cloudrun.sh <project-id>          # region defaults to asia-south1
```

The script enables the Cloud Run, Cloud Build and Artifact Registry APIs, builds the
Dockerfile through Cloud Build and deploys, then prints the URL.

`--max-instances 1` in that script is a correctness requirement, not a cost control. Games
are held in memory, so if Cloud Run started a second instance your next move could land on
one that has never seen your game and answer `404`. Lifting the cap means moving the game
store out of memory first, into Redis or Firestore.

Two smaller things: the service scales to zero, so the first request after an idle spell
pays a JVM cold start of a few seconds — `--min-instances 1` removes that but bills for an
always-warm instance. And with two full vCPUs the bot needs no time compensation, so
`CHESS_BOT_TIME_SCALE` stays at `1` here, unlike Render's free tier.

## Deploying to Render

[![Deploy to Render](https://render.com/images/deploy-to-render-button.svg)](https://render.com/deploy?repo=https://github.com/7078shiv/low-level-design)

`render.yaml` describes the service, so Render configures itself from the repo: Docker
runtime, health check on `/api/chess/health`, and auto deploy on push to `main`.

Two things to know about the free instance:

- it sleeps after 15 minutes idle, so the first request after that takes roughly a minute
- it is 0.1 CPU, and the search is CPU bound, so `CHESS_BOT_TIME_SCALE` is set to `3`.
  That buys back the depth but makes the hard bot think for about ten seconds a move.
  Lower it in the Render dashboard for quicker, weaker replies; no code change needed.

## Layout

```
org.example.lld.chess
├── Board, Cell, Position, Move, Player     domain model
├── Game                                    turn order, legality, castling, en passant,
│                                           promotion, checkmate, stalemate, draws
├── MoveOption                              a legal move, before it is played
├── pieces/                                 movement geometry, one class per piece
├── player/                                 HumanPlayer, BotPlayer
├── bot/
│   ├── BotLevel, BotStrategy, EngineBot    difficulty and the bridge to the search
│   └── engine/                             SearchBoard, Evaluation, Search
└── api/                                    controllers, service, DTOs
```

Pieces only know their own geometry. Anything needing the whole position or the move
history — pins, castling rights, en passant, the end conditions — lives in `Game`.

## The bot

`Game` copies the board for every legality test, which is right for a person clicking
squares and far too slow for a search. So `bot/engine` holds a second representation of the
same position: a flat `int[64]` with make/unmake, used only while thinking.

The search is alpha-beta with iterative deepening, a transposition table, killer and history
move ordering, null-move pruning, late move reductions and a quiescence search.

| Level  | Depth  | Budget |
|--------|--------|--------|
| Easy   | 2 ply  | 0.2s   |
| Medium | 5 ply  | 1s     |
| Hard   | to 64  | 3.5s   |

Hard reaches about ten ply on an opening position. `EngineBot` checks whatever the search
returns against `Game.legalMoveOptions()` before playing it, so the two representations can
never silently drift apart.

`PerftTest` counts every move sequence from the opening position and compares against the
published perft numbers (4,865,609 at depth 5), which is what proves the fast generator
handles castling, en passant, promotion and pins correctly.

## API

| Method | Path                                   | Purpose                                |
|--------|----------------------------------------|----------------------------------------|
| GET    | `/api/chess/health`                    | liveness probe                         |
| POST   | `/api/chess/games`                     | new game, optionally against a bot     |
| GET    | `/api/chess/games/{id}`                | full state                             |
| GET    | `/api/chess/games/{id}/legal-moves?from=e2` | destinations for one square       |
| POST   | `/api/chess/games/{id}/moves`          | `{"from":"e2","to":"e4","promotion":"QUEEN"}` |
| POST   | `/api/chess/games/{id}/bot-move`       | let the bot think and reply            |
| POST   | `/api/chess/games/{id}/undo`           | take back (two plies against a bot)    |
| POST   | `/api/chess/games/{id}/resign?colour=WHITE` | resign                            |
| POST   | `/api/chess/games/{id}/draw`           | agree a draw                           |
| DELETE | `/api/chess/games/{id}`                | forget the game                        |

Starting a game against the hard bot playing black:

```bash
curl -X POST localhost:8080/api/chess/games \
  -H 'Content-Type: application/json' \
  -d '{"whiteName":"Shivang","botColour":"BLACK","difficulty":"HARD"}'
```

Every response carries the whole state, including `legalMoves` for the side to move, so the
frontend highlights destinations without another round trip. Illegal moves answer `400`
with a readable reason; unknown game ids answer `404`.

## Frontend

`src/main/resources/static` — no build step, no dependencies. Click or drag to move, with
legal destinations marked, last move and check highlighted, SAN move list, captured pieces
and material count, promotion picker, undo, flip, resign and a shareable `?game=<id>` URL.

The board sizes itself from the space actually available, so it fits from a 320px phone
upwards; below 900px the side panel stacks under the board. Dragging uses HTML5 drag and
drop, which touch screens do not fire, so on a phone you move by tapping the piece and then
the destination.

## Fixes to the original LLD

The rules code could not make a single legal move before this:

- every piece compared the moving piece's colour to itself (`start.getPiece()`) instead of
  the destination's, so `isValidMove` always returned false
- `isPathClear` looped `while (row != endRow && col != endCol)`; on a rank or file one
  coordinate never changes, so it exited before checking any square for blockers
- `Pawn` had no direction, so pawns could move backwards, and its capture check compared
  the target piece's colour to itself
- `Position.isValid()` accepted index 8
- there was no `Game` at all: no turns, check, checkmate, castling, en passant or promotion
