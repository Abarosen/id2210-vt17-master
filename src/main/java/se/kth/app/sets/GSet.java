package se.kth.app.sets;

import se.sics.kompics.*;

/**
 * Created by Barosen on 2017-05-18.
 */
public class GSet extends SuperSet{

    GSet(){
        super();
        subscribe(handleAdd, cb);
        subscribe(handleLookup, app);
    }

    //Add
    Handler handleAdd = new Handler<SetOperations.InternalAdd>() {
        @Override
        public void handle(SetOperations.InternalAdd event) {
            storage.add(event.value);

        }
    };

    //Lookup
    Handler handleLookup = new Handler<SetOperations.Lookup>() {
        @Override
        public void handle(SetOperations.Lookup event) {
            if(storage.contains(event.key)){
                trigger(new SetOperations.Response(true), app);
                return;
            }
            trigger(new SetOperations.Response(false), app);
        }
    };

}
