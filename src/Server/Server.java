package Server;

/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Server 
{
	public static File kmlFile = null;
	public static String kmlPath = "../kml";
	public static String kmlFileName = kmlPath + "/doc.kml";
	public static String kmzFileName = kmlPath + "/doc.kmz";
	public static FileWriter fileWriter;
	public static LinkedList<KMLEntry> kmlEntries;
	
	private static final String PLACEMARK_OPENING_TAG = "<Placemark>";
	private static final String PLACEMARK_CLOSING_TAG = "</Placemark>";
	private static final String NAME_OPENING_TAG = "<name>";
	private static final String NAME_CLOSING_TAG = "</name>";
	private static final String POINT_OPENING_TAG = "<Point>";
	private static final String POINT_CLOSING_TAG = "</Point>";
	private static final String COORDINATES_OPENING_TAG = "<coordinates>";
	private static final String COORDINATES_CLOSING_TAG = "</coordinates>";
	
	/**
	 * Returns a String consisting of the necessities for a new KML file.
	 */
	private static String getKMLOpeningTags()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
		sb.append( "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" );
		sb.append( "<Document>" );
		sb.append( "<name>Location Notification Sample KML</name>\n" );
		sb.append( "<description>Displays all location notifications received</description>\n" );		
		
		return sb.toString();
	}
	
	/**
	 * Provides the end tags for the KML file
	 */
	private static String getKMLClosingTags()
	{
		StringBuilder sb = new StringBuilder();
		
		sb.append( "</Document>\n" );
		sb.append( "</kml>" );
		
		return sb.toString();
	}
	
	/**
	 * Creates the KML file that is used as this Server's home page.
	 */
	private static void initializeKMLFile()
	{
		// Create kml directory (if it doesn't exist)
		new File( kmlPath ).mkdir();
		
		try
		{		
			// Create KML file (if it doesn't exist)
			kmlFile = new File( kmlFileName );	
			fileWriter = new FileWriter( kmlFile, false ); // false means overwrite.
			
			// Add header info to the file
			fileWriter.write( getKMLOpeningTags() );
		}
		catch( IOException e )
		{
			System.err.println( "Error creating KML file, terminating server..." );
			e.printStackTrace();
			System.exit( 1 );
		}
	}
	
	/**
	 * Creates a KML Entry object using the String passed.
	 * Strings should be in one of these forms: 
	 * 	"latitude,longitude" or "latitude,longitude,altitude".
	 */
	private static KMLEntry createKMLEntry( String line )
	{
	    String[] tokens = line.split( "," );
	    
	    double latitude = Double.parseDouble( tokens[ 0 ] );
	    double longitude = Double.parseDouble( tokens[ 1 ] );
	    
	    double altitude = -1;
	    if( tokens.length == 3 )	// altitude included
	    {
	    	altitude = Double.parseDouble( tokens[ 2 ] );
	    }
	    
	    return new KMLEntry( latitude, longitude, altitude );	    
	}
	
	/**
	 * Updates the KML file that is used as this Server's home page.
	 * 
	 * Note: currently has to create a brand new KML file each time because 
	 * cannot simply add new kml entry to file because of the closing KML
	 * tags that have to go after the latest entry each time.
	 */
	private static void makeKMLFile( String line )
	{	
		StringBuilder sb = new StringBuilder();
		
		// Creates the new KML file that will be written to.
		initializeKMLFile();
				
		// Create new KMLEntry object from the one just received.
		KMLEntry entry = createKMLEntry( line );
		
		// Store the new entry into the list of entries
		kmlEntries.add( entry );
		
		// Loop through each KML entry and add its info to the KML file
		// An example entry in a file looks like this:
		// <Placemark><name>GeoB8501-2</name><Point><coordinates>-18.7587,18.505</coordinates></Point></Placemark>
		int i = 0;
		for( KMLEntry kmlEntry : kmlEntries )
		{
			sb.append( "\n" );
			sb.append( PLACEMARK_OPENING_TAG ).append( NAME_OPENING_TAG );
			sb.append( "Placemark_" ).append( i );
			sb.append( NAME_CLOSING_TAG );
			sb.append( POINT_OPENING_TAG ).append( COORDINATES_OPENING_TAG );
			sb.append( kmlEntry.getKMLCoordinatesString() );
			sb.append( COORDINATES_CLOSING_TAG ).append( POINT_CLOSING_TAG );
			sb.append( PLACEMARK_CLOSING_TAG );
			sb.append( "\n" );			
			
			i++;
		}
		
		sb.append( "\n" );
		sb.append( getKMLClosingTags() );		
		
		try
		{
			fileWriter.write(sb.toString() );
			fileWriter.flush();
			fileWriter.close();
			
			createKMZ();
		}
		catch( IOException e )
		{
			System.err.println( "Error creating KML file, exiting..." );
			e.printStackTrace();
			System.exit( 1 );
		}
	}
	
	/**
	 * Creates the kmz archive file from the KML file, which is ready by Google Earth.
	 * 
	 * Zipping the file example found here: 
	 *     http://java.sun.com/developer/technicalArticles/Programming/compression/
	 */
	private static void createKMZ()
	{    
		int BUFFER = 2048;

		try 
		{
	        BufferedInputStream origin = null;
	        FileOutputStream dest = new FileOutputStream( kmzFileName );
	        ZipOutputStream out = 
	        	new ZipOutputStream( new BufferedOutputStream( dest ) );
	        out.setMethod( ZipOutputStream.DEFLATED );

	         byte data[] = new byte[ BUFFER ];
	         
	         // get a list of files from the kml directory
	         File f = new File( kmlPath );
	         String files[] = f.list();

	         for ( int i = 0; i < files.length; i++ ) 
	         {
	        	 if( files[i].contains( ".kml" ) ) 	// only zip kml files
	             {
	        		 String curFile = kmlPath + "/" + files[ i ];
	            	 System.out.println("Adding: " + curFile );
	             
		             FileInputStream fi = new FileInputStream( curFile );
		             origin = new BufferedInputStream( fi, BUFFER );
		             ZipEntry entry = new ZipEntry( curFile );
		             out.putNextEntry(entry);
		           
		             int count;
		             while( ( count = origin.read( data, 0, BUFFER ) ) != -1 )
		             {
		               out.write( data, 0, count );
		             }
		             origin.close();
	             }
	          }
	         
	          out.close();
	      } 
		  catch( Exception e ) 
		  {
	         e.printStackTrace();
	      }

	 }
	
	/**
	 * Outputs HTML markup to display when our Server receives a request from
	 * a browser.
	 */
	private static String displayHTML()
	{
	    StringBuilder sb = new StringBuilder();
	    
	    sb.append( "<html>\n<head></head>" );
	    sb.append( "<body>TEST</body>" );
	    sb.append( "</html>" );
	    
	    return sb.toString();
	}
	
	/**
	 * Main function.
	 */
    public static void main(String[] args) throws IOException 
    {

        ServerSocket serverSocket = null;
    	
        try {
            serverSocket = new ServerSocket(9999);
            System.out.println( "Starting server on port 9999..." );
        } 
        catch (IOException e) 
        {
            System.err.println("Could not listen on port: 9999.");
            System.exit(1);
        }

        Socket clientSocket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        // For now, assume one kml file will be used for all connections
        // and locations sent to this server.
        // Eventually, would want to create a separate kml file for each unique
        // client that connects and sends location info.
        kmlEntries = new LinkedList<KMLEntry>();
        
        // Loop forever, accepting lines from any client that connects.
        while ( true ) 
        {
            try
            {
                clientSocket = serverSocket.accept();
                System.out.println( "Server accepted a client..." );
            }
            catch( IOException e )
            {
                System.err.println( "Accepting client failed, exiting..." );
                System.exit( 1 );
            }

            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(
                                    new InputStreamReader(
                                          clientSocket.getInputStream()));
            String inputLine;
            while( ( inputLine = in.readLine() ) != null ) // keep reading in lines until there are no more
            {
            	System.out.println( "Received this line: " + inputLine );
            	
                if( inputLine.equals( "BYE" ) )
                    break;
                
                if( inputLine.contains( "kml:" ) )
                {
                	inputLine = inputLine.replace( "kml:", "" ); 
                	makeKMLFile( inputLine );
                	System.out.println( "Made KML File..." );
                }
                else if( inputLine.contains( "HTTP" ) )
                {
                    out.println( displayHTML() );
                }
                else
                {
                	// ignore line
                }
                

            }

                out.close();
                in.close();
                clientSocket.close();
	}

	//System.out.println( "Stopping server..." );
        
        //serverSocket.close();
    }
}

