//package Singal;
//
//import java.util.*;
//
//import static java.lang.Math.abs;
//import static java.lang.Math.random;
//
//public class ANDE {
//    //List<List<Double>> population;
//    List<Individual> population;
//    double ub = 1;
//    double lb = 0;
//    int dim;
//    int N;
//    CulFitness culFitness;
//    int FES = 0;
//    int MaxFES = 10000;
//    //double F = 0.9;
//    double F = 0.9;
//    double CR = 0.1;
//    //double CR = 0.2;
//    //HashMap<List<Double>,Double> fitnessMap;
//    Random random = new Random();
//
//    public static void main(String[] args) {
//        ANDE ande = new ANDE(100, 10, new CulFitness() {
//            @Override
//            public Double cul(List<Double> list) {
//                return FUtil.Sphere(list);
//            }
//        });
//        ande.findSolution();
//    }
//    //无data版本
//    public ANDE(int dim,int N,CulFitness culFitness){
//        this.population = new ArrayList<>(N);
//        this.culFitness = culFitness;
//        for (int i = 0; i < N; i++) {
//            List list = new ArrayList(dim);
//            for (int j = 0; j < dim; j++) {
//                list.add(lb+(ub-lb)*Math.random());
//            }
//            Individual individual = new Individual(list,culFitness(list));
//            this.population.add(individual);
//        }
//        this.dim = dim;
//        this.N = N;
//    }
//    //有data版本
//    public ANDE(List<Individual> preData, CulFitness culFitness){
//        this.population = preData;
//        this.N = this.population.size();
//        this.dim = this.population.get(0).getPosition().size();
//        this.culFitness = culFitness;
//    }
//    //弱鸡版APC
//    public HashMap<Integer,List<Individual>> getAPC(List<Individual> population){
//        Collections.sort(population);
//        HashMap<Integer,List<Individual>> hashMap = new HashMap<>();
//        int nichesSize = 5;
//        List<Individual> niches[] = new List[nichesSize];
//        for (int i = 0; i < nichesSize; i++) {
//            niches[i] = new LinkedList<>();
//        }
//        for (int i = 0; i < population.size(); i++) {
//            int n = i%nichesSize;
//            niches[n].add(population.get(i));
//        }
//        for (int i = 0; i < 5; i++) {
//            hashMap.put(i,niches[i]);
//        }
//        return hashMap;
//    }
//
//    public void findSolution(){
//        //Collections.sort(population);
//        while(FES<=MaxFES){
//            Individual curBest = population.get(0);
//            //一个小生境的MAP，key是所属种类，Object是一个小生境里面的所有个体
//            //HashMap<Integer,List<Individual>> nicheMap = APCluster.getAPC(population);//
//            HashMap<Integer,List<Individual>> nicheMap = getAPC(population);
//            HashMap<Integer,Double> nicheSeedMap = new HashMap<>();//记录每个小生境最好的仔
//            for (Integer key:nicheMap.keySet()){
//                List<Individual> niche = nicheMap.get(key);
//
//                //利用DE生崽
//                if(niche.size()<=4){//使用全局的DE生崽
//                    int i = 0;
//                    int length = niche.size();
//                    while(niche.size()<=4){//小niches自动填充,这里和原文不一样，这里是变异单父亲
//                        i = i %length;//循环构建子代,这里不除niche.size()是防止子代生子代
//                        Individual father = niche.get(i);
//                        Individual son = getDERandSon(father,curBest);
//                        niche.add(son);
//                        i++;
//                    }
//                }else {
//                    for (int i = 0; i < niche.size(); i++) {
//                            Individual father = niche.get(i);
//                            Individual son = getDERandSon(father,curBest);
//                            if(son.getFitness()<father.getFitness()){
//                                niche.set(i,son);//差分置换
//                                //niche.add(son);
//                            }
//                    }
//                }
//                //使用CPA  CPA理解不能
//                Individual lBest = niche.get(0);
//                int CPAtimes = nicheMap.keySet().size();//CPA次数和小生境个数一样;
//                while(CPAtimes>=0){
//                    if(niche.size()<5){
//                        CPAtimes--;
//                        continue;
//                    }
//                    for (int j = 1; j < niche.size(); j++) {//找出小生境最好的仔
//                        Individual item = niche.get(j);
//                        if(item.getFitness()<lBest.getFitness()){
//                            lBest = item;
//                        }
//                    }
//                    List<List<Double>> interpolatedXs = new LinkedList<>();
//                    double fSubFlBest = -0.2*Math.abs(lBest.getFitness())-0.1;
//                    for (int j = 0; j<niche.size()&&j<5; j++) {//仿照论文说的最多只产出5个点
//                        List<Double> xi = niche.get(j).getPosition();
//                        double fiSubFlBest = niche.get(j).getFitness()-lBest.getFitness()+Double.MIN_VALUE;
//                        List<Double> xiPi = new ArrayList<>(dim);
//                        for (int k = 0; k < dim; k++) {
//                            double temp = lBest.getPosition().get(k)+fSubFlBest/fiSubFlBest*(xi.get(k)-lBest.getPosition().get(k));
//                            xiPi.add(getSuitLimitX(temp));
//                            //对应eq10
//                        }
//                        interpolatedXs.add(xiPi);//记录所有插值点
//                    }
//                    List<Double> potentialPoint = new ArrayList<>(dim);
//                    for (int j = 0; j < dim; j++) {
//                        double x = 0;
//                        for (int k = 0; k < interpolatedXs.size(); k++) {
//                            x+=interpolatedXs.get(k).get(j);
//                        }
//                        x/=interpolatedXs.size();
//                        potentialPoint.add(x);
//                    }//eq(12)
//                    double fPotentialPoint = culFitness(potentialPoint);
//                    if(fPotentialPoint<lBest.getFitness()){//比原来的好就替换随机一个
//                        lBest = new Individual(potentialPoint,fPotentialPoint);
//                        int i = (int)(Math.random()*niche.size());//todo:排序后淘汰最垃圾的
//                        niche.set(i,lBest);
//                    }
//                    CPAtimes--;
//                }
//                nicheSeedMap.put(key,lBest.fitness);
//            };
//
//            //"TLLS算法大显神威"
//            //double sigma = Math.pow(10,-1.0-(10.0/dim+3.0)*FES/MaxFES);
//            //double sigma = Math.pow(10,-1.0-(10.0/dim+3.0)*FES/MaxFES);
//            double sigma = 1;
//            HashMap<Integer,Double> nicheProbability = getNicheProbability(nicheSeedMap);
//            for (Integer key: nicheMap.keySet()) {
//                if(Math.random()<nicheProbability.get(key)){
//                    List<Individual> niche = nicheMap.get(key);//简简单单排个序
//                    Collections.sort(niche);
//                    for(int i = 0;i < niche.size();i++){
//                        if(random()<(double) (niche.size()-i)/niche.size()){//eq16
//                            Individual father = niche.get(i);
//                            Individual son1 = getGaussianSon(father,sigma);
//                            Individual son2 = getGaussianSon(father,sigma);
//                            if(son1.getFitness()>son2.getFitness()){
//                                son1 = son2;
//                            }
//                            if(son1.getFitness()<father.getFitness()){
//                                niche.set(i,son1);//儿子替代父亲
//                            }
//                        }
//                    }
//                }
//            }
//            List<Individual> newPopulation = new ArrayList<>();
//            for(Integer key:nicheMap.keySet()){
//                List<Individual> niche = nicheMap.get(key);
//                newPopulation.addAll(niche);
//            }
//            Collections.sort(newPopulation);//留下适应度高的babby
//            //System.out.println(FES+"current best:"+newPopulation.get(0).getFitness()+"  "+newPopulation.get(0).getPosition());
//            System.out.println(FES+"current best:"+newPopulation.get(0).getFitness());
//            population = newPopulation.subList(0,N);//切割
//        }
//    }
//    public HashMap<Integer,Double> getNicheProbability(HashMap<Integer,Double> nicheSeedMap){
//        Set<Integer> keySet = nicheSeedMap.keySet();
//        Integer[] table = new Integer[keySet.size()];
//        int count = 0;
//        for(Integer i:keySet){
//            table[count] = i;
//            count++;
//        }
//        Arrays.sort(table, new Comparator<Integer>() {
//            @Override
//            public int compare(Integer o1, Integer o2) {
//                double r = nicheSeedMap.get(o1)-nicheSeedMap.get(o2);
//                if(r==0)return 0;
//                return r>0?1:-1;
//            }
//        });
//        HashMap<Integer,Double> result = new HashMap<>();
//        for (int i = 0; i < table.length; i++) {
//            result.put(table[i],(double)(keySet.size()-i)/keySet.size());
//        }
//        return result;
//    }
//    public Individual getDERandSon(Individual father, Individual best){
//        List<Double> son = null;//生成一个至少和父代一样的解
//        int generateTimes = 1;//todo:这里可以加退火
//        double fitness = -1;
//        while(son==null||(generateTimes>0&&fitness<=father.getFitness())){//利用断路
//            son = new ArrayList<>(dim);
//            if(random()<1){//DE/rand/1 mutation
//                int r1 = (int)(Math.random()*N);
//                int r2 = (int)(Math.random()*N);
//                int r3 = (int)(Math.random()*N);
//                while(r1!=r2&&r1!=r3&&r2!=r3){
//                    r2 = (int)(Math.random()*N);
//                    r3 = (int)(Math.random()*N);//小偷懒了这里
//                }
//                List<Double> Xr1 = population.get(r1).getPosition();
//                List<Double> Xr2 = population.get(r2).getPosition();
//                List<Double> Xr3 = population.get(r3).getPosition();
//                int jRand = (int)(Math.random()*dim);
//                //double CR = 0.5*(1+random());//0.5~1;//交叉算子
//                for (int i = 0; i < dim; i++) {
//                    if(random()<=CR||jRand==i){
//                        double temp = Xr1.get(i)+F*(Xr2.get(i)-Xr3.get(i));
//                        temp = getSuitLimitX(temp);
//                        son.add(temp);
//                    }else {
//                        son.add(father.getPosition().get(i));
//                    }
//                }
//            }else {//The DE/current-to-best/1
//                int r1 = (int)(Math.random()*N);
//                int r2 = (int)(Math.random()*N);
//                while(r1!=r2){
//                    r2 = (int)(Math.random()*N);
//                }
//                List<Double> Xr1 = population.get(r1).getPosition();
//                List<Double> Xr2 = population.get(r2).getPosition();
//                List<Double> Xi = father.getPosition();
//                List<Double> Xbest = best.getPosition();
//                int jRand = (int)(Math.random()*dim);
//                double CR = 0.5*(1+random());//0.5~1;//交叉算子
//                for (int i = 0; i < dim; i++) {
//                    if(random()<=CR||jRand==i){
//                        double temp = Xi.get(i)+F*(Xbest.get(i)-Xi.get(i))+F*(Xr1.get(i)-Xr2.get(i));
//                        temp = getSuitLimitX(temp);
//                        son.add(temp);
//                    }else {
//                        son.add(father.getPosition().get(i));
//                    }
//                }
//            }
//
//            fitness = culFitness(son);
//            generateTimes--;
//        }
//        return new Individual(son,fitness);
//    }
//    //只创造一个son ， 不算fitness的;
//    public Individual getGaussianSon(Individual father, double sigma){
//        List<Double> son = new ArrayList<>(dim);
//        List<Double> fat = father.getPosition();
//        for(int i = 0;i<dim;i++){
////            if(random()<0.2){//todo:原本是没有random的
////                double gaussian = random.nextGaussian();
////                double temp = sigma * gaussian*fat.get(i)+fat.get(i);
////                son.add(getSuitLimitX(temp));//eq(14a)
////            }else {
////                son.add(fat.get(i));
////            }
//            double gaussian = random.nextGaussian();
//            double temp = sigma * gaussian*fat.get(i)+fat.get(i);
//            son.add(getSuitLimitX(temp));//eq(14a)
//        }
//        return new Individual(son,culFitness(son));
//    }
//    private double getSuitLimitX(double x){
//        if (x > ub || x < lb) {//越界判断
//            x =  lb + abs(x) % (ub - lb);
//        }
//
//        return x;
//    }
//    interface CulFitness{
//        Double cul(List<Double> list);//通过坐标算出适应值
//    }
//    private Double culFitness(List<Double> list){
//        FES++;//加次数
//        return this.culFitness.cul(list);
//    }
//}
