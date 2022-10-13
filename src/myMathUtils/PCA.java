package myMathUtils;

import java.util.List;

//用于数据降维
public class PCA {
    private static final double threshold = 0.95;
    public static double[][] getPCA(int n,double[][] input){
        return Matrix.transpose(Data.getPCA(n,input));
    }

}
class Matrix {

    static int numMults = 0; //Keeps track of the number of multiplications performed

    /**
     * Test code for SVD. Uses example from MIT video: http://www.youtube.com/watch?v=cOUTpqlX-Xs
     */
    public static void main(String[] args) {
        System.out.println("Original matrix:");
        double[][] test = {{0, 1,1}, {1, 1,0}}; //C
        Matrix.print(test);
        double[][][] SVD = Matrix.singularValueDecomposition(test);
        double[][] U = SVD[0];
        double[][] S = SVD[1];
        double[][] V = SVD[2];
        System.out.println("U-matrix:");
        Matrix.print(U);
        System.out.println("Sigma-matrix:");
        Matrix.print(S);
        System.out.println("V-matrix:");
        Matrix.print(V);
        System.out.println("Decomposition product (C = US(V^T)):");
        Matrix.print(Matrix.multiply(U, Matrix.multiply(S, Matrix.transpose(V)))); //Should be C
    }


    /**
     * Computes the singular value decomposition (SVD) of the input matrix.
     * @param input		the input matrix
     * @return			the SVD of input, {U,S,V}, such that input = US(V^T). U and S are
     * 					orthogonal matrix, and the non-zero entries of the diagonal matrix S are
     * 					the
     */
    static double[][][] singularValueDecomposition(double[][] input) {
        double[][] C = Matrix.copy(input);
        double[][] CTC = multiply(transpose(C), C); //(C^T)C = V(S^T)S(V^T)
        EigenSet eigenC = eigenDecomposition(CTC);
        double[][] S = new double[C.length][C.length]; //Diagonal matrix
        for(int i = 0; i < S.length; i++) {
            S[i][i] = Math.sqrt(eigenC.values[i]); //Squareroots of eigenvalues are entries of S
        }
        double[][] V = eigenC.vectors;
        double[][] CV = multiply(C, V); //CV = US
        double[][] invS = copy(S); //Inverse of S
        for(int i = 0; i < invS.length; i++) {
            invS[i][i] = 1.0/S[i][i];
        }
        double[][] U = multiply(CV, invS); //U = CV(S^-1)
        return new double[][][] {U, S, V};
    }

    /**
     * Determines the eigenvalues and eigenvectors of a matrix by using the QR algorithm. Repeats
     * until no eigenvalue changes by more than 1/100000.
     * @param	input	input matrix; must be square
     * @return			an EigenSet containing the eigenvalues and corresponding eigenvectors of
     * 					input
     */
    static EigenSet eigenDecomposition(double[][] input) {
        if(input.length != input[0].length) {
            throw new MatrixException("Eigendecomposition not defined on nonsquare matrices.");
        }
        double[][] copy = copy(input);
        double[][] Q = new double[copy.length][copy.length];
        for(int i = 0; i < Q.length; i++) {
            Q[i][i] = 1; //Q starts as an identity matrix
        }
        boolean done = false;
        while(!done) {
            double[][][] fact = Matrix.QRFactorize(copy);
            double[][] newMat = Matrix.multiply(fact[1], fact[0]); //[A_k+1] := [R_k][Q_k]
            Q = Matrix.multiply(fact[0], Q);
            //Stop the loop if no eigenvalue changes by more than 1/100000
            for(int i = 0; i < copy.length; i++) {
                if(Math.abs(newMat[i][i] - copy[i][i]) > 0.1) {//todo：精度必须解决
                    copy = newMat;
                    break;
                } else if(i == copy.length - 1) { //End of copy table
                    done = true;
                }
            }
        }
        EigenSet ret = new EigenSet();
        ret.values = Matrix.extractDiagonalEntries(copy); //Eigenvalues lie on diagonal
        ret.vectors = Q; //Columns of Q converge to the eigenvectors
        return ret;
    }

    /**
     * Produces an array of the diagonal entries in the input matrix.
     * @param input	input matrix
     * @return		the entries on the diagonal of input
     */
    static double[] extractDiagonalEntries(double[][] input) {
        double[] out = new double[input.length];
        for(int i = 0; i<input.length; i++) {
            out[i] = input[i][i];
        }
        return out;
    }

