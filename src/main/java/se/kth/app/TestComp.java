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

    public TestComp(Init init){
        selfAdr = init.selfAdr;
        mode = init.mode;
        logPrefix = "<nid:" + selfAdr.getId() + ">";

        subscribe(handleStart, control);
        subscribe(handleResponse, networkPort);

    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting TestComp... ", logPrefix);

            KHeader header = new BasicHeader(selfAdr, selfAdr, Transport.UDP);
            if(mode == 0) {

            } else if(mode == 1) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                trigger(msg, networkPort);
            } else if(mode == 2) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                KContentMsg removeMsg = new BasicContentMsg(header, new ExternalEvents.Remove("Test") );
                trigger(msg, networkPort);
                trigger(removeMsg, networkPort);
            } else if(mode == 3) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                KContentMsg addMsg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                trigger(msg, networkPort);
                trigger(addMsg, networkPort);
            } else if(mode == 4) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                trigger(msg, networkPort);
                KContentMsg lookup = new BasicContentMsg(header, new ExternalEvents.Lookup("Test") );
                trigger(lookup, networkPort);
            } else if(mode == 5) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                KContentMsg removeMsg = new BasicContentMsg(header, new ExternalEvents.Remove("Test") );
                trigger(msg, networkPort);
                trigger(removeMsg, networkPort);
            } else if(mode == 6) {
                KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Test") );
                KContentMsg removeMsg = new BasicContentMsg(header, new ExternalEvents.Remove("Test") );
                trigger(msg, networkPort);
                trigger(removeMsg, networkPort);
            }
        }
    };

    ClassMatchedHandler handleResponse
            = new ClassMatchedHandler<ExternalEvents.Response, KContentMsg<?, ?, ExternalEvents.Response>>() {

        @Override
        public void handle(ExternalEvents.Response content, KContentMsg<?, ?, ExternalEvents.Response> container) {
            if(content.res == true) {
                LOG.warn("I GOT IT");
            }
            LOG.info("{} Response received!");
        }
    };

    public static class Init extends se.sics.kompics.Init<TestComp> {

        public final KAddress selfAdr;
        int mode;
        int setTestMode;
        public Init(KAddress selfAdr, int mode) {
            this.selfAdr = selfAdr;
            this.mode = mode;
        }
    }
}
