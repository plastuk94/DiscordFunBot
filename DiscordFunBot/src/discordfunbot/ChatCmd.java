package discordfunbot;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;

import com.dropbox.core.DbxException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageHistory.MessageRetrieveAction;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class ChatCmd extends ListenerAdapter {
	    @Override
	    public void onMessageReceived(MessageReceivedEvent event) {
	    	// Ignore bot messages
	        if(event.getAuthor().isBot()) {
	            return;
	        }
	        
	        // Ignore DMs
	        if(event.isFromType(ChannelType.PRIVATE)) {
	            return;
	        }

	        // Gets the message that triggered the event
	        Message msg = event.getMessage();
	        MessageChannel channel = event.getChannel();
	        Guild guild = event.getGuild();
	        
	        switch (msg.getContentRaw()) {
	        case "!ping" : // Respond to !ping with "Pong!"
	        	channel.sendMessage("Pong! ").queue();
	        	break;
	        
	        case "!file" : // Upload file (can be image)
	        	File image = new File("C:\\Users\\Justin\\Pictures\\2.png");
	        	channel.sendFile(image).queue();
	        	break;
	        	
	        case "!meme" : // Post a random meme from Reddit.
	        	String Json = "";
	        	String nsfw = "true";
	        	ObjectMapper objectMapper = new ObjectMapper();
	        	try {
	        		while (nsfw.equals("true")) {
	            		RestApiRequest memeRequest = new RestApiRequest(new URL("https://meme-api.herokuapp.com/gimme/wholesomememes"),null);
	            		Json = memeRequest.getResponse();
	            		System.out.println(Json);
	    				JsonNode jsonNode = objectMapper.readTree(Json);
	    				URL memeFileURL = new URL(jsonNode.get("url").asText());
	    				nsfw = jsonNode.get("nsfw").asText();
	    				BufferedImage memeImg = ImageIO.read(memeFileURL);
	    				File memeFile = new File("downloaded.jpg");
	    				ImageIO.write(memeImg, "jpg", memeFile);
	    				if (nsfw.equals("false")) {
	    					channel.sendFile(memeFile).queue();
	    					break;
	    				}
	    				break;
	        		}
				} catch (IOException e) {
					e.printStackTrace();
				}
	        	break;
	        	
	        case "!deletemessages" :
	        	
	        	String userID = msg.getAuthor().getId();
	        	if (!userID.equals(guild.getOwnerId())) {
	        		break;           // At the moment, I only want the owner 
	        	}                    // to be able to delete messages (in future, this will check roles).
	        	
	            MessageHistory history = new MessageHistory(channel);
	            List<Message> msgs;
	            msgs = history.retrievePast(100).complete();
	            channel.purgeMessages(msgs);
	            break;
	            
	        case "!oof" :
	        	
	        	try {
					DropboxAPIRequest dropboxRequest = new DropboxAPIRequest();
					dropboxRequest.browseFiles();
					
					File oofFile = dropboxRequest.randomImage();
					channel.sendFile(oofFile).queue();
					
				} catch (IOException | DbxException e1) {
					e1.printStackTrace();
				}
	        	
	        	break;

	        default:
	        	if (msg.getContentRaw().contains("!music ")) {
	        		String identifier = msg.getContentRaw().replace("!music ", "");
	        		
		        	VoiceChannel myChannel = event.getGuild().getVoiceChannelsByName("General", true).get(0);
		        	AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
		   
		        	AudioSourceManagers.registerRemoteSources(playerManager);
		        	AudioPlayer player = playerManager.createPlayer();
		        	
		        	AudioPlayerSendHandler playerHandler = new AudioPlayerSendHandler(player);
		        	TrackScheduler trackScheduler = new TrackScheduler();	
		        	player.addListener(trackScheduler);
		        	
		        	if (identifier.equals("stop")) {
		        		System.out.println("Stopping player...");
		        		guild.getAudioManager().closeAudioConnection();
		        		break;
		        	}	
		        	
		        	try {
						new URL(identifier);
					} catch (MalformedURLException e) {
						identifier = ("ytsearch:"+identifier);
					}
		        	
		        	playerManager.loadItem(identifier, new AudioLoadResultHandler() {
		        	    @Override
		        	    public void trackLoaded(AudioTrack track) {
		        	        player.playTrack(track);
		        	    }
		        	    @Override
		        	    public void playlistLoaded(AudioPlaylist playlist) {
		        	    	player.playTrack(playlist.getTracks().get(0));
		        	    }
		        	    @Override
		        	    public void noMatches() {
		        	        System.out.println("Nothing found for this track");
		        	        channel.sendMessage("Sorry, Nothing found for this link.").queue();
		        	    }
		        	    @Override
		        	    public void loadFailed(FriendlyException throwable) {
		        	        // Notify the user that everything exploded
		        	    }
		        	});
		        	
		        	
		        	AudioManager audioManager = guild.getAudioManager();
		        	audioManager.setSendingHandler(playerHandler);
		        	audioManager.openAudioConnection(myChannel);
		        	
	        	}
	        	break;
	        }
	    }
	}