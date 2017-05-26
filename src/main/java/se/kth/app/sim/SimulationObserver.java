package se.kth.app.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.kompics.timer.CancelPeriodicTimeout;
import se.sics.kompics.timer.SchedulePeriodicTimeout;
import se.sics.kompics.timer.Timeout;
import se.sics.kompics.timer.Timer;

import java.util.UUID;

public class SimulationObserver extends ComponentDefinition {

    private static final Logger LOG = LoggerFactory.getLogger(SimulationObserver.class);

    Positive<Timer> timer = requires(Timer.class);
    Positive<Network> network = requires(Network.class);

    private final int minPings;
    private final int minDeadNodes;
    private int timeouts = 0;

    private UUID timerId;
    GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);

    public SimulationObserver(Init init) {
        minPings = init.minPings;
        minDeadNodes = init.minDeadNodes;

        subscribe(handleStart, control);
        subscribe(handleCheck, timer);

        gv.setValue("simulation.pongs", 0);
        gv.setValue("GBEB.samplesize", 0);
        gv.setValue("GBEB.sentmessages", 0);
        gv.setValue("GBEB.receivedmessages", 0);
        gv.setValue("text", "cpbarn");
        LOG.info("simtext: {} ", gv.getValue("text", GlobalView.class));

        LOG.info("Starting Observer!");
    }

    Handler<Start> handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            schedulePeriodicCheck();
        }
    };

    @Override
    public void tearDown() {
        trigger(new CancelPeriodicTimeout(timerId), timer);
    }

    Handler<CheckTimeout> handleCheck = new Handler<CheckTimeout>() {
        @Override
        public void handle(CheckTimeout event) {
            //gv.setValue("simulation.pongs", 0);

            LOG.info("Amount of sent messages: {}", gv.getValue("GBEB.sentmessages", Integer.class));
            LOG.info("Amount of receieved messagaes: {}", gv.getValue("GBEB.receivedmessages", Integer.class));
            if(gv.getValue("GBEB.sentmessages", Integer.class) > 125 && gv.getValue("GBEB.receivedmessages", Integer.class) == gv.getValue("GBEB.sentmessages", Integer.class)) {
                //LOG.info("Terminating simulation as the minimum pings:{} is achieved", minPings);
                LOG.warn("Amount of sent messages match the amount of received messages: {}, {}", gv.getValue("GBEB.samplesize", Integer.class), gv.getValue("GBEB.sentmessages", Integer.class));
                gv.terminate();
            }
            /*if(gv.getValue("GBEB.sentmessages", Integer.class) > 10 && gv.getValue("GBEB.samplesize", Integer.class) == gv.getValue("GBEB.sentmessages", Integer.class)) {
                //LOG.info("Terminating simulation as the minimum pings:{} is achieved", minPings);
                LOG.warn("Amount of sent messages match the sample size: {}, {}", gv.getValue("GBEB.samplesize", Integer.class), gv.getValue("GBEB.sentmessages", Integer.class));
                gv.terminate();
            }*/
            if(gv.getDeadNodes().size() > minDeadNodes) {
                LOG.info("Terminating simulation as the min dead nodes:{} is achieved", minDeadNodes);
                gv.terminate();
            }
            if(++timeouts == 300) {
                LOG.info("Taking too long to terminate simulation, terminating");
                gv.terminate();
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<SimulationObserver> {

        public final int minPings;
        public final int minDeadNodes;

        public Init(int minPings, int minDeadNodes) {
            this.minPings = minPings;
            this.minDeadNodes = minDeadNodes;
        }
    }

    private void schedulePeriodicCheck() {
        long period = config().getValue("pingpong.simulation.checktimeout", Long.class);
        SchedulePeriodicTimeout spt = new SchedulePeriodicTimeout(period, period);
        CheckTimeout timeout = new CheckTimeout(spt);
        spt.setTimeoutEvent(timeout);
        trigger(spt, timer);
        timerId = timeout.getTimeoutId();
    }

    public static class CheckTimeout extends Timeout {

        public CheckTimeout(SchedulePeriodicTimeout spt) {
            super(spt);
        }
    }
}