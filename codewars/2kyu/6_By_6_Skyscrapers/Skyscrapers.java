import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zhe
 * @createTime 2018/9/20
 * @description
 */

class DancingLinks {

    static final boolean verbose = false;

    class DancingNode {
        DancingNode L, R, U, D;
        ColumnNode C;
        String explain;

        // hooks node n1 `below` current node
        DancingNode hookDown(DancingNode n1) {
            assert (this.C == n1.C);
            n1.D = this.D;
            n1.D.U = n1;
            n1.U = this;
            this.D = n1;
            return n1;
        }

        // hooke a node n1 to the right of `this` node
        DancingNode hookRight(DancingNode n1) {
            n1.R = this.R;
            n1.R.L = n1;
            n1.L = this;
            this.R = n1;
            return n1;
        }

        void unlinkLR() {
            this.L.R = this.R;
            this.R.L = this.L;
        }

        void relinkLR() {
            this.L.R = this.R.L = this;
        }

        void unlinkUD() {
            this.U.D = this.D;
            this.D.U = this.U;
        }

        void relinkUD() {
            this.U.D = this.D.U = this;
        }

        public DancingNode(String explain) {
            L = R = U = D = this;
            this.explain = explain;
        }

        public DancingNode(ColumnNode c, String explain) {
            this(explain);
            C = c;
        }

        @Override
        public String toString() {
            return this.C.name + "," + explain;
        }
    }

    class ColumnNode extends DancingNode {
        int size; // number of ones in current column
        String name;

        public ColumnNode(String n) {
            super(null);
            size = 0;
            name = n;
            C = this;
        }

        void cover() {
            unlinkLR();
            for (DancingNode i = this.D; i != this; i = i.D) {
                for (DancingNode j = i.R; j != i; j = j.R) {
                    j.unlinkUD();
                    j.C.size--;
                }
            }
            header.size--; // not part of original
        }

