package Singal;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.random;

//Sparrow Search Algorithm 麻雀优化算法
public class SSA {
    double ST = 0.8;//警戒值
    double pd = 0.2;//faxi0
    double sd = 0.1;
    double ub = 500.0;
    double lb = -500.0;
    int pop = 50;
    double epsilon = 1e-10;
    int PDCount;
    int SDCount;
    int iterTimes;
    int dim;
    int maxIter;
    List<Individual> population;
    List<Individual> newPopulation;
    Random random = new Random();
    Individual best;
    Individual worst;
    Individual globalBest;
    Evaluator e;

    public static void main(String[] args) {
        new SSA(100,new Evaluator(){
            @Override
            public double culFitness(Individual individual) {
                return FUtil.F8(individual.position);
            }
        },30);
    }
    public SSA(int pop,Evaluator evaluator,int dim){
        this.pop =pop;
        this.e = evaluator;
        this.dim = dim;
        iterTimes = 0;
        maxIter = 1000;
        PDCount = (int) (pop*pd);
        SDCount = (int) (pop*ST);
        Init();
        Sort();
        globalBest = population.get(0).clone();
        globalBest.setFitness(population.get(0).fitness);
        while(iterTimes<maxIter){
            setCurrentWorstAndBest();
            UpdateProducer();
            setCurrentWorstAndBest();
            UpdateScrounger();
            setCurrentWorstAndBest();
            UpdateDetector();
            globalBest = getGlobalBest();
            System.out.println(globalBest.fitness);
            iterTimes++;
        }
    }
    private void setCurrentWorstAndBest(){
        int bestIndex = 0;
        int worstIndex = 0;
        //深拷贝一下
        for (int i = 0; i < pop; i++) {
            if (population.get(i).fitness<population.get(bestIndex).fitness){
                bestIndex = i;
            }
            if (population.get(i).fitness>population.get(worstIndex).fitness){
                worstIndex = i;
            }
        }
        best = population.get(bestIndex).clone();
        worst =population.get(worstIndex).clone();
    }
    private Individual getGlobalBest(){
        int bestIndex = 0;
        for (int i = 0; i < pop; i++) {
            if (population.get(i).fitness<population.get(bestIndex).fitness){
                bestIndex = i;
            }
        }
        if(globalBest==null||globalBest.fitness>population.get(bestIndex).fitness){
            globalBest = population.get(bestIndex).clone();
        }
        return globalBest;
    }
    public void Init(){
        population = new ArrayList<>(pop);
        for (int i = 0; i < pop; i++) {
            List<Double> position = new ArrayList<>(dim);
            for (int j = 0; j < dim; j++) {
                double x = lb + (ub-lb)*random();
                x = getSuitLimitX(x);
                position.add(new Double(x));
            }
            Individual individual = new Individual(position);
            double Fitness = culFitness(individual);
            individual.setFitness(Fitness);
            population.add(individual);
        }
        newPopulation = new ArrayList<>(pop);
    }
    public void Sort(){
        Collections.sort(population);
    }
    //更新生产者
    public void UpdateProducer(){//这时的适应度还没更新
        for (int i = 0; i < PDCount; i++) {
            double alpha = random();
            Individual individual = population.get(i);
            List<Double> position = individual.getPosition();
            double r2 = random();
            if(r2<ST){
                for (int j = 0; j < dim; j++) {
                    double x = position.get(j);
                    x = x*Math.exp((double) -i/(alpha*maxIter));
                    x = getSuitLimitX(x);
                    position.set(j,x);
                }
            }else {
                double Q = random.nextGaussian();//看看在里面还是外面
                for (int j = 0; j < dim; j++) {
                    double x = position.get(j);
                    x = x + Q;
                    x = getSuitLimitX(x);
                    position.set(j,x);
                }
            }
            updateFitness(individual);
        }
    }
    //
    //更新发现者(发现危险的麻雀)
    public void UpdateScrounger(){
        for (int i = PDCount; i < pop; i++) {
            Individual individual = population.get(i);
            List<Double> position = individual.getPosition();
            List<Double> worstPosition = worst.getPosition();
            List<Double> p = best.getPosition();
            double Q = random.nextGaussian();
            if(i > pop/2){
                for (int j = 0; j < dim; j++) {
                    double x = position.get(j);
                    x = Q*Math.exp((worstPosition.get(j)-position.get(j))/(i*i));
                    x = getSuitLimitX(x);
                    position.set(j,x);
                }
            }else {
                double aPlus[] = getAPlus();
                /**
                 * | xi - xp|* APlus is 1*dim * dim*1
                 * the result is 1*1 so the x^{t+1} just |x^t_i,j - xPj| * APlus j
                 */
                for (int j = 0; j < dim; j++) {
                    double x = position.get(j);
                    double pX = p.get(j);
                    x = pX + Math.abs(x-pX)*aPlus[j];
                    x = getSuitLimitX(x);
                    position.set(j,x);
                }
            }
            updateFitness(individual);
        }
    }
    public void UpdateDetector(){
        List<Double> gPosition = best.getPosition();
        List<Double> wPosition = worst.getPosition();
        double beta = random.nextGaussian();
        Individual globalBest = getGlobalBest();
        List<Double> globalBestPosition = globalBest.getPosition();
        boolean visit[] = new boolean[pop];
        for (int i = 0; i < SDCount; i++) {
            int k = (int)(pop*random());//随机抽出一个
            while(visit[k]) k = (int)(pop*random());
            visit[k] = true;
            Individual individual = population.get(k);
            List<Double> position = individual.getPosition();
            if(individual.getFitness()>globalBest.getFitness()){
                for (int j = 0; j < dim; j++) {
                    double x = individual.getPosition().get(j);
                    double xBest = globalBestPosition.get(j);
                    x = xBest + beta*Math.abs(x-xBest);
                    x = getSuitLimitX(x);
                    position.set(j,x);
                }
            }else if(individual.getFitness()==globalBest.getFitness()){
                double fi = individual.getFitness();
                double fw = worst.getFitness();
                for (int j = 0; j < dim; j++) {
                    double x = individual.getPosition().get(j);
                    double K = random()*2 - 1;//k [-1,1]
                    x = x + K * (Math.abs(x-wPosition.get(j))/(fi-fw+epsilon));
                    x = getSuitLimitX(x);
                    position.set(j,x);
                }
            }
            updateFitness(individual);
        }

    }
    private void updateFitness(Individual individual){
        double fitness = culFitness(individual);
        individual.setFitness(fitness);
    }

    public double culFitness(Individual individual){
        return e.culFitness(individual);
    }
    //
    interface Evaluator{
        public double culFitness(Individual individual);
    }
    private double getSuitLimitX(double x){
        if (x > ub || x < lb) {//越界判断
            x =  lb + abs(x) % (ub - lb);
        }
        return x;
    }
    //A+ * L;
    private double[] getAPlus(){
        //AT is controlling +- ,and (AAT)^-1 just 1/dim (= _=') silly boy
        double APlus[] = new double[dim];
        for (int i = 0; i < dim; i++) {//building random A 1 x dim;
            APlus[i] = random()<0.5?1/dim:-1/dim;
        }
        return APlus;
    }
}
