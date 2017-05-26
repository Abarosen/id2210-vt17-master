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
import se.kth.app.sets.CRDTPort;
import se.kth.app.sets.ExternalEvents;
import se.kth.app.sets.graph.GraphOperations;
import se.sics.kompics.*;
import se.sics.kompics.network.Network;
import se.sics.kompics.network.Transport;
import se.sics.kompics.timer.Timer;
import se.sics.ktoolbox.croupier.CroupierPort;
import se.sics.ktoolbox.croupier.event.CroupierSample;
import se.sics.ktoolbox.util.identifiable.Identifier;
import se.sics.ktoolbox.util.network.KAddress;
import se.sics.ktoolbox.util.network.KContentMsg;
import se.sics.ktoolbox.util.network.KHeader;
import se.sics.ktoolbox.util.network.basic.BasicContentMsg;
import se.sics.ktoolbox.util.network.basic.BasicHeader;

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



  //**************************************************************************
  private KAddress selfAdr;
  private int counter = 0;

  public AppComp(Init init) {
    selfAdr = init.selfAdr;
    logPrefix = "<nid:" + selfAdr.getId() + ">";
    LOG.info("{}initiating...", logPrefix);



    subscribe(handleStart, control);

    subscribe(handleCroupierSample, croupierPort);



    //Set handlers
    subscribe(handleResponse, setPort);
    subscribe(handleAdd, networkPort);
    subscribe(handleRemove, networkPort);
    subscribe(handleLookup, networkPort);

    //Graph
    subscribe(handleGResponse, setPort);
    subscribe(handleAddE, networkPort);
    subscribe(handleAddV, networkPort);
    subscribe(handleRemoveV, networkPort);
    subscribe(handleRemoveE, networkPort);

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
        //LOG.info("{} TESTING! counter:{}", logPrefix, counter);
        //trigger(new CB.CB_Broadcast(new TestEvent("hello: " + counter)), cb);
        //counter++;
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


  //Handle Set

  ClassMatchedHandler handleLookup
          = new ClassMatchedHandler<ExternalEvents.Lookup, KContentMsg<?, ?, ExternalEvents.Lookup>>() {

    @Override
    public void handle(ExternalEvents.Lookup content, KContentMsg<?, ?, ExternalEvents.Lookup> container) {
      LOG.info("{}received Lookup from:{}", logPrefix, container.getHeader().getSource());
      trigger(new ExternalEvents.Lookup(container.getHeader().getSource(), content.key), setPort);
    }
  };

  Handler handleResponse = new Handler<ExternalEvents.Response>() {
    @Override
    public void handle(ExternalEvents.Response event) {
      KHeader header = new BasicHeader(selfAdr, event.ret, Transport.UDP);
      KContentMsg msg = new BasicContentMsg(header, event);
      trigger(msg, networkPort);
    }
  };

  ClassMatchedHandler handleRemove
    = new ClassMatchedHandler<ExternalEvents.Remove, KContentMsg<?, ?, ExternalEvents.Remove>>() {

      @Override
      public void handle(ExternalEvents.Remove content, KContentMsg<?, ?, ExternalEvents.Remove> container) {
        LOG.trace("{}received Remove from {}", logPrefix, container.getHeader().getSource());
        trigger(content, setPort);
      }
    };


  ClassMatchedHandler handleAdd
          = new ClassMatchedHandler<ExternalEvents.Add, KContentMsg<?, ?, ExternalEvents.Add>>() {

    @Override
    public void handle(ExternalEvents.Add content, KContentMsg<?, ?, ExternalEvents.Add> container) {
      LOG.trace("{}received Add from {}", logPrefix, container.getHeader().getSource());
      trigger(content, setPort);
    }
  };

  //Graph

  Handler handleGResponse = new Handler<GraphOperations.Response>() {
    @Override
    public void handle(GraphOperations.Response event) {
      KHeader header = new BasicHeader(selfAdr, event.ret, Transport.UDP);
      KContentMsg msg = new BasicContentMsg(header, event);
      trigger(msg, networkPort);
    }
  };

  ClassMatchedHandler handleRemoveE
          = new ClassMatchedHandler<GraphOperations.RemoveE, KContentMsg<?, ?, GraphOperations.RemoveE>>() {

    @Override
    public void handle(GraphOperations.RemoveE content, KContentMsg<?, ?, GraphOperations.RemoveE> container) {
      LOG.trace("{}received RemoveEdge from {}", logPrefix, container.getHeader().getSource());
      trigger(content, setPort);
    }
  };

  ClassMatchedHandler handleRemoveV
          = new ClassMatchedHandler<GraphOperations.RemoveV, KContentMsg<?, ?, GraphOperations.RemoveV>>() {

    @Override
    public void handle(GraphOperations.RemoveV content, KContentMsg<?, ?, GraphOperations.RemoveV> container) {
      LOG.trace("{}received RemoveVertex from {}", logPrefix, container.getHeader().getSource());
      trigger(content, setPort);
    }
  };

  ClassMatchedHandler handleAddE
          = new ClassMatchedHandler<GraphOperations.AddE, KContentMsg<?, ?, GraphOperations.AddE>>() {

    @Override
    public void handle(GraphOperations.AddE content, KContentMsg<?, ?, GraphOperations.AddE> container) {
      LOG.trace("{}received AddEdse from {}", logPrefix, container.getHeader().getSource());
      trigger(content, setPort);
    }
  };

  ClassMatchedHandler handleAddV
          = new ClassMatchedHandler<GraphOperations.AddV, KContentMsg<?, ?, GraphOperations.AddV>>() {

    @Override
    public void handle(GraphOperations.AddV content, KContentMsg<?, ?, GraphOperations.AddV> container) {
      LOG.trace("{}received AddV from {}", logPrefix, container.getHeader().getSource());
      trigger(content, setPort);
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