    /**
     * Performs a QR factorization on the input matrix.
     * @param input	input matrix
     * @return		{Q, R}, the QR factorization of input.
     */
    static double[][][] QRFactorize(double[][] input) {
        double[][][] out = new double[2][][];
        double[][] orthonorm = gramSchmidt(input);
        out[0] = orthonorm; //Q is the matrix of the orthonormal vectors formed by GS on input
        double[][] R = new double[orthonorm.length][orthonorm.length];
        for(int i = 0; i < R.length; i++) {
            for(int j = 0; j <= i; j++) {
                R[i][j] = dot(input[i], orthonorm[j]);
            }
        }
        out[1] = R;
        return out;
    }

    /**
     * Converts the input list of vectors into an orthonormal list with the same span.
     * @param input	list of vectors
     * @return		orthonormal list with the same span as input
     */
    static double[][] gramSchmidt(double[][] input) {
        double[][] out = new double[input.length][input[0].length];
        for(int outPos = 0; outPos < out.length; outPos++) {
            double[] v = input[outPos];
            for(int j = outPos - 1; j >= 0; j--) {
                double[] sub = proj(v, out[j]);
                v = subtract(v, sub); //Subtract off non-orthogonal components
            }
            out[outPos] = normalize(v); //return an orthonormal list
        }
        return out;
    }

    /**
     * Returns the Givens rotation matrix with parameters (i, j, th).
     * @param size	total number of rows/columns in the matrix
     * @param i		the first axis of the plane of rotation; i > j
     * @param j		the second axis of the plane of rotation; i > j
     * @param th	the angle of the rotation
     * @return		the Givens rotation matrix G(i,j,th)
     */
    static double[][] GivensRotation(int size, int i, int j, double th) {
        double[][] out = new double[size][size];
        double sine = Math.sin(th);
        double cosine = Math.cos(th);
        for(int x = 0; x < size; x++) {
            if(x != i && x != j) {
                out[x][x] = cosine;
            } else {
                out[x][x] = 1;
            }
        }
        out[i][j] = -sine;//ith column, jth row
        out[j][i] = sine;
        return out;
    }

