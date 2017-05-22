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
    Negative<SuperSetPort> app = provides(SuperSetPort.class);

    Set<String> storage;

    SuperSet(){
        storage = new HashSet<>();

        subscribe(handleExternalAdd, app);
        subscribe(handleExternalRemove, app);
    }

    Handler handleExternalAdd = new Handler<SetOperations.Add>() {
        @Override
        public void handle(SetOperations.Add event) {
            trigger(new CB.CB_Broadcast(new SetOperations.InternalAdd(event.value)), cb);
        }
    };

    Handler handleExternalRemove = new Handler<SetOperations.Remove>() {
        @Override
        public void handle(SetOperations.Remove event) {
            trigger(new CB.CB_Broadcast(new SetOperations.InternalRemove(event.value)), cb);
        }
    };

}
