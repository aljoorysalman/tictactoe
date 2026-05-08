import java.util.*;

public class MCTS {

    // ── Node ──────────────────────────────────────────────────────
    static class MCTSNode {
        Game game;
        char  player;       // whose turn it is FROM this node
        MCTSNode parent;
        int[] move;         // move played to reach this node (null for root)

        int wins   = 0;
        int visits = 0;

        List<int[]>     untriedMoves;
        List<MCTSNode>  children = new ArrayList<>();

        MCTSNode(Game game, char player, MCTSNode parent, int[] move) {
            this.game        = game.copy();
            this.player       = player;
            this.parent       = parent;
            this.move         = move;
            this.untriedMoves = new ArrayList<>(game.getLegalMoves());
        }
    }

    // ── UCB1 score ────────────────────────────────────────────────
    private static double ucb1(MCTSNode node, int parentVisits, double c) {
        if (node.visits == 0) return Double.POSITIVE_INFINITY;
        return (double) node.wins / node.visits
               + c * Math.sqrt(Math.log(parentVisits) / node.visits);
    }

    private static final Random RNG = new Random();

    // ── Public entry point ────────────────────────────────────────
    /**
     * Returns the best [row,col] move for 'player' after running
     * numSimulations rollouts.  exploration ≈ 1.41 is a good default.
     */
    public int[] getMove(Game game, char player, int numSimulations, double exploration) {

        MCTSNode root = new MCTSNode(game, player, null, null);

        for (int sim = 0; sim < numSimulations; sim++) {

            // ── 1. SELECTION ──────────────────────────────────────
            MCTSNode node = root;
            while (node.untriedMoves.isEmpty() && !node.children.isEmpty()) {
                final int pv = node.visits;
                final double ex = exploration;
                node = node.children.stream()
                    .max(Comparator.comparingDouble(c -> ucb1(c, pv, ex)))
                    .get();
            }

            // ── 2. EXPANSION ──────────────────────────────────────
            Integer terminalResult = null;

            if (!node.untriedMoves.isEmpty()) {
                int idx      = RNG.nextInt(node.untriedMoves.size());
                int[] picked = node.untriedMoves.remove(idx);

                Game newGame = node.game.copy();
                char  mover   = node.player;
                newGame.applyMove(picked[0], picked[1], mover);

                char nextPlayer = (mover == 'X') ? 'O' : 'X';
                MCTSNode child  = new MCTSNode(newGame, nextPlayer, node, picked);
                node.children.add(child);
                node = child;

                if (node.game.checkWinner(mover)) {
                    terminalResult = (mover == player) ? 1 : -1;
                }
            }

            // ── 3. SIMULATION (random rollout) ────────────────────
            int result;
            if (terminalResult != null) {
                result = terminalResult;
            } else {
                Game  simGame  = node.game.copy();
                char   simPlayer = node.player;
                char   opp       = (player == 'X') ? 'O' : 'X';

                // Cycle detection: the 3-piece rule can create loops
                Map<String, Integer> seenStates = new HashMap<>();
                boolean won = false;

                for (int step = 0; step < 100; step++) {
                    String key = boardKey(simGame);
                    int cnt = seenStates.getOrDefault(key, 0) + 1;
                    seenStates.put(key, cnt);
                    if (cnt > 1) break;  // cycle → treat as draw

                    List<int[]> moves = simGame.getLegalMoves();
                    if (moves.isEmpty()) break;

                    int[] m = moves.get(RNG.nextInt(moves.size()));
                    simGame.applyMove(m[0], m[1], simPlayer);

                    if (simGame.checkWinner(simPlayer)) { won = true; break; }
                    simPlayer = (simPlayer == 'X') ? 'O' : 'X';
                }

                if      (simGame.checkWinner(player)) result =  1;
                else if (simGame.checkWinner(opp))    result = -1;
                else                                   result =  0;
            }

            // ── 4. BACKPROPAGATION ────────────────────────────────
            MCTSNode n = node;
            while (n != null) {
                n.visits++;
                // wins are from the perspective of the node's *parent* (the one who moved)
                char moverToNode = (n.player == 'X') ? 'O' : 'X'; // who placed to get here
                if (moverToNode == player) n.wins += result;
                else                       n.wins -= result;
                n = n.parent;
            }
        }

        // Pick the most-visited child
        MCTSNode best = root.children.stream()
            .max(Comparator.comparingInt(c -> c.visits))
            .orElse(null);

        if (best == null) {
            // Fallback: random legal move (shouldn't happen)
            List<int[]> legal = game.getLegalMoves();
            return legal.get(RNG.nextInt(legal.size()));
        }
        return best.move;
    }

    // ── Convenience overload (default exploration = 1.41) ─────────
    public int[] getMove(Game game, char player, int numSimulations) {
        return getMove(game, player, numSimulations, 1.41);
    }

    // ── Game state key for cycle detection ───────────────────────
    private static String boardKey(Game b) {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                sb.append(b.getBoard());
        // Also encode queue order so shifted states differ
        for (int[] cell : b.Xhistory) sb.append(cell[0]).append(cell[1]);
        sb.append('|');
        for (int[] cell : b.Ohistory) sb.append(cell[0]).append(cell[1]);
        return sb.toString();
    }
}