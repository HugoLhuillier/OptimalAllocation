package inet.experiment;

import microsim.annotation.GUIparameter;
import microsim.data.DataExport;
import microsim.engine.AbstractSimulationCollectorManager;
import microsim.engine.SimulationManager;
import microsim.event.EventGroup;
import microsim.event.EventListener;
import microsim.event.Order;

import org.apache.log4j.Logger;

import inet.model.IUModel;

public class IUCollector extends AbstractSimulationCollectorManager implements EventListener {

	private final static Logger log = Logger.getLogger(IUCollector.class);

	@GUIparameter(description = "Toggle to export snapshot to .csv files")
	boolean exportToCSV = true;				//If true, data will be recorded to .csv files in the output directory

	@GUIparameter(description = "Toggle to export snapshot to output database")
	boolean exportToDatabase = true;		//If true, data will be recorded in the output database in the output directory

	@GUIparameter(description = "Set the time at which to start exporting snaphots to the database and/or .csv files")
	Double timeOfFirstSnapshot = 0.;

	@GUIparameter(description = "Set the time between snapshots to be exported to the database and/or .csv files")
	Double timestepsBetweenSnapshots = 1.;

	public IUCollector(SimulationManager manager) {
		super(manager);
	}

	//DataExport objects to handle exporting data to database and/or .csv files
	private DataExport exportAgentsFromDatabase;
	private DataExport exportAgentsCreated;

	// ---------------------------------------------------------------------
	// Manager methods
	// ---------------------------------------------------------------------

	public void buildObjects() {

//		exportAgentsFromDatabase = new DataExport(((IUModel) getManager()).getAgentsLoadedFromDatabase(), exportToDatabase, exportToCSV);
		exportAgentsCreated = new DataExport(((IUModel) getManager()).getAgentsCreated(), exportToDatabase, exportToCSV);
	}

	public void buildSchedule() {

			EventGroup eventGroup = new EventGroup();

			eventGroup.addEvent(this, Processes.DumpInfo);

			getEngine().getEventList().scheduleRepeat(eventGroup, timeOfFirstSnapshot, Order.AFTER_ALL.getOrdering()-1, timestepsBetweenSnapshots);

	}


	// ---------------------------------------------------------------------
	// EventListener
	// ---------------------------------------------------------------------

	public enum Processes {
		DumpInfo;
	}

	public void onEvent(Enum<?> type) {
		switch ((Processes) type) {

		case DumpInfo:

			//Export to database and/or .csv files
//			exportAgentsFromDatabase.export();
			exportAgentsCreated.export();

			break;

		}
	}


	// ---------------------------------------------------------------------
	// Own methods
	// ---------------------------------------------------------------------



	// ---------------------------------------------------------------------
	// Access methods
	// ---------------------------------------------------------------------

	public boolean isExportToCSV() {
		return exportToCSV;
	}

	public void setExportToCSV(boolean exportToCSV) {
		this.exportToCSV = exportToCSV;
	}

	public boolean isExportToDatabase() {
		return exportToDatabase;
	}

	public void setExportToDatabase(boolean exportToDatabase) {
		this.exportToDatabase = exportToDatabase;
	}

	public Double getTimeOfFirstSnapshot() {
		return timeOfFirstSnapshot;
	}

	public void setTimeOfFirstSnapshot(Double timeOfFirstSnapshot) {
		this.timeOfFirstSnapshot = timeOfFirstSnapshot;
	}

	public Double getTimestepsBetweenSnapshots() {
		return timestepsBetweenSnapshots;
	}

	public void setTimestepsBetweenSnapshots(Double timestepsBetweenSnapshots) {
		this.timestepsBetweenSnapshots = timestepsBetweenSnapshots;
	}

}