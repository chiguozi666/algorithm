import java.util.*;

public class MopMetricUtil {
    List<List<Individual>> dataSet;
    //计算出收敛距离convergence distance;
    public double[] calCD(List<List<Individual>> dataSet){
        List<Individual> NDS = getNDS(dataSet);//这里可能要改一下
        double[] res = new double[dataSet.size()];
        for (int i = 0; i < res.length; i++) {
            double curCD = 0;
            List<Individual> curList = dataSet.get(i);
            for (int j = 0; j < curList.size(); j++) {
                Individual curIndividual = curList.get(j);
                Individual neighbourNDS = finMinDistance(curIndividual,NDS);
                curCD+=culEuclideanDistance(curIndividual,neighbourNDS);
            }
            curCD/=curList.size();
            res[i] = curCD;
        }
        return res;
    }
    //Spacing Z Z越小多样性越好。

    public List<Individual> getNDS(List<List<Individual>> dataSet){
        List<Individual> res = new LinkedList<>();
        for (List<Individual> iList:
             dataSet) {
            for (Individual i:
                 iList) {
                res.add(i);//可以优化一下这一步
            }
        }
        noDominateSort(res);
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).rank!=0){
                res.remove(i);
            }
        }
        return res;
    }
    //寻找出集合里面离individual最近的个体。
    public Individual finMinDistance(Individual individual,List<Individual> population){
        double minDis = Double.MAX_VALUE;
        int neighbour = 0;
        for (int i = 0; i < population.size(); i++) {
            double dis = culEuclideanDistance(individual,population.get(i));
            if(dis<minDis){
                neighbour = i;
                minDis = dis;
            }
        }
        return population.get(neighbour);
    }
    //计算fitness欧几里得距离
    private double culEuclideanDistance(Individual a,Individual b){
        int n = a.fitness.length;
        double res = 0;
        for (int i = 0; i < n; i++) {
            res += (a.fitness[i]-b.fitness[i])*(a.fitness[i]-b.fitness[i]);
        }
        return res;
    }
    public void noDominateSort(List<Individual> individuals){
        //public void setNoDominateRank(List<Individual> individuals){
        for (int i = 0; i < individuals.size(); i++) {
            individuals.get(i).setRank(0);
        }
        int len = individuals.size();
        int n[] = new int[len];//记录个体被多少个人支配
        Set<Integer> S[] = new HashSet[len];
        List<Integer> front = new LinkedList<>();
        int rank[] = new int[len];
        for (int i = 0; i < len; i++) {
            Set<Integer> set = new HashSet();
            S[i] = set;
            for (int j = 0; j < len; j++) {
                if(i==j)continue;//自己和自己比就跳过
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
                        individuals.get(q).setRank(rank[q]);//设置每个的等级
                        Q.add(q);
                    }
                }
            }
            i = i + 1;
            front = Q;
        }
        //启用这个代码能够直接获得一个hashmap。
//        HashMap<Integer,List<Individual>> res = new HashMap<>();
//        for (int j = 0; j < len; j++) {
//            if (res.get(rank[j])==null){
//                List<Individual> list = new ArrayList<>();;
//                list.add(individuals.get(j));
//                res.put(rank[j],list);
//            }else {
//                res.get(rank[j]).add(individuals.get(j));
//            }
//        }

        for (int j = 0; j < len; j++) {
            individuals.get(j).setRank(rank[j]);
        }
        individuals.sort(((o1, o2) -> o1.rank-o2.rank));//按rank逆序排序
        return;
    }
    class Individual{
        int rank;
        int totalRank;
        double distance;
        double[] fitness;//越小越好捏
        List<Double> position = new LinkedList<>();
        public Individual(double[] fitness) {
            this.fitness = fitness;
        }
        //will auto set fitness
        public Individual(List<Double> positon){
            this.position = positon;
//            this.fitness = culFitness(position);
        }
        // a > b return 1;
        // a >< b return 0;
        // a < b return -1;
        public int isDominate (Individual b){
            double[] bf = b.fitness;
            boolean flag = true;
            boolean notEqualFlag = false;
            //先判断a是否支配b
            for (int i = 0; i < fitness.length; i++) {
                if(fitness[i]>bf[i]){//a不支配b;
                    flag = false;
                    break;
                }
                if (fitness[i]!=bf[i]){
                    notEqualFlag = true;
                }
            }
            if (flag&&notEqualFlag){
                return 1;
            }
            flag = true;
            notEqualFlag = false;
            for (int i = 0; i < fitness.length; i++) {
                if(fitness[i]<bf[i]){//b不支配a;
                    flag = false;
                    break;
                }
                if (fitness[i]!=bf[i]){
                    notEqualFlag = true;
                }
            }
            if (flag&&notEqualFlag){
                return -1;
            }else return 0;
        }

        @Override
        public String toString() {
            return new StringBuffer("rank:"+rank+",distance:"+distance+",fitness:"+ Arrays.toString(fitness)).toString();
        }

        public int getRank() {
            return rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public double getDistance() {
            return distance;
        }

        public void setDistance(double distance) {
            this.distance = distance;
        }
    }
}
