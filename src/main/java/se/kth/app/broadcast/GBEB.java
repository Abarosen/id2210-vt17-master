package se.kth.app.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.kth.croupier.util.CroupierHelper;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.croupier.event.CroupierSample;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;


import java.util.LinkedList;
import java.util.List;

/**
 * Created by Barosen on 2017-04-25.
 */
public class GBEB extends ComponentDefinition {
    private static final Logger LOG = LoggerFactory.getLogger(GBEB.class);
    private KAddress selfAdr;
    List<PastEntry> past;
    String logPrefix;


    //Ports
    Positive<Network> networkPort = requires(Network.class);
    Negative<GBEBPort> gbeb = provides(GBEBPort.class);
    Positive<CroupierPort> croupierPort = requires(CroupierPort.class);


    public GBEB(Init init){

        //TODO Limit size of datastructure?
        past = new LinkedList<PastEntry>();


        selfAdr = init.selfAdr;
        logPrefix = "<nid:" + selfAdr.getId() + ">";
        //logPrefix = " ";
        LOG.info("{} GBEB started", logPrefix);

        //subscribe to ports
        subscribe(handleRequest, networkPort);
        subscribe(handleResponse, networkPort);
        subscribe(handleCroupierSample, croupierPort);
        subscribe(handleBroadcast, gbeb);
    }

    Handler handleBroadcast = new Handler<GBEB_Broadcast>() {
        @Override
        public void handle(GBEB_Broadcast event) {
            past.add(new PastEntry(selfAdr, event.getContent()));
        }
    };

    Handler handleCroupierSample = new Handler<CroupierSample>() {
        @Override
        public void handle(CroupierSample croupierSample) {

            if (croupierSample.publicSample.isEmpty()) {
                return;
            }
            List<KAddress> sample = CroupierHelper.getSample(croupierSample);
            LOG.trace("{} samples({}) recieved", logPrefix, sample.size());
            for (KAddress peer : sample) {
                KHeader header = new BasicHeader(selfAdr, peer, Transport.UDP);
                KContentMsg msg = new BasicContentMsg(header, new HistoryRequest());
                trigger(msg, networkPort);
            }
        }
    };

    ClassMatchedHandler handleResponse =  new ClassMatchedHandler<HistoryResponse, KContentMsg<?, ?, HistoryResponse>>() {
        @Override
        public void handle(HistoryResponse content, KContentMsg<?, ?, HistoryResponse> container) {
            LOG.trace("{} handling response from {}", logPrefix, container.getHeader().getSource());
            for(PastEntry x : content.past){
                if(!past.contains(x)){
                    trigger(new GBEB_Deliver(x.content), gbeb);
                    past.add(x);
                }
            }
        }
    };

    ClassMatchedHandler handleRequest =  new ClassMatchedHandler<HistoryRequest, KContentMsg<?, ?, HistoryRequest>>() {
        @Override
        public void handle(HistoryRequest content, KContentMsg<?, ?, HistoryRequest> container) {
            LOG.trace("{} Recieved HistoryRequest from {}",logPrefix, container.getHeader().getSource());
            trigger(container.answer(new HistoryResponse(past)), networkPort);
        }
    };

    public class PastEntry {
        KAddress from;
        KompicsEvent content;
        PastEntry(KAddress from, KompicsEvent content){
            this.from = from;
            this.content = content;
        }
    }

    public static class HistoryRequest implements KompicsEvent{

        public HistoryRequest(){
        }
    }

    public static class HistoryResponse implements KompicsEvent{
        List<PastEntry> past;
        public HistoryResponse(List<PastEntry> past){
            this.past = past;
        }
    }

    public class GBEB_Update implements KompicsEvent{
        final List<KAddress> sample;
        public GBEB_Update(List<KAddress> sample){
            this.sample = sample;
        }
    }

    public static class GBEB_Deliver implements KompicsEvent{
        private final KompicsEvent content;
        public GBEB_Deliver(KompicsEvent content){
            this.content = content;
        }
        public KompicsEvent getContent() {
            return content;
        }
    }
    public static class GBEB_Broadcast implements KompicsEvent{
        private final KompicsEvent content;

        public GBEB_Broadcast(KompicsEvent content){
            this.content = content;
        }

        public KompicsEvent getContent() {
            return content;
        }
    }

    public static class Init extends se.sics.kompics.Init<GBEB> {

        public final KAddress selfAdr;

        public Init(KAddress selfAdr) {
            this.selfAdr = selfAdr;
        }
    }
}


