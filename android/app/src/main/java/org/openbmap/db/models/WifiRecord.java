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

package org.openbmap.db.models;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openbmap.RadioBeacon;

/**
 * Model for wifi records
 */
public class WifiRecord extends AbstractLogEntry<WifiRecord> {
	
	@SuppressWarnings("unused")
	private static final String TAG = WifiRecord.class.getSimpleName();
	
	public enum CatalogStatus {NEW, OPENBMAP, LOCAL};
	
	private String mBSsid;
	private String mSsid;
	private String mCapabilities;
	private int mFrequency;
	private int mLevel;
	
	/**
	 * Timestamp in openbmap format: YYYYMMDDHHMMSS
	 */
	private long mOpenBmapTimestamp;

	private PositionRecord mBeginPosition;
	private PositionRecord mEndPosition;
	private int mSessionID;

	private CatalogStatus mCatalogStatus;

	/**
	 * used for contains method
	 */
	public final boolean equals(final Object aWifi) {
		WifiRecord oneWifi = (WifiRecord) aWifi;

		return getBssid().equals(oneWifi.getBssid());
	}

	public WifiRecord() {

	}

	/**
	 * Initialises Wifi Record without setting session id
	 * 
	 * @param bssid
	 * @param ssid
	 * @param capabilities
	 * @param frequency
	 * @param level
	 * @param timestamp
	 * @param request
	 * @param last
	 */
	public WifiRecord(String bssid, String ssid, String capabilities, int frequency, int level, long timestamp, PositionRecord request, PositionRecord last, CatalogStatus catalogStatus)
	{
		this(bssid, ssid, capabilities, frequency, level, timestamp, request, last, RadioBeacon.SESSION_NOT_TRACKING, catalogStatus);
	}

	public WifiRecord(String bssid, String ssid, String capabilities, int frequency, int level, long timestamp, PositionRecord request, PositionRecord last, int session, CatalogStatus catalogStatus)
	{
		setBssid(bssid);
		setSsid(ssid);
		setCapabilities(capabilities);
		setFrequency(frequency);
		setLevel(level);
		setOpenBmapTimestamp(timestamp);
		setBeginPosition(request);
		setEndPosition(last);
		setSessionId(session);
		//setNew(knownWifi);
		setCatalogStatus(catalogStatus);
	}

	@Override
	public final int compareTo(final WifiRecord aWifi) {
		if (this.equals(aWifi)) {
			return 0;
		}
		return -1;
	}


	public final int getSessionId() {
		return mSessionID;
	}


	public final void setSessionId(final int session) {
		this.mSessionID = session;
	}


	public final String toString() {
		return "BSSID " + mBSsid + "/ SSID " + mSsid + " / Capabilities " + mCapabilities + " / Frq. " + mFrequency + " / Level " + mLevel;
	}

	public final String getBssid() {
		return mBSsid;
	}

	public final void setBssid(final String bssid) {
		this.mBSsid = bssid;
	}

	public final String getSsid() {
		return mSsid;
	}

	public final String getMd5Ssid() {
		return md5(mSsid);
	}

	public final void setSsid(final String ssid) {
		this.mSsid = ssid;
	}

	public final String getCapabilities() {
		return mCapabilities;
	}

	public final void setCapabilities(final String capabilities) {
		this.mCapabilities = capabilities;
	}

	public final boolean isFree() {
		return mCapabilities.equals("[ESS]");
	}
	
	public final int getFrequency() {
		return mFrequency;
	}

	public final void setFrequency(final int frequency) {
		this.mFrequency = frequency;
	}

	public final int getLevel() {
		return mLevel;
	}

	public final void setLevel(final int level) {
		this.mLevel = level;
	}

	public final long getOpenBmapTimestamp() {
		return mOpenBmapTimestamp;
	}

	public final void setOpenBmapTimestamp(final long timestamp) {
		this.mOpenBmapTimestamp = timestamp;
	}

	public final PositionRecord getBeginPosition() {
		return mBeginPosition;
	}

	public final void setBeginPosition(final PositionRecord begin) {
		this.mBeginPosition = begin;
	}

	public final PositionRecord getEndPosition() {
		return mEndPosition;
	}

	public final void setEndPosition(final PositionRecord end) {
		this.mEndPosition = end;
	}

	/**
	 * Is wifi new, in openbmap wifi catalog or in local wifi catalog
	 * @param catalogStatus Wifi's status
	 */
	public void setCatalogStatus(CatalogStatus catalogStatus) {
		mCatalogStatus = catalogStatus;
	}
	
	public CatalogStatus getCatalogStatus() {
		return mCatalogStatus;
	}
	
	public int getCatalogStatusInt() {
		return mCatalogStatus.ordinal();
	}

	public static String md5(final String source) {  
		try {  
			// Create MD5 Hash  
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");  
			digest.update(source.getBytes());  
			byte[] messageDigest = digest.digest();  

			// Create Hex String  
			StringBuffer hexString = new StringBuffer();  
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}  
			return hexString.toString();  

		} catch (NoSuchAlgorithmException e) {  
			e.printStackTrace();  
		}  
		return "";  
	}  

	
}

