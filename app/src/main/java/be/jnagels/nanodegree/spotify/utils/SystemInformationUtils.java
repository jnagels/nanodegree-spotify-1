package be.jnagels.nanodegree.spotify.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by jelle on 16/07/15.
 */
public class SystemInformationUtils
{
	/**
	 * @param context
	 * @return true if the device has an internet connection
	 */
	public final static boolean hasInternetConnection(Context context)
	{
		if (PackageManager.PERMISSION_GRANTED == context.checkCallingOrSelfPermission(Manifest.permission.ACCESS_NETWORK_STATE))
		{
			final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
			if (networkInfo != null)
			{
				return networkInfo.isConnected();
			}
		}
		return false;
	}
}
