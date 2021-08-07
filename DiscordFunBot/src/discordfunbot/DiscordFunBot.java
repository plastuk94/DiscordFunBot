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

public class DiscordFunBot extends ListenerAdapter {

	public static void main(String[] args) throws LoginException, InterruptedException, IOException {

		String homePath = System.getProperty("user.home");
		System.out.println(homePath);
		
		File botTokenTxt = new File(homePath+"\\token.txt");
		BufferedReader br = new BufferedReader(new FileReader(botTokenTxt));
		
		String botToken = br.readLine();
		

		
		JDA jda = JDABuilder.createDefault(botToken)
	            .addEventListeners(new ChatCmd())
	            .build();
		//TODO Pull the token from a text file instead of leaving it visible in the code.
		jda.awaitReady();
		
	}
}