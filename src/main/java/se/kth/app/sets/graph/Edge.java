package se.kth.app.sets.graph;

/**
 * Created by Barosen on 2017-05-23.
 */
public class Edge{
    String v1, v2;

    public Edge(String v1, String v2){
        this.v1 = v1;
        this.v2 = v2;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Edge))
            return false;
        Edge other = (Edge) obj;
        return this.v1.equals(other.v1) && this.v2.equals(other.v2);
    }
}