    /**
     * Returns the transpose of the input matrix.
     * @param matrix	double[][] matrix of values
     * @return			the matrix transpose of matrix
     */
    static double[][] transpose(double[][] matrix) {
        double[][] out = new double[matrix[0].length][matrix.length];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[0].length; j++) {
                out[i][j] = matrix[j][i];
            }
        }
        return out;
    }

    /**
     * Returns the sum of a and b.
     * @param a	double[][] matrix of values
     * @param b	double[][] matrix of values
     * @return	the matrix sum a + b
     */
    static double[][] add(double[][] a, double[][] b) {
        if(a.length != b.length || a[0].length != b[0].length) {
            throw new MatrixException("Matrices not same size.");
        }
        double[][] out = new double[a.length][a[0].length];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[0].length; j++) {
                out[i][j] = a[i][j] + b[i][j];
            }
        }
        return out;
    }

    /**
     * Returns the difference of a and b.
     * @param a	double[][] matrix of values
     * @param b	double[][] matrix of values
     * @return	the matrix difference a - b
     */
    static double[][] subtract(double[][] a, double[][] b) {
        if(a.length != b.length || a[0].length != b[0].length) {
            throw new MatrixException("Matrices not same size.");
        }
        double[][] out = new double[a.length][a[0].length];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[0].length; j++) {
                out[i][j] = a[i][j] - b[i][j];
            }
        }
        return out;
    }

    /**
     * Returns the sum of a and b.
     * @param a	double[] vector of values
     * @param b	double[] vector of values
     * @return	the vector sum a + b
     */
    static double[] add(double[] a, double[] b) {
        if(a.length != b.length) {
            throw new MatrixException("Vectors are not same length.");
        }
        double[] out = new double[a.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = a[i] + b[i];
        }
        return out;
    }

    /**
     * Returns the difference of a and b.
     * @param a	double[] vector of values
     * @param b	double[] vector of values
     * @return	the vector difference a - b
     */
    static double[] subtract(double[] a, double[] b) {
        if(a.length != b.length) {
            throw new MatrixException("Vectors are not same length.");
        }
        double[] out = new double[a.length];
        for(int i = 0; i < out.length; i++) {
            out[i] = a[i] - b[i];
        }
        return out;
    }

    /**
     * Returns the matrix product of a and b; if the horizontal length of a is not equal to the
     * vertical length of b, throws an exception.
     * @param a	double[][] matrix of values
     * @param b	double[][] matrix of values
     * @return	the matrix product ab
     */
    static double[][] multiply(double[][] a, double[][] b) {
        if(a.length != b[0].length) {
            throw new MatrixException("Matrices not compatible for multiplication.");
        }
        double[][] out = new double[b.length][a[0].length];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[0].length; j++) {
                double[] row = getRow(a, j);
                double[] column = getColumn(b, i);
                out[i][j] = dot(row, column);
            }
        }
        return out;
    }

    /**
     * Returns a version of mat scaled by a constant.
     * @param mat	input matrix
     * @param coeff	constant by which to scale
     * @return		mat scaled by coeff
     */
    static double[][] scale(double[][] mat, double coeff) {
        double[][] out = new double[mat.length][mat[0].length];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out[0].length; j++) {
                out[i][j] = mat[i][j] * coeff;
            }
        }
        return out;
    }

    /**
     * Takes the dot product of two vectors, {a[0]b[0], ..., a[n]b[n]}.
     * @param a	double[] of values
     * @param b	double[] of values
     * @return	the dot product of a with b
     */
    static double dot(double[] a, double[] b) {
        if(a.length != b.length) {
            throw new MatrixException("Vector lengths not equal: " + a.length + "=/=" + b.length);
        }
        double sum = 0;
        for(int i = 0; i < a.length; i++) {
            numMults++;
            sum += a[i] * b[i];
        }
        return sum;
    }

    /**
     * Returns a copy of the input matrix.
     * @param input	double[][] to be copied
     */
    static double[][] copy(double[][] input) {
        double[][] copy = new double[input.length][input[0].length];
        for(int i = 0; i < copy.length; i++) {
            for(int j = 0; j < copy[i].length; j++) {
                copy[i][j] = input[i][j];
            }
        }
        return copy;
    }

    /**
     * Returns the ith column of the input matrix.
     */
    static double[] getColumn(double[][] matrix, int i) {
        return matrix[i];
    }

    /**
     * Returns the ith row of the input matrix.
     */
    static double[] getRow(double[][] matrix, int i) {
        double[] vals = new double[matrix.length];
        for(int j = 0; j < vals.length; j++) {
            vals[j] = matrix[j][i];
        }
        return vals;
    }

    /**
     * Returns the projection of vec onto the subspace spanned by proj
     * @param vec	vector to be projected
     * @param proj	spanning vector of the target subspace
     * @return		proj_proj(vec)
     */
    static double[] proj(double[] vec, double[] proj) {
        double constant = dot(proj, vec)/dot(proj, proj);
        double[] projection = new double[vec.length];
        for(int i = 0; i < proj.length; i++) {
            projection[i] = proj[i]*constant;
        }
        return projection;
    }

    /**
     * Returns a normalized version of the input vector, i.e. vec scaled such that ||vec|| = 1.
     * @return	vec/||vec||
     */
    static double[] normalize(double[] vec) {
        double[] newVec = new double[vec.length];
        double norm = norm(vec);
        for(int i = 0; i < vec.length; i++) {
            newVec[i] = vec[i]/norm;
        }
        return newVec;
    }

    /**
     * Computes the norm of the input vector
     * @return ||vec||
     */
    static double norm(double[] vec) {
        return Math.sqrt(dot(vec,vec));
    }

    /**
     * Prints the input matrix with each value rounded to 4 significant figures
     */
    static void print(double[][] matrix) {
        for(int j = 0; j < matrix[0].length; j++) {
            for(int i = 0; i < matrix.length; i++) {
                double formattedValue = Double.parseDouble(String.format("%.4g%n", matrix[i][j]));
                if(Math.abs(formattedValue) < 0.00001) { //Hide negligible values
                    formattedValue = 0;
                }
                System.out.print(formattedValue + "\t");
            }
            System.out.print("\n");
        }
        System.out.println("");
    }
}

/**
 * Exception class thrown when invalid matrix calculations are attempted
 */
class MatrixException extends RuntimeException {
    MatrixException(String string) {
        super(string);
    }
}

/**
 * Data holder class that contains a set of eigenvalues and their corresponding eigenvectors.
 * @author	Kushal Ranjan
 * @version 051413
 */
