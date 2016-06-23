package inet.experiment;

import microsim.annotation.GUIparameter;
import microsim.engine.AbstractSimulationObserverManager;
import microsim.engine.SimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;

import org.apache.log4j.Logger;

public class IUObserver extends AbstractSimulationObserverManager implements EventListener {

	private final static Logger log = Logger.getLogger(IUObserver.class);

	@GUIparameter(description = "Set a regular time for any charts to update")
	Double chartUpdatePeriod = 1.;

	public IUObserver(SimulationManager manager, SimulationCollectorManager collectorManager) {
		super(manager, collectorManager);
	}


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		Update;
	}
	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

		case Update:

			break;
		}
	}

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

	}

	public void buildSchedule() {
		EventGroup eventGroup = new EventGroup();

		eventGroup.addEvent(this, Processes.Update);
		getEngine().getEventList().scheduleRepeat(eventGroup, 0., Order.AFTER_ALL.getOrdering()-1, chartUpdatePeriod);

	}

	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------



	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public Double getChartUpdatePeriod() {
		return chartUpdatePeriod;
	}

	public void setChartUpdatePeriod(Double chartUpdatePeriod) {
		this.chartUpdatePeriod = chartUpdatePeriod;
	}

}
