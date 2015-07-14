package be.jnagels.nanodegree.spotify.playback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Created by jelle on 14/07/15.
 */
public class MediaButtonEventReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()))
		{
			KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			final String action;

			switch(event.getKeyCode())
			{
				case KeyEvent.KEYCODE_MEDIA_PLAY:
				case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
					action = PlaybackService.ACTION_PLAY;
					break;

				case KeyEvent.KEYCODE_MEDIA_PAUSE:
					action = PlaybackService.ACTION_PAUSE;
					break;

				case KeyEvent.KEYCODE_MEDIA_NEXT:
					action = PlaybackService.ACTION_NEXT;
					break;

				case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
					action = PlaybackService.ACTION_PREVIOUS;
					break;

				case KeyEvent.KEYCODE_MEDIA_STOP:
					action = PlaybackService.ACTION_STOP;
					break;

				default:
					action = null;
					break;
			}

			if (action != null)
			{
				//call the onStartCommand() :)
				final Intent serviceIntent = new Intent(context, PlaybackService.class);
				serviceIntent.setAction(action);
				context.startService(serviceIntent);
			}
		}
	}
}
