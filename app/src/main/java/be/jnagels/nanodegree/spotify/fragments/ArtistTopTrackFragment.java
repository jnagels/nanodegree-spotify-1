package be.jnagels.nanodegree.spotify.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.adapters.TracksAdapter;
import be.jnagels.nanodegree.spotify.spotify.SpotifyCallback;
import be.jnagels.nanodegree.spotify.spotify.SpotifyInstance;
import be.jnagels.nanodegree.spotify.spotify.model.Artist;
import be.jnagels.nanodegree.spotify.spotify.model.Track;
import be.jnagels.nanodegree.spotify.utils.HorizontalDividerItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jelle on 03/07/15.
 */
public class ArtistTopTrackFragment extends Fragment implements TracksAdapter.OnTrackClickListener
{
	public final static Bundle createArguments(Artist artist)
	{
		final Bundle args = new Bundle();
		args.putParcelable(param_artist, artist);
		return args;
	}


	public interface OnTrackPlayListener
	{
		void onPlayTrack(Track track, ArrayList<Track> tracks);
	}

	private final static String param_artist = "artist";

	//data
	private Artist artist;
	private TracksAdapter adapter;

	//views
	@Bind(R.id.progressview)
	View progressView;

	@Bind(R.id.recyclerview)
	RecyclerView recyclerView;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.artist = getArguments().getParcelable(param_artist);
		this.adapter = new TracksAdapter();

		if (savedInstanceState != null)
		{
			final ArrayList<Track> tracks = savedInstanceState.getParcelableArrayList("tracks");
			this.adapter.setData(tracks);
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
		ButterKnife.bind(this, view);

		this.adapter.setOnTrackClickListener(this);

		this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		this.recyclerView.addItemDecoration(new HorizontalDividerItemDecoration(getResources()));
		this.recyclerView.setAdapter(this.adapter);

		this.fetchTopTracks();

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		this.callback.cancel();
		this.adapter.setOnTrackClickListener(null);
		ButterKnife.unbind(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("tracks", this.adapter.getData());
	}

	private void fetchTopTracks()
	{
		progressView.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);

		final HashMap<String,Object> parameters = new HashMap<>();
		parameters.put("country", "BE");
		SpotifyInstance.get(getActivity()).getArtistTopTrack(this.artist.getId(), parameters, this.callback);
	}

	private void onDataLoaded(ArrayList<Track> tracks)
	{
		progressView.setVisibility(View.GONE);
		recyclerView.setVisibility(View.VISIBLE);
		adapter.setData(tracks);
	}

	@Override
	public void onTrackClick(Track track)
	{
		if (getActivity() instanceof OnTrackPlayListener)
		{
			((OnTrackPlayListener) getActivity()).onPlayTrack(track, this.adapter.getData());
		}
	}

	private SpotifyCallback<Tracks> callback = new SpotifyCallback<Tracks>() {
		@Override
		protected void onSuccess(Tracks data, Response response)
		{
			final ArrayList<Track> tracks = new ArrayList<>();
			for(kaaes.spotify.webapi.android.models.Track track : data.tracks)
			{
				tracks.add(new Track(artist.getName(), track));
			}
			onDataLoaded(tracks);
		}

		@Override
		protected void onFailure(RetrofitError error)
		{
			progressView.setVisibility(View.GONE);
			Toast.makeText(getActivity(), R.string.top_tracks_error, Toast.LENGTH_SHORT).show();
		}
	};
}
