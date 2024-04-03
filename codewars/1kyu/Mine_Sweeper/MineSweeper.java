import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;

class MineSweeper {

    public static final int MINE = -1;
    public static final int UNKNOWN = -999;
    public static final int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
    public static final int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

    public static int initMap(String mapString, int[][] map) {
        int mines = 0;
        String[] lines = mapString.split("\n");
        int n = lines.length;
        for (int i = 0; i < n; ++i) {
            String[] elements = lines[i].split(" ");
            int m = elements.length;
            map[i] = new int[m];
            for (int j = 0; j < m; ++j) {
                switch (elements[j]) {
                    case "0":
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                    case "8":
                        map[i][j] = elements[j].charAt(0) - '0';
                        break;
                    case "x":
                        map[i][j] = MINE;
                        mines++;
                        break;
                    case "?":
                        map[i][j] = UNKNOWN;
                        break;
                    case "9":
                        throw new RuntimeException("Illegal status");

                }
            }
        }
        return mines;
    }

    private static boolean checkRange(int pos, int delta, int max) {
        return 0 <= pos + delta && pos + delta < max;
    }

    private static int[][] deepclone(int[][] source) {
        int[][] target = new int[source.length][];
        for (int i = 0; i < source.length; ++i) {
            target[i] = new int[source[i].length];
            System.arraycopy(source[i], 0, target[i], 0, source[i].length);
        }
        return target;
    }

    static class Pair {
        public Integer key;
        public Integer value;

        public Pair(Integer key, Integer value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            return ((Pair) o).key == key && ((Pair) o).value == value;
        }

        @Override
        public int hashCode() {
            return key * 100000 + value;
        }

    }

    private int nMines;
    int[][] actualMap;

    public MineSweeper(final String board, final int nMines) {
        this.nMines = nMines;
        actualMap = new int[board.split("\n").length][];
        initMap(board, actualMap);
    }

