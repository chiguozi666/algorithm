package myMathUtils;

import java.util.Arrays;

public class VectorUtil {
    //矩阵求逆
    public static double[][] inverse(double[][] A) {
        double[][] B = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                B[i][j] = A[i][j];
            }
        }
        double[][] C = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                C[i][j] = ((i+j)%2==0?1:-1) * determinant(minor(B, j, i));
            }
        }
        double[][] D = new double[A.length][A[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                D[i][j] = C[i][j] / determinant(A);
            }
        }
        return D;
    }
    //求解矩阵的行列式
    private static double determinant(double[][] a) {
        if (a.length == 1) {
            return a[0][0];
        }
        int det = 0;
        for (int i = 0; i < a[0].length; i++) {
            det += (int) Math.pow(-1, i) * a[0][i] * determinant(minor(a, 0, i));
        }
        return det;
    }
    //求解二维矩阵在某一位置的伴随矩阵
    private static double[][] minor(double[][] b, int i, int j) {
        double[][] a = new double[b.length - 1][b[0].length - 1];
        for (int x = 0, y = 0; x < b.length; x++) {
            if (x == i) {
                continue;
            }
            for (int m = 0,n = 0; m < b[0].length; m++) {
                if (m == j) {
                    continue;
                }
                a[y][n] = b[x][m];
                n++;
            }
            y++;
        }
        return a;
    }

    public static void main(String[] args) {
        double d[][] = new double[][]{{1,1},{1,1}};
        double res[][] = inverse(d);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                System.out.print(res[i][j]+"  ");
            }
            System.out.println();
        }
    }
}
