package com.android.example;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

/**
 * This class represents a storage object for Location information.
 * Each time new location info comes in, a new object of this type will be
 * created to hold the info.
 * 
 * @author Matthew Robinson
 *
 */

public class LocationInfo 
{
	DecimalFormat df = new DecimalFormat("#.####");
	
	Location location;
	Geocoder geocoder;
	
	double latitude;
	double longitude;
	double altitude;
	double bearing;
	double distanceFromLastLocation;
	long time;
	float accuracy;
	
	String provider;
	String latString;
	String longString;
	String address;
	String mapLink;
	boolean emptyObject;
	private static final String FLAGTAG = LocationNotification.FLAGTAG;
	
	/**
	 * Constructor.
	 * 
	 * @param location - Holds location information
	 */
    LocationInfo( Location location, Geocoder geocoder )
    {
    	// DEBUG
    	//Log.i( FLAGTAG, "Reaches LocationInfo()..." );
    	
    	this.location = location;
    	this.geocoder = geocoder;
    	distanceFromLastLocation = 0;
    	altitude = -1;
    	
    	if( location != null )
    	{
    		emptyObject = false;
	    	latitude = location.getLatitude();
	    	longitude = location.getLongitude();
	    	
	    	latString = df.format( latitude );
	    	longString = df.format( longitude );

	    	/*latString = Location.convert( latitude, 
	        		Location.FORMAT_DEGREES );
	        longString = Location.convert( longitude, 
	        		Location.FORMAT_DEGREES );*/
	        	        
	        address = getAddress( location );
            
            mapLink = createLink( latString, longString );
            
            accuracy = location.getAccuracy();
            altitude = location.getAltitude();
            bearing = location.getBearing();
            provider = location.getProvider();
            time = location.getTime();
            
    	}
    	else
    	{
    		emptyObject = true;
    	}  	
    }
    
    public double getDistanceFromLastLocation() {
		return distanceFromLastLocation;
	}

	public void setDistanceFromLastLocation(double distanceFromLastLocation) {
		this.distanceFromLastLocation = distanceFromLastLocation;
	}

	/**
     * Gets an address based on the location passed
     * @param location
     * @return
     */
    private String getAddress( Location location )
    {
    	StringBuilder addressString = new StringBuilder();
    	
    	try
    	{
    		Address address = geocoder.getFromLocation( location.getLatitude(), 
        		location.getLongitude(), 1 ).get( 0 ); // just get first for now

    		int numAddressLines = address.getMaxAddressLineIndex();
    		for( int i = 0; i <= numAddressLines; i++ )
    		{
    			addressString.append( address.getAddressLine( i ) );
    			addressString.append( "\n" );
    		}
    		
    	}
    	catch( Exception e )
    	{
    		e.printStackTrace();
    	}
    	
    	return addressString.toString();
    }
    
    /**
	 * Creates a String of location information based on the Location passed.
	 */
	public String getLocationInfoString()
	{
		StringBuilder sb = new StringBuilder();
		Calendar calendar = Calendar.getInstance();
	       
		if( emptyObject == false )
		{
			//sb.append( "Time: " ).append( time );
			sb.append( calendar.getTime() );
			sb.append( "\nLatitude: ").append(latString ); 
	        sb.append( "\nLongitude: " ).append( longString );
	        sb.append( "\nAltitude: " ).append( altitude );
	        sb.append( "\nDistance from Last Location: " );
	        sb.append( distanceFromLastLocation ).append( " M" );
	        sb.append( "\n\nAddress: ").append( address );
	        sb.append( "\n\nMap Link: " ).append( mapLink );
	        sb.append( "\n\nProvider: ").append( provider );
	        sb.append( "\nAccuracy: " ).append( accuracy ).append( "M" );
	        sb.append( "\nBearing: " ).append( bearing );	        
	        sb.append( "\nEND OF LOCATION INFO" );
    	}
    	else
    	{
    		
    		sb.append( "Location information unavailable..." );               		
    	}
    	
    	return sb.toString();
	}
	
	/**
	 * Returns latitude, longitude, altitude (optional) string that will be
	 * used on the Server to update the KML file.
	 * 
	 * NOTE: No spaces allowed between commas and values!
	 * 
	 * @return A line containing info for the KML file.
	 */
    public String getKMLCoordinatesString()
    {

		StringBuilder sb = new StringBuilder();
		
		sb.append( latitude ).append( "," ).append( longitude );
		
		if( altitude != -1 )
		{
			sb.append( "," ).append( altitude ); 
		}
		
		return sb.toString();
    }
		   
    // Creates a google static map link using latitude/longitude info
    private String createLink( String lat, String lon )
    {
    	StringBuilder link = new StringBuilder();
    	int zoom = 17;	// sets the zoom level of the map
    	String picUrl = "http://images.intellicast.com/App_Images/icon_alert_1.gif";
    	
    	// Construct map link
    	link.append( "http://maps.google.com/maps/api/staticmap?center=");
    	link.append( lat ).append( "," ).append( lon );
    	
    	// Zoom and size of map
    	link.append( "&zoom=" ).append( zoom ).append( "&size=400x400&sensor=false" );
    	
    	// Marker to point to location
    	/*link.append( "&markers=color:blue|label:S|" ).append( lat ).append( "," );
    	link.append( lon ).append( "|" ).append( "icon:" ).append( picUrl );*/
    	
    	return link.toString();
    }

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public Geocoder getGeocoder() {
		return geocoder;
	}

	public void setGeocoder(Geocoder geocoder) {
		this.geocoder = geocoder;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getLatString() {
		return latString;
	}

	public void setLatString(String latString) {
		this.latString = latString;
	}

	public String getLongString() {
		return longString;
	}

	public void setLongString(String longString) {
		this.longString = longString;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getMapLink() {
		return mapLink;
	}

	public void setMapLink(String mapLink) {
		this.mapLink = mapLink;
	}

	public boolean isEmptyObject() {
		return emptyObject;
	}

	public void setEmptyObject(boolean emptyObject) {
		this.emptyObject = emptyObject;
	}
    
    
}
