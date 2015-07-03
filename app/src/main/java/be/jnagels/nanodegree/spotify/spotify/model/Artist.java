package be.jnagels.nanodegree.spotify.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jelle on 03/07/15.
 */
public class Artist implements Parcelable
{
	private String name;
	private String previewUrl;

	public Artist(String name, String previewUrl)
	{
		this.name = name;
		this.previewUrl = previewUrl;
	}

	public Artist(kaaes.spotify.webapi.android.models.Artist spotifyArtist)
	{
		this.name = spotifyArtist.name;
		if (!spotifyArtist.images.isEmpty())
		{
			this.previewUrl = spotifyArtist.images.get(0).url;
		}
	}

	public String getName()
	{
		return name;
	}

	public String getPreviewUrl()
	{
		return previewUrl;
	}


	@Override
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.name);
		dest.writeString(this.previewUrl);
	}

	protected Artist(Parcel in)
	{
		this.name = in.readString();
		this.previewUrl = in.readString();
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
