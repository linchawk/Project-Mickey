import java.io.File;
import java.io.IOException;

/*
 * Created by Mohan on 5/3/2016
 */

public class Prelaunch {

	public static void main(String[] args) {
		// This program will be executed on Windows startup
		try {
			/*
			 * args[0] -> Path of mysql bin folder, args[1] -> Path of our app's
			 * lock folder
			 */
			// First launch mysql service
			Runtime.getRuntime().exec(args[0] + "\\mysqld");
			// Delete lock file it exists for some reason
			File lockFile = new File(args[1] + "\\lck.bin");
			if (lockFile.exists())
				lockFile.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
