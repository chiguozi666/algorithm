package Singal;

import java.util.ArrayList;
import java.util.List;

public class Individual implements Comparable<Individual>{
    Position position;
    Double fitness;
    public Individual(Position position) {
        this.position = position;
    }

    public Individual(Position position, Double fitness) {
        this.position = position;
        this.fitness = fitness;
    }

    @Override
    protected Individual clone(){
        Position position = this.position.clone();
        Individual individual = new Individual(position);
        individual.setFitness(this.fitness);
        return individual;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
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