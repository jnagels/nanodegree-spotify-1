package be.jnagels.nanodegree.spotify.playback;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.ArrayList;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.activities.PlayerActivity;
import be.jnagels.nanodegree.spotify.spotify.model.Track;

/**
 * Created by jelle on 12/07/15.
 */
public class PlaybackService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener
{
	private final static String BASE_ACTION = "be.jnagels.nanodegree.spotify.action.";

	public static final String ACTION_PLAY = BASE_ACTION + "PLAY";
	public static final String ACTION_PAUSE = BASE_ACTION + "PAUSE";
	public static final String ACTION_PLAYPAUSE = BASE_ACTION + "PLAYPAUSE";
	public static final String ACTION_STOP = BASE_ACTION + "STOP";
	public static final String ACTION_NEXT = BASE_ACTION + "NEXT";
	public static final String ACTION_PREVIOUS = BASE_ACTION + "PREVIOUS";

	public static final String BROADCAST_TRACK_CHANGED = "be.jnagels.nanodegree.spotify.broadcast.TRACK_CHANGED";
	public static final String BROADCAST_SETTINGS_CHANGED = "be.jnagels.nanodegree.spotify.broadcast.SETTINGS_CHANGED";

	@IntDef({STATUS_LOADING, STATUS_PLAYING, STATUS_PAUSED, STATUS_ERROR, STATUS_FINISHED, STATUS_STOPPED})
	public @interface PlaybackStatus {}

