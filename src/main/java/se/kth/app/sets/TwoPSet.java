package se.kth.app.sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.kth.app.sets.graph.TwoP2PGraph;
import se.sics.kompics.*;
import se.sics.ktoolbox.util.network.KAddress;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Barosen on 2017-05-18.
 */
public class TwoPSet extends SuperSet{
    private static final Logger LOG = LoggerFactory.getLogger(GSet.class);

    private Set<String> tombstones;

    private KAddress selfAdr;
    String logPrefix;

    public TwoPSet(Init init){
        super();
        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        tombstones = new HashSet<>();

        subscribe(handleLookup, app);
        subscribe(handleInternal, cb);

        LOG.info("{} TwoPSet started", logPrefix);
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
                    LOG.trace("{} adding value {}", logPrefix, temp.value);
                }else if(temp.type.equals(SetOperations.OpType.Remove)){
                    //Remove
                    if(storage.contains(temp.value)) {
                        storage.add(temp.value);
                        LOG.trace("{} removing value {}", logPrefix, temp.value);
                    }
                }
            }catch(ClassCastException  e){
                LOG.debug("{}Got something strange", logPrefix);
            }
        }
    };


    //Lookup
    Handler handleLookup = new Handler<SetOperations.Lookup>() {
        @Override
        public void handle(SetOperations.Lookup event) {
            if(storage.contains(event.key)){
                if(!tombstones.contains(event.key)) {
                    LOG.trace("{} lookup({}), result: true", logPrefix, event.key);
                    trigger(new SetOperations.Response(true), app);
                    return;
                }
            }
            LOG.trace("{} lookup({}), result: false", logPrefix, event.key);
            trigger(new SetOperations.Response(false), app);
        }
    };

    public static class Init extends se.sics.kompics.Init<TwoPSet> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
