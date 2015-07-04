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

		this.tracks = getArguments().getParcelableArrayList(param_tracks);
		this.selectedTrack = getArguments().getParcelable(param_selected_track);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_player, container, false);
		ButterKnife.bind(this, view);

		this.updateViewsForSelectedTrack();

		return view;
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		Picasso.with(getActivity()).cancelRequest(this.imageViewArt);
		ButterKnife.unbind(this);
	}

	private void updateViewsForSelectedTrack()
	{
		this.textViewTrack.setText(this.selectedTrack.track);
		this.textViewAlbum.setText(this.selectedTrack.album);

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

	@OnClick(R.id.next)
	public void onClickNext()
	{

	}

	@OnClick(R.id.previous)
	public void onClickPrevious()
	{

	}

	@OnClick(R.id.play)
	public void onClickPlay()
	{

	}
}
