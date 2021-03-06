/*
	Radiobeacon - Openbmap wifi and cell logger
    Copyright (C) 2013  wish7

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.openbmap.services.position;

import org.openbmap.RadioBeacon;
import org.openbmap.db.DataHelper;
import org.openbmap.db.models.PositionRecord;
import org.openbmap.services.AbstractService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;


/**
 * Saves positions to database. This is mainly for debugging purposes and functionally not needed for wifi tracking.
 */

public class GpxLoggerService extends AbstractService {

	/**
	 * Minimum distance between two trackpoints in meters
	 */
	private static final int MIN_TRACKPOINT_DISTANCE = 3;

	private static final String TAG = GpxLoggerService.class.getSimpleName();

	/**
	 * Keeps the SharedPreferences
	 */
	private SharedPreferences prefs = null;

	private static final String	POWERLOCKNAME	= "WakeLock.Position";

	/*
	 * last known location
	 */
	private Location mMostCurrentLocation = new Location("DUMMY");
	private String mMostCurrentLocationProvider;

	/**
	 * Are we currently tracking ?
	 */
	private boolean mIsTracking = false;

	/**
	 * Current session id
	 */
	private int mSessionId = RadioBeacon.SESSION_NOT_TRACKING;

	private WakeLock myPowerLock;

	/*
	 * DataHelper for persisting recorded information in database
	 */
	private DataHelper mDataHelper;

	/**
	 * Receives GPS location updates as well as wifi scan result updates
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			Log.d(TAG, "Received intent " + intent.getAction());
			// handling gps broadcasts
			if (RadioBeacon.INTENT_POSITION_UPDATE.equals(intent.getAction())) {
				if (!mIsTracking) {
					return;
				}

				final Location location = intent.getExtras().getParcelable("android.location.Location");
				final String source = intent.getExtras().getString("position_provider_type");

				if (location.distanceTo(mMostCurrentLocation) > MIN_TRACKPOINT_DISTANCE) {
					performGpsUpdate(location, source);
				}
				mMostCurrentLocation = location;
			} 

		}
	};

	@Override
	public final void onCreate() {		
		Log.d(TAG, "GpxLoggerService created");
		super.onCreate();
		// get shared preferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		/*
		 * Setting up database connection
		 */
		mDataHelper = new DataHelper(this);

		final PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		try {
			myPowerLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, POWERLOCKNAME);
			myPowerLock.setReferenceCounted(true);
		} catch (final Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Registers receivers for GPS and wifi scan results.
	 */
	private void registerReceiver() {
		final IntentFilter filter = new IntentFilter();
		// Register our gps broadcast mReceiver
		filter.addAction(RadioBeacon.INTENT_POSITION_UPDATE);
		registerReceiver(mReceiver, filter);
	}

	/**
	 * Unregisters receivers for GPS and wifi scan results.
	 */
	private void unregisterReceiver() {
		try {
			unregisterReceiver(mReceiver);
		} catch (final IllegalArgumentException e) {
			// do nothing here {@see http://stackoverflow.com/questions/2682043/how-to-check-if-receiver-is-registered-in-android}
		}
	}

	@Override
	public final void onDestroy() {
		if (mIsTracking) {
			// If we're currently tracking, save user data.
			stopTracking();
		}

		unregisterReceiver();
		super.onDestroy();
	}

	/**
	 * Saves cell related information
	 * @param gpsLocation
	 */
	private void performGpsUpdate(final Location gpsLocation, final String source) {
		// Do we have gps?
		if 	(gpsLocation == null) {
			Log.e(TAG, "No GPS position available");
			return;
		}

		final PositionRecord pos = new PositionRecord(gpsLocation, mSessionId, source);

		// so far we set end position = begin position 
		mDataHelper.storePosition(pos);

	}

	@Override
	public final void onStartService() {
		registerReceiver();

	}

	@Override
	public final void onStopService() {
		Log.d(TAG, "OnStopService called");
		unregisterReceiver();

	}

	/**
	 * Starts gps logging .
	 * @param sessionId 
	 */
	private void startTracking(final int sessionId) {
		Log.d(TAG, "Start tracking on session " + sessionId);
		mIsTracking = true;
		mSessionId = sessionId;
		mMostCurrentLocation = new Location("dummy");
	}

	/**
	 * Stops gpx Logging
	 */
	private void stopTracking() {
		Log.d(TAG, "Stop tracking on session " + mSessionId);
		mIsTracking = false;
		mSessionId = RadioBeacon.SESSION_NOT_TRACKING;
	}

	/**
	 * Setter for mIsTracking
	 * @return true if we're currently tracking, otherwise false.
	 */
	public final boolean isTracking() {
		return mIsTracking;
	}

	/**
	 * Message mReceiver
	 */
	@Override
	public final void onReceiveMessage(final Message msg) {
		switch(msg.what) {
			case RadioBeacon.MSG_START_TRACKING: 
				Log.d(TAG, "GPX logger received MSG_START_TRACKING signal");

				final Bundle aBundle = msg.getData();
				final int sessionId = aBundle.getInt(RadioBeacon.MSG_KEY, RadioBeacon.SESSION_NOT_TRACKING); 

				startTracking(sessionId);
				break;
			case RadioBeacon.MSG_STOP_TRACKING:
				Log.d(TAG, "GPX logger received MSG_STOP_TRACKING signal");
				stopTracking();
				
				// before manager stopped the service
				GpxLoggerService.this.stopSelf();
				break;
			default:
				Log.d(TAG, "Unrecognized message received: " + msg.what);
		}
	}

}
