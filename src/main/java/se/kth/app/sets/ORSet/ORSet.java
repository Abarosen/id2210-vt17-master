package se.kth.app.sets.ORSet;

import se.kth.app.broadcast.CB;
import se.kth.app.broadcast.CBPort;
import se.kth.app.sets.SuperSetPort;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;

import java.util.*;

/**
 * Created by Barosen on 2017-05-19.
 */
public class ORSet extends ComponentDefinition {

    Positive<CBPort> cb = requires(CBPort.class);
    Negative<SuperSetPort> app = provides(SuperSetPort.class);


    Map<String,Set<UUID>> set;


    ORSet(){
        set = new HashMap<>();

        subscribe(handleExternalAdd, app);
        subscribe(handleExternalRemove, app);
        subscribe(handleExternalLookup, app);

        subscribe(handleInternalAdd, cb);
        subscribe(handleInternalRemove, cb);
    }

    Handler handleExternalAdd = new Handler<ORSetOperations.Add>() {
        @Override
        public void handle(ORSetOperations.Add event) {
            trigger(new CB.CB_Broadcast(new ORSetOperations.InternalAdd(event.value, UUID.randomUUID())), cb);
        }
    };

    Handler handleExternalLookup = new Handler<ORSetOperations.Lookup>() {
        @Override
        public void handle(ORSetOperations.Lookup event) {
            Set<UUID> temp = set.get(event.key);
            if(temp == null || temp.isEmpty()){
                trigger(new ORSetOperations.Response(false), app);
                return;
            }
            trigger(new ORSetOperations.Response(true), app);
        }
    };
    Handler handleExternalRemove = new Handler<ORSetOperations.Remove>() {
        @Override
        public void handle(ORSetOperations.Remove event) {
            Set<UUID> temp = set.get(event.value);
            if(temp == null || temp.isEmpty())
                return;
            
            trigger(new CB.CB_Broadcast(new ORSetOperations.InternalRemove(event.value, temp)), cb);
        }
    };


    Handler handleInternalAdd = new Handler<ORSetOperations.InternalAdd>() {
        @Override
        public void handle(ORSetOperations.InternalAdd event) {
            Set<UUID> temp = set.get(event.value);
            if(temp == null){
                temp = new HashSet<UUID>();
                temp.add(event.id);
                set.put(event.value, temp);
            }else{
                temp.add(event.id);
            }
        }
    };

    Handler handleInternalRemove = new Handler<ORSetOperations.InternalRemove>() {
        @Override
        public void handle(ORSetOperations.InternalRemove event) {
            Set<UUID> temp = set.get(event.value);
            if(temp == null)
                return;
            temp.removeAll(event.id);
        }
    };
}
