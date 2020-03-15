package oopnet.chess.client;

import oopnet.chess.core.Game;
import oopnet.chess.core.Move;
import oopnet.chess.core.Position;
import oopnet.chess.core.event.*;
import oopnet.chess.core.pieces.ChessPiece;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

class ChessGameUI extends JFrame implements MouseListener, MouseMotionListener {

    private ChessClient client;
    // The current game
    private Game game;

    private JLayeredPane layeredPane;
    private JPanel chessBoard;

    // Current chesspiece (component) being moved
    private JLabel chessPiece;
    private Position from;

    // The position within the dragged piece
    // Basically just where we clicked on the piece
    private int dragAdjustX;
    private int dragAdjustY;

    private Dimension uiDim;

    private final ChessPiece.Color playerColor;

    public ChessGameUI(ChessClient chessClient, Game game, String nickname, ChessPiece.Color playerColor) {
        this.client = chessClient;
        this.game = game;
        this.playerColor = playerColor;

        this.setLayout(new FlowLayout());

        uiDim = new Dimension(600, 600);

        Dimension chessDimension = new Dimension(uiDim.width - 1, uiDim.height);
        layeredPane = new JLayeredPane();
        layeredPane.setPreferredSize(uiDim);

        chessBoard = new JPanel();
        layeredPane.add(chessBoard, JLayeredPane.DEFAULT_LAYER);
        chessBoard.setLayout(new GridLayout(8, 8));
        chessBoard.setPreferredSize(chessDimension);
        chessBoard.setBounds(0, 0, chessDimension.width, chessDimension.height);

        layeredPane.addMouseListener(this);
        layeredPane.addMouseMotionListener(this);

        this.getContentPane().add(layeredPane);

        JPanel other = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.ipadx = 100;
        c.insets.bottom = 10;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.ipady = 15;

        JButton surrenderBtn = new JButton("Surrender");
        surrenderBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        surrenderBtn.addActionListener(l -> {
            int input = JOptionPane.showConfirmDialog(null, "Do you want to surrender?", "Surrender",
                    JOptionPane.YES_NO_OPTION);
            if (input == 0) {
                client.getEventManager().sendEvent(new SurrenderEvent());
            }
        });
        c.gridy++;
        other.add(surrenderBtn, c);

        JButton offerDrawBtn = new JButton("Offer a Draw");
        offerDrawBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        offerDrawBtn.addActionListener(l -> {
            int input = JOptionPane.showConfirmDialog(null, "Do you want to send a draw offer?", "Draw Offer",
                    JOptionPane.YES_NO_OPTION);
            if (input == 0) {
                client.getEventManager().sendEvent(new DrawOfferEvent());
            }
        });
        c.gridy++;
        c.insets.bottom = 60;
        other.add(offerDrawBtn, c);

        JTextArea textArea = new JTextArea("Game launched!");
        textArea.setPreferredSize(new Dimension(60, 180));
        textArea.setEditable(false);
        JScrollPane textAreaScroll = new JScrollPane(textArea);
        c.insets.bottom = 0;
        c.gridy++;
        c.ipady = 180;
        other.add(textAreaScroll, c);

        JTextPane textPane = new JTextPane();
        JScrollPane textPaneScroll = new JScrollPane(textPane);
        textPane.setToolTipText("Type and click send!");
        c.gridy++;
        c.ipady = 20;
        other.add(textPaneScroll, c);

        JButton send = new JButton("Send");
        send.setToolTipText("Click to send a message");
        c.insets = new Insets(0, 5, 0, 0);
        c.ipady = 5;
        c.ipadx = 15;
        c.gridx += 2;
        send.addActionListener(l -> {
            client.getEventManager().sendEvent(new ChatEvent(nickname, textPane.getText()));
            textPane.setText(null);
        });
        client.getEventManager().addListener(event -> {
            if (event instanceof ChatEvent) {
                ChatEvent chatEvent = ((ChatEvent) event);
                textArea.append(System.lineSeparator() + chatEvent.getNickname() + ": " + chatEvent.getMessage());
            }
        });

        other.add(send, c);

        this.getContentPane().add(other);

        loadPieces();

        client.getEventManager().addListener(event -> {
            if (event instanceof PieceMoveEvent) {
                PieceMoveEvent moveEvent = ((PieceMoveEvent) event);
                game.handleMove(moveEvent);
                Move move = moveEvent.getMove(game.getChessboard());
                performMove(move);
            } else if (event instanceof GameEndEvent) {
                JOptionPane.showMessageDialog(null, "Game result: " + ((GameEndEvent) event).getGameResult());
                this.dispose();
            }
        });
    }

    private void loadPieces() {
        for (int i = 0; i < 64; i++) {
            JPanel square = new JPanel(new BorderLayout());
            chessBoard.add(square);
            int row = (i / 8) % 2;
            if (row == 0) {
                square.setBackground(i % 2 == 0 ? Color.lightGray : new Color(96, 96, 96));
            } else {
                square.setBackground(i % 2 == 0 ? new Color(96, 96, 96) : Color.lightGray);
            }
            ChessPiece piece = game.getChessboard().getPiecesList().get(i);
            if (piece != null) {
                // Testing
                JLabel icon = new JLabel(piece.getIcon());
                icon.setFont(new Font(icon.getFont().getName(), Font.PLAIN, 70));
                square.add(icon);
            }
        }
    }

