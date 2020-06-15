package ca.ualberta.stothard.cgview;

import jargs.gnu.CmdLineParser;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.commons.lang.StringEscapeUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.*;

/**
 * This class contains static methods for converting Cgview objects to image files. Images can be
 * generated in PNG, JPG, SVG, and SVGZ (gzipped SVG) formats.
 *
 * @author Paul Stothard
 */
public class CgviewIO implements CgviewConstants {
  private static String outputDirectory;

  private static Integer legendValue;
  private static Boolean useOverlibValue;
  private static Integer heightValue;
  private static Integer widthValue;
  private static Boolean excludeSVGValue;
  private static Boolean useExternalStylesheetValue;
  private static Integer useInnerLabelsValue;
  private static Boolean removeLabelsValue;
  private static Boolean removeLegendsValue;
  private static String seriesValue;
  private static String seriesNumbersValue;

  private static Boolean embedFontsValue;

  private static Integer legendFontValue;
  private static Integer rulerFontValue;
  private static Integer labelFontValue;

  private static Double tickDensityValue;

  private static final String CGVIEW_VERSION = "CGView 1.0.0 2020-06-14";

  private static final String PROBLEM_MESSAGE =
    "The following error occurred: ";

  private static final String INCLUDES_PATH = "includes";

  private static final String INCLUDES_OUT_PATH = "includes";
  private static final String PNG_OUT_PATH = "png";
  private static final String SVG_OUT_PATH = "svg";

  private static final String ZOOM_IN_BUTTON = "expand_in.png";
  private static final String ZOOM_IN_BUTTON_OFF = "expand_in_g.png";

  private static final String ZOOM_OUT_BUTTON = "expand_out.png";
  private static final String ZOOM_OUT_BUTTON_OFF = "expand_out_g.png";

  private static final String INDEX_BUTTON = "full.png";
  private static final String INDEX_BUTTON_OFF = "full_g.png";

  private static final String MOVE_FORWARD_BUTTON = "move_forward.png";
  private static final String MOVE_FORWARD_BUTTON_OFF = "move_forward_g.png";

  private static final String MOVE_BACK_BUTTON = "move_back.png";
  private static final String MOVE_BACK_BUTTON_OFF = "move_back_g.png";

  private static final String TO_SVG_BUTTON = "as_svg.png";
  private static final String TO_PNG_BUTTON = "as_png.png";

  private static final String HELP_BUTTON = "help.png";

  private static final String HELP_FILE = "help.html";
  private static final String HELP_FILE_PNG = "help_png.html";
  private static final String STYLE_FILE = "stylesheet.css";
  private static final String SVG_JAVASCRIPT = "info.js";

  private static final String OVERLIB_JAVASCRIPT = "overlib.js";

  private static final String EXTERNAL_LEGEND = "legend.png";

  private static void printUsage() {
    System.err.println("CGView - drawing circular genome maps.");
    System.err.println("");
    System.err.println("DISPLAY HELP AND EXIT:");
    System.err.println("");
    System.err.println("  usage:");
    System.err.println("");
    System.err.println("     java -jar cgview.jar --help");
    System.err.println("");
    System.err.println("DISPLAY VERSION AND EXIT:");
    System.err.println("");
    System.err.println("  usage:");
    System.err.println("");
    System.err.println("     java -jar cgview.jar --version");
    System.err.println("");
    System.err.println("CREATING A SINGLE MAP IMAGE:");
    System.err.println("");
    System.err.println("   usage:");
    System.err.println("");
    System.err.println("      java -jar cgview.jar -i <file> -f <format> -o <file> [Options]");
    System.err.println("");
    System.err.println("   required arguments:");
    System.err.println("");
    System.err.println("      -i  Input file in CGView XML or TAB format.");
    System.err.println("      -f  Output file format: png, jpg, svg, or svgz.");
    System.err.println("      -o  Output file to create.");
    System.err.println("");
    System.err.println("   optional arguments (these override corresponding values specified in XML input):");
    System.err.println("");
    System.err.println("      -A  Font size for feature labels (default 10).");
    System.err.println("      -c  Base position to center on when using -z option (default 1).");
    System.err.println("      -D  Font size for legends (default 8).");
    System.err.println("      -d  Density of tick marks, between 0 and 1.0 (default 1.0).");
    System.err.println("      -E  Embed vector-based text in SVG output, T or F (default T).");
    System.err.println("      -H  Height of map (default 700).");
    System.err.println("      -h  HTML file to create for image map functionality.");
    System.err.println("      -I  Allow labels to be drawn on inside of circle, T or F (default F).");
    System.err.println("      -L  Width of user-supplied legend png file (legend.png) to be referenced in html output.");
    System.err.println("      -p  Path to image file in HTML file created using -h (default is -o value).");
    System.err.println("      -r  Remove legends, T or F (default F).");
    System.err.println("      -R  Remove feature labels, T or F (default F).");
    System.err.println("      -U  Font size for sequence ruler (default 8).");
    System.err.println("      -u  Include overlip.js for mouseover labels for png and jpg image maps in html output, T or F (default T).");
    System.err.println("      -w  Width of map (default 700).");
    System.err.println("      -z  Zoom multiplier (default 1).");
    System.err.println("");
    System.err.println("   example usage:");
    System.err.println("");
    System.err.println("      java -jar cgview.jar -i test.xml -o map.png -f png");
    System.err.println("");
    System.err.println("CREATING A NAVIGABLE SERIES OF LINKED MAP IMAGES:");
    System.err.println("");
    System.err.println("   usage:");
    System.err.println("");
    System.err.println("      java -jar cgview.jar -i <file> -s <directory> [Options]");
    System.err.println("");
    System.err.println("   required arguments:");
    System.err.println("");
    System.err.println("      -i  Input file in CGView XML or TAB format.");
    System.err.println("      -s  Output directory for image series.");
    System.err.println("");
    System.err.println("   optional arguments:");
    System.err.println("");
    System.err.println("      -e  Exclude SVG output from image series, T or F (default F).");
    System.err.println("      -L  Width of user-supplied legend png file (legend.png) to be referenced in html output.");
    System.err.println("      -u  Include overlip.js for mouseover labels for png and jpg image maps in html output, T or F (default T).");
    System.err.println("      -x  Zoom multipliers to use, comma-separated (default is 1,6,36).");
    System.err.println("");
    System.err.println("   example usage:");
    System.err.println("");
    System.err.println("      java -jar cgview.jar -i test.xml -o image_series");
  }

