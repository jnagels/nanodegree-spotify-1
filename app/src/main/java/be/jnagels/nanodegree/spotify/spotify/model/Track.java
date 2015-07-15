package be.jnagels.nanodegree.spotify.spotify.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by jelle on 03/07/15.
 */
public class Track implements Parcelable
{
	private final String id;
	private final String artist;
	private final String track;
	private final String album;
	private final String artUrlLarge;
	private final String artUrlSmall;
	private final String previewUrl;
	private final String spotifyUri;

	public Track(String artistName, kaaes.spotify.webapi.android.models.Track track)
	{
		this.id = track.id;
		this.artist = artistName;
		this.track = track.name;
		this.album = track.album.name;
		this.previewUrl = track.preview_url;
		this.spotifyUri = track.uri;

		if (track.album != null && track.album.images != null && !track.album.images.isEmpty())
		{
			this.artUrlLarge = track.album.images.get(0).url;
			this.artUrlSmall = track.album.images.get(track.album.images.size()-1).url;
		}
		else
		{
			this.artUrlLarge = null;
			this.artUrlSmall = null;
		}
	}

	public String getId()
	{
		return id;
	}

	public String getArtist()
	{
		return artist;
	}

	public String getTrack()
	{
		return track;
	}

	public String getAlbum()
	{
		return album;
	}

	public String getArtUrlLarge()
	{
		return artUrlLarge;
	}

	public String getArtUrlSmall()
	{
		return artUrlSmall;
	}

	public String getPreviewUrl()
	{
		return previewUrl;
	}

	public String getSpotifyUri()
	{
		return spotifyUri;
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
		dest.writeString(this.artUrlLarge);
		dest.writeString(this.artUrlSmall);
		dest.writeString(this.previewUrl);
		dest.writeString(this.spotifyUri);
	}

	protected Track(Parcel in)
	{
		this.id = in.readString();
		this.artist = in.readString();
		this.track = in.readString();
		this.album = in.readString();
		this.artUrlLarge = in.readString();
		this.artUrlSmall = in.readString();
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
