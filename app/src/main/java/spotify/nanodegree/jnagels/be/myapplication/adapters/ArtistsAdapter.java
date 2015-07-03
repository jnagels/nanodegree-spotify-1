package spotify.nanodegree.jnagels.be.myapplication.adapters;

import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Artist;
import spotify.nanodegree.jnagels.be.myapplication.R;

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

		if (!item.images.isEmpty())
		{
			Picasso.with(holder.itemView.getContext())
					.load(Uri.parse(item.images.get(0).url))
					.placeholder(R.drawable.placeholder)
					.resize(size, size)
					.centerCrop()
					.into(holder.preview);
		}
		else
		{
			holder.preview.setImageResource(R.drawable.placeholder_empty);
		}

		holder.title.setText(item.name);
	}

	public final class ViewHolder extends RecyclerView.ViewHolder
	{
		ImageView preview;
		TextView title;

		public ViewHolder(final View itemView)
		{
			super(itemView);
			this.preview = (ImageView) itemView.findViewById(R.id.preview);
			this.title = (TextView) itemView.findViewById(R.id.title);
			itemView.setOnClickListener(new View.OnClickListener()
										{
											@Override
											public void onClick(View v)
											{
												if (onArtistClickListener != null)
												{
													final Artist artist = getItem(getAdapterPosition());
													onArtistClickListener.onArtistClick(artist);
												}
											}
										}
			);
		}
	}

}
