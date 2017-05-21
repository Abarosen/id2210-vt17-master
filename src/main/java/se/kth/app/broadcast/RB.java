package se.kth.app.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.*;
import se.sics.ktoolbox.util.network.KAddress;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Barosen on 2017-05-03.
 *
 * Eager reliable broadcast.
 */

public class RB extends ComponentDefinition {
    Negative<RBPort> rb = provides(RBPort.class);
    Positive<GBEBPort> gbeb = requires(GBEBPort.class);


    private KAddress selfAdr;
    String logPrefix;

    private static final Logger LOG = LoggerFactory.getLogger(RB.class);

    private List<KompicsEvent> delivered;

    public RB(Init init) {
        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";
        delivered = new LinkedList<>();
        LOG.trace("{} starting RB", logPrefix);
        subscribe(handleBroadcast, rb);
        subscribe(handleDelivery, gbeb);
    }

    Handler handleBroadcast = new Handler<RB_Broadcast>() {
        @Override
        public void handle(RB_Broadcast event) {
            LOG.trace("{} broadcasting message", logPrefix);
            trigger(new GBEB.GBEB_Broadcast(event.getContent()), gbeb);
        }
    };

    Handler handleDelivery = new Handler<GBEB.GBEB_Deliver>() {
        @Override
        public void handle(GBEB.GBEB_Deliver event) {
            if(!delivered.contains(event.getContent())){
                //LOG.info("{} delivering!" + delivered, logPrefix);
                delivered.add(event.getContent());
                trigger(new RB_Deliver(event.getContent()), rb);
                trigger(new GBEB.GBEB_Broadcast(event.getContent()), gbeb);
            }
        }
    };


    public static class RB_Broadcast implements KompicsEvent{
        final KompicsEvent content;
        public RB_Broadcast(KompicsEvent e){
            this.content = e;
        }

        public KompicsEvent getContent() {
            return content;
        }
    }

    public static class RB_Deliver implements KompicsEvent{
        KompicsEvent content;
        public RB_Deliver(KompicsEvent e){
            this.content = e;
        }
    }

    public static class Init extends se.sics.kompics.Init<RB> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
