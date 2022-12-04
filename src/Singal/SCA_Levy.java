package Singal;

import myMathUtils.CulDisturbute;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Math.random;
import static myMathUtils.CulDisturbute.gamma;

//正弦余弦算法
public class SCA_Levy {
    int dim;
    int populationSize;
    int gen = 0;
    int maxGen = 20000;
    double ub = 500;
    double lb = -500;
    double a = 2.0;
    private List<Individual> pop;
    private Individual totalBest;
    PositionFactory positionFactory;
    Individual.FitnessCulTor fitnessCulTor;
    Random random;
    public SCA_Levy(int dim, int populationSize, Individual.FitnessCulTor fitnessCulTor){
        this.random = new Random();
        this.populationSize = populationSize;
        this.fitnessCulTor = fitnessCulTor;
        this.dim = dim;
        this.positionFactory = new PositionFactory(dim,ub,lb);
        init();
        while(gen<maxGen) {
            if(gen==10000){
                System.out.println(1);
            }
            totalBest = findTotalBest().clone();
            update();
            System.out.println(findTotalBest().fitness);
            gen++;
        }
    }

    public static void main(String[] args) {
        new SCA_Levy(30, 30, new Individual.FitnessCulTor() {
            @Override
            public double culFitness(Individual i) {
                return FUtil.F8(i.getPosition().getData());
            }
        });
    }
    public void LevyUpdate(){
        for (int i = 0; i < pop.size(); i++) {
            for (int j = 0; j < dim; j++) {
                double lambda = 2.0 * random();
                double sigma_u =
                double u = random.nextGaussian();
            }
        }
    }
    public void update(){
        double r1 = 2*Math.exp(-30.0*Math.pow((double)gen/(double) maxGen,2));//这里是非线性改变的
        Position bestPosition =  totalBest.getPosition();
        for (int i = 0; i < pop.size(); i++) {
            Individual individual = pop.get(i);
            Position position = individual.getPosition();
            for (int j = 0; j < dim; j++) {
                double p = bestPosition.get(j);
                double x = position.get(j);
                double r2 = random()*2*Math.PI;
                double r3 = random()*2;
                double r4 = random();
                if (r4<0.5) {
                    x = x + r1 * Math.sin(r2) * Math.abs(r3 * p - x);//差值越大变化越大；
                }
                else {
                    x = x + r1 * Math.cos(r2) * Math.abs(r3 * p - x);//差值越大变化越大；
                }
                position.set(j,x);
            }
            updateFitness(individual);
        }
    }
    public void init(){
        this.pop = new ArrayList<>(dim);
        for (int i = 0; i < populationSize; i++) {
            Individual individual = new Individual(positionFactory.initRandomPosition());
            updateFitness(individual);
            this.pop.add(individual);
        }
    }
    public void updateFitness(Individual individual){
        double f = this.fitnessCulTor.culFitness(individual);
        individual.fitness = f;
    }
    public Individual findTotalBest(){
        Individual curBest = pop.get(0);
        for (int i = 1; i < pop.size(); i++) {
            Individual individual = pop.get(i);
            if(curBest.fitness > individual.fitness){
                curBest = individual;
            }
        }
        if(totalBest==null)totalBest = curBest;
        else{
            if(totalBest.fitness < curBest.fitness){
                curBest = totalBest;
            }
        }
        return curBest;
    }
}
