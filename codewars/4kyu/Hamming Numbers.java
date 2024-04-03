import java.util.ArrayList;
import java.util.List;

public class Hamming {

    private static final List<Long> queue = new ArrayList<>(5001);

    public static long hamming(int n) {
        queue.clear();
        queue.add(1L);
        int p2, p3, p5;
        p2 = p3 = p5 = 0;
        if (n > 1) {
            for (int i = 0;i < n; ++i) {
                long n2 = queue.get(p2) * 2;
                long n3 = queue.get(p3) * 3;
                long n5 = queue.get(p5) * 5;
                long min = Math.min(Math.min(n2, n3), n5);
                queue.add(min);
                if (n2 == min) {
                    p2++;
                }
                if (n3 == min) {
                    p3++;
                }
                if (n5 == min) {
                    p5++;
                }
            }
        }
        return queue.get(n - 1);
    }

}