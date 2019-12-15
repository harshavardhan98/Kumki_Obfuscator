package dummy;

import java.io.File;

public class ThrashCode {
    //List<String> result = Unix4j.cat(Constants.projectRootDirectory + Constants.packageName + "MainActivity.java").grep("import").sed("s/import/logesh/g").toStringList();
    //System.out.println(result.toString());

    //Unix4j.cat(Constants.projectRootDirectory + Constants.packageName + "MainActivity.java").sed("s/import/logesh/g").toFile(Constants.projectRootDirectory + Constants.packageName + "MainActivity.java");

    /****************************************************************************/

    public static void list(String path, int level) {
        File folder = new File(path);
        File[] files = folder.listFiles();

        for (File file : files) {
            if (file.isFile()) {
                for(int i=0;i<level;i++)
                    System.out.print(" ");

                if(file.getName().charAt(0)!='.')
                    System.out.println(file.getName());
            }
            else if (file.isDirectory()) {
                System.out.println(file.getName());
                list(file.getAbsolutePath(),level+2);
            }
        }
    }

    /****************************************************************************/
}