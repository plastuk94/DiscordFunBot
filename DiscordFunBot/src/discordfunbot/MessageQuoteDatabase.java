package discordfunbot;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import net.dv8tion.jda.api.entities.Message;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class MessageQuoteDatabase {
	private Connection connection;
	//private Message msg;
	private Statement statement;
	private String quote;
	private String username;
	private OffsetDateTime datetime;

	MessageQuoteDatabase() throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		if (System.getProperty("os.name").contains("indows")) {
			connection = DriverManager.getConnection("jdbc:sqlite:C:/sqlite/discordquotes.db"); // Completely unnecessary and Windows specific
		} else {
			connection = DriverManager.getConnection("jdbc:sqlite:"+System.getProperty("user.home")+"/discordquotes.db"); // Preferable
		}
		
		statement = connection.createStatement();
		statement.setQueryTimeout(30);
	}

	public void processMessage(Message msg) throws SQLException {
		quote = msg.getContentRaw();
		username = msg.getAuthor().getName();
		datetime = msg.getTimeCreated();

		String time = datetime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " ");
		
		quote    = ("\'"+quote+"\'");
		username = ("\'"+username+"\'");
		time     = ("\'"+time+"\'");
	
					
		statement.executeUpdate(
				"INSERT INTO quotes (Username, Quote, Time) VALUES (" + username + ", " + quote + ", " + time + ");");

		ResultSet rs = statement.executeQuery("SELECT * FROM quotes;");

		while (rs.next()) {
			System.out.println("Username = " + rs.getString("Username"));
			System.out.println("ID = " + rs.getInt("ID"));
			System.out.println("Time = " + rs.getString("Time"));
			System.out.println("Quote = " + rs.getString("Quote"));
		}
	}

	public String randomQuote() throws SQLException {
		String randomQuote = "No quote found.";
		String Username    = "No username found";
		int ID             = 0;
		String Quote       = "No quote found";
		String Time          = null;

		ResultSet randomQuoteSet = statement.executeQuery("SELECT * FROM quotes ORDER BY RANDOM() LIMIT 1;");

		while (randomQuoteSet.next()) {

			Username = randomQuoteSet.getString("Username");
			ID       = randomQuoteSet.getInt("ID");
			Time     = randomQuoteSet.getString("Time");
			Quote    = randomQuoteSet.getString("Quote");

			System.out.println("Username = " + Username);
			System.out.println("ID = "       + ID);
			System.out.println("Time = "     + Time);
			System.out.println("Quote = "    + Quote);
	
		}
			randomQuote = ("#"+ID+" @"+Username+": "+Quote+": "+Time); //TODO ID not working, SQL primary key stuck at #0?
			return randomQuote;
		}

}
