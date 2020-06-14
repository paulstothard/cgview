package ca.ualberta.stothard.cgview;

import java.io.*;
import java.net.*;

/**
 * This class contains methods for moving jar resources to the output directory.
 *
 * @author Paul Stothard
 */
public class FileMover {

  protected boolean moveFile(
    String filePath,
    String fileName,
    String destinationPath
  ) {
    try {
      // text files
      if (
        (fileName.endsWith("html")) ||
        (fileName.endsWith("js")) ||
        (fileName.endsWith("css"))
      ) {
        URL includeURL =
          this.getClass()
            .getClassLoader()
            .getResource(filePath + "/" + fileName);

        BufferedReader br = new BufferedReader(
          new InputStreamReader(includeURL.openStream())
        );

        Writer output = new BufferedWriter(
          new FileWriter(destinationPath + File.separator + fileName)
        );

        String line;
        while ((line = br.readLine()) != null) {
          output.write(line + System.getProperty("line.separator"));
        }
        br.close();
        output.flush();
        output.close();
      }
      // binary files or text files
      else {
        URL includeURL =
          this.getClass()
            .getClassLoader()
            .getResource(filePath + "/" + fileName);
        URLConnection connection = includeURL.openConnection();
        InputStream stream = connection.getInputStream();
        BufferedInputStream in = new BufferedInputStream(stream);
        FileOutputStream file = new FileOutputStream(
          destinationPath + File.separator + fileName
        );
        BufferedOutputStream out = new BufferedOutputStream(file);
        int i;
        while ((i = in.read()) != -1) {
          out.write(i);
        }
        out.flush();
        in.close();
        out.close();
      }

      return true;
    } catch (Exception e) {
      System.out.println(e.toString());
      return false;
    }
  }
}
