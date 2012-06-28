package fi.seco.semweb.util;

public strictfp class GoogleMapsUtil {

	public static String encodeCoordinate(String lat, String lng,String llat, String llng) {
		return encodeCoordinate(Float.parseFloat(lat),Float.parseFloat(lng),Float.parseFloat(llat),Float.parseFloat(llng));
	}

	public static String encodeCoordinate(float lat, float lng, float llat, float llng) {
		StringBuilder sb = new StringBuilder();
		encodeNumber(sb,lat,llat);
		encodeNumber(sb,lng,llng);
		return sb.toString();
	}

	private static void encodeNumber(StringBuilder encodeString, float coordinate, float lcoordinate) {
		int num = Math.round(coordinate * 1e5f)-Math.round(lcoordinate * 1e5f);
		num<<=1;
		if (num < 0)
			num = ~num;
		while (num >= 0x20) {
			int nextValue = (0x20 | (num & 0x1f)) + 63;
			if (nextValue == 92)
				encodeString.append((char)(nextValue));
			encodeString.append((char)(nextValue));
			num >>= 5;
		}

		num += 63;
		if (num == 92)
			encodeString.append((char)(num));

		encodeString.append((char)(num));
	}
	
	public static void main(String[] args)
    {  
        System.out.println(encodeCoordinate(63.35f, 23.23f, 0, 0));
        System.out.println(encodeCoordinate(62.35f, 22.23f, 0, 0));
        System.out.println(encodeCoordinate(65.35f, 25.23f, 0, 0));

    }
}
