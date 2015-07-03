package be.jnagels.nanodegree.spotify.utils;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import be.jnagels.nanodegree.spotify.R;

/**
 * Just draw a divider
 */
public class HorizontalDividerItemDecoration extends RecyclerView.ItemDecoration
{
	private final Paint paint;
	private final float density;

	public HorizontalDividerItemDecoration(Resources resources)
	{
		this.paint = new Paint();
		this.paint.setColor(resources.getColor(R.color.seperator));
		this.paint.setStyle(Paint.Style.STROKE);

		this.density = resources.getDisplayMetrics().density;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state)
	{
		final int itemCount = parent.getLayoutManager().getItemCount();
		//don't draw a bottom line for the last item!
		if (parent.getLayoutManager().getPosition(view) < itemCount-1)
		{
			outRect.set(0, 0, 0, (int) Math.max(1f, 1f * this.density));
		}
		else
		{
			outRect.set(0, 0, 0, 0);
		}
	}

	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state)
	{
		final int itemCount = parent.getLayoutManager().getItemCount();

		final int left = parent.getPaddingLeft();
		final int right = c.getWidth() - parent.getPaddingRight();

		final int childCount = parent.getChildCount();
		View child;
		for(int i = 0 ; i < childCount ; i++)
		{
			child = parent.getChildAt(i);

			//don't draw a bottom line for the last item!
			if (parent.getLayoutManager().getPosition(child) < itemCount-1)
			{
				//add 1 to account for the itemOffset created in getItemOffsets
				final float y = child.getBottom() + child.getTranslationY() + 1f;
				//draw the line !
				c.drawLine(left, y, right, y, this.paint);
			}
		}
	}
}
