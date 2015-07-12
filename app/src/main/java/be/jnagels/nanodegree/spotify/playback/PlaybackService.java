package be.jnagels.nanodegree.spotify.playback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;

import java.io.IOException;

import be.jnagels.nanodegree.spotify.spotify.model.Track;

/**
 * Created by jelle on 12/07/15.
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
	private final static String BASE_ACTION = "be.jnagels.nanodegree.spotify.action.";

	public static final String ACTION_PLAY = BASE_ACTION + "PLAY";
	public static final String ACTION_PAUSE = BASE_ACTION + "PLAY";
	public static final String ACTION_STOP = BASE_ACTION + "PLAY";
	public static final String ACTION_SEEK = BASE_ACTION + "SEEK";

	@IntDef({STATUS_LOADING, STATUS_PLAYING, STATUS_PAUSED, STATUS_ERROR, STATUS_FINISHED})
	public @interface PlaybackStatus {}

	public final static int STATUS_LOADING = 0;
	public final static int STATUS_PLAYING = 1;
	public final static int STATUS_PAUSED = 2;
	public final static int STATUS_ERROR = 3;
	public final static int STATUS_FINISHED = 4;

	public interface OnPlaybackListener
	{
		void onPlaybackProgressed(int progress);

		/**
		 * @param progress in milliseconds
		 * @param duration in milliseconds
		 */
		void onTrackInformationLoaded(int progress, int duration);

		void onPlaybackStatusChanged(@PlaybackStatus int status);
	}

	// Binder given to clients
	private final IBinder binder = new LocalBinder();

	private OnPlaybackListener onPlaybackListener;
	private MediaPlayer mediaPlayer = null;
	private Track currentTrack = null;

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
			//get the track and start playing!
			Track track = intent.getParcelableExtra("track");
			play(track);
		}
		else if (ACTION_PAUSE.equals(action))
		{
			pause();
		}
		else if (ACTION_SEEK.equals(action))
		{
			int seekToPosition = intent.getIntExtra("seek_to", -1);
			if (seekToPosition > -1)
			{
				seekTo(seekToPosition);
			}
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
	 * @param track
	 */
	public void play(Track track)
	{
		this.ensureMediaPlayer();

		if (this.currentTrack != null && this.currentTrack.id == track.id)
		{
			//we want to start playing the same track, so let's just start it again
			if (!this.mediaPlayer.isPlaying())
			{
				this.mediaPlayer.start();
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
				this.play(this.currentTrack);
			}
		}
	}

	/**
	 * Stop the whole playback (and destroy the service)
	 */
	public void stop()
	{
		this.stopSelf();
	}

	/**
	 * Seek to a specific position
	 * @param position
	 */
	public void seekTo(int position)
	{
		if (this.mediaPlayer != null)
		{
			this.mediaPlayer.seekTo(position);
			this.dispatchStatus(STATUS_LOADING);
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

	private void dispatchTrackInformation()
	{
		if (this.onPlaybackListener != null)
		{
			this.onPlaybackListener.onTrackInformationLoaded(this.mediaPlayer.getCurrentPosition(), this.mediaPlayer.getDuration());
		}
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
	}

	private void showNotification()
	{

	}

	private void hideNotification()
	{

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

}
