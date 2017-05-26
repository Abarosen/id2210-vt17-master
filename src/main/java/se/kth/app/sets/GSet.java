package se.kth.app.sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.sics.kompics.*;
import se.sics.ktoolbox.util.network.KAddress;

/**
 * Created by Barosen on 2017-05-18.
 */
public class GSet extends SuperSet{
    private static final Logger LOG = LoggerFactory.getLogger(GSet.class);

    private KAddress selfAdr;
    private String logPrefix;

    public GSet(Init init){
        super();
        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";
        subscribe(handleInternal, cb);
        subscribe(handleLookup, app);
        LOG.info("{}GSet started", logPrefix);
    }


    Handler handleInternal = new Handler<CB.CB_Deliver>() {
        @Override
        public void handle(CB.CB_Deliver event) {
            SetOperations.InternalOperation temp;
            try{
                temp = (SetOperations.InternalOperation) event.getContent();
                if(temp.type.equals(SetOperations.OpType.Add)) {
                    //Add
                    LOG.trace("{} Adding value, Set: {}", logPrefix, storage.toString());
                    storage.add(temp.value);
                }
            }catch(ClassCastException  e){
                LOG.trace("{}Got something strange", logPrefix);
            }
        }
    };

    //Lookup
    Handler handleLookup = new Handler<ExternalEvents.Lookup>() {
        @Override
        public void handle(ExternalEvents.Lookup event) {
            boolean result = storage.contains(event.key);
            LOG.trace("{} Lookup({}), result: {}" , logPrefix, event.key, result);
            trigger(new ExternalEvents.Response(event.ret, result), app);
        }
    };

    public static class Init extends se.sics.kompics.Init<GSet> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
