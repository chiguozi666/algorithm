package Singal;

import java.util.*;

import static java.lang.Math.abs;
import static java.lang.Math.random;

//Sparrow Search Algorithm 麻雀优化算法
public class SSA {
    double ST = 0.6;//警戒值
    double pd = 0.7;//faxi0
    double sd = 0.2;
    double ub = 1.0;
    double lb = 0.0;
    int pop = 50;
    int PDCount;
    int SDCount;
    int iterTimes;
    int dim;
    int maxIter;
    List<Individual> population;
    Random random = new Random();
    Evaluator e;
    public void SSA(int pop,Evaluator evaluator,int dim){
        this.pop =pop;
        this.e = evaluator;
        this.dim = dim;
        iterTimes = 0;
        maxIter = 8000;
        PDCount = (int) (pop*pd);
        SDCount = (int) (pop*ST);
        Init();
        Sort();
        while(iterTimes<maxIter){
            UpdateProducer();
            UpdateScrounger();
            iterTimes++;
        }
    }
    public void Init(){
        population = new ArrayList<>(pop);
        for (int i = 0; i < pop; i++) {
            List<Double> position = new ArrayList<>(dim);
            for (int j = 0; j < dim; j++) {
                double x = lb + (ub-lb)*random();
                x = getSuitLimitX(x);
                position.set(j,x);
            }
            Individual individual = new Individual(position);
            double Fitness = culFitness(individual);
            individual.setFitness(Fitness);
            population.add(individual);
        }
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
        }
    }
    //
    //更新发现者(发现危险的麻雀)
    public void UpdateScrounger(){

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
    private double[][] getAPlusDotL(){

        double A[] = new double[dim];
        for (int i = 0; i < dim; i++) {//building random A 1 x dim;
            A[i] = random()<0.5?1:-1;
        }
        double AAT = 0;// A * A^T = 1 x dim dot dim * 1 = 1 x 1;
        for (int i = 0; i < dim; i++) {
            AAT += A[i]*A[i];
        }
        double AATinverse = 1.0/AAT;//A^-1

        double[] APlus = new double[dim];
        for (int i = 0; i < dim; i++) {
            APlus[i] = A[i]*AATinverse;//A+ = AT * (AAT)-1; A+ is dim*1
        }

        double res[][] = new double[dim][dim];//A+ dot L is dim * dim
        for (int i = 0; i < dim; i++) {
            for (int j = 0; j < dim; j++) {
                res[i][j] = APlus[i];
            }
        }

        return 0;
    }
}
