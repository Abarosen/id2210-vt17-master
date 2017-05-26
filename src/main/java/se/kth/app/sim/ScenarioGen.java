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
import se.kth.sim.compatibility.SimNodeIdExtractor;
import se.kth.system.HostMngrComp;
import se.sics.kompics.Init;
import se.sics.kompics.network.Address;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.adaptor.Operation;
import se.sics.kompics.simulator.adaptor.Operation1;
import se.sics.kompics.simulator.adaptor.distributions.extra.BasicIntSequentialDistribution;
import se.sics.kompics.simulator.events.system.KillNodeEvent;
import se.sics.kompics.simulator.events.system.SetupEvent;
import se.sics.kompics.simulator.events.system.StartNodeEvent;
import se.sics.kompics.simulator.network.identifier.IdentifierExtractor;
import se.sics.kompics.simulator.util.GlobalView;
import se.sics.ktoolbox.omngr.bootstrap.BootstrapServerComp;
import se.sics.ktoolbox.util.network.KAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Alex Ormenisan <aaor@kth.se>
 */
public class ScenarioGen {
    private static final Logger LOG = LoggerFactory.getLogger(ScenarioGen.class);

    static Operation<SetupEvent> systemSetupOp = new Operation<SetupEvent>() {
        @Override
        public SetupEvent generate() {
            return new SetupEvent() {
                @Override
                public IdentifierExtractor getIdentifierExtractor() {
                    return new SimNodeIdExtractor();
                }
            };
        }
    };

