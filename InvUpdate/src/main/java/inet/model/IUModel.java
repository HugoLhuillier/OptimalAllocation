package inet.model;

import microsim.engine.AbstractSimulationManager;
import microsim.annotation.GUIparameter;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;
import microsim.event.SystemEventType;
import inet.data.Parameters;

import java.util.ArrayList;
import java.util.List;

public class IUModel extends AbstractSimulationManager implements EventListener {

	@GUIparameter(description = "Set the number of agents to create")
	Integer numberOfAgents = 1;

	@GUIparameter(description = "Set the time at which the simulation will terminate")
	Double endTime = 20.;
	
	@GUIparameter(description = "Price mark up over the marginal cost (c=1)")
	Double pMarkUp = 1.2; // price of the consumption good
	
	static double r = 0.02; // int. rate on the debt 

	private List<CFirm> cFirms;

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {
		// load the parameters of the model 
		Parameters.calibration();
		// create the agents
		cFirms = new ArrayList<CFirm>();
		for(int i=0; i < numberOfAgents; i++) {
			CFirm cFirm = new CFirm(this, i);
			cFirms.add(cFirm);
		}

	}

	public void buildSchedule() {
		EventGroup eventGroup = new EventGroup();

		 eventGroup.addEvent(this, Processes.Update);
		 eventGroup.addCollectionEvent(cFirms, CFirm.Processes.Update);
		 eventGroup.addCollectionEvent(cFirms, CFirm.Processes.Inv);		
		getEngine().getEventList().scheduleRepeat(eventGroup, 0., 0, 1.); 

		getEngine().getEventList().scheduleSystem(endTime, Order.AFTER_ALL.getOrdering(), 0., getEngine(), SystemEventType.Stop);
	
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
			update();
			break;
		}
	}
	
	public void update(){
	}
	
	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public Double getEndTime() {
		return endTime;
	}

	public void setEndTime(Double endTime) {
		this.endTime = endTime;
	}

	public Integer getNumberOfAgents() {
		return numberOfAgents;
	}

	public void setNumberOfAgents(Integer numberOfAgents) {
		this.numberOfAgents = numberOfAgents;
	}

	public List<CFirm> getAgentsCreated() {
		return cFirms;
	}

	public void setAgentsCreated(List<CFirm> agentsCreated) {
		this.cFirms = agentsCreated;
	}

	public List<CFirm> getcFirms() {
		return cFirms;
	}

	public void setcFirms(List<CFirm> cFirms) {
		this.cFirms = cFirms;
	}

	public Double getpMarkUp() {
		return pMarkUp;
	}

	public void setpMarkUp(Double pMarkUp) {
		this.pMarkUp = pMarkUp;
	}

}