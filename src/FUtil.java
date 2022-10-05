import java.util.List;

public class FUtil {
    public static double Sphere(List<Double> x){
        double sum = 0;
        for (int i = 0; i < x.size(); i++) {
            sum += x.get(i)*x.get(i);
        }
        return sum;//修正函数
    }
}
