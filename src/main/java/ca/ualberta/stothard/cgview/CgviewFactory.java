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

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Stack;
import java.util.regex.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

/**
 * This class reads an XML document and creates a Cgview object. The various elements and attributes
 * in the file are used to describe sequence features (position, type, name, color, label font, and
 * opacity). Optional XML attributes can be included, to control global map characteristics, and to
 * add legends, a title, and footnotes.
 *
 * @author Paul Stothard
 */
public class CgviewFactory extends DefaultHandler implements CgviewConstants {
  private static final Hashtable COLORS = new Hashtable();
  private static final Hashtable LABEL_TYPES = new Hashtable();
  private static final Hashtable GLOBAL_LABEL_TYPES = new Hashtable();
  private static final Hashtable DECORATIONS = new Hashtable();
  private static final Hashtable RULER_UNITS = new Hashtable();
  private static final Hashtable USE_INNER_LABELS = new Hashtable();
  private static final Hashtable GIVE_FEATURE_POSITIONS = new Hashtable();
  private static final Hashtable FEATURE_THICKNESSES = new Hashtable();
  private static final Hashtable FEATURESLOT_SPACINGS = new Hashtable();
  private static final Hashtable BACKBONE_THICKNESSES = new Hashtable();
  private static final Hashtable ARROWHEAD_LENGTHS = new Hashtable();
  private static final Hashtable MINIMUM_FEATURE_LENGTHS = new Hashtable();
  private static final Hashtable ORIGINS = new Hashtable();
  private static final Hashtable TICK_THICKNESSES = new Hashtable();
  private static final Hashtable TICK_LENGTHS = new Hashtable();
  private static final Hashtable LABEL_LINE_THICKNESSES = new Hashtable();
  private static final Hashtable LABEL_LINE_LENGTHS = new Hashtable();
  private static final Hashtable LABEL_PLACEMENT_QUALITIES = new Hashtable();
  private static final Hashtable BOOLEANS = new Hashtable();
  private static final Hashtable SWATCH_TYPES = new Hashtable();
  private static final Hashtable LEGEND_POSITIONS = new Hashtable();
  private static final Hashtable LEGEND_ALIGNMENTS = new Hashtable();
  private static final Hashtable LEGEND_SHOW_ZOOM = new Hashtable();

  private static final int MAX_BASES = 200000000;
  private static final int MIN_BASES = 10;
  private static final double MIN_BACKBONE_RADIUS = 10.0d;
  private static final double MAX_BACKBONE_RADIUS = 12000.0d; // default is 190.0d
  private static final int MAX_IMAGE_WIDTH = 30000; // default is 700.0d
  private static final int MIN_IMAGE_WIDTH = 100;

  private static final int MAX_IMAGE_HEIGHT = 30000; // default is 700.0d
  private static final int MIN_IMAGE_HEIGHT = 100;

  private Pattern fontDescriptionPattern = Pattern.compile(
    "\\s*(\\S+)\\s*,\\s*(\\S+)\\s*,\\s*(\\d+)\\s*,*\\s*"
  );
  private Pattern colorDescriptionPattern = Pattern.compile(
    "\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,*\\s*"
  );
  private Matcher m;

  private int cgviewLength;
  private int imageWidth;
  private int imageHeight;

  // set to true if combining data
  private boolean ignoreCgviewTag = false;
  private boolean ignoreLegendTag = false;
  private boolean ignoreLegendItemTag = false;

  private Cgview currentCgview;
  private FeatureSlot currentFeatureSlot;
  private Feature currentFeature;
  private FeatureRange currentFeatureRange;
  private Legend currentLegend;
  private LegendItem currentLegendItem;

  private int labelFontSize = -1;
  private int rulerFontSize = -1;
  private int legendFontSize = -1;

  private StringBuffer content = new StringBuffer();
  private Locator locator;
  private Stack context = new Stack();