class EigenSet {
    double[] values;
    double[][] vectors;
}
class Data {
    double[][] matrix; //matrix[i] is the ith row; matrix[i][j] is the ith row, jth column

    /**
     * Constructs a new data matrix.
     * @param vals	data for new Data object; dimensions as columns, data points as rows.
     */
    Data(double[][] vals) {
        matrix = Matrix.copy(vals);
    }

    /**
     * Test code. Constructs an arbitrary data table of 5 data points with 3 variables, normalizes
     * it, and computes the covariance matrix and its eigenvalues and orthonormal eigenvectors.
     * Then determines the two principal components.
     */
    public static void main(String[] args) {
        double[][] data = {{4, 4.2, 3.9, 4.3, 4.1}, {2, 2.1, 2, 2.1, 2.2},
                {0.6, 0.59, 0.58, 0.62, 0.63}};
        System.out.println("Raw data:");
        Matrix.print(data);
        Data dat = new Data(data);
        dat.center();
        double[][] cov = dat.covarianceMatrix();
        System.out.println("Covariance matrix:");
        Matrix.print(cov);
        EigenSet eigen = dat.getCovarianceEigenSet();
        double[][] vals = {eigen.values};
        System.out.println("Eigenvalues:");
        Matrix.print(vals);
        System.out.println("Corresponding eigenvectors:");
        Matrix.print(eigen.vectors);
        System.out.println("Two principal components:");
        Matrix.print(dat.buildPrincipalComponents(3, eigen));
        System.out.println("Principal component transformation:");
        Matrix.print(Data.principalComponentAnalysis(data, 3));
    }
    public static double[][] getPCA(int n, double[][] input){
//        double data[][] = new double[input.size()][input.get(0).size()];
//        for (int i = 0; i < input.size(); i++) {
//            List<Double> doubles = input.get(i);
//            for (int j = 0; j < input.get(0).size(); j++) {
//                data[i][j] = doubles.get(j);
//            }
//        }
        return principalComponentAnalysis(input,n);
    }

    /**
     * PCA implemented using the NIPALS algorithm. The return value is a double[][], where each
     * double[] j is an array of the scores of the jth data point corresponding to the desired
     * number of principal components.
     * @param input			input raw data array
     * @param numComponents	desired number of PCs
     * @return				the scores of the data array against the PCS
     */
    private static double[][] PCANIPALS(double[][] input, int numComponents) {
        Data data = new Data(input);
        data.center();
        double[][][] PCA = data.NIPALSAlg(numComponents);
        double[][] scores = new double[numComponents][input[0].length];
        for(int point = 0; point < scores[0].length; point++) {
            for(int comp = 0; comp < PCA.length; comp++) {
                scores[comp][point] = PCA[comp][0][point];
            }
        }
        return scores;
    }

    /**
     * Implementation of the non-linear iterative partial least squares algorithm on the data
     * matrix for this Data object. The number of PCs returned is specified by the user.
     * @param numComponents	number of principal components desired
     * @return				a double[][][] where the ith double[][] contains ti and pi, the scores
     * 						and loadings, respectively, of the ith principal component.
     */
    private double[][][] NIPALSAlg(int numComponents) {
        final double THRESHOLD = 0.00001;
        double[][][] out = new double[numComponents][][];
        double[][] E = Matrix.copy(matrix);
        for(int i = 0; i < out.length; i++) {
            double eigenOld = 0;
            double eigenNew = 0;
            double[] p = new double[matrix[0].length];
            double[] t = new double[matrix[0].length];
            double[][] tMatrix = {t};
            double[][] pMatrix = {p};
            for(int j = 0; j < t.length; j++) {
                t[j] = matrix[i][j];
            }
            do {
                eigenOld = eigenNew;
                double tMult = 1/Matrix.dot(t, t);
                tMatrix[0] = t;
                p = Matrix.scale(Matrix.multiply(Matrix.transpose(E), tMatrix), tMult)[0];
                p = Matrix.normalize(p);
                double pMult = 1/Matrix.dot(p, p);
                pMatrix[0] = p;
                t = Matrix.scale(Matrix.multiply(E, pMatrix), pMult)[0];
                eigenNew = Matrix.dot(t, t);
            } while(Math.abs(eigenOld - eigenNew) > THRESHOLD);
            tMatrix[0] = t;
            pMatrix[0] = p;
            double[][] PC = {t, p}; //{scores, loadings}
            E = Matrix.subtract(E, Matrix.multiply(tMatrix, Matrix.transpose(pMatrix)));
            out[i] = PC;
        }
        return out;
    }

