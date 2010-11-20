package com.android.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Locale;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class LocationNotification extends Activity 
{
	private NotificationManager notificationManager;
	LinkedList<LocationInfo> locations = new LinkedList<LocationInfo>();
	public static final String FLAGTAG = "LocationNotification";
	DBAdapter db;
	
	// used for averages
	double gpsAverage = 0.0;
	double networkAverage = 0.0;
	double gpsDistanceTotal = 0.0;		
	double networkDistanceTotal = 0.0;	
	int numGpsReadings = 0;
	int numNetworkReadings = 0;
	double lastLatitudeReading;
	double lastLongitudeReading;
	boolean firstReading = true;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {   
    	// DEBUG
    	//Log.i( FLAGTAG, "Reaches onCreate()..." );
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        // Create database object
        db = new DBAdapter(this);
        
        // Set up notification and location managers
        String ns = Context.NOTIFICATION_SERVICE;
		notificationManager = 
			(NotificationManager) getSystemService(ns);
		
        // Set appropriate location-based objects
        LocationManager locationManager = (LocationManager)getSystemService
    		( Context.LOCATION_SERVICE ); 
        
        // Create location listener. Used for determining location updates
        LocationListener locationListener = new LocationListener() 
        {
            public void onLocationChanged( Location location ) 
            {
            	// DEBUG
            	//Log.i(FLAGTAG, "Reaches onLocationChanged()..." );
            	
                // Called when a new location is found by the network location provider.
                alertNewLocation( location );
            }

            public void onStatusChanged( String provider, int status, Bundle extras )
            {}

            public void onProviderEnabled(String provider)
            {}

            public void onProviderDisabled(String provider) 
            {}
          };
        
       // Register the listener with the Location Manager to receive location updates
       // from both available Network and GPS providers.
       // The third parameter is the minimum change in distance before receiving 
       // new information. 
          
       // Set to request location update every 15 seconds
       locationManager.requestLocationUpdates
          (LocationManager.NETWORK_PROVIDER, 15000, 0, locationListener);
       locationManager.requestLocationUpdates
          (LocationManager.GPS_PROVIDER, 15000, 0, locationListener);
      
       // Set to request location update every 10 meters.
       //locationManager.requestLocationUpdates
       //    (LocationManager.NETWORK_PROVIDER, 0, 10, locationListener);
       locationManager.requestLocationUpdates
           (LocationManager.GPS_PROVIDER, 0, 10, locationListener);                		
    }
    
    private void alertNewLocation( Location location )
    {
    	// DEBUG
    	//Log.i( FLAGTAG, "Reaches alertNewLocation()..." );
    	Log.i(FLAGTAG, "----------------------------------" );
    	
    	// Set the notification information
		int icon = R.drawable.icon;
		CharSequence notificationText = "New Location Information Available";
		long curTime = System.currentTimeMillis();

		Notification notification = 
			new Notification( icon, notificationText, curTime );
		
		// Set the expanded information and Intent, which will be displaying
		// the current location information on the screen
		Context context = getApplicationContext();
		CharSequence contentTitle = "Location Information";
		CharSequence contentText = "Updated Location Info";
		
		Intent notificationIntent = new Intent(this, LocationNotification.class);
		
		// params: context, requestcode (not used), the intent, flags
		PendingIntent contentIntent = 
			PendingIntent.getActivity(this, 0, notificationIntent, 0);

		// Set how the notification should be triggered and what to display
		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		
		// Add appropriate settings to the notification
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_LIGHTS;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		// Create a new LocationInfo object for manipulating and storing
		// location info
		Geocoder geocoder = new Geocoder( this, Locale.getDefault() );
		LocationInfo locationInfo = new LocationInfo( location, geocoder );		
		
		// DEBUG
		//Log.i( FLAGTAG, "LocationInfo object successfully created..." );
		
		/*if( !( locations.isEmpty() ) )
		{
			int n = locations.size();
			for( int i = 0; i < n; i++ )
			{
				lastLocation = locations.get( i );
				String lastProvider = lastLocation.getProvider();
				String curProvider = locationInfo.getProvider();
								
				// DEBUG
				Log.i(FLAGTAG, "Cur Provider: " + curProvider );
				Log.i(FLAGTAG, "Cur Last Provider: " + lastProvider );
				
				if( !( lastProvider.equals( curProvider ) ) )
				{
					lastLocation = null;
				}
				else //Same provider type, break with lastLocation being valid.
				{
					// DEBUG
					Log.i(FLAGTAG, "Found old location with same type." );
					
					break;
				}
			}
		}
		else
		{
			// DEBUG
			Log.i(FLAGTAG, "This is the first location information found..." );
		}*/

		
		// DEBUG 
		//Log.i(FLAGTAG, "Current number of locations: " + locations.size() );
		//Log.i(FLAGTAG, "locations content info: " + locations.toString() );
		
		// Get distance from last location info.
		// Check to make sure to get distance between last location info
		// of the same provider type for valid comparison.
		/*int locationsSize = locations.size();
		LocationInfo lastLocation = null;
		if( locationsSize != 0)
		{
			lastLocation = locations.getLast();
		}*/
		
		// Add newest location info object to the end of the locations list
		//locations.addLast( locationInfo );
		
		// DEBUG 
		//Log.i(FLAGTAG, "Current number of locations (after add): " + locations.size() );
		//Log.i(FLAGTAG, "locations content info (after add): " + locations.toString() );
		
		double distance = 0;
		//if( lastLocation != null )
		if( !firstReading )
		{			
			//Log.i( FLAGTAG, "Calculating distance between current location and last location" );
			
			double curLat = locationInfo.getLatitude();
			double curLong = locationInfo.getLongitude();
			double lastLat = lastLatitudeReading;
			double lastLong = lastLongitudeReading;
			//double lastLat = lastLocation.getLatitude();
			//double lastLong = lastLocation.getLongitude();			
			
			// DEBUG
			Log.i(FLAGTAG, "Cur Lat: " + curLat + ", Cur Long: " + curLong );
			Log.i(FLAGTAG, "Last Lat: " + lastLat + ", Last Long" + lastLong );
			
			distance = Distance.distance( curLat, curLong, lastLat, lastLong, "K" );
			
			// Convert to meters
			distance *= 1000.00;
			
			// DEBUG
			Log.i(FLAGTAG, "Distance: " + distance );
			locationInfo.setDistanceFromLastLocation( distance );
			
			
			// Calculate and output the new average for distance
			getNewAverageDistance( locationInfo );
			
			
			// DEBUG
			//Log.i(FLAGTAG, "Distance calculated and set for latest LocationInfo object..." );	
		}	
		else	// first reading
		{
			firstReading = false;
			locationInfo.setDistanceFromLastLocation( 0 );
		}
		
		// DEBUG
		//Log.i(FLAGTAG, "LocationInfo: " + locationInfo.getLocationInfoString() );
		
		// Post the location string info to RemoteView	 
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.location_notification);
		contentView.setImageViewResource(R.id.image, R.drawable.icon);
		contentView.setTextViewText(R.id.text, locationInfo.getLocationInfoString() );
		notification.contentView = contentView;
		
		// Send notification to the manager. This puts it at the top of the screen.
		// First parameter is an id, second is the notification itself
		notificationManager.notify(1, notification);
				
		// DEBUG
		//Log.i(FLAGTAG, "Notification Sent." );
		
		// Collect some data
		//collectStats( locationInfo );
		
		// Send data to a file on the phone itself
		//sendToFile( locationInfo );
		
		// Send data to server waiting for it... (Currently cannot connect properly to server)
		Log.i(FLAGTAG, "Ready to send data to server..." );		
		sendInfoToServer( locationInfo );	
		
		// Put data in database.
		//Log.i( FLAGTAG, "----\nReady to send data to the database..." );
		sendInfoToDB( locationInfo );
		
		// I have a suspicion using the same LocationInfo object is a bad idea
		// and that it's overwriting each time. Let's just keep a global
		// variable for keeping last info
		lastLatitudeReading = locationInfo.getLatitude();
		lastLongitudeReading = locationInfo.getLongitude();
    }
    
    /**
     * Calculates the new average distance since last reading.
     */
    private void getNewAverageDistance( LocationInfo locationInfo )
    {
    	// DEBUG
    	Log.i( FLAGTAG, "Reaches getNewAverageDistance()..." );
    	
    	String provider = locationInfo.getProvider();
    	double distance = locationInfo.getDistanceFromLastLocation();
    	
    	if( provider.equals( "gps" ) )
    	{
    		numGpsReadings++;
    		gpsDistanceTotal += distance;
    		gpsAverage = gpsDistanceTotal / numGpsReadings;
    	}
    	else
    	{
    		numNetworkReadings++;
    		networkDistanceTotal += distance;
    		networkAverage = networkDistanceTotal / numNetworkReadings;
    	}
    	
    	
    	// Can't get database averages to work currently...
    	//gpsAverage = db.getAverageDistance( "gps" );
    	//networkAverage = db.getAverageDistance( "network" );
    	    	
    	Log.i(FLAGTAG, "GPS Average Distance: " + gpsAverage );
    	Log.i(FLAGTAG, "Network Average Distance: " + networkAverage );
    }
    
    /**
     * Collects some simple stats based on the location info.
     * Appends the info to /sdcard/download/locationstats.txt
     */
    private void collectStats( LocationInfo locationInfo )
    {
    	Log.i( FLAGTAG, "Reaches collectStats..." );
    	
    	long time = locationInfo.getTime();
    	String FILENAME = "/sdcard/download/stats/locationstats.txt";

    	try
    	{
    		File file = new File( FILENAME );
    		if( new File( "/sdcard/download/stats" ).mkdir() )
    		{
    			Log.i(FLAGTAG, "New directory \"/sdcard/download\" created..." );
    		}
    		else
    		{
    			Log.i(FLAGTAG, "Found directory \"/sdcard/download/stats\"..." );
    		}
    		
            FileWriter f = new FileWriter( file, true );
    		
    		f.write( "\n---------------\n" );
    		
    		double distance = locationInfo.getDistanceFromLastLocation();
    		String provider = locationInfo.getProvider();
    		double accuracy = locationInfo.getAccuracy();
    		double lon = locationInfo.getLongitude();
    		double lat = locationInfo.getLatitude();
    		
    		f.write( "Entry: " );
    		f.write( Long.toString( time ) );
    		f.write( "\nProvider: " );
    		f.write( provider );
    		f.write( "\nLatitude: " );
    		f.write( Double.toString( lat ) );
    		f.write( ", Longitude: " );
    		f.write( Double.toString( lon ) );
    		f.write( "\nDistance Since Last Location Info: " );
    		f.write( Double.toString( distance ) );
    		f.write( "M\nAccuracy: " );
    		f.write( Double.toString( accuracy ) );
    		f.write( "M" );
    		f.write( "\nAvg. Distance Between Locations: " );
    		f.write( Double.toString( gpsAverage ) );
    		f.write( "M" );
    		    		
    		f.flush();
    		f.close();

    		Log.i(FLAGTAG, "Location Info Written to: " + FILENAME );
    	}
    	catch( Exception e )
    	{
    		Log.e( FLAGTAG, "Exception in collectStats..." );
    		System.exit( 1 );
    	}	
    }
    
    /**
     * Stores (appends) location information in a specified file on the phone.
     * 
     * Creates a new file for this location information.
     */
    private void sendToFile( LocationInfo locationInfo )
    {
    	Log.i( FLAGTAG, "Reaches sendToFile()..." );
    	
    	long time = locationInfo.getTime();
    	String FILENAME = "/sdcard/download/LocationInfo" + time + ".txt";
    	String locationInfoString = locationInfo.getLocationInfoString();
    	
    	try
    	{
    		File file = new File( FILENAME );
    		if( new File( "/sdcard/download" ).mkdir() )
    		{
    			Log.i(FLAGTAG, "New directory \"/sdcard/download\" created..." );
    		}
    		else
    		{
    			Log.i(FLAGTAG, "Found directory \"/sdcard/download\"..." );
    		}
    		
            FileWriter f = new FileWriter( file );
    		
    		f.write( "\n---------------\n" );
    		f.write( locationInfoString );
    		
    		f.flush();
    		f.close();

    		Log.i(FLAGTAG, "Location Info Written to: " + FILENAME );
    	}
    	catch( Exception e )
    	{
    		Log.e( FLAGTAG, "Exception in sendToFile()..." );
    		System.exit( 1 );
    	}
    }
    
    /**
     * Sends the current location info to a database.
     */
    public void sendInfoToDB( LocationInfo locationInfo )
    {
    	long id;
        db.open();    
        
        double latitude = locationInfo.getLatitude();
        double longitude = locationInfo.getLongitude();
        String provider = locationInfo.getProvider();
        double distance = locationInfo.getDistanceFromLastLocation();
        
        id = db.insertLocationInfo( latitude, longitude, provider, distance );      

        if( id == -1 )
        {
        	Log.e( FLAGTAG, "Error inserting location info into database." );
        }
        else
        {
        	Log.i( FLAGTAG, "Successfully inserted location info into database." );
        }
        
        // DEBUG ONLY
        // Test to see that the info actually got put into the database by
        // retrieving it back out.
        Cursor cursor = db.get( id );
        parseCursor( cursor );
        
        
        db.close();
    }
    
    /**
     * Parses the Cursor object passed containing Location Information.
     */
    public void parseCursor( Cursor cursor )
    {
    	if( cursor.moveToFirst() )
    	{
    		int numCols;
    		double lat, lon, distance;
    		String provider;
    		
    		numCols = cursor.getColumnCount();
    		lat = cursor.getDouble( 1 );
    		lon = cursor.getDouble( 2 );
    		provider = cursor.getString( 3 );
    		distance = cursor.getDouble( 4 );
    		
    		Log.i(FLAGTAG, "Latest Information Obtained from Database:" );
    		Log.i(FLAGTAG, "Latitude: " + lat + ", Longitude: " + lon );
    		Log.i(FLAGTAG, "Provider: " + provider + ", Distance: " + distance +" M" );
    		//Log.i(FLAGTAG, "Number of Columns (DEBUG): " + numCols );
    		
    	}
    	else
    	{
    		Log.e( FLAGTAG, "Error retrieving location information from database" );
    	}
    }
    
    /**
	 * Sends the current location info to the server waiting to receive it.
	 */
	public void sendInfoToServer( LocationInfo locationInfo )
	{
		Log.i( FLAGTAG, "Reaches sendInfoToServer()..." );
		
		Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        try {
        	Log.i( FLAGTAG, "Connecting to listening server..." );
        	
            socket = new Socket( "agate.cs.unh.edu", 9999);
            
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));          
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));

            // DEBUG
            Log.i(FLAGTAG, "Created output stream to server..." );
            
            // Wait for server to say something to us so we know we're connected.
            if( socket.isConnected() )
            {   
                // Send location information to the server
                out.println( locationInfo.getLocationInfoString() );
            }
            else
            {
            	Log.e(FLAGTAG, "Not connected to the server, exiting..." );
            	System.exit(1);
            }

            Log.i( FLAGTAG, "Sent location info to server..." );

            out.close();
            in.close();
            stdIn.close();
            socket.close();
        } catch (UnknownHostException e) {
            Log.e(FLAGTAG, "Don't know about host.");
            System.exit(1);
        } catch (IOException e) {
            Log.e(FLAGTAG, "Couldn't get I/O for the connection to the host.");
            System.exit(1);
        }
	}
}