    static Operation startObserverOp = new Operation<StartNodeEvent>() {
        @Override
        public StartNodeEvent generate() {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    selfAdr = ScenarioSetup.observer;
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    HashMap<String, Object> config = new HashMap<>();
                    config.put("pingpong.simulation.checktimeout", 1000);
                    return config;
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return SimulationObserver.class;
                }

                @Override
                public Init getComponentInit() {
                    return new SimulationObserver.Init(100, 2);
                }
            };
        }
    };

    static Operation<StartNodeEvent> startBootstrapServerOp = new Operation<StartNodeEvent>() {

        @Override
        public StartNodeEvent generate() {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    selfAdr = ScenarioSetup.bootstrapServer;
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return BootstrapServerComp.class;
                }

                @Override
                public BootstrapServerComp.Init getComponentInit() {
                    return new BootstrapServerComp.Init(selfAdr);
                }
            };
        }
    };

    static Operation1<StartNodeEvent, Integer> startNodeOp = new Operation1<StartNodeEvent, Integer>() {

        @Override
        public StartNodeEvent generate(final Integer nodeId) {
            return new StartNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = "193.0.0." + nodeId;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, nodeId);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public Class getComponentDefinition() {
                    return HostMngrComp.class;
                }

                @Override
                public HostMngrComp.Init getComponentInit() {
                    return new HostMngrComp.Init(selfAdr, ScenarioSetup.bootstrapServer, ScenarioSetup.croupierOId, 0);
                }

                @Override
                public Map<String, Object> initConfigUpdate() {
                    Map<String, Object> nodeConfig = new HashMap<>();
                    nodeConfig.put("system.id", nodeId);
                    nodeConfig.put("system.seed", ScenarioSetup.getNodeSeed(nodeId));
                    nodeConfig.put("system.port", ScenarioSetup.appPort);
                    return nodeConfig;
                }
            };
        }
    };

    static Operation1 killPongerOp = new Operation1<KillNodeEvent, Integer>() {
        @Override
        public KillNodeEvent generate(final Integer self) {
            return new KillNodeEvent() {
                KAddress selfAdr;

                {
                    String nodeIp = "193.0.0." + self;
                    selfAdr = ScenarioSetup.getNodeAdr(nodeIp, self);
                    LOG.warn("Nóde {} has been killed", self);
                }

                @Override
                public Address getNodeAddress() {
                    return selfAdr;
                }

                @Override
                public String toString() {
                    return "KillPonger<" + selfAdr.toString() + ">";
                }
            };
        }
    };



    public static SimulationScenario noChurn() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startObserver = new StochasticProcess() {
                    {
                        raise(1, startObserverOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(3, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };
                //Kills an amount of nodes.
                StochasticProcess killPonger = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        //The Second argument identifies the node to be killed
                        raise(3, killPongerOp, new BasicIntSequentialDistribution((1)));
                    }
                };

                systemSetup.start();
                startObserver.startAfterTerminationOf(1000, systemSetup);
                startBootstrapServer.startAfterTerminationOf(1000, startObserver);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                //killPonger.startAfterTerminationOf(3000, startPeers);
                terminateAfterTerminationOf(1000*1000, startPeers);
            }
        };
        return scen;
    }

    //Another scenario to test
    public static SimulationScenario churn() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startObserver = new StochasticProcess() {
                    {
                        raise(1, startObserverOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(3, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };
                //Kills an amount of nodes.
                StochasticProcess killPonger = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        //The Second argument identifies the node to be killed
                        raise(1, killPongerOp, new BasicIntSequentialDistribution((3)));
                    }
                };

                systemSetup.start();
                startObserver.startAfterTerminationOf(1000, systemSetup);
                startBootstrapServer.startAfterTerminationOf(1000, startObserver);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                killPonger.startAfterTerminationOf(10000, startPeers);
                terminateAfterTerminationOf(1000*1000, startPeers);
            }
        };
        return scen;
    }

    //Another scenario to test
    public static SimulationScenario churnRevive() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startObserver = new StochasticProcess() {
                    {
                        raise(1, startObserverOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(3, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };
                //Kills an amount of nodes.
                StochasticProcess killPonger = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        //The Second argument identifies the node to be killed
                        raise(1, killPongerOp, new BasicIntSequentialDistribution((1)));
                    }
                };
                StochasticProcess revivePinger = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, startNodeOp, new BasicIntSequentialDistribution(1));
                        LOG.info("Node 1 has been revived");
                    }
                };

                systemSetup.start();
                startObserver.startAfterTerminationOf(1000, systemSetup);
                startBootstrapServer.startAfterTerminationOf(1000, startObserver);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                killPonger.startAfterTerminationOf(20000, startPeers);
                revivePinger.startAfterTerminationOf(7000, killPonger);
                terminateAfterTerminationOf(1000*1000, startPeers);
            }
        };
        return scen;
    }

    public static SimulationScenario gset() {
        SimulationScenario scen = new SimulationScenario() {
            {
                StochasticProcess systemSetup = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, systemSetupOp);
                    }
                };
                StochasticProcess startObserver = new StochasticProcess() {
                    {
                        raise(1, startObserverOp);
                    }
                };
                StochasticProcess startBootstrapServer = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(1000));
                        raise(1, startBootstrapServerOp);
                    }
                };
                StochasticProcess startPeers = new StochasticProcess() {
                    {
                        eventInterArrivalTime(uniform(1000, 1100));
                        raise(3, startNodeOp, new BasicIntSequentialDistribution(1));
                    }
                };
                //Kills an amount of nodes.
                StochasticProcess killPonger = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        //The Second argument identifies the node to be killed
                        raise(1, killPongerOp, new BasicIntSequentialDistribution((1)));
                    }
                };
                StochasticProcess revivePinger = new StochasticProcess() {
                    {
                        eventInterArrivalTime(constant(0));
                        raise(1, startNodeOp, new BasicIntSequentialDistribution(1));
                        LOG.info("Node 1 has been revived");
                    }
                };
                systemSetup.start();
                startObserver.startAfterTerminationOf(1000, systemSetup);
                startBootstrapServer.startAfterTerminationOf(1000, startObserver);
                startPeers.startAfterTerminationOf(1000, startBootstrapServer);
                //killPonger.startAfterTerminationOf(20000, startPeers);
                //revivePinger.startAfterTerminationOf(7000, killPonger);
                terminateAfterTerminationOf(1000*1000, startPeers);
            }
        };
        return scen;
    }
}
