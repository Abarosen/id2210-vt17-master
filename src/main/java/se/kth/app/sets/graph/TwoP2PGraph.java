package se.kth.app.sets.graph;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.kth.app.broadcast.CBPort;
import se.kth.app.sets.CRDTPort;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.ktoolbox.util.network.KAddress;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by Barosen on 2017-05-18.
 */
public class TwoP2PGraph extends ComponentDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(TwoP2PGraph.class);

    Positive<CBPort> cb = requires(CBPort.class);
    Negative<CRDTPort> app = provides(CRDTPort.class);

    Set<Vertex> VA;
    Set<Vertex> VR;
    Set<Edge> EA;
    Set<Edge> ER;

    private KAddress selfAdr;
    private String logPrefix;

    public TwoP2PGraph(Init init){
        super();

        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        VA = new HashSet<>();
        VR = new HashSet<>();
        EA = new HashSet<>();
        ER = new HashSet<>();


        subscribe(handleInternal, cb);
        subscribe(handleLookup, app);

        LOG.info("{} GSet started", logPrefix);
    }

    /*
    EXTERNAL
     */

    //Lookup
    Handler handleLookup = new Handler<GraphOperations.Lookup>() {
        @Override
        public void handle(GraphOperations.Lookup event) {
            boolean result = false;
            if(event.type.equals(GraphOperations.OpType.Edge)){
                result = VA.contains(event.e.v1) && !VR.contains(event.e.v1) && VA.contains(event.e.v2) && !VR.contains(event.e.v2) && EA.contains(event.e) && !ER.contains(event.e);
                LOG.trace("{} lookup({}): {}", logPrefix, event.e);
            }else{
                result = VA.contains(event.v) && !VR.contains(event.v);
                LOG.trace("{} lookup({}): {}", logPrefix, event.v);
            }
            trigger(new GraphOperations.Response(event.ret, result), app);

        }
    };

    Handler handleAddE = new Handler<GraphOperations.AddE>() {
        @Override
        public void handle(GraphOperations.AddE event) {

        }
    };

    Handler handleAddV = new Handler<GraphOperations.AddV>() {
        @Override
        public void handle(GraphOperations.AddV event) {
            trigger(new GraphOperations.InternalOperation(GraphOperations.OpType.Vertex, event.v), cb);
        }
    };

    Handler handleRemoveE = new Handler<GraphOperations.RemoveE>() {
        @Override
        public void handle(GraphOperations.RemoveE event) {

        }
    };

    Handler handleRemoveV = new Handler<GraphOperations.RemoveV>() {
        @Override
        public void handle(GraphOperations.RemoveV event) {

        }
    };

    /*
    INTERNAL
     */


    Handler handleInternal = new Handler<CB.CB_Deliver>() {
        @Override
        public void handle(CB.CB_Deliver event) {

        }
    };
    public static class Init extends se.sics.kompics.Init<TwoP2PGraph> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
