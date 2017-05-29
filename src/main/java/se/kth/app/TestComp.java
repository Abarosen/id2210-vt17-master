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
    private int mode;

    Positive<Network> networkPort = requires(Network.class);

    public TestComp(Init init){
        selfAdr = init.selfAdr;
        mode = init.mode;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        subscribe(handleStart, control);
        subscribe(handleResponse, networkPort);
        subscribe(handleGResponse, networkPort);

    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
            KHeader header = new BasicHeader(selfAdr, selfAdr, Transport.UDP);

            if(mode != 3) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test"));
                trigger(msg, networkPort);
                //trigger(msg, networkPort);

                KContentMsg msg2 = new BasicContentMsg(header, new ExternalEvents.Add("Test2"));
                trigger(msg2, networkPort);

                KContentMsg msg3 = new BasicContentMsg(header, new ExternalEvents.Remove("Test"));
                trigger(msg3, networkPort);
            }else{

                KContentMsg msg = new BasicContentMsg(header, new GraphOperations.AddV(new Vertex("V1")));
                trigger(msg, networkPort);
                //trigger(msg, networkPort);

                KContentMsg msg2 = new BasicContentMsg(header, new GraphOperations.AddV(new Vertex("V2")));
                trigger(msg2, networkPort);

                KContentMsg msg3 = new BasicContentMsg(header, new GraphOperations.AddE(new Edge("V1", "V2")));
                trigger(msg3, networkPort);

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
        int mode;
        public Init(KAddress selfAdr, int mode) {
            this.selfAdr = selfAdr;
            this.mode = mode;
        }
    }
}
