package se.kth.app.sets.graph;

/**
 * Created by Barosen on 2017-05-23.
 */

public class Vertex{
    String id;
    public Vertex(String id){
        this.id = id;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == null || !(obj instanceof Vertex))
            return false;
        Vertex other = (Vertex) obj;
        return this.id.equals(other.id);
    }

    @Override
    public String toString() {
        return "<Vertex: " + id + ">";
    }
}
