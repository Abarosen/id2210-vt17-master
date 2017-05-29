package se.kth.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.sets.ExternalEvents;
import se.kth.app.sets.graph.Edge;
import se.kth.app.sets.graph.GraphOperations;
import se.kth.app.sets.graph.Vertex;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;

/**
 * Created by Barosen on 2017-05-26.
 *
 */
public class TestComp extends ComponentDefinition{
    private static final Logger LOG = LoggerFactory.getLogger(TestComp.class);
    private String logPrefix = " ";
    private KAddress selfAdr;
    private int testSelect;

    Positive<Network> networkPort = requires(Network.class);

    public TestComp(Init init){
        selfAdr = init.selfAdr;
        testSelect = init.testSelect;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        subscribe(handleStart, control);
        subscribe(handleResponse, networkPort);
        subscribe(handleGResponse, networkPort);

    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting TestComp... ", logPrefix);

            KHeader header = new BasicHeader(selfAdr, selfAdr, Transport.UDP);
            KContentMsg add = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
            KContentMsg remove = new BasicContentMsg(header, new ExternalEvents.Remove("Test") );
            KContentMsg lookup = new BasicContentMsg(header, new ExternalEvents.Lookup("Test") );
            if(testSelect == 0) {
                //Add one message
            } else if(testSelect == 1) {
                trigger(add, networkPort);
                //Add one message and remove one message
            } else if(testSelect == 2) {
                trigger(add, networkPort);
                trigger(remove, networkPort);
                //Add two messages
            } else if(testSelect == 3) {
                trigger(add, networkPort);
                trigger(add, networkPort);
                //Add one message and do a lookup
            } else if(testSelect == 4) {
                trigger(add, networkPort);
                trigger(lookup, networkPort);
            }
        }
    };

    ClassMatchedHandler handleResponse
            = new ClassMatchedHandler<ExternalEvents.Response, KContentMsg<?, ?, ExternalEvents.Response>>() {

        @Override
        public void handle(ExternalEvents.Response content, KContentMsg<?, ?, ExternalEvents.Response> container) {
            LOG.info("{} Response received! Key: {}, Result: {}", logPrefix, content.key, content.res);
        }
    };

    ClassMatchedHandler handleGResponse
            = new ClassMatchedHandler<GraphOperations.Response, KContentMsg<?, ?, GraphOperations.Response>>() {

        @Override
        public void handle(GraphOperations.Response content, KContentMsg<?, ?, GraphOperations.Response> container) {
            LOG.info("{} Response received! Key: {}, Result: {}", logPrefix, content.key, content.response);
        }
    };

    public static class Init extends se.sics.kompics.Init<TestComp> {

        public final KAddress selfAdr;
        int testSelect;
        public Init(KAddress selfAdr, int testSelect) {
            this.selfAdr = selfAdr;
            this.testSelect = testSelect;
        }
    }
}