  /**
   * Writes a Cgview object to a SVG or a SVGZ file. Any mouseover or hyperlink information
   * associated with the Cgview object is embedded directly in the SVG.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param embedFonts whether or not to embed fonts in the SVG. Embedded fonts produce a much nicer
   *     map but yield a larger file.
   * @param useCompression whether or not to generate compressed SVG (SVGZ).
   * @param nextZoomValue the zoom value of the next Cgview map to draw in the series, or <code>0
   *     </code> if there is not another Cgview in the series.
   * @param keepLastLabels whether or not to use labels generated by a previous call to one of the
   *     Cgview objects draw() or drawZoomed() methods.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws SVGGraphics2DIOException
   */
  private static void writeToSVGFile(
    Cgview cgview,
    String filename,
    boolean embedFonts,
    boolean useCompression,
    int nextZoomValue,
    boolean keepLastLabels
  )
    throws FileNotFoundException, IOException, UnsupportedEncodingException, SVGGraphics2DIOException {
    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

    // Create an instance of org.w3c.dom.Document
    Document document = domImpl.createDocument(null, "svg", null);
    // String svgNS = "http://www.w3.org/2000/svg";

    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
    ctx.setComment(
      "Generated by CGView (written by Paul Stothard, University of Alberta) using the Batik SVG Generator"
    );
    ctx.setPrecision(12);

    SVGGraphics2D graphics2D;

    if (embedFonts) {
      graphics2D = new SVGGraphics2D(ctx, true);
    } else {
      graphics2D = new SVGGraphics2D(ctx, false);
    }

    //graphics2D.setSVGCanvasSize(new Dimension(cgview.getWidth(),cgview.getHeight()));

    try {
      if (cgview.getDesiredZoom() > 1.0d) {
        cgview.drawZoomed(
          graphics2D,
          cgview.getDesiredZoom(),
          cgview.getDesiredZoomCenter(),
          keepLastLabels
        );
      } else {
        cgview.draw(graphics2D, keepLastLabels);
      }

      // try adding some custom tags to the document
      // get the root element

      Element root = graphics2D.getRoot();
      // set width and height attributes
      root.setAttributeNS(null, "width", Integer.toString(cgview.getWidth()));
      root.setAttributeNS(null, "height", Integer.toString(cgview.getHeight()));

      Document factory = graphics2D.getDOMFactory();

      ArrayList labelBounds = cgview.getLabelBounds();
      Iterator i;

      boolean hasMouseover = false;
      // if hasMouseover add javascript
      i = labelBounds.iterator();
      while (i.hasNext()) {
        LabelBounds currentLabelBounds = (LabelBounds) i.next();
        if (
          (currentLabelBounds.getUse() == true) &&
          (
            (currentLabelBounds.getMouseover() != null) ||
            (
              (currentLabelBounds.getType() == BOUNDS_RULER) &&
              (nextZoomValue > 0)
            )
          )
        ) {
          hasMouseover = true;
        }
      }

      if (hasMouseover) {
        Element script = factory.createElementNS(null, "script");
        // -1 indicates that image is not part of an image series and thus javascript should
        // be embedded in the svg rather than linked to an includes directory

        if (nextZoomValue == -1) {
          String javascript =
            "//Written by Paul Stothard, University of Alberta, Canada 2004\nfunction showMouseover(evt, message) {var PADDING = 8; var X_SHIFT = 20; var Y_SHIFT = 20; var svgDoc = evt.target.ownerDocument; var translateX = svgDoc.rootElement.currentTranslate.x; var translateY = svgDoc.rootElement.currentTranslate.y; var scale = 1 / svgDoc.rootElement.currentScale; var effectiveDocWidth = svgDoc.rootElement.getAttribute(\"width\") - translateX; var effectiveDocHeight = svgDoc.rootElement.getAttribute(\"height\") - translateY; var targetText = svgDoc.getElementById(\"mouseoverBox\"); var x = evt.clientX - translateX + X_SHIFT; var y = evt.clientY - translateY + Y_SHIFT; var newText = svgDoc.createTextNode(message); targetText.replaceChild(newText, targetText.firstChild); var textBounds = targetText.getBBox(); y = y + textBounds.height; if (x + textBounds.width + PADDING > effectiveDocWidth) {x = x - (x + textBounds.width + PADDING - effectiveDocWidth); if (y > effectiveDocWidth / 2) { y = y - Y_SHIFT - Y_SHIFT - textBounds.height; } else {}} if (y + textBounds.height + PADDING > effectiveDocHeight) {y = y - (y + textBounds.height + PADDING - effectiveDocHeight); } if (x - PADDING < 0) {x = 0 + PADDING;} if (y - textBounds.height - PADDING < 0) {y = 0 + textBounds.height + PADDING;}targetText.setAttribute(\"x\", x); targetText.setAttribute(\"y\", y); textBounds = targetText.getBBox(); targetTextBackground = svgDoc.getElementById(\"mouseoverBoxBackground\"); targetTextBackground.setAttribute(\"transform\", \"scale(\" + scale + \",\" + scale + \")\"); targetTextBackground.setAttribute(\"x\", textBounds.x - PADDING / 2); targetTextBackground.setAttribute(\"y\", textBounds.y - PADDING / 2); targetTextBackground.setAttribute(\"width\", textBounds.width + PADDING); targetTextBackground.setAttribute(\"height\", textBounds.height + PADDING); targetText.setAttribute(\"transform\", \"scale(\" + scale + \",\" + scale + \")\");} function showMouseout(evt) {var svgDoc = evt.target.ownerDocument; var targetText = svgDoc.getElementById(\"mouseoverBox\"); var newText = svgDoc.createTextNode(\"\"); targetText.setAttribute(\"x\", 0); targetText.setAttribute(\"y\", 0); targetText.replaceChild(newText, targetText.firstChild); targetTextBackground = svgDoc.getElementById(\"mouseoverBoxBackground\"); targetTextBackground.setAttribute(\"x\", 0); targetTextBackground.setAttribute(\"y\", 0); targetTextBackground.setAttribute(\"width\", 0); targetTextBackground.setAttribute(\"height\", 0);}";
          // create CDATA section
          script.setAttributeNS(null, "type", "text/javascript");
          CDATASection cdata = factory.createCDATASection(javascript);
          script.appendChild(cdata);
        } else {
          script.setAttributeNS(null, "type", "text/javascript");
          script.setAttributeNS(
            null,
            "xlink:href",
            "../" + INCLUDES_PATH + "/" + SVG_JAVASCRIPT
          );
        }

        // add javascript element to root
        root.appendChild(script);

        // create a background for the mouseoverBox
        Element mouseoverBoxBackground = factory.createElementNS(null, "rect");
        mouseoverBoxBackground.setAttributeNS(
          null,
          "id",
          "mouseoverBoxBackground"
        );
        mouseoverBoxBackground.setAttributeNS(null, "x", "0");
        mouseoverBoxBackground.setAttributeNS(null, "y", "0");
        mouseoverBoxBackground.setAttributeNS(null, "width", "0");
        mouseoverBoxBackground.setAttributeNS(null, "height", "0");
        mouseoverBoxBackground.setAttributeNS(
          null,
          "style",
          "fill: rgb(204,204,255); stroke: rgb(51,51,153); stroke-width:1"
        );

        // add background element to root
        root.appendChild(mouseoverBoxBackground);

        // add a text element to show the mouseover text
        Element mouseoverBox = factory.createElementNS(null, "text");
        mouseoverBox.setAttributeNS(null, "id", "mouseoverBox");
        mouseoverBox.setAttributeNS(null, "x", "0");
        mouseoverBox.setAttributeNS(null, "y", "0");
        mouseoverBox.setAttributeNS(
          null,
          "style",
          "fill:black; stroke:none; font-family:Arial; font-size:12"
        );

        // add a text node to the element
        Text text = factory.createTextNode(" ");
        mouseoverBox.appendChild(text);

        // add mouseoverBox element to root
        root.appendChild(mouseoverBox);
      }

      i = labelBounds.iterator();

      NumberFormat nf = NumberFormat.getInstance();

      while (i.hasNext()) {
        LabelBounds currentLabelBounds = (LabelBounds) i.next();
        Rectangle2D bounds = currentLabelBounds.getBounds();

        if (
          (nextZoomValue > 0) && (currentLabelBounds.getType() == BOUNDS_RULER)
        ) {
          currentLabelBounds.setHyperlink(
            "../" +
            Integer.toString(nextZoomValue) +
            "_" +
            Integer.toString(currentLabelBounds.getBase()) +
            ".html"
          );
          currentLabelBounds.setMouseover(
            "expand " +
            nf.format((long) currentLabelBounds.getBase()) +
            " bp region"
          );
        }

        if (
          (currentLabelBounds.getUse() == true) &&
          (
            (currentLabelBounds.getMouseover() != null) ||
            (currentLabelBounds.getHyperlink() != null)
          )
        ) {
          Element rectangle = factory.createElementNS(null, "rect");
          rectangle.setAttributeNS(null, "x", Double.toString(bounds.getX()));
          rectangle.setAttributeNS(null, "y", Double.toString(bounds.getY()));
          rectangle.setAttributeNS(
            null,
            "width",
            Double.toString(bounds.getWidth())
          );
          rectangle.setAttributeNS(
            null,
            "height",
            Double.toString(bounds.getHeight())
          );
          rectangle.setAttributeNS(null, "style", "fill:none; stroke:none");
          rectangle.setAttributeNS(null, "pointer-events", "fill");

          if (
            (currentLabelBounds.getMouseover() != null) &&
            (!(currentLabelBounds.getMouseover().matches("\\S*")))
          ) {
            rectangle.setAttributeNS(
              null,
              "onmouseover",
              "showMouseover(evt, \"" +
              StringEscapeUtils.escapeXml(currentLabelBounds.getMouseover()) +
              "\")"
            );
            rectangle.setAttributeNS(null, "onmouseout", "showMouseout(evt)");
          }

          if (currentLabelBounds.getHyperlink() != null) {
            // create hyperlink element
            Element hyperlink = factory.createElementNS(null, "a");
            hyperlink.setAttributeNS(
              null,
              "xlink:href",
              StringEscapeUtils.escapeXml(currentLabelBounds.getHyperlink())
            );
            hyperlink.appendChild(rectangle);
            root.appendChild(hyperlink);
          } else {
            root.appendChild(rectangle);
          }
        }
      }

      System.out.println("Writing picture to " + filename);
      boolean useCSS = true;

      FileOutputStream fileOutputStream = new FileOutputStream(
        new File(filename)
      );

      if (useCompression) {
        GZIPOutputStream gzipOut = new GZIPOutputStream(fileOutputStream);
        Writer out = new OutputStreamWriter(gzipOut, "UTF-8");
        ///graphics2D.stream(out, useCSS);
        ////graphics2D.stream(root, out, useCSS);
        graphics2D.stream(root, out);
        out.flush();
        gzipOut.flush();
        out.close();
        gzipOut.close();
      } else {
        Writer out = new OutputStreamWriter(fileOutputStream, "UTF-8");
        ///graphics2D.stream(out, useCSS);
        ////graphics2D.stream(root, out, useCSS);
        graphics2D.stream(root, out);
        out.flush();
        out.close();
      }
    } finally {
      graphics2D.dispose();
    }
  }

  /**
   * Writes a Cgview object to a SVG or a SVGZ file. Any mouseover or hyperlink information
   * associated with the Cgview object is embedded directly in the SVG.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param embedFonts whether or not to embed fonts in the SVG. Embedded fonts produce a much nicer
   *     map but yield a larger file.
   * @param useCompression whether or not to generate compressed SVG (SVGZ).
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws SVGGraphics2DIOException
   */
  public static void writeToSVGFile(
    Cgview cgview,
    String filename,
    boolean embedFonts,
    boolean useCompression
  )
    throws FileNotFoundException, IOException, UnsupportedEncodingException, SVGGraphics2DIOException {
    writeToSVGFile(cgview, filename, embedFonts, useCompression, -1, false);
  }