	public final static int STATUS_LOADING = 0;
	public final static int STATUS_PLAYING = 1;
	public final static int STATUS_PAUSED = 2;
	public final static int STATUS_ERROR = 3;
	public final static int STATUS_FINISHED = 4;
	public final static int STATUS_STOPPED = 5;

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
		 *
		 * @param status
		 */
		void onPlaybackStatusChanged(@PlaybackStatus int status);
	}

	/**
	 * @return the currently playing track, or null.
	 */
	public static Track getCurrentTrack()
	{
		return sCurrentTrack;
	}

	/**
	 * Not sure if this is the best way. It's only some attributes, so it's no big deal because
	 * no context or something is leaked. And when the app is closed by the system, the process
	 * is killed anyway, and all static stuff is freed.
	 */
	private static Track sCurrentTrack = null;


	// Binder given to clients
	private final IBinder binder = new LocalBinder();
	private final Handler handler = new MyHandler();

	private OnPlaybackListener onPlaybackListener;
	private MediaPlayer mediaPlayer = null;
	private MediaSessionCompat mediaSession;

	@PlaybackStatus
	private int currentPlaybackStatus = -1;
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
		//if action == null, then we're just starting this service. No action required :)

		if (action != null)
		{
			//call different methods based on the given action
			switch (action)
			{
				case ACTION_PLAY:
					if (this.currentTrack != null && this.currentTrackList != null)
					{
						play(this.currentTrackList, this.currentTrack);
					}
					break;

				case ACTION_PLAYPAUSE:
					playOrPause();
					break;

				case ACTION_PAUSE:
					pause();
					break;

				case ACTION_NEXT:
					skip(1);
					break;

				case ACTION_PREVIOUS:
					skip(-1);
					break;

				case ACTION_STOP:
					stop();
					break;
			}
		}
		return START_NOT_STICKY;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		this.registerReceiver(this.settingsChangedBroadcastReceiver, new IntentFilter(BROADCAST_SETTINGS_CHANGED));
		this.isSettingsChangedReceiverRegistered = true;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		this.destroyInternal();
	}

	private void destroyInternal()
	{
		if (this.isSettingsChangedReceiverRegistered)
		{
			this.isSettingsChangedReceiverRegistered = false;
			this.unregisterReceiver(this.settingsChangedBroadcastReceiver);
		}
		this.hideNotification();

		if (this.mediaSession != null)
		{
			this.mediaSession.release();
			this.mediaSession = null;
		}

		if (this.mediaPlayer != null)
		{
			this.mediaPlayer.release();
			this.mediaPlayer = null;
		}
		this.currentTrackList = null;
		this.currentTrack = null;
		sCurrentTrack = null;

		this.sendBroadcast(new Intent(BROADCAST_TRACK_CHANGED));
	}

	/**
	 * Register a listener for playback-callbacks
	 *
	 * @param onPlaybackListener
	 */
	public void setOnPlaybackListener(OnPlaybackListener onPlaybackListener)
	{
		this.onPlaybackListener = onPlaybackListener;
	}

	/**
	 * This will do some callbacks to the {@link #onPlaybackListener}
	 */
	public void askForCurrentlyPlayingTrack()
	{
		//let the listener know which track we're playing (if we're playing)
		if (this.onPlaybackListener != null)
		{
			if (this.isPlaying() && this.currentTrack != null)
			{
				this.onPlaybackListener.onTrackChanged(this.currentTrack);
			}
			this.dispatchStatus(this.currentPlaybackStatus);
		}
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
	 *
	 * @param tracks the tracklist
	 * @param track  the track to play
	 */
	public void play(ArrayList<Track> tracks, Track track)
	{
		if (tracks == null || tracks.isEmpty() || track == null)
		{
			//don't do anything here, we have no information to play...
			return;
		}

		this.ensureMediaPlayer();

		if (this.currentTrack != null && this.currentTrack.getId() == track.getId())
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

		//save track and tracklist
		this.currentTrack = track;
		this.currentTrackList = tracks;
		sCurrentTrack = this.currentTrack;
		this.sendBroadcast(new Intent(BROADCAST_TRACK_CHANGED));

		//prepare the audio preview
		try
		{
			this.mediaPlayer.setDataSource(this.currentTrack.getPreviewUrl());
			this.mediaPlayer.prepareAsync();
			this.showOrUpdateNotification();
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
		this.dispatchStatus(STATUS_STOPPED);
		this.destroyInternal();

		this.handler.removeMessages(MSG_PROGRESS);
		this.stopSelf();
	}

	/**
	 * Seek to a specific position
	 *
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
					newIndex = this.currentTrackList.size() - 1;
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

			final ComponentName mediaButtonEventReceiver = new ComponentName(getPackageName(), MediaButtonEventReceiver.class.getName());
			this.mediaSession = new MediaSessionCompat(this, "spotify_streamer", mediaButtonEventReceiver, null);
			this.mediaSession.setCallback(this.mediaSessionCallback);
			this.mediaSession.setActive(true);
		}
	}

	private void dispatchStatus(@PlaybackStatus int status)
	{
		this.currentPlaybackStatus = status;
		//update the notification to reflect the current state :)
		this.showOrUpdateNotification();
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

		if (this.currentTrackList.size() > 1)
		{
			//play the next track!
			this.skip(1);
		}
	}

	/**
	 * Show (or update) the notification
	 */
	private void showOrUpdateNotification()
	{
		if (this.currentTrack == null)
		{
			//can't do anything without a track here :)
			return ;
		}

		//We could optimize here by saving the bitmap in memory here (if the old ArtUrl is the same
		// as the new one), but we count on Picasso to do this for us :)

		//load the album art with picasso.
		//When finished, use that bitmap to create a notification!
		if (!TextUtils.isEmpty(this.currentTrack.getArtUrlLarge()))
		{
			Picasso.with(this).load(this.currentTrack.getArtUrlLarge())
					.into(new Target()
						  {
							  @Override
							  public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from)
							  {
								  onImageReadyForNotification(bitmap);
							  }

							  @Override
							  public void onBitmapFailed(Drawable errorDrawable)
							  {
								  //no image to show!
								  onImageReadyForNotification(null);
							  }

							  @Override
							  public void onPrepareLoad(Drawable placeHolderDrawable)
							  {
							  }
						  }
					);
		}
		else
		{
			onImageReadyForNotification(null);
		}
	}

	/**
	 * Actually create the notification!
	 *
	 * @param bitmap
	 */
	private void onImageReadyForNotification(Bitmap bitmap)
	{
		final boolean isPlaying = this.isPlaying();
		//only support next/previous if there is more than 1 track in the tracklist
		final boolean showNextAndPreviousButtons = this.currentTrackList.size() > 1;

		final Intent activityIntent = new Intent(this, PlayerActivity.class);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);

		final NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
		notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		notificationBuilder.setContentIntent(contentIntent);
		notificationBuilder.setDeleteIntent(getPendingIntentForAction(ACTION_STOP));
		notificationBuilder.setSmallIcon(isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play);
		notificationBuilder.setContentTitle(this.currentTrack.getTrack());
		notificationBuilder.setContentText(this.currentTrack.getArtist() + " - " + this.currentTrack.getAlbum());
		notificationBuilder.setLargeIcon(bitmap);

		final boolean hideMusicControls = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_music_controls", false);

		if (!hideMusicControls)
		{
			//previous button (if necessary)
			if (showNextAndPreviousButtons)
			{
				notificationBuilder.addAction(android.R.drawable.ic_media_previous, getString(R.string.previous), getPendingIntentForAction(ACTION_PREVIOUS));
			}

			//show pause or play depending on the current state!
			if (this.isPlaying())
			{
				notificationBuilder.addAction(android.R.drawable.ic_media_pause, getString(R.string.pause), getPendingIntentForAction(ACTION_PAUSE));
			}
			else
			{
				notificationBuilder.addAction(android.R.drawable.ic_media_play, getString(R.string.play), getPendingIntentForAction(ACTION_PLAY));
			}

			//next button (if necessary)
			if (showNextAndPreviousButtons)
			{
				notificationBuilder.addAction(android.R.drawable.ic_media_next, getString(R.string.next), getPendingIntentForAction(ACTION_NEXT));
			}

			//add media style stuff
			final NotificationCompat.MediaStyle mediaStyle = new NotificationCompat.MediaStyle();
			mediaStyle.setMediaSession(this.mediaSession.getSessionToken());
			mediaStyle.setShowCancelButton(true);
			mediaStyle.setCancelButtonIntent(getPendingIntentForAction(ACTION_STOP));
			notificationBuilder.setStyle(mediaStyle);
		}

		final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
		nm.notify(R.id.notification_id, notificationBuilder.build());
	}

	/**
	 * @param action
	 * @return a pending intent that will execute the given action in the PlaybackService. See {@link #onStartCommand(Intent, int, int)}
	 */
	private PendingIntent getPendingIntentForAction(String action)
	{
		final Intent intent = new Intent(this, PlaybackService.class);
		intent.setAction(action);
		return PendingIntent.getService(this, 0, intent, 0);
	}

	/**
	 * Hide the notification
	 */
	private void hideNotification()
	{
		final NotificationManagerCompat nm = NotificationManagerCompat.from(this);
		nm.cancel(R.id.notification_id);
	}

	/**
	 * Callback methods for the mediaSession
	 */
	private final MediaSessionCompat.Callback mediaSessionCallback = new MediaSessionCompat.Callback()
	{
		@Override
		public void onPlay()
		{
			playOrPause();
		}

		@Override
		public void onSkipToNext()
		{
			skip(1);
		}

		@Override
		public void onSkipToPrevious()
		{
			skip(-1);
		}

		@Override
		public void onPause()
		{
			playOrPause();
		}

		@Override
		public void onStop()
		{
			PlaybackService.this.stop();
		}


	};

	private boolean isSettingsChangedReceiverRegistered = false;
	private final BroadcastReceiver settingsChangedBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent)
		{
			showOrUpdateNotification();
		}
	};

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
