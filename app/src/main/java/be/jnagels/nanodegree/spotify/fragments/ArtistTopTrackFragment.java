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
import be.jnagels.nanodegree.spotify.utils.HorizontalDividerItemDecoration;
import butterknife.Bind;
import butterknife.ButterKnife;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jelle on 03/07/15.
 */
public class ArtistTopTrackFragment extends Fragment
{
	public final static String param_artist = "artist";

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
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_top_tracks, container, false);
		ButterKnife.bind(this, view);

		this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		this.recyclerView.addItemDecoration(new HorizontalDividerItemDecoration(getResources()));
		this.recyclerView.setAdapter(this.adapter);


		this.fetchTopTracks();


		return view;
	}

	private void fetchTopTracks()
	{
		progressView.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);

		final HashMap<String,Object> parameters = new HashMap<>();
		parameters.put("country", "BE");
		SpotifyInstance.get(getActivity()).getArtistTopTrack(this.artist.id, parameters, this.callback);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		this.callback.cancel();
	}

	private SpotifyCallback<Tracks> callback = new SpotifyCallback<Tracks>() {
		@Override
		protected void onSuccess(Tracks data, Response response)
		{
			progressView.setVisibility(View.GONE);
			recyclerView.setVisibility(View.VISIBLE);
			adapter.setData(new ArrayList<>(data.tracks));

		}

		@Override
		protected void onFailure(RetrofitError error)
		{
			progressView.setVisibility(View.GONE);
			Toast.makeText(getActivity(), R.string.top_tracks_error, Toast.LENGTH_SHORT).show();
		}
	};
}
