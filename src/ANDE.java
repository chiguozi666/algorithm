import myMathUtils.PCA;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class ANDE {
    List<List<Double>> population;
    double ub = 1;
    double lb = 0;
    int dim;
    int N;
    CulFitness culFitness;
    int FES = 0;
    int MaxFES = 20000;
    double F = 0.1;
    double CR = 0.3;
    HashMap<List<Double>,Double> fitnessMap;
    Random random = new Random();
    //无data版本
    public ANDE(int dim,int N,CulFitness culFitness){
        this.population = new ArrayList<>(N);
        this.culFitness = culFitness;
        fitnessMap = new HashMap<>();
        for (int i = 0; i < N; i++) {
            List list = new ArrayList(dim);
            for (int j = 0; j < dim; j++) {
                list.add(Math.random());
            }
            this.population.add(list);
            fitnessMap.put(list,culFitness(list));
        }
        this.dim = dim;
        this.N = N;

    }
    //有data版本
    public ANDE(List<List<Double>> preData,CulFitness culFitness){
        this.population = preData;
        this.N = this.population.size();
        this.dim = this.population.get(0).size();
        this.culFitness = culFitness;
    }

    public void findSolution(){
        while(FES<=MaxFES){
            //一个小生境的MAP，key是所属种类，Object是一个小生境里面的所有个体
            HashMap<Integer,List<List<Double>>> nicheMap = APCluster.getAPC(population);//
            HashMap<Integer,Double> nicheSeedMap = new HashMap<>();//记录每个小生境最好的仔
            for (Integer key:nicheMap.keySet()){
                List<List<Double>> niche = nicheMap.get(key);

                //利用DE生崽
                if(niche.size()<=4){//使用全局的DE生崽
                    int i = 0;
                    int length = niche.size();
                    while(niche.size()<=4){//小niches自动填充,这里和原文不一样，这里是变异单父亲
                        i = i %length;//循环构建子代,这里不除niche.size()是防止子代生子代
                        List<Double> father = niche.get(i);
                        List<Double> son = getDERandSon(father,fitnessMap.get(father));
                        niche.add(son);
                        i++;
                    }
                }else {

                }
                //使用CPA
                List<Double> lBest = niche.get(0);
                double flBest = fitnessMap.get(lBest);
                int CPAtimes = nicheMap.keySet().size();//CPA次数和小生境个数一样;
                while(CPAtimes>=0){
                    for (int j = 1; j < niche.size(); j++) {//找出小生境最好的仔
                        List<Double> item = niche.get(j);
                        double fItem = fitnessMap.get(item);
                        if(fItem<flBest){
                            flBest = fItem;
                            lBest = item;
                        }
                    }
                    List<List<Double>> interpolatedXs = new LinkedList<>();
                    double fSubFlBest = -0.2*Math.abs(flBest)-0.1;
                    for (int j = 0; j<niche.size()&&j<5; j++) {//仿照论文说的最多只产出5个点
                        List<Double> xi = niche.get(j);
                        double fxi = fitnessMap.get(xi);
                        double fiSubFlBest = fxi-flBest;
                        List<Double> xiPi = new ArrayList<>(dim);
                        for (int k = 0; k < dim; k++) {
                            xiPi.add(lBest.get(k)+fSubFlBest/fiSubFlBest*(xi.get(k)-lBest.get(k)));
                            //对应eq10
                        }
                        interpolatedXs.add(xiPi);//记录所有插值点
                    }
                    List<Double> potentialPoint = new ArrayList<>(dim);
                    for (int j = 0; j < dim; j++) {
                        double x = 0;
                        for (int k = 0; k < interpolatedXs.size(); k++) {
                            x+=interpolatedXs.get(k).get(j);
                        }
                        x/=interpolatedXs.size();
                        potentialPoint.add(x);
                    }//eq(12)
                    double fPotentialPoint = culFitness(potentialPoint);
                    if(fPotentialPoint<flBest){//比原来的好就加入啦
                        lBest = potentialPoint;
                        flBest = fPotentialPoint;
                        niche.add(lBest);//todo:要改成替换
                        fitnessMap.put(lBest,flBest);//存放映射关系
                    }
                    CPAtimes--;
                }
                nicheSeedMap.put(key,flBest);
            };

            //"TLLS算法大显神威"
            double sigma = Math.pow(10,-1.0-(10/dim+3)*FES/MaxFES);
            HashMap<Integer,Double> nicheProbability = getNicheProbability(nicheSeedMap);
            for (Integer key: nicheMap.keySet()) {
                if(Math.random()<nicheProbability.get(key)){
                    List<List<Double>> niche = nicheMap.get(key);//简简单单排个序
                    Collections.sort(niche, new Comparator<List<Double>>() {
                        @Override
                        public int compare(List<Double> o1, List<Double> o2) {
                            double r = fitnessMap.get(o1)-fitnessMap.get(o2);
                            if(r == 0) return 0;
                            return r>0?1:-1;
                        }
                    });
                    for(int i = 0;i < niche.size();i++){
                        if(random()<(double) i+1/niche.size()){//eq16
                            List<Double> father = niche.get(i);
                            List<Double> son1 = getGaussianSon(father,sigma);
                            List<Double> son2 = getGaussianSon(father,sigma);
                            Double son1Fitness = culFitness(son1);
                            Double son2Fitness = culFitness(son2);
                            Double fatherFitness = fitnessMap.get(father);
                            if(son1Fitness>son2Fitness){
                                son1Fitness = son2Fitness;
                                son1 = son2;
                            }
                            if(fatherFitness>son1Fitness){
                                niche.set(i,son1);//儿子替代父亲
                                fitnessMap.remove(father);
                                fitnessMap.put(son1,son1Fitness);
                            }
                        }
                    }
                }
            }
        }
    }
    public HashMap<Integer,Double> getNicheProbability(HashMap<Integer,Double> nicheSeedMap){
        Set<Integer> keySet = nicheSeedMap.keySet();
        Integer[] table = new Integer[keySet.size()];//0号位置装 小神经编号，1号转fitness;
        int count = 0;
        for(Integer i:keySet){
            table[count] = i;
            count++;
        }
        Arrays.sort(table, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                double r = nicheSeedMap.get(o1)-nicheSeedMap.get(o2);
                if(r==0)return 0;
                return r>0?1:-1;
            }
        });
        HashMap<Integer,Double> result = new HashMap<>();
        for (int i = 0; i < table.length; i++) {
            result.put(table[i],(double)i/keySet.size());
        }
        return result;
    }
    public List<Double> getDERandSon(List<Double> father,double fatherFitness){
        List<Double> son = null;//生成一个至少和父代一样的解
        int generateTimes = 10;//todo:这里可以加退火，防止生成子代太多
        double fitness = -1;
        while(son==null||generateTimes>0||fitness<=fatherFitness){//利用断路
            son = new ArrayList<>(dim);
            int r1 = (int)(Math.random()*N);
            int r2 = (int)(Math.random()*N);
            int r3 = (int)(Math.random()*N);
            while(r1!=r2&&r1!=r3&&r2!=r3){
                r2 = (int)(Math.random()*N);
                r3 = (int)(Math.random()*N);//小偷懒了这里
            }
            List<Double> Xr1 = population.get(r1);
            List<Double> Xr2 = population.get(r2);
            List<Double> Xr3 = population.get(r3);
            int jRand = (int)(Math.random()*dim);
            double CR = 0.5*(1+random());//0.5~1;//交叉算子
            for (int i = 0; i < dim; i++) {
                if(random()<=CR||jRand==i){
                    double temp = Xr1.get(i)+F*(Xr2.get(i)-Xr3.get(i));
                    temp = getSuitLimitX(temp);
                    son.add(temp);
                }else {
                    son.add(father.get(i));
                }
            }
            fitness = culFitness(son);
            generateTimes--;
        }
        this.fitnessMap.put(son,fitness);
        return son;
    }
    //只创造一个son ， 不算fitness的;
    public List<Double> getGaussianSon(List<Double> father,double sigma){
        List<Double> son = new ArrayList<>(dim);
        for(int i = 0;i<dim;i++){
            son.add(sigma * random.nextGaussian()+father.get(i));//eq(14a)
        }
        return son;
    }
    private double getSuitLimitX(double x){
        if (x > ub || x < lb) {//越界判断
            x =  lb + abs(x) % (ub - lb);
        }

        return x;
    }
    interface CulFitness{
        Double cul(List<Double> list);//通过坐标算出适应值
    }
    private Double culFitness(List<Double> list){
        FES++;//加次数
        return this.culFitness.cul(list);
    }
    private class Individual{
        List<Double> position;
        Double fitness;

        public Individual(List<Double> position) {
            this.position = position;
        }

        public Individual(List<Double> position, Double fitness) {
            this.position = position;
            this.fitness = fitness;
        }

        public List<Double> getPosition() {
            return position;
        }

        public void setPosition(List<Double> position) {
            this.position = position;
        }

        public Double getFitness() {
            return fitness;
        }

        public void setFitness(Double fitness) {
            this.fitness = fitness;
        }
    }
}
