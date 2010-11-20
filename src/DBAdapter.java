package com.android.example;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * This class handles all database operations.
 * 
 * Reference: http://www.devx.com/wireless/Article/40842 
 * 
 * @author Matthew Robinson
 *
 */
public class DBAdapter 
{
	public static final String KEY_ROWID = "id";
	public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_PROVIDER = "provider";
    public static final String KEY_DISTANCE = "distance";
    private static final String FLAGTAG = LocationNotification.FLAGTAG;
    
    private static final String DATABASE_NAME = "location_info";
    private static final String DATABASE_TABLE = "location_reading";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE =
        "create table location_reading (id integer primary key autoincrement, "
        + "latitude double not null, longitude double not null, " 
        + "provider text not null, distance double not null);";
        
    private final Context context; 
    
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    /**
     * Constructor
     * 
     * @param ctx
     */
    public DBAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    // Inner helper class
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, 
        int newVersion) 
        {
            Log.w(FLAGTAG, "Upgrading database from version " + oldVersion 
                    + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS titles");
            onCreate(db);
        }
    }    
    
    //---opens the database---
    public DBAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert location info into the database---
    public long insertLocationInfo(double lat, double lon, String provider, 
    		double distance) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_LATITUDE, lat);
        initialValues.put(KEY_LONGITUDE, lon);
        initialValues.put(KEY_PROVIDER, provider);
        initialValues.put(KEY_DISTANCE, distance);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //---deletes a particular location info entry---
    public boolean deleteLocationInfo(long rowId) 
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + 
        		"=" + rowId, null) > 0;
    }

    //---retrieves all the location info entries---
    public Cursor getAllInfo() 
    {
        return db.query(DATABASE_TABLE, new String[] {
        		KEY_ROWID, 
        		KEY_LATITUDE,
        		KEY_LONGITUDE,
        		KEY_PROVIDER,
                KEY_DISTANCE}, 
                null, 
                null, 
                null, 
                null, 
                null);
    }
    
    //---retrieves specific particular location info ---
    public Cursor get(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {
                		KEY_ROWID, 
                		KEY_LATITUDE,
                		KEY_LONGITUDE,
                		KEY_PROVIDER,
                        KEY_DISTANCE
                		}, 
                		KEY_ROWID + "=" + rowId, 
                		null,
                		null, 
                		null, 
                		null, 
                		null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    //--- Get average distance for the provider type provided.
    // NOT CURRENTLY WORKING...
    /*public double getAverageDistance( String provider )
    {
    	double average = 0.0;
    	
    	StringBuilder sql = new StringBuilder();
    	sql.append( "select avg( distance ) from " );
    	sql.append( DATABASE_TABLE).append( " where provider='" );
    	sql.append( provider ).append( "';" ); 
    	
    	Log.i(FLAGTAG, "Avg. Distance SQL: " + sql.toString() );
    	
    	Cursor cursor = db.rawQuery( sql.toString(), null );
    	
    	if( cursor != null )
    	{
    		// DEBUG
    		Log.i(FLAGTAG, "Cursor is not null..." );
    		
    		cursor.moveToFirst();
    		average = cursor.getDouble( 0 );
    		 
    	}
    	else
    	{
    		Log.e( FLAGTAG, "Error getting average distance value..." );
    	}
    	
    	return average;

    }*/
}
