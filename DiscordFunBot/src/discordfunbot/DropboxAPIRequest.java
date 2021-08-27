package discordfunbot;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.DownloadErrorException;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderErrorException;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.users.FullAccount;
import com.dropbox.core.DbxRequestConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;



public class DropboxAPIRequest {
	
	private DbxClientV2      client;
	private DbxRequestConfig config;
	private String           ACCESS_TOKEN;
	
	DropboxAPIRequest() throws IOException{
		
		String homePath = System.getProperty("user.home");
		File accessTokenTxt = new File(homePath+"\\dropboxtoken.txt");
		BufferedReader br = new BufferedReader(new FileReader(accessTokenTxt));
		ACCESS_TOKEN = br.readLine();
		br.close();
		System.out.println("Using access token: "+ACCESS_TOKEN);
		
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
		ListFolderResult result = client.files().listFolder("");
		ArrayList<String> filePaths = new ArrayList<String>();
		
		while (true) {
			for (Metadata metadata : result.getEntries()) {
				String filePath = metadata.getPathLower();
				
				if ((filePath.contains(".jpg")) || (filePath.contains(".png")) || filePath.contains(".gif")) {
					System.out.println(filePath);
					filePaths.add(filePath);
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
		
		DbxDownloader<FileMetadata> downloader = client.files().download(randomPath);
		
		File oofFile = new File("oof.jpg");
		
		FileOutputStream fout = new FileOutputStream(oofFile);
		downloader.download(fout);
		fout.flush();
		fout.close();
		
		return oofFile;
		
	}
}
