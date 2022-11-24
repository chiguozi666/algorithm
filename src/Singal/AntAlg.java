package Singal;

import java.util.*;

import static java.lang.Math.*;

public class AntAlg {
    static double alpha = 5.0;
    static double beta = 13.0;
    static double rho = 0.5;
    static double q = 100;
    static double[] x =  new double[]{178,272,176,171,650,499,267,703,408,437,491,74,532,
            416,626,42,271,359,163,508,229,576,147,560,35,714,
            757,517,64,314,675,690,391,628,87,240,705,699,258,
            428,614,36,360,482,666,597,209,201,492,294};
    static double[] y =  {
            170,395,198,151,242,556,57,401,305,421,267,105,525,
            381,244,330,395,169,141,380,153,442,528,329,232,48,
            498,265,343,120,165,50,433,63,491,275,348,222,288,
            490,213,524,244,114,104,552,70,425,227,331
    };
    static double pheromoneGraph[][] = new double[x.length][x.length];
    static double distance_graph[][] = new double[x.length][x.length];
    public static void main(String[] args) {
        for(int i=0;i<x.length;i++){
            for(int j=0;j<x.length;j++){
                pheromoneGraph[i][j] = 1;
                distance_graph[i][j] = sqrt((x[i]-x[j])*(x[i]-x[j])+(y[i]-y[j])*(y[i]-y[j]));
            }
        }
        AntAlg antAlg = new AntAlg();
        ArrayList<Ant> ants = new ArrayList<>();
        for(int i=0;i<100;i++){
            ants.add(antAlg.new Ant());
        }
        double best_distance = Double.MAX_VALUE;
        int best_path[] = null;
        int times = 1;
        while(true){
            ants.forEach(ant -> ant.searchPath());
            int bestAnt = 0;
            boolean flag = false;
            for(int i = 0;i < ants.size();i++){
                if(ants.get(i).bestDistance<=best_distance){
                    best_distance = ants.get(i).bestDistance;
                    bestAnt = i;
                    flag =true;
                    best_path = Arrays.copyOf(ants.get(i).bestPath,ants.get(i).bestPath.length);
                }
            }
            if(flag){
                System.out.println("第"+times+"轮\t"+"最佳距离是:" + best_distance+" ----路径是----"+Arrays.toString(best_path));
            }
            times++;
            updatePheromoneGraph(ants);
            //ants.forEach(ant -> ant.gotoNext());
        }

    }
    public static void updatePheromoneGraph(ArrayList<Ant> ants){
        double temp_pheromone[][] = new double[x.length][x.length];
        for (int i = 0; i < x.length; i++) {
            Arrays.fill(temp_pheromone[i],0);
        }
        for (int i = 0; i < ants.size(); i++) {
            Ant ant = ants.get(i);
            for(int j = 1;j < x.length;j++) {
                int start = ant.path[j - 1];
                int end = ant.path[j];
                temp_pheromone[start][end] += q / ant.moveDistance;
                temp_pheromone[end][start] = temp_pheromone[start][end];
            }
        }
        for(int i = 0;i < x.length;i++){
            for (int j = 0; j < x.length; j++) {//挥发
                pheromoneGraph[i][j] = rho*pheromoneGraph[i][j] + temp_pheromone[i][j];
            }
        }
    }
    public class Ant{
        int path[] = null;
        int bestPath[] = null;
        boolean isVisit[] = null;
        int curLocate = -1;
        int moveCount = 0;
        double moveDistance = 0;
        double bestDistance = Double.MAX_VALUE;//存放最优路径

        public Ant(){
            reSetAnt();
        }
        public void reSetAnt(){
            if(path!=null){
                bestPath = Arrays.copyOf(path,path.length);
            }
            bestDistance = Double.MAX_VALUE;//存放最优路径
            path = new int[x.length];
            isVisit = new boolean[x.length];
            curLocate = (int)( random()* x.length);//初始化位置
            isVisit[curLocate] = true;
            moveCount = 0;
            bestDistance = moveDistance<bestDistance?moveDistance:bestDistance;
            moveDistance = 0;
            path[0] = curLocate;
        }
        public int chooseNextLoc(){
            double maxPxj = 0;
            int nexti = -1;
            double sumPxj = 0;
            double pheromoneList[] = new double[x.length];
            for(int i = 0;i<x.length; i++){
                if(!isVisit[i]){//还没浏览过的节点
                    double curPxj = pow(pheromoneGraph[curLocate][i],alpha)*pow(1/distance_graph[curLocate][i],beta);
                    pheromoneList[i] = curPxj;
                    sumPxj+=curPxj;
                }
            }
            double prob = random()*sumPxj;
            double prob_temp = 0;
            for (int i = 0; i < x.length; i++) {
                if(!isVisit[i]){
                    prob_temp+=pheromoneList[i];
                    if(prob<=prob_temp){
                        maxPxj=pheromoneList[i];
                        nexti = i;
                        break;
                    }
                }
            }
            return nexti;
        }

        public void searchPath(){
            while(this.moveCount+1!=x.length){
                int nexti = chooseNextLoc();
                moveDistance+=distance_graph[curLocate][nexti];
                curLocate = nexti;
                isVisit[curLocate] = true;
                moveCount+=1;
                path[moveCount] = nexti;
            }
            reSetAnt();
        }
    }

}
