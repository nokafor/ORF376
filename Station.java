import java.util.*;

public class Station {
	int cx; // xPixel around which station is formed
	int cy; // yPixel around which station is formed

	List<String> pixels;
	TreeSet<Taxi> dTaxis; // taxis that depart from this station, and must be returned at the end of the day
	TreeSet<Taxi> aTaxis; // taxis that arrive to this station at any point in time

	Map<String, List<Double>> arrivals;
	Map<String, List<Double>> departures;

	public Station(int i, int j) {
		cx = i;
		cy = j;
		dTaxis = new TreeSet<Taxi>();
		aTaxis = new TreeSet<Taxi>();
		pixels = new ArrayList<String>();
		pixels.add(new String(i + ", " + j));

		arrivals = new HashMap<String, List<Double>>();
		departures = new HashMap<String, List<Double>>();
	}

	public void cycleDepartures() {
		TreeMap<Double, TreeSet<Taxi>> returnTimes = new TreeMap<Double, TreeSet<Taxi>>();
		TreeSet<Taxi> fTaxis = new TreeSet<Taxi>();

		// at the beginning, each taxi only has one trip, so update all the return times and add them to list
		for (Taxi d : dTaxis) {
			// if taxi is already being used by another county on its return
			if (d.returnTime() > Double.NEGATIVE_INFINITY) {
				TreeSet<Taxi> returns = returnTimes.get(d.returnTime());
				if (returns == null) {
					returns = new TreeSet<Taxi>();
				}
				returns.add(d);
				returnTimes.put(d.returnTime(), returns);
			}

			// if taxis return hasnt been accounted for yet
			else {
				List<Trip> trips = d.trips();
				Trip trip = trips.get(0);

				// return time = arrival time + the time it takes to make the trip back
				double returnTime = d.endTime() + (60 * 1.2 * trip.tripMiles() / 30);
				
				// update the departure taxi
				d.updateReturn(returnTime);
				d.updateEmptyMiles(trip.tripMiles());

				// update list of return times
				TreeSet<Taxi> returns = returnTimes.get(d.returnTime());
				if (returns == null) {
					returns = new TreeSet<Taxi>();
				}
				returns.add(d);
				returnTimes.put(d.returnTime(), returns);
			}
		}

		// go through departing taxis in ascending order
		for (Taxi d : dTaxis) {

			// find the times <=  this taxis departure time
			NavigableMap<Double, TreeSet<Taxi>> times = returnTimes.headMap(d.startTime(), true);
			Set<Double> retTimes = times.descendingKeySet();

			Taxi current = null; // marker variable

			// go through each time in descending order
			for (double retTime : retTimes) {
				// find the taxi associated with return time
				NavigableSet<Taxi> retTaxis = returnTimes.get(retTime);
				retTaxis = retTaxis.descendingSet();

				for (Taxi t : retTaxis) {
					// find a taxi that is the proper size
					if (t.size() >= d.size()) {
						d.combine(t); // combine the taxis
						current = t; // mark this taxi
						fTaxis.remove(t); // remove combined taxi from final list of departing taxis
						break;
					}
				}

				if (current != null) break;
			}

			// if a returning taxi was combined
			if (current != null) {
				TreeSet<Taxi> currentTaxis = returnTimes.get(current.returnTime());
				currentTaxis.remove(current);

				if (currentTaxis.size() == 0) returnTimes.remove(current.returnTime());
			}

			// add this taxi to the final list of departing taxis
			fTaxis.add(d);

			// update list of return times
			TreeSet<Taxi> rTaxis;
			if (returnTimes.containsKey(d.returnTime()))
				rTaxis = returnTimes.get(d.returnTime());
			else
				rTaxis = new TreeSet<Taxi>();

			rTaxis.add(d);
			returnTimes.put(d.returnTime(), rTaxis);
			
		}
	}

	private Double getAcceptableArrival(String pixel, double dTime, double offset) {
		List<Double> aTimes = arrivals.get(pixel);

		for (double time : aTimes) {
			// if within the acceptable range
			if (time <= dTime && time >= (dTime - offset)) { 
				return time;
			}
		}

		return null;
	}

