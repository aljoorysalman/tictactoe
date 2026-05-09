import java.util.*;

public class MCTS {

    private static final Random random = new Random();

    //  Node class representing a game state in the MCTS tree
    static class Node {
        Game game;
        char playerTurn;   // who plays from this node
        Node parent;
        int[] moveFromParent;
        int wins = 0; // number of wins during simulations from this node
        int visits = 0; // number of times this node was visited during simulations
        List<int[]> untriedMoves; // legal moves that haven't been tried yet from this node
        List<Node> children = new ArrayList<>(); // child nodes representing game states after applying move

        Node(Game game, char playerTurn, Node parent, int[] moveFromParent) { // copy game 
            this.game = game.copy();
            this.playerTurn = playerTurn;
            this.parent = parent;
            this.moveFromParent = moveFromParent;
            this.untriedMoves = new ArrayList<>(game.getLegalMoves()); // get legal moves for this state
        }
    }

    // UCB1 (selection formula) 
    private double ucb1(Node node, int parentVisits) { // c is exploration parameter
        if (node.visits == 0) return Double.MAX_VALUE; // prioritize unvisited nodes

        double winRate = (double) node.wins / node.visits; // exploitation term, we know how good this node is based on simulations
        double explore = 2 * Math.sqrt(Math.log(parentVisits) / node.visits); // exploration term, encourages trying less visited nodes to discover their potential

        return winRate + explore; // exploitation + exploration
    }

    // MAIN FUNCTION to get best move using MCTS
    public int[] getMove(Game game, char player, int simulations) {

        Node root = new Node(game, player, null, null); // root node with current game state

        for (int i = 0; i < simulations; i++) { // repeat for the number of simulations

            //  1. SELECTION 
            Node node = root;

            while (node.untriedMoves.isEmpty() && !node.children.isEmpty()) { // keep selecting best child by UCB1 , until we reach a node that has untried moves or is a leaf node (no children)

                Node bestChild = null;
                double bestScore = -1;
                int parentVisits = node.visits;

                for (Node child : node.children) { // calculate UCB1 score for each child
                    double score = ucb1(child, parentVisits);

                    if (score > bestScore) {  // select child with highest UCB1 score
                        bestScore = score;
                        bestChild = child;
                    } 
                }

                node = bestChild;
            }

            //  2. EXPANSION 
            int result = 0;

            if (!node.untriedMoves.isEmpty()) { // if there are untried moves, expand by trying one of them

                int moveIndex = random.nextInt(node.untriedMoves.size()); // select a random move from untried moves to expand
                int[] move = node.untriedMoves.remove(moveIndex); // select and remove a move from untried moves

                Game newState = node.game.copy(); // create a copy of the game state to apply the move without affecting the original node's game state
                char currentPlayer = node.playerTurn; // the player who will make the move at this node

                newState.applyMove(move[0], move[1], currentPlayer);

                char nextPlayer = (currentPlayer == 'X') ? 'O' : 'X'; 

                Node child = new Node(newState, nextPlayer, node, move); // create new child node with the new game state after applying the move
                node.children.add(child); // add the new child to the current node's children

                node = child; // move down to the new child node for the simulation step

                if (newState.checkWinner(currentPlayer)) { // if the move results in a win, we can immediately determine the result for backpropagation
                    result = (currentPlayer == player) ? 1 : -1;
                }
            }

            //  3. SIMULATION 
            if (result == 0) { //not a win from expansion, we need to simulate a random playout to determine the result

                Game simGame = node.game.copy(); // create a copy of the game state at the current node for simulation
                char simPlayer = node.playerTurn;

                for (int step = 0; step < 100; step++) { // limit simulation steps to prevent infinite loops

                    List<int[]> moves = simGame.getLegalMoves();
                    
                              // availble moves , picks one randomly for the simulation playout, simulating a random game 
                    int[] move = moves.get(random.nextInt(moves.size())); // select random move for simulation
                    simGame.applyMove(move[0], move[1], simPlayer);

                    if (simGame.checkWinner(simPlayer)) {
                        result = (simPlayer == player) ? 1 : -1; //record win or loss result
                        break;
                    }

                    simPlayer = (simPlayer == 'X') ? 'O' : 'X';
                }
            }

            //  4. BACKPROPAGATION 
            Node temp = node;

            // backpropagate the result up the tree,
            //  updating wins and visits for each node along the path 
            // from the expanded/simulated node back to the root
            while (temp != null) { 
                temp.visits++; // increment visit count for this node

                char movePlayer = (temp.playerTurn == 'X') ? 'O' : 'X';

                if (movePlayer == player) {
                    temp.wins += result;  // propagate result upward
                } else {
                    temp.wins -= result; // opponent's perspective 
                }

                temp = temp.parent; // move up to parent node
            }
        }

        //  BEST MOVE 
        Node best = null;
        int bestVisits = -1;

        for (Node child : root.children) {
            if (child.visits > bestVisits) { // select child with most visits as best move, Most visited = MCTS found it consistently promising
                bestVisits = child.visits;
                best = child;
            }
        }

        return best.moveFromParent;
    }

    
}