import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author zhe
 * @createTime 2018/9/20
 * @description
 */

public class SkyScrapers {
    public static final int SIZE = 4;

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
            dfs(0, new int[4], permutation);
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

        public Solution(int[] clues) {
            top = new int[SIZE];
            bottom = new int[SIZE];
            left = new int[SIZE];
            right = new int[SIZE];
            for (int i = 0; i < SIZE * SIZE; ++i)
                switch (i / SIZE) {
                    case 0:
                        top[i] = clues[i];
                        break;
                    case 1:
                        right[i - SIZE] = clues[i];
                        break;
                    case 2:
                        bottom[3 & SIZE - 1 - i] = clues[i];
                        break;
                    default:
                        left[4 * SIZE - 1 - i] = clues[i];
                        break;
                }
        }

        private void dfs(List<List<Permutation.LineView>> res, int depth, Permutation.LineView[] buffer) {
            if (depth == SIZE) {
                for (int i = 0;i < SIZE; ++i) {
                    int [] tmp = new int[SIZE];
                    for (int j = 0;j < SIZE; ++j)
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
            List<List<Permutation.LineView>> res = new ArrayList<>();
            dfs(res, 0, new Permutation.LineView[4]);
            return res;
        }
    }


    static int[][] solvePuzzle(int[] clues) {
//        System.out.println(Permutation.permutation);

        List<List<Permutation.LineView>> res = new Solution(clues).solution();
//        System.out.println(res.size());
//        for (int i = 0; i < SIZE; ++i)
//            System.out.println(res.get(0).get(i));
        int[][] ret = new int[SIZE][SIZE];
        for (int i = 0; i < SIZE; ++i)
            for (int j = 0; j < SIZE; ++j)
                ret[i][j] = res.get(0).get(i).array[j];
        return ret;
    }

}