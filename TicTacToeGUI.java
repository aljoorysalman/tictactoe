import java.awt.*;
import javax.swing.*;


public class TicTacToeGUI extends JFrame {
    private Game game; 
    private boolean gameOver = false; // flag to track if the game has ended
    private Timer aiTimer; // Timer to manage AI move delays
    private JButton[] gameGridButtons = new JButton[9];
    private JComboBox<String> xPlayerSelector;
    private JComboBox<String> oPlayerSelector;
    private JComboBox<String> DifficultySelector;
   
    //These are the main methods used to create window and interface. 
   public TicTacToeGUI() {
    game = new Game();
    CreateWindow();
    CreateGameInterface();
}
//These are the window Settings .
    private void CreateWindow () {
        setTitle("Tic Tac Toe Game");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        getContentPane().setBackground(Color.BLACK);
        setLayout(new BorderLayout());
    }
//These are the interface Settings .
    private void CreateGameInterface () {
        add(HeaderSection(), BorderLayout.NORTH);
        add(BoardSection(), BorderLayout.CENTER);
        add(ControlSection(), BorderLayout.SOUTH);
    } 
//This method is used to create the header section of the game and selection menu for (X or O) to start the game.
    private JPanel HeaderSection() {
        JPanel header = new JPanel(new GridLayout(2, 1));
        header.setBackground(Color.BLACK);

        JLabel mainTitle = new JLabel("Tic Tac Toe Game", SwingConstants.CENTER);
        mainTitle.setForeground(Color.WHITE);
        mainTitle.setFont(new Font("Verdana", Font.BOLD, 26));

        JPanel mainPanel = new JPanel(); 
        mainPanel.setBackground(Color.BLACK);
        xPlayerSelector = new JComboBox<>(
        new String[]{"Human", "AlphaBeta", "MCTS"});

       oPlayerSelector = new JComboBox<>(
        new String[]{"Human", "AlphaBeta", "MCTS"});
        DifficultySelector = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
                
        mainPanel.add(new JLabel("X Type:"));
        mainPanel.add(xPlayerSelector);
        mainPanel.add(new JLabel("Difficulty:"));
        mainPanel.add(DifficultySelector);
        mainPanel.add(new JLabel("O Type:"));
        mainPanel.add(oPlayerSelector);
        header.add(mainTitle);
        header.add(mainPanel);
        return header;
    }
// This method is used to create the game grid.
    private JPanel BoardSection() {
        JPanel board = new JPanel(new GridLayout(3, 3, 8, 8));
        board.setBackground(Color.BLACK);
        board.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 9; i++) {
            gameGridButtons[i] = new JButton("");
            gameGridButtons[i].setFont(new Font("Arial", Font.BOLD, 55));
            gameGridButtons[i].setBackground(new Color(20, 20, 20));
            gameGridButtons[i].setFocusPainted(false);
            gameGridButtons[i].setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

            final int pos = i;
            gameGridButtons[i].addActionListener(e -> executeMove(pos));
            board.add(gameGridButtons[i]);
        }
        return board;
    }
// This method is used to create start and reset buttons.
    private JPanel ControlSection() {
        JPanel actions = new JPanel();
        actions.setBackground(Color.BLACK);

        JButton startBtn = new JButton("START");
        startBtn.setBackground(Color.GREEN);
        startBtn.setForeground(Color.BLACK);
        startBtn.setPreferredSize(new Dimension(120, 40));

        JButton resetBtn = new JButton("RESET");
        resetBtn.setBackground(Color.RED);
        resetBtn.setForeground(Color.WHITE);
        resetBtn.setPreferredSize(new Dimension(120, 40));

        startBtn.addActionListener(e -> {

    if (aiTimer != null) aiTimer.stop(); // Stop any existing AI timer before starting a new 

        ResetGame(); // Reset the game state before starting


    String xType = (String) xPlayerSelector.getSelectedItem(); // Get the selected player type for X
    String oType = (String) oPlayerSelector.getSelectedItem(); // Get the selected player type for O

    boolean anyAI = !xType.equals("Human") || !oType.equals("Human"); // Check if either player is an AI

    if (anyAI) {
        startAIGameLoop();
    }
});

        resetBtn.addActionListener(e -> ResetGame() );
        actions.add(startBtn);
        actions.add(resetBtn);
        return actions;
    }
