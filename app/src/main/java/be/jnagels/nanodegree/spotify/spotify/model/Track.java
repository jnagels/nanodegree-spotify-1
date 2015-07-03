package be.jnagels.nanodegree.spotify.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jelle on 03/07/15.
 */
public class Track implements Parcelable
{
	public final String id;
	public final String track;
	public final String album;
	public final String artUrl;
	public final String previewUrl;
	public final String spotifyUri;

	public Track(kaaes.spotify.webapi.android.models.Track track)
	{
		this.id = track.id;
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
	public int describeContents()
	{
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags)
	{
		dest.writeString(this.id);
		dest.writeString(this.track);
		dest.writeString(this.album);
		dest.writeString(this.artUrl);
		dest.writeString(this.previewUrl);
		dest.writeString(this.spotifyUri);
	}

	protected Track(Parcel in)
	{
		this.id = in.readString();
		this.track = in.readString();
		this.album = in.readString();
		this.artUrl = in.readString();
		this.previewUrl = in.readString();
		this.spotifyUri = in.readString();
	}

	public static final Parcelable.Creator<Track> CREATOR = new Parcelable.Creator<Track>()
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
