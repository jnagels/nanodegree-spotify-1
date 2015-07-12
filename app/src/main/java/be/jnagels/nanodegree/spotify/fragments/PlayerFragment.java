package be.jnagels.nanodegree.spotify.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.playback.PlaybackService;
import be.jnagels.nanodegree.spotify.spotify.model.Track;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PlayerFragment extends DialogFragment implements SeekBar.OnSeekBarChangeListener, PlaybackService.OnPlaybackListener
{
	public final static String param_tracks = "tracks";
	public final static String param_selected_track = "track";

	//data
	private ArrayList<Track> tracks;
	private Track selectedTrack;
	private PlaybackService playbackService;

	//views
	@Bind(R.id.artist)
	TextView textViewArtist;
	@Bind(R.id.track)
	TextView textViewTrack;
	@Bind(R.id.preview)
	ImageView imageViewArt;
	@Bind(R.id.album)
	TextView textViewAlbum;

	@Bind(R.id.seekbar)
	SeekBar seekBar;
	@Bind(R.id.text_progress)
	TextView textViewProgress;
	@Bind(R.id.text_duration)
	TextView textViewDuration;

	@Bind(R.id.play)
	ImageButton buttonPlay;

	@Bind(R.id.next)
	View buttonNext;

	@Bind(R.id.previous)
	View buttonPrevious;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.setStyle(STYLE_NO_TITLE, 0);

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

		this.seekBar.setOnSeekBarChangeListener(this);
		this.seekBar.setEnabled(false);

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
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);


		final Intent serviceIntent = new Intent(getActivity(), PlaybackService.class);
		//start the service, so it doesn't get destroying when we unbind when closing the fragment
		getActivity().startService(serviceIntent);
		//bind to the service to call its methods
		getActivity().bindService(serviceIntent, this.serviceConnection, Context.BIND_AUTO_CREATE);
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

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		//unbind when destroying
		getActivity().unbindService(this.serviceConnection);
		playbackService.setOnPlaybackListener(null);
		playbackService = null;
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
		this.textViewArtist.setText(this.selectedTrack.artist);
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

		if (this.playbackService != null)
		{
			this.playbackService.play(this.selectedTrack);
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
		if (this.playbackService != null)
		{
			this.playbackService.playOrPause();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
	{
		if (fromUser)
		{
			//the user has seeked to a new position, skip to that position for the playback
			this.playbackService.seekTo(progress);
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar){}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar){}

	@Override
	public void onPlaybackStatusChanged(@PlaybackService.PlaybackStatus int status)
	{
		switch(status)
		{
			case PlaybackService.STATUS_LOADING:
				this.buttonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				this.buttonPlay.setEnabled(false);
				break;

			case PlaybackService.STATUS_PLAYING:
				this.buttonPlay.setEnabled(true);
				this.buttonPlay.setImageResource(R.drawable.ic_pause_black_24dp);
				this.seekBar.setEnabled(true);
				break;

			case PlaybackService.STATUS_PAUSED:
				this.buttonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				break;

			case PlaybackService.STATUS_ERROR:
				this.buttonNext.setEnabled(false);
				this.buttonPrevious.setEnabled(false);
				this.buttonPlay.setEnabled(false);
				this.seekBar.setEnabled(false);
				break;
			case PlaybackService.STATUS_FINISHED:
				this.buttonPlay.setImageResource(R.drawable.ic_play_arrow_black_24dp);
				this.seekBar.setProgress(this.seekBar.getMax());
				this.textViewProgress.setText(this.textViewDuration.getText());
				break;
		}
	}

	@Override
	public void onTrackInformationLoaded(int progress, int duration)
	{
		this.seekBar.setProgress(progress);
		this.seekBar.setMax(duration);
		this.textViewProgress.setText(formatDuration(progress));
		this.textViewDuration.setText(formatDuration(duration));
	}

	private final static String formatDuration(int milliseconds)
	{
		int seconds = milliseconds / 1000;
		int minutes = seconds / 60;
		seconds = seconds % 60;

		return new StringBuilder()
				.append(minutes)
				.append(":")
				.append(seconds < 10 ? "0" : "")
				.append(seconds)
				.toString();
	}

	/**
	 * Service Connection object to connect to the PlaybackService
	 */
	private ServiceConnection serviceConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service)
		{
			playbackService = ((PlaybackService.LocalBinder)service).getService();
			//register a listener to get updates here when something changed!
			playbackService.setOnPlaybackListener(PlayerFragment.this);
			//start playing
			playbackService.play(selectedTrack);
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			if (playbackService != null)
			{
				//remove the listener
				playbackService.setOnPlaybackListener(null);
			}
			playbackService = null;
		}
	};
}
