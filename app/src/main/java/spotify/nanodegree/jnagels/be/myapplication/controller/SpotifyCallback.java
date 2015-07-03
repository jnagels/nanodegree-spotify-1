package spotify.nanodegree.jnagels.be.myapplication.controller;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Special callback that is cancelable.
 * If {@link #cancel()} is called, no calls will be made to {@link #onSuccess(Object, Response)} or {@link #onFailure(RetrofitError)}.
 */
public abstract class SpotifyCallback<T> implements Callback<T>
{
	private boolean canceled = false;

	public void cancel()
	{
		this.canceled = true;
	}

	public boolean isCanceled()
	{
		return canceled;
	}

	protected abstract void onSuccess(T data, Response response);

	protected abstract void onFailure(RetrofitError error);

	@Override
	public void success(T t, Response response)
	{
		if (!isCanceled())
		{
			this.onSuccess(t, response);
		}
	}

	@Override
	public void failure(RetrofitError error)
	{
		if (!isCanceled())
		{
			this.onFailure(error);
		}
	}
}
