package be.jnagels.nanodegree.spotify.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

/**
 * Will set the title and subtitle if given :-)
 */
public abstract class AbstractActivity extends AppCompatActivity
{
	public final static String EXTRA_TITLE = "title";
	public final static String EXTRA_SUBTITLE = "subtitle";

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//set the title if there is one!
		final String title = getIntent().getStringExtra(EXTRA_TITLE);
		if (!TextUtils.isEmpty(title))
		{
			getSupportActionBar().setTitle(title);
		}

		//set the subtitle if there is one!
		final String subtitle = getIntent().getStringExtra(EXTRA_SUBTITLE);
		if (!TextUtils.isEmpty(title))
		{
			getSupportActionBar().setSubtitle(subtitle);
		}
	}
}
