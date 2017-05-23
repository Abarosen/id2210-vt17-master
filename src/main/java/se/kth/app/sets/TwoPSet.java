package se.kth.app.sets;

import se.kth.app.broadcast.CB;
import se.sics.kompics.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Barosen on 2017-05-18.
 */
public class TwoPSet extends SuperSet{

    private Set<String> tombstones;

    public TwoPSet(){
        super();
        tombstones = new HashSet<>();
        subscribe(handleLookup, app);
        subscribe(handleInternal, cb);
    }

    Handler handleInternal = new Handler<CB.CB_Deliver>() {
        @Override
        public void handle(CB.CB_Deliver event) {
            SetOperations.InternalOperation temp;
            try{
                temp = (SetOperations.InternalOperation) event.getContent();
                if(temp.type.equals(SetOperations.OpType.Add)) {
                    //Add
                    storage.add(temp.value);
                }else if(temp.type.equals(SetOperations.OpType.Remove)){
                    //Remove
                    if(storage.contains(temp.value)) {
                        storage.add(temp.value);
                    }
                }
            }catch(ClassCastException  e){}
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
