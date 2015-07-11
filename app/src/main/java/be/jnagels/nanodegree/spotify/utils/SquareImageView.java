package be.jnagels.nanodegree.spotify.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jelle on 04/07/15.
 */
public class SquareImageView extends ImageView
{
	public SquareImageView(Context context)
	{
		this(context, null);
	}

	public SquareImageView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public SquareImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		final int widthSize = Math.min(getMaxWidth(), MeasureSpec.getSize(widthMeasureSpec)) - getPaddingLeft() - getPaddingRight();
		final int heightSize = Math.min(getMaxHeight(), MeasureSpec.getSize(heightMeasureSpec)) - getPaddingTop() - getPaddingBottom();
		final int size = Math.min(widthSize, heightSize);
		setMeasuredDimension(size, size);
	}
}