        void uncover() {
            for (DancingNode i = this.U; i != this; i = i.U) {
                for (DancingNode j = i.L; j != i; j = j.L) {
                    j.C.size++;
                    j.relinkUD();
                }
            }
            relinkLR();
            header.size++; // not part of original
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    private ColumnNode header;
    private int solutions = 0;
    private final SolutionHandler handler;
    private List<DancingNode> answer;

    // Heart of the algorithm
    private void search(int k) {
        if (header.R == header) { // all the columns removed

            if (verbose) {
                System.out.println("-----------------------------------------");
                System.out.println("Solution #" + solutions + "\n");
            }
            int[][] grid = new int[Skyscrapers.SIZE][Skyscrapers.SIZE];
            for (DancingNode node : answer) {
                int v = Integer.parseInt(String.valueOf(node.explain.charAt(7)));
                int x = Integer.parseInt(String.valueOf(node.explain.charAt(1)));
                int y = Integer.parseInt(String.valueOf(node.explain.charAt(4)));
                grid[x][y] = v;
            }
            handler.handler(grid);

            if (verbose) {

                for (int i = 0; i < Skyscrapers.SIZE; ++i) {
                    for (int j = 0; j < Skyscrapers.SIZE; ++j) {
                        System.out.print(grid[i][j] + " ");
                    }
                    System.out.println();
                }
//                System.out.println(answer.size());
//                System.out.println(String.valueOf(answer));
            }
            if (verbose) {
                System.out.println("-----------------------------------------");
            }
            solutions++;
        } else {
            ColumnNode c = selectColumnNodeHeuristic();
            c.cover();

            for (DancingNode r = c.D; r != c; r = r.D) {
                answer.add(r);

                for (DancingNode j = r.R; j != r; j = j.R) {
                    j.C.cover();
                }

                search(k + 1);

                r = answer.remove(answer.size() - 1);
                c = r.C;

                for (DancingNode j = r.L; j != r; j = j.L) {
                    j.C.uncover();
                }
            }
            c.uncover();
        }
    }

    private ColumnNode selectColumnNodeNaive() {
        return (ColumnNode) header.R;
    }

    private ColumnNode selectColumnNodeHeuristic() {
        int min = Integer.MAX_VALUE;
        ColumnNode ret = null;
        for (ColumnNode c = (ColumnNode) header.R; c != header; c = (ColumnNode) c.R) {
            if (c.size < min) {
                min = c.size;
                ret = c;
            }
        }
        return ret;
    }

    // Ha, another Knuth algorithm
    private ColumnNode selectColumnNodeRandom() { // select a column randomly
        ColumnNode ptr = (ColumnNode) header.R;
        ColumnNode ret = null;
        int c = 1;
        while (ptr != header) {
            if (Math.random() <= 1 / (double) c) {
                ret = ptr;
            }
            c++;
            ptr = (ColumnNode) ptr.R;
        }
        return ret;
    }

    private ColumnNode selectColumnNodeNth(int n) {
        int go = n % header.size;
        ColumnNode ret = (ColumnNode) header.R;
        while (go-- > 0) ret = (ColumnNode) ret.R;
        return ret;
    }

    private void printBoard() { // diagnostics to have a look at the board state
        System.out.println("Board Config: ");
        for (ColumnNode tmp = (ColumnNode) header.R; tmp != header; tmp = (ColumnNode) tmp.R) {

            for (DancingNode d = tmp.D; d != tmp; d = d.D) {
                String ret = "";
                ret += d.C.name + " --> ";
                for (DancingNode i = d.R; i != d; i = i.R) {
                    ret += i.C.name + " --> ";
                }
                System.out.println(ret);
            }
        }
    }

    // grid is a grid of 0s and 1s to solve the exact cover for
    // returns the root column header node
    private ColumnNode makeDLXBoard(int[][] grid, List<String> colName, List<String> rowName) {
        if (colName != null) assert colName.size() == grid[0].length;
        final int COLS = grid[0].length;
        final int ROWS = grid.length;

        ColumnNode headerNode = new ColumnNode("header");
        ArrayList<ColumnNode> columnNodes = new ArrayList<ColumnNode>();

        for (int i = 0; i < COLS; i++) {
            ColumnNode n = new ColumnNode(colName == null ? Integer.toString(i) : colName.get(i));
            columnNodes.add(n);
            headerNode = (ColumnNode) headerNode.hookRight(n);
        }
        headerNode = headerNode.R.C;

        for (int i = 0; i < ROWS; i++) {
            DancingNode prev = null;
            for (int j = 0; j < COLS; j++) {
                if (grid[i][j] == 1) {
                    ColumnNode col = columnNodes.get(j);
                    DancingNode newNode = new DancingNode(col, rowName == null ? null : rowName.get(i));
                    if (prev == null)
                        prev = newNode;
                    col.U.hookDown(newNode);
                    prev = prev.hookRight(newNode);
                    col.size++;
                }
            }
        }

        headerNode.size = COLS;

        return headerNode;
    }


    // Grid consists solely of 1s and 0s. Undefined behaviour otherwise

    public DancingLinks(int[][] grid, List<String> colName, List<String> rowName, SolutionHandler handler) {
        this.handler = handler;
        header = makeDLXBoard(grid, colName, rowName);
    }

    public DancingLinks(int[][] grid, SolutionHandler handler) {
        this(grid, null, null, handler);
    }

    public void runSolver() {
        solutions = 0;
        answer = new LinkedList<>();
        search(0);
    }

    public interface SolutionHandler {
        void handler(Object object);
    }
}

public class Skyscrapers {
    public static final int SIZE = 6;

    static class Pair<L, R> {
        L leftValue;
        R rightValue;

        public Pair(L leftValue, R rightValue) {
            this.leftValue = leftValue;
            this.rightValue = rightValue;
        }

        public L getLeftValue() {
            return leftValue;
        }

        public void setLeftValue(L leftValue) {
            this.leftValue = leftValue;
        }

        public R getRightValue() {
            return rightValue;
        }

        public void setRightValue(R rightValue) {
            this.rightValue = rightValue;
        }
    }

    static class Permutation {
        static final List<Permutation.LineView> permutation;
        static final MutexGraph mutexGraph;

        static {
            permutation = new ArrayList<>(SIZE * 1000);
            dfs(0, new int[SIZE], permutation);
            mutexGraph = MutexGraph.build(permutation);
        }

        public static class LineView implements Comparator<LineView> {
            final int frontView, backView;
            final int[] array;
            final int code;

            public LineView(int[] array, int code) {
                this.array = array.clone();
                Pair<Integer, Integer> pair = mountainView(array);
                this.frontView = pair.leftValue;
                this.backView = pair.rightValue;
                this.code = code;
            }

            @Override
            public String toString() {
                return String.format("[%d, (%d, %d), %s]", code, frontView, backView, Arrays.toString(array));
            }

            @Override
            public int compare(LineView o1, LineView o2) {
                return o1.code - o2.code;
            }
        }

        public static class MutexGraph {
            boolean[][] graph;

            static boolean checkCross(Permutation.LineView o1, Permutation.LineView o2) {
                for (int i = 0; i < SIZE; ++i)
                    if (o1.array[i] == o2.array[i])
                        return false;
                return true;
            }

            public MutexGraph(List<Permutation.LineView> permutation) {
                graph = new boolean[permutation.size()][permutation.size()];
                for (int i = 0; i < permutation.size(); ++i)
                    for (int j = i + 1; j < permutation.size(); ++j)
                        graph[i][j] = graph[j][i] = checkCross(permutation.get(i), permutation.get(j));

            }

