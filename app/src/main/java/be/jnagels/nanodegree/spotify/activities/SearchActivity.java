package be.jnagels.nanodegree.spotify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.adapters.ArtistsAdapter;
import be.jnagels.nanodegree.spotify.fragments.ArtistTopTrackFragment;
import be.jnagels.nanodegree.spotify.fragments.PlayerFragment;
import be.jnagels.nanodegree.spotify.fragments.SearchFragment;
import be.jnagels.nanodegree.spotify.spotify.model.Artist;
import be.jnagels.nanodegree.spotify.spotify.model.Track;

/**
 * Created by jelle on 03/07/15.
 */
public class SearchActivity extends AbstractActivity implements ArtistsAdapter.OnArtistClickListener, ArtistTopTrackFragment.OnTrackPlayListener
{
	private boolean isMultipane;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_search);

		this.isMultipane = findViewById(R.id.placeholder_detail) != null;

		if (getSupportFragmentManager().findFragmentByTag("master") == null)
		{
			//only load master fragment if it doesn't exist already!
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.placeholder_master, new SearchFragment(), "master")
					.commit();
		}
	}

	@Override
	public int onArtistClick(Artist artist)
	{
		final Bundle arguments = ArtistTopTrackFragment.createArguments(artist);

		if (this.isMultipane)
		{
			//we are in multi-pane mode!
			final Fragment fragment = new ArtistTopTrackFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.placeholder_detail, fragment, "detail")
					.commit();

			return OPEN_MODE_FRAGMENT;
		}
		else
		{
			final Intent intent = new Intent(this, ArtistTopTracksActivity.class);
			intent.putExtras(arguments);
			intent.putExtra(ArtistTopTracksActivity.EXTRA_SUBTITLE, artist.getName());

			startActivity(intent);

			return OPEN_MODE_NEW_ACTIVITY;
		}
	}

	@Override
	public void onPlayTrack(Track track, ArrayList<Track> tracks)
	{
		final PlayerFragment fragment = new PlayerFragment();
		final Bundle args = new Bundle();
		args.putParcelable(PlayerFragment.param_selected_track, track);
		args.putParcelableArrayList(PlayerFragment.param_tracks, tracks);
		fragment.setArguments(args);
		fragment.show(getSupportFragmentManager(), "player");
	}
}
