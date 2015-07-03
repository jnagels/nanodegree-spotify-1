package be.jnagels.nanodegree.spotify.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jelle on 03/07/15.
 */
public class Artist implements Parcelable
{
	public final String id;
	public final String artUrl;
	public final String name;

	public Artist(kaaes.spotify.webapi.android.models.Artist artist)
	{
		this.id = artist.id;
		this.name = artist.name;

		if (artist.images.isEmpty())
		{
			this.artUrl = null;
		}
		else
		{
			this.artUrl = artist.images.get(0).url;
		}
	}

	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.id);
		dest.writeString(this.artUrl);
		dest.writeString(this.name);
	}

	protected Artist(Parcel in)
	{
		this.id = in.readString();
		this.artUrl = in.readString();
		this.name = in.readString();
	}

	public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>()
	{
		public Artist createFromParcel(Parcel source)
		{
			return new Artist(source);
		}

		public Artist[] newArray(int size)
		{
			return new Artist[size];
		}
	};
}
