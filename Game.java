import java.util.*;

public class Game {

   
    private char currentPlayer;
      char[][] board; //3*3 board
    Queue<int[]> Xhistory; // track last 3 moves for x
    Queue<int[]> Ohistory; // track last 3 moves for o 
    int max = 3;
     long startTime;
     int timeLimit = 20 * 60 * 1000; // 20 minutes in milliseconds


    public Game() {
        board         = new char[3][3];
        Xhistory = new LinkedList<>();
        Ohistory = new LinkedList<>();
        startTime = System.currentTimeMillis();  
         currentPlayer = 'X';
        // Initialize board with spaces
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    
public boolean applyMove(int row, int col, char player) {
    // Check if cell is empty
    if (board[row][col] != ' ') {
        return false;
    }

    //  removing oldest move
    if (player == 'X') {
        if (Xhistory.size() == max) {
            int[] oldMove = Xhistory.poll();   // dequeue
            board[oldMove[0]][oldMove[1]] = ' '; // clear board
        }
    } else {
        if (Ohistory.size() == max) {
            int[] oldMove = Ohistory.poll();   // dequeue
            board[oldMove[0]][oldMove[1]] = ' '; // clear board
        }
    }

    // Place new piece
    board[row][col] = player;

    // Save position in queue
    if (player == 'X') {
        Xhistory.add(new int[]{row, col});
    } else {
        Ohistory.add(new int[]{row, col});
    }
     
    return true;
}



    
    public boolean checkWinner(char currentPlayer) {

        for (int i = 0; i < 3; i++) { //rows
            if (board[i][0] == currentPlayer && board[i][1] == currentPlayer &&  board[i][2] == currentPlayer) return true;
        }

        for (int j = 0; j < 3; j++) { // col
            if (board[0][j] == currentPlayer && board[1][j] == currentPlayer && board[2][j] == currentPlayer) return true;
        }
// diagonals 
        if (board[0][0] == currentPlayer &&  board[1][1] == currentPlayer &&  board[2][2] == currentPlayer) return true;

        if (board[0][2] == currentPlayer &&   board[1][1] == currentPlayer &&  board[2][0] == currentPlayer) return true;

        return false;
    }

    public void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }
    public boolean endTime() {
    return (System.currentTimeMillis() - startTime >= timeLimit);
}

  
 public Queue<int[]> getHistory(char player) {
        return (player == 'X') ? Xhistory : Ohistory;
    }

    public char getCurrentPlayer() { return currentPlayer; }

    public char[][] getBoard() { return board; }

    public void reset() {
     
        startTime = System.currentTimeMillis();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
        Xhistory.clear();
        Ohistory.clear();
        currentPlayer = 'X';
    }
    
         public List<int[]> getLegalMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                if (board[r][c] == ' ') {
                    moves.add(new int[]{r, c}); }
                   }
                }
                     return moves;
                 }


                 
public int evaluate(char player) {
   /*  Looks at the whole board
Splits it into lines (rows, cols, diagonals)
Adds all line scores together */

    char opponent = (player == 'X') ? 'O' : 'X'; 

    // Terminal states first
    if (checkWinner(player)) return 1000; // if true return high score for AI win
    if (checkWinner(opponent)) return -1000; // if true return low score for opponent win

    int score = 0;

    // Rows
    for (int i = 0; i < 3; i++) { 
        score += evaluateLine(player, opponent, i, 0, i, 1, i, 2);
    }

    // Columns
    for (int j = 0; j < 3; j++) {
        score += evaluateLine(player, opponent, 0, j, 1, j, 2, j);
    }

    // Diagonals
    score += evaluateLine(player, opponent, 0, 0, 1, 1, 2, 2);
    score += evaluateLine(player, opponent, 0, 2, 1, 1, 2, 0);

    return score;
}
private int evaluateLine(char player, char opp, int r1, int c1, int r2, int c2,  int r3, int c3) {
/*  Looks at one line only (3 cells)
Counts:
how many AI pieces
how many opponent pieces
 it uses getWeight() to detrmine weight of each peice (newer pieces are more valuable)
 */

    int aiScore = 0;
    int oppScore = 0;

    int[][] cells = {{r1,c1}, {r2,c2}, {r3,c3}};

    for (int[] cell : cells) { // for each cell in the line
        int r = cell[0]; // get the row of the cell
        int c = cell[1];  // get the column of the cell
        char value = board[r][c]; // get the value of the cell (X, O, or empty)

        if (value == player) {
            aiScore += getWeight(r, c, player); // add the weight of the piece to aiScore
        } else if (value == opp) { // if the cell belongs to the opponent
            oppScore += getWeight(r, c, opp); // add the weight of the piece to oppScore
        }
    }


    // Blocked line 
    if (aiScore > 0 && oppScore > 0) return 0; // if both players have pieces in the line, it's blocked and has no value

    // Only AI
    if (oppScore == 0) return aiScore * aiScore; //no opp in line = Stronger line = much bigger reward

    // Only opponent
    if (aiScore == 0) return -(oppScore * oppScore); // only opp in line = Negative because it’s bad for AI

    return 0;
}

private int getWeight(int row, int col, char player) { // Newer pieces are more valuable, so we check how recently this cell was played on
    Queue<int[]> history = (player == 'X') ? Xhistory : Ohistory; // get the move history for the player

    int i = 0;
    for (int[] move : history) { // iterate through the move history
        if (move[0] == row && move[1] == col) { 
            // values for each peice: oldest = 1, middle = 2, newest = 3 
            return i + 1;
        }
        i++;
    }

    return 0; // if the cell is empty, it has no weight
}

    public Game copy() { // create a deep copy of the game state for simulation purposes
        Game clone = new Game();
        clone.currentPlayer = currentPlayer;
        clone.startTime = startTime;
        for (int i = 0; i < 3; i++) {
            clone.board[i] = Arrays.copyOf(board[i], 3);
        }

        clone.Xhistory = new LinkedList<>();
        for (int[] cell : Xhistory) {
            clone.Xhistory.offer(new int[]{cell[0], cell[1]});
        }

        clone.Ohistory = new LinkedList<>();
        for (int[] cell : Ohistory) {
            clone.Ohistory.offer(new int[]{cell[0], cell[1]});
            
        }

        return clone;
    }


      
    }

