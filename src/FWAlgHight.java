import java.util.*;

import static java.lang.Math.*;

public class FWAlgHight {
    final static double offset = 0;
    static List<Firework> fireworks = null;
    static int initFireworkNums = 5;
    public static double upLimit = 100;
    public static double downLimit = -100;
    public static double ymax = -Double.MAX_VALUE+1;
    public static double ymin = Double.MAX_VALUE;
    public static List<Double> xmax = new Vector<>();
    public static List<Double> xmin = new Vector<>();
    final public static double kesi = Double.MIN_VALUE;
    final public static int vecDimension = 50;
    final public static double a = 0.04;
    final public static double b = 0.8;
    final public static double m = 50;
    final public static double mhat = 5;
    final public static double AHat = 40;
    public static int bestIndex = 0;
    public static double Sphere(List<Double> x){
        double sum = 0;
        for (int i = 0; i < x.size(); i++) {
            sum += x.get(i)*x.get(i);
        }
        return sum+offset;//修正函数
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
        for (int i = 0; i < initFireworkNums; i++) {
            List<Double> loc = getRandomVector();//随机生成位置
            double y = Sphere(loc);
            fireworks.add(fwAlg.new Firework(loc,y));//内部类构造方法
            if(ymin > y){//看看是找最大解还是最小解
                bestIndex = i;
            }
            if(ymax<y){
                ymax = y;
                xmax = loc;
                //Collections.copy(xmax,loc);
            } else if(ymin>y){
                ymin = y;
                xmin = loc;
                //Collections.copy(xmin,loc);
            }
//            xmax = ymax < y ? loc : xmax;
//            ymax = ymax < y ? y : ymax;
//            xmin = ymin > y ? loc : xmin;
//            ymin = ymin > y ? y : ymin;
        }
    }
    public static void RefreshData(){
        ymax = -Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        //ymin = 0;
        for (int i = 0; i < fireworks.size(); i++) {
            List<Double> loc = fireworks.get(i).getLoc();//随机生成位置
            double y = fireworks.get(i).getY();
            if(ymin > y){//看看是找最大解还是最小解
                bestIndex = i;
            }
            if(ymax<y){
                ymax = y;
                Collections.copy(xmax,loc);
            } else if(ymin>y){
                ymin = y;
                Collections.copy(xmin,loc);
            }

        }
    }
    public static void PrintData(){
        //System.out.println("第"+times+++"轮"+"最好的xmax:"+xmin+"ymax:"+(ymin));
        System.out.println("第"+times+++"轮"+"ymax:"+(ymin));
    }
    public static void initS_A(){
        double siSum = 0;//si分母累加的部分
        double s[] = new double[fireworks.size()];
        sHat = new double[fireworks.size()];
        A = new double[fireworks.size()];
        double ASum = 0;//a分母累加的部分
        setYmin();
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
            s[i] = m*s[i]/(siSum+kesi);
            if(s[i] < a*m) sHat[i] = round(a * m);
            else if (s[i] > b*m) sHat[i] = round(b * m);
            else sHat[i] = round(s[i]);
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
                List<Double> loc =  Arrays.asList(new Double[vecDimension]);;//深拷贝
                Collections.copy(loc,firework.getLoc());
                for (int j = 0; j < z;) {//j是投标的数目
                    boolean isVisit[] = new boolean[vecDimension];//这里的visit用于抽出n维，这n维是不重复的。
                    double h = A[index]*(random()*2+-1);
                    double prob = random()*vecDimension;//轮盘赌的飞镖
                    double prob_temp = 0;
                    for (int k = 0; k < vecDimension; k++) {
                        prob_temp+=1;
                        if(prob<prob_temp){
                            if(!isVisit[k]) {
                                loc.set(k, loc.get(k) + h);
                                if (loc.get(k) > upLimit || loc.get(k) < downLimit) {//越界判断
                                    loc.set(k, downLimit + abs(loc.get(k)) % (upLimit - downLimit));
                                    isVisit[k] = true;
                                }
                                j++;//成功更改一个
                            }
                            break;//这一轮投标结束
                        }
                    }
                }
                Spark spark = this.new Spark(loc,Sphere(loc));
                sparks.add(spark);
            }
        }
    }
    public void getGaussianSpark(){
        for (int i = 0; i < mhat; i++) {
            int index = (int)round(random()*fireworks.size())%fireworks.size();//随机挑出一个烟花
            Firework firework = fireworks.get(index);
            List<Double> loc =  Arrays.asList(new Double[vecDimension]);;//深拷贝
            Collections.copy(loc,firework.getLoc());
            double z = round((vecDimension)*random());//一共有z维度的被影响
            for (int j = 0; j < z;) {//j是投标的数目
                boolean isVisit[] = new boolean[vecDimension];//这里的visit用于抽出n维，这n维是不重复的。
                double prob = random()*vecDimension;//轮盘赌的飞镖
                double prob_temp = 0;
                for (int k = 0; k < vecDimension; k++) {
                    prob_temp+=1;
                    if(prob<prob_temp){
                        if(!isVisit[k]) {
                            loc.set(k, loc.get(k)*random.nextGaussian());
                            if (loc.get(k) > upLimit || loc.get(k) < downLimit) {//越界判断
                                loc.set(k, downLimit + abs(loc.get(k)) % (upLimit - downLimit));
                            }
                            j++;//成功更改一个
                        }
                        break;//这一轮投标结束
                    }
                }
            }
            Spark spark = this.new Spark(loc,Sphere(loc));
            sparks.add(spark);
        }
    }
    private static double culDistance(List<Double> a,List<Double> b){
        int distance = 0;
        for (int i = 0; i < a.size(); i++) {
            distance+=sqrt(a.get(i)-b.get(i))*(a.get(i)-b.get(i));
        }
        return (distance);
    }
    public static void setYmin(){
        double min = Double.MAX_VALUE;
        for (int i = 0; i < fireworks.size(); i++) {
            if(min>fireworks.get(i).getY()){
                bestIndex=i;
                min = fireworks.get(i).getY();
            }
            ymin = min;
        }
        Collections.copy(xmin,fireworks.get(bestIndex).getLoc());
    }
    public void seleteNewSpark(){
        double sum = 0;
        List<Firework> newFireWorks = new LinkedList<>();
        fireworks.addAll(sparks);//将所有烟花，候选的加入候选集
        setYmin();//找出最小的index
        Firework firework = fireworks.remove(bestIndex);//将最好的加进来
        newFireWorks.add(firework);

        boolean isVisit[] = new boolean[fireworks.size()];
        double R[] = new double[fireworks.size()];
        for (int i = 0; i < fireworks.size(); i++) {
            R[i] = 0;
            Firework fireI = fireworks.get(i);
            for (int j = 0; j < fireworks.size(); j++) {
                Firework fireJ = fireworks.get(j);
                R[i]+=culDistance(fireI.getLoc(),fireJ.getLoc())+Double.MIN_VALUE;//算出距离,防止全0,这里会失去精度，因为sqrt的缘故
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
        List<Double> loc;
        double y;

        public Firework(List<Double> loc, double y) {
            this.loc = loc;
            this.y = y;
        }

        public List<Double> getLoc() {
            return loc;
        }

        public void setLoc(List<Double> loc) {
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
        public Spark(List<Double> loc, double y) {
            super(loc, y);
        }
    }

}
