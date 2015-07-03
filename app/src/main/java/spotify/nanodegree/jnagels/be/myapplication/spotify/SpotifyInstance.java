package spotify.nanodegree.jnagels.be.myapplication.spotify;

import android.content.Context;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 * Created by jelle on 03/07/15.
 */
public class SpotifyInstance
{
	//can be static, as these will all be killed as soon as the app
	//gets killed by the operating system.
	private static SpotifyService spotifyService;

	private SpotifyInstance() {}

	public final static SpotifyService get(Context context)
	{
		synchronized (SpotifyInstance.class)
		{
			if (spotifyService == null)
			{
				final SpotifyApi api = new SpotifyApi();
				spotifyService = api.getService();
			}
		}
		return spotifyService;
	}
}
