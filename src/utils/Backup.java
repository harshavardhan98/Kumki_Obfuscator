package utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import static utils.Constants.*;

public class Backup {
    public static void backupProject() {
        try {
            File sourceFolder = new File(projectDirectory);
            File destinationFolder = new File(sourceFolder.getAbsolutePath() + "1");

            String backupProjectDirectory = destinationFolder.getAbsolutePath() + File.separator;
            System.out.println("Backup is available in the path: " + backupProjectDirectory);

            copyFolder(sourceFolder, destinationFolder);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void copyFolder(File sourceFolder, File destinationFolder) {
        if (sourceFolder.isDirectory()) {
            if (!destinationFolder.exists())
                destinationFolder.mkdir();

            String[] files = sourceFolder.list();

            if (files != null) {
                for (String file : files) {
                    File srcFile = new File(sourceFolder, file);
                    File destFile = new File(destinationFolder, file);
                    copyFolder(srcFile, destFile);
                }
            }
        } else {
            try {
                Files.copy(sourceFolder.toPath(), destinationFolder.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
