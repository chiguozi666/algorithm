package Singal;

import java.util.*;

import static java.lang.Math.*;

//Sparrow Search Algorithm 麻雀优化算法
public class OOLSSA {
    double ST = 0.8;//警戒值
    double pd = 0.2;//faxi0
    double sd = 0.1;
    double ub = 500.0;
    double lb = -500.0;
    int pop = 50;
    double epsilon = 1e-10;
    int PDCount;
    int SDCount;
    int iterTimes;
    static int[][] table = {
            {0,0,0,0,0,0,0},
            {0,0,0,1,1,1,1},
            {0,1,1,0,0,1,1},
            {0,1,1,1,1,0,0},
            {1,0,1,1,0,1,0},
            {1,1,0,0,1,1,0},
            {1,1,0,1,0,0,1}
    };
    int dim;
    int maxIter;
    List<Individual> population;
    List<Individual> newPopulation;
    Random random = new Random();
    Individual best;
    Individual worst;
    Individual globalBest;
    Individual.FitnessCulTor fitnessCulTor;
    PositionFactory positionFactory;
    public static void main(String[] args) {
        new OOLSSA(30, new Individual.FitnessCulTor() {
            @Override
            public double culFitness(Individual i) {
                return FUtil.F8(i.position.getData());
            }
        },30);
    }
    public OOLSSA(int pop, Individual.FitnessCulTor fitnessCulTor, int dim){
        this.fitnessCulTor = fitnessCulTor;
        this.positionFactory = new PositionFactory(dim,this.ub,this.lb);
        this.pop =pop;
        this.dim = dim;
        iterTimes = 0;
        maxIter = 1000;
        PDCount = (int) (pop*pd);
        SDCount = (int) (pop*sd);
        Init();
        Sort();
        globalBest = population.get(0).clone();
        globalBest.setFitness(population.get(0).fitness);
        while(iterTimes<maxIter){
            setCurrentWorstAndBest();
            UpdateProducer();
            setCurrentWorstAndBest();
            UpdateScrounger();
            setCurrentWorstAndBest();
            ScroungerZhengJiao();
            setCurrentWorstAndBest();
            UpdateDetector();

            globalBest = getGlobalBest();
            System.out.println(globalBest.fitness);
            iterTimes++;
            Sort();
        }
    }
    private void setCurrentWorstAndBest(){
        int bestIndex = 0;
        int worstIndex = 0;
        //深拷贝一下
        for (int i = 0; i < pop; i++) {
            if (population.get(i).fitness<population.get(bestIndex).fitness){
                bestIndex = i;
            }
            if (population.get(i).fitness>population.get(worstIndex).fitness){
                worstIndex = i;
            }
        }
        best = population.get(bestIndex).clone();
        worst =population.get(worstIndex).clone();
    }
    private Individual getGlobalBest(){
        int bestIndex = 0;
        for (int i = 0; i < pop; i++) {
            if (population.get(i).fitness<population.get(bestIndex).fitness){
                bestIndex = i;
            }
        }
        if(globalBest==null||globalBest.fitness>population.get(bestIndex).fitness){
            globalBest = population.get(bestIndex).clone();
        }
        return globalBest;
    }
    public void Init(){
        population = new ArrayList<>(pop);
        for (int i = 0; i < pop; i++) {
            Position position = this.positionFactory.initRandomPosition();
            Individual individual = new Individual(position);
            updateFitness(individual);
            population.add(individual);
        }
        newPopulation = new ArrayList<>(pop);
    }
    public void Sort(){
        Collections.sort(population);
    }
    //更新生产者
    public void UpdateProducer(){//这时的适应度还没更新
        for (int i = 0; i < PDCount; i++) {
            double alpha = random();
            Individual individual = population.get(i);
            Position position = individual.getPosition();
            double r2 = random();
            if(r2<ST){
                for (int j = 0; j < dim; j++) {
                    double x = position.get(j);
                    x = x*Math.exp((double) -(i+1)/(alpha*maxIter));
                    position.set(j,x);
                }
            }else {
                double Q = random.nextGaussian();//看看在里面还是外面
                for (int j = 0; j < dim; j++) {
                    double x = position.get(j);
                    x = x + Q*x;
                    position.set(j,x);
                }
            }
            updateFitness(individual);
        }
    }
    //
    //更新发现者(发现危险的麻雀)
    public void UpdateScrounger(){
        Position p = best.getPosition();
        Position Rp = p.clone();
        for (int j = 0; j < Rp.size(); j++) {
            double a = random.nextGaussian();
            double F = random()<0.5?-1:1;
            double x = p.get(j)*F*a;//高斯扰动
            Rp.set(j,x);
        }
        Position worstPosition = worst.getPosition();
        for (int i = PDCount; i < pop; i++) {
            Individual individual = population.get(i);
            Position position = individual.getPosition();
            if(i > pop/2){
                for (int j = 0; j < dim; j++) {
                    double Q = random.nextGaussian();
                    double x = position.get(j);
                    x = Q*Math.exp((worstPosition.get(j)-position.get(j))/(i*i));
                    position.set(j,x);
                }
            }else {

                /**
                 * | xi - xp|* APlus is 1*dim * dim*1
                 * the result is 1*1 so the x^{t+1} just |x^t_i,j - xPj| * APlus j
                 */
                double aPlus[] = getAPlus();
                for (int j = 0; j < dim; j++) {

                    double x = position.get(j);
                    double pX = Rp.get(j);
                    x = pX + Math.abs(x-pX)*aPlus[j];
                    position.set(j,x);
                }
            }
            updateFitness(individual);
        }
    }
    public void ScroungerZhengJiao(){
        for (int i = SDCount; i < pop; i++) {//估摸着要 8 * 0.8 * pop size = 180大小的迭代
            Individual individual = population.get(i);//目前最好的解假装是他
            Position position = individual.getPosition();
            Position oppoPosition = getOppositeLearning(individual);//对立的个体
            List<Position> orthogonalPositions = buildOrthogonalPosition(position,oppoPosition);
            //正交化的解们
            for (int j = 1; j < orthogonalPositions.size(); j++) {
                Individual curI = new Individual(orthogonalPositions.get(j));
                updateFitness(curI);
                if(curI.fitness<individual.fitness){//比最好的解要好的话就替换
                    individual = curI;
                }
            }
            population.set(i,individual);
        }
    }
    private Position getOppositeLearning(Individual individual){
        Position father =  individual.getPosition();
        Position oppoLearning = positionFactory.buildEmpty();
        double beta = random();
        if (individual.fitness>best.fitness){
            Position bestPosition = best.getPosition();

            for (int i = 0; i < dim; i++) {
                double x = bestPosition.get(i)+beta*Math.abs(father.get(i)-bestPosition.get(i));
                oppoLearning.set(i,x);
            }
        }else {
            for (int i = 0; i < dim; i++) {
                double x = lb + ub - beta*father.get(i);
                oppoLearning.set(i,x);
            }
        }
        return oppoLearning;
    }
    private List<Position> buildOrthogonalPosition(Position a,Position b){
        int low = 0;
        int high = dim;
        int[] div = new int[table[0].length+1];//一共要切割成 正交表列号个块;加个哨兵节点
        div[0] = 0;//div = {0,7,8,16,30,97,64} ,[0,6] [7,7] [8,16]....[64,dim]
        div[div.length-1] = dim;
        //构建切割表
        for (int i = 1; i < div.length-1; i++) {
            int r = low + (int)(random()*(high-low));
            div[i] = r;
        }
        Arrays.sort(div);//直接偷懒了，这样其实更好，更均匀，
        List<Position> res = new ArrayList<>(table.length);
        Position[] exchangeList = new Position[]{a,b};
        for (int i = 0; i < table.length; i++) {//查表
            Position position = this.positionFactory.buildEmpty();
            for (int j = 0; j < table[i].length; j++) {//查表
                int select = table[i][j];
                for (int k = div[j]; k<div[j+1]; k++) {
                    position.set(k,exchangeList[select].get(k));
                }
            }
            res.add(position);
        }
        return res;
    }
    private void exchange(Position a,Position b,int left,int right){
        if(left>right)throw new RuntimeException("left should < right");
        for (int i = left; i < right; i++) {
            double temp = a.get(i);
            a.set(i,b.get(i));
            b.set(i,temp);
        }
    }
    public void UpdateDetector(){
        Position gPosition = best.getPosition();
        Position wPosition = worst.getPosition();
        double beta = random.nextGaussian();
        Individual globalBest = getGlobalBest();
        Position globalBestPosition = globalBest.getPosition();
        boolean visit[] = new boolean[pop];
        for (int i = 0; i < SDCount; i++) {
            int k = (int)(pop*random());//随机抽出一个
            while(visit[k]) k = (int)(pop*random());
            visit[k] = true;
            Individual individual = population.get(k);
            //Individual individual = population.get(i);
            Position position = individual.getPosition();
            if(individual.getFitness()>globalBest.getFitness()){
                for (int j = 0; j < dim; j++) {
                    double x = individual.getPosition().get(j);
                    double xBest = globalBestPosition.get(j);
                    x = xBest + beta*Math.abs(x-xBest);
                    position.set(j,x);
                }
            }else if(individual.getFitness()==globalBest.getFitness()){
                double fi = individual.getFitness();
                double fw = worst.getFitness();
                for (int j = 0; j < dim; j++) {
                    double x = individual.getPosition().get(j);
                    double K = random()*2 - 1;//k [-1,1]
                    x = x + K * (Math.abs(x-wPosition.get(j))/(fi-fw+epsilon));
                    position.set(j,x);
                }
            }
            updateFitness(individual);
        }

    }
    private void updateFitness(Individual individual){
        double fitness = this.fitnessCulTor.culFitness(individual);
        individual.setFitness(fitness);
    }


    //A+ * L;
    private double[] getAPlus(){
        //AT is controlling +- ,and (AAT)^-1 just 1/dim (= _=') silly boy
        double APlus[] = new double[dim];
        for (int i = 0; i < dim; i++) {//building random A 1 x dim;
            APlus[i] = random()<0.5?(double) 1/dim:(double) -1/dim;
        }
        return APlus;
    }
}
