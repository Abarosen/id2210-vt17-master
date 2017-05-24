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
package se.kth.app.mngr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.*;
import se.kth.app.broadcast.*;
import se.kth.app.sets.GSet;
import se.kth.app.sets.ORSet.ORSet;
import se.kth.app.sets.CRDTPort;
import se.kth.app.sets.TwoPSet;
import se.kth.app.sets.graph.TwoP2PGraph;
import se.kth.croupier.util.NoView;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapClientComp;
import se.sics.ktoolbox.overlaymngr.OverlayMngrPort;
import se.sics.ktoolbox.overlaymngr.events.OMngrCroupier;
import se.sics.ktoolbox.util.identifiable.overlay.OverlayId;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdate;
import se.sics.ktoolbox.util.overlays.view.OverlayViewUpdatePort;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class AppMngrComp extends ComponentDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(BootstrapClientComp.class);
  private String logPrefix = "";
  //*****************************CONNECTIONS**********************************
  Positive<OverlayMngrPort> omngrPort = requires(OverlayMngrPort.class);
  //***************************EXTERNAL_STATE*********************************
  private ExtPort extPorts;
  private KAddress selfAdr;
  private OverlayId croupierId;
  private int mode;
  //***************************INTERNAL_STATE*********************************
  private Component appComp;
  //our Components
  private Component gbeb;
  private Component rb;
  private Component cb;
  private Component set;
  //TODO Add Additional Components

  //******************************AUX_STATE***********************************
  private OMngrCroupier.ConnectRequest pendingCroupierConnReq;
  //**************************************************************************

  public AppMngrComp(Init init) {
    selfAdr = init.selfAdr;

    logPrefix = "<nid:" + selfAdr.getId() + ">";
    LOG.info("{}initiating...{}", logPrefix, mode);

    extPorts = init.extPorts;
    croupierId = init.croupierOId;
    mode = init.mode;

    subscribe(handleStart, control);
    subscribe(handleCroupierConnected, omngrPort);
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting...", logPrefix);

      pendingCroupierConnReq = new OMngrCroupier.ConnectRequest(croupierId, false);
      trigger(pendingCroupierConnReq, omngrPort);
    }
  };

  Handler handleCroupierConnected = new Handler<OMngrCroupier.ConnectResponse>() {
    @Override
    public void handle(OMngrCroupier.ConnectResponse event) {
      LOG.info("{}overlays connected", logPrefix);
      connectAppComp();
      trigger(Start.event, appComp.control());
      trigger(new OverlayViewUpdate.Indication<>(croupierId, false, new NoView()), extPorts.viewUpdatePort);
    }
  };

  private void connectAppComp() {

    appComp = create(AppComp.class, new AppComp.Init(selfAdr, croupierId));
    gbeb = create(GBEB.class, new GBEB.Init(selfAdr));
    rb = create(RB.class, new RB.Init(selfAdr));
    cb = create(CB.class, new CB.Init(selfAdr));


    connect(appComp.getNegative(Timer.class), extPorts.timerPort, Channel.TWO_WAY);
    connect(appComp.getNegative(Network.class), extPorts.networkPort, Channel.TWO_WAY);
    connect(appComp.getNegative(CroupierPort.class), extPorts.croupierPort, Channel.TWO_WAY);

    //GBEB
    connect(gbeb.getPositive(GBEBPort.class), appComp.getNegative(GBEBPort.class), Channel.TWO_WAY);
    connect(gbeb.getNegative(Network.class), extPorts.networkPort, Channel.TWO_WAY);
    connect(gbeb.getNegative(CroupierPort.class), extPorts.croupierPort, Channel.TWO_WAY);
    trigger(Start.event, gbeb.control());

    //RB
    connect(rb.getNegative(GBEBPort.class), gbeb.getPositive(GBEBPort.class), Channel.TWO_WAY);
    trigger(Start.event, rb.control());

    //CB
    connect(cb.getPositive(CBPort.class), appComp.getNegative(CBPort.class), Channel.TWO_WAY);
    connect(cb.getNegative(RBPort.class), rb.getPositive(RBPort.class), Channel.TWO_WAY);
    trigger(Start.event, cb.control());

    /*Selection of Set to be used
    // 0 = Grow-Only Set
    // 1 = Two-Phase Set (2P-Set)
    // 2 = Observed-Removed Set (OR-Set)
    // 3 = 2P2P-Graph
    */
    if(mode == 0){
      set = create(GSet.class, new GSet.Init(selfAdr));
    }else if(mode == 1){
      set = create(TwoPSet.class, new TwoPSet.Init(selfAdr));
    }else if(mode == 2){
      set = create(ORSet.class, new ORSet.Init(selfAdr));
    }else if(mode == 3){
      set = create(TwoP2PGraph.class, new TwoP2PGraph.Init(selfAdr));
    }
    if(set != null) {
      connect(set.getPositive(CRDTPort.class), appComp.getNegative(CRDTPort.class), Channel.TWO_WAY);
      connect(set.getNegative(CBPort.class), cb.getPositive(CBPort.class), Channel.TWO_WAY);
      trigger(Start.event, set.control());
    }
  }

  public static class Init extends se.sics.kompics.Init<AppMngrComp> {

    public final ExtPort extPorts;
    public final KAddress selfAdr;
    public final OverlayId croupierOId;
    public final int mode; //Used to select which set to use

    public Init(ExtPort extPorts, KAddress selfAdr, OverlayId croupierOId, int mode) {
      this.extPorts = extPorts;
      this.selfAdr = selfAdr;
      this.croupierOId = croupierOId;
      this.mode = mode;
    }
  }

  public static class ExtPort {

    public final Positive<Timer> timerPort;
    public final Positive<Network> networkPort;
    public final Positive<CroupierPort> croupierPort;
    public final Negative<OverlayViewUpdatePort> viewUpdatePort;

    public ExtPort(Positive<Timer> timerPort, Positive<Network> networkPort, Positive<CroupierPort> croupierPort,
      Negative<OverlayViewUpdatePort> viewUpdatePort) {
      this.networkPort = networkPort;
      this.timerPort = timerPort;
      this.croupierPort = croupierPort;
      this.viewUpdatePort = viewUpdatePort;
    }
  }
}
