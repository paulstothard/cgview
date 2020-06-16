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

import java.awt.geom.*;
import java.util.*;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * This class contains methods for constructing HTML pages that contain Cgview maps. Tags are added
 * to the document in the order that the various tag adding methods are called. The methods in this
 * class escape all supplied text to make it suitable for inclusion in an HTML document.
 *
 * @author Paul Stothard
 */
public class CgviewHTMLDocument implements CgviewConstants {
  private ArrayList contents;
  private String newline;

  /** Constructs a new CgviewHTMLDocument object. */
  public CgviewHTMLDocument() {
    contents = new ArrayList();
    newline = System.getProperty("line.separator");
  }

  /**
   * Adds header information to this CgviewHTMLDocument, including an html opening tag. This method
   * should be the first method called after a CgviewHTMLDocument is created.
   *
   * @param title the title in the header.
   * @param zoom the value to appear in the 'zoom' meta tag.
   * @param zoomCenter the value to appear in the 'zoomCenter' meta tag.
   * @param imageFormat the format of the image to which this CgviewHTMLDocument refers.
   * @param useOverlib whether or not to use the overlib.js JavaScript library for PNG and JPG image
   *     maps.
   * @param useExternalStyleSheet whether or not to use an external stylesheet.
   */
  public void addHeader(
    String title,
    String zoom,
    String zoomCenter,
    String imageFormat,
    Boolean useOverlib,
    Boolean useExternalStyleSheet
  ) {
    contents.add(
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">" +
      newline
    );
    contents.add("<html lang=\"en\">" + newline);
    contents.add("<head>" + newline);
    contents.add(
      "<title>" + StringEscapeUtils.escapeHtml(title) + "</title>" + newline
    );
    // contents.add("<meta name=\"mapInfo\" zoom=\"" + StringEscapeUtils.escapeHtml(zoom) + "\" />"
    // + newline);
    // contents.add("<meta name=\"mapInfo\" zoomCenter=\"" +
    // StringEscapeUtils.escapeHtml(zoomCenter) + "\" />" + newline);
    // contents.add("<meta name=\"mapInfo\" format=\"" + StringEscapeUtils.escapeHtml(imageFormat) +
    // "\" />" + newline);
    contents.add(
      "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\" />" +
      newline
    );
    if (
      (useExternalStyleSheet != null) && (useExternalStyleSheet.booleanValue())
    ) {
      contents.add(
        "<link rel=\"stylesheet\" href=\"includes/stylesheet.css\" type=\"text/css\" />" +
        newline
      );
    } else {
      contents.add("<style type=\"text/css\">" + newline);
      contents.add(
        "body {background-color: #FFFFFF; font-family: arial, sans-serif; color: #000000}" +
        newline
      );
      contents.add(
        "span.note {font-size: small; font-style: italic}" + newline
      );
      contents.add("span.validInfo {font-size: x-small}" + newline);
      contents.add("span.fileSize {font-size: x-small}" + newline);
      contents.add("span.warning {font-size: x-small; color: red}" + newline);
      contents.add(
        "div.heading {color: #FFFFFF; background-color: #6666FF; font-size: large; padding: 0.1cm}" +
        newline
      );
      contents.add(
        "div.title {font-size: x-large; text-align: center}" + newline
      );
      contents.add("table.noBorder {border-style: none}" + newline);
      contents.add("table.border {border-style: solid}" + newline);
      contents.add(
        "td.center {text-align: center; font-size: small}" + newline
      );
      contents.add("td.left {text-align: left; font-size: small}" + newline);
      contents.add("td.right {text-align: right; font-size: small}" + newline);
      contents.add("a:link {color: #000099; text-decoration: none}" + newline);
      contents.add(
        "a:visited {color: #000099; text-decoration: none}" + newline
      );
      contents.add(
        "a:active {color: #FF0000; text-decoration: underline}" + newline
      );
      contents.add(
        "a:hover {color: #FF0000; text-decoration: underline}" + newline
      );
      contents.add("</style>" + newline);
    }

    if (
      (useOverlib != null) &&
      (
        (useOverlib.booleanValue()) &&
        (
          (imageFormat.equalsIgnoreCase("png")) ||
          (imageFormat.equalsIgnoreCase("jpg"))
        )
      )
    ) {
      contents.add(
        "<script type=\"text/javascript\" src=\"includes/overlib.js\"><!-- overLIB (c) Erik Bosrup --></script>" +
        newline
      );
    }
    contents.add("</head>" + newline);
    contents.add("<body>" + newline);
    if (
      (useOverlib != null) &&
      (
        (useOverlib.booleanValue()) &&
        (
          (imageFormat.equalsIgnoreCase("png")) ||
          (imageFormat.equalsIgnoreCase("jpg"))
        )
      )
    ) {
      contents.add(
        "<div id=\"overDiv\" style=\"position:absolute; visibility:hidden; z-index:1000;\"></div>" +
        newline
      );
    }
  }