  /** Constructs a new CgviewFactory object. */
  public CgviewFactory() {
    super();
    COLORS.put("black", new Color(0, 0, 0)); // longTickColor default //rulerFontColor default //titleFontColor default //
    // shortTickColor default //zeroTickColor default
    COLORS.put("silver", new Color(192, 192, 192));
    COLORS.put("gray", new Color(128, 128, 128)); // backbone default
    COLORS.put("grey", new Color(128, 128, 128));
    COLORS.put("white", new Color(255, 255, 255)); // background default
    COLORS.put("maroon", new Color(128, 0, 0));
    COLORS.put("red", new Color(255, 0, 0));
    COLORS.put("purple", new Color(128, 0, 128));
    COLORS.put("fuchsia", new Color(255, 0, 255));
    COLORS.put("green", new Color(0, 128, 0));
    COLORS.put("lime", new Color(0, 255, 0));
    COLORS.put("olive", new Color(128, 128, 0));
    COLORS.put("yellow", new Color(255, 255, 0));
    COLORS.put("orange", new Color(255, 153, 0));
    COLORS.put("navy", new Color(0, 0, 128));
    COLORS.put("blue", new Color(0, 0, 255)); // feature and featureRange default
    COLORS.put("teal", new Color(0, 128, 128));
    COLORS.put("aqua", new Color(0, 255, 255));

    LABEL_TYPES.put("true", new Integer(LABEL));
    LABEL_TYPES.put("false", new Integer(LABEL_NONE)); // default for feature and featureRange
    LABEL_TYPES.put("force", new Integer(LABEL_FORCE));

    GLOBAL_LABEL_TYPES.put("true", new Integer(LABEL)); // default for Cgview
    GLOBAL_LABEL_TYPES.put("false", new Integer(LABEL_NONE));
    GLOBAL_LABEL_TYPES.put("auto", new Integer(LABEL_ZOOMED));

    DECORATIONS.put("arc", new Integer(DECORATION_STANDARD)); // default for feature and featureRange
    DECORATIONS.put("hidden", new Integer(DECORATION_HIDDEN));
    DECORATIONS.put(
      "counterclockwise-arrow",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );
    DECORATIONS.put("clockwise-arrow", new Integer(DECORATION_CLOCKWISE_ARROW));

    RULER_UNITS.put("bases", new Integer(BASES)); // default for rulerUnits
    RULER_UNITS.put("centisomes", new Integer(CENTISOMES));

    USE_INNER_LABELS.put("true", new Integer(INNER_LABELS_SHOW)); // should have true, false, auto
    USE_INNER_LABELS.put("false", new Integer(INNER_LABELS_NO_SHOW));
    USE_INNER_LABELS.put("auto", new Integer(INNER_LABELS_AUTO));

    GIVE_FEATURE_POSITIONS.put("true", new Integer(POSITIONS_SHOW));
    GIVE_FEATURE_POSITIONS.put("false", new Integer(POSITIONS_NO_SHOW)); // default
    GIVE_FEATURE_POSITIONS.put("auto", new Integer(POSITIONS_AUTO));

    FEATURE_THICKNESSES.put("xxx-small", new Float(1.0f));
    FEATURE_THICKNESSES.put("xx-small", new Float(2.0f));
    FEATURE_THICKNESSES.put("x-small", new Float(4.0f));
    FEATURE_THICKNESSES.put("small", new Float(6.0f));
    FEATURE_THICKNESSES.put("medium", new Float(8.0f)); // default for featureThickness
    FEATURE_THICKNESSES.put("large", new Float(10.0f));
    FEATURE_THICKNESSES.put("x-large", new Float(12.0f));
    FEATURE_THICKNESSES.put("xx-large", new Float(14.0f));
    FEATURE_THICKNESSES.put("xxx-large", new Float(16.0f));

    FEATURESLOT_SPACINGS.put("xxx-small", new Float(0.0f));
    FEATURESLOT_SPACINGS.put("xx-small", new Float(1.0f));
    FEATURESLOT_SPACINGS.put("x-small", new Float(2.0f));
    FEATURESLOT_SPACINGS.put("small", new Float(3.0f));
    FEATURESLOT_SPACINGS.put("medium", new Float(4.0f)); // default for featureSlotSpacing
    FEATURESLOT_SPACINGS.put("large", new Float(5.0f));
    FEATURESLOT_SPACINGS.put("x-large", new Float(6.0f));
    FEATURESLOT_SPACINGS.put("xx-large", new Float(7.0f));
    FEATURESLOT_SPACINGS.put("xxx-large", new Float(8.0f));

    BACKBONE_THICKNESSES.put("xxx-small", new Float(1.0f));
    BACKBONE_THICKNESSES.put("xx-small", new Float(2.0f));
    BACKBONE_THICKNESSES.put("x-small", new Float(3.0f));
    BACKBONE_THICKNESSES.put("small", new Float(4.0f));
    BACKBONE_THICKNESSES.put("medium", new Float(5.0f)); // default for backboneThickness
    BACKBONE_THICKNESSES.put("large", new Float(6.0f));
    BACKBONE_THICKNESSES.put("x-large", new Float(7.0f));
    BACKBONE_THICKNESSES.put("xx-large", new Float(8.0f));
    BACKBONE_THICKNESSES.put("xxx-large", new Float(9.0f));

    ARROWHEAD_LENGTHS.put("xxx-small", new Double(1.0d));
    ARROWHEAD_LENGTHS.put("xx-small", new Double(2.0d));
    ARROWHEAD_LENGTHS.put("x-small", new Double(3.0d));
    ARROWHEAD_LENGTHS.put("small", new Double(4.0d));
    ARROWHEAD_LENGTHS.put("medium", new Double(5.0d)); // default for arrowheadLength
    ARROWHEAD_LENGTHS.put("large", new Double(6.0d));
    ARROWHEAD_LENGTHS.put("x-large", new Double(7.0d));
    ARROWHEAD_LENGTHS.put("xx-large", new Double(8.0d));
    ARROWHEAD_LENGTHS.put("xxx-large", new Double(9.0d));

    TICK_LENGTHS.put("xxx-small", new Float(3.0f));
    TICK_LENGTHS.put("xx-small", new Float(4.0f));
    TICK_LENGTHS.put("x-small", new Float(5.0f));
    TICK_LENGTHS.put("small", new Float(6.0f));
    TICK_LENGTHS.put("medium", new Float(7.0f)); // default for ticks
    TICK_LENGTHS.put("large", new Float(8.0f));
    TICK_LENGTHS.put("x-large", new Float(9.0f));
    TICK_LENGTHS.put("xx-large", new Float(10.0f));
    TICK_LENGTHS.put("xxx-large", new Float(11.0f));

    MINIMUM_FEATURE_LENGTHS.put("xxx-small", new Double(0.02d)); // default for minimumFeatureLength
    MINIMUM_FEATURE_LENGTHS.put("xx-small", new Double(0.05d));
    MINIMUM_FEATURE_LENGTHS.put("x-small", new Double(0.1d));
    MINIMUM_FEATURE_LENGTHS.put("small", new Double(0.5d));
    MINIMUM_FEATURE_LENGTHS.put("medium", new Double(1.0d));
    MINIMUM_FEATURE_LENGTHS.put("large", new Double(1.5d));
    MINIMUM_FEATURE_LENGTHS.put("x-large", new Double(2.0d));
    MINIMUM_FEATURE_LENGTHS.put("xx-large", new Double(2.5d));
    MINIMUM_FEATURE_LENGTHS.put("xxx-large", new Double(3.0d));

    ORIGINS.put("0", new Double(90.0d)); // modified this on 2015-10-28;
    ORIGINS.put("0.0", new Double(90.0d));
    ORIGINS.put("0.1", new Double(87.0d));
    ORIGINS.put("0.2", new Double(84.0d));
    ORIGINS.put("0.3", new Double(81.0d));
    ORIGINS.put("0.4", new Double(78.0d));
    ORIGINS.put("0.5", new Double(75.0d));
    ORIGINS.put("0.6", new Double(72.0d));
    ORIGINS.put("0.7", new Double(69.0d));
    ORIGINS.put("0.8", new Double(66.0d));
    ORIGINS.put("0.9", new Double(63.0d));
    ORIGINS.put("1", new Double(60.0d));
    ORIGINS.put("1.0", new Double(60.0d));
    ORIGINS.put("1.1", new Double(57.0d));
    ORIGINS.put("1.2", new Double(54.0d));
    ORIGINS.put("1.3", new Double(51.0d));
    ORIGINS.put("1.4", new Double(48.0d));
    ORIGINS.put("1.5", new Double(45.0d));
    ORIGINS.put("1.6", new Double(42.0d));
    ORIGINS.put("1.7", new Double(39.0d));
    ORIGINS.put("1.8", new Double(36.0d));
    ORIGINS.put("1.9", new Double(33.0d));
    ORIGINS.put("2", new Double(30.0d));
    ORIGINS.put("2.0", new Double(30.0d));
    ORIGINS.put("2.1", new Double(27.0d));
    ORIGINS.put("2.2", new Double(24.0d));
    ORIGINS.put("2.3", new Double(21.0d));
    ORIGINS.put("2.4", new Double(18.0d));
    ORIGINS.put("2.5", new Double(15.0d));
    ORIGINS.put("2.6", new Double(12.0d));
    ORIGINS.put("2.7", new Double(9.0d));
    ORIGINS.put("2.8", new Double(6.0d));
    ORIGINS.put("2.9", new Double(3.0d));
    ORIGINS.put("3", new Double(0.0d));
    ORIGINS.put("3.0", new Double(0.0d));
    ORIGINS.put("3.1", new Double(-3.0d));
    ORIGINS.put("3.2", new Double(-6.0d));
    ORIGINS.put("3.3", new Double(-9.0d));
    ORIGINS.put("3.4", new Double(-12.0d));
    ORIGINS.put("3.5", new Double(-15.0d));
    ORIGINS.put("3.6", new Double(-18.0d));
    ORIGINS.put("3.7", new Double(-21.0d));
    ORIGINS.put("3.8", new Double(-24.0d));
    ORIGINS.put("3.9", new Double(-27.0d));
    ORIGINS.put("4", new Double(-30.0d));
    ORIGINS.put("4.0", new Double(-30.0d));
    ORIGINS.put("4.1", new Double(-33.0d));
    ORIGINS.put("4.2", new Double(-36.0d));
    ORIGINS.put("4.3", new Double(-39.0d));
    ORIGINS.put("4.4", new Double(-42.0d));
    ORIGINS.put("4.5", new Double(-45.0d));
    ORIGINS.put("4.6", new Double(-48.0d));
    ORIGINS.put("4.7", new Double(-51.0d));
    ORIGINS.put("4.8", new Double(-54.0d));
    ORIGINS.put("4.9", new Double(-57.0d));
    ORIGINS.put("5", new Double(-60.0d));
    ORIGINS.put("5.0", new Double(-60.0d));
    ORIGINS.put("5.1", new Double(-63.0d));
    ORIGINS.put("5.2", new Double(-66.0d));
    ORIGINS.put("5.3", new Double(-69.0d));
    ORIGINS.put("5.4", new Double(-72.0d));
    ORIGINS.put("5.5", new Double(-75.0d));
    ORIGINS.put("5.6", new Double(-78.0d));
    ORIGINS.put("5.7", new Double(-81.0d));
    ORIGINS.put("5.8", new Double(-84.0d));
    ORIGINS.put("5.9", new Double(-87.0d));
    ORIGINS.put("6", new Double(-90.0d));
    ORIGINS.put("6.0", new Double(-90.0d));
    ORIGINS.put("6.1", new Double(-93.0d));
    ORIGINS.put("6.2", new Double(-96.0d));
    ORIGINS.put("6.3", new Double(-99.0d));
    ORIGINS.put("6.4", new Double(-102.0d));
    ORIGINS.put("6.5", new Double(-105.0d));
    ORIGINS.put("6.6", new Double(-108.0d));
    ORIGINS.put("6.7", new Double(-111.0d));
    ORIGINS.put("6.8", new Double(-114.0d));
    ORIGINS.put("6.9", new Double(-117.0d));
    ORIGINS.put("7", new Double(-120.0d));
    ORIGINS.put("7.0", new Double(-120.0d));
    ORIGINS.put("7.1", new Double(-123.0d));
    ORIGINS.put("7.2", new Double(-126.0d));
    ORIGINS.put("7.3", new Double(-129.0d));
    ORIGINS.put("7.4", new Double(-132.0d));
    ORIGINS.put("7.5", new Double(-135.0d));
    ORIGINS.put("7.6", new Double(-138.0d));
    ORIGINS.put("7.7", new Double(-141.0d));
    ORIGINS.put("7.8", new Double(-144.0d));
    ORIGINS.put("7.9", new Double(-147.0d));
    ORIGINS.put("8", new Double(-150.0d));
    ORIGINS.put("8.0", new Double(-150.0d));
    ORIGINS.put("8.1", new Double(-153.0d));
    ORIGINS.put("8.2", new Double(-156.0d));
    ORIGINS.put("8.3", new Double(-159.0d));
    ORIGINS.put("8.4", new Double(-162.0d));
    ORIGINS.put("8.5", new Double(-165.0d));
    ORIGINS.put("8.6", new Double(-168.0d));
    ORIGINS.put("8.7", new Double(-171.0d));
    ORIGINS.put("8.8", new Double(-174.0d));
    ORIGINS.put("8.9", new Double(-177.0d));
    ORIGINS.put("9", new Double(-180.0d));
    ORIGINS.put("9.0", new Double(-180.0d));
    ORIGINS.put("9.1", new Double(-183.0d));
    ORIGINS.put("9.2", new Double(-186.0d));
    ORIGINS.put("9.3", new Double(-189.0d));
    ORIGINS.put("9.4", new Double(-192.0d));
    ORIGINS.put("9.5", new Double(-195.0d));
    ORIGINS.put("9.6", new Double(-198.0d));
    ORIGINS.put("9.7", new Double(-201.0d));
    ORIGINS.put("9.8", new Double(-204.0d));
    ORIGINS.put("9.9", new Double(-207.0d));
    ORIGINS.put("10", new Double(-210.0d));
    ORIGINS.put("10.0", new Double(-210.0d));
    ORIGINS.put("10.1", new Double(-213.0d));
    ORIGINS.put("10.2", new Double(-216.0d));
    ORIGINS.put("10.3", new Double(-219.0d));
    ORIGINS.put("10.4", new Double(-222.0d));
    ORIGINS.put("10.5", new Double(-225.0d));
    ORIGINS.put("10.6", new Double(-228.0d));
    ORIGINS.put("10.7", new Double(-231.0d));
    ORIGINS.put("10.8", new Double(-234.0d));
    ORIGINS.put("10.9", new Double(-237.0d));
    ORIGINS.put("11", new Double(-240.0d));
    ORIGINS.put("11.0", new Double(-240.0d));
    ORIGINS.put("11.1", new Double(-243.0d));
    ORIGINS.put("11.2", new Double(-246.0d));
    ORIGINS.put("11.3", new Double(-249.0d));
    ORIGINS.put("11.4", new Double(-252.0d));
    ORIGINS.put("11.5", new Double(-255.0d));
    ORIGINS.put("11.6", new Double(-258.0d));
    ORIGINS.put("11.7", new Double(-261.0d));
    ORIGINS.put("11.8", new Double(-264.0d));
    ORIGINS.put("11.9", new Double(-267.0d));
    ORIGINS.put("12", new Double(90.0d)); // default for origin
    ORIGINS.put("12.0", new Double(90.0d));

    TICK_THICKNESSES.put("xxx-small", new Float(0.02f));
    TICK_THICKNESSES.put("xx-small", new Float(0.5f));
    TICK_THICKNESSES.put("x-small", new Float(1.0f));
    TICK_THICKNESSES.put("small", new Float(1.5f));
    TICK_THICKNESSES.put("medium", new Float(2.0f)); // default for tickThickness
    TICK_THICKNESSES.put("large", new Float(2.5f));
    TICK_THICKNESSES.put("x-large", new Float(3.0f));
    TICK_THICKNESSES.put("xx-large", new Float(3.5f));
    TICK_THICKNESSES.put("xxx-large", new Float(4.0f));

    LABEL_LINE_THICKNESSES.put("xxx-small", new Float(0.02f));
    LABEL_LINE_THICKNESSES.put("xx-small", new Float(0.25f));
    LABEL_LINE_THICKNESSES.put("x-small", new Float(0.50f));
    LABEL_LINE_THICKNESSES.put("small", new Float(0.75f));
    LABEL_LINE_THICKNESSES.put("medium", new Float(1.0f)); // default for labelLineThickness
    LABEL_LINE_THICKNESSES.put("large", new Float(1.25f));
    LABEL_LINE_THICKNESSES.put("x-large", new Float(1.5f));
    LABEL_LINE_THICKNESSES.put("xx-large", new Float(1.75f));
    LABEL_LINE_THICKNESSES.put("xxx-large", new Float(2.0f));

    LABEL_LINE_LENGTHS.put("xxx-small", new Double(10.0d));
    LABEL_LINE_LENGTHS.put("xx-small", new Double(20.0d));
    LABEL_LINE_LENGTHS.put("x-small", new Double(30.0d));
    LABEL_LINE_LENGTHS.put("small", new Double(40.0d));
    LABEL_LINE_LENGTHS.put("medium", new Double(50.0d)); // default for labelLineLength
    LABEL_LINE_LENGTHS.put("large", new Double(60.0d));
    LABEL_LINE_LENGTHS.put("x-large", new Double(70.0d));
    LABEL_LINE_LENGTHS.put("xx-large", new Double(80.0d));
    LABEL_LINE_LENGTHS.put("xxx-large", new Double(90.0d));

    LABEL_PLACEMENT_QUALITIES.put("good", new Integer(5));
    LABEL_PLACEMENT_QUALITIES.put("better", new Integer(8)); // default for labelPlacementQuality
    LABEL_PLACEMENT_QUALITIES.put("best", new Integer(10));

    BOOLEANS.put("true", new Boolean(true)); // default for showShading //default for moveInnerLabelsToOuter
    BOOLEANS.put("false", new Boolean(false)); // default for allowLabelClash //default for showWarning

    LEGEND_POSITIONS.put("upper-left", new Integer(LEGEND_UPPER_LEFT));
    LEGEND_POSITIONS.put("upper-center", new Integer(LEGEND_UPPER_CENTER));
    LEGEND_POSITIONS.put("upper-right", new Integer(LEGEND_UPPER_RIGHT));
    LEGEND_POSITIONS.put("middle-left", new Integer(LEGEND_MIDDLE_LEFT));
    LEGEND_POSITIONS.put(
      "middle-left-of-center",
      new Integer(LEGEND_MIDDLE_LEFT_OF_CENTER)
    );
    LEGEND_POSITIONS.put("middle-center", new Integer(LEGEND_MIDDLE_CENTER));
    LEGEND_POSITIONS.put(
      "middle-right-of-center",
      new Integer(LEGEND_MIDDLE_RIGHT_OF_CENTER)
    );
    LEGEND_POSITIONS.put("middle-right", new Integer(LEGEND_MIDDLE_RIGHT)); // default for legend
    LEGEND_POSITIONS.put("lower-left", new Integer(LEGEND_LOWER_LEFT));
    LEGEND_POSITIONS.put("lower-center", new Integer(LEGEND_LOWER_CENTER));
    LEGEND_POSITIONS.put("lower-right", new Integer(LEGEND_LOWER_RIGHT));

    LEGEND_SHOW_ZOOM.put("true", new Integer(LEGEND_DRAW_ZOOMED)); // default for legend
    LEGEND_SHOW_ZOOM.put("false", new Integer(LEGEND_NO_DRAW_ZOOMED));

    LEGEND_ALIGNMENTS.put("left", new Integer(LEGEND_ITEM_ALIGN_LEFT)); // default for legend //default for legendItem
    LEGEND_ALIGNMENTS.put("center", new Integer(LEGEND_ITEM_ALIGN_CENTER));
    LEGEND_ALIGNMENTS.put("right", new Integer(LEGEND_ITEM_ALIGN_RIGHT));

    SWATCH_TYPES.put("true", new Integer(SWATCH_SHOW));
    SWATCH_TYPES.put("false", new Integer(SWATCH_NO_SHOW)); // default for legendItem
  }