    private void performMove(Move move) {
        // Might be called from a different thread
        SwingUtilities.invokeLater(() -> {
            Component from = positionToComponent(move.getFrom());
            Component to = positionToComponent(move.getTo());

            JPanel fromPanel = ((JPanel) from);
            JLabel piece = (JLabel) fromPanel.getComponent(0);
            // Remove the old piece
            fromPanel.remove(piece);
            if (to instanceof JLabel) {
                Container parent = to.getParent();
                parent.remove(0);
                parent.add(from);
            } else {
                JPanel toPanel = (JPanel) to;
                if (toPanel.getComponents().length > 0) {
                    toPanel.remove(0);
                }
                toPanel.add(piece);
            }
            this.revalidate();
            this.repaint();
        });
    }

    public void mousePressed(MouseEvent e) {
        chessPiece = null;
        Component c = chessBoard.findComponentAt(e.getX(), e.getY());
        if (c instanceof JPanel) {
            return;
        }
        // Only current player can move
        if (game.getCurrentPlayer() != playerColor) {
            return;
        }
        Point parentPoint = c.getParent().getLocation();
        // So we know where the piece was
        from = pointToPosition(parentPoint);
        ChessPiece piece = from.getPiece(game.getChessboard());
        // Can only move own pieces
        if (piece.getColor() != playerColor) {
            return;
        }
        // Calculate moves asynchronously to make it more responsive
        CompletableFuture.runAsync(() -> {
            Set<Position> moves = piece.getLegalMovePositions(game.getChessboard());
            SwingUtilities.invokeLater(() -> {
                moves.stream().map(this::positionToComponent).forEach(component -> {
                    Color old = component.getBackground();
                    component.setBackground(new Color(old.getRed(), old.getGreen(), 0));
                });
            });

        });

        dragAdjustX = parentPoint.x - e.getX();
        dragAdjustY = parentPoint.y - e.getY();
        chessPiece = (JLabel) c;
        chessPiece.setLocation(e.getX() + dragAdjustX, e.getY() + dragAdjustY);
        chessPiece.setSize(chessPiece.getWidth(), chessPiece.getHeight());
        layeredPane.add(chessPiece, JLayeredPane.DRAG_LAYER);

    }

    public void mouseDragged(MouseEvent e) {
        if (chessPiece == null) {
            return;
        }
        // Prevents dragging outside the frame
        if (e.getX() > 0 && e.getY() > 0 && e.getX() < uiDim.width && e.getY() < uiDim.getHeight()) {
            chessPiece.setLocation(e.getX() + dragAdjustX, e.getY() + dragAdjustY);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (chessPiece == null) {
            return;
        }
        Component component = chessBoard.findComponentAt(e.getX(), e.getY());
        // The piece was dragged outside the frame
        if (component == null || component.getParent() == null) {
            // Move the piece back
            component = positionToComponent(from);
        }
        // Temp fix
        // Getting without this
        Point targetPoint = component.getLocation().getX() == 0 && component.getLocation().getY() == 0 ?
                component.getParent().getLocation() : component.getLocation();
        Position pos = pointToPosition(targetPoint);
        ChessPiece piece = from.getPiece(game.getChessboard());
        // whether the move was legal
        Set<Position> moves = piece.getLegalMovePositions(game.getChessboard());
        boolean legalMove = moves.contains(pos);
        if (!legalMove) {
            // Move the piece back
            component = positionToComponent(from);
        }
        // Move the icon
        chessPiece.setVisible(false);
        if (component instanceof JLabel) {
            Container parent = component.getParent();
            parent.remove(0);
            parent.add(chessPiece);
        } else {
            Container parent = (Container) component;
            parent.add(chessPiece);
        }
        chessPiece.setVisible(true);
        // Reset colors
        moves.stream().map(this::positionToComponent).forEach(comp -> {
            Color old = comp.getBackground();
            // Gray always has same values so just use green to get the old blue
            comp.setBackground(new Color(old.getRed(), old.getGreen(), old.getGreen()));
        });
        // Only do game logic if it was legal move
        if (legalMove) {
            // Chess logic
            Move move = new Move(game.getChessboard(), piece, pos);
            System.out.println("New Move: " + move.toString(game.getChessboard()));
            PieceMoveEvent moveEvent = new PieceMoveEvent(move);

            // The server doesn't send the move event to us so call it manually
            game.handleMove(moveEvent);
            client.getEventManager().sendEvent(moveEvent);
        }
    }

    private Component positionToComponent(Position pos) {
        // see Position#getPiece(ChessPiece[][]), same stuff
        int i = 7 - pos.getRank() + 1;
        int j = pos.getFileAsInt() - 1;

        int index = i * 8 + j;
        Component comp = chessBoard.getComponents()[index];
        return comp;
    }

    private Position pointToPosition(Point point) {
        for (int i = 0; i < chessBoard.getComponents().length; i++) {
            Component comp = chessBoard.getComponents()[i];
            // For some reason chessboard.findComponentAt didn't work
            if ((point.x == comp.getX() && point.y == comp.getY())) {
                // Here row is 0-7
                int row = i / 8;
                // Convert the row as top row is 8 and bottom 1
                row = 8 - row;
                int file = (i % 8) + 1;
                return new Position(row, file);
            }
        }
        // Should never happen
        System.out.println("Something went wrong. Couldn't find chess position at UI position " + point);
        return null;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}
 