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
import se.sics.kompics.simulator.util.GlobalView;
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
        subscribe(handleAddE, app);
        subscribe(handleAddV, app);
        subscribe(handleRemoveV, app);
        subscribe(handleRemoveE, app);

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
            Object retKey;
            if(event.type.equals(GraphOperations.OpType.Edge)){
                retKey = event.e;
                result = VA.contains(event.e.v1) && !VR.contains(event.e.v1) && VA.contains(event.e.v2) && !VR.contains(event.e.v2) && EA.contains(event.e) && !ER.contains(event.e);
                LOG.trace("{} lookup({}): {}", logPrefix, event.e);

            }else{
                retKey = event.v;
                result = VA.contains(event.v) && !VR.contains(event.v);
                LOG.trace("{} lookup({}): {}", logPrefix, event.v);
            }
            trigger(new GraphOperations.Response(event.ret, retKey, result), app);

        }
    };
    //Add Edge
    Handler handleAddE = new Handler<GraphOperations.AddE>() {
        @Override
        public void handle(GraphOperations.AddE event) {
            if(VA.contains(event.e.v1) && !VR.contains(event.e.v1) && VA.contains(event.e.v2) && !VR.contains(event.e.v2)){
                LOG.trace("{} External add: {}", logPrefix, event.e);
                trigger(new CB.CB_Broadcast(new GraphOperations.InternalOperation(GraphOperations.OpType.Edge, event.e)), cb);
            }
        }
    };

    Handler handleAddV = new Handler<GraphOperations.AddV>() {
        @Override
        public void handle(GraphOperations.AddV event) {
            trigger(new CB.CB_Broadcast(new GraphOperations.InternalOperation(GraphOperations.OpType.Vertex, event.v)), cb);
          GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
            gv.setValue("Set.receivedadds", gv.getValue("Set.receivedadds", Integer.class) + 1);
        }
    };

    Handler handleRemoveV = new Handler<GraphOperations.RemoveV>() {
        @Override
        public void handle(GraphOperations.RemoveV event) {
            if(VA.contains(event.v) && !VR.contains(event.v)){
                Set<Edge> temp = new HashSet<>(EA);
                temp.removeAll(ER);
                for(Edge e: temp){
                    if(e.v1.equals(event.v) || e.v2.equals(event.v))
                        ER.add(e);
                }
                LOG.trace("{} removing {}", logPrefix, event.v);
                trigger(new CB.CB_Broadcast(new GraphOperations.InternalOperation(GraphOperations.OpType.RemoveV, event.v)), cb);
                GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);
                gv.setValue("Set.receivedremoves", gv.getValue("Set.receivedremoves", Integer.class) + 1);
            }
        }
    };

    Handler handleRemoveE = new Handler<GraphOperations.RemoveE>() {
        @Override
        public void handle(GraphOperations.RemoveE event) {
            if(VA.contains(event.e.v1) && !VR.contains(event.e.v1)){
                trigger(new CB.CB_Broadcast(new GraphOperations.InternalOperation(GraphOperations.OpType.RemoveE, event.e)),cb);
            }
        }
    };



    /*
    INTERNAL
     */


    Handler handleInternal = new Handler<CB.CB_Deliver>() {
        @Override
        public void handle(CB.CB_Deliver event) {
            GraphOperations.InternalOperation temp;

            try {
                temp = (GraphOperations.InternalOperation) event.getContent();
                switch (temp.type) {
                    case Edge:
                        EA.add(temp.e);
                        LOG.trace("{} Adding: {}", logPrefix, temp.e);
                        break;
                    case Vertex:    //Add Vertex
                        VA.add(temp.v);
                        LOG.trace("{} Adding: {} ", logPrefix, temp.v);
                        break;
                    case RemoveE:
                        if(EA.contains(temp.e)){
                            ER.add(temp.e);
                        }
                        LOG.trace("{} Removing: {} ", logPrefix, temp.e);
                        break;
                    case RemoveV:
                        if(VA.contains(temp.v)){
                            VR.add(temp.v);
                        }
                        LOG.trace("{} Removing: {}", logPrefix);
                        break;
                }
            }catch(ClassCastException  e){
                LOG.debug("{}Got something strange", logPrefix);
            }
        }
    };
    public static class Init extends se.sics.kompics.Init<TwoP2PGraph> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
