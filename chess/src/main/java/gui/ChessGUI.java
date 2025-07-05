package gui;

import core.board.Board;
import core.board.Move;
import core.engine.ChessEngine;
import core.engine.SearchResult;
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
import java.io.File;
import java.io.FileWriter;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.List;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import core.fen.FenParser;
import core.fen.FenGenerator;
import core.fen.FenConstants;

public class ChessGUI extends JFrame {
    private Board board;
    private final ChessEngine engine;
    private final BoardPanel boardPanel;
    private Square selectedSquare = null;
    private JTextArea moveHistoryArea;
    private JLabel gameStatusLabel;
    private JTextField fenInputField;

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
        JPanel fenPanel = new JPanel(new BorderLayout());
        JButton applyFenBtn = new JButton("Apply FEN");
        fenInputField = new JTextField(FenConstants.STARTING_POSITION);

        // Add a control panel
        JPanel controlPanel = new JPanel(new BorderLayout());

        // Button panel
        JPanel buttonPanel = new JPanel();
        JButton newGameBtn = new JButton("New Game");
        JButton engineMoveBtn = new JButton("Engine Move");
        JButton undoBtn = new JButton("Undo");
        JButton exportGameBtn = new JButton("Export Game");

        newGameBtn.addActionListener(e -> resetBoard());
        engineMoveBtn.addActionListener(e -> makeEngineMove());
        undoBtn.addActionListener(e -> undoMove());
        exportGameBtn.addActionListener(e -> exportGameHistory());

        fenPanel.add(new JLabel("FEN Position: "), BorderLayout.WEST);
        fenPanel.add(fenInputField, BorderLayout.CENTER);
        fenPanel.add(applyFenBtn, BorderLayout.EAST);
        buttonPanel.add(newGameBtn);
        buttonPanel.add(engineMoveBtn);
        buttonPanel.add(undoBtn);
        buttonPanel.add(exportGameBtn);
        controlPanel.add(buttonPanel, BorderLayout.NORTH);
        applyFenBtn.addActionListener(e -> applyFenPosition());
        JButton copyFenBtn = new JButton("Copy Current FEN");
        buttonPanel.add(copyFenBtn);
        copyFenBtn.addActionListener(e -> copyCurrentFen());


        // Move history area
        moveHistoryArea = new JTextArea(5, 20);
        moveHistoryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        controlPanel.add(fenPanel, BorderLayout.SOUTH);
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
    private void exportGameHistory() {
        // Create a file chooser dialog
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Game History");

        // Add options for PGN and FEN history
        String[] options = {"PGN (moves only)", "FEN Sequence (all positions)"};
        int choice = JOptionPane.showOptionDialog(this,
                "Choose export format:",
                "Export Game History",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        if (choice == 0) {
            // PGN export
            fileChooser.setFileFilter(new FileNameExtensionFilter("PGN files (*.pgn)", "pgn"));
            fileChooser.setSelectedFile(new File("game.pgn"));
        } else if (choice == 1) {
            // FEN sequence export
            fileChooser.setFileFilter(new FileNameExtensionFilter("FEN files (*.fen)", "fen"));
            fileChooser.setSelectedFile(new File("game.fen"));
        } else {
            // User cancelled the format selection
            return;
        }

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            try {
                if (choice == 0) {
                    // Export PGN
                    exportPGN(selectedFile);
                } else {
                    // Export FEN sequence
                    exportFENSequence(selectedFile);
                }

                // Log the export
                logMove("Game exported to " + selectedFile.getName());

                // Show confirmation
                JOptionPane.showMessageDialog(this,
                        "Game history exported to " + selectedFile.getAbsolutePath(),
                        "Export Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting game: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void applyFenPosition() {
        String fen = fenInputField.getText().trim();
        if (fen.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid FEN string",
                    "Invalid FEN",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create a new board to validate the FEN first
        Board tempBoard = new Board();
        boolean success = FenParser.loadPosition(tempBoard, fen);

        if (success) {
            // Apply to the actual board
            FenParser.loadPosition(board, fen);
            boardPanel.repaint();
            selectedSquare = null;
            logMove("Applied FEN position: " + fen);
            updateGameStatus();
        } else {
            JOptionPane.showMessageDialog(this,
                    "The FEN string is invalid. Please check and try again.",
                    "Invalid FEN",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    private void copyCurrentFen() {
        String currentFen = FenGenerator.generateFen(board);

        // Copy to clipboard
        StringSelection selection = new StringSelection(currentFen);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);

        // Update the FEN input field
        fenInputField.setText(currentFen);

        // Log the action
        logMove("Copied current FEN: " + currentFen);

        // Optional: Show a confirmation message
        JOptionPane.showMessageDialog(this,
                "Current FEN position copied to clipboard!",
                "FEN Copied",
                JOptionPane.INFORMATION_MESSAGE);
    }
    private void exportPGN(File file) throws IOException {
        StringBuilder pgn = new StringBuilder();

        // Add PGN headers
        pgn.append("[Event \"Chess Game\"]\n");
        pgn.append("[Site \"?\"]\n");
        pgn.append("[Date \"").append(java.time.LocalDate.now()).append("\"]\n");
        pgn.append("[Round \"?\"]\n");
        pgn.append("[White \"Human\"]\n");
        pgn.append("[Black \"Computer\"]\n");

        // Add game result
        String result = "*";
        if (board.isCheckmate()) {
            result = board.isWhiteToMove() ? "0-1" : "1-0";
        } else if (board.isStalemate() || board.isDraw()) {
            result = "1/2-1/2";
        }
        pgn.append("[Result \"").append(result).append("\"]\n\n");

        // Get moves in PGN format
        List<String> pgnMoves = board.getGameHistoryAsPGN();

        // Format moves with proper spacing
        for (int i = 0; i < pgnMoves.size(); i++) {
            pgn.append(pgnMoves.get(i));
            pgn.append(" ");

            // Add line breaks every 5 move pairs for readability
            if (i % 10 == 9) {
                pgn.append("\n");
            }
        }

        // Add the result at the end
        pgn.append(result);

        // Write to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(pgn.toString());
        }
    }
    private void exportFENSequence(File file) throws IOException {
        // Get the list of FEN strings representing the game history
        List<String> fenHistory = board.getGameHistoryAsFEN();

        // Join them with newlines
        StringBuilder fenSequence = new StringBuilder();
        for (String fen : fenHistory) {
            fenSequence.append(fen).append("\n");
        }

        // Write to file
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(fenSequence.toString());
        }
    }
    private void resetBoard() {
        board = new Board();
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
            String algebraicMove = generateAlgebraicNotation(bestMove);
            logMove("Engine: " + algebraicMove);
            board.makeMove(bestMove);
            boardPanel.repaint();
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
    private String generateAlgebraicNotation(Move move) {
        // Clone the current board to preserve its state
        Board tempBoard = board.clone();
        // Generate notation before making the move
        return MoveNotation.toAlgebraic(tempBoard, move);
    }
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
    private boolean isGameOver() {
        return board.isCheckmate() || board.isStalemate() || board.isDraw();
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
                    String algebraicMove = generateAlgebraicNotation(matchingMove);
                    logMove("Human: " + algebraicMove);
                    board.makeMove(matchingMove);
                    selectedSquare = null;
                    possibleMoves.clear();  // Clear highlights
                    repaint();

                    // Check game state after human move
                    updateGameStatus();

                    // Make engine move if game is not over
                    if (!isGameOver()) {
                        //SwingUtilities.invokeLater(() -> makeEngineMove());
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