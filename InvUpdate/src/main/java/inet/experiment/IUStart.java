package inet.experiment;

import microsim.engine.ExperimentBuilder;
import microsim.engine.SimulationEngine;
import microsim.gui.shell.MicrosimShell;
import inet.model.IUModel;
import inet.experiment.IUCollector;
import inet.experiment.IUObserver;

public class IUStart implements ExperimentBuilder {

	public static void main(String[] args) {
		boolean showGui = true;

		SimulationEngine engine = SimulationEngine.getInstance();
		MicrosimShell gui = null;
		if (showGui) {
			gui = new MicrosimShell(engine);
			gui.setVisible(true);
		}

		IUStart experimentBuilder = new IUStart();
		engine.setExperimentBuilder(experimentBuilder);

		engine.setup();
	}

	public void buildExperiment(SimulationEngine engine) {
		IUModel model = new IUModel();
		IUCollector collector = new IUCollector(model);
		IUObserver observer = new IUObserver(model, collector);

		engine.addSimulationManager(model);
		engine.addSimulationManager(collector);
		engine.addSimulationManager(observer);	
	}
}