  /**
   * Generates a Cgview object from an XML file.
   *
   * @param filename the XML file to read.
   * @return the newly created Cgview object.
   * @throws SAXException
   * @throws IOException
   */
  public Cgview createCgviewFromFile(String filename)
    throws SAXException, IOException {
    XMLReader xr = new org.apache.xerces.parsers.SAXParser();
    xr.setContentHandler(this);

    ErrorHandler handler = new ErrorHandler() {

      public void warning(SAXParseException e) throws SAXException {
        System.err.println("[warning] " + e.getMessage());
      }

      public void error(SAXParseException e) throws SAXException {
        System.err.println("[error] " + e.getMessage());
      }

      public void fatalError(SAXParseException e) throws SAXException {
        System.err.println("[fatal error] " + e.getMessage());
        throw e;
      }
    };

    xr.setErrorHandler(handler);
    FileReader r = new FileReader(filename);
    xr.parse(new InputSource(r));

    if (currentCgview == null) {
      String error = "no cgview tags were encountered";
      throw new SAXException(error);
    }

    return currentCgview;
  }

  /**
   * Generates a Cgview object from a String of XML content.
   *
   * @param xml the XML content to read.
   * @return the newly created Cgview object.
   * @throws SAXException
   * @throws IOException
   */
  public Cgview createCgviewFromString(String xml)
    throws SAXException, IOException {
    XMLReader xr = new org.apache.xerces.parsers.SAXParser();
    xr.setContentHandler(this);

    ErrorHandler handler = new ErrorHandler() {

      public void warning(SAXParseException e) throws SAXException {
        System.err.println("[warning] " + e.getMessage());
      }

      public void error(SAXParseException e) throws SAXException {
        System.err.println("[error] " + e.getMessage());
      }

      public void fatalError(SAXParseException e) throws SAXException {
        System.err.println("[fatal error] " + e.getMessage());
        throw e;
      }
    };

    xr.setErrorHandler(handler);

    byte[] xml_bytes = xml.getBytes();
    ByteArrayInputStream b = new ByteArrayInputStream(xml_bytes);
    xr.parse(new InputSource(b));

    if (currentCgview == null) {
      String error = "no cgview tags were encountered";
      throw new SAXException(error);
    }

    return currentCgview;
  }

  /**
   * Adds FeatureSlot, Feature, and FeatureRange objects described in an XML file to an existing
   * Cgview object. Any Legend and LegendItem objects in the XML are ignored, as are attributes in
   * the cgview element.
   *
   * @param cgview the Cgview object to modify.
   * @param filename the XML file to supply the additional map content.
   * @throws SAXException
   * @throws IOException
   */
  public void addToCgviewFromFile(Cgview cgview, String filename)
    throws SAXException, IOException {
    ignoreCgviewTag = true;
    ignoreLegendTag = true;
    ignoreLegendItemTag = true;

    currentCgview = cgview;

    XMLReader xr = new org.apache.xerces.parsers.SAXParser();
    xr.setContentHandler(this);

    ErrorHandler handler = new ErrorHandler() {

      public void warning(SAXParseException e) throws SAXException {
        System.err.println("[warning] " + e.getMessage());
      }

      public void error(SAXParseException e) throws SAXException {
        System.err.println("[error] " + e.getMessage());
      }

      public void fatalError(SAXParseException e) throws SAXException {
        System.err.println("[fatal error] " + e.getMessage());
        throw e;
      }
    };

    xr.setErrorHandler(handler);
    FileReader r = new FileReader(filename);
    xr.parse(new InputSource(r));
    ignoreCgviewTag = false;
    ignoreLegendTag = false;
    ignoreLegendItemTag = false;
  }

