import java.util.*;
import java.util.function.IntFunction;

import static java.lang.Math.random;
import static java.lang.Math.round;

public class IParetoProblem {
    public static void main(String[] args) {
        List<Individual> individuals = new LinkedList<>();
        IParetoProblem iParetoProblem = new IParetoProblem();
        individuals.add(iParetoProblem.new Individual(new double[]{1.2,4.4,61.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{12.2,4.4,63.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{41.2,48.4,6.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{13.2,49.4,6.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{1.2,48.4,6.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{12.2,4.4,62.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{1.2,4.4,6.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{11.2,4.4,16.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{1.2,4.47,6.5}));
        individuals.add(iParetoProblem.new Individual(new double[]{13.2,4.48,6.65}));
        individuals.add(iParetoProblem.new Individual(new double[]{41.2,4.74,6.55}));
        HashMap hashMap = iParetoProblem.noDominateSort(individuals);
        return;
    }
    public HashMap<Integer,List<Individual>> noDominateSort(List<Individual> individuals){
        int len = individuals.size();
        int n[] = new int[len];//记录个体被多少个人支配
        Set<Integer> S[] = new HashSet[len];
        List<Integer> front = new LinkedList<>();
        int rank[] = new int[len];
        for (int i = 0; i < len; i++) {
            Set<Integer> set = new HashSet();
            S[i] = set;
            for (int j = 0; j < len; j++) {
                int isDominate = individuals.get(i).isDominate(individuals.get(j));
                if(isDominate==1){//i支配j
                    set.add(j);
                }
                else if (isDominate==-1){//j支配i
                    n[i] = n[i]+1;
                }
            }
            if(n[i]==0){
                rank[i] = 1;
                front.add(i);
            }
        }
        int i = 1;
        while(front.size()!=0){
            List<Integer> Q = new LinkedList<>();
            for (int p:
                 front) {
                for (int q:
                     S[p]) {
                    n[q] = n[q] - 1;
                    if(n[q] == 0){
                        rank[q] = i+1;
                        Q.add(q);
                    }
                }
            }
            i = i + 1;
            front = Q;
        }
        HashMap<Integer,List<Individual>> res = new HashMap<>();
        for (int j = 0; j < len; j++) {
            if (res.get(rank[j])==null){
                List<Individual> list = new ArrayList<>();
                list.add(individuals.get(j));
                res.put(rank[j],list);
            }else {
                res.get(rank[j]).add(individuals.get(j));
            }
        }
        return res;
    }

    private class Individual{
        double[] fitness;//越小越好捏

        public Individual(double[] fitness) {
            this.fitness = fitness;
        }
        // a > b return 1;
        // a >< b return 0;
        // a < b return -1;
        public int isDominate (Individual b){
            double[] bf = b.fitness;
            boolean flag = true;
            //先判断a是否支配b
            for (int i = 0; i < fitness.length; i++) {
                if(fitness[i]>bf[i]){//a不支配b;
                    flag = false;
                    break;
                }
            }
            if (flag){
                return 1;
            }
            flag = true;
            for (int i = 0; i < fitness.length; i++) {
                if(fitness[i]<bf[i]){//b不支配a;
                    flag = false;
                    break;
                }
            }
            if (flag){
                return -1;
            }else return 0;
        }
    }

}