  /**
   * Adds table and tbody opening tags to this CgviewHTMLDocument.
   *
   * @param width the width of the table in pixels.
   */
  public void addTableStart(int width) {
    addTableStart(width, 0);
  }

  /**
   * Adds table and tbody opening tags to this CgviewHTMLDocument.
   *
   * @param width the width of the table in pixels.
   * @param border the width of the border in pixels.
   */
  public void addTableStart(int width, int border) {
    if (border == 0) {
      contents.add(
        "<table class=\"noBorder\" width=\"" +
        Integer.toString(width) +
        "\" cellspacing=\"0\" cellpadding=\"1\" align=\"left\">" +
        newline
      );
    } else {
      contents.add(
        "<table class=\"border\" width=\"" +
        Integer.toString(width) +
        "\" border=\"" +
        border +
        "\" cellspacing=\"0\" cellpadding=\"1\" align=\"left\">" +
        newline
      );
    }
    contents.add("<tbody>" + newline);
  }

  /** Adds table and tbody closing tags to this CgviewHTMLDocument. */
  public void addTableEnd() {
    contents.add("</tbody>" + newline);
    contents.add("</table>" + newline);
  }

  /** Adds tr opening tag to this CgviewHTMLDocument. */
  public void addTableRowStart() {
    contents.add("<tr>" + newline);
  }

  /** Adds tr closing tag to this CgviewHTMLDocument. */
  public void addTableRowEnd() {
    contents.add("</tr>" + newline);
  }

  /** Adds td opening tag to this CgviewHTMLDocument. */
  public void addTableColumnStart() {
    contents.add("<td>" + newline);
  }

  /** Adds td opening tag with center alignment to this CgviewHTMLDocument. */
  public void addTableColumnCenterStart() {
    contents.add("<td class=\"center\">" + newline);
  }

  /** Adds td opening tag with left alignment to this CgviewHTMLDocument. */
  public void addTableColumnLeftStart() {
    contents.add("<td class=\"left\">" + newline);
  }

  /** Adds td opening tag with right alignment to this CgviewHTMLDocument. */
  public void addTableColumnRightStart() {
    contents.add("<td class=\"right\">" + newline);
  }

  /** Adds td closing tag to this CgviewHTMLDocument. */
  public void addTableColumnEnd() {
    contents.add("</td>" + newline);
  }

  /**
   * Adds text to this CgviewHTMLDocument stating that the document is valid XHTML 1.0 and valid
   * CSS.
   */
  public void addValidationInfo() {
    contents.add("Valid XHTML 1.0;");
    contents.add("&nbsp;");
    contents.add("Valid CSS.");
  }

  /**
   * Adds body and html closing tags to this CgviewHTMLDocument. This method should be the last
   * method called before the contents of this document are retrieved using {@link #getContents()}.
   */
  public void addFooter() {
    contents.add("</body>" + newline);
    contents.add("</html>" + newline);
  }

  /** Adds a span opening tag with the 'note' style to this CgviewHTMLDocument. */
  public void addSpanNoteStart() {
    contents.add("<span class=\"note\">" + newline);
  }

  /** Adds a span opening tag with the 'fileSize' style to this CgviewHTMLDocument. */
  public void addSpanFileSizeStart() {
    contents.add("<span class=\"fileSize\">" + newline);
  }

