package Singal;

import java.util.*;
import java.util.function.UnaryOperator;

import static java.lang.Math.abs;
import static java.lang.Math.random;

public class Position{
    double ub;
    double lb;
    private List<Double> data;
    public Position(Double[] a,double ub,double lb){
        this.ub = ub;
        this.lb = lb;
        this.data = new ArrayList<>(a.length);
        for (int i = 0; i < a.length; i++) {
            this.data.add(i,a[i]);
        }
        letItLimit(ub,lb);
    }
    public Position(List<Double> a,double ub,double lb){
        this.ub = ub;
        this.lb = lb;
        this.data = a;
        letItLimit(ub,lb);
    }
    public Position(int dim,double ub,double lb){
        this.ub = ub;
        this.lb = lb;
        data = new ArrayList<>(dim);
        for (int j = 0; j < dim; j++) {
            double x = lb + (ub-lb)*random();
            //x = getSuitLimitX(x);
            data.add(x);
        }
    }
    public Position(int dim){
        this.data = new ArrayList<>(dim);
        for (int i = 0; i < dim; i++) {
            this.data.add(lb);
        }
    }
    public Position clone(){
        List<Double> newPosition = new ArrayList<Double>(this.data.size());
        for (int i = 0; i < this.data.size(); i++) {
            newPosition.add(this.data.get(i));
        }
        Position res = new Position(newPosition,ub,lb);
        return res;
    }
    public void letItLimit(double ub,double lb){
        for (int i = 0; i < data.size(); i++) {
            Double x = data.get(i);
            if (x > ub || x < lb) {//越界判断
                x =  lb + abs(x) % (ub - lb);
            }
            this.data.set(i,x);
        }
    }
    private Double getSuitLimitX(double x){
        if (x > ub || x < lb) {//越界判断
            x =  lb + abs(x) % (ub - lb);
        }
        return x;
    }
    public int size(){
        return this.data.size();
    }
    public void set(int i,Double value){
        value = getSuitLimitX(value);
        this.data.set(i,value);
    }

    public Double get(int i){
        return this.data.get(i);
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return this.data.toString();
    }
}
