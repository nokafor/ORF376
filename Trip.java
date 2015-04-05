public class Trip {
	int riders;
	double tripMiles;
	double departTime;
	double arriveTime;
	String oPixel;
	String dPixel;

	public Trip(String pixel, double dTime, String pixel2, double eTime, double distance, int size) {
		riders = size;
		tripMiles = distance;
		departTime = dTime;
		arriveTime = eTime;
		oPixel = pixel;
		dPixel = pixel2;

	}

	public double departTime() {
		return departTime;
	}

	public int size() {
		return riders;
	}

	public String originPixel() {
		return oPixel;
	}

	public String destinationPixel() {
		return dPixel;
	}

	public double arriveTime() {
		return arriveTime;
	}

	public double tripMiles() {
		return tripMiles;
	}
}