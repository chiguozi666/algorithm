import java.util.*;
import java.util.function.IntFunction;

import static java.lang.Math.random;
import static java.lang.Math.round;

public class IParetoProblem {
    public static void main(String[] args) {
        List<Per> list = new ArrayList<>();
        list.add(new Per(1,5,3));
        list.add(new Per(3,2,7));
        list.add(new Per(1,4,1));
        list.add(new Per(8,1,8));
        list.add(new Per(3,5,4));
        list.add(new Per(6,1,2));
        list.add(new Per(4,9,3));
        list.add(new Per(1,4,2));
        list.add(new Per(2,3,3));
        list.add(new Per(1,8,8));
        list.add(new Per(3,1,9));
        list.add(new Per(1,9,1));
//        for(int i=0;i<20;i++){
//            list.add(new Per((int)(random()*10),(int)(random()*10),(int)(random()*10)));
//        }
        //进行个排序方便自己看
        list.sort(new Comparator<Per>() {
            @Override
            public int compare(Per o1, Per o2) {
                if(o1.a<o2.a){
                    return -1;
                }else if(o1.a==o2.a){
                    if(o1.b<o2.b){
                        return -1;
                    }else if(o1.b==o2.b){
                        if(o1.c<o2.c){
                            return -1;
                        }else if(o1.c==o2.c){
                            return 0;
                        }else return 0;
                    }else return 1;
                }else return 1;
            }
        });
        for(Per p:list){
            System.out.println(p);
        }
        //寻找每个解的非支配解
        LinkedList<LinkedList<Per>> Pop = new LinkedList<>();
        //visit[]数组记录的是本轮的已经跑掉的数组
        Boolean visit[] = new Boolean[list.size()];
        Arrays.setAll(visit, new IntFunction<Boolean>() {
            @Override
            public Boolean apply(int value) {
                return false;
            }
        });
        System.out.println(Arrays.toString(visit));
        int p = 0;
        int lastp=p;
        //run数组是记录下一轮里面已经走掉的元素，标记为ture
        Boolean run[] = new Boolean[list.size()];
        Arrays.setAll(run
                , new IntFunction<Boolean>() {
            @Override
            public Boolean apply(int value) {
                return false;
            }
        });
        while(p<list.size()){
            LinkedList<Per> temp = new LinkedList<>();
            for(int i=0;i<list.size();i++){
                if(visit[i])continue;
                Per first = list.get(i);
                boolean flag = true;//判断是否有大哥支配他
                for(int j=0;j<list.size();j++){
                    if(visit[j]||j==i)continue;
                    Per second = list.get(j);
                    if(first.a>=second.a&&first.b>=second.b&&first.c>=second.c){//被支配了
                        flag=false;break;
                    }
                }
                if(flag){
                    temp.add(first);
                    run[i] = true;
                    p++;
                }
            }
            if(p==lastp){
                for(int i=0;i<list.size();i++){
                    if(!visit[i]){
                        temp.add(list.get(i));
                        p++;
                    }
                }
            }
            Pop.add(temp);
            visit = Arrays.copyOf(run,run.length);
            lastp = p;
        }
        for(List a:Pop){
            System.out.println(Arrays.toString(a.toArray()));
        }
    }

}
class Per{
    int a;
    int b;
    int c;

    public Per(int a, int b, int c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    public int getC() {
        return c;
    }

    public void setC(int c) {
        this.c = c;
    }

    @Override
    public String toString() {
        return "Per{" +
                "a=" + a +
                ", b=" + b +
                ", c=" + c +
                '}';
    }
}