  /**
   * Writes a Cgview object to a SVG or a SVGZ file. Any mouseover or hyperlink information
   * associated with the Cgview object is embedded directly in the SVG.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param embedFonts whether or not to embed fonts in the SVG. Embedded fonts produce a much nicer
   *     map but yield a larger file.
   * @param useCompression whether or not to generate compressed SVG (SVGZ).
   * @param keepLastLabels whether or not to use labels generated by a previous call to one of the
   *     Cgview objects draw() or drawZoomed() methods.
   * @throws FileNotFoundException
   * @throws IOException
   * @throws UnsupportedEncodingException
   * @throws SVGGraphics2DIOException
   */
  public static void writeToSVGFile(
    Cgview cgview,
    String filename,
    boolean embedFonts,
    boolean useCompression,
    boolean keepLastLabels
  )
    throws FileNotFoundException, IOException, UnsupportedEncodingException, SVGGraphics2DIOException {
    writeToSVGFile(
      cgview,
      filename,
      embedFonts,
      useCompression,
      -1,
      keepLastLabels
    );
  }

  /**
   * Writes a Cgview object to a PNG file.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param keepLastLabels whether or not to use labels generated by a previous call to one of the
   *     Cgview objects draw() or drawZoomed() methods.
   * @throws IOException
   */
  public static void writeToPNGFile(
    Cgview cgview,
    String filename,
    boolean keepLastLabels
  )
    throws IOException {
    BufferedImage buffImage = new BufferedImage(
      cgview.getWidth(),
      cgview.getHeight(),
      BufferedImage.TYPE_INT_RGB
    );

    Graphics2D graphics2D = buffImage.createGraphics();
    try {
      if (cgview.getDesiredZoom() > 1.0d) {
        cgview.drawZoomed(
          graphics2D,
          cgview.getDesiredZoom(),
          cgview.getDesiredZoomCenter(),
          keepLastLabels
        );
      } else {
        cgview.draw(graphics2D, keepLastLabels);
      }
      System.out.println("Writing picture to " + filename);
      ImageIO.write(buffImage, "PNG", new File(filename));
    } finally {
      graphics2D.dispose();
    }
  }

  /**
   * Writes a Cgview object to a PNG file.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @throws IOException
   */
  public static void writeToPNGFile(Cgview cgview, String filename)
    throws IOException {
    writeToPNGFile(cgview, filename, false);
  }

  /**
   * Writes a Cgview object to a JPG file.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param keepLastLabels whether or not to use labels generated by a previous call to one of the
   *     Cgview objects draw() or drawZoomed() methods.
   * @throws IOException
   */
  public static void writeToJPGFile(
    Cgview cgview,
    String filename,
    boolean keepLastLabels
  )
    throws IOException {
    BufferedImage buffImage = new BufferedImage(
      cgview.getWidth(),
      cgview.getHeight(),
      BufferedImage.TYPE_INT_RGB
    );

    Graphics2D graphics2D = buffImage.createGraphics();
    try {
      if (cgview.getDesiredZoom() > 1.0d) {
        cgview.drawZoomed(
          graphics2D,
          cgview.getDesiredZoom(),
          cgview.getDesiredZoomCenter(),
          keepLastLabels
        );
      } else {
        cgview.draw(graphics2D, keepLastLabels);
      }
      System.out.println("Writing picture to " + filename);
      ImageIO.write(buffImage, "JPG", new File(filename));
    } finally {
      graphics2D.dispose();
    }
  }

  /**
   * Writes a Cgview object to a JPG file.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @throws IOException
   */
  public static void writeToJPGFile(Cgview cgview, String filename)
    throws IOException {
    writeToJPGFile(cgview, filename, false);
  }

