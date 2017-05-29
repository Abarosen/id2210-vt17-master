package se.kth.app.sets.ORSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.kth.app.broadcast.CBPort;
import se.kth.app.sets.CRDTPort;
import se.kth.app.sets.ExternalEvents;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.ktoolbox.util.network.KAddress;


import java.util.*;

/**
 * Created by Barosen on 2017-05-19.
 */
public class ORSet extends ComponentDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(ORSet.class);

    Positive<CBPort> cb = requires(CBPort.class);
    Negative<CRDTPort> app = provides(CRDTPort.class);

    private KAddress selfAdr;
    private String logPrefix;

    Map<String,Set<UUID>> set;


    public ORSet(Init init){
        set = new HashMap<>();
        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        subscribe(handleExternalAdd, app);
        subscribe(handleExternalRemove, app);
        subscribe(handleExternalLookup, app);

        subscribe(handleInternal, cb);
        LOG.info("{} ORSet started");
    }

    Handler handleExternalAdd = new Handler<ExternalEvents.Add>() {
        @Override
        public void handle(ExternalEvents.Add event) {
            trigger(new CB.CB_Broadcast(new ORSetOperations.InternalOperation(event.value, UUID.randomUUID())), cb);
        }
    };

    Handler handleExternalLookup = new Handler<ExternalEvents.Lookup>() {
        @Override
        public void handle(ExternalEvents.Lookup event) {
            Set<UUID> temp = set.get(event.key);
            if(temp == null || temp.isEmpty()){
                trigger(new ExternalEvents.Response(event.ret, event.key, false), app);
                return;
            }
            trigger(new ExternalEvents.Response(event.ret,event.key ,true), app);
        }
    };
    Handler handleExternalRemove = new Handler<ExternalEvents.Remove>() {
        @Override
        public void handle(ExternalEvents.Remove event) {
            Set<UUID> temp = set.get(event.value);
            if(temp == null || temp.isEmpty())
                return;

            trigger(new CB.CB_Broadcast(new ORSetOperations.InternalOperation(event.value, temp)), cb);
        }
    };

    Handler handleInternal = new Handler<CB.CB_Deliver>() {
        @Override
        public void handle(CB.CB_Deliver event) {
            ORSetOperations.InternalOperation temp;
            Set<UUID> tempset;
            try{
                temp = (ORSetOperations.InternalOperation) event.getContent();

                if(temp.type.equals(ORSetOperations.OpType.Add)) {
                    //Add
                    tempset = set.get(temp.value);
                    if(tempset == null){
                        tempset = new HashSet<UUID>();
                        tempset.add(temp.id);
                        set.put(temp.value, tempset);
                    }else{
                        tempset.add(temp.id);
                    }
                    LOG.trace("{} Adding value... set: {}", logPrefix, set);
                }else if(temp.type.equals(ORSetOperations.OpType.Remove)) {
                    //Remove
                    tempset = set.get(temp.value);
                    if(tempset == null)
                        return;
                    tempset.removeAll(temp.ids);
                    LOG.trace("{} removing ids({}), remaining for key: {}", logPrefix,temp.ids, set.get(temp.value));
                }
            }catch(ClassCastException  e){
                LOG.debug("{}Got something strange", logPrefix);
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<ORSet> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
