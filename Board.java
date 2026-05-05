import java.util.*;
public class Board {

    char[][] board; //3*3 board
    Queue<int[]> Xhistory; // track last 3 moves for x
    Queue<int[]> Ohistory; // track last 3 moves for o 
    int max = 3;

    public Board() { // constructor

        board = new char[3][3];
        Xhistory = new LinkedList<>();
        Ohistory = new LinkedList<>();

        for (int i = 0; i < 3; i++) { // board cells are empty 
            for (int j = 0; j < 3; j++) {
                board[i][j] = ' ';
            }
        }
    }

    public char getCell(int row, int col) { // get value of cell
        return board[row][col];
    }

    // 
    // public void setCell(int row, int col, char player) { // put value in cell
       // board[row][col] = player;

   //     if (player == 'X') {
      //      Xhistory.offer(new int[]{row, col});
      //  } else {
        //    Ohistory.offer(new int[]{row, col});
      //  }
    //} 

    //  remove specific cell 
    public void clearCell(int row, int col, char player) {
        board[row][col] = ' ';

        Queue<int[]> history = (player == 'X') ? Xhistory : Ohistory;

        Iterator<int[]> it = history.iterator();
        while (it.hasNext()) {
            int[] cell = it.next();
            if (cell[0] == row && cell[1] == col) {
                it.remove();
                break;
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



  
         public List<int[]> getLegalMoves() {
        List<int[]> moves = new ArrayList<>();
        for (int r = 0; r < 3; r++) 
            for (int c = 0; c < 3; c++)
                if (board[r][c] == ' ')
                    moves.add(new int[]{r, c});
                     return moves;
                 }

    public boolean checkWinner(char player) {

        for (int i = 0; i < 3; i++) { //rows
            if (board[i][0] == player &&
                board[i][1] == player &&
                board[i][2] == player) return true;
        }

        for (int j = 0; j < 3; j++) { // col
            if (board[0][j] == player &&
                board[1][j] == player &&
                board[2][j] == player) return true;
        }

        if (board[0][0] == player && // diagonals
            board[1][1] == player &&
            board[2][2] == player) return true;

        if (board[0][2] == player && 
            board[1][1] == player &&
            board[2][0] == player) return true;

        return false;
    }

public int evaluate(char aiPlayer) {
    char opponent = (aiPlayer == 'X') ? 'O' : 'X';

    if (checkWinner(aiPlayer)) return +100;
    if (checkWinner(opponent)) return -100;

    int score = 0;

    // rows
    for (int i = 0; i < 3; i++) {
        score += evaluateLine(aiPlayer, opponent,
            board[i][0], board[i][1], board[i][2]);
    }

    // columns
    for (int j = 0; j < 3; j++) {
        score += evaluateLine(aiPlayer, opponent,
            board[0][j], board[1][j], board[2][j]);
    }

    // diagonals
    score += evaluateLine(aiPlayer, opponent,
        board[0][0], board[1][1], board[2][2]);

    score += evaluateLine(aiPlayer, opponent,
        board[0][2], board[1][1], board[2][0]);

    return score;
}

private int evaluateLine(char ai, char opp, char c1, char c2, char c3) {
    int aiCount = 0, oppCount = 0;

    char[] cells = {c1, c2, c3};

    for (char c : cells) {
        if (c == ai) aiCount++;
        else if (c == opp) oppCount++;
    }

    if (oppCount == 0) return (int) Math.pow(10, aiCount);
    if (aiCount == 0) return -(int) Math.pow(10, oppCount);

    return 0;
}

    public Board copy() {
        Board clone = new Board();

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

    public void print() {
        System.out.println();
        System.out.println(" Game        Reference");

        for (int r = 0; r < 3; r++) {
            String gameRow = String.format(" %c | %c | %c ",
                board[r][0], board[r][1], board[r][2]);

            String refRow = String.format(" %d | %d | %d ",
                r * 3, r * 3 + 1, r * 3 + 2);

            System.out.println(gameRow + "    " + refRow);

            if (r < 2)
                System.out.println("---|---|---    ---|---|---");
        }

        System.out.println();
    }
}

 class Main {
    public static void main(String[] args) {

        Board b = new Board();

        b.applyMove(0, 0, 'X');
        b.applyMove(1, 1, 'O');
        b.applyMove(0, 1, 'X');

        b.print();
    }
}