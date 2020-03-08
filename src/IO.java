import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class IO {
	public static String mountPath(String System, Boolean checkPath, Object... obj) {

		String path = null;
		String bar = System == "win" ? "\\" : "//";

		path = (String) obj[0];
		for (int i = 1; i < obj.length; i++)
			path += bar + (String) obj[i];

		// Check if path is not in use
		if (checkPath)
			path = DoNotOverwrite(path);

		return path;
	}

	public static void copyFile(File sourceFile, File destFile) throws IOException {
		FileChannel source = null;
		FileChannel destination = null;

		// Create if already doest exists
		if (!destFile.exists())
			destFile.createNewFile();

		RandomAccessFile SourceAcess = new RandomAccessFile(sourceFile, "rw");
		RandomAccessFile DestinationAcess = new RandomAccessFile(destFile, "rw");

		// Get full path of file
		source = SourceAcess.getChannel();
		destination = DestinationAcess.getChannel();

		// Move file
		source.transferTo(0L, source.size(), destination);

		// Close files
		SourceAcess.close();
		DestinationAcess.close();
		source.close();
		destination.close();

	}


	public static String DoNotOverwrite(String Destination) {
		int fileNo = 0;
		String destinationVerifier = Destination;
		File DestTest = new File(Destination);

		// Increase index number untill is not overything another file
		while (DestTest.exists()) {
			fileNo++;

			int index = Destination.lastIndexOf(".");
			destinationVerifier = Destination.substring(0, index) + "(" + fileNo + ")" + Destination.substring(index);

			// Check if this file exists
			DestTest = new File(destinationVerifier);
		}

		return destinationVerifier;
	}
}