            static MutexGraph build(List<Permutation.LineView> permutation) {
                return new MutexGraph(permutation);
            }
        }

        private static void dfs(int pos, int[] r, List<LineView> res) {
            if (pos == SIZE) {
                res.add(new LineView(r, res.size()));
                return;
            }

            loop:
            for (int i = 0; i < SIZE; ++i) {
                for (int j = 0; j < pos; ++j)
                    if (r[j] == i + 1) continue loop;

                r[pos] = i + 1;
                dfs(pos + 1, r, res);
            }
        }

        public static Pair<Integer, Integer> mountainView(int[] array) {
            int frontView, backView;
            frontView = backView = 1;
            int frontTop = array[0], backTop = array[SIZE - 1];
            for (int i = 0; i < SIZE; ++i) {
                if (array[i] > frontTop) {
                    frontTop = array[i];
                    frontView++;
                }
                if (array[SIZE - 1 - i] > backTop) {
                    backTop = array[SIZE - 1 - i];
                    backView++;
                }
            }
            return new Pair<>(frontView, backView);
        }

        public static boolean checkCross(int code1, int code2) {
            return mutexGraph.graph[code1][code2];
        }
    }

    static class Solution {
        int[] top, bottom, left, right;
        DancingLinks dancingLinks;
        List<List<Permutation.LineView>> res = new ArrayList<>();

        public Solution(int[] clues) {
            top = new int[SIZE];
            bottom = new int[SIZE];
            left = new int[SIZE];
            right = new int[SIZE];
            for (int i = 0; i < 4 * SIZE; ++i)
                switch (i / SIZE) {
                    case 0:
                        top[i] = clues[i];
                        break;
                    case 1:
                        right[i - SIZE] = clues[i];
                        break;
                    case 2:
                        bottom[3 * SIZE - 1 - i] = clues[i];
                        break;
                    default:
                        left[4 * SIZE - 1 - i] = clues[i];
                        break;
                }

            List<String> rowName = new ArrayList<>(SIZE * SIZE * SIZE);
            for (int i = 0; i < SIZE * SIZE * SIZE; ++i)
                rowName.add(String.format("(%d. %d)=%d", i / SIZE / SIZE, i / SIZE % SIZE, i % SIZE + 1));
            List<String> colName = new ArrayList<>(7 * SIZE * SIZE);
            for (int i = 0; i < SIZE; ++i)
                for (int j = 1; j <= SIZE; ++j)
                    colName.add(String.format("ROW[%d]=%d", i, j));
            for (int i = 0; i < SIZE; ++i)
                for (int j = 1; j <= SIZE; ++j)
                    colName.add(String.format("COL[%d]=%d", i, j));
            for (int i = 0; i < SIZE; ++i)
                for (int j = 1; j <= SIZE; ++j)
                    colName.add(String.format("TOP[%d]=%d", i, j));
            for (int i = 0; i < SIZE; ++i)
                for (int j = 1; j <= SIZE; ++j)
                    colName.add(String.format("BOTTOM[%d]=%d", i, j));
            for (int i = 0; i < SIZE; ++i)
                for (int j = 1; j <= SIZE; ++j)
                    colName.add(String.format("LEFT[%d]=%d", i, j));
            for (int i = 0; i < SIZE; ++i)
                for (int j = 1; j <= SIZE; ++j)
                    colName.add(String.format("RIGHT[%d]=%d", i, j));
            for (int i = 0; i < SIZE; ++i)
                for (int j = 0; j < SIZE; ++j)
                    colName.add(String.format("CELL[%d.%d]", i, j));

            int[][] gridMatrix = new int[SIZE * SIZE * SIZE][7 * SIZE * SIZE];
            for (int row = 0; row < SIZE; ++row) { // ROW
                for (int col = 0; col < SIZE; ++col) { // COL
                    for (int k = 0; k < SIZE; ++k) { // V
                        gridMatrix[row * SIZE * SIZE + col * SIZE + k][row * SIZE + k] =
                                gridMatrix[row * SIZE * SIZE + col * SIZE + k][SIZE * SIZE + col * SIZE + k] =
                                        gridMatrix[row * SIZE * SIZE + col * SIZE + k][6 * SIZE * SIZE + row * SIZE + col] = 1;
                    }
                }
            }

            for (Permutation.LineView lineView : Permutation.permutation) {
                for (int index = 0; index < SIZE; ++index) {  // INDEX
                    if (top[index] == 0 || top[index] == lineView.frontView) { // COL
                        for (int row = 0; row < SIZE; ++row) { // ROW
                            gridMatrix[row * SIZE * SIZE + index * SIZE + lineView.array[row] - 1][2 * SIZE * SIZE + index * SIZE + lineView.array[row] - 1] = 1;
                        }
                    }
                    if (bottom[index] == 0 || bottom[index] == lineView.frontView) { // COL
                        for (int row = 0; row < SIZE; ++row) { // ROW
                            gridMatrix[row * SIZE * SIZE + index * SIZE + lineView.array[SIZE - 1 - row] - 1][3 * SIZE * SIZE + index * SIZE + lineView.array[SIZE - 1 - row] - 1] = 1;
                        }
                    }
                    if (left[index] == 0 || left[index] == lineView.frontView) { // ROW
                        for (int col = 0; col < SIZE; ++col) { // COL
                            gridMatrix[index * SIZE * SIZE + col * SIZE + lineView.array[col] - 1][4 * SIZE * SIZE + col * SIZE + lineView.array[col] - 1] = 1;
                        }
                    }
                    if (right[index] == 0 || right[index] == lineView.frontView) { // ROW
                        for (int col = 0; col < SIZE; ++col) { // COL
                            gridMatrix[index * SIZE * SIZE + col * SIZE + lineView.array[SIZE - 1 - col] - 1][5 * SIZE * SIZE + index * SIZE + lineView.array[SIZE - 1 - col] - 1] = 1;
                        }
                    }
                }
            }

//            System.out.println("," + String.valueOf(colName));
//            for (int i = 0; i < rowName.size(); ++i)
//                System.out.println(rowName.get(i) + "," + Arrays.toString(gridMatrix[i]));

            res = new ArrayList<>();

            dancingLinks = new DancingLinks(gridMatrix, colName, rowName, new DancingLinks.SolutionHandler() {
                @Override
                public void handler(Object object) {
                    List<Permutation.LineView> tempRes = new ArrayList<>(SIZE);
                    for (int i = 0;i < SIZE; ++i) {
                        tempRes.add(new Permutation.LineView(((int[][])object)[i], -1));
                    }
                    res.add(tempRes);
                }
            });
        }

