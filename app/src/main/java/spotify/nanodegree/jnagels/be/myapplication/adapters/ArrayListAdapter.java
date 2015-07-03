package spotify.nanodegree.jnagels.be.myapplication.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jelle on 03/07/15.
 */
public abstract class ArrayListAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH>
{
	private List<T> data;

	public ArrayListAdapter()
	{
		this.data = new ArrayList<>();
	}

	public void setData(List<T> data)
	{
		this.data = data == null ? new ArrayList<T>() : data;
		this.notifyDataSetChanged();
	}

	public T getItem(int position)
	{
		return this.data.get(position);
	}

	@Override
	public int getItemCount()
	{
		return this.data.size();
	}

	protected abstract void onBindViewHolder(VH holder, T item, int position);

	@Override
	public final void onBindViewHolder(VH holder, int position)
	{
		final T item = this.data.get(position);
		this.onBindViewHolder(holder, item, position);
	}
}
