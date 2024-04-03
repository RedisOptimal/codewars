/**
 * It's just a simple matrix multiply.
 * 
 * [F(0), F(1)] * [[0, 1]   =  [F(1), F(0) + F(1)] = [F(1), F(2)]
 *                 [1, 1]]
 * 
 * [F(0), F(1)] * [[0, 1] ^ N = [F(N), F(N+1)]
 *                 [1, 1]]
 * 
 * X^N have fast calcuate algorithm. The time complexity is O(LogN)
 */
import java.math.BigInteger;

public class Fibonacci {

    private static class Matrix {
        private final int n;
        private final int m;
        private final BigInteger [][] arr;

        public Matrix(int n, int m) {
            this.n = n;
            this.m = m;
            arr = new BigInteger[n][m];
            for (int i = 0;i < n; ++i) {
                for (int j = 0;j < m; ++j) {
                    arr[i][j] = BigInteger.valueOf(0);
                }
            }
        }

        public BigInteger [][] getArr() {
            return arr;
        }

        public static Matrix mul(final Matrix a, final Matrix b) {
            if (a == null || b == null) {
                return null;
            }
            assert a.m == b.n;
            Matrix ret = new Matrix(a.n, b.m);
            BigInteger [][] aData = a.getArr();
            BigInteger [][] bData = b.getArr();
            BigInteger [][] retData = ret.getArr();
            for (int i = 0;i < a.n; ++i) {
                for (int j = 0;j < b.m; ++j) {
                    for (int k = 0;k < a.m; ++k) {
                        retData[i][j] = retData[i][j].add(aData[i][k].multiply(bData[k][j]));
                    }
                }
            }
            return ret;
        }
    }

    private final static Matrix elementMatrix;

    static {
        elementMatrix = new Matrix(2, 2);
        BigInteger [][] data = elementMatrix.getArr();
        data[0][0] = BigInteger.valueOf(0);
        data[0][1] = data[1][0] = data[1][1] = BigInteger.valueOf(1);
    }

    private static Matrix power(Matrix m, long n) {
        if (n == 0) {
            return null;
        } else if (n == 1) {
            return m;
        } else {
            Matrix tmp = power(m, n / 2);
            tmp = Matrix.mul(tmp, tmp);
            if ((n & 1) == 1) {
                tmp = Matrix.mul(tmp, m);
            }
            return tmp;
        }
    }

    public static BigInteger fib(BigInteger n) {
        if (Math.abs(n.longValue()) < 2) {
            return n;
        }
        Matrix matrix = new Matrix(1, 2);
        BigInteger [][] data = matrix.getArr();
        data[0][0] = BigInteger.valueOf(0);
        data[0][1] = BigInteger.valueOf(1);
        Matrix factor = power(elementMatrix, Math.abs(n.longValue()));
        Matrix result = Matrix.mul(matrix, factor);
        // ...
        return n.longValue() >= 0 || (Math.abs(n.longValue()) & 1) == 1  ? result.getArr()[0][0] : result.getArr()[0][0].multiply(BigInteger.valueOf(-1));
    }

}