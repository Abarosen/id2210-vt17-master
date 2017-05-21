package se.kth.app.sets;

import se.sics.kompics.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Barosen on 2017-05-18.
 */
public class TwoPSet extends SuperSet{

    private Set<String> tombstones;

    TwoPSet(){
        super();
        tombstones = new HashSet<>();
        subscribe(handleLookup, app);
        subscribe(handleAdd, cb);
        subscribe(handleRemove, cb);
    }


    //Add
    Handler handleAdd = new Handler<SetOperations.InternalAdd>() {
        @Override
        public void handle(SetOperations.InternalAdd event) {
            storage.add(event.value);

        }
    };

    //Remove
    Handler handleRemove = new Handler<SetOperations.InternalRemove>() {
        @Override
        public void handle(SetOperations.InternalRemove event) {
            if(storage.contains(event.value)) {
                storage.add(event.value);
            }
        }
    };

    //Lookup
    Handler handleLookup = new Handler<SetOperations.Lookup>() {
        @Override
        public void handle(SetOperations.Lookup event) {
            if(storage.contains(event.key)){
                if(!tombstones.contains(event.key)) {
                    trigger(new SetOperations.Response(true), app);
                    return;
                }
            }
            trigger(new SetOperations.Response(false), app);
        }
    };

}