  /**
   * Creates an HTML file that links to a Cgview map. Any hyperlink or mouseover information
   * associated with the Cgview object is included in an image map in the HTML file (in the case of
   * PNG and JPG maps). In the case of SVG and SVGZ maps, the mouseover and hyperlink information is
   * embedded directly in the SVG.
   *
   * @param cgview the Cgview object embedded in the HTML.
   * @param imageFilename the name of the image file containing the Cgview image.
   * @param imageFormat the format of the image file.
   * @param htmlFilename the HTML file to generate.
   * @param zoomOutFile the HTML file to link the 'zoom out' button to, or <code>null</code> if no
   *     button should be included.
   * @param zoomInFile the HTML file to link the 'zoom in' button to, or <code>null</code> if no
   *     button should be included.
   * @param clockwiseFile the HTML file to link the 'move clockwise' button to, or <code>null</code>
   *     if no button should be included.
   * @param counterclockwiseFile the HTML file to link the 'move counterclockwise' button to, or
   *     <code>null</code> if no button should be included.
   * @throws FileNotFoundException
   * @throws IOException
   */
  private static void writeHTMLFile(
    Cgview cgview,
    String imageFilename,
    String imageFormat,
    String htmlFilename,
    String zoomOutFile,
    String zoomInFile,
    String clockwiseFile,
    String counterclockwiseFile
  )
    throws FileNotFoundException, IOException {
    String title;
    int tableWidth;

    if (legendValue == null) {
      tableWidth = cgview.getWidth();
    } else {
      tableWidth = cgview.getWidth() + legendValue.intValue();
    }

    if ((cgview.getTitle() == null) || (cgview.getTitle().equals(""))) {
      // title = "zoom = " + Double.toString(cgview.getDesiredZoom()) + ", center = " +
      // Integer.toString(cgview.getDesiredZoomCenter());
      title = "CGView map";
    } else {
      // title = cgview.getTitle() + ", zoom = " + Double.toString(cgview.getDesiredZoom()) + ",
      // center = " + Integer.toString(cgview.getDesiredZoomCenter());
      title = cgview.getTitle();
    }

    CgviewHTMLDocument htmlDocument = new CgviewHTMLDocument();

    htmlDocument.addHeader(
      title,
      Double.toString(cgview.getDesiredZoom()),
      Integer.toString(cgview.getDesiredZoomCenter()),
      imageFormat,
      useOverlibValue,
      useExternalStylesheetValue
    );

    // start master table
    htmlDocument.addTableStart(tableWidth);
    // first table row
    htmlDocument.addTableRowStart();
    if (seriesValue != null) {
      htmlDocument.addTableColumnCenterStart();
    } else {
      htmlDocument.addTableColumnStart();
    }

    // start image table
    htmlDocument.addTableStart(tableWidth, 1);
    htmlDocument.addTableRowStart();

    if (seriesValue != null) {
      htmlDocument.addTableColumnCenterStart();
    } else {
      htmlDocument.addTableColumnStart();
    }

    if (
      (imageFormat.equalsIgnoreCase("png")) ||
      (imageFormat.equalsIgnoreCase("jpg"))
    ) {
      ArrayList labelBounds = cgview.getLabelBounds();
      boolean makeImageMap = false;
      Iterator i;
      i = labelBounds.iterator();

      while (i.hasNext()) {
        LabelBounds currentLabelBounds = (LabelBounds) i.next();
        if (
          (currentLabelBounds.getUse() == true) &&
          (
            (currentLabelBounds.getMouseover() != null) ||
            (currentLabelBounds.getHyperlink() != null)
          )
        ) {
          makeImageMap = true;
          break;
        }
      }

      if (makeImageMap) {
        htmlDocument.addImageMap(
          imageFilename,
          cgview.getWidth(),
          cgview.getHeight(),
          labelBounds,
          useOverlibValue
        );
      } else {
        htmlDocument.addImage(
          imageFilename,
          cgview.getWidth(),
          cgview.getHeight()
        );
      }
    } else if (
      (imageFormat.equalsIgnoreCase("svg")) ||
      (imageFormat.equalsIgnoreCase("svgz"))
    ) {
      htmlDocument.addSVG(imageFilename, cgview.getWidth(), cgview.getHeight());
    }

    htmlDocument.addTableColumnEnd();

    // if space needs to be reserved for an external legend, add the space here.
    if (legendValue != null) {
      if (seriesValue != null) {
        htmlDocument.addTableColumnCenterStart();
        // add image tag
        htmlDocument.addImage(
          INCLUDES_OUT_PATH + "/" + EXTERNAL_LEGEND,
          legendValue.intValue(),
          cgview.getHeight(),
          "Legend Image"
        );
      } else {
        htmlDocument.addTableColumnStart();
        htmlDocument.addImage(
          EXTERNAL_LEGEND,
          legendValue.intValue(),
          cgview.getHeight(),
          "Legend Image"
        );
      }

      htmlDocument.addTableColumnEnd();
    }

    htmlDocument.addTableRowEnd();
    htmlDocument.addTableEnd();

    htmlDocument.addTableColumnEnd();
    htmlDocument.addTableRowEnd();

    if (seriesValue != null) {
      // add table row
      htmlDocument.addTableRowStart();
      htmlDocument.addTableColumnCenterStart();

      if (zoomOutFile != null) {
        htmlDocument.addButton(
          INCLUDES_OUT_PATH + "/" + ZOOM_OUT_BUTTON,
          zoomOutFile,
          "[Expand -]"
        );
      } else {
        htmlDocument.addButtonNoLink(
          INCLUDES_OUT_PATH + "/" + ZOOM_OUT_BUTTON_OFF,
          "[Expand -]"
        );
      }

      if (zoomInFile != null) {
        htmlDocument.addButton(
          INCLUDES_OUT_PATH + "/" + ZOOM_IN_BUTTON,
          zoomInFile,
          "[Expand +]"
        );
      } else {
        htmlDocument.addButtonNoLink(
          INCLUDES_OUT_PATH + "/" + ZOOM_IN_BUTTON_OFF,
          "[Expand +]"
        );
      }

      if (zoomOutFile != null) {
        if (imageFormat.equalsIgnoreCase("svgz")) {
          // htmlDocument.addButton(INCLUDES_OUT_PATH + "/" + INDEX_BUTTON, "index_svg.html", "[Full
          // view]");
          htmlDocument.addButton(
            INCLUDES_OUT_PATH + "/" + INDEX_BUTTON,
            "index.html",
            "[Full view]"
          );
        } else if (imageFormat.equalsIgnoreCase("png")) {
          htmlDocument.addButton(
            INCLUDES_OUT_PATH + "/" + INDEX_BUTTON,
            "index.html",
            "[Full view]"
          );
        }
      } else {
        htmlDocument.addButtonNoLink(
          INCLUDES_OUT_PATH + "/" + INDEX_BUTTON_OFF,
          "[Full view]"
        );
      }

      if (counterclockwiseFile != null) {
        htmlDocument.addButton(
          INCLUDES_OUT_PATH + "/" + MOVE_BACK_BUTTON,
          counterclockwiseFile,
          "[Rotate -]"
        );
      } else {
        htmlDocument.addButtonNoLink(
          INCLUDES_OUT_PATH + "/" + MOVE_BACK_BUTTON_OFF,
          "[Rotate -]"
        );
      }

      if (clockwiseFile != null) {
        htmlDocument.addButton(
          INCLUDES_OUT_PATH + "/" + MOVE_FORWARD_BUTTON,
          clockwiseFile,
          "[Rotate +]"
        );
      } else {
        htmlDocument.addButtonNoLink(
          INCLUDES_OUT_PATH + "/" + MOVE_FORWARD_BUTTON_OFF,
          "[Rotate +]"
        );
      }

      if (!excludeSVGValue.booleanValue()) {
        if (imageFormat.equalsIgnoreCase("svgz")) {
          htmlDocument.addButton(
            INCLUDES_OUT_PATH + "/" + TO_PNG_BUTTON,
            Integer.toString((int) cgview.getDesiredZoom()) +
            "_" +
            Integer.toString(cgview.getDesiredZoomCenter()) +
            "." +
            "html",
            "[View as PNG]"
          );
        } else if (imageFormat.equalsIgnoreCase("png")) {
          htmlDocument.addButton(
            INCLUDES_OUT_PATH + "/" + TO_SVG_BUTTON,
            Integer.toString((int) cgview.getDesiredZoom()) +
            "_" +
            Integer.toString(cgview.getDesiredZoomCenter()) +
            "_svg." +
            "html",
            "[View as SVG]"
          );
        }
      }

      if (!excludeSVGValue.booleanValue()) {
        htmlDocument.addButton(
          INCLUDES_OUT_PATH + "/" + HELP_BUTTON,
          INCLUDES_OUT_PATH + "/" + HELP_FILE,
          "[Help]"
        );
      } else {
        htmlDocument.addButton(
          INCLUDES_OUT_PATH + "/" + HELP_BUTTON,
          INCLUDES_OUT_PATH + "/" + HELP_FILE_PNG,
          "[Help]"
        );
      }

      // end table row
      htmlDocument.addTableColumnEnd();

      htmlDocument.addTableRowEnd();

      // add row for format information
      htmlDocument.addTableRowStart();
      htmlDocument.addTableColumnCenterStart();

      htmlDocument.addTableStart(tableWidth);

      htmlDocument.addTableRowStart();
      htmlDocument.addTableColumnLeftStart();

      htmlDocument.addSpanNoteStart();

      if (zoomInFile != null) {
        htmlDocument.addText("Click tick marks to expand the view.");
      }

      if ((zoomInFile == null) && (zoomOutFile != null)) {
        htmlDocument.addText("This is a fully expanded view.");
      }

      htmlDocument.addSpanEnd();

      htmlDocument.addTableColumnEnd();

      htmlDocument.addTableColumnRightStart();

      htmlDocument.addSpanFileSizeStart();

      NumberFormat nf = NumberFormat.getInstance();

      File svgFile = null;
      long svgLength = 0;

      if (!excludeSVGValue.booleanValue()) {
        svgFile =
          new File(
            outputDirectory +
            File.separator +
            SVG_OUT_PATH +
            File.separator +
            Integer.toString((int) cgview.getDesiredZoom()) +
            "_" +
            Integer.toString(cgview.getDesiredZoomCenter()) +
            ".svgz"
          );
        svgLength = svgFile.length();
        svgLength = (long) Math.floor((float) svgLength / 1000.0f + 0.5f);
      }

      File pngFile = new File(
        outputDirectory +
        File.separator +
        PNG_OUT_PATH +
        File.separator +
        Integer.toString((int) cgview.getDesiredZoom()) +
        "_" +
        Integer.toString(cgview.getDesiredZoomCenter()) +
        ".png"
      );

      long pngLength = pngFile.length();

      pngLength = (long) Math.floor((float) pngLength / 1000.0f + 0.5f);

      if (!excludeSVGValue.booleanValue()) {
        if (imageFormat.equalsIgnoreCase("svgz")) {
          htmlDocument.addText("Displayed SVG file size: ");
          if (svgLength > 1000) {
            htmlDocument.addSpanWarningStart();
            htmlDocument.addText(nf.format(svgLength) + " kb; ");
            htmlDocument.addSpanEnd();
          } else {
            htmlDocument.addText(nf.format(svgLength) + " kb; ");
          }
          htmlDocument.addText("PNG file size: ");
          if (pngLength > 1000) {
            htmlDocument.addSpanWarningStart();
            htmlDocument.addText(nf.format(pngLength) + " kb.");
            htmlDocument.addSpanEnd();
          } else {
            htmlDocument.addText(nf.format(pngLength) + " kb.");
          }
        } else if (imageFormat.equalsIgnoreCase("png")) {
          htmlDocument.addText("Displayed PNG file size: ");
          if (pngLength > 1000) {
            htmlDocument.addSpanWarningStart();
            htmlDocument.addText(nf.format(pngLength) + " kb; ");
            htmlDocument.addSpanEnd();
          } else {
            htmlDocument.addText(nf.format(pngLength) + " kb; ");
          }
          htmlDocument.addText("SVG file size: ");
          if (svgLength > 1000) {
            htmlDocument.addSpanWarningStart();
            htmlDocument.addText(nf.format(svgLength) + " kb.");
            htmlDocument.addSpanEnd();
          } else {
            htmlDocument.addText(nf.format(svgLength) + " kb.");
          }
        }
      } else {
        htmlDocument.addText("Displayed PNG file size: ");
        if (pngLength > 1000) {
          htmlDocument.addSpanWarningStart();
          htmlDocument.addText(nf.format(pngLength) + " kb.");
          htmlDocument.addSpanEnd();
        } else {
          htmlDocument.addText(nf.format(pngLength) + " kb.");
        }
      }

      htmlDocument.addSpanEnd();

      htmlDocument.addTableColumnEnd();
      htmlDocument.addTableRowEnd();

      htmlDocument.addTableRowStart();

      htmlDocument.addTableColumnLeftStart();
      htmlDocument.addSpanValidStart();
      htmlDocument.addValidationInfo();
      htmlDocument.addSpanEnd();
      htmlDocument.addTableColumnEnd();

      long centerBase = (long) Math.floor(
        (float) cgview.getDesiredZoomCenter() + 0.5f
      );

      htmlDocument.addTableColumnRightStart();
      htmlDocument.addSpanFileSizeStart();
      htmlDocument.addText(
        "Centered on base " +
        nf.format(centerBase) +
        "; Zoom = " +
        Integer.toString((int) cgview.getDesiredZoom()) +
        "."
      );
      htmlDocument.addSpanEnd();
      htmlDocument.addTableColumnEnd();

      htmlDocument.addTableRowEnd();

      htmlDocument.addTableEnd();

      htmlDocument.addTableColumnEnd();

      htmlDocument.addTableRowEnd();
    }

    // end table
    htmlDocument.addTableEnd();

    htmlDocument.addFooter();

    try {
      System.out.println("Writing HTML to " + htmlFilename);
      BufferedWriter out = new BufferedWriter(new FileWriter(htmlFilename));
      ArrayList contents = htmlDocument.getContents();
      Iterator i;
      i = contents.iterator();
      while (i.hasNext()) {
        out.write((String) i.next());
      }
      out.close();
    } finally {}
  }

  /**
   * Creates an HTML file that links to a Cgview map. Any hyperlink or mouseover information
   * associated with the Cgview object is included in an image map in the HTML file (in the case of
   * PNG and JPG maps). In the case of SVG and SVGZ maps, the mouseover and hyperlink information is
   * embedded directly in the SVG.
   *
   * @param cgview the Cgview object embedded in the HTML.
   * @param imageFilename the name of the image file containing the Cgview image.
   * @param imageFormat the format of the image file.
   * @param htmlFilename the HTML file to generate.
   */
  public static void writeHTMLFile(
    Cgview cgview,
    String imageFilename,
    String imageFormat,
    String htmlFilename
  )
    throws FileNotFoundException, IOException {
    writeHTMLFile(
      cgview,
      imageFilename,
      imageFormat,
      htmlFilename,
      null,
      null,
      null,
      null
    );
  }

