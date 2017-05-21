package se.kth.app.broadcast;

import se.sics.kompics.PortType;

/**
 * Created by Barosen on 2017-05-02.
 */
public class RBPort extends PortType {
    {
        indication(RB.RB_Deliver.class);
        request(RB.RB_Broadcast.class);
    }
}