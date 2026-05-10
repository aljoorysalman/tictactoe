public class AlphaBetaAI {

    private int maxDepth;
    private int nodesExplored; // Counter variable
    public AlphaBetaAI(int depth) { //constrctor to set max depth for search
        this.maxDepth = depth;
    }


    public int[] getMove(Game board, char aiPlayer) { 
        int bestScore = Integer.MIN_VALUE; 
        int[] bestMove = null;
         nodesExplored = 0;
         long startTime = System.currentTimeMillis();
        for (int[] move : board.getLegalMoves()) {
            Game copy = board.copy();
            copy.applyMove(move[0], move[1], aiPlayer); //simulate the move

            int score = alphaBeta(copy, maxDepth - 1,Integer.MIN_VALUE, Integer.MAX_VALUE, false, aiPlayer); //evaluate the move using alpha-beta 
          

            if (score > bestScore) { //update best move if this move is better
                bestScore = score;
                bestMove = move;
            }
        }
          long endTime = System.currentTimeMillis();
          long timeTaken = endTime - startTime;
          System.out.println("======= Alpha-Beta Performance =======");
         System.out.println("Time Taken: " + timeTaken + " ms");
         System.out.println("Total Nodes Explored: " + nodesExplored);
    

        return bestMove; // return the best move found
    }

    private int alphaBeta(Game board, int depth, int alpha, int beta, boolean isMaximizing, char aiPlayer) { 
        nodesExplored++;
        char opponent = (aiPlayer == 'X') ? 'O' : 'X';


        // Terminal
        if (board.checkWinner(aiPlayer)) return 1000; // If AI already wins return big positive score
        if (board.checkWinner(opponent)) return -1000; //If opponent wins return big negative score
        if (depth == 0) return board.evaluate(aiPlayer); //If depth ends use heuristic evalution 

        if (isMaximizing) {
            int best = Integer.MIN_VALUE; //  -infinity, alpha

            for (int[] move : board.getLegalMoves()) {
                Game copy = board.copy();
                copy.applyMove(move[0], move[1], aiPlayer); //simulate the move

                int val = alphaBeta(copy, depth - 1, alpha, beta, false, aiPlayer); //go deeper but its opponent's turn
                best = Math.max(best, val);  //update best score for maximizing player
                alpha = Math.max(alpha, best);

                if (alpha >= beta) break; // prune 
            }
            return best;

        } else { // minimizing opponent
            int best = Integer.MAX_VALUE; // +infinity, beta

            for (int[] move : board.getLegalMoves()) { 
                Game copy = board.copy(); // create a copy of the board to simulate on
                copy.applyMove(move[0], move[1], opponent);

                int val = alphaBeta(copy, depth - 1, alpha, beta, true, aiPlayer); //next level is player's turn
                best = Math.min(best, val);
                beta = Math.min(beta, best);

                if (alpha >= beta) break; // prune
            }
            return best;
        }
    }
}
