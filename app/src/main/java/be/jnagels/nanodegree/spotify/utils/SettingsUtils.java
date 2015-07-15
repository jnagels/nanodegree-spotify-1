package be.jnagels.nanodegree.spotify.utils;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by jelle on 15/07/15.
 */
public class SettingsUtils
{
	private final static String prefsKeyHideMusicControls = "hide_music_controls";

	public final static boolean isHideMusicControls(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(prefsKeyHideMusicControls, false);
	}

	public final static boolean setHideMusicControls(Context context, boolean hideMusicControls)
	{
		return PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putBoolean(prefsKeyHideMusicControls, hideMusicControls)
				.commit();
	}
}
