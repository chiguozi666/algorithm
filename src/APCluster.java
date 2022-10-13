import myMathUtils.PCA;

import java.util.*;

public class APCluster {
    double a[][];
    double lastA[][];
    double r[][];
    double lastR[][];
    double s[][];
    double lambda = 0.9;
    double[][] data;
    //用于获得分类
    public static HashMap<Integer,List<List<Double>>> getAPC(List<List<Double>> data){
        double[][] pca= PCA.getPCA(3,data);
        APCluster apCluster = new APCluster(pca);
        int []result = apCluster.cul();
        HashMap<Integer,List<List<Double>>> hashMap = new HashMap<Integer,List<List<Double>>>();
        for (int i = 0; i < data.size(); i++) {
            //hashMap.put(data.get(i),result[i]);
            if(hashMap.get(result[i])!=null){
                hashMap.get(result[i]).add(data.get(i));
            }else {
                List<List<Double>> item = new LinkedList<>();
                item.add(data.get(i));
                hashMap.put(result[i],item);
            }
            //System.out.println(total.get(i)+"  "+result[i]);
        }

        return hashMap;
    }
    public static void main(String[] args) {
        List<List<Double>> total = new ArrayList<>();
        for (int i = 0; i <20; i++) {
            List<Double> data = new ArrayList<>();
            for (int j = 0; j < 100; j++) {
                data.add(Math.random());
            }
            total.add(data);
        }
        HashMap hashMap2 = getAPC(total);
        double[][] pca= PCA.getPCA(3,total);
        APCluster apCluster = new APCluster(pca);
        int []result = apCluster.cul();
        HashMap<List,Integer> hashMap = new HashMap<>();
        for (int i = 0; i < total.size(); i++) {
            hashMap.put(total.get(i),result[i]);
            //System.out.println(total.get(i)+"  "+result[i]);
        }
        total.sort(new Comparator<List<Double>>() {
            @Override
            public int compare(List<Double> o1, List<Double> o2) {
                if(o1.get(0)>o2.get(0))return 1;
                else if(o1.get(0)<o2.get(0))return -1;
                else return 0;
            }
        });
        for (int i = 0; i < total.size(); i++) {
            System.out.println(total.get(i)+"  "+hashMap.get(total.get(i)));;
        }
    }
    //每个list记得等长
    public APCluster(double[][] data) {
        this.data = data;
        int n = data.length;
        a = new double[n][n];
        lastA = new double[n][n];
        r = new double[n][n];
        lastR = new double[n][n];
        s = new double[n][n];
    }
    private int[] cul(){
        int m = 80;
        initS();
        while(m>0){
            updataR();
            updataA();
            updateAll();
            m--;
        }
        int []result = new int[data.length];
        for (int i = 0; i < data.length; i++) {
            double max = r[i][0]+a[i][0];
            int maxj = 0;
            for (int j = 0; j < data.length; j++) {
                if(max<r[i][j]+a[i][j]){
                    max = r[i][j]+a[i][j];
                    maxj = j;
                }
            }
            result[i] = maxj;
        }
        return result;
    }
    private void initS(){
        double min = 0;
        int queueSize = (data.length-1)*(data.length-1);
        PriorityQueue<Double> priorityQueue = new PriorityQueue<>(queueSize);
        for (int i = 0;i<data.length;i++){
            for (int j = 0;j<data.length;j++){
                if(i==j){
                    s[i][j] = 0;
                }else {
                    s[i][j] = -getDistance(data[i],data[j]);
                    if(min>s[i][j]) min = s[i][j];
                    priorityQueue.add(s[i][j]);
                }
            }
        }
        double sii = (double)priorityQueue.toArray()[queueSize/2];
        for (int i = 0; i < data.length; i++) {
            s[i][i] = sii;
        }
    }
    private void updataR(){
        for (int i = 0; i < data.length; i++) {
            for (int k = 0; k < data.length; k++) {
                double max = -Double.MAX_VALUE;
                if(i!=k){
                    for(int j = 0; j < data.length; j++){
                        if(j!=k){
                            double cur = a[i][j]+r[i][j];
                            if(max<cur)max = cur;
                        }
                    }
                }else {
                    for(int j = 0; j < data.length; j++){
                        if(j!=k){
                            double cur = s[i][j];
                            if(max<cur)max = cur;
                        }
                    }
                }
                lastR[i][k] = r[i][k];
                r[i][k] = s[i][k] - max;
            }
        }
    }
    private void updataA(){
        for (int i = 0; i < data.length; i++) {
            for (int k = 0; k < data.length; k++) {
                lastA[i][k] = a[i][k];
                if(i!=k){
                    double sum = 0;
                    for (int j = 0; j < data.length; j++) {
                        if(j!=i&&j!=k){
                            sum+=Math.max(r[j][k],0);
                        }
                    }
                    a[i][k] = Math.min(0,r[k][k]+sum);
                }else {
                    double sum = 0;
                    for (int j = 0; j < data.length; j++) {
                        if(j!=k){
                            sum+=Math.max(r[j][k],0);
                        }
                    }
                    a[i][k] = sum;
                }
            }
        }
    }
    private void updateAll(){
        for (int i = 0; i < data.length; i++) {
            for (int k = 0; k < data.length; k++) {
                r[i][k] = lambda*lastR[i][k] + (1-lambda) *r[i][k];
                a[i][k] = lambda*lastA[i][k] + (1-lambda) *a[i][k];
            }
        }
    }
    private double getDistance(double[] a,double[] b){
        double result = 0;
        for (int i = 0; i < a.length; i++) {
            result+=Math.pow(a[i]-b[i],2);
        }
        return result;
    }
}
