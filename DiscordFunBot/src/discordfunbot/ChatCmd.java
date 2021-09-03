package discordfunbot;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
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
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction.ReactionEmote;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class ChatCmd extends ListenerAdapter {

	private HashMap<String, Boolean> configOptions;
	boolean noConfig;
	MessageQuoteDatabase messageDatabase;

	ChatCmd() {
		String homePath = System.getProperty("user.home");
		File configFile = new File(homePath + "\\config.txt"); // Read config.txt for cat functions to disable.
		try {
			messageDatabase = new MessageQuoteDatabase();
			configOptions = new HashMap<String, Boolean>();
			BufferedReader br = new BufferedReader(new FileReader(configFile));
			String configLine = br.readLine();
			while (configLine != null) {
				String configOption = configLine.split("=")[0];
				String configValue = configLine.split("=")[1];

				Boolean configBool;
				switch (configValue) { // Disable command if =false, =off, =disabled
				case "false":
					configBool = false;
					break;
				case "off":
					configBool = false;
					break;
				case "disabled":
					configBool = false;
					break;
				default:
					configBool = true;
				}
				configOptions.put(configOption, configBool);
				noConfig = false;
				configLine = br.readLine();

			}
			br.close();

			configOptions.forEach((k, v) -> System.out.println("key: " + k + " value:" + v));

		} catch (IOException e) {
			System.out.println("Config file not found, enabling all chat functions.");
			noConfig = true;
		} catch (SQLException e) {
			System.out.println("ERROR: Failed to access Message Database.");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		ReactionEmote emoji     = event.getReactionEmote();
		String        messageId = event.getMessageId();
		Guild         guild     = event.getGuild();
		TextChannel   channel   = guild.getDefaultChannel();
		Message       msg       = channel.retrieveMessageById(messageId).complete(); //Blocks until complete
		String        emojiCode = emoji.getAsCodepoints();
		
		System.out.println(emojiCode);

		if (emojiCode.equals("U+1f4ac")) {
			try {
				messageDatabase.processMessage(msg);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onMessageReceived(MessageReceivedEvent event) {

		// Ignore bot messages
		if (event.getAuthor().isBot()) { // Ignore all messages from bots.
			return;
		}

		// Ignore DMs
		if (event.isFromType(ChannelType.PRIVATE)) {
			return;
		}

		// Gets the message that triggered the event
		Message msg = event.getMessage();
		MessageChannel channel = event.getChannel();
		Guild guild = event.getGuild(); // Guild = Discord server

		String messageText = msg.getContentRaw();
		Boolean runCommand = true;

		if (configOptions != null) {
			String messageCheck = messageText.replace("!", "");
			if (messageCheck.contains("music")) {
				messageCheck = "music";
			}
			if (configOptions.get(messageCheck) != null) {
				if (!configOptions.get(messageCheck)) {
					runCommand = false;
				}
			}
		}

		if ((runCommand) || (noConfig)) {

			switch (messageText) {
			case "!ping": // Respond to !ping with "Pong!"

				channel.sendMessage("Pong! ").queue();
				break;

			case "!file": // Upload file (can be image, GIFs do not animate)
				File image = new File("C:\\Users\\Justin\\Pictures\\2.png");
				channel.sendFile(image).queue();
				break;

			case "!meme": // Post a random meme from Reddit.
				String Json = "";
				String nsfw = "true";
				ObjectMapper objectMapper = new ObjectMapper();
				try {
					while (nsfw.equals("true")) { // No NSFW memes!

						// Use (https://github.com/D3vd/Meme_Api) to pull Reddit memes
						RestApiRequest memeRequest = new RestApiRequest(
								new URL("https://meme-api.herokuapp.com/gimme/wholesomememes"), null);
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

			case "!deletemessages":

				String userID = msg.getAuthor().getId();
				if (!userID.equals(guild.getOwnerId())) {
					break; // At the moment, I only want the owner
				} // to be able to delete messages (in future, this will check roles).

				MessageHistory history = new MessageHistory(channel);
				List<Message> msgs;
				msgs = history.retrievePast(100).complete();
				channel.purgeMessages(msgs);
				break;

			case "!oof": // Post an image from Dropbox

				try {
					DropboxAPIRequest dropboxRequest = new DropboxAPIRequest();
					dropboxRequest.browseFiles(); // Iterate through list of files in Dropbox folder.

					File oofFile = dropboxRequest.randomImage(); // Pick one from the list
					channel.sendFile(oofFile).queue();

				} catch (IOException | DbxException e1) {
					e1.printStackTrace();
				}

				break;

			case "!quote":
				try {
					String randomQuote = messageDatabase.randomQuote();
					channel.sendMessage(randomQuote).queue();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				break;

			default:
				if (msg.getContentRaw().contains("!music ")) {
					String identifier = msg.getContentRaw().replace("!music ", "");

					// Play music in "General" voice channel for now.
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
						new URL(identifier); // Check if a valid URL
					} catch (MalformedURLException e) {
						identifier = ("ytsearch:" + identifier); // otherwise, search Youtube

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
							// Notify the user that load failed
						}
					});

					// Connect to audio for channel.
					AudioManager audioManager = guild.getAudioManager();
					audioManager.setSendingHandler(playerHandler);
					audioManager.openAudioConnection(myChannel);
				}
				break;
			}
		}
	}
}