package chess;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Enums;
import core.board.Board;
import core.board.Move;
import core.engine.ChessEngine;
import core.engine.SearchResult;
import core.fen.FenParser;
import core.util.MoveNotation;

import java.lang.reflect.InvocationTargetException;

import static core.fen.FenFromMoves.convertMovesToFen;

public class LichessBotStarter {

    // Store the color globally so we can use it later
    private static String botColor = "unknown";

    public static void start(String token) {
        ClientAuth client = Client.auth(token);

        System.out.println("Starting Lichess bot...");

        // First, upgrade to bot account if needed
        try {
            var upgradeResult = client.bot().upgradeToBotAccount();
            System.out.println("Bot upgrade result: " + upgradeResult);
        } catch (Exception e) {
            System.out.println("Bot upgrade failed or already upgraded: " + e.getMessage());
        }

        // Connect to incoming events
        try {
            var eventStream = client.bot().connect();

            eventStream.stream().forEach(event -> {
                System.out.println("Received event: " + event.getClass().getSimpleName());
                System.out.println("Event content: " + event.toString());

                // Process the event
                processEvent(client, event);
            });

        } catch (Exception e) {
            System.err.println("Error in event stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processEvent(ClientAuth client, Object event) {
        String eventType = event.getClass().getSimpleName();

        switch (eventType) {
            case "ChallengeEvent":
                handleChallengeEvent(client, event);
                break;
            case "ChallengeCreatedEvent":
                handleChallengeCreatedEvent(client, event);
                break;
            case "ChallengeCanceledEvent":
                handleChallengeCanceledEvent(client, event);
                break;
            case "GameStartEvent":
                handleGameStartEvent(client, event);
                break;
            case "GameFinishEvent":
                handleGameFinishEvent(client, event);
                break;
            case "GameStopEvent":
                handleGameStopEvent(client, event);
                break;
            default:
                System.out.println("Unknown event type: " + eventType);
        }
    }

    private static void handleChallengeEvent(ClientAuth client, Object challengeEvent) {
        System.out.println("Processing challenge event...");

        // Try to extract challenge information using reflection
        try {
            var challengeMethod = challengeEvent.getClass().getMethod("challenge");
            var challenge = challengeMethod.invoke(challengeEvent);

            var challengerMethod = challenge.getClass().getMethod("challenger");
            var challenger = challengerMethod.invoke(challengeEvent);

            var nameMethod = challenger.getClass().getMethod("name");
            String challengerName = (String) nameMethod.invoke(challenger);

            System.out.println("Challenge from: " + challengerName);

            // Get challenge ID
            var idMethod = challenge.getClass().getMethod("id");
            String challengeId = (String) idMethod.invoke(challenge);

            // Accept the challenge
            System.out.println("Accepting challenge: " + challengeId);
            var result = client.bot().acceptChallenge(challengeId);
            System.out.println("Accept result: " + result);

        } catch (Exception e) {
            System.err.println("Error processing challenge: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleChallengeCreatedEvent(ClientAuth client, Object challengeCreatedEvent) {
        System.out.println("Processing challenge created event...");

        try {
            var challengeMethod = challengeCreatedEvent.getClass().getMethod("challenge");
            var challenge = challengeMethod.invoke(challengeCreatedEvent);

            // Get challenge ID
            var idMethod = challenge.getClass().getMethod("id");
            String challengeId = (String) idMethod.invoke(challenge);

            // Get challenger information
            var playersMethod = challenge.getClass().getMethod("players");
            var players = playersMethod.invoke(challenge);

            var challengerMethod = players.getClass().getMethod("challenger");
            var challenger = challengerMethod.invoke(players);

            var userMethod = challenger.getClass().getMethod("user");
            var user = userMethod.invoke(challenger);

            // Extract username - handle the format like "debil500elo (debil500elo)"
            String userInfo = user.toString();
            String challengerName = userInfo.split(" ")[0]; // Get just the username part

            System.out.println("Challenge created by: " + challengerName + " (ID: " + challengeId + ")");

            // Accept the challenge
            System.out.println("Accepting challenge: " + challengeId);
            var result = client.bot().acceptChallenge(challengeId);
            System.out.println("Accept result: " + result);

        } catch (Exception e) {
            System.err.println("Error processing challenge created event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleChallengeCanceledEvent(ClientAuth client, Object challengeCanceledEvent) {
        System.out.println("Processing challenge canceled event...");

        try {
            var challengeMethod = challengeCanceledEvent.getClass().getMethod("challenge");
            var challenge = challengeMethod.invoke(challengeCanceledEvent);

            // Get challenge ID
            var idMethod = challenge.getClass().getMethod("id");
            String challengeId = (String) idMethod.invoke(challenge);

            System.out.println("Challenge canceled: " + challengeId);

        } catch (Exception e) {
            System.err.println("Error processing challenge canceled event: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleGameStartEvent(ClientAuth client, Object gameStartEvent) {
        System.out.println("Processing game start event...");

        try {
            // Extract color first
            botColor = extractColorFromGameStart(gameStartEvent);

            var gameMethod = gameStartEvent.getClass().getMethod("game");
            var game = gameMethod.invoke(gameStartEvent);

            // Get game ID
            String gameId = extractGameId(game);

            System.out.println("Game started: " + gameId + ", we are: " + botColor);

            // Start handling the game
            handleGame(client, gameId);

        } catch (Exception e) {
            System.err.println("Error processing game start: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String extractColorFromGameStart(Object gameStartEvent) {
        try {
            var gameMethod = gameStartEvent.getClass().getMethod("game");
            var game = gameMethod.invoke(gameStartEvent);

            // Try to get the color field
            var colorMethod = game.getClass().getMethod("color");
            Object colorObj = colorMethod.invoke(game);
            String color = colorObj.toString();

            System.out.println("Color from GameStartEvent: " + color);
            return color;

        } catch (Exception e) {
            System.err.println("Could not extract color from GameStartEvent: " + e.getMessage());
        }
        return "unknown";
    }

    private static String extractGameId(Object game) {
        try {
            var gameIdMethod = game.getClass().getMethod("gameId");
            return (String) gameIdMethod.invoke(game);
        } catch (NoSuchMethodException e1) {
            // If gameId() doesn't exist, try id()
            try {
                var idMethod = game.getClass().getMethod("id");
                return (String) idMethod.invoke(game);
            } catch (NoSuchMethodException e2) {
                // If neither works, try to get it from the game object's toString or other methods
                System.out.println("Game object class: " + game.getClass().getName());
                System.out.println("Game object content: " + game.toString());
                System.out.println("Available methods:");
                for (var method : game.getClass().getMethods()) {
                    if (method.getName().toLowerCase().contains("id") ||
                            method.getParameterCount() == 0 && method.getReturnType() == String.class) {
                        System.out.println("  - " + method.getName() + "() -> " + method.getReturnType());
                    }
                }
                throw new RuntimeException("Could not find game ID method");
            } catch (IllegalAccessException | InvocationTargetException e2) {
                throw new RuntimeException("Error invoking id() method: " + e2.getMessage(), e2);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Error invoking gameId() method: " + e.getMessage(), e);
        }
    }

    private static void handleGameStopEvent(ClientAuth client, Object gameStopEvent) {
        System.out.println("Game stop event received");

        try {
            var gameMethod = gameStopEvent.getClass().getMethod("game");
            var game = gameMethod.invoke(gameStopEvent);

            // Based on the output, we can see gameId field exists
            var gameIdMethod = game.getClass().getMethod("gameId");
            String gameId = (String) gameIdMethod.invoke(game);

            System.out.println("Game stopped/aborted: " + gameId);

        } catch (Exception e) {
            System.err.println("Error processing game stop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleGameFinishEvent(ClientAuth client, Object gameFinishEvent) {
        System.out.println("Game finished event received");

        try {
            var gameMethod = gameFinishEvent.getClass().getMethod("game");
            var game = gameMethod.invoke(gameFinishEvent);

            // Try gameId first since we know it exists
            String gameId = null;

            try {
                var gameIdMethod = game.getClass().getMethod("gameId");
                gameId = (String) gameIdMethod.invoke(game);
            } catch (NoSuchMethodException e1) {
                try {
                    var idMethod = game.getClass().getMethod("id");
                    gameId = (String) idMethod.invoke(game);
                } catch (NoSuchMethodException e2) {
                    System.out.println("Game finish - Game object class: " + game.getClass().getName());
                    System.out.println("Game finish - Game object content: " + game.toString());
                    // For game finish, we might not need the ID, so just log and continue
                    System.out.println("Could not extract game ID from finish event");
                    return;
                } catch (IllegalAccessException | InvocationTargetException e2) {
                    System.err.println("Error invoking id() method in finish event: " + e2.getMessage());
                    return;
                }
            } catch (IllegalAccessException | InvocationTargetException e1) {
                System.err.println("Error invoking gameId() method in finish event: " + e1.getMessage());
                return;
            }

            System.out.println("Game finished: " + gameId);

        } catch (Exception e) {
            System.err.println("Error processing game finish: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void handleGame(ClientAuth client, String gameId) {
        System.out.println("Starting game handler for: " + gameId);

        try {
            var gameStream = client.bot().connectToGame(gameId);

            gameStream.stream().forEach(gameEvent -> {
                System.out.println("Game event: " + gameEvent.getClass().getSimpleName());
                System.out.println("Game event content: " + gameEvent.toString());

                processGameEvent(client, gameId, gameEvent);
            });

        } catch (Exception e) {
            System.err.println("Error in game stream: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void processGameEvent(ClientAuth client, String gameId, Object gameEvent) {
        String eventType = gameEvent.getClass().getSimpleName();

        switch (eventType) {
            case "Full":
                handleFullGameState(client, gameId, gameEvent);
                break;
            case "State":
                handleGameState(client, gameId, gameEvent);
                break;
            case "ChatLine":
                handleChatLine(gameEvent);
                break;
            default:
                System.out.println("Unknown game event type: " + eventType);
        }
    }

    private static void handleFullGameState(ClientAuth client, String gameId, Object fullEvent) {
        System.out.println("Processing full game state...");

        try {
            // Get the state from the full event
            var stateMethod = fullEvent.getClass().getMethod("state");
            var state = stateMethod.invoke(fullEvent);

            // Get initial player information from Full event
            var whiteMethod = fullEvent.getClass().getMethod("white");
            var white = whiteMethod.invoke(fullEvent);

            var blackMethod = fullEvent.getClass().getMethod("black");
            var black = blackMethod.invoke(fullEvent);

            // If we still don't know our color, try to determine it from the Full event
            if ("unknown".equals(botColor)) {
                botColor = determineOurColorFromFullEvent(fullEvent);
            }

            processGameState(client, gameId, state, white, black);

        } catch (Exception e) {
            System.err.println("Error processing full game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String determineOurColorFromFullEvent(Object fullEvent) {
        try {
            System.out.println("Determining color from Full event...");

            // Try to extract information from the Full event structure
            var whiteMethod = fullEvent.getClass().getMethod("white");
            var white = whiteMethod.invoke(fullEvent);

            var blackMethod = fullEvent.getClass().getMethod("black");
            var black = blackMethod.invoke(fullEvent);

            System.out.println("White player object: " + white.toString());
            System.out.println("Black player object: " + black.toString());

            // Look for patterns in the player objects that indicate which one is us
            String whiteStr = white.toString();
            String blackStr = black.toString();

            // Check if either contains "diplomachessengine" or "DiplomaChessEngine"
            if (whiteStr.toLowerCase().contains("diplomachessengine")) {
                System.out.println("Found our bot name in white player");
                return "white";
            } else if (blackStr.toLowerCase().contains("diplomachessengine")) {
                System.out.println("Found our bot name in black player");
                return "black";
            }

            // Also check for BOT indicators
            if (whiteStr.contains("[BOT]") && !blackStr.contains("[BOT]")) {
                System.out.println("Found BOT indicator in white player only");
                return "white";
            } else if (blackStr.contains("[BOT]") && !whiteStr.contains("[BOT]")) {
                System.out.println("Found BOT indicator in black player only");
                return "black";
            }

        } catch (Exception e) {
            System.err.println("Error determining color from Full event: " + e.getMessage());
        }

        return "unknown";
    }

    private static void handleGameState(ClientAuth client, String gameId, Object stateEvent) {
        System.out.println("Processing game state...");
        // For state updates, we don't have player info, pass null
        processGameState(client, gameId, stateEvent, null, null);
    }

    private static void processGameState(ClientAuth client, String gameId, Object state, Object white, Object black) {
        try {
            var movesMethod = state.getClass().getMethod("moves");
            String moves = (String) movesMethod.invoke(state);

            System.out.println("Current moves: " + moves);
            System.out.println("We are playing as: " + botColor);

            boolean isOurTurn = isOurTurn(moves, botColor);
            System.out.println("Is our turn: " + isOurTurn);

            if (isOurTurn && !"unknown".equals(botColor)) {
                String nextMove = calculateMoveWithFen(moves, botColor);
                makeMove(client, gameId, nextMove);
            } else {
                System.out.println("Waiting for opponent's move...");
            }

        } catch (Exception e) {
            System.err.println("Error processing game state: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean isOurTurn(String moves, String ourColor) {
        if (moves == null || moves.trim().isEmpty()) {
            // No moves yet, white plays first
            return "white".equals(ourColor);
        }

        String[] moveList = moves.trim().split(" ");
        int moveCount = moveList.length;

        // Even number of moves means it's white's turn, odd means black's turn
        if (moveCount % 2 == 0) {
            return "white".equals(ourColor);
        } else {
            return "black".equals(ourColor);
        }
    }

    private static String calculateMoveWithFen(String moves, String ourColor) {
        System.out.println("Calculating move with engine for color: " + ourColor);

        // Convert moves to FEN
        String fenPosition = convertMovesToFen(moves);
        System.out.println("Current FEN position: " + fenPosition);

        // Create board from FEN position
        Board board = new Board();
        FenParser.loadPosition(board, fenPosition);

        // Use engine with FEN-based board
        ChessEngine engine = new ChessEngine();
        SearchResult result = engine.search(board);
        Move bestMove = result.getBestMove();
        String algebraicMove = MoveNotation.ToUci(bestMove);;

        return algebraicMove;
    }


    private static void handleChatLine(Object chatEvent) {
        System.out.println("Chat message received: " + chatEvent.toString());
    }

    private static void makeMove(ClientAuth client, String gameId, String move) {
        System.out.println("Making move: " + move + " in game: " + gameId);

        try {
            var result = client.bot().move(gameId, move);
            System.out.println("Move result: " + result);
        } catch (Exception e) {
            System.err.println("Error making move: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java LichessBotStarter <lichess-token>");
            System.out.println();
            System.out.println("Make sure to:");
            System.out.println("1. Create a Lichess account");
            System.out.println("2. Generate an API token with bot permissions");
            System.out.println("3. The account will be upgraded to a bot account");
            System.exit(1);
        }

        String token = args[0];
        System.out.println("Starting Lichess bot with token: " + token.substring(0, 8) + "...");

        try {
            start(token);
        } catch (Exception e) {
            System.err.println("Error starting bot: " + e.getMessage());
            e.printStackTrace();
        }
    }
}