  /**
   * Creates an HTML file that links to a Cgview map. Any hyperlink or mouseover information
   * associated with the Cgview object is included in an image map in the HTML file (in the case of
   * PNG and JPG maps). In the case of SVG and SVGZ maps, the mouseover and hyperlink information is
   * embedded directly in the SVG.
   *
   * @param cgview the Cgview object embedded in the HTML.
   * @param imageFilename the name of the image file containing the Cgview image.
   * @param imageFormat the format of the image file.
   * @param htmlFilename the HTML file to generate.
   * @param useOverlib whether to use the overlib javascript library for mouseovers (recommended)
   */
  public static void writeHTMLFile(
    Cgview cgview,
    String imageFilename,
    String imageFormat,
    String htmlFilename,
    boolean useOverlib
  )
    throws FileNotFoundException, IOException {
    useOverlibValue = new Boolean(useOverlib);
    writeHTMLFile(
      cgview,
      imageFilename,
      imageFormat,
      htmlFilename,
      null,
      null,
      null,
      null
    );
  }

  /**
   * Writes a Cgview object to an image file.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param imageFormat the image format to create ("svg", "svgz", "png", or "jpg").
   * @param embedFonts whether or not to embed fonts in the SVG. Embedded fonts produce a much nicer
   *     map but yield a larger file.
   * @param nextZoomValue the zoom value of the next Cgview map to draw in the series, or <code>0
   *     </code> if there is not another Cgview in the series.
   */
  private static void writeImageToFile(
    Cgview cgview,
    String filename,
    String imageFormat,
    boolean embedFonts,
    int nextZoomValue
  ) {
    writeImageToFile(
      cgview,
      filename,
      imageFormat,
      embedFonts,
      nextZoomValue,
      false
    );
  }

  /**
   * Writes a Cgview object to an image file.
   *
   * @param cgview the Cgview object.
   * @param filename the file to create.
   * @param imageFormat the image format to create ("svg", "svgz", "png", or "jpg").
   * @param embedFonts whether or not to embed fonts in the SVG. Embedded fonts produce a much nicer
   *     map but yield a larger file.
   * @param nextZoomValue the zoom value of the next Cgview map to draw in the series, or <code>0
   *     </code> if there is not another Cgview in the series.
   */
  private static void writeImageToFile(
    Cgview cgview,
    String filename,
    String imageFormat,
    boolean embedFonts,
    int nextZoomValue,
    boolean keepLastLabels
  ) {
    try {
      if (imageFormat.equalsIgnoreCase("svg")) {
        writeToSVGFile(
          cgview,
          filename,
          embedFonts,
          false,
          nextZoomValue,
          keepLastLabels
        );
      } else if (imageFormat.equalsIgnoreCase("svgz")) {
        writeToSVGFile(
          cgview,
          filename,
          embedFonts,
          true,
          nextZoomValue,
          keepLastLabels
        );
      } else if (imageFormat.equalsIgnoreCase("png")) {
        writeToPNGFile(cgview, filename, keepLastLabels);
      } else if (imageFormat.equalsIgnoreCase("jpg")) {
        writeToJPGFile(cgview, filename, keepLastLabels);
      } else {
        System.err.println("The output format was not recognized.");
        System.exit(1);
      }
    } catch (SVGGraphics2DIOException e) {
      e.printStackTrace(System.err);
      System.err.println(PROBLEM_MESSAGE + e.toString());
      System.exit(1);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace(System.err);
      System.err.println(PROBLEM_MESSAGE + e.toString());
      System.exit(1);
    } catch (IOException e) {
      e.printStackTrace(System.err);
      System.err.println(PROBLEM_MESSAGE + e.toString());
      System.exit(1);
    }
  }

  /**
   * Creates an HTML file that links to a Cgview map. Any hyperlink or mouseover information
   * associated with the Cgview object is included in an image map in the HTML file (in the case of
   * PNG and JPG maps). In the case of SVG and SVGZ maps, the mouseover and hyperlink information is
   * embedded directly in the SVG.
   *
   * @param cgview the Cgview object embedded in the HTML.
   * @param imageFilename the name of the image file containing the Cgview image.
   * @param imageFormat the format of the image file.
   * @param htmlFilename the HTML file to generate.
   * @param zoomOutFile the HTML file to link the 'zoom out' button to, or <code>null</code> if no
   *     button should be included.
   * @param zoomInFile the HTML file to link the 'zoom in' button to, or <code>null</code> if no
   *     button should be included.
   * @param clockwiseFile the HTML file to link the 'move clockwise' button to, or <code>null</code>
   *     if no button should be included.
   * @param counterclockwiseFile the HTML file to link the 'move counterclockwise' button to, or
   *     <code>null</code> if no button should be included.
   */
  private static void writeHTMLToFile(
    Cgview cgview,
    String imageFilename,
    String imageFormat,
    String htmlFilename,
    String zoomOutFile,
    String zoomInFile,
    String clockwiseFile,
    String counterclockwiseFile
  ) {
    try {
      writeHTMLFile(
        cgview,
        imageFilename,
        imageFormat,
        htmlFilename,
        zoomOutFile,
        zoomInFile,
        clockwiseFile,
        counterclockwiseFile
      );
    } catch (IOException e) {
      e.printStackTrace(System.err);
      System.err.println(PROBLEM_MESSAGE + e.toString());
      System.exit(1);
    }
  }

