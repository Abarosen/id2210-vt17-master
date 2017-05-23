package se.kth.app.sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.sics.kompics.*;

/**
 * Created by Barosen on 2017-05-18.
 */
public class GSet extends SuperSet{
    private static final Logger LOG = LoggerFactory.getLogger(GSet.class);

    public GSet(){
        super();
        LOG.info("GSet started");

        subscribe(handleInternal, cb);
        subscribe(handleLookup, app);
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
                }
            }catch(ClassCastException  e){
                LOG.debug("Got something strange");
            }
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
