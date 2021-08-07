package discordfunbot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerPauseEvent;
import com.sedmelluq.discord.lavaplayer.player.event.PlayerResumeEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackExceptionEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStuckEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {

	private TrackList trackList;
	
	public TrackList getTrackList() {
		return trackList;
	}
	
	/**
	 * @param player Audio player
	 */
	public void onPlayerPause(AudioPlayer player) {
		player.setPaused(true);
	}

	/**
	 * @param player Audio player
	 */
	public void onPlayerResume(AudioPlayer player) {
		player.setPaused(false);
	}

	/**
	 * @param player Audio player
	 * @param track Audio track that started
	 */
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		//player.playTrack(track);
	}

	/**
	 * @param player Audio player
	 * @param track Audio track that ended
	 * @param endReason The reason why the track stopped playing
	 */
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		player.stopTrack();
	}

	/**
	 * @param player Audio player
	 * @param track Audio track where the exception occurred
	 * @param exception The exception that occurred
	 */
	public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
		player.stopTrack();
	}

	/**
	 * @param player Audio player
	 * @param track Audio track where the exception occurred
	 * @param thresholdMs The wait threshold that was exceeded for this event to trigger
	 */
	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
		player.stopTrack();
	}

	public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs, StackTraceElement[] stackTrace) {
		onTrackStuck(player, track, thresholdMs);
	}
	
	public void queue(AudioTrack track) {
		trackList.add(track);
	}

	@Override
	public void onEvent(AudioEvent event) {
		if (event instanceof PlayerPauseEvent) {
			onPlayerPause(event.player);
		} else if (event instanceof PlayerResumeEvent) {
			onPlayerResume(event.player);
		} else if (event instanceof TrackStartEvent) {
			onTrackStart(event.player, ((TrackStartEvent) event).track);
		} else if (event instanceof TrackEndEvent) {
			onTrackEnd(event.player, ((TrackEndEvent) event).track, ((TrackEndEvent) event).endReason);
		} else if (event instanceof TrackExceptionEvent) {
			onTrackException(event.player, ((TrackExceptionEvent) event).track, ((TrackExceptionEvent) event).exception);
		} else if (event instanceof TrackStuckEvent) {
			TrackStuckEvent stuck = (TrackStuckEvent) event;
			onTrackStuck(event.player, stuck.track, stuck.thresholdMs, stuck.stackTrace);
		}
	}
}
