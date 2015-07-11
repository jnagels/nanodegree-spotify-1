package be.jnagels.nanodegree.spotify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import java.util.ArrayList;

import be.jnagels.nanodegree.spotify.fragments.ArtistTopTrackFragment;
import be.jnagels.nanodegree.spotify.fragments.PlayerFragment;
import be.jnagels.nanodegree.spotify.spotify.model.Track;

/**
 * Created by jelle on 03/07/15.
 */
public class ArtistTopTracksActivity extends AbstractActivity implements ArtistTopTrackFragment.OnTrackPlayListener
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentByTag("top_tracks") == null)
		{
			final Bundle args = new Bundle();
			args.putAll(getIntent().getExtras());

			final Fragment fragment = new ArtistTopTrackFragment();
			fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, fragment, "top_tracks")
					.commit();
		}

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPlayTrack(Track track, ArrayList<Track> tracks)
	{
		final Intent intent = new Intent(this, PlayerActivity.class);
		intent.putExtra(PlayerActivity.EXTRA_TITLE, this.getSupportActionBar().getSubtitle());
		intent.putExtra(PlayerFragment.param_selected_track, track);
		intent.putExtra(PlayerFragment.param_tracks, tracks);
		startActivity(intent);
	}
}
