import java.util.*;
import java.util.List;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.*;
import javax.swing.*;

/**
 * 3-Pieces Tic-Tac-Toe
 * =====================
 * Java translation of the Python original.
 * Includes: Alpha-Beta agent, MCTS agent, terminal play, GUI, and experiments.
 */
public class javaver {

    // ─────────────────────────────────────────────────────────────
    // PART 1: THE BOARD
    //
    //   0 | 1 | 2
    //   ---------
    //   3 | 4 | 5
    //   ---------
    //   6 | 7 | 8
    //
    // Each cell: ' ' (empty), 'X', or 'O'
    // ─────────────────────────────────────────────────────────────

    static char[] makeBoard() {
        char[] board = new char[9];
        Arrays.fill(board, ' ');
        return board;
    }

    static void printBoard(char[] board) {
        System.out.println();
        System.out.printf("  %c | %c | %c     0 | 1 | 2%n", board[0], board[1], board[2]);
        System.out.println("  ---------     ---------");
        System.out.printf("  %c | %c | %c     3 | 4 | 5%n", board[3], board[4], board[5]);
        System.out.println("  ---------     ---------");
        System.out.printf("  %c | %c | %c     6 | 7 | 8%n", board[6], board[7], board[8]);
        System.out.println();
    }

    static List<Integer> getEmptyCells(char[] board) {
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (board[i] == ' ') empty.add(i);
        }
        return empty;
    }

    // All 8 winning lines: 3 rows, 3 columns, 2 diagonals
    static final int[][] WIN_LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8},  // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8},  // columns
        {0, 4, 8}, {2, 4, 6}              // diagonals
    };

    static boolean checkWinner(char[] board, char player) {
        for (int[] line : WIN_LINES) {
            if (board[line[0]] == player && board[line[1]] == player && board[line[2]] == player)
                return true;
        }
        return false;
    }

    // ─────────────────────────────────────────────────────────────
    // PART 2: PIECE EXPIRY (3-Piece Rule)
    //
    // history: maps 'X' -> deque of cell indices (oldest first),
    //          maps 'O' -> deque of cell indices (oldest first)
    // When a player already has 3 pieces and places a 4th,
    // their oldest piece is removed automatically.
    // ─────────────────────────────────────────────────────────────

    /** Returns a fresh history: {"X": [], "O": []} */
    static Map<Character, Deque<Integer>> makeHistory() {
        Map<Character, Deque<Integer>> h = new HashMap<>();
        h.put('X', new ArrayDeque<>());
        h.put('O', new ArrayDeque<>());
        return h;
    }

    /**
     * Place player's piece on cell.
     * If player already has 3 pieces, remove the oldest one first.
     * Returns true if this move wins the game.
     */
    static boolean putPiece(char[] board, Map<Character, Deque<Integer>> history,
                             char player, int cell) {
        Deque<Integer> hist = history.get(player);
        if (hist.size() == 3) {
            int oldest = hist.pollFirst();  // remove oldest from front
            board[oldest] = ' ';
        }
        board[cell] = player;
        hist.addLast(cell);  // newest at back
        return checkWinner(board, player);
    }

    // ─────────────────────────────────────────────────────────────
    // PART 3: HEURISTIC EVALUATION (for Alpha-Beta)
    // ─────────────────────────────────────────────────────────────

    /**
     * Score the board from aiPlayer's point of view.
     * +100 = AI wins, -100 = opponent wins.
     * In between: weighted count of open win lines.
     */
    static int evaluateBoard(char[] board, char player) {
        char opponent = (player == 'X') ? 'O' : 'X';

        if (checkWinner(board, player))   return  100;
        if (checkWinner(board, opponent)) return -100;

        int score = 0;
        for (int[] line : WIN_LINES) {
            int plCount  = 0, oppCount = 0;
            for (int i : line) {
                if (board[i] == player)   plCount++;
                if (board[i] == opponent) oppCount++;
            }
            if (oppCount == 0) score += (int) Math.pow(10, plCount);
            if (plCount  == 0) score -= (int) Math.pow(10, oppCount);
        }
        return score;
    }

    // ─────────────────────────────────────────────────────────────
    // PART 4: ALPHA-BETA AGENT
    // ─────────────────────────────────────────────────────────────

    /**
     * Minimax with Alpha-Beta pruning.
     * Returns the best score the AI can guarantee from this position.
     */
    static int alphaBeta(char[] board, Map<Character, Deque<Integer>> history,
                          int depth, int alpha, int beta,
                          boolean isMaximising, char aiPlayer) {
        char opponent = (aiPlayer == 'X') ? 'O' : 'X';

        if (checkWinner(board, aiPlayer))   return  100;
        if (checkWinner(board, opponent))   return -100;
        if (depth == 0)                     return evaluateBoard(board, aiPlayer);

        List<Integer> empty = getEmptyCells(board);
        char currentPlayer = isMaximising ? aiPlayer : opponent;

        if (isMaximising) {
            int best = Integer.MIN_VALUE;
            for (int cell : empty) {
                char[] bCopy = board.clone();
                Map<Character, Deque<Integer>> hCopy = copyHistory(history);
                putPiece(bCopy, hCopy, currentPlayer, cell);
                int val = alphaBeta(bCopy, hCopy, depth - 1, alpha, beta, false, aiPlayer);
                best  = Math.max(best, val);
                alpha = Math.max(alpha, best);
                if (alpha >= beta) break;  // prune
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int cell : empty) {
                char[] bCopy = board.clone();
                Map<Character, Deque<Integer>> hCopy = copyHistory(history);
                putPiece(bCopy, hCopy, currentPlayer, cell);
                int val = alphaBeta(bCopy, hCopy, depth - 1, alpha, beta, true, aiPlayer);
                best = Math.min(best, val);
                beta = Math.min(beta, best);
                if (alpha >= beta) break;  // prune
            }
            return best;
        }
    }

    /** Pick the best move for player using Alpha-Beta at the given depth. */
    static int alphaBetaMove(char[] board, Map<Character, Deque<Integer>> history,
                              char player, int depth) {
        int bestScore = Integer.MIN_VALUE;
        int bestCell  = -1;

        for (int cell : getEmptyCells(board)) {
            char[] bCopy = board.clone();
            Map<Character, Deque<Integer>> hCopy = copyHistory(history);
            putPiece(bCopy, hCopy, player, cell);
            int score = alphaBeta(bCopy, hCopy, depth - 1,
                                  Integer.MIN_VALUE, Integer.MAX_VALUE, false, player);
            if (score > bestScore) {
                bestScore = score;
                bestCell  = cell;
            }
        }
        return bestCell;
    }

    // ─────────────────────────────────────────────────────────────
    // PART 5: MCTS AGENT
    // ─────────────────────────────────────────────────────────────

    static class MCTSNode {
        char[] board;
        Map<Character, Deque<Integer>> history;
        char player;       // who moves FROM this node
        MCTSNode parent;
        int move;          // cell played to reach this node (-1 for root)

        int wins   = 0;
        int visits = 0;

        List<Integer> untriedMoves;
        List<MCTSNode> children = new ArrayList<>();

        MCTSNode(char[] board, Map<Character, Deque<Integer>> history,
                 char player, MCTSNode parent, int move) {
            this.board   = board.clone();
            this.history = copyHistory(history);
            this.player  = player;
            this.parent  = parent;
            this.move    = move;
            this.untriedMoves = new ArrayList<>(getEmptyCells(board));
        }
    }

    static double ucb1Score(MCTSNode node, int parentVisits, double exploration) {
        if (node.visits == 0) return Double.POSITIVE_INFINITY;
        return (double) node.wins / node.visits
               + exploration * Math.sqrt(Math.log(parentVisits) / node.visits);
    }

    static final Random RNG = new Random();

    /** Choose the best move using MCTS with numSimulations rollouts. */
    static int mctsMove(char[] board, Map<Character, Deque<Integer>> history,
                         char player, int numSimulations) {
        MCTSNode root = new MCTSNode(board, history, player, null, -1);

        for (int sim = 0; sim < numSimulations; sim++) {

            // ── Phase 1: SELECTION
            MCTSNode node = root;
            while (node.untriedMoves.isEmpty() && !node.children.isEmpty()) {
                final int pv = node.visits;
                node = node.children.stream()
                    .max(Comparator.comparingDouble((MCTSNode c) -> ucb1Score(c, pv, 1.41)))
                    .get();
            }

            // ── Phase 2: EXPANSION
            Integer terminalResult = null;

            if (!node.untriedMoves.isEmpty()) {
                int idx  = RNG.nextInt(node.untriedMoves.size());
                int cell = node.untriedMoves.remove(idx);

                char[] newBoard   = node.board.clone();
                Map<Character, Deque<Integer>> newHist = copyHistory(node.history);
                char mover = node.player;
                boolean won = putPiece(newBoard, newHist, mover, cell);

                char nextPlayer = (mover == 'X') ? 'O' : 'X';
                MCTSNode child = new MCTSNode(newBoard, newHist, nextPlayer, node, cell);
                node.children.add(child);
                node = child;

                if (won) {
                    terminalResult = (mover == player) ? 1 : -1;
                }
            }

            // ── Phase 3: SIMULATION (rollout)
            int result;
            if (terminalResult == null) {
                char[] simBoard   = node.board.clone();
                Map<Character, Deque<Integer>> simHist = copyHistory(node.history);
                char simPlayer = node.player;

                // Track seen states to detect cycles (3-piece rule can loop)
                Map<String, Integer> seenStates = new HashMap<>();
                boolean simWon = false;

                for (int step = 0; step < 100; step++) {
                    String stateKey = stateKey(simBoard, simHist);
                    int count = seenStates.getOrDefault(stateKey, 0) + 1;
                    seenStates.put(stateKey, count);
                    if (count > 1) break;  // cycle → draw

                    List<Integer> empty = getEmptyCells(simBoard);
                    if (empty.isEmpty()) break;

                    int move2 = empty.get(RNG.nextInt(empty.size()));
                    simWon = putPiece(simBoard, simHist, simPlayer, move2);
                    if (simWon) break;
                    simPlayer = (simPlayer == 'X') ? 'O' : 'X';
                }

                char opp = (player == 'X') ? 'O' : 'X';
                if      (checkWinner(simBoard, player)) result =  1;
                else if (checkWinner(simBoard, opp))    result = -1;
                else                                     result =  0;
            } else {
                result = terminalResult;
            }

            // ── Phase 4: BACKPROPAGATION
            MCTSNode n = node;
            while (n != null) {
                n.visits++;
                if (n.player != player) n.wins += result;
                else                    n.wins -= result;
                n = n.parent;
            }
        }

        // Return the most-visited child's move
        return root.children.stream()
            .max(Comparator.comparingInt((MCTSNode c) -> c.visits))
            .get().move;
    }

    // ─────────────────────────────────────────────────────────────
    // PART 6: GAME LOOP
    // ─────────────────────────────────────────────────────────────

    static int getAgentMove(char[] board, Map<Character, Deque<Integer>> history,
                             char player, String agentType) {
        if (agentType.equals("human")) {
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.printf("  Your move (%c) – enter cell 0-8: ", player);
                try {
                    int cell = Integer.parseInt(sc.nextLine().trim());
                    if (getEmptyCells(board).contains(cell)) return cell;
                    System.out.println("  That cell is taken or invalid. Try again.");
                } catch (NumberFormatException e) {
                    System.out.println("  Please enter a number between 0 and 8.");
                }
            }
        } else if (agentType.startsWith("ab")) {
            int depth = Integer.parseInt(agentType.substring(2));
            return alphaBetaMove(board, history, player, depth);
        } else if (agentType.startsWith("mcts")) {
            int sims = Integer.parseInt(agentType.substring(4));
            return mctsMove(board, history, player, sims);
        }
        throw new IllegalArgumentException("Unknown agent type: " + agentType);
    }

    /**
     * Play one complete game between two agents.
     * Returns 'X', 'O', or 'D' (draw).
     */
    static char playGame(String agentX, String agentO, boolean verbose) {
        char[] board   = makeBoard();
        Map<Character, Deque<Integer>> history = makeHistory();
        char[] players = {'X', 'O'};
        Map<Character, String> agents = new HashMap<>();
        agents.put('X', agentX);
        agents.put('O', agentO);

        if (verbose) {
            System.out.println("\n" + "=".repeat(40));
            System.out.printf("  X: %s   vs   O: %s%n",
                agentX.toUpperCase(), agentO.toUpperCase());
            System.out.println("=".repeat(40));
            printBoard(board);
        }

        long startTime = System.currentTimeMillis();
        long MAX_MS    = 20L * 60 * 1000;  // 20-minute limit
        int  turn      = 0;

        while (true) {
            if (System.currentTimeMillis() - startTime > MAX_MS) {
                if (verbose) System.out.println("  Time limit reached! Draw.");
                return 'D';
            }

            char   player = players[turn];
            String agent  = agents.get(player);

            long t0   = System.currentTimeMillis();
            int  cell = getAgentMove(board, history, player, agent);
            double elapsed = (System.currentTimeMillis() - t0) / 1000.0;

            boolean won = putPiece(board, history, player, cell);

            if (verbose) {
                System.out.printf("  %s (%c) plays cell %d  [%.3fs]%n",
                    agent.toUpperCase(), player, cell, elapsed);
                printBoard(board);
            }

            if (won) {
                if (verbose) System.out.printf("  *** %c wins! ***%n%n", player);
                return player;
            }

            turn = 1 - turn;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PART 7: EXPERIMENTS
    // ─────────────────────────────────────────────────────────────

    static Map<Character, Integer> runExperiment(String agentX, String agentO, int nGames) {
        Map<Character, Integer> wins = new HashMap<>();
        wins.put('X', 0); wins.put('O', 0); wins.put('D', 0);

        System.out.printf("%n  Running %d games: %s (X) vs %s (O)%n",
            nGames, agentX.toUpperCase(), agentO.toUpperCase());

        for (int g = 0; g < nGames; g++) {
            char result = playGame(agentX, agentO, false);
            wins.put(result, wins.get(result) + 1);
            System.out.printf("\r    Game %d: %c", g + 1, result);
        }
        System.out.printf("%n    Results: X wins: %d  |  O wins: %d  |  Draws: %d    %n",
            wins.get('X'), wins.get('O'), wins.get('D'));
        return wins;
    }

    static void runPlayExperiments() {
        int N = 10;

        System.out.println("\n" + "#".repeat(50));
        System.out.println("  PLAY EXPERIMENT 1: Alpha-Beta vs Alpha-Beta");
        System.out.println("#".repeat(50));
        runExperiment("ab2",  "ab5",  N);
        runExperiment("ab2",  "ab10", N);
        runExperiment("ab10", "ab5",  N);

        System.out.println("\n" + "#".repeat(50));
        System.out.println("  PLAY EXPERIMENT 2: Alpha-Beta vs MCTS");
        System.out.println("#".repeat(50));
        runExperiment("ab2",     "mcts200",  N);
        runExperiment("mcts1000","ab2",      N);
        runExperiment("ab10",    "mcts1000", N);
        runExperiment("mcts500", "ab5",      N);

        System.out.println("\n" + "#".repeat(50));
        System.out.println("  PLAY EXPERIMENT 3: MCTS vs MCTS");
        System.out.println("#".repeat(50));
        runExperiment("mcts500",  "mcts200",  N);
        runExperiment("mcts1000", "mcts200",  N);
        runExperiment("mcts500",  "mcts1000", N);

        System.out.println("\n" + "#".repeat(50));
    }

    static void runTimeExperiments() {
        System.out.println("\n" + "#".repeat(50));
        System.out.println("  TIME EXPERIMENT 1: First Move Timing");
        System.out.println("#".repeat(50));

        System.out.println("\n  Alpha-Beta timing per depth:");
        for (int d : new int[]{2, 5, 10}) {
            char[] board = makeBoard();
            Map<Character, Deque<Integer>> history = makeHistory();
            long t0 = System.nanoTime();
            alphaBetaMove(board, history, 'X', d);
            double elapsed = (System.nanoTime() - t0) / 1e9;
            System.out.printf("    AB depth=%d:  %.4fs for first move%n", d, elapsed);
        }

        System.out.println("\n  MCTS timing per simulation count:");
        for (int s : new int[]{200, 500, 1000}) {
            char[] board = makeBoard();
            Map<Character, Deque<Integer>> history = makeHistory();
            long t0 = System.nanoTime();
            mctsMove(board, history, 'X', s);
            double elapsed = (System.nanoTime() - t0) / 1e9;
            System.out.printf("    MCTS sims=%d:  %.4fs for first move%n", s, elapsed);
        }

        System.out.println("\n" + "#".repeat(50));
        System.out.println("  TIME EXPERIMENT 2: Average Move Timing");
        System.out.println("#".repeat(50));

        System.out.println("\n  Alpha-Beta average time per move:");
        for (int d : new int[]{2, 5, 10}) {
            char[] board = makeBoard();
            Map<Character, Deque<Integer>> history = makeHistory();
            char player = 'X';
            List<Double> times = new ArrayList<>();

            for (int i = 0; i < 200; i++) {
                if (getEmptyCells(board).isEmpty()) break;
                long t0 = System.nanoTime();
                int cell = alphaBetaMove(board, history, player, d);
                times.add((System.nanoTime() - t0) / 1e9);
                boolean won = putPiece(board, history, player, cell);
                if (won) break;
                player = (player == 'X') ? 'O' : 'X';
            }

            double avg = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            System.out.printf("    AB depth=%d:  %.4fs average per move  (%d moves)%n",
                d, avg, times.size());
        }

        System.out.println("\n  MCTS average time per move:");
        for (int s : new int[]{200, 500, 1000}) {
            char[] board = makeBoard();
            Map<Character, Deque<Integer>> history = makeHistory();
            char player = 'X';
            List<Double> times = new ArrayList<>();

            for (int i = 0; i < 200; i++) {
                if (getEmptyCells(board).isEmpty()) break;
                long t0 = System.nanoTime();
                int cell = mctsMove(board, history, player, s);
                times.add((System.nanoTime() - t0) / 1e9);
                boolean won = putPiece(board, history, player, cell);
                if (won) break;
                player = (player == 'X') ? 'O' : 'X';
            }

            double avg = times.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            System.out.printf("    MCTS sims=%d:  %.4fs average per move  (%d moves)%n",
                s, avg, times.size());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PART 8: GUI (Swing)
    // ─────────────────────────────────────────────────────────────

    static class TicTacToeGUI extends JFrame {
        private char[]   board;
        private Map<Character, Deque<Integer>> history;
        private char     currentPlayer = 'X';
        private String   agentX, agentO;

        private JButton[]  buttons   = new JButton[9];
        private JLabel     statusLbl;
        private JComboBox<String> xCombo, oCombo;

        private volatile boolean running           = false;
        private volatile boolean waitingForHuman   = false;
        private volatile int     humanMove         = -1;
        private final Object     humanLock         = new Object();

        private static final String[] AGENT_LABELS = {
            "Human", "AB k=2", "AB k=5", "AB k=10",
            "MCTS 200", "MCTS 500", "MCTS 1000"
        };

        TicTacToeGUI() {
            super("3-Piece Tic-Tac-Toe");
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setResizable(false);
            buildUI();
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }

        private String parseAgent(String label) {
            return switch (label) {
                case "Human"    -> "human";
                case "AB k=2"   -> "ab2";
                case "AB k=5"   -> "ab5";
                case "AB k=10"  -> "ab10";
                case "MCTS 200" -> "mcts200";
                case "MCTS 500" -> "mcts500";
                case "MCTS 1000"-> "mcts1000";
                default -> "human";
            };
        }

        private void buildUI() {
            JPanel root = new JPanel(new BorderLayout(8, 8));
            root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
            root.setBackground(new Color(240, 240, 240));

            // Title
            JLabel title = new JLabel("3-Piece Tic-Tac-Toe", SwingConstants.CENTER);
            title.setFont(new Font("Arial", Font.BOLD, 18));
            root.add(title, BorderLayout.NORTH);

            // Config panel: agent selectors + Start button
            JPanel cfg = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
            cfg.setBackground(new Color(240, 240, 240));
            cfg.add(new JLabel("X:"));
            xCombo = new JComboBox<>(AGENT_LABELS);
            xCombo.setSelectedItem("Human");
            cfg.add(xCombo);
            cfg.add(new JLabel("O:"));
            oCombo = new JComboBox<>(AGENT_LABELS);
            oCombo.setSelectedItem("AB k=5");
            cfg.add(oCombo);

            JButton startBtn = new JButton("Start");
            startBtn.setBackground(new Color(76, 175, 80));
            startBtn.setForeground(Color.WHITE);
            startBtn.setFocusPainted(false);
            startBtn.addActionListener(e -> startGame());
            cfg.add(startBtn);

            JButton resetBtn = new JButton("Reset");
            resetBtn.addActionListener(e -> resetGame());
            cfg.add(resetBtn);

            // Board grid
            JPanel grid = new JPanel(new GridLayout(3, 3, 4, 4));
            grid.setBackground(new Color(200, 200, 200));
            for (int i = 0; i < 9; i++) {
                final int idx = i;
                buttons[i] = new JButton("");
                buttons[i].setFont(new Font("Arial", Font.BOLD, 28));
                buttons[i].setPreferredSize(new Dimension(80, 80));
                buttons[i].setBackground(Color.WHITE);
                buttons[i].setFocusPainted(false);
                buttons[i].addActionListener(e -> onCellClick(idx));
                grid.add(buttons[i]);
            }

            // Status label
            statusLbl = new JLabel("Pick agents and press Start.", SwingConstants.CENTER);
            statusLbl.setFont(new Font("Arial", Font.PLAIN, 13));

            JPanel center = new JPanel(new BorderLayout(6, 6));
            center.setBackground(new Color(240, 240, 240));
            center.add(cfg,   BorderLayout.NORTH);
            center.add(grid,  BorderLayout.CENTER);
            center.add(statusLbl, BorderLayout.SOUTH);

            root.add(center, BorderLayout.CENTER);
            add(root);
        }

        private void startGame() {
            resetGame();
            agentX  = parseAgent((String) xCombo.getSelectedItem());
            agentO  = parseAgent((String) oCombo.getSelectedItem());
            running = true;
            statusLbl.setText("Player X's turn (" + xCombo.getSelectedItem() + ")");
            new Thread(this::gameLoop, "game-loop").start();
        }

        private void gameLoop() {
            while (running) {
                char   p     = currentPlayer;
                String agent = (p == 'X') ? agentX : agentO;
                String label = (p == 'X') ? (String) xCombo.getSelectedItem()
                                           : (String) oCombo.getSelectedItem();

                int cell;
                if (agent.equals("human")) {
                    SwingUtilities.invokeLater(() ->
                        statusLbl.setText("Player " + p + "'s turn — click a cell."));
                    waitingForHuman = true;
                    synchronized (humanLock) {
                        while (waitingForHuman && running) {
                            try { humanLock.wait(); }
                            catch (InterruptedException ignored) {}
                        }
                    }
                    if (!running) break;
                    cell = humanMove;
                } else {
                    SwingUtilities.invokeLater(() ->
                        statusLbl.setText("Player " + p + "'s turn — " + label + " thinking…"));
                    cell = getAgentMove(board, history, p, agent);
                }

                if (!running) break;

                boolean won = putPiece(board, history, p, cell);
                SwingUtilities.invokeLater(this::refreshBoard);

                if (won) {
                    final char winner = p;
                    SwingUtilities.invokeLater(() -> endGame("Player " + winner + " wins!"));
                    return;
                }

                currentPlayer = (p == 'X') ? 'O' : 'X';
            }
        }

        private void onCellClick(int idx) {
            if (!waitingForHuman) return;
            if (board[idx] != ' ') {
                statusLbl.setText("That cell is taken, pick another please.");
                return;
            }
            humanMove = idx;
            waitingForHuman = false;
            synchronized (humanLock) { humanLock.notifyAll(); }
        }

        private void refreshBoard() {
            for (int i = 0; i < 9; i++) {
                char val = board[i];
                if (val == ' ') {
                    buttons[i].setText("");
                    buttons[i].setForeground(Color.BLACK);
                } else {
                    Deque<Integer> hist = history.get(val);
                    boolean isOldest = (hist.size() == 3 && hist.peekFirst() == i);
                    Color fg = isOldest ? Color.GRAY
                                        : (val == 'X') ? new Color(30, 100, 200)
                                                       : new Color(200, 50, 50);
                    buttons[i].setText(String.valueOf(val));
                    buttons[i].setForeground(fg);
                }
            }
        }

        private void endGame(String msg) {
            running = false;
            statusLbl.setText(msg);
            JOptionPane.showMessageDialog(this, msg, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }

        private void resetGame() {
            running         = false;
            waitingForHuman = false;
            synchronized (humanLock) { humanLock.notifyAll(); }
            board         = makeBoard();
            history       = makeHistory();
            currentPlayer = 'X';
            statusLbl.setText("Pick agents and press Start.");
            for (JButton btn : buttons) {
                btn.setText("");
                btn.setBackground(Color.WHITE);
                btn.setForeground(Color.BLACK);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // PART 9: MAIN MENU
    // ─────────────────────────────────────────────────────────────

    static final Map<String, String[]> AGENT_MENU = new LinkedHashMap<>();
    static {
        AGENT_MENU.put("1", new String[]{"Human",           "human"});
        AGENT_MENU.put("2", new String[]{"Alpha-Beta k=2",  "ab2"});
        AGENT_MENU.put("3", new String[]{"Alpha-Beta k=5",  "ab5"});
        AGENT_MENU.put("4", new String[]{"Alpha-Beta k=10", "ab10"});
        AGENT_MENU.put("5", new String[]{"MCTS 200 sims",   "mcts200"});
        AGENT_MENU.put("6", new String[]{"MCTS 500 sims",   "mcts500"});
        AGENT_MENU.put("7", new String[]{"MCTS 1000 sims",  "mcts1000"});
    }

    static String pickAgent(String role, Scanner sc) {
        System.out.printf("%n  Pick agent for %s:%n", role);
        for (Map.Entry<String, String[]> e : AGENT_MENU.entrySet())
            System.out.printf("    %s. %s%n", e.getKey(), e.getValue()[0]);
        while (true) {
            System.out.print("  Enter number: ");
            String choice = sc.nextLine().trim();
            if (AGENT_MENU.containsKey(choice)) return AGENT_MENU.get(choice)[1];
            System.out.println("  Invalid choice, try again.");
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("   3-Pieces Tic-Tac-Toe  ");
        System.out.println("=".repeat(50));

        while (true) {
            System.out.println("\n  Main Menu:");
            System.out.println("    1. Play a game in Terminal");
            System.out.println("    2. Play a game in GUI");
            System.out.println("    3. Run experiment: AI vs AI");
            System.out.println("    4. Run experiment: Computation Time");
            System.out.println("    0. Exit");
            System.out.print("\n  Select: ");
            String choice = sc.nextLine().trim();

            switch (choice) {
                case "0" -> { System.out.println("  Goodbye!"); return; }
                case "1" -> {
                    String ax = pickAgent("X (goes first)", sc);
                    String ao = pickAgent("O", sc);
                    playGame(ax, ao, true);
                }
                case "2" -> SwingUtilities.invokeLater(TicTacToeGUI::new);
                case "3" -> runPlayExperiments();
                case "4" -> runTimeExperiments();
                default  -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // UTILITIES
    // ─────────────────────────────────────────────────────────────

    /** Deep-copy a history map (both deques). */
    static Map<Character, Deque<Integer>> copyHistory(Map<Character, Deque<Integer>> h) {
        Map<Character, Deque<Integer>> copy = new HashMap<>();
        copy.put('X', new ArrayDeque<>(h.get('X')));
        copy.put('O', new ArrayDeque<>(h.get('O')));
        return copy;
    }

    /** Produce a stable string key for a (board, historyX, historyY) state. */
    static String stateKey(char[] board, Map<Character, Deque<Integer>> history) {
        return new String(board)
            + "|" + history.get('X').toString()
            + "|" + history.get('O').toString();
    }
}