  /**
   * Adds FeatureSlot, Feature, and FeatureRange objects described in a String of XML to an existing
   * Cgview object. Any Legend and LegendItem objects in the XML are ignored, as are attributes in
   * the cgview element.
   *
   * @param cgview the Cgview object to modify.
   * @param xml the XML content to read.
   * @throws SAXException
   * @throws IOException
   */
  public void addToCgviewFromString(Cgview cgview, String xml)
    throws SAXException, IOException {
    ignoreCgviewTag = true;
    ignoreLegendTag = true;
    ignoreLegendItemTag = true;

    currentCgview = cgview;

    XMLReader xr = new org.apache.xerces.parsers.SAXParser();
    xr.setContentHandler(this);

    ErrorHandler handler = new ErrorHandler() {

      public void warning(SAXParseException e) throws SAXException {
        System.err.println("[warning] " + e.getMessage());
      }

      public void error(SAXParseException e) throws SAXException {
        System.err.println("[error] " + e.getMessage());
      }

      public void fatalError(SAXParseException e) throws SAXException {
        System.err.println("[fatal error] " + e.getMessage());
        throw e;
      }
    };

    xr.setErrorHandler(handler);

    byte[] xml_bytes = xml.getBytes();
    ByteArrayInputStream b = new ByteArrayInputStream(xml_bytes);
    xr.parse(new InputSource(b));

    ignoreCgviewTag = false;
    ignoreLegendTag = false;
    ignoreLegendItemTag = false;
  }

  /**
   * Sets the font size of feature labels. Use this method before calling createCgviewFromURL() or
   * createCgviewFromFile().
   *
   * @param size the font size of feature labels.
   */
  public void setLabelFontSize(int size) {
    if (size < 0) {
      size = 0;
    } else if (size > 100) {
      size = 100;
    }

    labelFontSize = size;
  }

  /**
   * Sets the font size of the sequence ruler. Use this method before calling createCgviewFromURL()
   * or createCgviewFromFile().
   *
   * @param size the font size of the sequence ruler.
   */
  public void setRulerFontSize(int size) {
    if (size < 0) {
      size = 0;
    } else if (size > 100) {
      size = 100;
    }

    rulerFontSize = size;
  }

  /**
   * Sets the font size of legends. Use this method before calling createCgviewFromURL() or
   * createCgviewFromFile().
   *
   * @param size the font size of legends.
   */
  public void setLegendFontSize(int size) {
    if (size < 0) {
      size = 0;
    } else if (size > 100) {
      size = 100;
    }

    legendFontSize = size;
  }

  public void setDocumentLocator(Locator loc) {
    locator = loc;
  }

  public void startDocument() {
    System.out.println("Parsing XML input.");
  }

  public void endDocument() {
    // System.out.println("End document");
  }

  public void startElement(
    String uri,
    String name,
    String qName,
    Attributes atts
  )
    throws SAXException {
    // System.out.println("Start element: " + name);
    ElementDetails details = new ElementDetails(name, atts);
    context.push(details);

    if ((name.equalsIgnoreCase("cgview")) && (!(ignoreCgviewTag))) {
      handleCgview();
    } else if (name.equalsIgnoreCase("featureSlot")) {
      handleFeatureSlot();
    } else if (name.equalsIgnoreCase("feature")) {
      handleFeature();
    } else if (name.equalsIgnoreCase("featureRange")) {
      handleFeatureRange();
    } else if ((name.equalsIgnoreCase("legend")) && (!(ignoreLegendTag))) {
      handleLegend();
    } else if (
      (name.equalsIgnoreCase("legendItem")) && (!(ignoreLegendItemTag))
    ) {
      handleLegendItem();
    }

    content.setLength(0);
  }

  public void characters(char[] ch, int start, int length) {
    content.append(ch, start, length);
  }

  public void endElement(String uri, String name, String qName)
    throws SAXException {
    // System.out.println("End element: " + name);
    content.setLength(0);
    context.pop();

    if (name.equalsIgnoreCase("cgview")) {
      // currentCgview = null;
    } else if (name.equalsIgnoreCase("featureSlot")) {
      currentFeatureSlot = null;
    } else if (name.equalsIgnoreCase("feature")) {
      currentFeature = null;
    } else if (name.equalsIgnoreCase("featureRange")) {
      currentFeatureRange = null;
    } else if (name.equalsIgnoreCase("legend")) {
      currentLegend = null;
    } else if (name.equalsIgnoreCase("legendItem")) {
      currentLegendItem = null;
    }
  }