  /** Adds a span opening tag with the 'validInfo' style to this CgviewHTMLDocument. */
  public void addSpanValidStart() {
    contents.add("<span class=\"validInfo\">" + newline);
  }

  /** Adds a span opening tag with the 'warning' style to this CgviewHTMLDocument. */
  public void addSpanWarningStart() {
    contents.add("<span class=\"warning\">" + newline);
  }

  /** Adds a span closing tag to this CgviewHTMLDocument. */
  public void addSpanEnd() {
    contents.add("</span>" + newline);
  }

  /**
   * Adds an image with an image map to this CgviewHTMLDocument, to implement mouseovers and
   * hyperlinks associated with Cgview Feature objects and FeatureRange objects. Image maps are used
   * for PNG and JPG maps. SVG maps contain the mouseover and hyperlink information internally.
   *
   * @param imageFile the image URL that the image map refers to.
   * @param width the width of the image.
   * @param height the height of the image.
   * @param labelBounds an ArrayList of LabelBounds objects, obtained from a previously drawn Cgview
   *     object using the {@link Cgview#getLabelBounds()} method.
   * @param useOverlib whether or not to use the overlib.js JavaScript library for PNG and JPG image
   *     maps.
   */
  public void addImageMap(
    String imageFile,
    int width,
    int height,
    ArrayList labelBounds,
    Boolean useOverlib
  ) {
    contents.add(
      "<img style=\"border:0\" src=\"" +
      StringEscapeUtils.escapeHtml(imageFile) +
      "\" width=\"" +
      Integer.toString(width) +
      "\" height=\"" +
      Integer.toString(height) +
      "\" usemap=\"#cgviewmap\" />" +
      newline
    );
    contents.add("<map id=\"cgviewmap\" name=\"cgviewmap\">" + newline);

    // add areas
    Iterator i;
    i = labelBounds.iterator();
    while (i.hasNext()) {
      LabelBounds currentLabelBounds = (LabelBounds) i.next();
      Rectangle2D bounds = currentLabelBounds.getBounds();
      if (
        (currentLabelBounds.getUse() == true) &&
        (
          (currentLabelBounds.getMouseover() != null) ||
          (currentLabelBounds.getHyperlink() != null)
        )
      ) {
        contents.add(
          "<area shape=\"rect\" coords=\"" +
          Integer.toString((int) Math.floor(bounds.getX() + 0.5d)) +
          "," +
          Integer.toString((int) Math.floor(bounds.getY() + 0.5d)) +
          "," +
          Integer.toString(
            (int) Math.floor(bounds.getX() + 0.5d) +
            (int) Math.floor(bounds.getWidth() + 0.5d)
          ) +
          "," +
          Integer.toString(
            (int) Math.floor(bounds.getY() + 0.5d) +
            (int) Math.floor(bounds.getHeight() + 0.5d)
          ) +
          "\" "
        );

        if (currentLabelBounds.getHyperlink() != null) {
          contents.add("href=\"" + currentLabelBounds.getHyperlink() + "\" ");
        }

        if (
          (currentLabelBounds.getMouseover() != null) &&
          (!(currentLabelBounds.getMouseover().matches("\\S*")))
        ) {
          if ((useOverlib != null) && (useOverlib.booleanValue())) {
            contents.add(
              "onmouseover=\"return overlib('" +
              StringEscapeUtils.escapeJavaScript(
                currentLabelBounds.getMouseover()
              ) +
              "');\" "
            );
            contents.add("onmouseout=\"return nd();\" ");
          } else {
            contents.add(
              "onmouseover=\"self.status='" +
              StringEscapeUtils.escapeJavaScript(
                currentLabelBounds.getMouseover()
              ) +
              "'; return true;\" "
            );
            contents.add("onmouseout=\"self.status=' '; return true;\" ");
          }
        }
        contents.add("/>" + newline);
      }
    }
    contents.add("</map>" + newline);
  }

  /**
   * Adds an image to this CgviewHTMLDocument. Use this method for adding a JPG or PNG image to a
   * CgviewHTMLDocument. To add an SVG image use {@link #addSVG}.
   *
   * @param imageFile the image URL.
   * @param width the width of the image.
   * @param height the height of the image.
   */
  public void addImage(String imageFile, int width, int height) {
    addImage(imageFile, width, height, "map");
  }

