# OOP-chess

Object-oriented programming course project.
Multiplayer chess (client and server) with a drag and drop GUI.

Commits have been removed for privacy.

![image of game UI](http://i.imgur.com/V8QaUe1.png "Game UI")


## Prerequisites
- Java 8 or newer
- Maven (to build)

## Build
1. Clone this repository: `git clone git@github.com:toppev/oop-chess.git`
2. Build with `mvn package`
    - Client: `client/target/client-X.Y-SNAPSHOT.jar`
    - Server: `server/target/server-X.Y-SNAPSHOT.jar`

## Playing
1. Run the server: `java -jar server/target/server-*`
2. Run the client: `java -jar client/target/client-*` or just double-click to execute.
3. Check server address (e.g "localhost") and click "Join Server"
4. Launch the second client
5. Join using the game id