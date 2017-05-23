package se.kth.app.sets;

import se.sics.kompics.PortType;

/**
 * Created by Barosen on 2017-05-19.
 */
public class SuperSetPort extends PortType{
    {
        indication(SetOperations.Response.class);
        request(SetOperations.Lookup.class);
        request(SetOperations.Add.class);
        request(SetOperations.Remove.class);
    }
}