  public static void main(String args[]) {
    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option help = parser.addBooleanOption("help");
    CmdLineParser.Option version = parser.addBooleanOption("version");    
    CmdLineParser.Option labelFont = parser.addIntegerOption('A', "labelFont");
    CmdLineParser.Option centerBase = parser.addIntegerOption(
      'c',
      "centerBase"
    );
    CmdLineParser.Option legendFont = parser.addIntegerOption(
      'D',
      "legendFont"
    );
    CmdLineParser.Option tickDensity = parser.addDoubleOption(
      'd',
      "tickDensity"
    );
    CmdLineParser.Option embedFonts = parser.addStringOption('E', "embedFonts");
    CmdLineParser.Option excludeSVG = parser.addStringOption('e', "excludeSVG");
    CmdLineParser.Option format = parser.addStringOption('f', "formatOfOutput");
    CmdLineParser.Option html = parser.addStringOption('h', "htmlFile");
    CmdLineParser.Option height = parser.addIntegerOption('H', "heightOfMap");
    CmdLineParser.Option input = parser.addStringOption('i', "inputFile");
    CmdLineParser.Option innerLabels = parser.addStringOption(
      'I',
      "innerLabels"
    );
    CmdLineParser.Option legend = parser.addIntegerOption('L', "legendSpace");
    CmdLineParser.Option output = parser.addStringOption('o', "outputFile");
    CmdLineParser.Option path = parser.addStringOption('p', "pathToImage");

    CmdLineParser.Option removeLegends = parser.addStringOption(
      'r',
      "removeLegends"
    );
    CmdLineParser.Option removeLabels = parser.addStringOption(
      'R',
      "removeLabels"
    );

    CmdLineParser.Option series = parser.addStringOption(
      's',
      "seriesDirectory"
    );
    CmdLineParser.Option stylesheet = parser.addStringOption('S', "stylesheet");
    CmdLineParser.Option useOverlib = parser.addStringOption('u', "useOverlib");
    CmdLineParser.Option rulerFont = parser.addIntegerOption('U', "rulerFont");
    CmdLineParser.Option width = parser.addIntegerOption('W', "widthOfMap");
    CmdLineParser.Option seriesNumbers = parser.addStringOption(
      'x',
      "seriesNumbers"
    );
    CmdLineParser.Option zoom = parser.addDoubleOption('z', "zoomAmount");

    try {
      parser.parse(args);
    } catch (CmdLineParser.OptionException e) {
      System.err.println(e.getMessage());
      printUsage();
      System.exit(1);
    }

    // Process help and version options first
    if (Boolean.TRUE.equals((Boolean) parser.getOptionValue(help))) {
      printUsage();
      System.exit(0);
    }

    if (Boolean.TRUE.equals((Boolean) parser.getOptionValue(version))) {
      System.err.println(CGVIEW_VERSION);
      System.exit(0);
    }    

    // Extract the values entered for the various options -- if the
    // options were not specified, the corresponding values will be
    // null.
    Integer centerBaseValue = (Integer) parser.getOptionValue(centerBase);
    String embedFontsValueString = (String) parser.getOptionValue(embedFonts);
    String excludeSVGValueString = (String) parser.getOptionValue(excludeSVG);
    String formatValue = (String) parser.getOptionValue(format);
    String htmlValue = (String) parser.getOptionValue(html);
    String inputValue = (String) parser.getOptionValue(input);
    String useInnerLabelsValueString = (String) parser.getOptionValue(
      innerLabels
    );
    legendValue = (Integer) parser.getOptionValue(legend);
    String outputValue = (String) parser.getOptionValue(output);
    String pathValue = (String) parser.getOptionValue(path);
    seriesValue = (String) parser.getOptionValue(series);
    String useExternalStylesheetValueString = (String) parser.getOptionValue(
      stylesheet
    );
    String useOverlibValueString = (String) parser.getOptionValue(useOverlib);
    Double zoomValue = (Double) parser.getOptionValue(zoom);

    heightValue = (Integer) parser.getOptionValue(height);
    widthValue = (Integer) parser.getOptionValue(width);

    String removeLabelsValueString = (String) parser.getOptionValue(
      removeLabels
    );
    String removeLegendsValueString = (String) parser.getOptionValue(
      removeLegends
    );

    legendFontValue = (Integer) parser.getOptionValue(legendFont);
    rulerFontValue = (Integer) parser.getOptionValue(rulerFont);
    labelFontValue = (Integer) parser.getOptionValue(labelFont);

    tickDensityValue = (Double) parser.getOptionValue(tickDensity);

    seriesNumbersValue = (String) parser.getOptionValue(seriesNumbers);

    if (formatValue == null) {
      formatValue = "png";
    }
    if (inputValue == null) {
      System.err.println("Please specify an input file name");
      printUsage();
      System.exit(1);
    }
    if ((outputValue == null) && (seriesValue == null)) {
      System.err.println("Please specify an output file name");
      printUsage();
      System.exit(1);
    }
    if (centerBaseValue == null) {
      centerBaseValue = new Integer(1);
    }
    if (zoomValue == null) {
      zoomValue = new Double(1.0d);
    }

    if (useOverlibValueString == null) {
      useOverlibValue = new Boolean(true);
    } else if (
      (useOverlibValueString.equalsIgnoreCase("t")) ||
      (useOverlibValueString.equalsIgnoreCase("true"))
    ) {
      useOverlibValue = new Boolean(true);
    } else {
      useOverlibValue = new Boolean(false);
    }

    if (useInnerLabelsValueString == null) {
      useInnerLabelsValue = null;
    } else if (
      (useInnerLabelsValueString.equalsIgnoreCase("t")) ||
      (useInnerLabelsValueString.equalsIgnoreCase("true"))
    ) {
      useInnerLabelsValue = (Integer) INNER_LABELS_SHOW;
    } else {
      useInnerLabelsValue = (Integer) INNER_LABELS_NO_SHOW;
    }

    if (excludeSVGValueString == null) {
      excludeSVGValue = new Boolean(false);
    } else if (
      (excludeSVGValueString.equalsIgnoreCase("t")) ||
      (excludeSVGValueString.equalsIgnoreCase("true"))
    ) {
      excludeSVGValue = new Boolean(true);
    } else {
      excludeSVGValue = new Boolean(false);
    }

    if (embedFontsValueString == null) {
      embedFontsValue = new Boolean(true);
    } else if (
      (embedFontsValueString.equalsIgnoreCase("t")) ||
      (embedFontsValueString.equalsIgnoreCase("true"))
    ) {
      embedFontsValue = new Boolean(true);
    } else {
      embedFontsValue = new Boolean(true);
    }

    if (useExternalStylesheetValueString == null) {
      useExternalStylesheetValue = new Boolean(false);
    } else if (
      (useExternalStylesheetValueString.equalsIgnoreCase("t")) ||
      (useExternalStylesheetValueString.equalsIgnoreCase("true"))
    ) {
      useExternalStylesheetValue = new Boolean(true);
    } else {
      useExternalStylesheetValue = new Boolean(false);
    }

    if (removeLabelsValueString == null) {
      removeLabelsValue = new Boolean(false);
    } else if (
      (removeLabelsValueString.equalsIgnoreCase("t")) ||
      (removeLabelsValueString.equalsIgnoreCase("true"))
    ) {
      removeLabelsValue = new Boolean(true);
    } else {
      removeLabelsValue = new Boolean(false);
    }

    if (removeLegendsValueString == null) {
      removeLegendsValue = new Boolean(false);
    } else if (
      (removeLegendsValueString.equalsIgnoreCase("t")) ||
      (removeLegendsValueString.equalsIgnoreCase("true"))
    ) {
      removeLegendsValue = new Boolean(true);
    } else {
      removeLegendsValue = new Boolean(false);
    }

    // determine input format type
    String inputFormat = "xml";
    if (inputValue.length() >= 3) {
      inputFormat = inputValue.substring(inputValue.length() - 3);
    }

    // if not generating an image series, generate the single image
    if (seriesValue == null) {
      Cgview cgview = new Cgview(1);
      if (inputFormat.equalsIgnoreCase("xml")) {
        try {
          CgviewFactory cgviewFactory = new CgviewFactory();

          if (legendFontValue != null) {
            cgviewFactory.setLegendFontSize(legendFontValue.intValue());
          }
          if (labelFontValue != null) {
            cgviewFactory.setLabelFontSize(labelFontValue.intValue());
          }
          if (rulerFontValue != null) {
            cgviewFactory.setRulerFontSize(rulerFontValue.intValue());
          }

          cgview = cgviewFactory.createCgviewFromFile(inputValue);

          if (heightValue != null) {
            cgview.setHeight(heightValue.intValue());
          }

          if (widthValue != null) {
            cgview.setWidth(widthValue.intValue());
          }

          if (removeLabelsValue.booleanValue()) {
            cgview.setGlobalLabel(LABEL_NONE);
          }

          if (removeLegendsValue.booleanValue()) {
            cgview.setDrawLegends(false);
          }

          if (useInnerLabelsValue != null) {
            cgview.setUseInnerLabels(useInnerLabelsValue.intValue());
          }

          if (tickDensityValue != null) {
            cgview.setTickDensity(tickDensityValue.doubleValue());
          }

          cgview.setDesiredZoomCenter(centerBaseValue.intValue());
          cgview.setDesiredZoom(zoomValue.doubleValue());
        } catch (SAXException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (inputFormat.equalsIgnoreCase("tab")) {
        try {
          CgviewFactoryTab cgviewFactory = new CgviewFactoryTab();

          if (legendFontValue != null) {
            cgviewFactory.setLegendFontSize(legendFontValue.intValue());
          }
          if (labelFontValue != null) {
            cgviewFactory.setLabelFontSize(labelFontValue.intValue());
          }
          if (rulerFontValue != null) {
            cgviewFactory.setRulerFontSize(rulerFontValue.intValue());
          }

          if (heightValue != null) {
            cgviewFactory.setHeight(heightValue.intValue());
          }

          if (widthValue != null) {
            cgviewFactory.setWidth(widthValue.intValue());
          }

          if (tickDensityValue != null) {
            cgviewFactory.setTickDensity(tickDensityValue.doubleValue());
          }

          cgview = cgviewFactory.createCgviewFromFile(inputValue);
          cgview.setDesiredZoomCenter(centerBaseValue.intValue());
          cgview.setDesiredZoom(zoomValue.doubleValue());

          if (removeLabelsValue.booleanValue()) {
            cgview.setGlobalLabel(LABEL_NONE);
          }

          if (removeLegendsValue.booleanValue()) {
            cgview.setDrawLegends(false);
          }

          if (useInnerLabelsValue != null) {
            cgview.setUseInnerLabels(useInnerLabelsValue.intValue());
          }
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (Exception e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (inputFormat.equalsIgnoreCase("ptt")) {
        try {
          CgviewFactoryPtt cgviewFactory = new CgviewFactoryPtt();

          if (legendFontValue != null) {
            cgviewFactory.setLegendFontSize(legendFontValue.intValue());
          }
          if (labelFontValue != null) {
            cgviewFactory.setLabelFontSize(labelFontValue.intValue());
          }
          if (rulerFontValue != null) {
            cgviewFactory.setRulerFontSize(rulerFontValue.intValue());
          }

          if (heightValue != null) {
            cgviewFactory.setHeight(heightValue.intValue());
          }

          if (widthValue != null) {
            cgviewFactory.setWidth(widthValue.intValue());
          }

          if (tickDensityValue != null) {
            cgviewFactory.setTickDensity(tickDensityValue.doubleValue());
          }

          cgview = cgviewFactory.createCgviewFromFile(inputValue);
          cgview.setDesiredZoomCenter(centerBaseValue.intValue());
          cgview.setDesiredZoom(zoomValue.doubleValue());

          if (removeLabelsValue.booleanValue()) {
            cgview.setGlobalLabel(LABEL_NONE);
          }

          if (removeLegendsValue.booleanValue()) {
            cgview.setDrawLegends(false);
          }

          if (useInnerLabelsValue != null) {
            cgview.setUseInnerLabels(useInnerLabelsValue.intValue());
          }
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (Exception e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else {
        System.err.println("Input file extension was not recognized.");
        System.exit(1);
      }

      if (formatValue.equalsIgnoreCase("svg")) {
        try {
          if (embedFontsValue.booleanValue() == false) {
            writeToSVGFile(cgview, outputValue, false, false);
          } else {
            writeToSVGFile(cgview, outputValue, true, false);
          }
        } catch (SVGGraphics2DIOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (formatValue.equalsIgnoreCase("svgz")) {
        try {
          if (embedFontsValue.booleanValue() == false) {
            writeToSVGFile(cgview, outputValue, false, true);
          } else {
            writeToSVGFile(cgview, outputValue, true, true);
          }
        } catch (SVGGraphics2DIOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (formatValue.equalsIgnoreCase("png")) {
        try {
          writeToPNGFile(cgview, outputValue);
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (formatValue.equalsIgnoreCase("jpg")) {
        try {
          writeToJPGFile(cgview, outputValue);
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else {
        System.err.println("The output format was not recognized.");
        System.exit(1);
      }

      if (htmlValue != null) {
        // write HTML file
        try {
          if (pathValue == null) {
            writeHTMLFile(cgview, outputValue, formatValue, htmlValue);
          } else {
            writeHTMLFile(cgview, pathValue, formatValue, htmlValue);
          }
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      }
    }
    // seriesValue is defined.
    else {
      useExternalStylesheetValue = new Boolean(true);

      File seriesDirectory = new File(seriesValue);
      if (!(seriesDirectory.isDirectory())) {
        if (!(seriesDirectory.mkdirs())) {
          System.err.println(
            "The directory " + seriesValue + " could not be created."
          );
          System.exit(1);
        }
      }

      File includesDirectory = new File(
        seriesValue + File.separator + INCLUDES_OUT_PATH
      );
      if (!(includesDirectory.isDirectory())) {
        if (!(includesDirectory.mkdirs())) {
          System.err.println(
            "The directory " +
            seriesValue +
            File.separator +
            INCLUDES_OUT_PATH +
            " could not be created."
          );
          System.exit(1);
        }
      }

      File pngDirectory = new File(seriesValue + File.separator + PNG_OUT_PATH);
      if (!(pngDirectory.isDirectory())) {
        if (!(pngDirectory.mkdirs())) {
          System.err.println(
            "The directory " +
            seriesValue +
            File.separator +
            PNG_OUT_PATH +
            " could not be created."
          );
          System.exit(1);
        }
      }

      File svgDirectory = null;
      if (!excludeSVGValue.booleanValue()) {
        svgDirectory = new File(seriesValue + File.separator + SVG_OUT_PATH);
        if (!(svgDirectory.isDirectory())) {
          if (!(svgDirectory.mkdirs())) {
            System.err.println(
              "The directory " +
              seriesValue +
              File.separator +
              SVG_OUT_PATH +
              " could not be created."
            );
            System.exit(1);
          }
        }
      }

      outputDirectory = seriesValue;

      // now create a cgview
      Cgview cgview = new Cgview(1);
      if (inputFormat.equalsIgnoreCase("xml")) {
        try {
          CgviewFactory cgviewFactory = new CgviewFactory();

          if (legendFontValue != null) {
            cgviewFactory.setLegendFontSize(legendFontValue.intValue());
          }
          if (labelFontValue != null) {
            cgviewFactory.setLabelFontSize(labelFontValue.intValue());
          }
          if (rulerFontValue != null) {
            cgviewFactory.setRulerFontSize(rulerFontValue.intValue());
          }

          cgview = cgviewFactory.createCgviewFromFile(inputValue);

          if (heightValue != null) {
            cgview.setHeight(heightValue.intValue());
          }

          if (widthValue != null) {
            cgview.setWidth(widthValue.intValue());
          }

          if (removeLabelsValue.booleanValue()) {
            cgview.setGlobalLabel(LABEL_NONE);
          }

          if (removeLegendsValue.booleanValue()) {
            cgview.setDrawLegends(false);
          }

          if (useInnerLabelsValue != null) {
            cgview.setUseInnerLabels(useInnerLabelsValue.intValue());
          }

          if (tickDensityValue != null) {
            cgview.setTickDensity(tickDensityValue.doubleValue());
          }
          // cgview.setDesiredZoomCenter(centerBaseValue.intValue());
          // cgview.setDesiredZoom(zoomValue.doubleValue());
        } catch (SAXException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (inputFormat.equalsIgnoreCase("tab")) {
        try {
          CgviewFactoryTab cgviewFactory = new CgviewFactoryTab();

          if (legendFontValue != null) {
            cgviewFactory.setLegendFontSize(legendFontValue.intValue());
          }
          if (labelFontValue != null) {
            cgviewFactory.setLabelFontSize(labelFontValue.intValue());
          }
          if (rulerFontValue != null) {
            cgviewFactory.setRulerFontSize(rulerFontValue.intValue());
          }

          if (heightValue != null) {
            cgviewFactory.setHeight(heightValue.intValue());
          }

          if (widthValue != null) {
            cgviewFactory.setWidth(widthValue.intValue());
          }
          if (tickDensityValue != null) {
            cgviewFactory.setTickDensity(tickDensityValue.doubleValue());
          }

          cgview = cgviewFactory.createCgviewFromFile(inputValue);
          // cgview.setDesiredZoomCenter(centerBaseValue.intValue());
          // cgview.setDesiredZoom(zoomValue.doubleValue());

          if (removeLabelsValue.booleanValue()) {
            cgview.setGlobalLabel(LABEL_NONE);
          }

          if (removeLegendsValue.booleanValue()) {
            cgview.setDrawLegends(false);
          }

          if (useInnerLabelsValue != null) {
            cgview.setUseInnerLabels(useInnerLabelsValue.intValue());
          }
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (Exception e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else if (inputFormat.equalsIgnoreCase("ptt")) {
        try {
          CgviewFactoryPtt cgviewFactory = new CgviewFactoryPtt();

          if (legendFontValue != null) {
            cgviewFactory.setLegendFontSize(legendFontValue.intValue());
          }
          if (labelFontValue != null) {
            cgviewFactory.setLabelFontSize(labelFontValue.intValue());
          }
          if (rulerFontValue != null) {
            cgviewFactory.setRulerFontSize(rulerFontValue.intValue());
          }

          if (heightValue != null) {
            cgviewFactory.setHeight(heightValue.intValue());
          }

          if (widthValue != null) {
            cgviewFactory.setWidth(widthValue.intValue());
          }

          if (tickDensityValue != null) {
            cgviewFactory.setTickDensity(tickDensityValue.doubleValue());
          }

          cgview = cgviewFactory.createCgviewFromFile(inputValue);
          // cgview.setDesiredZoomCenter(centerBaseValue.intValue());
          // cgview.setDesiredZoom(zoomValue.doubleValue());

          if (removeLabelsValue.booleanValue()) {
            cgview.setGlobalLabel(LABEL_NONE);
          }

          if (removeLegendsValue.booleanValue()) {
            cgview.setDrawLegends(false);
          }

          if (useInnerLabelsValue != null) {
            cgview.setUseInnerLabels(useInnerLabelsValue.intValue());
          }
        } catch (IOException e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        } catch (Exception e) {
          e.printStackTrace(System.err);
          System.err.println(PROBLEM_MESSAGE + e.toString());
          System.exit(1);
        }
      } else {
        System.err.println("Input file extension was not recognized.");
        System.exit(1);
      }

      // now try to copy button images to the output directory
      FileMover fileMover = new FileMover();
      if (excludeSVGValue.booleanValue()) {
        if (
          !(
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_IN_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_OUT_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_FORWARD_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_BACK_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              INDEX_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_IN_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_OUT_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_FORWARD_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_BACK_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              INDEX_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              HELP_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              HELP_FILE_PNG,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              STYLE_FILE,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              OVERLIB_JAVASCRIPT,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            )
          )
        ) {
          System.err.println(
            "Include files could not be copied to the " +
            seriesValue +
            File.separator +
            INCLUDES_OUT_PATH +
            " directory"
          );
          System.exit(1);
        }
      } else {
        if (
          !(
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_IN_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_OUT_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_FORWARD_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_BACK_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              INDEX_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_IN_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              ZOOM_OUT_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_FORWARD_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              MOVE_BACK_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              INDEX_BUTTON_OFF,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              TO_SVG_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              TO_PNG_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              HELP_BUTTON,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              HELP_FILE,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              STYLE_FILE,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              SVG_JAVASCRIPT,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            ) &&
            fileMover.moveFile(
              INCLUDES_PATH,
              OVERLIB_JAVASCRIPT,
              seriesValue + File.separator + INCLUDES_OUT_PATH
            )
          )
        ) {
          System.err.println(
            "Include files could not be copied to the " +
            seriesValue +
            File.separator +
            INCLUDES_OUT_PATH +
            " directory"
          );
          System.exit(1);
        }
      }

      // use a default zoom scheme
      int[] zoomValues = { 1, 6, 36 };
      // zoom values must start with 1, and must be in ascending order.
      // there must be no spaces between numbers.
      if (seriesNumbersValue != null) {
        try {
          String values[] = seriesNumbersValue.split(",");
          zoomValues = new int[values.length];
          int previousValue = 0;
          for (int k = 0; k < values.length; k++) {
            zoomValues[k] = Integer.parseInt(values[k]);
            if (k == 0) {
              if (zoomValues[k] != 1) {
                System.err.println(
                  "There is a problem with the supplied zoom values: " +
                  seriesNumbersValue
                );
                System.err.println(
                  "The first value in the zoom values must be 1."
                );
                System.exit(1);
              }
            } else {
              if (zoomValues[k] <= previousValue) {
                System.err.println(
                  "There is a problem with the supplied zoom values: " +
                  seriesNumbersValue
                );
                System.err.println(
                  "The zoom values must be given in ascending order."
                );
                System.exit(1);
              }
            }
            previousValue = zoomValues[k];
          }
        } catch (Exception e) {
          System.err.println(
            "There is a problem with the supplied zoom values: " +
            seriesNumbersValue
          );
          System.err.println(
            "Please enter comma separated values, for example: 1, 6, 36"
          );
        }
      }

      ArrayList toDrawPreviousZoom = new ArrayList();
      ArrayList toDrawCurrentZoom = new ArrayList();
      ArrayList toDrawNextZoom = new ArrayList();

      ArrayList labelBounds;
      Iterator i;
      Iterator j;
      Iterator m;

      NumberFormat nf = NumberFormat.getInstance();

      for (int k = 0; k < zoomValues.length; k++) {
        // add first SeriesImage if k is 0
        if (k == 0) {
          toDrawCurrentZoom.add(new SeriesImage(1, 1));
        }
        // need to sort toDrawCurrentZoom by zoomCenter
        Comparator comparator = new SortSeriesImageByZoomCenter();
        Collections.sort(toDrawCurrentZoom, comparator);

        m = toDrawCurrentZoom.iterator();

        while (m.hasNext()) {
          SeriesImage imageToDraw = (SeriesImage) m.next();
          cgview.setDesiredZoomCenter(imageToDraw.getZoomCenter());
          cgview.setDesiredZoom(imageToDraw.getZoomValue());

          // draw to file. The zoomValues value is needed for svg and svgz output, to modify the
          // BOUNDS_RULER type labelBounds.
          if (k < zoomValues.length - 1) {
            // png
            writeImageToFile(
              cgview,
              seriesValue +
              File.separator +
              PNG_OUT_PATH +
              File.separator +
              Integer.toString(imageToDraw.getZoomValue()) +
              "_" +
              Integer.toString(imageToDraw.getZoomCenter()) +
              "." +
              "png",
              "png",
              embedFontsValue.booleanValue(),
              zoomValues[k + 1]
            );
            // svgz
            if (!excludeSVGValue.booleanValue()) {
              writeImageToFile(
                cgview,
                seriesValue +
                File.separator +
                SVG_OUT_PATH +
                File.separator +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "svgz",
                "svgz",
                embedFontsValue.booleanValue(),
                zoomValues[k + 1],
                true
              );
            }
          } else {
            // png
            writeImageToFile(
              cgview,
              seriesValue +
              File.separator +
              PNG_OUT_PATH +
              File.separator +
              Integer.toString(imageToDraw.getZoomValue()) +
              "_" +
              Integer.toString(imageToDraw.getZoomCenter()) +
              "." +
              "png",
              "png",
              embedFontsValue.booleanValue(),
              0
            );
            // svgz
            if (!excludeSVGValue.booleanValue()) {
              writeImageToFile(
                cgview,
                seriesValue +
                File.separator +
                SVG_OUT_PATH +
                File.separator +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "svgz",
                "svgz",
                embedFontsValue.booleanValue(),
                0,
                true
              );
            }
          }

          labelBounds = cgview.getLabelBounds();

          // now modify the BOUNDS_RULER type labelBounds for png output.
          if (k < zoomValues.length - 1) {
            i = labelBounds.iterator();
            while (i.hasNext()) {
              LabelBounds currentLabelBounds = (LabelBounds) i.next();
              if (currentLabelBounds.getType() == BOUNDS_RULER) {
                currentLabelBounds.setHyperlink(
                  Integer.toString(zoomValues[k + 1]) +
                  "_" +
                  Integer.toString(currentLabelBounds.getBase()) +
                  ".html"
                );
                currentLabelBounds.setMouseover(
                  "expand " +
                  nf.format((long) currentLabelBounds.getBase()) +
                  " bp region"
                );
              }
            }
          }

          // now examine the BOUNDS_RULER type labelBounds and add seriesImage objects to the
          // toDrawNextZoom arrayList

          i = labelBounds.iterator();
          while (i.hasNext()) {
            LabelBounds currentLabelBounds = (LabelBounds) i.next();
            if (currentLabelBounds.getType() == BOUNDS_RULER) {
              if (k < zoomValues.length - 1) {
                // if this particular labelBounds is new, add it to the stack
                SeriesImage newSeriesImage = new SeriesImage(
                  zoomValues[k + 1],
                  currentLabelBounds.getBase()
                );
                boolean isNew = true;
                j = toDrawNextZoom.iterator();
                while (j.hasNext()) {
                  SeriesImage existingSeriesImage = (SeriesImage) j.next();
                  if (existingSeriesImage.isEqual(newSeriesImage)) {
                    isNew = false;
                    break;
                  }
                }
                if (isNew) {
                  toDrawNextZoom.add(newSeriesImage);
                }
              }
            }
          }

          // now create html file for the image file that was written
          if (k == 0) {
            // there are more than one items in the list of zoom values
            if (k < zoomValues.length - 1) {
              // png
              writeHTMLToFile(
                cgview,
                PNG_OUT_PATH +
                "/" +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "png",
                "png",
                seriesValue + File.separator + "index.html",
                null,
                imageToDraw.getZoomInFilePrefix(
                  labelBounds,
                  zoomValues[k + 1]
                ) +
                "." +
                "html",
                null,
                null
              );
              writeHTMLToFile(
                cgview,
                PNG_OUT_PATH +
                "/" +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "png",
                "png",
                seriesValue +
                File.separator +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "html",
                null,
                imageToDraw.getZoomInFilePrefix(
                  labelBounds,
                  zoomValues[k + 1]
                ) +
                "." +
                "html",
                null,
                null
              );

              // svgz
              if (!excludeSVGValue.booleanValue()) {
                writeHTMLToFile(
                  cgview,
                  SVG_OUT_PATH +
                  "/" +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "." +
                  "svgz",
                  "svgz",
                  seriesValue + File.separator + "index_svg.html",
                  null,
                  imageToDraw.getZoomInFilePrefix(
                    labelBounds,
                    zoomValues[k + 1]
                  ) +
                  "." +
                  "html",
                  null,
                  null
                );
                writeHTMLToFile(
                  cgview,
                  SVG_OUT_PATH +
                  "/" +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "." +
                  "svgz",
                  "svgz",
                  seriesValue +
                  File.separator +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "_svg." +
                  "html",
                  null,
                  imageToDraw.getZoomInFilePrefix(
                    labelBounds,
                    zoomValues[k + 1]
                  ) +
                  "." +
                  "html",
                  null,
                  null
                );
              }
            }
            // there is only one item in the list of zoom values
            else {
              // png
              writeHTMLToFile(
                cgview,
                PNG_OUT_PATH +
                "/" +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "png",
                "png",
                seriesValue + File.separator + "index.html",
                null,
                null,
                null,
                null
              );

              // svgz
              if (!excludeSVGValue.booleanValue()) {
                writeHTMLToFile(
                  cgview,
                  SVG_OUT_PATH +
                  "/" +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "." +
                  "svgz",
                  "svgz",
                  seriesValue + File.separator + "index_svg.html",
                  null,
                  null,
                  null,
                  null
                );
              }
            }
          } else {
            // this is not the most zoomed in level
            if (k < zoomValues.length - 1) {
              // png
              writeHTMLToFile(
                cgview,
                PNG_OUT_PATH +
                "/" +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "png",
                "png",
                seriesValue +
                File.separator +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "html",
                imageToDraw.getZoomOutFilePrefix(
                  toDrawPreviousZoom,
                  zoomValues[k - 1]
                ) +
                "." +
                "html",
                imageToDraw.getZoomInFilePrefix(
                  labelBounds,
                  zoomValues[k + 1]
                ) +
                "." +
                "html",
                imageToDraw.getClockwiseFilePrefix(toDrawCurrentZoom) +
                "." +
                "html",
                imageToDraw.getCounterclockwiseFilePrefix(toDrawCurrentZoom) +
                "." +
                "html"
              );

              // svgz
              if (!excludeSVGValue.booleanValue()) {
                writeHTMLToFile(
                  cgview,
                  SVG_OUT_PATH +
                  "/" +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "." +
                  "svgz",
                  "svgz",
                  seriesValue +
                  File.separator +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "_svg." +
                  "html",
                  imageToDraw.getZoomOutFilePrefix(
                    toDrawPreviousZoom,
                    zoomValues[k - 1]
                  ) +
                  "." +
                  "html",
                  imageToDraw.getZoomInFilePrefix(
                    labelBounds,
                    zoomValues[k + 1]
                  ) +
                  "." +
                  "html",
                  imageToDraw.getClockwiseFilePrefix(toDrawCurrentZoom) +
                  "." +
                  "html",
                  imageToDraw.getCounterclockwiseFilePrefix(toDrawCurrentZoom) +
                  "." +
                  "html"
                );
              }
            }
            // this is the most zoomed in level
            else {
              // png
              writeHTMLToFile(
                cgview,
                PNG_OUT_PATH +
                "/" +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "png",
                "png",
                seriesValue +
                File.separator +
                Integer.toString(imageToDraw.getZoomValue()) +
                "_" +
                Integer.toString(imageToDraw.getZoomCenter()) +
                "." +
                "html",
                imageToDraw.getZoomOutFilePrefix(
                  toDrawPreviousZoom,
                  zoomValues[k - 1]
                ) +
                "." +
                "html",
                null,
                imageToDraw.getClockwiseFilePrefix(toDrawCurrentZoom) +
                "." +
                "html",
                imageToDraw.getCounterclockwiseFilePrefix(toDrawCurrentZoom) +
                "." +
                "html"
              );

              // svgz
              if (!excludeSVGValue.booleanValue()) {
                writeHTMLToFile(
                  cgview,
                  SVG_OUT_PATH +
                  "/" +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "." +
                  "svgz",
                  "svgz",
                  seriesValue +
                  File.separator +
                  Integer.toString(imageToDraw.getZoomValue()) +
                  "_" +
                  Integer.toString(imageToDraw.getZoomCenter()) +
                  "_svg." +
                  "html",
                  imageToDraw.getZoomOutFilePrefix(
                    toDrawPreviousZoom,
                    zoomValues[k - 1]
                  ) +
                  "." +
                  "html",
                  null,
                  imageToDraw.getClockwiseFilePrefix(toDrawCurrentZoom) +
                  "." +
                  "html",
                  imageToDraw.getCounterclockwiseFilePrefix(toDrawCurrentZoom) +
                  "." +
                  "html"
                );
              }
            }
          }
        }
        // now need to move toDrawNextZoom items into toDrawCurrentZoom
        toDrawPreviousZoom.clear();
        toDrawPreviousZoom = (ArrayList) toDrawCurrentZoom.clone();
        toDrawCurrentZoom.clear();
        toDrawCurrentZoom = (ArrayList) toDrawNextZoom.clone();
        toDrawNextZoom.clear();
      }
    }
    System.exit(0);
  }
}
