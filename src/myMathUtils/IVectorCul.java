package myMathUtils;

import java.util.List;

public interface IVectorCul {
    public List<Double> addReturnNew(List<Double> a,List<Double> b);
    public List<Double> subReturnNew(List<Double> a,List<Double> b);
    public List<Double> addReturnNew(List<Double> a,double x);
    public List<Double> subReturnNew(List<Double> a,double x);
    public List<Double> multiReturnNew(List<Double> a,double x);
    public List<Double> divReturnNew(List<Double> a,double x);
    public void addSetA(List<Double> a,List<Double> b);
    public void subSetA(List<Double> a,List<Double> b);
    public void multiSetA(List<Double> a,double x);
    public void divSetA(List<Double> a,double x);

}
