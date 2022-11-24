package Singal;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class OSAlg {
    int dim = 30;
    int populationSize = 20;
    int gen = 0;
    int maxGen = 20000;
    double ub = 500;
    double lb = -500;
    double miu = 1.05;
    double sigma = random();//一个随机种子

    double Ici[] = new double[populationSize];
    public static void main(String[] args) {
        new OSAlg();
    }
    private List<Individual> pop;
    public OSAlg(){
        init(populationSize);
        double gobalMin = 0;
        while(gen<maxGen){
            update();
            double min = Double.MAX_VALUE;
            for (int i = 0; i < pop.size(); i++) {
                Individual cur = pop.get(i);
                if (cur.fitness<min){
                    min = cur.fitness;
                }
            }
            if (gobalMin>min){
                gobalMin = min;
            }
            if (gen%100==0){
                System.out.println(123);
            }
            System.out.println(gen+"  "+gobalMin);
            gen++;
        }
    }
    private double getSuitLimitX(double x){
        if (x > ub || x < lb) {//越界判断
            x =  lb + abs(x) % (ub - lb);
        }
        return x;
    }
    private void init(int populationSize){
        List<Individual> result = new ArrayList<>(populationSize);
        for (int i = 0; i < populationSize; i++) {
            List<Double> position = new ArrayList<>(dim);
            for (int j = 0; j < dim; j++) {
                position.add(lb+random()*(ub-lb));
            }
            Individual individual = new Individual(position);
            result.add(individual);
        }
        this.pop=result;
    }
    private void update(){
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        int minIndex = 0;
        double R[] = new double[populationSize];
        double Ii[] = new double[populationSize];
        for (int i = 0; i < pop.size(); i++) {
            Individual cur = pop.get(i);
            if (cur.fitness>max){
                max = cur.fitness;
            }
            if (cur.fitness<min){
                min = cur.fitness;
                minIndex = i;
            }
        }
        sigma = random();
        sigma = miu*sigma*(1-sigma);
        List<Double> V = new ArrayList<>(dim);
        List<Double> originV = pop.get(minIndex).position;
        for (int i = 0; i < dim; i++) {
            V.add(originV.get(i));
        }
        for (int i = 0; i < populationSize; i++) {
            if(i==minIndex)continue;
            List<Double> Oi = pop.get(i).position;
            double dis = 0;
            for (int j = 0; j < dim; j++) {
                dis += Math.pow(V.get(j)-Oi.get(j),2);
            }
            R[i] = dis;
            Ii[i] = (max - pop.get(i).fitness)/(max - min);//越接近最优值越大
            Ici[i] = Ii[i]/R[i]+sigma;
        }
        double beta = (1.0-gen/maxGen)*1.9;//这个1.9应该可以改
        double alpha = random()/2.0;
        for (int i = 0; i < populationSize; i++) {
            List<Double> newOne = new ArrayList<>(dim);
            List<Double> last = pop.get(i).position;
            int x = 1;
            if(random()<0.5)x = -1;
            for (int j = 0; j < dim; j++) {
                double cur;
                cur = last.get(j) + x * beta * Ici[i] * Math.abs(alpha * V.get(j) - last.get(j));
                cur = getSuitLimitX(cur);
                newOne.add(cur);
            }
            pop.set(i,new Individual(newOne));
        }

    }
    private double culFitness(List<Double> position){
        return FUtil.F8(position);
    }
    private class Individual implements Comparable<Individual>{
        List<Double> position;
        Double fitness;
        public Individual(List<Double> position) {
            this.position = position;
            this.fitness = culFitness(position);
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

        @Override
        public String toString() {
            return new String("fitness: "+fitness+" position: "+position);
        }

        @Override
        public int compareTo(Individual o) {
            double r = this.getFitness() - o.getFitness();
            if(r==0)return 0;
            return r>0?1:-1;
        }
    }
}
