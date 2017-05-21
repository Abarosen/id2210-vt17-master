package se.kth.app.broadcast;

import se.sics.kompics.PortType;

/**
 * Created by Barosen on 2017-05-02.
 */
public class CBPort extends PortType {
    {
        indication(CB.CB_Deliver.class);
        request(CB.CB_Broadcast.class);
    }
}