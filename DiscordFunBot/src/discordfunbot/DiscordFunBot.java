package discordfunbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordFunBot {

	public static void main(String[] args) throws LoginException, InterruptedException, IOException {

		// For now, storing token in "token.txt" in user's home directory.
		
		String homePath = System.getProperty("user.home");
		File botTokenTxt = new File(homePath+"\\token.txt");
		BufferedReader br = new BufferedReader(new FileReader(botTokenTxt));
		String botToken = br.readLine();
		br.close();
		
		JDA jda = JDABuilder.createDefault(botToken) // Log in to Discord using bot access token.
	            .addEventListeners(new ChatCmd())    // Attach ChatCmd to listen for messages.
	            .build();
		jda.awaitReady();
		
	}
}