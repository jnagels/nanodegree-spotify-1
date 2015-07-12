package be.jnagels.nanodegree.spotify.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by jelle on 03/07/15.
 */
public class Track implements Parcelable
{
	public final String id;
	public final String artist;
	public final String track;
	public final String album;
	public final String artUrl;
	public final String previewUrl;
	public final String spotifyUri;

	public Track(String artistName, kaaes.spotify.webapi.android.models.Track track)
	{
		this.id = track.id;
		this.artist = artistName;
		this.track = track.name;
		this.album = track.album.name;
		this.previewUrl = track.preview_url;
		this.spotifyUri = track.uri;

		if (track.album.images.isEmpty())
		{
			this.artUrl = null;
		}
		else
		{
			this.artUrl = track.album.images.get(0).url;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if (o != null && o instanceof Track)
		{
			final Track oTrack = (Track) o;
			return TextUtils.equals(this.id, oTrack.id);
		}
		return false;
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
		dest.writeString(this.artist);
		dest.writeString(this.track);
		dest.writeString(this.album);
		dest.writeString(this.artUrl);
		dest.writeString(this.previewUrl);
		dest.writeString(this.spotifyUri);
	}

	protected Track(Parcel in)
	{
		this.id = in.readString();
		this.artist = in.readString();
		this.track = in.readString();
		this.album = in.readString();
		this.artUrl = in.readString();
		this.previewUrl = in.readString();
		this.spotifyUri = in.readString();
	}

	public static final Creator<Track> CREATOR = new Creator<Track>()
	{
		public Track createFromParcel(Parcel source)
		{
			return new Track(source);
		}

		public Track[] newArray(int size)
		{
			return new Track[size];
		}
	};
}
