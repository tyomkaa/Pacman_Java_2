import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GameWindow extends JFrame implements Serializable {

    private int boardSize;
    private Pacman pacman;
    private Cell[][] board;
    private int lives = 4;  // Number of lives
    private int score = 0;  // Player score
    private int time = 0;   // Time in the game (in seconds)
    public boolean inGame = true;
    private GameBoard gameBoard;
    private Thread scoreThread;

    private ArrayList<PowerUp> powerUps;
    private ArrayList<Ghost> ghosts;

    public GameWindow(int size) {
        this.boardSize = size;
        pacman = new Pacman(20);
        ghosts = new ArrayList<>();
        initializeBoard();
        gameBoard = new GameBoard(board);
        powerUps = new ArrayList<>();
        initialize(); 
        initializeGhosts();
    }

    private void startPowerUpThread() {
        Thread powerUpThread = new Thread(() -> {
            while (inGame) {
                try {
                    Thread.sleep(5000); 
                    dropPowerUp();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        powerUpThread.start();
    }

    private void dropPowerUp() {
        PowerUp powerUp = new PowerUp(pacman.getCellSize());
        positionPowerUpRandomly(powerUp);
        powerUps.add(powerUp);
        gameBoard.add(powerUp);
        gameBoard.repaint();
    }

    private void positionPowerUpRandomly(PowerUp powerUp) {
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(boardSize);
            col = random.nextInt(boardSize);
        } while (board[row][col] == Cell.WALL);
        int x = col * pacman.getCellSize();
        int y = row * pacman.getCellSize();
        powerUp.setBounds(x, y, pacman.getCellSize(), pacman.getCellSize());
    }

    private void initializeGhosts() {
        int numberOfGhosts = 4; 
        for (int i = 0; i < numberOfGhosts; i++) {
            Ghost ghost = new Ghost(pacman.getCellSize());
            ghosts.add(ghost);
            positionGhostRandomly(ghost);
            gameBoard.add(ghost); 
        }
    }

    private void positionGhostRandomly(Ghost ghost) {
        Random random = new Random();
        int row, col;
        do {
            row = random.nextInt(boardSize);
            col = random.nextInt(boardSize);
        } while (board[row][col] == Cell.WALL);
        int x = col * pacman.getCellSize();
        int y = row * pacman.getCellSize();
        ghost.setBounds(x, y, pacman.getCellSize(), pacman.getCellSize()); 
    }

    public void initialize() {
        setTitle("Pacman Game");
        int windowSize = boardSize * pacman.getCellSize();
        setSize(windowSize, windowSize);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTable jTable = new JTable(new CustomTableModel(boardSize));
        jTable.setRowHeight(pacman.getCellSize()); 
        jTable.setDefaultRenderer(Object.class, new CellRenderer()); 
        JScrollPane jScrollPane = new JScrollPane(jTable);
        add(jScrollPane, BorderLayout.CENTER);

        gameBoard.setFocusable(true);
        gameBoard.requestFocusInWindow();

        jTable.addKeyListener(new PacmanKeyListener());

        pacman = new Pacman(20);

        gameBoard.add(pacman);
        add(gameBoard, BorderLayout.CENTER);

        gameBoard.setFocusable(true);
        gameBoard.requestFocusInWindow();

        gameBoard.addKeyListener(new PacmanKeyListener());

        gameBoard.setFocusable(true);

        Thread pacmanThread = new Thread(() -> {
            while (true) { 
                try {
                    Thread.sleep(pacman.getAnimationDelay()); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pacman.move();
                SwingUtilities.invokeLater(() -> {
                    gameBoard.repaint(); 
                });
            }
        });
        pacmanThread.start();

        Thread ghostThread = new Thread(() -> {
            while (true) { 
                try {
                    Thread.sleep(250); 
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (Ghost ghost : ghosts) {
                    ghost.move(board);
                    SwingUtilities.invokeLater(() -> {
                        ghost.repaint(); 
                    });
                }
            }
        });
        ghostThread.start();

        scoreThread = new Thread(() -> {
            while (inGame) { 
                try {
                    Thread.sleep(5000);
                    score += 10; 
                    SwingUtilities.invokeLater(() -> {
                        gameBoard.repaint(); 
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); 
                }
            }
        });
        scoreThread.start();

        startPowerUpThread();


        setVisible(true);
    }

    private void initializeBoard() {
        board = new Cell[boardSize][boardSize];
        Random random = new Random();

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (random.nextDouble() < 0.15) {
                    board[row][col] = Cell.WALL;
                } else {
                    board[row][col] = Cell.EMPTY;
                }
            }
        }

        int pacmanRow, pacmanCol;
        do {
            pacmanRow = random.nextInt(boardSize);
            pacmanCol = random.nextInt(boardSize);
        } while (board[pacmanRow][pacmanCol] == Cell.WALL);

        pacman.setLocation(pacmanCol * pacman.getCellSize(), pacmanRow * pacman.getCellSize());
    }

    private class PacmanKeyListener implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
            
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
                if (key == KeyEvent.VK_UP){
                    pacman.setDirection(3); 
                }else if (key == KeyEvent.VK_DOWN){
                    pacman.setDirection(1);
                }else if (key == KeyEvent.VK_LEFT){
                    pacman.setDirection(2);
                }else if (key == KeyEvent.VK_RIGHT){
                    pacman.setDirection(0);
                }else if (key == KeyEvent.VK_Q && e.isControlDown() && e.isShiftDown()) {
                    serializeScore();
                    MainMenu mainMenu = new MainMenu();
                    mainMenu.setVisible(true);
                    Component component = (Component) e.getSource();
                    Window window = SwingUtilities.windowForComponent(component);
                    window.dispose();
                }
        }

        @Override
        public void keyReleased(KeyEvent e) {
           
        }
    }

    private static class CustomTableModel extends AbstractTableModel {
        private int size;

        public CustomTableModel(int size) {
            this.size = size;
        }

        @Override
        public int getRowCount() {
            return size;
        }

        @Override
        public int getColumnCount() {
            return size;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return "";
        }
    }

    private class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (board[row][column] == Cell.WALL) {
                rendererComponent.setBackground(Color.BLUE);
            } else {
                rendererComponent.setBackground(Color.BLACK);
            }
            return rendererComponent;
        }
    }

    private class GameBoard extends JPanel {
        private Cell[][] board;

        public GameBoard(Cell[][] board) {
            this.board = board;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            int cellSize = 20;  
            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    if (board[row][col] == Cell.WALL) {
                        g.setColor(Color.BLUE);
                    } else {
                        g.setColor(Color.BLACK);
                    }
                    g.fillRect(col * cellSize, row * cellSize, cellSize, cellSize);
                }
            }

            g.setColor(Color.WHITE); // Выбираем цвет текста
            g.drawString("Lives: " + lives, 10, getHeight() - 50); 
            g.drawString("Score: " + score, 10, getHeight() - 35); 
            g.drawString("Time: " + time + "s", 10, getHeight() - 20); 

            for (PowerUp powerUp : powerUps) {
                powerUp.paintComponent(g);
            }
        }
    }

    public class Pacman extends JPanel {
        private int direction; 
        private int faceDirection; 
        private int cellSize;
        private int mouthAngle; 
        private boolean isMouthOpen; 
        private int animationDelay;
        private boolean isInvincible = false; 
        private int invincibilityDuration = 10000;
        private boolean canGoThroughWall = false; 
        private int goThroughWallDuration = 5000;
        private int originalAnimationDelay;

        public int getAnimationDelay() {
            return animationDelay;
        }

        public Pacman(int cellSize) {
            this.cellSize = cellSize;
            this.originalAnimationDelay = 150; 
            this.animationDelay = originalAnimationDelay;
            setPreferredSize(new Dimension(cellSize, cellSize));
            setOpaque(false); 
            mouthAngle = 45;
            isMouthOpen = true; 
            faceDirection = 0; 
            Thread mouthAnimationThread = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(100); 
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isMouthOpen = !isMouthOpen;
                    SwingUtilities.invokeLater(() -> {
                        repaint(); 
                    });
                }
            });
            mouthAnimationThread.start();
        }

        public int getFaceDirection() {
            return faceDirection;
        }

        public void move() {
            int currentX = getX();
            int currentY = getY();

            try {
                Thread.sleep(animationDelay); 
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int newX = currentX;
            int newY = currentY;

            switch (direction) {
                case 0:
                    newX = Math.min(currentX + cellSize, (boardSize - 1) * cellSize);
                    faceDirection = 0;
                    break;
                case 1:
                    newY = Math.min(currentY + cellSize, (boardSize - 1) * cellSize);
                    faceDirection = 3; 
                    break;
                case 2:
                    newX = Math.max(currentX - cellSize, 0);
                    faceDirection = 2; 
                    break;
                case 3:
                    newY = Math.max(currentY - cellSize, 0);
                    faceDirection = 1; 
                    break;
            }

            if (isValidMove(newX, newY)) {
                setLocation(newX, newY);
                repaint();
            }

            checkPowerUpCollision(powerUps);

            Rectangle pacmanBounds = new Rectangle(getX(), getY(), getWidth(), getHeight());
            for (Ghost currentGhost : ghosts) {
                Rectangle ghostBounds = new Rectangle(currentGhost.getX(), currentGhost.getY(), currentGhost.getWidth(), currentGhost.getHeight());
                if (pacmanBounds.intersects(ghostBounds)) {
                    if (!isInvincible) {
                        lives--; 
                        if (lives <= 0) {
                            gameOver();
                        } else {
                            positionPacmanRandomly();
                            for (Ghost resetGhost : ghosts) {
                                positionGhostRandomly(resetGhost);
                            }
                        }
                    }
                    break;
                }
            }
        }

        private void positionPacmanRandomly() {
            Random random = new Random();
            int row, col;
            do {
                row = random.nextInt(boardSize);
                col = random.nextInt(boardSize);
            } while (board[row][col] == Cell.WALL);
            pacman.setLocation(col * pacman.getCellSize(), row * pacman.getCellSize());
        }

        public int getCellSize() {
            return cellSize;
        }

        public void setDirection(int direction) {
            this.direction = direction;
            this.faceDirection = direction;
        }

        private boolean isValidMove(int x, int y) {
            int pacmanRow = y / cellSize;
            int pacmanCol = x / cellSize;

            if (pacmanRow < 0 || pacmanRow >= boardSize || pacmanCol < 0 || pacmanCol >= boardSize) {
                return false;
            }

            for (Component component : getParent().getComponents()) {
                if (component instanceof Pacman && component != this) {
                    Rectangle pacmanBounds = component.getBounds();
                    Rectangle newBounds = new Rectangle(x, y, getWidth(), getHeight());

                    if (pacmanBounds.intersects(newBounds)) {
                        return false;
                    }
                }
            }

            if (canGoThroughWall) {
                return true;
            }

            return board[pacmanRow][pacmanCol] != Cell.WALL;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.setColor(Color.YELLOW);

            int startAngle;
            int arcAngle;

            if (isMouthOpen) {
                switch (faceDirection) {
                    case 0: // Вправо
                        startAngle = 45;
                        break;
                    case 1: // Вниз
                        startAngle = 135;
                        break;
                    case 2: // Влево
                        startAngle = 225;
                        break;
                    case 3: // Вверх
                        startAngle = 315;
                        break;
                    default:
                        startAngle = 0;
                }
                arcAngle = 360 - 2 * mouthAngle;
            } else {
                startAngle = 0;
                arcAngle = 360;
            }

            g.fillArc(0, 0, getWidth(), getHeight(), startAngle, arcAngle);

            int eyeSize = cellSize / 5;
            int eyeOffsetX = cellSize / 4;
            int eyeOffsetY = cellSize / 4;
            g.setColor(Color.BLACK);
            switch (getFaceDirection()) {
                case 0:
                    g.fillOval(eyeOffsetX, eyeOffsetY, eyeSize, eyeSize);
                    break;
                case 1: // Вниз
                    g.fillOval(eyeOffsetX, getHeight() - eyeOffsetY - eyeSize, eyeSize, eyeSize);
                    break;
                case 2: // Влево
                    g.fillOval(getWidth() - eyeOffsetX - eyeSize, eyeOffsetY, eyeSize, eyeSize);
                    break;
                case 3: // Вверх
                    g.fillOval(eyeOffsetX, eyeOffsetY, eyeSize, eyeSize);
                    break;
            }

            if (isInvincible) {
                g.setColor(Color.MAGENTA); 
                g.drawOval(0, 0, getWidth(), getHeight()); 
            }

            if (canGoThroughWall) {
                g.setColor(new Color(0, 255, 0, 128)); 
                g.fillRect(0, 0, getWidth(), getHeight()); 
            }
        }

        public void checkPowerUpCollision(ArrayList<PowerUp> powerUps) {
            Rectangle pacmanBounds = new Rectangle(getX(), getY(), getWidth(), getHeight());
            Iterator<PowerUp> powerUpIterator = powerUps.iterator();

            while (powerUpIterator.hasNext()) {
                PowerUp powerUp = powerUpIterator.next();
                Rectangle powerUpBounds = new Rectangle(powerUp.getX(), powerUp.getY(), powerUp.getWidth(), powerUp.getHeight());

                if (pacmanBounds.intersects(powerUpBounds)) {
                    powerUpIterator.remove(); 
                    gameBoard.remove(powerUp); 

                    int randomUpgrade = new Random().nextInt(5); 
                    switch (randomUpgrade) {
                        case 0: 
                            System.out.println("speed boost");
                            speedBoost(); 
                            break;
                        case 1: 
                            System.out.println("invincible");
                            invincible(); 
                            break;
                        case 2: 
                            System.out.println("go through wall");
                            goThroughWall(); 
                            break;
                        case 3: 
                            System.out.println("extra life");
                            extraLife(); 
                            break;
                        case 4:
                            System.out.println("extra score");
                            extraScore();
                            break;
                    }
                }
            }
        }



        private void speedBoost() {
            animationDelay /= 2; 
            Thread speedBoostThread = new Thread(() -> {
                try {
                    Thread.sleep(5000); 
                    animationDelay = originalAnimationDelay; 
                    System.out.println("boost finished");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            speedBoostThread.start();
        }



        private void invincible() {
            isInvincible = true;
            Thread invincibilityThread = new Thread(() -> {
                try {
                    Thread.sleep(invincibilityDuration);
                    deactivateInvincibility();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            invincibilityThread.start();
        }

        public void deactivateInvincibility() {
            isInvincible = false;
            System.out.println("deactivated");
        }

        public void goThroughWall() {
            canGoThroughWall = true;
            Thread goThroughWallThread = new Thread(() -> {
                try {
                    Thread.sleep(goThroughWallDuration);
                    deactivateGoThroughWall();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            goThroughWallThread.start();
        }

        public void deactivateGoThroughWall() {
            canGoThroughWall = false;
            System.out.println("deactivated");
        }

        private void extraLife(){
            lives++;
        }

        private void extraScore(){
            score += 10;
        }


    }

    public void gameOver() {
        JOptionPane.showMessageDialog(this, "Game Over! Your score: " + score);
        serializeScore();
        this.dispose();
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new MainMenu().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public class Ghost extends JPanel {
        private int direction; 
        private int cellSize;
        private Random random = new Random();

        public Ghost(int cellSize) {
            this.cellSize = cellSize;
            setPreferredSize(new Dimension(cellSize, cellSize));
            setOpaque(false);
        }

        public void move(Cell[][] board) {
            direction = random.nextInt(4);

            int x = getX();
            int y = getY();
            switch (direction) {
                case 0: x += cellSize; break;
                case 1: y += cellSize; break;
                case 2: x -= cellSize; break;
                case 3: y -= cellSize; break;
            }

            if (isValidMove(x, y, board)) {
                setLocation(x, y);
            }
        }

        private boolean isValidMove(int x, int y, Cell[][] board) {
            int row = y / cellSize;
            int col = x / cellSize;
            if (row < 0 || col < 0 || row >= board.length || col >= board[row].length || board[row][col] == Cell.WALL) {
                return false;
            }
            return true;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.RED);
            g.fillRect(0, 0, cellSize, cellSize); // Draw the ghost body

            g.setColor(Color.BLACK);

            g.fillOval(cellSize / 4, cellSize / 4, cellSize / 6, cellSize / 6); // Left eye
            g.fillOval(cellSize * 5 / 8, cellSize / 4, cellSize / 6, cellSize / 6); // Right eye

            g.setColor(Color.BLACK);
            g.drawLine(cellSize / 4, cellSize / 2, cellSize / 2, 3 * cellSize / 4);
            g.drawLine(cellSize / 2, 3 * cellSize / 4, 3 * cellSize / 4, cellSize / 2);

            g.drawLine(cellSize / 4, cellSize / 4, cellSize / 2, cellSize / 4);
            g.drawLine(cellSize / 2, cellSize / 4, 3 * cellSize / 4, cellSize / 4);

        }

    }

    public class PowerUp extends JPanel {
        private int cellSize;

        public PowerUp(int cellSize) {
            this.cellSize = cellSize;
            setPreferredSize(new Dimension(cellSize, cellSize));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.GREEN);
            g.fillOval(0, 0, cellSize, cellSize);
        }
    }




    private enum Cell {
        EMPTY,
        WALL
    }


    private void serializeScore() {
        String playerName = JOptionPane.showInputDialog(null, "Enter your name:");
        Player player = new Player(playerName, score);

        ArrayList<Player> playerArrayList = new ArrayList<>();
        try {
            HighScore.readFile("score.ser", playerArrayList);
        } catch (IOException e) {
            e.printStackTrace();
        }

        playerArrayList.add(player);

        try (FileOutputStream fileOut = new FileOutputStream("score.ser");
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {

            for (Player p : playerArrayList) {
                objectOut.writeObject(p);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
