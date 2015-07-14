package be.jnagels.nanodegree.spotify.playback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;

import be.jnagels.nanodegree.spotify.spotify.model.Track;

/**
 * Created by jelle on 12/07/15.
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
	private final static String BASE_ACTION = "be.jnagels.nanodegree.spotify.action.";

	public static final String ACTION_PLAY = BASE_ACTION + "PLAY";
	public static final String ACTION_PAUSE = BASE_ACTION + "PLAY";
	public static final String ACTION_STOP = BASE_ACTION + "STOP";
	public static final String ACTION_NEXT = BASE_ACTION + "NEXT";
	public static final String ACTION_PREVIOUS = BASE_ACTION + "PREVIOUS";

	@IntDef({STATUS_LOADING, STATUS_PLAYING, STATUS_PAUSED, STATUS_ERROR, STATUS_FINISHED})
	public @interface PlaybackStatus {}

	public final static int STATUS_LOADING = 0;
	public final static int STATUS_PLAYING = 1;
	public final static int STATUS_PAUSED = 2;
	public final static int STATUS_ERROR = 3;
	public final static int STATUS_FINISHED = 4;

	private final static int MSG_PROGRESS = 0;

	public interface OnPlaybackListener
	{
		/**
		 * @param progress in milliseconds
		 * @param duration in milliseconds
		 */
		void onTrackInformationLoaded(int progress, int duration);

		/**
		 * Called when the currently playing track changes!
		 */
		void onTrackChanged(Track track);

		/**
		 * The playback status is changed
		 * @param status
		 */
		void onPlaybackStatusChanged(@PlaybackStatus int status);
	}

	// Binder given to clients
	private final IBinder binder = new LocalBinder();
	private final Handler handler = new MyHandler();

	private OnPlaybackListener onPlaybackListener;
	private MediaPlayer mediaPlayer = null;

	private Track currentTrack = null;
	private ArrayList<Track> currentTrackList = null;

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return this.binder;
	}

	public int onStartCommand(Intent intent, int flags, int startId)
	{
		final String action = intent.getAction();

		if (ACTION_PLAY.equals(action))
		{
			if (this.currentTrack != null && this.currentTrackList != null)
			{
				play(this.currentTrackList, this.currentTrack);
			}
		}
		else if (ACTION_PAUSE.equals(action))
		{
			pause();
		}
		else if (ACTION_NEXT.equals(action))
		{
			skip(1);
		}
		else if (ACTION_PREVIOUS.equals(action))
		{
			skip(-1);
		}
		else if (ACTION_STOP.equals(action))
		{
			stop();
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		this.hideNotification();

		if (this.mediaPlayer != null)
		{
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
		this.currentTrack = null;
	}

	/**
	 * Register a listener for playback-callbacks
	 * @param onPlaybackListener
	 */
	public void setOnPlaybackListener(OnPlaybackListener onPlaybackListener)
	{
		this.onPlaybackListener = onPlaybackListener;
	}

	/**
	 * @return true if playing, false otherwise
	 */
	public boolean isPlaying()
	{
		if (this.mediaPlayer != null && this.mediaPlayer.isPlaying() && this.currentTrack != null)
		{
			return true;
		}
		return false;
	}

	/**
	 * Play the given track. Will stop playback if currently playing another track
	 * @param tracks the tracklist
	 * @param track the track to play
	 */
	public void play(ArrayList<Track> tracks, Track track)
	{
		if (tracks == null || tracks.isEmpty() || track == null)
		{
			//don't do anything here, we have no information to play...
			return ;
		}

		this.ensureMediaPlayer();

		if (this.currentTrack != null && this.currentTrack.id == track.id)
		{
			//we want to start playing the same track, so let's just start it again
			if (!this.mediaPlayer.isPlaying())
			{
				this.mediaPlayer.start();
				this.dispatchTrackInformation();
				this.dispatchStatus(STATUS_PLAYING);
			}

			return;
		}

		if (this.mediaPlayer != null)
		{
			//stop playback for this track!
			this.mediaPlayer.reset();
		}

		this.currentTrack = track;
		this.currentTrackList = tracks;
		try
		{
			this.mediaPlayer.setDataSource(this.currentTrack.previewUrl);
			this.mediaPlayer.prepareAsync();
			this.dispatchStatus(STATUS_LOADING);
		}
		catch (IOException e)
		{
			this.dispatchStatus(STATUS_ERROR);
		}

		//call the listener too
		if (this.onPlaybackListener != null)
		{
			this.onPlaybackListener.onTrackChanged(this.currentTrack);
		}
	}

	/**
	 * Pause the current playback (if it was started)
	 */
	public void pause()
	{
		if (this.mediaPlayer == null || !this.mediaPlayer.isPlaying())
		{
			//can't pause..
			return;
		}
		this.mediaPlayer.pause();
		this.handler.removeMessages(MSG_PROGRESS);
		this.dispatchStatus(STATUS_PAUSED);
	}

	/**
	 * Play or pause based on the current state!
	 */
	public void playOrPause()
	{
		if (this.mediaPlayer != null)
		{
			if (this.mediaPlayer.isPlaying())
			{
				this.pause();
			}
			else
			{
				this.play(this.currentTrackList, this.currentTrack);
			}
		}
	}

	/**
	 * Stop the whole playback (and destroy the service)
	 */
	public void stop()
	{
		this.handler.removeMessages(MSG_PROGRESS);
		this.stopSelf();
	}

	/**
	 * Seek to a specific position
	 * @param position
	 */
	public void seekTo(int position)
	{
		//stop the progress-loop
		this.handler.removeMessages(MSG_PROGRESS);

		if (this.mediaPlayer != null)
		{
			this.mediaPlayer.seekTo(position);
			this.dispatchTrackInformation();
		}
	}

	/**
	 * @param delta 1 for next, -1 for previous.
	 */
	public void skip(int delta)
	{
		if (this.currentTrack != null && this.currentTrackList != null)
		{
			final int currentIndex = this.currentTrackList.indexOf(this.currentTrack);
			if (currentIndex > -1)
			{
				int newIndex = currentIndex + delta;
				if (newIndex < 0)
				{
					newIndex = this.currentTrackList.size()-1;
				}
				else if (newIndex >= this.currentTrackList.size())
				{
					newIndex = 0;
				}

				this.play(this.currentTrackList, this.currentTrackList.get(newIndex));
			}
		}
	}

	/**
	 * Will create a mediaplayer if necessary.
	 */
	private void ensureMediaPlayer()
	{
		if (this.mediaPlayer == null)
		{
			this.mediaPlayer = new MediaPlayer();
			this.mediaPlayer.setOnPreparedListener(this);
			this.mediaPlayer.setOnCompletionListener(this);
			this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		}
	}

	private void dispatchStatus(@PlaybackStatus int status)
	{
		if (this.onPlaybackListener != null)
		{
			this.onPlaybackListener.onPlaybackStatusChanged(status);
		}
	}

	/**
	 * Dispatch track information to the connected listener.
	 * This also includes the current position in the track
	 */
	private void dispatchTrackInformation()
	{
		if (this.onPlaybackListener != null && this.mediaPlayer.isPlaying())
		{
			this.onPlaybackListener.onTrackInformationLoaded(this.mediaPlayer.getCurrentPosition(), this.mediaPlayer.getDuration());
		}

		//do this again in 1 second
		this.handler.sendEmptyMessageDelayed(MSG_PROGRESS, 500l);
	}

	@Override
	public void onPrepared(MediaPlayer player)
	{
		//start playing
		player.start();
		this.dispatchStatus(STATUS_PLAYING);
		this.dispatchTrackInformation();
	}

	@Override
	public void onCompletion(MediaPlayer mp)
	{
		this.dispatchStatus(STATUS_FINISHED);
		this.handler.removeMessages(MSG_PROGRESS);
	}

	private void showNotification()
	{
//		final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
//
//		Notification notification = new Notification.Builder(this)
//				// Show controls on lock screen even when user hides sensitive content.
//				.setVisibility(Notification.VISIBILITY_PUBLIC)
//				.setSmallIcon(R.drawable.ic_pause_white_24dp)
//				// Add media control buttons that invoke intents in your media service
////				.addAction(R.drawable.ic_prev, "Previous", prevPendingIntent) // #0
////				.addAction(R.drawable.ic_pause, "Pause", pausePendingIntent)  // #1
////				.addAction(R.drawable.ic_next, "Next", nextPendingIntent)     // #2
//						// Apply the media style template
//				.setStyle(new Notification.MediaStyle()
//								.setContentTitle(this.currentTrack.track)
//								.setContentText(this.currentTrack.artist)
//								.setLargeIcon()
//								.build();
	}

	private void hideNotification()
	{
//		final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
//		nm.cancel();
	}

	/**
	 * Local binder class
	 */
	public class LocalBinder extends Binder
	{
		public PlaybackService getService()
		{
			// Return this instance of LocalService so clients can call public methods
			return PlaybackService.this;
		}
	}

	private class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			if (msg.what == MSG_PROGRESS)
			{
				dispatchTrackInformation();
			}
		}
	}

}