    public String toString() {
        for (int i = 0; i < actualMap.length; ++i) {
            for (int j = 0; j < actualMap[0].length; ++j) {
                if (actualMap[i][j] == UNKNOWN) {
                    return "?";
                }
            }
        }

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < actualMap.length; ++i) {
            if (i != 0) sb.append("\n");
            for (int j = 0; j < actualMap[0].length; ++j) {
                if (j != 0) sb.append(" ");
                if (actualMap[i][j] == MINE) sb.append("x");
                else if (actualMap[i][j] == UNKNOWN) sb.append("?");
                else sb.append(actualMap[i][j]);
            }
        }
        return sb.toString();
    }

    private static void add(Set<Pair> set, Queue<Pair> queue, int x, int y) {
        if (!set.contains(new Pair(x, y))) {
            Pair pair = new Pair(x, y);
            set.add(pair);
            queue.add(pair);
        }
    }

    private void expand(int x, int y, Queue<Pair> queue, Set<Pair> set) {
        int unkonwnCount = 0;
        int mineCount = 0;

        for (int i = 0; i < 8; ++i) {
            if (checkRange(x, dx[i], actualMap.length)
                    && checkRange(y, dy[i], actualMap[0].length)) {
                if (actualMap[x + dx[i]][y + dy[i]] == UNKNOWN) unkonwnCount++;
                if (actualMap[x + dx[i]][y + dy[i]] == MINE) mineCount++;
            }
        }

        if (unkonwnCount + mineCount == actualMap[x][y]) { // MARInteger ALL UNKNOWN AS MINE
            for (int i = 0; i < 8; ++i) {
                if (checkRange(x, dx[i], actualMap.length)
                        && checkRange(y, dy[i], actualMap[0].length)) {
                    if (actualMap[x + dx[i]][y + dy[i]] == UNKNOWN) {
                        actualMap[x + dx[i]][y + dy[i]] = MINE;
                        for (int j = 0; j < 8; ++j) {
                            if (checkRange(x + dx[i], dx[j], actualMap.length)
                                    && checkRange(y + dy[i], dy[j], actualMap[0].length)) {
                                if (actualMap[x + dx[i] + dx[j]][y + dy[i] + dy[j]] > 0) {
                                    add(set, queue, x + dx[i] + dx[j], y + dy[i] + dy[j]);
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        if (mineCount == actualMap[x][y] && unkonwnCount != 0) { // OPEN UP
            for (int i = 0; i < 8; ++i) {
                if (checkRange(x, dx[i], actualMap.length)
                        && checkRange(y, dy[i], actualMap[0].length)) {
                    if (actualMap[x + dx[i]][y + dy[i]] == UNKNOWN) {
                        actualMap[x + dx[i]][y + dy[i]] = Game.open(x + dx[i], y + dy[i]);
                        add(set, queue, x + dx[i], y + dy[i]);
                    }
                }
            }
        }
    }

    private void doExplicitWork() {
        Queue<Pair> queue = new ArrayBlockingQueue<>(1000);
        Set<Pair> set = new HashSet<>();

        for (int i = 0; i < actualMap.length; ++i) {
            for (int j = 0; j < actualMap[0].length; ++j) {
                if (actualMap[i][j] != UNKNOWN) {
                    add(set, queue, i, j);
                }
            }
        }

        while (!queue.isEmpty()) {
            Pair pair = queue.poll();
            expand(pair.key, pair.value, queue, set);
            set.remove(pair);
        }

        int mineCount = 0;
        int unknownCount = 0;
        for (int i = 0; i < actualMap.length; ++i) {
            for (int j = 0; j < actualMap[0].length; ++j) {
                switch (actualMap[i][j]) {
                    case MINE:
                        mineCount++;
                        break;
                    case UNKNOWN:
                        unknownCount++;
                        break;
                }
            }
        }

        if (unknownCount != 0 && unknownCount + mineCount == nMines) {  // MARK ALL UNKNOWN AS MINE
            for (int i = 0; i < actualMap.length; ++i) {
                for (int j = 0; j < actualMap[0].length; ++j) {
                    if (actualMap[i][j] == UNKNOWN) actualMap[i][j] = MINE;
                }
            }
        }

        if (unknownCount != 0 && mineCount == nMines) {
            for (int i = 0; i < actualMap.length; ++i) {
                for (int j = 0; j < actualMap[0].length; ++j) {
                    if (actualMap[i][j] == UNKNOWN) actualMap[i][j] = Game.open(i, j);
                }
            }
        }
    }

    private void doSubControl(Map.Entry<Integer, Set<Pair>> a, Map.Entry<Integer, Set<Pair>> b) {  // A - B
        if (a.getValue().containsAll(b.getValue())) { // A - B
            Set<Pair> tmp = new HashSet<>(a.getValue());
            tmp.removeAll(b.getValue());
            if (tmp.size() == 0) return;
            if (a.getKey() == b.getKey()) { // MARK ALL AS BLANK
                for (Pair pair : tmp) {
                    actualMap[pair.key][pair.value] = Game.open(pair.key, pair.value);
                }
            } else if (tmp.size() == a.getKey() - b.getKey()) { // MARK ALL AS MINE
                for (Pair pair : tmp) {
                    actualMap[pair.key][pair.value] = MINE;
                }
            }
        }
    }

    private void doImplicitWork() {
        List<Map.Entry<Integer, Set<Pair>>> list = new ArrayList<>(100);

        for (int i = 0; i < actualMap.length; ++i) {
            for (int j = 0; j < actualMap[0].length; ++j) {
                if (actualMap[i][j] > 0) {
                    final Set<Pair> set = new HashSet<>(10);
                    int mines = 0;
                    for (int k = 0; k < 8; ++k) {
                        if (checkRange(i, dx[k], actualMap.length)
                                && checkRange(j, dy[k], actualMap[0].length)) {
                            if (actualMap[i + dx[k]][j + dy[k]] == UNKNOWN) {
                                set.add(new Pair(i + dx[k], j + dy[k]));
                            }
                            mines += actualMap[i + dx[k]][j + dy[k]] == MINE ? 1 : 0;
                        }
                    }
                    int leftMines = actualMap[i][j] - mines;
                    if (leftMines > 0 && set.size() > 0) {
                        list.add(new Map.Entry<Integer, Set<Pair>>() {
                            @Override
                            public Integer getKey() {
                                return leftMines;
                            }

                            @Override
                            public Set<Pair> getValue() {
                                return set;
                            }

                            @Override
                            public Set<Pair> setValue(Set<Pair> value) {
                                return null;
                            }

                            @Override
                            public boolean equals(Object o) {
                                return false;
                            }

                            @Override
                            public int hashCode() {
                                return 0;
                            }
                        });
                    }
                }
            }
        }

        for (int i = 0; i < list.size(); ++i) {
            for (int j = i + 1; j < list.size(); ++j) {
                doSubControl(list.get(i), list.get(j));
                doSubControl(list.get(j), list.get(i));
            }
        }

    }

    private boolean checkState(int[][] map) {
        for (int i = 0; i < map.length; ++i) {
            for (int j = 0; j < map[0].length; ++j) {
                if (map[i][j] > 0) {
                    int mines = 0;
                    for (int k = 0; k < 8; ++k) {
                        if (checkRange(i, dx[k], map.length)
                                && checkRange(j, dy[k], map[0].length)) {
                            mines += map[i + dx[k]][j + dy[k]] == MINE ? 1 : 0;
                        }
                    }
                    if (mines != map[i][j]) return false;
                }
            }
        }
        return true;
    }

    private void deepmind(int[][] map, final int pos, final List<Pair> unknownBlocks, final int leftMines, final List<List<Pair>> resultSet, Stack<Pair> result) {
        if (pos == unknownBlocks.size() && leftMines != 0) return;
        if (leftMines == 0 || pos == unknownBlocks.size()) {
            if (checkState(map)) resultSet.add(new ArrayList<>(result));
            return;
        }

        Pair pair = unknownBlocks.get(pos);
        map[pair.key][pair.value] = MINE;
        result.push(pair);
        deepmind(map, pos + 1, unknownBlocks, leftMines - 1, resultSet, result);
        map[pair.key][pair.value] = UNKNOWN;
        result.pop();

        deepmind(map, pos + 1, unknownBlocks, leftMines, resultSet, result);
    }

    private void deepmind() {
        int markMines = 0;
        Set<Pair> unknownBlocks = new HashSet<>();
        int[][] copy = deepclone(actualMap);
        boolean onlyOneArea = true;

        for (int i = 0; i < actualMap.length; ++i) {
            for (int j = 0; j < actualMap[0].length; ++j) {
                if (copy[i][j] == UNKNOWN) {
                    onlyOneArea &= unknownBlocks.size() == 0;
                    Queue<Pair> queue = new ArrayBlockingQueue<>(1000);
                    add(unknownBlocks, queue, i, j);

                    while (!queue.isEmpty()) {
                        Pair pair = queue.poll();
                        copy[pair.key][pair.value] = 0;

                        for (int k = 0; k < 8; ++k) {
                            if (checkRange(pair.key, dx[k], actualMap.length)
                                    && checkRange(pair.value, dy[k], actualMap[0].length)) {
                                if (copy[pair.key + dx[k]][pair.value + dy[k]] == UNKNOWN) {
                                    add(unknownBlocks, queue, pair.key + dx[k], pair.value + dy[k]);
                                }
                            }
                        }
                    }

                }
                markMines += copy[i][j] == MINE ? 1 : 0;
            }
        }

//         if (!onlyOneArea) return;
        if (unknownBlocks.size() > 20) return;  // GIVE UP

        int leftMines = nMines - markMines;

        List<List<Pair>> resultSet = new ArrayList<>();

        deepmind(copy, 0, new ArrayList<>(unknownBlocks), leftMines, resultSet, new Stack<>());

//        System.err.println("ONLY ONE AREA " + leftMines + " " + resultSet.size());

        Map<Pair, Integer> countMap = new HashMap<>();
        for (Pair pair : unknownBlocks) countMap.put(pair, 0);

        for (List<Pair> list : resultSet) {
            for (Pair pair : list) {
                countMap.put(pair, countMap.get(pair) + 1);
            }
        }

        for (Map.Entry<Pair, Integer> entry : countMap.entrySet()) {
            if (entry.getValue() == 0) {
                actualMap[entry.getKey().key][entry.getKey().value] = Game.open(entry.getKey().key, entry.getKey().value);
            } else if (entry.getValue() == resultSet.size()) {
                actualMap[entry.getKey().key][entry.getKey().value] = MINE;
            }
        }

    }

    private boolean CanImprove(int[][] array) {
        for (int i = 0; i < actualMap.length; ++i) {
            for (int j = 0; j < actualMap[0].length; ++j) {
                if (array[i][j] != actualMap[i][j]) {
                    return true;
                }
            }
        }
        return false;
    }

    public String solve() {

        int[][] copy;
        int epoch = 0;
        do {
            copy = deepclone(actualMap);
//            System.err.println("EPOCH : " + epoch++);
//            System.err.println(toString());

            doExplicitWork();
            doImplicitWork();
            if (!CanImprove(copy)) deepmind();
        } while (CanImprove(copy));

        return toString();
    }
}