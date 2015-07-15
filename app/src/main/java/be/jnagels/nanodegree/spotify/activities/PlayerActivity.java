package be.jnagels.nanodegree.spotify.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.fragments.PlayerFragment;

/**
 * Created by jelle on 04/07/15.
 */
public class PlayerActivity extends AbstractActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentByTag("player") == null)
		{
			final Bundle args = new Bundle();
			if (getIntent().getExtras() != null)
			{
				args.putAll(getIntent().getExtras());
			}

			final Fragment fragment = new PlayerFragment();
			fragment.setArguments(args);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.placeholder, fragment, "player")
					.commit();
		}

		//make sure the user can get back
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == android.R.id.home)
		{
			//just finish the activity.
			this.finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
