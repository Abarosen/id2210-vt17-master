package se.kth.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.sets.ExternalEvents;
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
 */
public class TestComp extends ComponentDefinition{
    private static final Logger LOG = LoggerFactory.getLogger(TestComp.class);
    private String logPrefix = " ";
    private KAddress selfAdr;
    private int mode;

    Positive<Network> networkPort = requires(Network.class);

    TestComp(Init init){
        selfAdr = init.selfAdr;
        mode = init.mode;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        subscribe(handleStart, control);
        subscribe(handleResponse, networkPort);


    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);

            //KHeader header = new BasicHeader(selfAdr, selfAdr, Transport.UDP);
            //KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
            //trigger(msg, networkPort);
        }
    };

    ClassMatchedHandler handleResponse
            = new ClassMatchedHandler<ExternalEvents.Response, KContentMsg<?, ?, ExternalEvents.Response>>() {

        @Override
        public void handle(ExternalEvents.Response content, KContentMsg<?, ?, ExternalEvents.Response> container) {

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
