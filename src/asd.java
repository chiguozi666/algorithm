import java.util.*;

import static java.lang.Math.*;

public class asd {
    public static void main(String[] args) {
        List<Double> a = Arrays.asList(new Double[]{1.0,2.0,30.0});
        List<Double> b = Arrays.asList();
        a.set(1,999.0);
        System.out.println(b.toString());

    }
}
class Solution {
    public int numMatchingSubseq(String s, String[] words) {
        char cs[] = s.toCharArray();
        int res = 0;
        for (int i = 0; i < words.length; i++) {
            int cur = 0;
            char curcc[] = words[i].toCharArray();
            int n =  curcc.length;
            boolean flag = false;
            for (int j = 0; j < curcc.length; j++) {
                flag = false;
                char c = curcc[j];
                for (; cur < cs.length; cur++) {
                    if (c==cs[cur]){
                        flag = true;
                        cur++;
                        break;
                    }
                }
                if (flag)continue;else break;
            }
            if (flag)res++;
        }
        return res;
    }
}