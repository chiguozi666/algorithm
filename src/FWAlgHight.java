import java.util.*;

import static java.lang.Math.*;

public class FWAlgHight {
    static List<Firework> fireworks = null;
    static int initFireworkNums = 50;
    public static double upLimit = 50;
    public static double downLimit = 0;
    public static double ymax = 0;
    public static double ymin = Double.MAX_VALUE;
    public static Vector<Double> xmax = new Vector<>();
    public static Vector<Double> xmin = new Vector<>();
    final public static double kesi = Double.MIN_VALUE;
    final public static double vecDimension = 15;
    final public static double a = 1;
    final public static double b = 3;
    final public static double m = 3;
    final public static double AHat = 5;
    public static int bestIndex = 0;
    public static double Sphere(Vector<Double> x){
        double sum = 0;
        for (int i = 0; i < x.size(); i++) {
            sum += x.get(i)*x.get(i);
        }
        return sum;
    }
    static double sHat[] = null;
    static double A[] = null;
    static int times = 0;
    Random random = new Random();
    static FWAlgHight fwAlg = new FWAlgHight();
    List<Spark> sparks = new ArrayList<>();
    public static void main(String[] args) {
        initRandomFireWork();
        while(true){
            initS_A();
            fwAlg.getSparkLoc();
            fwAlg.getGaussianSpark();
            fwAlg.seleteNewSpark();
            PrintData();
            RefreshData();
        }
    }
    public static Vector<Double> getRandomVector(){
        Vector<Double> vector = new Vector<>();
        for (int i = 0; i < vecDimension; i++) {
            vector.add(random()*(upLimit-downLimit)+downLimit);
        }
        return vector;
    }
    public static void initRandomFireWork(){
        fireworks = new ArrayList<>();
        ymax = 0;
        ymin = 0;
        for (int i = 0; i < initFireworkNums; i++) {
            Vector<Double> loc = getRandomVector();//随机生成位置
            double y = Sphere(loc);
            fireworks.add(fwAlg.new Firework(loc,y));//内部类构造方法
            if(ymax < y){//看看是找最大解还是最小解
                bestIndex = i;
            }
            xmax = ymax < y ? loc : xmax;
            ymax = ymax < y ? y : ymax;
            xmin = ymin > y ? loc : xmin;
            ymin = ymin > y ? y : ymin;
        }
    }
    public static void RefreshData(){
        ymax = 0;
        ymin = 0;
        for (int i = 0; i < fireworks.size(); i++) {
            Vector<Double> loc = fireworks.get(i).getLoc();//随机生成位置
            double y = fireworks.get(i).getY();
            if(ymax < y){//看看是找最大解还是最小解
                bestIndex = i;
            }
            xmax = ymax < y ? loc : xmax;
            ymax = ymax < y ? y : ymax;
            xmin = ymin > y ? loc : xmin;
            ymin = ymin > y ? y : ymin;
        }
    }
    public static void PrintData(){
        System.out.println("第"+times+++"轮"+"最好的xmax:"+xmax+"ymax:"+(-ymax+1000));
    }
    public static void initS_A(){
        double siSum = 0;//si分母累加的部分
        double s[] = new double[fireworks.size()];
        sHat = new double[fireworks.size()];
        A = new double[fireworks.size()];
        double ASum = 0;//a分母累加的部分
        for (int i = 0; i < fireworks.size(); i++) {//对应eq(2)
            Firework firework = fireworks.get(i);
            s[i] = ymax - firework.getY();
            siSum += s[i];
            s[i] += kesi;
            //计算增幅向量
            A[i] = firework.getY() - ymin;
            ASum += A[i];
            A[i] += kesi;
        }
        for(int i = 0; i < fireworks.size();i++){//对应eq(3) s[i]是没有m的
            s[i] = s[i]/(siSum+kesi);
            if(s[i] < a) sHat[i] = round(a * m);
            else if (s[i] > b) sHat[i] = round(b * m);
            else sHat[i] = round(s[i] * m);
            A[i] = AHat*A[i]/(ASum+kesi);
        }
    }


    public void getSparkLoc(){
        sparks = new ArrayList<>();
        for(int index = 0;index < fireworks.size();index++){
            Firework firework = fireworks.get(index);
            for (int i = 0; i < sHat[index]; i++) {
                //
                double z = round((vecDimension)*random());//一共有z维度的被影响
                Vector<Double> loc = firework.getLoc();
                for (int j = 0; j < z; j++) {
                    boolean isVisit[] = new boolean[];//是否被击中
                    double h = A[index]*(random()*2+-1);
                    double prob = random()*vecDimension;//轮盘赌的飞镖
                    double prob_temp = 0;
                    for (int k = 0; k < vecDimension; k++) {
                        if(!isVisit[k]){
                            prob_temp+=loc[]
                        }
                    }
                }

                loc+=h;
                if(loc>upLimit||loc<downLimit){//越界判断
                    loc = downLimit+abs(loc)%(upLimit-downLimit);
                }
                Spark spark = this.new Spark(loc,f(loc));
                sparks.add(spark);
            }
        }
    }
    public void getGaussianSpark(){
        for (int i = 0; i < m; i++) {
            int index = (int)round(random()*fireworks.size())%fireworks.size();//随机挑出一个烟花
            Firework firework = fireworks.get(index);
            double gaussian = random.nextGaussian();
            double loc = firework.getLoc()*gaussian;
            if(loc>upLimit||loc<downLimit){//越界判断
                loc = downLimit+abs(loc)%(upLimit-downLimit);
            }
            Spark spark = this.new Spark(loc,f(loc));
            sparks.add(spark);
        }
    }
    public void seleteNewSpark(){
        double sum = 0;
        List<Firework> newFireWorks = new LinkedList<>();
        Firework firework = fireworks.remove(bestIndex);//将最好的加进来
        newFireWorks.add(firework);
        fireworks.addAll(sparks);//将所有烟花，候选的加入候选集
        boolean isVisit[] = new boolean[fireworks.size()];
        double R[] = new double[fireworks.size()];
        for (int i = 0; i < fireworks.size(); i++) {
            R[i] = 0;
            Firework fireI = fireworks.get(i);
            for (int j = 0; j < fireworks.size(); j++) {
                Firework fireJ = fireworks.get(j);
                R[i]+=abs(fireI.getLoc()-fireJ.getLoc());//算出距离
            }
        }
        while(newFireWorks.size()<initFireworkNums){
            sum = 0;
            for (int i = 0; i < fireworks.size(); i++) {
                if(!isVisit[i]){
                    sum+=R[i];
                }
            }
            double prob = random()*sum;
            double curProb = 0;
            for(int i = 0;i < fireworks.size();i++){
                if(!isVisit[i]){
                    curProb+=R[i];
                    if(prob<curProb){//轮盘命中
                        newFireWorks.add(fireworks.get(i));
                        isVisit[i] = true;
                        break;
                    }
                }
            }
        }
        fireworks = newFireWorks;//构建完新的
    }
    public class Firework{
        Vector<Double> loc;
        double y;

        public Firework(Vector<Double> loc, double y) {
            this.loc = loc;
            this.y = y;
        }

        public Vector<Double> getLoc() {
            return loc;
        }

        public void setLoc(Vector<Double> loc) {
            this.loc = loc;
        }

        public double getY() {
            return y;
        }

        public void setY(double y) {
            this.y = y;
        }

        @Override
        public String toString() {
            return "Firework{" +
                    "loc=" + loc +
                    ", y=" + y +
                    '}';
        }
    }
    public class Spark extends Firework{
        public Spark(Vector<Double> loc, double y) {
            super(loc, y);
        }
    }

}
