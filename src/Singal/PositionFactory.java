package Singal;

import java.util.List;

import static java.lang.Math.random;

public class PositionFactory {
    private double ub;
    private double lb;
    private int dim;
    private Individual.FitnessCulTor fitnessCulTor;
    public PositionFactory(int dim, double ub, double lb){
        this.ub = ub;
        this.lb = lb;
        this.dim = dim;
    }
    public Position initRandomPosition(){
        Position res = new Position(dim,ub,lb);
        return res;
    }
    public Position build(Double[] a){
        return new Position(a,ub,lb);
    }
    public Position build(List<Double> a){
        return new Position(a,ub,lb);
    }
    public Position buildEmpty(){
        return new Position(dim,lb,ub,lb);
    }
}