  /**
   * Adds an image to this CgviewHTMLDocument. Use this method for adding a JPG or PNG image to a
   * CgviewHTMLDocument. To add an SVG image use {@link #addSVG}.
   *
   * @param imageFile the image URL.
   * @param width the width of the image.
   * @param height the height of the image.
   * @param alt alternate text for the image
   */
  public void addImage(String imageFile, int width, int height, String alt) {
    contents.add(
      "<img style=\"border:0\" src=\"" +
      StringEscapeUtils.escapeHtml(imageFile) +
      "\" width=\"" +
      Integer.toString(width) +
      "\" height=\"" +
      Integer.toString(height) +
      "\" alt=\"" +
      StringEscapeUtils.escapeHtml(alt) +
      "\" />" +
      newline
    );
  }

  /**
   * Adds an image of a button to this CgviewHTMLDocument and links the button to another file.
   *
   * @param imageFile the button image URL.
   * @param linkFile the URL to be linked to the button.
   * @param altText a short description of the image.
   */
  public void addButton(String imageFile, String linkFile, String altText) {
    contents.add("<a href=\"" + StringEscapeUtils.escapeHtml(linkFile) + "\">");
    contents.add(
      "<img style=\"border:0\" src=\"" +
      StringEscapeUtils.escapeHtml(imageFile) +
      "\" alt=\"" +
      StringEscapeUtils.escapeHtml(altText) +
      "\" />"
    );
    contents.add("</a>" + newline);
  }

  /**
   * Adds an image of a button to this CgviewHTMLDocument.
   *
   * @param imageFile the button image URL.
   * @param altText a short description of the image.
   */
  public void addButtonNoLink(String imageFile, String altText) {
    contents.add(
      "<img style=\"border:0\" src=\"" +
      StringEscapeUtils.escapeHtml(imageFile) +
      "\" alt=\"" +
      StringEscapeUtils.escapeHtml(altText) +
      "\" />" +
      newline
    );
  }

  /**
   * Adds an SVG image to this CgviewHTMLDocument.
   *
   * @param imageFile the SVG image URL.
   * @param width the width of the SVG image.
   * @param height the height of the SVG image.
   */
  public void addSVG(String imageFile, int width, int height) {
    // contents.add("<embed src=\"" + StringEscapeUtils.escapeHtml(imageFile) + "\"
    // type=\"image/svg+xml\" pluginspage=\"http://www.adobe.com/svg/viewer/install/\" width=\"" +
    // Integer.toString(width) + "\" height=\"" + Integer.toString(height) + "\" />" + newline);

    contents.add(
      "<object data=\"" +
      StringEscapeUtils.escapeHtml(imageFile) +
      "\" type=\"image/svg+xml\" width=\"" +
      Integer.toString(width) +
      "\" height=\"" +
      Integer.toString(height) +
      "\" />" +
      newline
    );
  }

  /** Adds a br tag to this CgviewHTMLDocument. */
  public void addBreak() {
    contents.add("<br />" + newline);
  }

  /** Adds a non-breaking space to this CgviewHTMLDocument. */
  public void addSpace() {
    contents.add("&nbsp;");
  }

  /**
   * Adds a hyperlink to this CgviewHTMLDocument.
   *
   * @param name the text to appear in the link.
   * @param link the link URL.
   */
  public void addLink(String name, String link) {
    contents.add(
      "<a href=\"" +
      StringEscapeUtils.escapeHtml(link) +
      "\">" +
      StringEscapeUtils.escapeHtml(name) +
      "</a>" +
      newline
    );
  }

  /**
   * Adds text to this CgviewHTMLDocument.
   *
   * @param text
   */
  public void addText(String text) {
    contents.add(StringEscapeUtils.escapeHtml(text) + "" + newline);
  }

  /**
   * Returns the contents of this CgviewHTMLDocument.
   *
   * @return an ArrayList of the entries in this CgviewHTMLDocument.
   */
  public ArrayList getContents() {
    return contents;
  }
}
