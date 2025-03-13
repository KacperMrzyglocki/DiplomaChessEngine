package gui;

import core.board.Board;
import core.board.Move;
import core.engine.ChessEngine;
import core.engine.SearchResult;
import core.fen.FenParser;
import core.util.MoveNotation;
import core.util.Square;
import core.board.MoveList;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChessGUI extends JFrame {
    private final Board board;
    private final ChessEngine engine;
    private final BoardPanel boardPanel;
    private Square selectedSquare = null;
    private JTextArea moveHistoryArea;
    private JLabel gameStatusLabel;

    // Standard starting position in FEN notation
    private static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    // Image cache for pieces
    private final Map<Character, Image> pieceImages = new HashMap<>();

    public ChessGUI() {
        this.board = new Board();
        this.engine = new ChessEngine();
        engine.init();

        // Set up the frame
        setTitle("Chess Engine");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize the board panel
        boardPanel = new BoardPanel();
        add(boardPanel, BorderLayout.CENTER);

        // Add a status label at the top
        gameStatusLabel = new JLabel("Game in progress");
        gameStatusLabel.setHorizontalAlignment(JLabel.CENTER);
        gameStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(gameStatusLabel, BorderLayout.NORTH);

        // Add a control panel
        JPanel controlPanel = new JPanel(new BorderLayout());

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton newGameBtn = new JButton("New Game");
        JButton engineMoveBtn = new JButton("Engine Move");
        JButton undoBtn = new JButton("Undo");

        newGameBtn.addActionListener(e -> resetBoard());
        engineMoveBtn.addActionListener(e -> makeEngineMove());
        undoBtn.addActionListener(e -> undoMove());

        buttonPanel.add(newGameBtn);
        buttonPanel.add(engineMoveBtn);
        buttonPanel.add(undoBtn);
        controlPanel.add(buttonPanel, BorderLayout.NORTH);

        // Move history area
        moveHistoryArea = new JTextArea(5, 20);
        moveHistoryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        controlPanel.add(scrollPane, BorderLayout.CENTER);

        add(controlPanel, BorderLayout.SOUTH);

        // Initialize the board with the starting position
        resetBoard();

        // Load piece images
        loadPieceImages();

        // Set size and center the frame
        setSize(600, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void resetBoard() {
        board.setInitialPosition();
        boardPanel.repaint();
        selectedSquare = null;
        moveHistoryArea.setText("Game started with initial position.\n");
        updateGameStatus();
    }

    private void makeEngineMove() {
        // Check if the game is already over
        if (isGameOver()) {
            return;
        }

        // Debug info
        MoveList moveList = new MoveList(256);
        engine.getMoveGenerator().generateLegalMoves(board, moveList);
        System.out.println("Legal moves available for engine: " + moveList.size());

        SearchResult result = engine.search(board);
        if (result.getBestMove() != null) {
            Move bestMove = result.getBestMove();
            board.makeMove(bestMove);
            boardPanel.repaint();
            String algebraicMove = MoveNotation.toAlgebraic(bestMove);
            logMove("Engine: " + algebraicMove);
            System.out.println("Engine move: " + algebraicMove);
            System.out.println("Evaluation: " + result.getScore());

            // Check game state after engine move
            updateGameStatus();
        } else {
            logMove("Engine: No legal moves available");
            System.out.println("Engine couldn't find a best move. Nodes searched: " + result.getNodesSearched());
            updateGameStatus(); // This will check for stalemate or checkmate
        }
    }

    private void undoMove() {
        board.undoMove();
        boardPanel.repaint();
        selectedSquare = null;
        logMove("Move undone");
        updateGameStatus();
    }

    private void logMove(String moveText) {
        moveHistoryArea.append(moveText + "\n");
        // Scroll to the bottom
        moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());
    }

    /**
     * Check the current game state and update the UI accordingly
     */
    private void updateGameStatus() {
        if (board.isCheckmate()) {
            String winner = board.isWhiteToMove() ? "Black" : "White";
            gameStatusLabel.setText("Checkmate! " + winner + " wins");
            gameStatusLabel.setForeground(Color.RED);
            logMove("Game over: Checkmate - " + winner + " wins");
            JOptionPane.showMessageDialog(this,
                    "Checkmate! " + winner + " wins",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (board.isStalemate()) {
            gameStatusLabel.setText("Stalemate! Game is drawn");
            gameStatusLabel.setForeground(Color.BLUE);
            logMove("Game over: Stalemate");
            JOptionPane.showMessageDialog(this,
                    "Stalemate! The game is a draw",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (board.isDraw()) {
            gameStatusLabel.setText("Draw! Game is drawn");
            gameStatusLabel.setForeground(Color.BLUE);
            logMove("Game over: Draw");
            JOptionPane.showMessageDialog(this,
                    "Draw by repetition or 50-move rule",
                    "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (board.isInCheck()) {
            String sideInCheck = board.isWhiteToMove() ? "White" : "Black";
            gameStatusLabel.setText(sideInCheck + " is in check!");
            gameStatusLabel.setForeground(Color.ORANGE);
        } else {
            String sideToMove = board.isWhiteToMove() ? "White" : "Black";
            gameStatusLabel.setText(sideToMove + " to move");
            gameStatusLabel.setForeground(Color.BLACK);
        }
    }

    /**
     * Check if the game is over (checkmate, stalemate, or draw)
     */
    private boolean isGameOver() {
        return board.isCheckmate() || board.isStalemate() || board.isDraw();
    }

    /**
     * Check if the specified side (white or black) is in checkmate
     */
    private boolean isCheckmated(boolean white) {
        return board.isCheckmate() && board.isWhiteToMove() == white;
    }

    private void loadPieceImages() {
        // Piece characters and corresponding filenames
        char[] pieceChars = {'p', 'n', 'b', 'r', 'q', 'k', 'P', 'N', 'B', 'R', 'Q', 'K'};
        String[] fileNames = {
                "black_pawn.png", "black_knight.png", "black_bishop.png",
                "black_rook.png", "black_queen.png", "black_king.png",
                "white_pawn.png", "white_knight.png", "white_bishop.png",
                "white_rook.png", "white_queen.png", "white_king.png"
        };

        for (int i = 0; i < pieceChars.length; i++) {
            char pieceChar = pieceChars[i];
            String filename = "/pieces/" + fileNames[i]; // Use corresponding filename

            try (InputStream is = getClass().getResourceAsStream(filename)) {
                if (is != null) {
                    Image img = new ImageIcon(ImageIO.read(is)).getImage();
                    pieceImages.put(pieceChar, img);
                } else {
                    throw new IOException("Resource not found: " + filename);
                }
            } catch (Exception e) {
                System.err.println("Failed to load image for " + pieceChar + ": " + e.getMessage());
                // Create a fallback image with text
                Image fallbackImage = createFallbackImage(pieceChar);
                pieceImages.put(pieceChar, fallbackImage);
            }
        }
    }

    private Image createFallbackImage(char pieceChar) {
        // Create a simple image with the piece character
        int size = 64;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        // Fill with transparent background
        g2d.setColor(new Color(0, 0, 0, 0));
        g2d.fillRect(0, 0, size, size);

        // Draw the piece character
        g2d.setColor(Character.isUpperCase(pieceChar) ? Color.WHITE : Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g2d.getFontMetrics();
        int x = (size - fm.charWidth(pieceChar)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g2d.drawString(String.valueOf(pieceChar), x, y);

        g2d.dispose();
        return img;
    }

    private class BoardPanel extends JPanel {
        private static final int SQUARE_SIZE = 64;

        private java.util.List<Square> possibleMoves = new ArrayList<>();

        public BoardPanel() {
            setPreferredSize(new Dimension(8 * SQUARE_SIZE, 8 * SQUARE_SIZE));

            // Add mouse listener for piece movement
            addMouseAdapter();
        }

        private void addMouseAdapter() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Check if game is over
                    if (isGameOver()) {
                        return;
                    }

                    int file = e.getX() / SQUARE_SIZE;
                    int rank = 7 - (e.getY() / SQUARE_SIZE);

                    // Convert to square index (0-63)
                    int squareIndex = rank * 8 + file;

                    handleSquareClick(squareIndex);
                }
            });
        }

        private void handleSquareClick(int squareIndex) {
            if (selectedSquare == null) {
                if (board.getPiece(squareIndex) != 0) {
                    // Only allow selecting pieces of the current player's color
                    int piece = board.getPiece(squareIndex);
                    boolean isWhitePiece = piece >= 1 && piece <= 6;

                    if ((board.isWhiteToMove() && isWhitePiece) ||
                            (!board.isWhiteToMove() && !isWhitePiece)) {
                        selectedSquare = Square.fromIndex(squareIndex);
                        highlightPossibleMoves(selectedSquare);
                        repaint();
                    }
                }
            } else {
                Square targetSquare = Square.fromIndex(squareIndex);

                // Get all legal moves for the selected piece
                MoveList moveList = new MoveList(256);
                engine.getMoveGenerator().generateLegalMoves(board, moveList);

                // Find if there's a legal move matching our selected source and target squares
                Move matchingMove = null;
                for (int i = 0; i < moveList.size(); i++) {
                    Move move = moveList.get(i);
                    if (move.getFrom() == selectedSquare.getSquare() &&
                            move.getTo() == targetSquare.getSquare()) {
                        matchingMove = move;
                        break;
                    }
                }

                // Check for castling specifically
                if (matchingMove == null) {
                    int selectedPiece = board.getPiece(selectedSquare.getSquare());
                    // Is this a king?
                    if (selectedPiece == 6 || selectedPiece == 12) {
                        // Check if this could be a castling move
                        // Kingside castling: king moves 2 squares to the right
                        if (targetSquare.getSquare() == selectedSquare.getSquare() + 2 ||
                                targetSquare.getSquare() == selectedSquare.getSquare() - 2) {
                            for (int i = 0; i < moveList.size(); i++) {
                                Move move = moveList.get(i);
                                // Check if this is a castling move in our move list
                                if (move.getFrom() == selectedSquare.getSquare() &&
                                        move.getTo() == targetSquare.getSquare() &&
                                        move.getMoveType() == Move.CASTLING) {
                                    matchingMove = move;
                                    break;
                                }
                            }
                        }
                    }
                }

                if (matchingMove != null) {
                    board.makeMove(matchingMove);
                    String algebraicMove = MoveNotation.toAlgebraic(matchingMove);
                    logMove("Human: " + algebraicMove);
                    selectedSquare = null;
                    possibleMoves.clear();  // Clear highlights
                    repaint();

                    // Check game state after human move
                    updateGameStatus();

                    // Make engine move if game is not over
                    if (!isGameOver()) {
                        SwingUtilities.invokeLater(() -> makeEngineMove());
                    }
                } else {
                    if (board.getPiece(squareIndex) != 0) {
                        // If clicking on another piece of same color, select that piece instead
                        int piece = board.getPiece(squareIndex);
                        boolean isWhitePiece = piece >= 1 && piece <= 6;

                        if ((board.isWhiteToMove() && isWhitePiece) ||
                                (!board.isWhiteToMove() && !isWhitePiece)) {
                            selectedSquare = targetSquare;
                            highlightPossibleMoves(selectedSquare);
                        } else {
                            selectedSquare = null;
                            possibleMoves.clear();
                        }
                    } else {
                        selectedSquare = null;
                        possibleMoves.clear();
                    }
                    repaint();
                }
            }
        }

        private void highlightPossibleMoves(Square selected) {
            possibleMoves.clear();
            MoveList moveList = new MoveList(256);
            engine.getMoveGenerator().generateLegalMoves(board, moveList);

            for (int i = 0; i < moveList.size(); i++) {
                Move move = moveList.get(i);
                if (move.getFrom() == selected.getSquare()) {
                    possibleMoves.add(Square.fromIndex(move.getTo()));
                }
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            drawBoard(g2d);

            if (selectedSquare != null) {
                int file = selectedSquare.getFile();
                int rank = selectedSquare.getRank();
                g2d.setColor(new Color(255, 255, 0, 128));
                g2d.fillRect(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);
            }

            // Highlight legal moves
            g2d.setColor(new Color(0, 255, 0, 128));
            for (Square move : possibleMoves) {
                int file = move.getFile();
                int rank = move.getRank();
                g2d.fillOval(file * SQUARE_SIZE + SQUARE_SIZE / 4, (7 - rank) * SQUARE_SIZE + SQUARE_SIZE / 4,
                        SQUARE_SIZE / 2, SQUARE_SIZE / 2);
            }

            drawPieces(g2d);
        }

        private void drawBoard(Graphics2D g2d) {
            for (int rank = 0; rank < 8; rank++) {
                for (int file = 0; file < 8; file++) {
                    boolean isLightSquare = (file + rank) % 2 == 0;
                    Color squareColor = isLightSquare ? new Color(240, 217, 181) : new Color(181, 136, 99);
                    g2d.setColor(squareColor);
                    g2d.fillRect(file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE);

                    // Draw coordinates on the edge squares
                    if (file == 0 || rank == 0) {
                        g2d.setColor(isLightSquare ? new Color(181, 136, 99) : new Color(240, 217, 181));
                        if (file == 0) {
                            g2d.drawString(String.valueOf(rank + 1), 3, (7 - rank) * SQUARE_SIZE + 12);
                        }
                        if (rank == 0) {
                            g2d.drawString(String.valueOf((char)('a' + file)), file * SQUARE_SIZE + SQUARE_SIZE - 12, 8 * SQUARE_SIZE - 3);
                        }
                    }
                }
            }
        }

        private void drawPieces(Graphics2D g2d) {
            for (int rank = 0; rank < 8; rank++) {
                for (int file = 0; file < 8; file++) {
                    int squareIndex = rank * 8 + file;
                    char piece = getPieceCharAt(squareIndex);

                    if (piece != '.') {
                        Image img = pieceImages.get(piece);
                        if (img != null) {
                            g2d.drawImage(img, file * SQUARE_SIZE, (7 - rank) * SQUARE_SIZE, SQUARE_SIZE, SQUARE_SIZE, null);
                        } else {
                            // Fallback for missing images - draw text
                            g2d.setColor(Color.BLACK);
                            g2d.drawString(String.valueOf(piece), file * SQUARE_SIZE + SQUARE_SIZE/2 - 5, (7 - rank) * SQUARE_SIZE + SQUARE_SIZE/2 + 5);
                        }
                    }
                }
            }
        }

        private char getPieceCharAt(int squareIndex) {
            int piece = board.getPiece(squareIndex);
            if (piece == 0) return '.';

            // Map piece codes to characters based on your Board implementation
            switch (piece) {
                case 1: return 'P';  // White pawn
                case 2: return 'N';  // White knight
                case 3: return 'B';  // White bishop
                case 4: return 'R';  // White rook
                case 5: return 'Q';  // White queen
                case 6: return 'K';  // White king
                case 7: return 'p';  // Black pawn
                case 8: return 'n';  // Black knight
                case 9: return 'b';  // Black bishop
                case 10: return 'r'; // Black rook
                case 11: return 'q'; // Black queen
                case 12: return 'k'; // Black king
                default: return '.';
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}