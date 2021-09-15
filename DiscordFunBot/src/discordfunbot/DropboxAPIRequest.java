package discordfunbot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;

public class DropboxAPIRequest {

	private DbxClientV2 client;
	private DbxRequestConfig config;
	private String ACCESS_TOKEN;

	DropboxAPIRequest() throws IOException {

		String homePath = System.getProperty("user.home");

		// OAuth2 not implemented yet. Currently just generating tokens from the
		// account.
		File accessTokenTxt = new File(homePath,"dropboxtoken.txt");

		BufferedReader br = new BufferedReader(new FileReader(accessTokenTxt));
		ACCESS_TOKEN = br.readLine();
		br.close();
		System.out.println("Using access token: " + ACCESS_TOKEN);

		config = DbxRequestConfig.newBuilder("dropbox/Apps/DiscordOofBot").build();
		client = new DbxClientV2(config, ACCESS_TOKEN);

		FullAccount account;
		try {
			account = client.users().getCurrentAccount();
			System.out.println(account.getName().getDisplayName());
		} catch (DbxException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<String> browseFiles() throws ListFolderErrorException, DbxException {

		ListFolderResult result = client.files().listFolderBuilder("").withRecursive(true).start();
		ArrayList<String> filePaths = new ArrayList<String>();
		String pattern = "/.+/*.+[.].{3,}";

		while (true) {
			for (Metadata metadata : result.getEntries()) {
				String filePath = metadata.getPathLower();

				/*
				 * if ((filePath.contains(".jpg")) || (filePath.contains(".png")) ||
				 * filePath.contains(".gif")) {
				 */
				if ((filePath != null)) {
					if (Pattern.matches(pattern, filePath)) {
						filePaths.add(filePath);
						System.out.println(filePath);
					} 
					
				}
			}

			if (!result.getHasMore()) {
				break;
			}

			result = client.files().listFolderContinue(result.getCursor());

		}
		return filePaths;
	}

	public File randomImage() throws DownloadErrorException, DbxException, IOException {

		ArrayList<String> filePaths = this.browseFiles();

		int randomNum = ThreadLocalRandom.current().nextInt(0, (filePaths.size()));
		String randomPath = filePaths.get(randomNum);
		System.out.println("Random path: " + randomPath);

		DbxDownloader<FileMetadata> downloader = client.files().download(randomPath);

		File oofFile;
		Boolean isAnimated = false;

		if (randomPath.contains("gif")) {
			oofFile = new File("oof.gif");
			isAnimated = true;
		}

		else {
			String fileExt = randomPath.split("\\.")[1]; // Grabs file extension
			oofFile = new File("oof." + fileExt);
		}

		FileOutputStream fout = new FileOutputStream(oofFile);

		if (isAnimated) { // Animated GIFs need to be processed as a stream of bytes.
			InputStream is = downloader.getInputStream();
			byte[] oofBytes = IOUtils.toByteArray(is);
			FileUtils.writeByteArrayToFile(oofFile, oofBytes);
			is.close();
		}

		else {
			downloader.download(fout);
		}

		fout.flush();
		fout.close();

		return oofFile;

	}
}
