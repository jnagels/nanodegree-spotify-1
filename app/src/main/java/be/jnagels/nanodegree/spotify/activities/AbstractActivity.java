package be.jnagels.nanodegree.spotify.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.playback.PlaybackService;
import be.jnagels.nanodegree.spotify.spotify.model.Track;

/**
 * Will set the title and subtitle if given :-)
 */
public abstract class AbstractActivity extends AppCompatActivity
{
	public final static String EXTRA_TITLE = "title";
	public final static String EXTRA_SUBTITLE = "subtitle";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setContentView(this.getLayoutResourceId());

		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		this.setSupportActionBar(toolbar);

		//set the title if there is one!
		final String title = this.getIntent().getStringExtra(EXTRA_TITLE);
		if (!TextUtils.isEmpty(title))
		{
			this.getSupportActionBar().setTitle(title);
		}

		//set the subtitle if there is one!
		final String subtitle = getIntent().getStringExtra(EXTRA_SUBTITLE);
		if (!TextUtils.isEmpty(title))
		{
			this.getSupportActionBar().setSubtitle(subtitle);
		}
	}

	protected int getLayoutResourceId()
	{
		return R.layout.activity_single_fragment;
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		//something might have been started playing while we were paused. Invalidate to check again!
		this.supportInvalidateOptionsMenu();

		this.registerReceiver(this.broadcastReceiver, new IntentFilter(PlaybackService.BROADCAST_TRACK_CHANGED));
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		this.unregisterReceiver(this.broadcastReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		boolean result = super.onCreateOptionsMenu(menu);

		Track currentlyTracking = PlaybackService.getCurrentTrack();
		if (currentlyTracking != null)
		{
			//something is playing, so show a menu!
			this.getMenuInflater().inflate(R.menu.menu_share_track, menu);
			// Locate MenuItem with ShareActionProvider
			final MenuItem item = menu.findItem(R.id.menu_item_share);

			final Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, currentlyTracking.getSpotifyUri());

			// Fetch and store ShareActionProvider
			final ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
			shareActionProvider.setShareIntent(shareIntent);
		}

		return result;
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			//invalidate the option menu if the track has changed
			supportInvalidateOptionsMenu();
		}
	};
}