        private void dfs(List<List<Permutation.LineView>> res, int depth, Permutation.LineView[] buffer) {
            if (depth == SIZE) {
                for (int i = 0; i < SIZE; ++i) {
                    int[] tmp = new int[SIZE];
                    for (int j = 0; j < SIZE; ++j)
                        tmp[j] = buffer[j].array[i];
                    Pair<Integer, Integer> pair = Permutation.mountainView(tmp);
                    if (top[i] != 0 && top[i] != pair.getLeftValue()) return;
                    if (bottom[i] != 0 && bottom[i] != pair.getRightValue()) return;
                }
                res.add(Arrays.asList(buffer.clone()));
                return;
            }

            loop:
            for (Permutation.LineView lineView : Permutation.permutation) {
                if (left[depth] != 0 && left[depth] != lineView.frontView) continue;
                if (right[depth] != 0 && right[depth] != lineView.backView) continue;
                for (int j = 0; j < depth; ++j)
                    if (!Permutation.checkCross(buffer[j].code, lineView.code))
                        continue loop;

                buffer[depth] = lineView;
                dfs(res, depth + 1, buffer);
            }
        }

        public List<List<Permutation.LineView>> solution() {
            dancingLinks.runSolver();
//            dfs(res, 0, new Permutation.LineView[SIZE]);
            return res;
        }
    }


    static int[][] solvePuzzle(int[] clues) {
//        System.out.println(Permutation.permutation);
        Solution solution = new Solution(clues);
        List<List<Permutation.LineView>> res = solution.solution();
//        System.out.println(res.size());
//        for (int i = 0; i < SIZE; ++i)
//            System.out.println(res.get(0).get(i));
        loop: for (List<Permutation.LineView> list : res) {
            int[][] ret = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; ++i) {
                if (solution.left[i] != 0 && solution.left[i] != list.get(i).frontView) continue loop;
                if (solution.right[i] != 0 && solution.right[i] != list.get(i).backView) continue loop;

                for (int j = 0; j < SIZE; ++j)
                    ret[i][j] = list.get(i).array[j];
            }
            for (int i = 0;i < SIZE; ++i) {
                int[] buffer = new int [SIZE];
                for (int j = 0;j < SIZE; ++j) {
                    buffer[j] = ret[j][i];
                }
                Permutation.LineView lineView = new Permutation.LineView(buffer, -1);
                if (solution.top[i] != 0 && solution.top[i] != lineView.frontView) continue loop;
                if (solution.bottom[i] != 0 && solution.bottom[i] != lineView.backView) continue loop;
            }
            return ret;
        }
        return null;
    }

}