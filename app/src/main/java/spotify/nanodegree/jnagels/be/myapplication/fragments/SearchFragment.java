package spotify.nanodegree.jnagels.be.myapplication.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;
import retrofit.client.Response;
import spotify.nanodegree.jnagels.be.myapplication.R;
import spotify.nanodegree.jnagels.be.myapplication.adapters.ArtistsAdapter;
import spotify.nanodegree.jnagels.be.myapplication.controller.SpotifyCallback;
import spotify.nanodegree.jnagels.be.myapplication.controller.SpotifyInstance;

/**
 * Created by jelle on 03/07/15.
 */
public class SearchFragment extends Fragment implements TextView.OnEditorActionListener, TextWatcher, ArtistsAdapter.OnArtistClickListener
{
	private final static int MSG_SEARCH = 0;

	//data
	private MyHandler handler = new MyHandler();
	private ArtistsAdapter adapter;
	private SearchCallback callback;

	//views
	private RecyclerView recyclerView;
	private EditText editText;
	private View progressView;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		this.adapter = new ArtistsAdapter();
		this.adapter.setOnArtistClickListener(this);
		this.callback = new SearchCallback();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		final View view = inflater.inflate(R.layout.fragment_search, container, false);

		this.progressView = view.findViewById(R.id.progressview);

		this.recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
		this.recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
		this.recyclerView.setAdapter(this.adapter);

		this.editText = (EditText) view.findViewById(R.id.edittext);
		this.editText.setOnEditorActionListener(this);
		this.editText.addTextChangedListener(this);

		return view;
	}

	/**
	 * Search the spotify api!
	 */
	private void searchApi()
	{
		final String query = this.editText.getText().toString();
		this.adapter.setData(null);

		if (TextUtils.isEmpty(query))
		{
			//empty the adapter as there is no query + show the emptyview
		}
		else
		{
			//show the progress
			this.progressView.setVisibility(View.VISIBLE);
			this.recyclerView.setVisibility(View.GONE);

			//cancel preview callbak!
			if (!this.callback.isCanceled())
			{
				this.callback.cancel();
				this.callback = null;
			}
			//create new callback!
			this.callback = new SearchCallback();
			//query the api, so show a progress indicator!
			SpotifyInstance.get(getActivity()).searchArtists(query, this.callback);
		}
	}

	/**
	 * Data was loaded from the spotify apis
	 * @param data
	 */
	private void onDataLoaded(List<Artist> data)
	{
		this.progressView.setVisibility(View.GONE);
		this.recyclerView.setVisibility(View.VISIBLE);

		if (data.size() == 0)
		{
			this.adapter.setData(null);
		}
		else
		{
			this.adapter.setData(data);
		}
	}

	@Override
	public void onArtistClick(Artist artist)
	{
		//open the detail activity here!
	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
	{
		if (actionId == EditorInfo.IME_ACTION_SEARCH)
		{
			this.searchApi();
			return true;
		}
		return false;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		this.callback.cancel();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after)
	{
		//cancel a pending search.
		this.handler.removeMessages(MSG_SEARCH);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count)
	{
	}

	@Override
	public void afterTextChanged(Editable s)
	{
		//start searching, but only after 1,5 seconds. This can be canceled.
		this.handler.sendEmptyMessageDelayed(MSG_SEARCH, 750l);
	}

	/**
	 * Callback for the api-call.
	 */
	private final class SearchCallback extends SpotifyCallback<ArtistsPager>
	{
		@Override
		protected void onSuccess(ArtistsPager data, Response response)
		{
			onDataLoaded(data.artists.items);
		}

		@Override
		protected void onFailure(RetrofitError error)
		{
			//something failed!
			adapter.setData(null);
		}
	};

	/**
	 * Hander to search delayed
	 */
	private final class MyHandler extends Handler
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				case MSG_SEARCH:
					searchApi();
					break;
				default:
					super.handleMessage(msg);
					break;
			}
		}
	}
}
