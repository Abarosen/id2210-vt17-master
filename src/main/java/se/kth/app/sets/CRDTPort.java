package se.kth.app.sets;

import se.kth.app.sets.ORSet.ORSetOperations;
import se.kth.app.sets.graph.GraphOperations;
import se.sics.kompics.PortType;

/**
 * Created by Barosen on 2017-05-19.
 */
public class CRDTPort extends PortType{
    {
        //Simple Sets
        indication(SetOperations.Response.class);
        request(SetOperations.Lookup.class);
        request(SetOperations.Add.class);
        request(SetOperations.Remove.class);

        //OR-Set
        indication(ORSetOperations.Response.class);
        request(ORSetOperations.Lookup.class);
        request(ORSetOperations.Add.class);
        request(ORSetOperations.Remove.class);

        //Graph
        indication(GraphOperations.Response.class);
        request(GraphOperations.Lookup.class);
        request(GraphOperations.AddE.class);
        request(GraphOperations.AddV.class);
        request(GraphOperations.RemoveE.class);
        request(GraphOperations.RemoveV.class);
    }
}