  /**
   * Handles the cgview element and its attributes.
   *
   * @throws SAXException
   */
  // required attributes: sequenceLength.
  // optional attributes: title, rulerUnits, rulerFont, rulerPadding, labelFont, useInnerLabels,
  // featureThickness, featureSlotSpacing, backboneThickness, arrowheadLength, minimumFeatureLength,
  // titleFont, titleFontColor, rulerFontColor, origin, tickThickness, tickLength,
  // labelLineThickness, labelLineLength, backboneColor, backgroundColor, longTickColor,
  // giveFeaturePositions, shortTickColor, zeroTickColor, width, height, backboneRadius,
  // labelPlacementQuality, useColoredLabelBackgrounds, showWarning, warningFont, info,
  // warningFontColor, showShading, showBorder, borderColor, moveInnerLabelsToOuter, globalLabel,
  // labelsToKeep, globalLabelColor, tickDensity.
  private void handleCgview() throws SAXException {
    for (int p = context.size() - 1; p >= 0; p--) {
      ElementDetails elem = (ElementDetails) context.elementAt(p);
      if (elem.name.equalsIgnoreCase("cgview")) {
        if (currentCgview != null) {
          // an error because already in a cgview tag
          String error =
            "cgview element encountered inside of another cgview element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        }
        if (elem.attributes.getValue("sequenceLength") == null) {
          // an error because no length
          String error = "cgview element is missing 'sequenceLength' attribute";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else {
          try {
            cgviewLength =
              Integer.parseInt(elem.attributes.getValue("sequenceLength"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'sequenceLength' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (cgviewLength > MAX_BASES) {
            String error =
              "value for 'sequenceLength' attribute in cgview element must be less than " +
              MAX_BASES;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (cgviewLength < MIN_BASES) {
            String error =
              "value for 'sequenceLength' attribute in cgview element must be greater than " +
              MIN_BASES;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentCgview = new Cgview(cgviewLength);
        }

        // optional tags

        // title
        if (elem.attributes.getValue("title") != null) {
          currentCgview.setTitle(elem.attributes.getValue("title"));
        }

        // info
        if (elem.attributes.getValue("info") != null) {
          currentCgview.setWarningText(elem.attributes.getValue("info"));
        }

        // rulerUnits
        if (elem.attributes.getValue("rulerUnits") != null) {
          if (
            RULER_UNITS.get(
              ((elem.attributes.getValue("rulerUnits"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setRulerUnits(
              (
                (Integer) RULER_UNITS.get(
                  ((elem.attributes.getValue("rulerUnits"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'rulerUnits' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // showWarning
        if (elem.attributes.getValue("showWarning") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("showWarning"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setShowWarning(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("showWarning"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'showWarning' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // isLinear
        if (elem.attributes.getValue("isLinear") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("isLinear"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setIsLinear(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("isLinear"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'isLinear' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // showBorder
        if (elem.attributes.getValue("showBorder") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("showBorder"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setShowBorder(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("showBorder"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'showBorder' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // moveInnerLabelsToOuter
        if (elem.attributes.getValue("moveInnerLabelsToOuter") != null) {
          if (
            BOOLEANS.get(
              (
                (elem.attributes.getValue("moveInnerLabelsToOuter"))
              ).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setMoveInnerLabelsToOuter(
              (
                (Boolean) BOOLEANS.get(
                  (
                    (elem.attributes.getValue("moveInnerLabelsToOuter"))
                  ).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'moveInnerLabelsToOuter' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // showShading
        if (elem.attributes.getValue("showShading") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("showShading"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setShowShading(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("showShading"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'showShading' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // useColoredLabelBackgrounds
        if (elem.attributes.getValue("useColoredLabelBackgrounds") != null) {
          if (
            BOOLEANS.get(
              (
                (elem.attributes.getValue("useColoredLabelBackgrounds"))
              ).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setUseColoredLabelBackgrounds(
              (
                (Boolean) BOOLEANS.get(
                  (
                    (elem.attributes.getValue("useColoredLabelBackgrounds"))
                  ).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'useColoredLabelBackgrounds' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // labelPlacementQuality
        if (elem.attributes.getValue("labelPlacementQuality") != null) {
          if (
            LABEL_PLACEMENT_QUALITIES.get(
              (
                (elem.attributes.getValue("labelPlacementQuality"))
              ).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setLabelPlacementQuality(
              (
                (Integer) LABEL_PLACEMENT_QUALITIES.get(
                  (
                    (elem.attributes.getValue("labelPlacementQuality"))
                  ).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'labelPlacementQuality' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // useInnerLabels
        if (elem.attributes.getValue("useInnerLabels") != null) {
          if (
            USE_INNER_LABELS.get(
              ((elem.attributes.getValue("useInnerLabels"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setUseInnerLabels(
              (
                (Integer) USE_INNER_LABELS.get(
                  ((elem.attributes.getValue("useInnerLabels"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'useInnerLabels' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // giveFeaturePositions
        if (elem.attributes.getValue("giveFeaturePositions") != null) {
          if (
            GIVE_FEATURE_POSITIONS.get(
              ((elem.attributes.getValue("giveFeaturePositions"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setGiveFeaturePositions(
              (
                (Integer) GIVE_FEATURE_POSITIONS.get(
                  (
                    (elem.attributes.getValue("giveFeaturePositions"))
                  ).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'giveFeaturePositions' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // titleFont
        if (elem.attributes.getValue("titleFont") != null) {
          m =
            fontDescriptionPattern.matcher(
              elem.attributes.getValue("titleFont")
            );
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentCgview.setTitleFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'titleFont' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'titleFont' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // warningFont
        if (elem.attributes.getValue("warningFont") != null) {
          m =
            fontDescriptionPattern.matcher(
              elem.attributes.getValue("warningFont")
            );
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentCgview.setWarningFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'warningFont' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'warningFont' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // rulerFont
        if (elem.attributes.getValue("rulerFont") != null) {
          m =
            fontDescriptionPattern.matcher(
              elem.attributes.getValue("rulerFont")
            );
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentCgview.setRulerFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'rulerFont' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'rulerFont' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // labelFont
        if (elem.attributes.getValue("labelFont") != null) {
          m =
            fontDescriptionPattern.matcher(
              elem.attributes.getValue("labelFont")
            );
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentCgview.setLabelFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'labelFont' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'labelFont' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // titleFontColor
        if (elem.attributes.getValue("titleFontColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("titleFontColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setTitleFontColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("titleFontColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("titleFontColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setTitleFontColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'titleFontColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'titleFontColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // globalLabelColor
        if (elem.attributes.getValue("globalLabelColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("globalLabelColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setGlobalLabelColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("globalLabelColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("globalLabelColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setGlobalLabelColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'globalLabelColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'globalLabelColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // warningFontColor
        if (elem.attributes.getValue("warningFontColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("warningFontColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setWarningFontColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("warningFontColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("warningFontColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setWarningFontColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'warningFontColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'warningFontColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // borderColor
        if (elem.attributes.getValue("borderColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("borderColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setBorderColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("borderColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("borderColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setBorderColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'borderColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'borderColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // rulerFontColor
        if (elem.attributes.getValue("rulerFontColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("rulerFontColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setRulerFontColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("rulerFontColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("rulerFontColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setRulerFontColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'rulerFontColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'rulerFontColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // backboneColor
        if (elem.attributes.getValue("backboneColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("backboneColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setBackboneColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("backboneColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("backboneColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setBackboneColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'backboneColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'backboneColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // backgroundColor
        if (elem.attributes.getValue("backgroundColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("backgroundColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setBackgroundColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("backgroundColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("backgroundColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setBackgroundColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'backgroundColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'backgroundColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // longTickColor
        if (elem.attributes.getValue("longTickColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("longTickColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setLongTickColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("longTickColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("longTickColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setLongTickColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'longTickColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'longTickColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // shortTickColor
        if (elem.attributes.getValue("shortTickColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("shortTickColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setShortTickColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("shortTickColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("shortTickColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setShortTickColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'shortTickColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'shortTickColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // zeroTickColor
        if (elem.attributes.getValue("zeroTickColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("zeroTickColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setZeroTickColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("zeroTickColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("zeroTickColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentCgview.setZeroTickColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'zeroTickColor' attribute in cgview element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'zeroTickColor' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // featureThickness
        if (elem.attributes.getValue("featureThickness") != null) {
          if (
            FEATURE_THICKNESSES.get(
              ((elem.attributes.getValue("featureThickness"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setFeatureThickness(
              (
                (Float) FEATURE_THICKNESSES.get(
                  ((elem.attributes.getValue("featureThickness"))).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("featureThickness")
              );
              currentCgview.setFeatureThickness(s);
            } catch (Exception e) {
              String error =
                "value for 'featureThickness' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // featureSlotSpacing
        if (elem.attributes.getValue("featureSlotSpacing") != null) {
          if (
            FEATURESLOT_SPACINGS.get(
              ((elem.attributes.getValue("featureSlotSpacing"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setFeatureSlotSpacing(
              (
                (Float) FEATURESLOT_SPACINGS.get(
                  (
                    (elem.attributes.getValue("featureSlotSpacing"))
                  ).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("featureSlotSpacing")
              );
              currentCgview.setFeatureSlotSpacing(s);
            } catch (Exception e) {
              String error =
                "value for 'featureSlotSpacing' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // backboneThickness
        if (elem.attributes.getValue("backboneThickness") != null) {
          if (
            BACKBONE_THICKNESSES.get(
              ((elem.attributes.getValue("backboneThickness"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setBackboneThickness(
              (
                (Float) BACKBONE_THICKNESSES.get(
                  (
                    (elem.attributes.getValue("backboneThickness"))
                  ).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("backboneThickness")
              );
              currentCgview.setBackboneThickness(s);
            } catch (Exception e) {
              String error =
                "value for 'backboneThickness' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // rulerPadding
        if (elem.attributes.getValue("rulerPadding") != null) {
          try {
            double s = Double.parseDouble(
              elem.attributes.getValue("rulerPadding")
            );
            currentCgview.setRulerTextPadding(s);
          } catch (Exception e) {
            String error =
              "value for 'rulerPadding' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // arrowheadLength
        if (elem.attributes.getValue("arrowheadLength") != null) {
          if (
            ARROWHEAD_LENGTHS.get(
              ((elem.attributes.getValue("arrowheadLength"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setArrowheadLength(
              (
                (Double) ARROWHEAD_LENGTHS.get(
                  ((elem.attributes.getValue("arrowheadLength"))).toLowerCase()
                )
              ).doubleValue()
            );
          } else {
            try {
              double s = Double.parseDouble(
                elem.attributes.getValue("arrowheadLength")
              );
              currentCgview.setArrowheadLength(s);
            } catch (Exception e) {
              String error =
                "value for 'arrowheadLength' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // tickDensity
        if (elem.attributes.getValue("tickDensity") != null) {
          try {
            double s = Double.parseDouble(
              elem.attributes.getValue("tickDensity")
            );
            currentCgview.setTickDensity(s);
          } catch (Exception e) {
            String error =
              "value for 'tickDensity' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // tickLength
        if (elem.attributes.getValue("tickLength") != null) {
          if (
            TICK_LENGTHS.get(
              ((elem.attributes.getValue("tickLength"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setTickLength(
              (
                (Float) TICK_LENGTHS.get(
                  ((elem.attributes.getValue("tickLength"))).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("tickLength")
              );
              currentCgview.setTickLength(s);
            } catch (Exception e) {
              String error =
                "value for 'tickLength' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // minimumFeatureLength
        if (elem.attributes.getValue("minimumFeatureLength") != null) {
          if (
            MINIMUM_FEATURE_LENGTHS.get(
              ((elem.attributes.getValue("minimumFeatureLength"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setMinimumFeatureLength(
              (
                (Double) MINIMUM_FEATURE_LENGTHS.get(
                  (
                    (elem.attributes.getValue("minimumFeatureLength"))
                  ).toLowerCase()
                )
              ).doubleValue()
            );
          } else {
            try {
              double s = Double.parseDouble(
                elem.attributes.getValue("minimumFeatureLength")
              );
              currentCgview.setMinimumFeatureLength(s);
            } catch (Exception e) {
              String error =
                "value for 'minimumFeatureLength' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // tickThickness
        if (elem.attributes.getValue("tickThickness") != null) {
          if (
            TICK_THICKNESSES.get(
              ((elem.attributes.getValue("tickThickness"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setTickThickness(
              (
                (Float) TICK_THICKNESSES.get(
                  ((elem.attributes.getValue("tickThickness"))).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("tickThickness")
              );
              currentCgview.setTickThickness(s);
            } catch (Exception e) {
              String error =
                "value for 'tickThickness' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // shortTickThickness
        if (elem.attributes.getValue("shortTickThickness") != null) {
          if (
            TICK_THICKNESSES.get(
              ((elem.attributes.getValue("shortTickThickness"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setShortTickThickness(
              (
                (Float) TICK_THICKNESSES.get(
                  (
                    (elem.attributes.getValue("shortTickThickness"))
                  ).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("shortTickThickness")
              );
              currentCgview.setShortTickThickness(s);
            } catch (Exception e) {
              String error =
                "value for 'shortTickThickness' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // labelLineThickness
        if (elem.attributes.getValue("labelLineThickness") != null) {
          if (
            LABEL_LINE_THICKNESSES.get(
              ((elem.attributes.getValue("labelLineThickness"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setLabelLineThickness(
              (
                (Float) LABEL_LINE_THICKNESSES.get(
                  (
                    (elem.attributes.getValue("labelLineThickness"))
                  ).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("labelLineThickness")
              );
              currentCgview.setLabelLineThickness(s);
            } catch (Exception e) {
              String error =
                "value for 'labelLineThickness' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // labelLineLength
        if (elem.attributes.getValue("labelLineLength") != null) {
          if (
            LABEL_LINE_LENGTHS.get(
              ((elem.attributes.getValue("labelLineLength"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setLabelLineLength(
              (
                (Double) LABEL_LINE_LENGTHS.get(
                  ((elem.attributes.getValue("labelLineLength"))).toLowerCase()
                )
              ).doubleValue()
            );
          } else {
            try {
              double s = Double.parseDouble(
                elem.attributes.getValue("labelLineLength")
              );
              currentCgview.setLabelLineLength(s);
            } catch (Exception e) {
              String error =
                "value for 'labelLineLength' attribute in cgview element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // origin
        if (elem.attributes.getValue("origin") != null) {
          if (
            ORIGINS.get(((elem.attributes.getValue("origin"))).toLowerCase()) !=
            null
          ) {
            currentCgview.setOrigin(
              (
                (Double) ORIGINS.get(
                  ((elem.attributes.getValue("origin"))).toLowerCase()
                )
              ).doubleValue()
            );
          } else {
            String error =
              "value for 'origin' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // width
        if (elem.attributes.getValue("width") != null) {
          try {
            imageWidth = Integer.parseInt(elem.attributes.getValue("width"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'width' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (imageWidth < MIN_IMAGE_WIDTH) {
            String error =
              "value for 'width' attribute in cgview element must be greater than or equal to " +
              MIN_IMAGE_WIDTH;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (imageWidth > MAX_IMAGE_WIDTH) {
            String error =
              "value for 'width' attribute in cgview element must be less than or equal to " +
              MAX_IMAGE_WIDTH;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }
          currentCgview.setWidth(imageWidth);
        }

        // height
        if (elem.attributes.getValue("height") != null) {
          try {
            imageHeight = Integer.parseInt(elem.attributes.getValue("height"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'height' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (imageHeight < MIN_IMAGE_HEIGHT) {
            String error =
              "value for 'height' attribute in cgview element must be greater than or equal to " +
              MIN_IMAGE_HEIGHT;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (imageHeight > MAX_IMAGE_HEIGHT) {
            String error =
              "value for 'height' attribute in cgview element must be less than or equal to " +
              MAX_IMAGE_HEIGHT;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentCgview.setHeight(imageHeight);
        }

        // backboneRadius
        if (elem.attributes.getValue("backboneRadius") != null) {
          double radius;
          try {
            radius =
              Double.parseDouble(elem.attributes.getValue("backboneRadius"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'backboneRadius' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (radius < MIN_BACKBONE_RADIUS) {
            String error =
              "value for 'backboneRadius' attribute in cgview element must be greater than or equal to " +
              MIN_BACKBONE_RADIUS;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (radius > MAX_BACKBONE_RADIUS) {
            String error =
              "value for 'backboneRadius' attribute in cgview element must be less than or equal to " +
              MAX_BACKBONE_RADIUS;
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentCgview.setBackboneRadius(radius);
        }

        // globalLabel
        if (elem.attributes.getValue("globalLabel") != null) {
          if (
            GLOBAL_LABEL_TYPES.get(
              ((elem.attributes.getValue("globalLabel"))).toLowerCase()
            ) !=
            null
          ) {
            currentCgview.setGlobalLabel(
              (
                (Integer) GLOBAL_LABEL_TYPES.get(
                  ((elem.attributes.getValue("globalLabel"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'globalLabel' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // labelsToKeep
        if (elem.attributes.getValue("labelsToKeep") != null) {
          int labelsToKeep;
          try {
            labelsToKeep =
              Integer.parseInt(elem.attributes.getValue("labelsToKeep"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'labelsToKeep' attribute in cgview element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (labelsToKeep < 0) {
            String error =
              "value for 'labelsToKeep' attribute in cgview element must be greater than or equal to 0";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentCgview.setLabelsToKeep(labelsToKeep);
        }

        if (this.rulerFontSize != -1) {
          // rulerFont = new Font("SansSerif", Font.PLAIN, this.rulerFontSize);
          currentCgview.setRulerFont(
            new Font(
              currentCgview.getRulerFont().getName(),
              currentCgview.getRulerFont().getStyle(),
              this.rulerFontSize
            )
          );
        }
      }
    }
  }

  /**
   * Handles the featureSlot element and its attributes.
   *
   * @throws SAXException
   */
  // required attributes: strand.
  // optional attributes featureThickness, showShading:
  private void handleFeatureSlot() throws SAXException {
    for (int p = context.size() - 1; p >= 0; p--) {
      ElementDetails elem = (ElementDetails) context.elementAt(p);
      if (elem.name.equalsIgnoreCase("featureSlot")) {
        if (currentFeatureSlot != null) {
          // an error because already in a FeatureSlot tag
          String error =
            "featureSlot element encountered inside of another featureSlot element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentCgview == null) {
          // an error because no currentCgview
          String error =
            "featureSlot element encountered outside of a cgview element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (elem.attributes.getValue("strand") == null) {
          // an error because no strand given
          String error = "featureSlot element is missing 'strand' attribute";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else {
          if ((elem.attributes.getValue("strand")).equalsIgnoreCase("direct")) {
            currentFeatureSlot = new FeatureSlot(currentCgview, DIRECT_STRAND);
          } else if (
            (elem.attributes.getValue("strand")).equalsIgnoreCase("reverse")
          ) {
            currentFeatureSlot = new FeatureSlot(currentCgview, REVERSE_STRAND);
          } else {
            // an error because strand could not be understood
            String error =
              "value for 'strand' attribute in featureSlot element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }
        }
        // optional tags
        // featureThickness
        if (elem.attributes.getValue("featureThickness") != null) {
          if (
            FEATURE_THICKNESSES.get(
              ((elem.attributes.getValue("featureThickness"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeatureSlot.setFeatureThickness(
              (
                (Float) FEATURE_THICKNESSES.get(
                  ((elem.attributes.getValue("featureThickness"))).toLowerCase()
                )
              ).floatValue()
            );
          } else {
            try {
              float s = Float.parseFloat(
                elem.attributes.getValue("featureThickness")
              );
              currentFeatureSlot.setFeatureThickness(s);
            } catch (Exception e) {
              String error =
                "value for 'featureThickness' attribute in featureSlot element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // minimumFeatureLength
        if (elem.attributes.getValue("minimumFeatureLength") != null) {
          if (
            MINIMUM_FEATURE_LENGTHS.get(
              ((elem.attributes.getValue("minimumFeatureLength"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeatureSlot.setMinimumFeatureLength(
              (
                (Double) MINIMUM_FEATURE_LENGTHS.get(
                  (
                    (elem.attributes.getValue("minimumFeatureLength"))
                  ).toLowerCase()
                )
              ).doubleValue()
            );
          } else {
            try {
              double s = Double.parseDouble(
                elem.attributes.getValue("minimumFeatureLength")
              );
              currentFeatureSlot.setMinimumFeatureLength(s);
            } catch (Exception e) {
              String error =
                "value for 'minimumFeatureLength' attribute in featureSlot element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // showShading
        if (elem.attributes.getValue("showShading") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("showShading"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeatureSlot.setShowShading(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("showShading"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'showShading' attribute in featureSlot element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
      }
    }
  }

  /**
   * Handles the feature element and its attributes.
   *
   * @throws SAXException
   */
  // required attributes:
  // optional attributes: color, opacity, proportionOfThickness, radiusAdjustment, decoration,
  // showLabel, font, label, showShading, hyperlink, mouseover
  private void handleFeature() throws SAXException {
    for (int p = context.size() - 1; p >= 0; p--) {
      ElementDetails elem = (ElementDetails) context.elementAt(p);
      if (elem.name.equalsIgnoreCase("feature")) {
        if (currentFeature != null) {
          // an error because already in a Feature tag
          String error =
            "feature element encountered inside of another feature element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentCgview == null) {
          // an error because no currentCgview
          String error =
            "feature element encountered outside of a cgview element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentFeatureSlot == null) {
          // an error because no currentFeatureSlot
          String error =
            "feature element encountered outside of a featureSlot element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else {
          currentFeature = new Feature(currentFeatureSlot);
        }

        // optional tags
        // color
        if (elem.attributes.getValue("color") != null) {
          if (
            COLORS.get(((elem.attributes.getValue("color"))).toLowerCase()) !=
            null
          ) {
            currentFeature.setColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("color"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("color")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentFeature.setColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'color' attribute in feature element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'color' attribute in feature element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }
        // opacity
        if (elem.attributes.getValue("opacity") != null) {
          float opacity;
          try {
            opacity = Float.parseFloat(elem.attributes.getValue("opacity"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'opacity' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity > 1.0f) {
            String error =
              "value for 'opacity' attribute in feature element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity < 0.0f) {
            String error =
              "value for 'opacity' attribute in feature element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeature.setOpacity(opacity);
        }
        // proportionOfThickness
        if (elem.attributes.getValue("proportionOfThickness") != null) {
          float thickness;
          try {
            thickness =
              Float.parseFloat(
                elem.attributes.getValue("proportionOfThickness")
              );
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'proportionOfThickness' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (thickness > 1.0f) {
            String error =
              "value for 'proportionOfThickness' attribute in feature element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (thickness < 0.0f) {
            String error =
              "value for 'proportionOfThickness' attribute in feature element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeature.setProportionOfThickness(thickness);
        }
        // radiusAdjustment
        if (elem.attributes.getValue("radiusAdjustment") != null) {
          float height;
          try {
            height =
              Float.parseFloat(elem.attributes.getValue("radiusAdjustment"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'radiusAdjustment' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (height > 1.0f) {
            String error =
              "value for 'radiusAdjustment' attribute in feature element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (height < 0.0f) {
            String error =
              "value for 'radiusAdjustment' attribute in feature element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeature.setRadiusAdjustment(height);
        }
        // decoration
        if (elem.attributes.getValue("decoration") != null) {
          if (
            DECORATIONS.get(
              ((elem.attributes.getValue("decoration"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeature.setDecoration(
              (
                (Integer) DECORATIONS.get(
                  ((elem.attributes.getValue("decoration"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'decoration' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // showLabel
        if (elem.attributes.getValue("showLabel") != null) {
          if (
            LABEL_TYPES.get(
              ((elem.attributes.getValue("showLabel"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeature.setShowLabel(
              (
                (Integer) LABEL_TYPES.get(
                  ((elem.attributes.getValue("showLabel"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'showLabel' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // font
        if (elem.attributes.getValue("font") != null) {
          m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentFeature.setFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'font' attribute in feature element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'font' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // label
        if (elem.attributes.getValue("label") != null) {
          currentFeature.setLabel(elem.attributes.getValue("label"));
        }
        // showShading
        if (elem.attributes.getValue("showShading") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("showShading"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeature.setShowShading(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("showShading"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'showShading' attribute in feature element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // hyperlink
        if (elem.attributes.getValue("hyperlink") != null) {
          currentFeature.setHyperlink(elem.attributes.getValue("hyperlink"));
        }
        // mouseover
        if (elem.attributes.getValue("mouseover") != null) {
          currentFeature.setMouseover(elem.attributes.getValue("mouseover"));
        }

        if (this.labelFontSize != -1) {
          if (currentFeature.getFont() != null) {
            currentFeature.setFont(
              new Font(
                currentFeature.getFont().getName(),
                currentFeature.getFont().getStyle(),
                this.labelFontSize
              )
            );
          } else {
            currentFeature.setFont(
              new Font(
                currentCgview.getLabelFont().getName(),
                currentCgview.getLabelFont().getStyle(),
                this.labelFontSize
              )
            );
          }
        }
      }
    }
  }

  /**
   * Handles the featureRange element and its attributes.
   *
   * @throws SAXException
   */
  // required attributes: start, stop
  // optional attributes: color, opacity, proportionOfThickness, radiusAdjustment, decoration,
  // showLabel, font, label, showShading, hyperlink, mouseover
  private void handleFeatureRange() throws SAXException {
    for (int p = context.size() - 1; p >= 0; p--) {
      ElementDetails elem = (ElementDetails) context.elementAt(p);
      if (elem.name.equalsIgnoreCase("featureRange")) {
        if (currentFeatureRange != null) {
          // an error because already in a FeatureRange
          String error =
            "featureRange element encountered inside of another featureRange element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentFeature == null) {
          // an error because no current Feature
          String error =
            "featureRange element encountered outside of a feature element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentCgview == null) {
          // an error because no currentCgview
          String error =
            "featureRange element encountered outside of a cgview element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentFeatureSlot == null) {
          // an error because no currentFeatureSlot
          String error =
            "featureRange element encountered outside of a featureSlot element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (elem.attributes.getValue("start") == null) {
          // an error because no length
          String error = "featureRange element is missing 'start' attribute";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (elem.attributes.getValue("stop") == null) {
          // an error because no length
          String error = "featureRange element is missing 'stop' attribute";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else {
          int start;
          try {
            start = Integer.parseInt(elem.attributes.getValue("start"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'start' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (start > cgviewLength) {
            String error =
              "value for 'start' attribute in featureRange element must be less than or equal to the length of the plasmid";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (start < 1) {
            String error =
              "value for 'start' attribute in featureRange element must be greater than or equal to 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          int stop;
          try {
            stop = Integer.parseInt(elem.attributes.getValue("stop"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'stop' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (stop > cgviewLength) {
            String error =
              "value for 'stop' attribute in featureRange element must be less than or equal to the length of the plasmid";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (stop < 1) {
            String error =
              "value for 'stop' attribute in featureRange element must be greater than or equal to 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeatureRange = new FeatureRange(currentFeature, start, stop);
        }
        // optional tags
        // color
        if (elem.attributes.getValue("color") != null) {
          if (
            COLORS.get(((elem.attributes.getValue("color"))).toLowerCase()) !=
            null
          ) {
            currentFeatureRange.setColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("color"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("color")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentFeatureRange.setColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'color' attribute in featureRange element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'color' attribute in featureRange element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // opacity
        if (elem.attributes.getValue("opacity") != null) {
          float opacity;
          try {
            opacity = Float.parseFloat(elem.attributes.getValue("opacity"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'opacity' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity > 1.0f) {
            String error =
              "value for 'opacity' attribute in featureRange element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity < 0.0f) {
            String error =
              "value for 'opacity' attribute in featureRange element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeatureRange.setOpacity(opacity);
        }
        // proportionOfThickness
        if (elem.attributes.getValue("proportionOfThickness") != null) {
          float thickness;
          try {
            thickness =
              Float.parseFloat(
                elem.attributes.getValue("proportionOfThickness")
              );
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'proportionOfThickness' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (thickness > 1.0f) {
            String error =
              "value for 'proportionOfThickness' attribute in featureRange element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (thickness < 0.0f) {
            String error =
              "value for 'proportionOfThickness' attribute in featureRange element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeatureRange.setProportionOfThickness(thickness);
        }
        // radiusAdjustment
        if (elem.attributes.getValue("radiusAdjustment") != null) {
          float height;
          try {
            height =
              Float.parseFloat(elem.attributes.getValue("radiusAdjustment"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'radiusAdjustment' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (height > 1.0f) {
            String error =
              "value for 'radiusAdjustment' attribute in featureRange element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (height < 0.0f) {
            String error =
              "value for 'radiusAdjustment' attribute in featureRange element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentFeatureRange.setRadiusAdjustment(height);
        }
        // decoration
        if (elem.attributes.getValue("decoration") != null) {
          if (
            DECORATIONS.get(
              ((elem.attributes.getValue("decoration"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeatureRange.setDecoration(
              (
                (Integer) DECORATIONS.get(
                  ((elem.attributes.getValue("decoration"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'decoration' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // showLabel
        if (elem.attributes.getValue("showLabel") != null) {
          if (
            LABEL_TYPES.get(
              ((elem.attributes.getValue("showLabel"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeatureRange.setShowLabel(
              (
                (Integer) LABEL_TYPES.get(
                  ((elem.attributes.getValue("showLabel"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'showLabel' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // font
        if (elem.attributes.getValue("font") != null) {
          m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentFeatureRange.setFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'font' attribute in featureRange element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'font' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // label
        if (elem.attributes.getValue("label") != null) {
          currentFeatureRange.setLabel(elem.attributes.getValue("label"));
        }
        // showShading
        if (elem.attributes.getValue("showShading") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("showShading"))).toLowerCase()
            ) !=
            null
          ) {
            currentFeatureRange.setShowShading(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("showShading"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'showShading' attribute in featureRange element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }
        // hyperlink
        if (elem.attributes.getValue("hyperlink") != null) {
          currentFeatureRange.setHyperlink(
            elem.attributes.getValue("hyperlink")
          );
        }
        // mouseover
        if (elem.attributes.getValue("mouseover") != null) {
          currentFeatureRange.setMouseover(
            elem.attributes.getValue("mouseover")
          );
        }

        if (this.labelFontSize != -1) {
          if (currentFeatureRange.getFont() != null) {
            currentFeatureRange.setFont(
              new Font(
                currentFeatureRange.getFont().getName(),
                currentFeatureRange.getFont().getStyle(),
                this.labelFontSize
              )
            );
          } else if (currentFeature.getFont() != null) {
            currentFeatureRange.setFont(
              new Font(
                currentFeature.getFont().getName(),
                currentFeature.getFont().getStyle(),
                this.labelFontSize
              )
            );
          } else {
            currentFeatureRange.setFont(
              new Font(
                currentCgview.getLabelFont().getName(),
                currentCgview.getLabelFont().getStyle(),
                this.labelFontSize
              )
            );
          }
        }
      }
    }
  }

  /**
   * Handles the legend element and its attributes.
   *
   * @throws SAXException
   */
  // required attributes: none.
  // optional attributes: font, fontColor, position, drawWhenZoomed, textAlignment, backgroundColor,
  // backgroundOpacity, allowLabelClash.
  private void handleLegend() throws SAXException {
    for (int p = context.size() - 1; p >= 0; p--) {
      ElementDetails elem = (ElementDetails) context.elementAt(p);
      if (elem.name.equalsIgnoreCase("legend")) {
        if (currentLegend != null) {
          // an error because already in a legend tag
          String error =
            "legend element encountered inside of another legend element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentCgview == null) {
          // an error because no currentCgview
          String error =
            "legend element encountered outside of a cgview element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        }

        currentLegend = new Legend(currentCgview);

        // optional tags
        // fontColor
        if (elem.attributes.getValue("fontColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("fontColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegend.setFontColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("fontColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("fontColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentLegend.setFontColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'fontColor' attribute in legend element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'fontColor' attribute in legend element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // font
        if (elem.attributes.getValue("font") != null) {
          m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentLegend.setFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'font' attribute in legend element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'font' attribute in legend element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // position
        if (elem.attributes.getValue("position") != null) {
          if (
            LEGEND_POSITIONS.get(
              ((elem.attributes.getValue("position"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegend.setPosition(
              (
                (Integer) LEGEND_POSITIONS.get(
                  ((elem.attributes.getValue("position"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'position' attribute in legend element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // textAlignment
        if (elem.attributes.getValue("textAlignment") != null) {
          if (
            LEGEND_ALIGNMENTS.get(
              ((elem.attributes.getValue("textAlignment"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegend.setAlignment(
              (
                (Integer) LEGEND_ALIGNMENTS.get(
                  ((elem.attributes.getValue("textAlignment"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'textAlignment' attribute in legend element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // drawWhenZoomed
        if (elem.attributes.getValue("drawWhenZoomed") != null) {
          if (
            LEGEND_SHOW_ZOOM.get(
              ((elem.attributes.getValue("drawWhenZoomed"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegend.setPosition(
              (
                (Integer) LEGEND_POSITIONS.get(
                  ((elem.attributes.getValue("drawWhenZoomed"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'drawWhenZoomed' attribute in legend element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // backgroundOpacity
        if (elem.attributes.getValue("backgroundOpacity") != null) {
          float opacity;
          try {
            opacity =
              Float.parseFloat(elem.attributes.getValue("backgroundOpacity"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'backgroundOpacity' attribute in legend element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity > 1.0f) {
            String error =
              "value for 'backgroundOpacity' attribute in legend element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity < 0.0f) {
            String error =
              "value for 'backgroundOpacity' attribute in legend element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentLegend.setBackgroundOpacity(opacity);
        }

        // backgroundColor
        if (elem.attributes.getValue("backgroundColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("backgroundColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegend.setBackgroundColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("backgroundColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("backgroundColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentLegend.setBackgroundColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'backgroundColor' attribute in legend element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'backgroundColor' attribute in legend element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // allowLabelClash
        if (elem.attributes.getValue("allowLabelClash") != null) {
          if (
            BOOLEANS.get(
              ((elem.attributes.getValue("allowLabelClash"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegend.setAllowLabelClash(
              (
                (Boolean) BOOLEANS.get(
                  ((elem.attributes.getValue("allowLabelClash"))).toLowerCase()
                )
              ).booleanValue()
            );
          } else {
            String error =
              "value for 'allowLabelClash' attribute in legend element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        if (this.legendFontSize != -1) {
          if (currentLegend.getFont() != null) {
            currentLegend.setFont(
              new Font(
                currentLegend.getFont().getName(),
                currentLegend.getFont().getStyle(),
                this.legendFontSize
              )
            );
          } else {
            currentLegend.setFont(
              new Font(
                currentCgview.getLegendFont().getName(),
                currentCgview.getLegendFont().getStyle(),
                this.legendFontSize
              )
            );
          }
        }
      }
    }
  }

  /**
   * Handles the legendItem element and its attributes.
   *
   * @throws SAXException
   */
  // required attributes: text.
  // optional attributes: fontColor, swatchOpacity, drawSwatch, swatchColor, font, textAlignment.
  private void handleLegendItem() throws SAXException {
    for (int p = context.size() - 1; p >= 0; p--) {
      ElementDetails elem = (ElementDetails) context.elementAt(p);
      if (elem.name.equalsIgnoreCase("legendItem")) {
        if (currentLegend == null) {
          // an error because no current legend
          String error =
            "legendItem element encountered inside of legend element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentCgview == null) {
          // an error because no currentCgview
          String error =
            "legendItem element encountered outside of a cgview element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (currentLegendItem != null) {
          // an error because already inside legendItem tag
          String error =
            "legendItem element encountered inside of another legendItem element";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        } else if (elem.attributes.getValue("text") == null) {
          // an error because no length
          String error = "legendItem element is missing 'text' attribute";
          if (locator != null) {
            error =
              error +
              " in " +
              locator.getSystemId() +
              " at line " +
              locator.getLineNumber() +
              " column " +
              locator.getColumnNumber();
          }
          throw new SAXException(error);
        }

        currentLegendItem = new LegendItem(currentLegend);

        currentLegendItem.setLabel(elem.attributes.getValue("text"));

        // optional tags
        // swatchOpacity
        if (elem.attributes.getValue("swatchOpacity") != null) {
          float opacity;
          try {
            opacity =
              Float.parseFloat(elem.attributes.getValue("swatchOpacity"));
          } catch (NumberFormatException nfe) {
            String error =
              "value for 'swatchOpacity' attribute in legendItem element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity > 1.0f) {
            String error =
              "value for 'swatchOpacity' attribute in legendItem element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          if (opacity < 0.0f) {
            String error =
              "value for 'swatchOpacity' attribute in legendItem element must be between 0 and 1";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            throw new SAXException(error);
          }

          currentLegendItem.setSwatchOpacity(opacity);
        }
        // fontColor
        if (elem.attributes.getValue("fontColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("fontColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegendItem.setFontColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("fontColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("fontColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentLegendItem.setFontColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'fontColor' attribute in legendItem element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'fontColor' attribute in legendItem element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // swatchColor
        if (elem.attributes.getValue("swatchColor") != null) {
          if (
            COLORS.get(
              ((elem.attributes.getValue("swatchColor"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegendItem.setSwatchColor(
              (Color) COLORS.get(
                ((elem.attributes.getValue("swatchColor"))).toLowerCase()
              )
            );
          } else {
            m =
              colorDescriptionPattern.matcher(
                elem.attributes.getValue("swatchColor")
              );
            if (m.find()) {
              try {
                int r = Integer.parseInt(m.group(1));
                int g = Integer.parseInt(m.group(2));
                int b = Integer.parseInt(m.group(3));

                currentLegendItem.setSwatchColor(new Color(r, g, b));
              } catch (Exception e) {
                String error =
                  "value for 'swatchColor' attribute in legendItem element not understood";
                if (locator != null) {
                  error =
                    error +
                    " in " +
                    locator.getSystemId() +
                    " at line " +
                    locator.getLineNumber() +
                    " column " +
                    locator.getColumnNumber();
                }
                // throw new SAXException (error);
                System.err.println("[warning] " + error);
              }
            } else {
              String error =
                "value for 'swatchColor' attribute in legendItem element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          }
        }

        // drawSwatch
        if (elem.attributes.getValue("drawSwatch") != null) {
          if (
            SWATCH_TYPES.get(
              ((elem.attributes.getValue("drawSwatch"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegendItem.setDrawSwatch(
              (
                (Integer) SWATCH_TYPES.get(
                  ((elem.attributes.getValue("drawSwatch"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'drawSwatch' attribute in legendItem element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // textAlignment
        if (elem.attributes.getValue("textAlignment") != null) {
          if (
            LEGEND_ALIGNMENTS.get(
              ((elem.attributes.getValue("textAlignment"))).toLowerCase()
            ) !=
            null
          ) {
            currentLegendItem.setTextAlignment(
              (
                (Integer) LEGEND_ALIGNMENTS.get(
                  ((elem.attributes.getValue("textAlignment"))).toLowerCase()
                )
              ).intValue()
            );
          } else {
            String error =
              "value for 'textAlignment' attribute in legendItem element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        // font
        if (elem.attributes.getValue("font") != null) {
          m = fontDescriptionPattern.matcher(elem.attributes.getValue("font"));
          if (m.find()) {
            try {
              String name = m.group(1);
              String style = m.group(2);
              int size = Integer.parseInt(m.group(3));
              int intStyle = Font.PLAIN;

              if (style.equalsIgnoreCase("bold")) {
                intStyle = Font.BOLD;
              } else if (
                (style.equalsIgnoreCase("italic")) ||
                (style.equalsIgnoreCase("italics"))
              ) {
                intStyle = Font.ITALIC;
              } else if (
                (style.equalsIgnoreCase("bold-italic")) ||
                (style.equalsIgnoreCase("italic-bold"))
              ) {
                intStyle = Font.ITALIC + Font.BOLD;
              }
              currentLegendItem.setFont(new Font(name, intStyle, size));
            } catch (Exception e) {
              String error =
                "value for 'font' attribute in legendItem element not understood";
              if (locator != null) {
                error =
                  error +
                  " in " +
                  locator.getSystemId() +
                  " at line " +
                  locator.getLineNumber() +
                  " column " +
                  locator.getColumnNumber();
              }
              // throw new SAXException (error);
              System.err.println("[warning] " + error);
            }
          } else {
            String error =
              "value for 'font' attribute in legendItem element not understood";
            if (locator != null) {
              error =
                error +
                " in " +
                locator.getSystemId() +
                " at line " +
                locator.getLineNumber() +
                " column " +
                locator.getColumnNumber();
            }
            // throw new SAXException (error);
            System.err.println("[warning] " + error);
          }
        }

        if (this.legendFontSize != -1) {
          if (currentLegendItem.getFont() != null) {
            currentLegendItem.setFont(
              new Font(
                currentLegendItem.getFont().getName(),
                currentLegendItem.getFont().getStyle(),
                this.legendFontSize
              )
            );
          } else if (currentLegend.getFont() != null) {
            currentLegendItem.setFont(
              new Font(
                currentLegend.getFont().getName(),
                currentLegend.getFont().getStyle(),
                this.legendFontSize
              )
            );
          } else {
            currentLegendItem.setFont(
              new Font(
                currentCgview.getLegendFont().getName(),
                currentCgview.getLegendFont().getStyle(),
                this.legendFontSize
              )
            );
          }
        }
      }
    }
  }

  private class ElementDetails {
    public String name;
    public Attributes attributes;

    public ElementDetails(String name, Attributes atts) {
      this.name = name;
      this.attributes = new AttributesImpl(atts);
    }
  }
}
