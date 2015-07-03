package be.jnagels.nanodegree.spotify.activities;

import android.content.Intent;
import android.os.Bundle;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.adapters.ArtistsAdapter;
import be.jnagels.nanodegree.spotify.fragments.ArtistTopTrackFragment;
import be.jnagels.nanodegree.spotify.fragments.SearchFragment;
import be.jnagels.nanodegree.spotify.spotify.model.Artist;

/**
 * Created by jelle on 03/07/15.
 */
public class SearchActivity extends AbstractActivity implements ArtistsAdapter.OnArtistClickListener
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_search);

		if (getSupportFragmentManager().findFragmentByTag("master") == null)
		{
			//only load master fragment if it doesn't exist already!
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.placeholder_master, new SearchFragment(), "master")
					.commit();
		}
	}

	@Override
	public void onArtistClick(Artist artist)
	{
		final Intent intent = new Intent(this, ArtistTopTracksActivity.class);
		intent.putExtra(ArtistTopTrackFragment.param_artist, artist);
		startActivity(intent);
	}
}
