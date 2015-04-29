import java.util.*;

public class Taxi implements Comparable {
	private Station origin;
	private int maxSize;
	private double startTime;
	private double endTime;
	private double returnTime;
	private double emptyMiles;
	private List<Trip> trips;

	public Taxi(int tripSize) {
		maxSize = tripSize;
		startTime = Double.POSITIVE_INFINITY;
		endTime = Double.NEGATIVE_INFINITY;
		returnTime = Double.NEGATIVE_INFINITY;
		emptyMiles = 0;
		trips = new ArrayList<Trip>();
		origin = null;
	}

	public void assignTo(Station station) {
		if (origin == null)
			origin = station;
	}

	public void combine(Taxi t) {
		if (t.size() < this.size()) {
			System.out.println("size inconsistency");
			return;
		}
		for (Trip trip: t.trips()) {
			this.addTrip(trip);
		}
	}

	public void addTrip(Trip trip) {
		if (trip.size() > maxSize) return; // if trip size is too big for taxi...

		trips.add(trip);

		// update the start time of taxi, if needed
		if (trip.departTime() < startTime)
			startTime = trip.departTime();

		// update teh end time if taxi, if needed
		if (trip.arriveTime() > endTime)
			endTime = trip.arriveTime();
	}

	// update when this taxi will return to station
	public void updateReturn(double time) {
		if (time > returnTime)
			returnTime = time;
	}

	public void updateEmptyMiles(double miles) {
		emptyMiles += miles;
	}

	public Double startTime() {
		return (Double)startTime;
	}

	public int size() {
		return maxSize;
	}

	public Double endTime() {
		return (Double)endTime;
	}

	public Double returnTime() {
		return (Double)returnTime;
	}

	public boolean contains(Trip trip) {
		return trips.contains(trip);
	}

	//compare starttime with another taxi
	public int compareTo(Object obj) {
		Taxi other = (Taxi) obj;
		return ((Double)startTime).compareTo(other.startTime());
	}

	public String oStation() {
		return origin.center();
	}

	public List<Trip> trips() {
		return trips;
	}
}