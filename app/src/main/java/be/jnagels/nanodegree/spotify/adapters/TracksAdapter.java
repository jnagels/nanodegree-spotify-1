package be.jnagels.nanodegree.spotify.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import be.jnagels.nanodegree.spotify.R;
import be.jnagels.nanodegree.spotify.spotify.model.Track;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jelle on 03/07/15.
 */
public class TracksAdapter extends ArrayListAdapter<Track, TracksAdapter.ViewHolder>
{
	public TracksAdapter()
	{
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_track, parent, false));
	}

	@Override
	protected void onBindViewHolder(ViewHolder holder, Track item, int position)
	{
		final Context context = holder.itemView.getContext();
		final int size = holder.itemView.getResources().getDimensionPixelSize(R.dimen.album_preview_size);

		if (!TextUtils.isEmpty(item.artUrl))
		{
			Picasso.with(context)
					.load(Uri.parse(item.artUrl))
					.placeholder(R.drawable.placeholder)
					.resize(size, size)
					.centerCrop()
					.into(holder.preview);
		}
		else
		{
			Picasso.with(context).cancelRequest(holder.preview);
			holder.preview.setImageResource(R.drawable.placeholder_empty);
		}

		holder.track.setText(item.track);
		holder.album.setText(item.album);
	}

	public final class ViewHolder extends RecyclerView.ViewHolder
	{
		@Bind(R.id.preview)
		ImageView preview;

		@Bind(R.id.album)
		TextView album;

		@Bind(R.id.track)
		TextView track;

		public ViewHolder(View itemView)
		{
			super(itemView);
			ButterKnife.bind(this, itemView);
		}
	}
}