    /**
     * Previous algorithms for performing PCA
     */

    /**
     * Performs principal component analysis with a specified number of principal components.
     * @param input			input data; each double[] in input is an array of values of a single
     * 						variable for each data point
     * @param numComponents	number of components desired
     * @return				the transformed data set
     */
    private static double[][] principalComponentAnalysis(double[][] input, int numComponents) {
        Data data = new Data(input);
        data.center();
        EigenSet eigen = data.getCovarianceEigenSet();
        double[][] featureVector = data.buildPrincipalComponents(numComponents, eigen);
        double[][] PC = Matrix.transpose(featureVector);
        double[][] inputTranspose = Matrix.transpose(input);
        return Matrix.transpose(Matrix.multiply(PC, inputTranspose));
    }

    /**
     * Returns a list containing the principal components of this data set with the number of
     * loadings specified.
     * @param numComponents	the number of principal components desired
     * @param eigen			EigenSet containing the eigenvalues and eigenvectors
     * @return				the numComponents most significant eigenvectors
     */
    private double[][] buildPrincipalComponents(int numComponents, EigenSet eigen) {
        double[] vals = eigen.values;
        if(numComponents > vals.length) {
            throw new RuntimeException("Cannot produce more principal components than those provided.");
        }
        boolean[] chosen = new boolean[vals.length];
        double[][] vecs = eigen.vectors;
        double[][] PC = new double[numComponents][];
        for(int i = 0; i < PC.length; i++) {
            int max = 0;
            while(chosen[max]) {
                max++;
            }
            for(int j = 0; j < vals.length; j++) {
                if(Math.abs(vals[j]) > Math.abs(vals[max]) && !chosen[j]) {
                    max = j;
                }
            }
            chosen[max] = true;
            PC[i] = vecs[max];
        }
        return PC;
    }

    /**
     * Uses the QR algorithm to determine the eigenvalues and eigenvectors of the covariance
     * matrix for this data set. Iteration continues until no eigenvalue changes by more than
     * 1/10000.
     * @return	an EigenSet containing the eigenvalues and eigenvectors of the covariance matrix
     */
    private EigenSet getCovarianceEigenSet() {
        double[][] data = covarianceMatrix();
        return Matrix.eigenDecomposition(data);
    }

    /**
     * Constructs the covariance matrix for this data set.
     * @return	the covariance matrix of this data set
     */
    private double[][] covarianceMatrix() {
        double[][] out = new double[matrix.length][matrix.length];
        for(int i = 0; i < out.length; i++) {
            for(int j = 0; j < out.length; j++) {
                double[] dataA = matrix[i];
                double[] dataB = matrix[j];
                out[i][j] = covariance(dataA, dataB);
            }
        }
        return out;
    }

    /**
     * Returns the covariance of two data vectors.
     * @param a	double[] of data
     * @param b	double[] of data
     * @return	the covariance of a and b, cov(a,b)
     */
    private static double covariance(double[] a, double[] b) {
        if(a.length != b.length) {
            throw new MatrixException("Cannot take covariance of different dimension vectors.");
        }
        double divisor = a.length - 1;
        double sum = 0;
        double aMean = mean(a);
        double bMean = mean(b);
        for(int i = 0; i < a.length; i++) {
            sum += (a[i] - aMean) * (b[i] - bMean);
        }
        return sum/divisor;
    }

    /**
     * Centers each column of the data matrix at its mean.
     */
    private void center() {
        matrix = normalize(matrix);
    }


    /**
     * Normalizes the input matrix so that each column is centered at 0.
     */
    private double[][] normalize(double[][] input) {
        double[][] out = new double[input.length][input[0].length];
        for(int i = 0; i < input.length; i++) {
            double mean = mean(input[i]);
            for(int j = 0; j < input[i].length; j++) {
                out[i][j] = input[i][j] - mean;
            }
        }
        return out;
    }

    /**
     * Calculates the mean of an array of doubles.
     * @param entries	input array of doubles
     */
    private static double mean(double[] entries) {
        double out = 0;
        for(double d: entries) {
            out += d/entries.length;
        }
        return out;
    }
}