import java.util.ArrayList;
import java.util.List;

public abstract class Node {
    int id;
    FigureTable figure_table;
    Node predecessor_pointer;
    List<Integer> key_container;



    public abstract void join(Node p);

    public abstract void find(Node p, int k);

    public abstract void crash(Node p);

    public abstract void show(Node p);

    public abstract void showAll();
}


