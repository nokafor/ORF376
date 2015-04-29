import java.util.*;
import java.io.*;

public class Analysis {
	private static List<Station> countyStations = new ArrayList<Station>();
	private static int count = 0;
	private static boolean[][] visited;
	private static int X;
	private static int Y;

	public static void main(String[] args) {
		getDepartures(args[0]);
		getArrivals(args[1]);
		System.out.println(countyStations.size());

		// Station first = countyStations.get(0);
		// System.out.println(first.center());

		// //System.out.println(first.departures.size());
		// System.out.println(first.dTaxis.size());
		// System.out.println(first.aTaxis.size());

		// Set<String> dPixels = first.departures.keySet();
		// Set<String> aPixels = first.arrivals.keySet();

		// System.out.println("departures");
		// for (String pixel : dPixels) System.out.println(pixel);
		// System.out.println();

		// System.out.println("arrivals");
		// for (String pixel : aPixels) System.out.println(pixel);
		// System.out.println();

		// make a copy of all the stations
		List<Station> stations = new ArrayList<Station>();
		stations.addAll(countyStations);

		int count = 1;
		int totalTaxis = 0;
		for (Station station : stations) {
			System.out.println("checkpoint #" + count);
			int taxis = station.dTaxis.size();
			// System.out.println("Before: " + taxis);
			countyStations = station.minimizeDepartures(countyStations);
			taxis = station.dTaxis.size();
			// System.out.println("After: " + taxis);
			totalTaxis += taxis;
			count++;
			// System.out.println();
		}

		System.out.println("Total # taxis: " + totalTaxis);

		// RESET
		 count = 1;
		 totalTaxis = 0;
		 for (Station station : stations) {
		 	System.out.println("checkpoint #" + count + ".2");
		 	int taxis = station.dTaxis.size();
		 	// System.out.println("Before: " + taxis);
		 	countyStations = station.findNearby(countyStations);
		 	taxis = station.dTaxis.size();
		 	// System.out.println("After: " + taxis);
		 	totalTaxis += taxis;
		 	count++;
		 	// System.out.println();
		 }

		System.out.println("Total # taxis: " + totalTaxis);

		// RESET
		count = 1;
		totalTaxis = 0;
		for (Station station : countyStations) {
			// TreeSet<Taxi> finalTaxis = new TreeSet<Taxi>();
			countyStations = station.cycleDepartures(countyStations);
		 	System.out.println("checkpoint #" + count + ".3");

			// System.out.println(finalTaxis.size());
			System.out.println(station.dTaxis.size());
			// totalTaxis += finalTaxis.size();
			totalTaxis += station.dTaxis.size();
			count++;
		}

		System.out.println("Total # taxis: " + totalTaxis);

		// System.out.println("Total # taxis: " + totalTaxis);
		//countyStations = first.minimizeDepartures(countyStations);
		//System.out.println(first.dTaxis.size());


	}

	public static void getDepartures(String fileName) {
		BufferedReader reader = null;

		try {
			String line;
			reader = new BufferedReader(new FileReader(fileName));

			while ((line = reader.readLine()) != null) {
				String[] rawData = line.split(","); //split the data			

				// get the size of the trip
				int riders = Integer.parseInt(rawData[17]);

				int size = 0; // determine the size of the taxi
				if (riders <= 2)
					size = 2;

				else if (riders > 2 && riders <= 6)
					size = 6;

				else if (riders > 6)
					size = Integer.MAX_VALUE;

				// if 2/6 passenger trip, save the trip info
				if (size <= 6) {
					// get the departing pixel
					int i = Integer.parseInt(rawData[1]);
					int j = Integer.parseInt(rawData[2]);

					String pixel = i + ", " + j;
					Trip trip = new Trip(pixel, Double.parseDouble(rawData[3]), rawData[22] + ", " + rawData[23], Double.parseDouble(rawData[24]), Double.parseDouble(rawData[18]), size); 
					Taxi taxi = new Taxi(size);

					// create a station if not already created
					Station station = new Station(i, j);
					if (!countyStations.contains(station))
						countyStations.add(station);
					else {
						Station s = countyStations.get(countyStations.indexOf(station));
						station = s;
					}

					// each trip gets assigned its own taxi, and each taxi has an origin station
					taxi.assignTo(station);
					taxi.addTrip(trip);	

					// make sure to add taxi to saved station, and save where trip departs to
					//station = countyStations.get(countyStations.indexOf(new Station(i, j)));
					station.addDeparture(taxi, trip.destinationPixel());

					// check and see if this station is within range of any of the other stations
					for (Station s : countyStations) {
						if (s.withinRange(station)) {
							if (!station.pixels.contains(s.center()))
								station.add(s.center());
							if (!s.pixels.contains(station.center()))
								s.add(station.center());
						}
					}
				}
			}
		}

		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null){
				try {reader.close();} catch (Exception e) {}
			}
		}

		System.out.println("checkpoint1");
	}


	public static void getArrivals(String fileName) {
		BufferedReader reader = null;

		try {
			String line;
			reader = new BufferedReader(new FileReader(fileName));

			while ((line = reader.readLine()) != null) {
				String[] rawData = line.split(","); //split the data			

				// get the size of the trip
				int riders = Integer.parseInt(rawData[17]);

				int size = 0; // determine the size of the taxi
				if (riders <= 2)
					size = 2;

				else if (riders > 2 && riders <= 6)
					size = 6;

				else if (riders > 6)
					size = Integer.MAX_VALUE;

				// if 2/6 passenger trip, save the trip info
				if (size <= 6) {
					// get the arrival pixel
					int i = Integer.parseInt(rawData[22]);
					int j = Integer.parseInt(rawData[23]);

					//arrival trip, taxi, station
					String pixel = i + ", " + j;
					Trip trip = new Trip(rawData[1] + ", " + rawData[2], Double.parseDouble(rawData[3]), pixel, Double.parseDouble(rawData[24]), Double.parseDouble(rawData[18]), size); 
					Taxi taxi = new Taxi(size);

					// create a station if not already created
					Station station = new Station(i, j);
					if (!countyStations.contains(station))
						countyStations.add(station);
					else {
						Station s = countyStations.get(countyStations.indexOf(station));
						station = s;
					}

					// each trip gets assigned its own taxi, and each taxi has an origin station
					taxi.addTrip(trip);	

					// make sure to add taxi to saved station, and save where trip arrives from
					station.addArrival(taxi, trip.originPixel());

					// check and see if this station is within range of any of the other stations
					for (Station s : countyStations) {
						if (s.withinRange(station)) {
							if (!station.pixels.contains(s.center()))
								station.add(s.center());
							if (!s.pixels.contains(station.center()))
								s.add(station.center());
						}
					}

				}

			}
		}

		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			if (reader != null) {
				try {reader.close();} catch (Exception e) {}
			}
		}

		System.out.println("checkpoint2");
	}
}