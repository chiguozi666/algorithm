import java.util.*;

import static java.lang.Math.*;
import static java.lang.Math.sin;

public class GeneticAlgorithm {
    /**
     * 负责转码的操作
     * @param data
     * @param downLimit
     * @param upLimit
     * @param geneSize
     * @return
     */
    public static Boolean[] getGene(double data,double downLimit,double upLimit,int geneSize){
        double curRatio = (data-downLimit)/(upLimit-downLimit);//数据占的位置,肯定是0~1之间的数
        Boolean result[] = new Boolean[geneSize];
        double cur = curRatio;
        if(curRatio==1){
            result[0]=new Boolean(true);//第一位置1
            cur=cur-1;
        }else {
            result[0]=new Boolean(false);
        }
        int count = 1;
        while(geneSize>1){
            cur = cur*2;
            if(cur>=1){
                result[count] = true;
                cur = cur-1;
            }else {
                result[count] = false;
            }
            count++;
            geneSize--;
        }
        return result;
    }
    public static double decodeGene(Boolean[] gene,double downLimit,double upLimit,int geneSize){
        double cur = 1;
        int count = 0;
        double result = 0;
        //System.out.println(Arrays.toString(gene));
        for(;count<geneSize;count++){
            if(gene[count].equals(true)){
                result+=cur;
            }
            cur/=2;
        }
        result = downLimit+result*(upLimit-downLimit);
        return result;
    }
    public static LinkedList<Double> createPopulation(double downLimit, double upLimit, int size){
        LinkedList<Double> result = new LinkedList<>();
        for(;size>0;size--){
            result.add(downLimit+(upLimit-downLimit)*random());
        }
        return result;
    }
    public static LinkedList<Double> getF(LinkedList<Double> data){
        LinkedList<Double> result = new LinkedList<>();
        for(Double i:data){
            result.add(f(i));
        }
        return result;
    }
    //出来的结果是复制的概率
    public static LinkedList<Double> getmuta(List<Double> fdata){
        double sum = 0;
        for(double i:fdata){
            sum+=xf(i);//sum是指数变换后的适应度的累加
        }
        LinkedList<Double> result = new LinkedList<>();
        for(double i:fdata){
            result.add(xf(i)/sum);//这个是复制的概率
        }
        return result;
    }
    /**
     * 计算适应度的公式
     * @param x
     * @return
     */
    public static double f(double x){
        return x*sin(x)*cos(2*x)-2*x*sin(3*x)+3*x*sin(4*x);
    }
    public static double xf(double x){//指数变换出适应度
        return pow(E,-0.1*x);//e^(-βx)
    }
    public static void main(String[] args) {
        int populationSize = 10;
        double maxIter = 50;//最大迭代次数
        double changeRatio = 0.4;//交叉概率
        double mutatedRatio = 0.01;//变异概率
        int geneSize = 32;
        double downLimit = 0;
        double upLimit = 50;
        List<Double> population = createPopulation(0,50,10);
        int curIter=1;
        //System.out.println(Arrays.toString(getmuta(population).toArray()));
//        for(;curIter<=maxIter;curIter++){
//            LinkedList<Double> mutaMatrix = getmuta(population);
//        }
        while(curIter<maxIter){

            LinkedList<Double> mutaMatrix = getmuta(population);
            HashMap<Double,Double> map = new HashMap<>();
            for(int i=0;i<population.size();i++){
                map.put(mutaMatrix.get(i),population.get(i));//map里面包含键值对
            }
            LinkedList<Double> roulette = new LinkedList<>();
            double sum = 0;
            //创建轮盘
            for(int i=0;i<population.size();i++){
                sum = sum+mutaMatrix.get(i);
                roulette.add(sum);
            }
            //飞镖数组
            List<Double> dart = new LinkedList<Double>();
            for(int i=0;i<population.size();i++){
                dart.add(random());
            }
            //对飞镖排序
            Collections.sort(dart, new Comparator<Object>() {
                @Override
                public int compare(Object o1, Object o2) {
                    double result = (Double)o1 - (Double)o2;
                    if(result==0)return 0;
                    else if(result>0)return 1;
                    else return -1;
                }
            });
            double mutaAvg = 0;
            mutaAvg = 1/mutaMatrix.size();
            List<Double> sonPopulation = new LinkedList<>();
            boolean flag = false;//有前置的son
            for(int i=0;i<dart.size();i++){
                double son1 = 0;
                for(int j=0;j<roulette.size();j++){
                    if(dart.get(i)<roulette.get(j)&&sonPopulation.size()<populationSize){//飞镖命中
                        if(!flag) {//前面没有爸爸
                            son1 = population.get(j);
                            flag = !flag;
                        }
                        else{
                            flag = !flag;
                            double son2 = population.get(j);
                            int min = (int)(random()*geneSize);
                            int max = (int)(random()*geneSize);
                            if(min>max){//交换数字
                                min = max+min;
                                max = min-max;
                                min = min-max;
                            }
                            Boolean[] son1Gene = getGene(son1,downLimit,upLimit,geneSize);
                            Boolean[] son2Gene = getGene(son2,downLimit,upLimit,geneSize);
                            //进行基因的交叉
                            if(random()<changeRatio)
                            for(;min<max;min++){
                                boolean temp;
                                temp = son1Gene[min];
                                son1Gene[min] = son2Gene[min];
                                son2Gene[min] = temp;
                            }
                            //变异
                            if(random()<mutatedRatio){//变异率
                                int position = (int)Math.ceil(geneSize*random())-1;//上取整
                                son1Gene[position]=!son1Gene[position];
                                position = (int)Math.ceil(geneSize*random())-1;
                                son2Gene[position]=!son2Gene[position];
                            }
                            sonPopulation.add(decodeGene(son1Gene,downLimit,upLimit,geneSize));
                            sonPopulation.add(decodeGene(son2Gene,downLimit,upLimit,geneSize));
                        }
                    }else {
                        break;
                    }
                }

                //System.out.println(sonPopulation.toString());
                population = sonPopulation;
            }
            System.out.println(sonPopulation.toString());
            curIter++;
        }

    }
}
