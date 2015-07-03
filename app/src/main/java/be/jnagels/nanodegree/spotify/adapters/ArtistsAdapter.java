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
import be.jnagels.nanodegree.spotify.spotify.model.Artist;
import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by jelle on 03/07/15.
 */
public class ArtistsAdapter extends ArrayListAdapter<Artist, ArtistsAdapter.ViewHolder>
{
	public interface OnArtistClickListener
	{
		void onArtistClick(Artist artist);
	}

	private OnArtistClickListener onArtistClickListener;

	public ArtistsAdapter()
	{
	}

	public void setOnArtistClickListener(OnArtistClickListener onArtistClickListener)
	{
		this.onArtistClickListener = onArtistClickListener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
	{
		return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_artist, parent, false));
	}

	@Override
	protected void onBindViewHolder(ViewHolder holder, Artist item, int position)
	{
		final int size = holder.itemView.getResources().getDimensionPixelSize(R.dimen.album_preview_size);
		final Context context = holder.itemView.getContext();

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

		holder.title.setText(item.name);
	}

	public final class ViewHolder extends RecyclerView.ViewHolder
	{
		@Bind(R.id.preview)
		ImageView preview;

		@Bind(R.id.title)
		TextView title;

		public ViewHolder(final View itemView)
		{
			super(itemView);
			ButterKnife.bind(this, itemView);
			itemView.setOnClickListener(
					new View.OnClickListener()
					{
						@Override
						public void onClick(View v)
						{
							if (onArtistClickListener != null)
							{
								onArtistClickListener.onArtistClick(getItem(getAdapterPosition()));
							}
						}
					}
			);
		}
	}

}
