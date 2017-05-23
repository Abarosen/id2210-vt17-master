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
 *
 */

public class CB extends ComponentDefinition {
    Negative<CBPort> cb = provides(CBPort.class);
    Positive<RBPort> rb = requires(RBPort.class);


    private KAddress selfAdr;
    String logPrefix;

    private static final Logger LOG = LoggerFactory.getLogger(CB.class);

    private List<KompicsEvent> delivered;
    private List<Tuple> past;

    public CB(Init init) {

        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";
        delivered = new LinkedList<KompicsEvent>();
        past = new LinkedList<Tuple>();

        LOG.info("{} starting CB", logPrefix);

        subscribe(handleBroadcast, cb);
        subscribe(handleDelivery, rb);
    }

    class Tuple {
        KAddress addr;
        KompicsEvent event;
        Tuple(KAddress addr, KompicsEvent event){
            this.addr = addr;
            this.event = event;
        }

        @Override
        public boolean equals(Object obj){
            if(obj == null){
                return false;
            }
            if(!(obj instanceof Tuple)){
                return false;
            }
            Tuple other = (Tuple) obj;

            return this.event.equals(other.event);
        }

        @Override
        public String toString() {
            return event.toString();
        }
    }
    class Struct implements KompicsEvent{
        KAddress addr;
        List<Tuple> mpast;
        KompicsEvent event;
        Struct(KAddress addr, List<Tuple> mpast, KompicsEvent event){
            this.addr = addr;
            this.mpast = mpast;
            this.event = event;
        }
        public List<Tuple> getPast() {
            return this.mpast;
        }

        @Override
        public String toString() {
            return "Struct: " + event.toString() + ", mpast:" + mpast.toString();
        }
    }

    Handler handleBroadcast = new Handler<CB_Broadcast>() {
        @Override
        public void handle(CB_Broadcast event) {
            LOG.trace("{} broadcasting message", logPrefix);
            List<Tuple> oldpast = new LinkedList<Tuple>(past);
            trigger(new RB.RB_Broadcast(new Struct(selfAdr, oldpast, event.content)), rb);
            LOG.trace("{} nothing {}", logPrefix, past);
            past.add(new Tuple(selfAdr, event.getContent()));
        }
    };

    Handler handleDelivery = new Handler<RB.RB_Deliver>() {
        @Override
        public void handle(RB.RB_Deliver event) {
            Struct x;
            try{
                x = (Struct) event.content;
            }catch (ClassCastException e){
                LOG.warn("Received something strange");
                return;
            }
            LOG.trace("{} Check {}!", logPrefix, x.toString());
            if(!delivered.contains(x.event)){
                LOG.trace("x: " + x.event);
                for (Tuple t: x.getPast()){
                    LOG.trace("t: " + t.event.toString());
                    if(!delivered.contains(t.event)){
                        trigger(new CB_Deliver(t.event), cb);
                        delivered.add(t.event);

                        LOG.debug("{} Delivery Type 2, 1:{} 2:{}", logPrefix, x.event.hashCode(), t.event.hashCode());
                        if(!(past.contains(t))){
                            past.add(t);
                        }
                    }
                }
                LOG.debug("{} Delivery Type 1!", logPrefix);
                trigger(new CB_Deliver(x.event), cb);
                delivered.add(x.event);
                Tuple temp = new Tuple(x.addr, x.event);
                if(!past.contains(temp)){
                    past.add(temp);
                }
            }
        }
    };


    public static class CB_Broadcast implements KompicsEvent{
        final KompicsEvent content;
        public CB_Broadcast(KompicsEvent e){
            this.content = e;
        }

        public KompicsEvent getContent() {
            return content;
        }
    }

    public static class CB_Deliver implements KompicsEvent{
        private KompicsEvent content;
        public CB_Deliver(KompicsEvent e){
            this.content = e;
        }
        public KompicsEvent getContent() {
            return content;
        }
    }

    public static class Init extends se.sics.kompics.Init<CB> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}