	public List<Station> minimizeDepartures(List<Station> countyStations) {
		// offset = 600 - timeTo station
		Set<String> dPixels = departures.keySet();
		for (String pixel : dPixels) {
			if (arrivals.containsKey(pixel)) {
				for (double dTime : departures.get(pixel)) {
					Double time = getAcceptableArrival(pixel, dTime, 600);

					// if an acceptable arrival has been found
					if (time != null) {
						// delete the arrival time from this station, so that it won't be found again
						List<Double> aTimes = arrivals.get(pixel);
						aTimes.remove(time);
						arrivals.put(pixel, aTimes);

						//find departing taxi to be deleted
						Taxi d = null;
						for (Taxi taxi : dTaxis) {
							if (taxi.startTime() == dTime) {
								d = taxi;
								break;
							}
						}
						if (d == null) continue;

						// if intrapixel trip
						String[] coord = pixel.split(", ");
						if (countyStations.contains(new Station(Integer.parseInt(coord[0]), Integer.parseInt(coord[1])))) {
							// find endpoint station
							Station dest = countyStations.get(countyStations.indexOf(new Station(Integer.parseInt(coord[0]), Integer.parseInt(coord[1]))));

							// delete the previously expected arrival time in the endpoint station (may or may not delete this)
							List<Double> times = dest.arrivals.get(new String(cx + ", " + cy));
							//if (times != null) {
								times.remove(d.endTime());
								dest.arrivals.put(pixel, times);
							//}

							// delete previously expected arrival taxi in endpoint station
							Taxi a = null;
							for (Taxi aTaxi : dest.aTaxis) {
								if (aTaxi.startTime() == dTime) {
									a = aTaxi;
									break;
								}
							}
							dest.aTaxis.remove(a);

							// find the dTaxi that corresponds with arrival to this station
							a = null;
							for (Taxi dTaxi : dest.dTaxis) {
								if (dTaxi.endTime() == time) {
									a = dTaxi;
									break;
								}
							}

							// update the taxis return time
							if (a != null) a.updateReturn(d.endTime());

						}

						// delete the departure in this station
						dTaxis.remove(d);
					}
				}
			}
			//System.out.println("checkpoint");
		}
		return countyStations;
	}

	public boolean withinRange(int i, int j) {
		// if (i < 0 || j < 0) throw new OutOfBoundsException();
		double cartesianDistance = Math.sqrt(Math.pow(cx-i, 2) + Math.pow(cy-j, 2));
		double time = 60 * 1.2 * cartesianDistance / 30;

		return (time <= 2.5); // radius of circle = 2.5min, so that diameter = 5min
	}

	public boolean withinRange(Station that) {
		return withinRange(that.cx, that.cy);
	}

	public boolean contains(int i, int j) {
		String pixel = i + ", " + j;

		return pixels.contains(pixel);
	}

	public boolean equals(Object obj) {
		if (obj instanceof Station) {
			Station that = (Station) obj;
			return (cx == that.cx && cy == that.cy);
		}
		return false;
	}

	// make sure to check that it is within range, before pixel gets added to group
	public void add(int i, int j) {
		String pixel = i + ", " + j;
		pixels.add(pixel);
	}

	public void add(String pixel) {
		pixels.add(pixel);
	}

	public void addDeparture(Taxi taxi, String pixel) {
		dTaxis.add(taxi);

		List<Double> dTimes;
		if (departures.containsKey(pixel))
			dTimes = departures.get(pixel);
		else
			dTimes = new ArrayList<Double>();

		dTimes.add(taxi.startTime());

		departures.put(pixel, dTimes);

	}

	public void addArrival(Taxi taxi, String pixel) {
		aTaxis.add(taxi);

		List<Double> aTimes;
		if (arrivals.containsKey(pixel))
			aTimes = arrivals.get(pixel);
		else
			aTimes = new ArrayList<Double>();

		aTimes.add(taxi.endTime());

		arrivals.put(pixel, aTimes);
	}

	public int size() {
		return pixels.size();
	}

	public String center() {
		return new String(cx + ", " + cy);
	}

	public static void main(String[] args) {
		Station test = new Station(3, 4);

		System.out.println(test.withinRange(Integer.parseInt(args[0]), Integer.parseInt(args[1])));
		System.out.println(test.withinRange(3+1, 4));
		System.out.println(test.withinRange(3, 4+1));
		System.out.println(test.withinRange(3, 4-1));
		System.out.println(test.withinRange(3-1, 4));
	}
}