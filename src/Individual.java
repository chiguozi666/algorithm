import java.util.List;

public class Individual implements Comparable<Individual>{
    List<Double> position;
    Double fitness;
    public Individual(List<Double> position) {
        this.position = position;
    }

    public Individual(List<Double> position, Double fitness) {
        this.position = position;
        this.fitness = fitness;
    }

    public List<Double> getPosition() {
        return position;
    }

    public void setPosition(List<Double> position) {
        this.position = position;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    @Override
    public String toString() {
        return new String("fitness: "+fitness+" position: "+position);
    }

    @Override
    public int compareTo(Individual o) {
        double r = this.getFitness() - o.getFitness();
        if(r==0)return 0;
        return r>0?1:-1;
    }
}