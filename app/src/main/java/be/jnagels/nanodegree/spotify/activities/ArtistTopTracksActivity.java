package be.jnagels.nanodegree.spotify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import be.jnagels.nanodegree.spotify.fragments.ArtistTopTrackFragment;
import be.jnagels.nanodegree.spotify.spotify.model.Artist;

/**
 * Created by jelle on 03/07/15.
 */
public class ArtistTopTracksActivity extends AbstractActivity
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

		//also set the subtitle in the actionbar!
		final Intent intent = getIntent();
		final Artist artist = intent.getParcelableExtra(ArtistTopTrackFragment.param_artist);
		this.getSupportActionBar().setSubtitle(artist.name);
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
}
