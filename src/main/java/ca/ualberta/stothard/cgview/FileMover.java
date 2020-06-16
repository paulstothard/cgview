/*   CGView - a Java package for generating high-quality, zoomable maps of
 *   circular genomes.
 *   Copyright (C) 2005 Paul Stothard stothard@ualberta.ca
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
