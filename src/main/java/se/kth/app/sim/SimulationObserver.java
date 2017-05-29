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

    private final int minMessages;
    private final int minDeadNodes;
    private int timeouts = 0;

    private UUID timerId;
    GlobalView gv = config().getValue("simulation.globalview", GlobalView.class);

    public SimulationObserver(Init init) {
        minMessages = init.minMessages;
        minDeadNodes = init.minDeadNodes;

        subscribe(handleStart, control);
        subscribe(handleCheck, timer);

        gv.setValue("simulation.pongs", 0);
        gv.setValue("GBEB.samplesize", 0);
        gv.setValue("GBEB.sentmessages", 0);
        gv.setValue("GBEB.receivedmessages", 0);
        gv.setValue("Set.receivedadds", 0);
        gv.setValue("Set.receivedremoves", 0);
        gv.setValue("ORSet.internaladds", 0);
        gv.setValue("ORSet.internalremoves", 0);
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
            LOG.info("Amount of receieved adds: {}", gv.getValue("Set.receivedadds", Integer.class));
            LOG.info("Amount of receieved remove: {}", gv.getValue("Set.receivedremoves", Integer.class));
            LOG.info("Amount of internal ORSet adds: {}", gv.getValue("ORSet.internaladds", Integer.class));
            LOG.info("Amount of internal ORSet removes: {}", gv.getValue("ORSet.internalremoves", Integer.class));
            if(gv.getValue("Set.receivedadds", Integer.class) == 3) {
                LOG.warn("Received 3 adds, exiting...");
                //gv.terminate();
            }
            if(gv.getValue("Set.receivedremoves", Integer.class) == 3) {
                LOG.info("Received 3 removes");
                if(gv.getValue("Set.receivedadds", Integer.class) == gv.getValue("Set.receivedremoves", Integer.class)) {
                    LOG.warn("Received 3 adds and 3 removes, exiting...");
                    //gv.terminate();
                }
            }
            if(gv.getValue("GBEB.sentmessages", Integer.class) > 100 && gv.getValue("GBEB.receivedmessages", Integer.class) == gv.getValue("GBEB.sentmessages", Integer.class)) {
                LOG.warn("Amount of sent messages match the amount of received messages: {}, {}", gv.getValue("GBEB.samplesize", Integer.class), gv.getValue("GBEB.sentmessages", Integer.class));
                //gv.terminate();
            }
            if(gv.getValue("GBEB.sentmessages", Integer.class) > 10 && gv.getValue("GBEB.samplesize", Integer.class) == gv.getValue("GBEB.sentmessages", Integer.class)) {
                //LOG.info("Terminating simulation as the minimum pings:{} is achieved", minPings);
                LOG.warn("Amount of sent messages match the sample size: {}, {}", gv.getValue("GBEB.samplesize", Integer.class), gv.getValue("GBEB.sentmessages", Integer.class));
                //gv.terminate();
            }
            if(gv.getValue("ORSet.internaladds", Integer.class) >= 3 && gv.getValue("ORSet.internaladds", Integer.class) == gv.getValue("ORSet.internalremoves", Integer.class)) {
                LOG.warn("Amount of internal adds match the amount of internal removes: {}, {}", gv.getValue("ORSet.internaladds", Integer.class), gv.getValue("ORSet.internalremoves", Integer.class));
            }
            if(gv.getDeadNodes().size() > minDeadNodes) {
                LOG.warn("Terminating simulation as the min dead nodes:{} is achieved", minDeadNodes);
                //gv.terminate();
            }
            if(++timeouts == 300) {
                LOG.warn("Taking too long to terminate simulation, terminating");
                gv.terminate();
            }
        }
    };

    public static class Init extends se.sics.kompics.Init<SimulationObserver> {

        public final int minMessages;
        public final int minDeadNodes;

        public Init(int minMessages, int minDeadNodes) {
            this.minMessages = minMessages;
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