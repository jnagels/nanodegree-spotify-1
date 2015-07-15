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
import be.jnagels.nanodegree.spotify.utils.SettingsUtils;

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
		if (!TextUtils.isEmpty(subtitle))
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
			if (this.canShowNowPlayingMenuItem())
			{
				//something is playing, so show a menu!
				this.getMenuInflater().inflate(R.menu.menu_playing, menu);
			}

			//something is playing, so show a menu!
			this.getMenuInflater().inflate(R.menu.menu_share, menu);
			// Locate MenuItem with ShareActionProvider
			final MenuItem item = menu.findItem(R.id.menu_item_share);

			//create share intent
			final Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, currentlyTracking.getSpotifyUri());

			// Fetch and store ShareActionProvider
			final ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
			shareActionProvider.setShareIntent(shareIntent);
		}

		//hide music controls menu items
		this.getMenuInflater().inflate(R.menu.menu_toggle_controls, menu);
		final MenuItem menuItem = menu.findItem(R.id.menu_hide_music_controls);
		menuItem.setChecked(SettingsUtils.isHideMusicControls(this));

		return result;
	}

	protected boolean canShowNowPlayingMenuItem()
	{
		return true;
	}

	private void onHideMusicControlsClicked(MenuItem menuItem)
	{
		final boolean hideMusicControls = menuItem.isChecked();
		SettingsUtils.setHideMusicControls(this, hideMusicControls);

		//send a broadcast that the settings have changed!
		sendBroadcast(new Intent(PlaybackService.BROADCAST_SETTINGS_CHANGED));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_now_playing)
		{
			final Intent intent = new Intent(this, PlayerActivity.class);
			startActivity(intent);
			return true;
		}
		if (item.getItemId() == R.id.menu_hide_music_controls)
		{
			item.setChecked(!item.isChecked());
			onHideMusicControlsClicked(item);
			return true;
		}
		return super.onOptionsItemSelected(item);
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