// This method is used to execute the move when a player clicks on a cell. 
    private void executeMove(int index) {
     if (!isHumanTurn()) return; // Ignore clicks if it's not human's turn
    int row = index / 3;
    int col = index % 3;

    char current = game.getCurrentPlayer(); // Get the current player ('X' or 'O')

    boolean valid = game.applyMove(row, col, current); // Attempt to apply the move; returns false if the cell is not empty

    if (!valid) return;

    refreshBoard(); // Update the GUI to reflect the new move
    
    if (game.checkWinner(current)) {
        gameOver = true; 
        JOptionPane.showMessageDialog(this, current + " wins!"); 
        return;
    }
    if (game.endTime()) { 
        gameOver = true;
        JOptionPane.showMessageDialog(this, "Time's up! It's a draw!");
        return;
    }

    game.switchPlayer(); // Switch to the other player
    if (!isHumanTurn() && !gameOver) {
    startAIGameLoop(); 
}}

private int getDepth(String difficulty) {

    switch (difficulty) {
        case "Easy": return 2;
        case "Medium": return 5;
        default: return 10;
    }
}

private int getSimulations(String difficulty) {
    switch (difficulty) {
        case "Easy": return 500;
        case "Medium": return 1000;
        default: return 2000;
    }
}

    //This method used to clearing all button texts and resetting the data array to start a new match.
    private void ResetGame() {
    game.reset();
    gameOver = false;
    refreshBoard();
}

private void refreshBoard() {    
    char[][] board = game.getBoard();
    for (int r = 0; r < 3; r++) {
        for (int c = 0; c < 3; c++) {
            int index = r * 3 + c;
            char value = board[r][c];
            if (value == ' ') {
                gameGridButtons[index].setText("");
            } else {
                gameGridButtons[index].setText(String.valueOf(value));

                if (value == 'X') {
                    gameGridButtons[index].setForeground(Color.BLUE);
                } else {
                    gameGridButtons[index].setForeground(Color.RED);
                }
            }
        }
    }
}

private void startAIGameLoop() {
    aiTimer = new Timer(600, e -> {
        if (gameOver) { // Stop the timer if the game has ended
            aiTimer.stop();
            return;
        }

        playAIMove();
    });

    aiTimer.start();
}

private void playAIMove() {

    char current = game.getCurrentPlayer();

    String player = (current == 'X') ? (String) xPlayerSelector.getSelectedItem() : (String) oPlayerSelector.getSelectedItem();
     String difficulty = (String) DifficultySelector.getSelectedItem();
     int[] move = null;

    if (player.equals("AlphaBeta")) {
      int depth = getDepth(difficulty);
       AlphaBetaAI ai = new AlphaBetaAI(depth); // create an instance of the AlphaBetaAI with the selected depth
       move = ai.getMove(game, current);  // get the best move for the current game state and player

    } else if (player.equals("MCTS")) { 
     int sims = getSimulations(difficulty); 

       MCTS ai = new MCTS(); // create an instance of the MCTS AI
       move = ai.getMove(game, current, sims); // get the best move for the current game state and player using the selected number of simulations
    }

    if (move == null) return;

    game.applyMove(move[0], move[1], current); 

    refreshBoard();

    if (game.checkWinner(current)) {
    gameOver = true;
    if (aiTimer != null) aiTimer.stop();
    JOptionPane.showMessageDialog(this, current + " wins!");
    return;
}

    if (game.endTime()) {
        gameOver = true;
        if (aiTimer != null) aiTimer.stop();
        JOptionPane.showMessageDialog(this, "Time's up! It's a draw!");
        return;
    }

    game.switchPlayer();
}

private boolean isHumanTurn() { // Check if the current player is human based on the selection
    char current = game.getCurrentPlayer();

    if (current == 'X') {
        return xPlayerSelector.getSelectedItem().equals("Human");
    } else {
        return oPlayerSelector.getSelectedItem().equals("Human");
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TicTacToeGUI().setVisible(true);
        });
    }
}



