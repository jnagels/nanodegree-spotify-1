package be.jnagels.nanodegree.spotify.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.spotify.model.Track;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayerFragment extends DialogFragment
{
	public final static String param_tracks = "tracks";
	public final static String param_selected_track = "track";

	//data
	private ArrayList<Track> tracks;
	private Track selectedTrack;

	//views
	@Bind(R.id.play)
	View buttonPlay;
	@Bind(R.id.next)
	View buttonNext;
	@Bind(R.id.previous)
	View buttonPrevious;
	@Bind(R.id.preview)
	ImageView imageViewArt;
	@Bind(R.id.track)
	TextView textViewTrack;
	@Bind(R.id.album)
	TextView textViewAlbum;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//read the tracklist from the arguments. This should always be available.
		this.tracks = getArguments().getParcelableArrayList(param_tracks);

		if (this.tracks == null)
		{
			this.tracks = new ArrayList<>();
		}
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_player, container, false);
		ButterKnife.bind(this, view);

		//read the selected track from the arguments, or from the savedInstanceState if there is any!
		final Track selectedTrack;
		if (savedInstanceState == null)
		{
			//read from arguments
			selectedTrack = getArguments().getParcelable(param_selected_track);
		}
		else
		{
			//read from saved instance (because orientation change for example)
			selectedTrack = savedInstanceState.getParcelable("selected_track");
		}

		//just to be safe :-)
		if (this.tracks.isEmpty())
		{
			this.tracks.add(selectedTrack);
		}
		this.setSelectedTrack(selectedTrack);

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putParcelable("selected_track", this.selectedTrack);
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		Picasso.with(getActivity()).cancelRequest(this.imageViewArt);
		ButterKnife.unbind(this);
	}

	/**
	 * Set the selected track and update the views + start playing
	 * @param track
	 */
	private void setSelectedTrack(Track track)
	{
		if (this.selectedTrack == track)
		{
			return ;
		}
		this.selectedTrack = track;

		//set track information
		this.textViewTrack.setText(this.selectedTrack.track);
		this.textViewAlbum.setText(this.selectedTrack.album);

		//enable/disable the skip-buttons
		final boolean enableSkipButtons = this.tracks.size() > 1;
		this.buttonNext.setEnabled(enableSkipButtons);
		this.buttonPrevious.setEnabled(enableSkipButtons);

		//load the album art (or nothing)
		if (TextUtils.isEmpty(this.selectedTrack.artUrl))
		{
			this.imageViewArt.setImageResource(R.drawable.placeholder_empty);
			Picasso.with(getActivity()).cancelRequest(this.imageViewArt);
		}
		else
		{
			Picasso.with(getActivity())
					.load(Uri.parse(this.selectedTrack.artUrl))
					.into(this.imageViewArt);
		}
	}

	/**
	 * If at the beginning/end, it will skip to the last/first item in the tracklist.
	 * @param delta the number of items to skip ahead (> or < than 0 is possible).
	 */
	private void goTo(int delta)
	{
		final int currentIndex = this.tracks.indexOf(this.selectedTrack);
		int destinationIndex = currentIndex + delta;
		if (destinationIndex < 0)
		{
			destinationIndex = this.tracks.size()-1;
		}
		else if (destinationIndex >= this.tracks.size())
		{
			destinationIndex = 0;
		}

		this.setSelectedTrack(this.tracks.get(destinationIndex));
	}

	@OnClick(R.id.next)
	public void onClickNext()
	{
		this.goTo(1);
	}

	@OnClick(R.id.previous)
	public void onClickPrevious()
	{
		this.goTo(-1);
	}

	@OnClick(R.id.play)
	public void onClickPlay()
	{
		//TODO pause/play the playback. Connect to music service here.
	}
}
