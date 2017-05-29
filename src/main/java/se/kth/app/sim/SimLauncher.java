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

import se.kth.app.sets.ORSet.ORSet;
import se.sics.kompics.network.test.NetworkTest;
import se.sics.kompics.simulator.SimulationScenario;
import se.sics.kompics.simulator.run.LauncherComp;

public class SimLauncher {
    public static void main(String[] args) {
        SimulationScenario.setSeed(ScenarioSetup.scenarioSeed);
        //SimulationScenario simpleBootScenario = ScenarioGen.simpleBoot();
        //simpleBootScenario.simulate(LauncherComp.class);

        //SimulationScenario churnBootScensario = ScenarioGen.churn();
        //churnBootScensario.simulate(LauncherComp.class);

        //SimulationScenario churnReviveScenario = ScenarioGen.churnRevive();
        //churnReviveScenario.simulate(LauncherComp.class);

        //SimulationScenario gsetScenario = ScenarioGen.gset();
        //gsetScenario.simulate(LauncherComp.class);

        //SimulationScenario twopsetScenario = ScenarioGen.twoPSet();
        //twopsetScenario.simulate(LauncherComp.class);

        //SimulationScenario ORSetScenario = ScenarioGen.ORSet();
        //ORSetScenario.simulate(LauncherComp.class);

        SimulationScenario twoP2PScenario = ScenarioGen.twoP2PSet();
        twoP2PScenario.simulate(LauncherComp.class);
    }
}
