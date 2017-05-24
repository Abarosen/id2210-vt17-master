package se.kth.app.sets;

import se.kth.app.broadcast.CB;
import se.kth.app.broadcast.CBPort;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Barosen on 2017-05-19.
 */
public class SuperSet extends ComponentDefinition{

    Positive<CBPort> cb = requires(CBPort.class);
    Negative<CRDTPort> app = provides(CRDTPort.class);

    Set<String> storage;

    public SuperSet(){
        storage = new HashSet<>();

        subscribe(handleExternalAdd, app);
        subscribe(handleExternalRemove, app);
    }

    Handler handleExternalAdd = new Handler<ExternalEvents.Add>() {
        @Override
        public void handle(ExternalEvents.Add event) {
            trigger(new CB.CB_Broadcast(new SetOperations.InternalOperation(SetOperations.OpType.Add, event.value)), cb);
        }
    };

    Handler handleExternalRemove = new Handler<ExternalEvents.Remove>() {
        @Override
        public void handle(ExternalEvents.Remove event) {
            trigger(new CB.CB_Broadcast(new SetOperations.InternalOperation(SetOperations.OpType.Remove, event.value)), cb);
        }
    };

}
