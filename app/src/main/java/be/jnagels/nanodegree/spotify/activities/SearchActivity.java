package be.jnagels.nanodegree.spotify.activities;

import android.os.Bundle;

import be.jnagels.nanodegree.spotify.myapplication.R;
import be.jnagels.nanodegree.spotify.fragments.SearchFragment;

/**
 * Created by jelle on 03/07/15.
 */
public class SearchActivity extends AbstractActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_search);

		if (getSupportFragmentManager().findFragmentByTag("master") == null)
		{
			//only load master fragment if it doesn't exist already!
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.placeholder_master, new SearchFragment(), "master")
					.commit();
		}
	}
}
