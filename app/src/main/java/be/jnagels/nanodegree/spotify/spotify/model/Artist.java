package be.jnagels.nanodegree.spotify.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jelle on 03/07/15.
 */
public class Artist implements Parcelable
{
	private final String id;
	private final String artUrlSmall;
	private final String artUrlLarge;
	private final String name;

	public Artist(kaaes.spotify.webapi.android.models.Artist artist)
	{
		this.id = artist.id;
		this.name = artist.name;

		if (artist.images.isEmpty())
		{
			this.artUrlLarge = null;
			this.artUrlSmall = null;
		}
		else
		{

			this.artUrlLarge = artist.images.get(0).url;
			this.artUrlSmall = artist.images.get(artist.images.size()-1).url;
		}
	}

	public String getId()
	{
		return id;
	}

	public String getArtUrlSmall()
	{
		return artUrlSmall;
	}

	public String getArtUrlLarge()
	{
		return artUrlLarge;
	}

	public String getName()
	{
		return name;
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
		dest.writeString(this.artUrlSmall);
		dest.writeString(this.artUrlLarge);
		dest.writeString(this.name);
	}

	protected Artist(Parcel in)
	{
		this.id = in.readString();
		this.artUrlSmall = in.readString();
		this.artUrlLarge = in.readString();
		this.name = in.readString();
	}

	public static final Creator<Artist> CREATOR = new Creator<Artist>()
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
