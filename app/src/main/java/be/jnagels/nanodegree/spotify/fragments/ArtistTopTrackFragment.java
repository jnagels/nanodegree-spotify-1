package be.jnagels.nanodegree.spotify.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
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
	private final static String param_country_code = "country_code";

	//data
	private Artist artist;
	private TracksAdapter adapter;
	private String countryCode;

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



		this.setHasOptionsMenu(true);
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

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null)
		{
			this.countryCode = getArguments().getString(param_country_code, "BE");
		}
		else
		{
			this.countryCode = savedInstanceState.getString(param_country_code, "BE");

			final ArrayList<Track> tracks = savedInstanceState.getParcelableArrayList("tracks");
			this.onDataLoaded(tracks);
		}

		if (this.adapter.getData().isEmpty())
		{
			//fetch the top tracks if it isn't empty yet!
			this.fetchTopTracks();
		}
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_country_code, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_country)
		{
			this.showCountryCodeDialog();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showCountryCodeDialog()
	{
		final String[] countryCodes = getResources().getStringArray(R.array.country_codes);

		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setSingleChoiceItems(
				R.array.country_names,
				Arrays.binarySearch(countryCodes, this.countryCode),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						if (which >= 0)
						{
							countryCode = countryCodes[which];
							fetchTopTracks();
						}
						dialog.dismiss();
					}
				});
		builder.setTitle(R.string.change_country);
		builder.show();
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelableArrayList("tracks", this.adapter.getData());
		outState.putString(param_country_code, this.countryCode);
	}

	private void fetchTopTracks()
	{
		progressView.setVisibility(View.VISIBLE);
		recyclerView.setVisibility(View.GONE);

		final HashMap<String,Object> parameters = new HashMap<>();
		parameters.put("country", this.countryCode);
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
