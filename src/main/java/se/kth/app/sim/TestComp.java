/*
 * 2016 Royal Institute of Technology (KTH)
 *
 * LSelector is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package se.kth.app.sim;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.mngr.AppMngrComp;
import se.kth.app.sets.ExternalEvents;
import se.sics.kompics.Channel;
import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.cc.heartbeat.CCHeartbeatPort;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapClientComp;
import se.sics.ktoolbox.overlaymngr.OverlayMngrComp;
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;
import se.sics.ktoolbox.util.network.nat.NatAwareAddress;
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class TestComp extends ComponentDefinition {

    private static final Logger
            LOG = LoggerFactory.getLogger(TestComp.class);
    private String logPrefix = " ";

    //*****************************CONNECTIONS**********************************
    Positive<Network> networkPort = requires(Network.class);
    //***************************EXTERNAL_STATE*********************************
    private KAddress selfAdr;
    private int mode;

    public TestComp(Init init) {
        selfAdr = init.selfAdr;
        mode = init.mode;
        logPrefix = "<nid:" + selfAdr.getId() + ">";
        LOG.info("{}initiating...", logPrefix);

        subscribe(handleStart, control);

        KHeader header = new BasicHeader(selfAdr, peer, Transport.UDP);
        KContentMsg msg = new BasicContentMsg(header, new ExternalEvents.Add("Hello"));
        trigger(msg, networkPort);
    }

    Handler handleStart = new Handler<Start>() {
        @Override
        public void handle(Start event) {
            LOG.info("{}starting...", logPrefix);
        }
    };

    public static class Init extends se.sics.kompics.Init<TestComp> {

        public final KAddress selfAdr;
        public final int mode;

        public Init(int mode) {
            selfAdr = ScenarioSetup.testId;
            this.mode = mode;
        }
    }
}