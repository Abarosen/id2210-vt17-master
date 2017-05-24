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
package se.kth.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.kth.app.broadcast.CB;
import se.kth.app.broadcast.CBPort;
import se.kth.app.broadcast.GBEBPort;
import se.kth.app.sets.CRDTPort;
import se.kth.app.test.Ping;
import se.kth.app.test.Pong;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.croupier.event.CroupierSample;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class AppComp extends ComponentDefinition {

  private static final Logger LOG = LoggerFactory.getLogger(AppComp.class);
  private String logPrefix = " ";

  //*******************************CONNECTIONS********************************
  Positive<Timer> timerPort = requires(Timer.class);
  Positive<Network> networkPort = requires(Network.class);
  Positive<CroupierPort> croupierPort = requires(CroupierPort.class);
  Positive<CBPort> cb = requires(CBPort.class);
  Positive<CRDTPort> setPort = requires(CRDTPort.class);


  Positive<GBEBPort> gbebPort = requires(GBEBPort.class);

  //**************************************************************************
  private KAddress selfAdr;
  private int counter = 0;

  public AppComp(Init init) {
    selfAdr = init.selfAdr;
    logPrefix = "<nid:" + selfAdr.getId() + ">";
    LOG.info("{}initiating...", logPrefix);


    //TODO Subscribe handlers

    subscribe(handleStart, control);

    subscribe(handleCroupierSample, croupierPort);
    subscribe(handlePing, networkPort);
    subscribe(handlePong, networkPort);
    subscribe(handleTest, cb);
  }

  Handler handleStart = new Handler<Start>() {
    @Override
    public void handle(Start event) {
      LOG.info("{}starting...", logPrefix);
    }
  };

  Handler handleCroupierSample = new Handler<CroupierSample>() {
    @Override
    public void handle(CroupierSample croupierSample) {
      if (croupierSample.publicSample.isEmpty()) {
        return;
      }
      //List<KAddress> sample = CroupierHelper.getSample(croupierSample);
      if(selfAdr.getId().toString().equals("1")){
        LOG.info("{} TESTING! counter:{}", logPrefix, counter);
        trigger(new CB.CB_Broadcast(new TestEvent("hello: " + counter)), cb);
        counter++;
      }

      /*for (KAddress peer : sample) {
        KHeader header = new BasicHeader(selfAdr, peer, Transport.UDP);
        KContentMsg msg = new BasicContentMsg(header, new Ping());
        trigger(msg, networkPort);
      }*/
    }
  };
  class TestEvent implements KompicsEvent{
    String message;
    TestEvent(String message){
      this.message = message;
    }
  }

  Handler handleTest = new Handler<CB.CB_Deliver>() {

    @Override
    public void handle(CB.CB_Deliver cbEvent) {
      TestEvent t = (TestEvent) cbEvent.getContent();
      LOG.info("{} test received {}", logPrefix, t.message);
    }
  };

  ClassMatchedHandler handlePing
    = new ClassMatchedHandler<Ping, KContentMsg<?, ?, Ping>>() {

      @Override
      public void handle(Ping content, KContentMsg<?, ?, Ping> container) {
        LOG.info("{}received ping from:{}", logPrefix, container.getHeader().getSource());
        trigger(container.answer(new Pong()), networkPort);
      }
    };

  ClassMatchedHandler handlePong
    = new ClassMatchedHandler<Pong, KContentMsg<?, KHeader<?>, Pong>>() {

      @Override
      public void handle(Pong content, KContentMsg<?, KHeader<?>, Pong> container) {
        LOG.info("{}received pong from:{}", logPrefix, container.getHeader().getSource());
      }
    };

  public static class Init extends se.sics.kompics.Init<AppComp> {

    public final KAddress selfAdr;
    public final Identifier gradientOId;

    public Init(KAddress selfAdr, Identifier gradientOId) {
      this.selfAdr = selfAdr;
      this.gradientOId = gradientOId;
    }
  }
}
