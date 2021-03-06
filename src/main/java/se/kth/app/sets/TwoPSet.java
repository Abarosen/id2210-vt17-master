package se.kth.app.sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.kth.app.sets.graph.TwoP2PGraph;
import se.sics.kompics.*;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.ktoolbox.util.network.KAddress;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Barosen on 2017-05-18.
 */
public class TwoPSet extends SuperSet{
    private static final Logger LOG = LoggerFactory.getLogger(TwoPSet.class);

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
                    LOG.trace("{} adding value({}) store: {}, tombstone: {}", logPrefix, temp.value, storage, tombstones);
                  GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
                        gv.setValue("Set.receivedadds", gv.getValue("Set.receivedadds", Integer.class) + 1);
                }else if(temp.type.equals(SetOperations.OpType.Remove)){
                    //Remove
                    if(storage.contains(temp.value)) {
                        tombstones.add(temp.value);
                        LOG.trace("{} removing value({}) store: {}, tombstone: {}", logPrefix, temp.value, storage, tombstones);
                      GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
                            gv.setValue("Set.receivedremoves", gv.getValue("Set.receivedremoves", Integer.class) + 1);
                    }
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
            if(storage.contains(event.key)){
                if(!tombstones.contains(event.key)) {
                    LOG.trace("{} lookup({}), result: true", logPrefix, event.key);
                    trigger(new ExternalEvents.Response(event.ret, event.key, true), app);
                    return;
                }
            }
            LOG.trace("{} lookup({}), result: false", logPrefix, event.key);
            trigger(new ExternalEvents.Response(event.ret, event.key, false), app);
        }
    };

    public static class Init extends se.sics.kompics.Init<TwoPSet> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
