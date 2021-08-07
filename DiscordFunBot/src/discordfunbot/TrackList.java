package discordfunbot;

import java.util.ArrayList;
import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackList extends ArrayList<AudioTrack> implements AudioPlaylist {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}
	
	public void setName(String setName) {
		name = setName;
	}

	@Override
	public List<AudioTrack> getTracks() {
		return this;
	}

	@Override
	public AudioTrack getSelectedTrack() {
		return this.get(0);
	}

	@Override
	public boolean isSearchResult() {
		return false;
	}

}
