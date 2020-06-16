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
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.*;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

// note you can only call the draw methods once after the features are added. If you want to call
// the draw methods
// more than once you will need to add a method to store the features, and then add code to the
// prepareToDraw
// method, to draw the features. Currently features are drawn directly from the addFeature method.

public class Plasmid implements CgviewConstants {
  private Cgview p;
  private Legend legend;

  private FeatureSlot forwardSlot0;
  private FeatureSlot forwardSlot1;
  private FeatureSlot forwardSlot2;
  private FeatureSlot forwardSlot3;
  private FeatureSlot forwardSlot4;

  private FeatureSlot forwardSlot5;
  private FeatureSlot forwardSlot6;
  private FeatureSlot forwardSlot7;

  private FeatureSlot restrictionSlot;

  private FeatureSlot reverseSlot0;
  private FeatureSlot reverseSlot1;
  private FeatureSlot reverseSlot2;
  private FeatureSlot reverseSlot3;
  private FeatureSlot reverseSlot4;

  private FeatureSlot reverseSlot5;
  private FeatureSlot reverseSlot6;
  private FeatureSlot reverseSlot7;

  // static
  static final int NO_DIRECTION = 0;
  static final int FORWARD = 1;
  static final int REVERSE = 2;

  static final int REGULAR = 1;
  static final int INVERSE = 0;

  private Hashtable COLORS;

  private Hashtable MAP_ITEM_COLORS;
  private Hashtable MAP_ITEM_COLORS_INVERSE;

  private Hashtable FEATURE_COLORS;
  private Hashtable FEATURE_COLORS_INVERSE;

  private Hashtable FEATURE_DECORATIONS_DIRECT;
  private Hashtable FEATURE_DECORATIONS_REVERSE;

  private Hashtable FEATURE_CATEGORIES;

  private Hashtable FEATURE_THICKNESSES;
  private Hashtable BACKBONE_THICKNESSES;

  private Hashtable DRAW_LEGEND_ITEMS;
  private Hashtable LEGEND_ITEM_NAMES;

  // use these to adjust appearance of figure
  private float opacity = 1.0f;
  private int imageWidth = 800;
  private int imageHeight = 800;
  private int size;
  private Font labelFont = new Font("SansSerif", Font.PLAIN, 12);
  private Font titleFont = new Font("SansSerif", Font.PLAIN, 13);
  private Font legendFont = new Font("SansSerif", Font.PLAIN, 12);
  private Font rulerFont = new Font("SansSerif", Font.PLAIN, 8);
  private Font messageFont = new Font("SansSerif", Font.PLAIN, 13);
  private float featureThickness = 8.0f;
  private float backboneThickness = 8.0f;
  private float featureSpacing = 2.0f;
  private float tickLength = 7.0f;
  private double labelLineLength = 40.0d;
  private boolean useColoredLabelBackground = false;
  private boolean showTitle = true;
  private boolean showShading = true;
  private boolean showPositions = true;
  private boolean showBorder = true;
  private boolean allowLabelClashLegend = false;
  private float shadingProportion = 0.4f;
  private int useInnerLabels = INNER_LABELS_SHOW;
  private String message = "";
  private boolean moveInnerLabelsToOuter = true;
  private int colorScheme = REGULAR; // or INVERSE
  private String title = "";
  private int labelPlacementQuality = 9;
  private int legendPosition = LEGEND_UPPER_RIGHT;
  private boolean showMessage = true;
  private boolean showLabels = true;
  private boolean showLegend = true;
  private boolean useArrows = true;
  private boolean useColoredLabels = true;
  private boolean drawTickMarks = true;
  private boolean addCategoryInfo = false;

  private int MAXLABELS = 400;

  /**
   * Create a new Plasmid. Note that a constructor accepting a title and length is also available.
   *
   * @param size the size of the plasmid in base pairs.
   */
  public Plasmid(int size) {
    this.size = size;

    COLORS = new Hashtable();

    MAP_ITEM_COLORS = new Hashtable();
    MAP_ITEM_COLORS_INVERSE = new Hashtable();

    FEATURE_COLORS = new Hashtable();
    FEATURE_COLORS_INVERSE = new Hashtable();

    FEATURE_DECORATIONS_DIRECT = new Hashtable();
    FEATURE_DECORATIONS_REVERSE = new Hashtable();

    FEATURE_CATEGORIES = new Hashtable();

    FEATURE_THICKNESSES = new Hashtable();
    BACKBONE_THICKNESSES = new Hashtable();

    DRAW_LEGEND_ITEMS = new Hashtable();
    LEGEND_ITEM_NAMES = new Hashtable();

    COLORS.put("black", new Color(0, 0, 0)); // tickmark defaults //rulerFontColor default //titleFontColor default
    COLORS.put("silver", new Color(192, 192, 192));
    COLORS.put("gray", new Color(128, 128, 128)); // backbone default //partial tickmark default //zeroline default
    COLORS.put("white", new Color(255, 255, 255)); // background default
    COLORS.put("maroon", new Color(128, 0, 0));
    COLORS.put("red", new Color(255, 0, 0));
    COLORS.put("pink", new Color(255, 153, 204));
    COLORS.put("purple", new Color(128, 0, 128));
    COLORS.put("fuchsia", new Color(255, 0, 255));
    COLORS.put("orange", new Color(255, 153, 0));
    COLORS.put("green", new Color(0, 128, 0));
    COLORS.put("spring", new Color(204, 255, 204));
    COLORS.put("lime", new Color(0, 255, 0));
    COLORS.put("olive", new Color(128, 128, 0));
    COLORS.put("yellow", new Color(255, 255, 0));
    COLORS.put("navy", new Color(0, 0, 128));
    COLORS.put("blue", new Color(0, 0, 255));
    COLORS.put("azure", new Color(51, 153, 255));
    COLORS.put("lightBlue", new Color(102, 204, 255));
    COLORS.put("teal", new Color(153, 255, 204));
    COLORS.put("aqua", new Color(0, 255, 255));

    MAP_ITEM_COLORS.put("tick", COLORS.get("black"));
    MAP_ITEM_COLORS.put("rulerFont", COLORS.get("black"));
    MAP_ITEM_COLORS.put("titleFont", COLORS.get("black"));
    MAP_ITEM_COLORS.put("messageFont", COLORS.get("black"));
    MAP_ITEM_COLORS.put("backbone", COLORS.get("gray"));
    MAP_ITEM_COLORS.put("partialTick", COLORS.get("gray"));
    MAP_ITEM_COLORS.put("zeroLine", COLORS.get("gray"));
    MAP_ITEM_COLORS.put("background", COLORS.get("white"));

    MAP_ITEM_COLORS_INVERSE.put("tick", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("rulerFont", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("titleFont", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("messageFont", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("backbone", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("partialTick", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("zeroLine", COLORS.get("white"));
    MAP_ITEM_COLORS_INVERSE.put("background", COLORS.get("black"));

    FEATURE_COLORS.put("origin_of_replication", COLORS.get("black"));
    FEATURE_COLORS.put("promoter", COLORS.get("green"));
    FEATURE_COLORS.put("terminator", COLORS.get("maroon"));
    FEATURE_COLORS.put("selectable_marker", COLORS.get("orange"));
    FEATURE_COLORS.put("regulatory_sequence", COLORS.get("olive"));
    FEATURE_COLORS.put("tag", COLORS.get("silver"));
    FEATURE_COLORS.put("other_gene", COLORS.get("fuchsia"));
    FEATURE_COLORS.put("reporter_gene", COLORS.get("purple"));
    FEATURE_COLORS.put("unique_restriction_site", COLORS.get("blue"));
    FEATURE_COLORS.put("restriction_site", COLORS.get("red"));
    FEATURE_COLORS.put("open_reading_frame", COLORS.get("pink"));

    FEATURE_COLORS_INVERSE.put("origin_of_replication", COLORS.get("white"));
    FEATURE_COLORS_INVERSE.put("promoter", COLORS.get("lime"));
    FEATURE_COLORS_INVERSE.put("terminator", COLORS.get("yellow"));
    FEATURE_COLORS_INVERSE.put("selectable_marker", COLORS.get("orange"));
    FEATURE_COLORS_INVERSE.put("regulatory_sequence", COLORS.get("azure"));
    FEATURE_COLORS_INVERSE.put("tag", COLORS.get("silver"));
    FEATURE_COLORS_INVERSE.put("other_gene", COLORS.get("fuchsia"));
    FEATURE_COLORS_INVERSE.put("reporter_gene", COLORS.get("teal"));
    FEATURE_COLORS_INVERSE.put(
      "unique_restriction_site",
      COLORS.get("lightBlue")
    );
    FEATURE_COLORS_INVERSE.put("restriction_site", COLORS.get("pink"));
    FEATURE_COLORS_INVERSE.put("open_reading_frame", COLORS.get("red"));

    FEATURE_DECORATIONS_DIRECT.put(
      "origin_of_replication",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "promoter",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "terminator",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "selectable_marker",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "regulatory_sequence",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put("tag", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put(
      "other_gene",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "reporter_gene",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "unique_restriction_site",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "restriction_site",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "open_reading_frame",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );

    FEATURE_DECORATIONS_REVERSE.put(
      "origin_of_replication",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "promoter",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "terminator",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "selectable_marker",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "regulatory_sequence",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put("tag", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put(
      "other_gene",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "reporter_gene",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "unique_restriction_site",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "restriction_site",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "open_reading_frame",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );

    FEATURE_CATEGORIES.put("origin_of_replication", new String(" origin"));
    FEATURE_CATEGORIES.put("promoter", new String(" prom"));
    FEATURE_CATEGORIES.put("terminator", new String(" term"));
    FEATURE_CATEGORIES.put("selectable_marker", new String(" marker"));
    FEATURE_CATEGORIES.put("regulatory_sequence", new String(" reg"));
    FEATURE_CATEGORIES.put("tag", new String(" tag"));
    FEATURE_CATEGORIES.put("other_gene", new String(" gene"));
    FEATURE_CATEGORIES.put("reporter_gene", new String(" gene"));
    FEATURE_CATEGORIES.put("unique_restriction_site", new String(""));
    FEATURE_CATEGORIES.put("restriction_site", new String(""));
    FEATURE_CATEGORIES.put("open_reading_frame", new String(""));

    LEGEND_ITEM_NAMES.put("origin_of_replication", "Origin of replication");
    LEGEND_ITEM_NAMES.put("promoter", "Promoter");
    LEGEND_ITEM_NAMES.put("terminator", "Terminator");
    LEGEND_ITEM_NAMES.put("selectable_marker", "Selectable marker");
    LEGEND_ITEM_NAMES.put("regulatory_sequence", "Regulatory sequence");
    LEGEND_ITEM_NAMES.put("tag", "Tag");
    LEGEND_ITEM_NAMES.put("other_gene", "Other gene");
    LEGEND_ITEM_NAMES.put("reporter_gene", "Reporter gene");
    LEGEND_ITEM_NAMES.put("unique_restriction_site", "Unique restriction site");
    LEGEND_ITEM_NAMES.put("restriction_site", "Restriction site");
    LEGEND_ITEM_NAMES.put("open_reading_frame", "Open reading frame");

    FEATURE_THICKNESSES.put("xxx-small", new Float(4.0f));
    FEATURE_THICKNESSES.put("xx-small", new Float(5.0f));
    FEATURE_THICKNESSES.put("x-small", new Float(6.0f));
    FEATURE_THICKNESSES.put("small", new Float(7.0f));
    FEATURE_THICKNESSES.put("medium", new Float(8.0f)); // default for featureThickness
    FEATURE_THICKNESSES.put("large", new Float(9.0f));
    FEATURE_THICKNESSES.put("x-large", new Float(10.0f));
    FEATURE_THICKNESSES.put("xx-large", new Float(11.0f));
    FEATURE_THICKNESSES.put("xxx-large", new Float(12.0f));

    BACKBONE_THICKNESSES.put("xxx-small", new Float(4.0f));
    BACKBONE_THICKNESSES.put("xx-small", new Float(5.0f));
    BACKBONE_THICKNESSES.put("x-small", new Float(6.0f));
    BACKBONE_THICKNESSES.put("small", new Float(7.0f));
    BACKBONE_THICKNESSES.put("medium", new Float(8.0f)); // default for backboneThickness
    BACKBONE_THICKNESSES.put("large", new Float(9.0f));
    BACKBONE_THICKNESSES.put("x-large", new Float(10.0f));
    BACKBONE_THICKNESSES.put("xx-large", new Float(11.0f));
    BACKBONE_THICKNESSES.put("xxx-large", new Float(12.0f));

    p = new Cgview(size);
  }

  /**
   * Create a new Plasmid. Note that supplying a title does not ensure that the title will be shown.
   * Use the setShowTitle() method to force the title to be drawn.
   *
   * @param title the title of the plasmid.
   * @param size the size of the plasmid in base pairs.
   */
  public Plasmid(String title, int size) {
    this(size);
    this.title = title;
  }

  /**
   * Add a feature to this map. Note that the start of the feature should be a smaller number than
   * the stop of the feature, regardless of the strand. The only case where start is larger than the
   * stop is when the feature runs across the start/stop boundary, for example 6899-10 on a 7000bp
   * plasmid.
   *
   * @param type one of the following: origin_of_replication, promoter, terminator,
   *     selectable_marker, regulatory_sequence, tag, other_gene, reporter_gene,
   *     unique_restriction_site, restriction_site.
   * @param name the name of the feature, such as EcoRI.
   * @param start the start position of the feature. Must be between 1 and the length of the
   *     plasmid.
   * @param stop the end position of the feature. Must be between 1 and the length of the plasmid.
   * @param strand the strand of the feature. Can be Plasmid.FORWARD, Plasmid.REVERSE, or
   *     Plasmid.NO_DIRECTION.
   */
  public void addFeature(
    String type,
    String name,
    int start,
    int stop,
    int strand
  ) {
    // add the feature to the plasmid.
    int decoration;
    int label;
    Color color;
    // String slot;

    if (start > size) {
      start = size;
    }
    if (start < 1) {
      start = 1;
    }

    if (stop > size) {
      stop = size;
    }
    if (stop < 1) {
      stop = 1;
    }

    try {
      color = getFeatureColor(type);
      decoration = getFeatureDecoration(type, strand);
      label = getLabelType();
      addItemToLegend(type, strand);
    } catch (NullPointerException e) {
      color = new Color(0, 0, 128); // navy
      if (colorScheme == REGULAR) {
        color = new Color(0, 0, 128); // navy
      } else if (colorScheme == INVERSE) {
        color = new Color(0, 128, 128); // teal
      }
      decoration = DECORATION_STANDARD;
      label = LABEL;
    }

    // create a feature and a feature range
    // then figure out which feature slot to put the feature in.
    Feature feature = new Feature(showShading);
    FeatureRange featureRange = new FeatureRange(feature, start, stop);
    featureRange.setDecoration(decoration);
    featureRange.setColor(color);
    featureRange.setOpacity(opacity);
    featureRange.setShowLabel(label);
    if (
      (showPositions) &&
      (
        (type.equalsIgnoreCase("restriction_site")) ||
        (type.equalsIgnoreCase("unique_restriction_site"))
      )
    ) {
      featureRange.setLabel(name + " " + start);
    } else {
      if (addCategoryInfo) {
        try {
          featureRange.setLabel(name + (String) FEATURE_CATEGORIES.get(type));
        } catch (NullPointerException e) {
          featureRange.setLabel(name);
        }
      } else {
        featureRange.setLabel(name);
      }
    }

    if (
      (type.equalsIgnoreCase("restriction_site")) ||
      (type.equalsIgnoreCase("unique_restriction_site"))
    ) {
      if (restrictionSlot == null) {
        restrictionSlot = new FeatureSlot(DIRECT_STRAND, showShading);
        restrictionSlot.setFeatureThickness(1f);
      }
      feature.setFeatureSlot(restrictionSlot);
    } else if ((strand == NO_DIRECTION) || (strand == FORWARD)) {
      if (forwardSlot0 == null) {
        forwardSlot0 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot0);
      } else if (forwardSlot0.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot0);
      } else if (forwardSlot1 == null) {
        forwardSlot1 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot1);
      } else if (forwardSlot1.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot1);
      } else if (forwardSlot2 == null) {
        forwardSlot2 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot2);
      } else if (forwardSlot2.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot2);
      } else if (forwardSlot3 == null) {
        forwardSlot3 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot3);
      } else if (forwardSlot3.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot3);
      } else if (forwardSlot4 == null) {
        forwardSlot4 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot4);
      } else if (forwardSlot4.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot4);
      } else if (forwardSlot5 == null) {
        forwardSlot5 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot5);
      } else if (forwardSlot5.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot5);
      } else if (forwardSlot6 == null) {
        forwardSlot6 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot6);
      } else if (forwardSlot6.isRoom(feature)) {
        feature.setFeatureSlot(forwardSlot6);
      } else if (forwardSlot7 == null) {
        forwardSlot7 = new FeatureSlot(DIRECT_STRAND, showShading);
        feature.setFeatureSlot(forwardSlot7);
      } else {
        feature.setFeatureSlot(forwardSlot7);
      }
    } else if (strand == REVERSE) {
      if (reverseSlot0 == null) {
        reverseSlot0 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot0);
      } else if (reverseSlot0.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot0);
      } else if (reverseSlot1 == null) {
        reverseSlot1 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot1);
      } else if (reverseSlot1.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot1);
      } else if (reverseSlot2 == null) {
        reverseSlot2 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot2);
      } else if (reverseSlot2.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot2);
      } else if (reverseSlot3 == null) {
        reverseSlot3 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot3);
      } else if (reverseSlot3.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot3);
      } else if (reverseSlot4 == null) {
        reverseSlot4 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot4);
      } else if (reverseSlot4.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot4);
      } else if (reverseSlot5 == null) {
        reverseSlot5 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot5);
      } else if (reverseSlot5.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot5);
      } else if (reverseSlot6 == null) {
        reverseSlot6 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot6);
      } else if (reverseSlot6.isRoom(feature)) {
        feature.setFeatureSlot(reverseSlot6);
      } else if (reverseSlot7 == null) {
        reverseSlot7 = new FeatureSlot(REVERSE_STRAND, showShading);
        feature.setFeatureSlot(reverseSlot7);
      } else {
        feature.setFeatureSlot(reverseSlot7);
      }
    }
  }

  private Color getFeatureColor(String type) throws NullPointerException {
    Color colorToReturn = (Color) FEATURE_COLORS.get(type);

    if (colorScheme == REGULAR) {
      colorToReturn = (Color) FEATURE_COLORS.get(type);
    } else if (colorScheme == INVERSE) {
      colorToReturn = (Color) FEATURE_COLORS_INVERSE.get(type);
    }
    return colorToReturn;
  }

  private int getFeatureDecoration(String type, int strand)
    throws NullPointerException {
    int decoration = DECORATION_STANDARD;
    if (!(useArrows)) {
      decoration = DECORATION_STANDARD;
    } else {
      if (strand == FORWARD) {
        decoration =
          ((Integer) FEATURE_DECORATIONS_DIRECT.get(type)).intValue();
      } else if (strand == NO_DIRECTION) {
        decoration =
          ((Integer) FEATURE_DECORATIONS_DIRECT.get(type)).intValue();
      } else {
        decoration =
          ((Integer) FEATURE_DECORATIONS_REVERSE.get(type)).intValue();
      }
    }

    return decoration;
  }

  private int getLabelType() throws NullPointerException {
    int labelType = LABEL;
    if (showLabels) {
      labelType = LABEL;
    } else {
      labelType = LABEL_NONE;
    }
    return labelType;
  }

  private void addItemToLegend(String type, int strand)
    throws NullPointerException {
    if (
      (strand == NO_DIRECTION) || (strand == FORWARD) || (strand == REVERSE)
    ) {
      DRAW_LEGEND_ITEMS.put(type, new Boolean(true));
    }
  }

  /**
   * Sets the title of of the map. This title is drawn in the center of the backbone circle. Use
   * setShowTitle() to set whether the title is shown.
   *
   * @param title the title of the map.
   */
  public void setTitle(String title) {
    this.title = title.trim();
  }

  /**
   * Sets the map message. This message is drawn in the lower right of the map. Use setShowMessage()
   * to set whether the message is shown.
   *
   * @param message the map message.
   */
  public void setMessage(String message) {
    this.message = message.trim();
  }

  /**
   * Sets whether a legend is drawn on this map.
   *
   * @param show whether a legend is drawn on this map.
   */
  public void setShowLegend(boolean show) {
    showLegend = show;
  }

  /**
   * Sets whether a title is drawn on this map. Use the setTitle method to specify the title, or use
   * the Plasmid constructor that accepts a title.
   *
   * @param show whether to draw a title on this map.
   */
  public void setShowTitle(boolean show) {
    showTitle = show;
  }

  /**
   * Sets whether a message is drawn on this map. Use the setMessage method to specify the message.
   *
   * @param show whether to draw a title on this map.
   */
  public void setShowMessage(boolean show) {
    showMessage = show;
  }

  /**
   * Sets whether labels are drawn on this map.
   *
   * @param show whether labels are drawn on this map.
   */
  public void setShowLabels(boolean show) {
    showLabels = show;
  }

  /**
   * Sets whether a category is added to certain labels to provide more information. For example, a
   * label like "T7" might become "T7 prom" if this is set to true.
   *
   * @param show whether extra info is added to certain labels to provide more information.
   */
  public void setAddCategoryInfo(boolean show) {
    addCategoryInfo = show;
  }

  /**
   * Sets whether arrows are drawn on this map (true) or just arcs (false).
   *
   * @param show whether labels are drawn in color, or just black and white.
   */
  public void setUseArrows(boolean show) {
    useArrows = show;
  }

  /**
   * Sets whether labels are drawn using color (true), or just black and white (false).
   *
   * @param show whether labels are drawn in color, or just black and white.
   */
  public void setUseColoredLabels(boolean show) {
    useColoredLabels = show;
  }

  /**
   * Sets whether labels are drawn with a colored box surrounding them.
   *
   * @param show whether labels are drawn with a colored box surrounding them.
   */
  public void setUseColoredLabelBackground(boolean show) {
    useColoredLabelBackground = show;
  }

  /**
   * Sets whether the overall color scheme. Plasmid.REGULAR is a white background with dark labels,
   * while Plasmid.INVERSE is a black background with light labels.
   *
   * @param colorScheme Plasmid.REGULAR or Plasmid.INVERSE.
   */
  public void setColorScheme(int colorScheme) {
    if (colorScheme == INVERSE) {
      this.colorScheme = INVERSE;
    } else {
      this.colorScheme = REGULAR;
    }
  }

  /**
   * Sets whether tick marks are drawn.
   *
   * @param draw whether tick marks are drawn.
   */
  public void setDrawTickMarks(boolean draw) {
    drawTickMarks = draw;
  }

  /**
   * Sets whether items on the map are drawn with shading.
   *
   * @param showShading whether items on the map are draw with shading.
   */
  public void setShowShading(boolean showShading) {
    this.showShading = showShading;
  }

  /**
   * Sets whether this map is drawn with a border.
   *
   * @param showBorder whether this map is surrounded with a border.
   */
  public void setShowBorder(boolean showBorder) {
    this.showBorder = showBorder;
  }

  /**
   * Sets the legend position.
   *
   * @param position the position: "upper_right", "upper_left", or "none". Specifying "none" calls
   *     the showLegend() method automatically.
   */
  public void setLegendPosition(String position) {
    this.legendPosition = LEGEND_UPPER_RIGHT;

    if (position.equalsIgnoreCase("upper_right")) {
      this.legendPosition = LEGEND_UPPER_RIGHT;
    } else if (position.equalsIgnoreCase("upper_left")) {
      this.legendPosition = LEGEND_UPPER_LEFT;
    } else if (position.equalsIgnoreCase("none")) {
      this.setShowLegend(false);
    }
  }

  /**
   * Sets whether labels are allowed to be drawn on the inside of the plasmid.
   *
   * @param useInnerLabels whether labels are allowed to be drawn on the inside of the plasmid.
   */
  public void setShowInnerLabels(boolean useInnerLabels) {
    if (useInnerLabels) {
      this.useInnerLabels = INNER_LABELS_SHOW;
    } else {
      this.useInnerLabels = INNER_LABELS_NO_SHOW;
    }
  }

  /**
   * Sets the thickness of the arc used to draw the backbone.
   *
   * @param width "xxx-small", "xx-small", "x-small", "small", "medium", "large", "x-large",
   *     "xx-large", "xxx-large".
   */
  public void setBackboneThickness(String width) {
    try {
      backboneThickness =
        ((Float) BACKBONE_THICKNESSES.get(width)).floatValue();
    } catch (NullPointerException e) {
      backboneThickness =
        ((Float) BACKBONE_THICKNESSES.get("medium")).floatValue();
    }
  }

  /**
   * Sets the thickness of the arc used to draw features.
   *
   * @param width "xxx-small", "xx-small", "x-small", "small", "medium", "large", "x-large",
   *     "xx-large", "xxx-large".
   */
  public void setFeatureThickness(String width) {
    try {
      featureThickness = ((Float) FEATURE_THICKNESSES.get(width)).floatValue();
    } catch (NullPointerException e) {
      featureThickness =
        ((Float) FEATURE_THICKNESSES.get("medium")).floatValue();
    }
  }

  /**
   * Sets the width of this map.
   *
   * @param width the width of the map.
   */
  public void setImageWidth(int width) {
    if (width < 0) {
      width = 0;
    }
    this.imageWidth = width;
  }

  /**
   * Returns the width of this map.
   *
   * @return the width of the map.
   */
  public int getImageWidth() {
    return imageWidth;
  }

  /**
   * Sets the height of this map.
   *
   * @param height the height of the map.
   */
  public void setImageHeight(int height) {
    if (height < 0) {
      height = 0;
    }
    this.imageHeight = height;
  }

  /**
   * Returns the height of this map.
   *
   * @return the height of the map.
   */
  public int getImageHeight() {
    return imageHeight;
  }

  private void prepareToDraw() {
    if (forwardSlot0 != null) {
      forwardSlot0.setCgview(p);
      forwardSlot0.setFeatureThickness(featureThickness);
    }
    if (forwardSlot1 != null) {
      forwardSlot1.setCgview(p);
      forwardSlot1.setFeatureThickness(featureThickness);
    }
    if (forwardSlot2 != null) {
      forwardSlot2.setCgview(p);
      forwardSlot2.setFeatureThickness(featureThickness);
    }
    if (forwardSlot3 != null) {
      forwardSlot3.setCgview(p);
      forwardSlot3.setFeatureThickness(featureThickness);
    }
    if (forwardSlot4 != null) {
      forwardSlot4.setCgview(p);
      forwardSlot4.setFeatureThickness(featureThickness);
    }
    if (forwardSlot5 != null) {
      forwardSlot5.setCgview(p);
      forwardSlot5.setFeatureThickness(featureThickness);
    }
    if (forwardSlot6 != null) {
      forwardSlot6.setCgview(p);
      forwardSlot6.setFeatureThickness(featureThickness);
    }
    if (forwardSlot7 != null) {
      forwardSlot7.setCgview(p);
      forwardSlot7.setFeatureThickness(featureThickness);
    }

    if (reverseSlot0 != null) {
      reverseSlot0.setCgview(p);
      reverseSlot0.setFeatureThickness(featureThickness);
    }
    if (reverseSlot1 != null) {
      reverseSlot1.setCgview(p);
      reverseSlot1.setFeatureThickness(featureThickness);
    }
    if (reverseSlot2 != null) {
      reverseSlot2.setCgview(p);
      reverseSlot2.setFeatureThickness(featureThickness);
    }
    if (reverseSlot3 != null) {
      reverseSlot3.setCgview(p);
      reverseSlot3.setFeatureThickness(featureThickness);
    }
    if (reverseSlot4 != null) {
      reverseSlot4.setCgview(p);
      reverseSlot4.setFeatureThickness(featureThickness);
    }
    if (reverseSlot5 != null) {
      reverseSlot5.setCgview(p);
      reverseSlot5.setFeatureThickness(featureThickness);
    }
    if (reverseSlot6 != null) {
      reverseSlot6.setCgview(p);
      reverseSlot6.setFeatureThickness(featureThickness);
    }
    if (reverseSlot7 != null) {
      reverseSlot7.setCgview(p);
      reverseSlot7.setFeatureThickness(featureThickness);
    }

    if (restrictionSlot != null) {
      restrictionSlot.setCgview(p);
      restrictionSlot.setFeatureThickness(featureThickness);
    }

    // send settings to p
    if (showTitle) {
      p.setTitle(title);
    }
    p.setWidth(imageWidth);
    p.setHeight(imageHeight);
    p.setLabelsToKeep(MAXLABELS);
    p.setDrawTickMarks(drawTickMarks);
    p.setTitleFont(titleFont);
    p.setLabelFont(labelFont);
    p.setFeatureThickness(featureThickness);
    p.setBackboneThickness(backboneThickness);
    p.setFeatureSlotSpacing(featureSpacing);
    p.setLegendFont(legendFont);
    p.setTickLength(tickLength);
    p.setLabelLineLength(labelLineLength);
    p.setLabelPlacementQuality(labelPlacementQuality);
    p.setUseColoredLabelBackgrounds(useColoredLabelBackground);
    p.setShowBorder(showBorder);
    p.setShowShading(showShading);
    p.setShadingProportion(shadingProportion);
    p.setUseInnerLabels(useInnerLabels);
    p.setMoveInnerLabelsToOuter(moveInnerLabelsToOuter);
    p.setWarningFont(rulerFont);
    p.setRulerFont(rulerFont);

    // if not drawing labels, don't show message.
    if (!(showLabels)) {
      p.setShowWarning(false);
    }

    // set backboneRadius based on smallest image dimension
    int smallestDimension = Math.min(imageWidth, imageHeight);
    if (smallestDimension <= 750) {
      p.setBackboneRadius(0.50d * (double) smallestDimension / 2.0d);
    } else {
      p.setBackboneRadius(0.50d * 750.0d / 2.0d);
    }

    // check coloredLabels
    if (!(useColoredLabels)) {
      if (colorScheme == REGULAR) {
        p.setGlobalLabelColor((Color) MAP_ITEM_COLORS.get("titleFont"));
      } else if (colorScheme == INVERSE) {
        p.setGlobalLabelColor((Color) MAP_ITEM_COLORS_INVERSE.get("titleFont"));
      }
    }

    // set map item colors
    if (colorScheme == REGULAR) {
      p.setLongTickColor((Color) MAP_ITEM_COLORS.get("tick"));
      p.setShortTickColor((Color) MAP_ITEM_COLORS.get("partialTick"));
      p.setZeroTickColor((Color) MAP_ITEM_COLORS.get("zeroLine"));
      p.setRulerFontColor((Color) MAP_ITEM_COLORS.get("rulerFont"));
      p.setBackboneColor((Color) MAP_ITEM_COLORS.get("backbone"));
      p.setTitleFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));
      p.setWarningFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));
      p.setBackgroundColor((Color) MAP_ITEM_COLORS.get("background"));
    } else if (colorScheme == INVERSE) {
      p.setLongTickColor((Color) MAP_ITEM_COLORS_INVERSE.get("tick"));
      p.setShortTickColor((Color) MAP_ITEM_COLORS_INVERSE.get("partialTick"));
      p.setZeroTickColor((Color) MAP_ITEM_COLORS_INVERSE.get("zeroLine"));
      p.setRulerFontColor((Color) MAP_ITEM_COLORS_INVERSE.get("rulerFont"));
      p.setBackboneColor((Color) MAP_ITEM_COLORS_INVERSE.get("backbone"));
      p.setTitleFontColor((Color) MAP_ITEM_COLORS_INVERSE.get("titleFont"));
      p.setWarningFontColor((Color) MAP_ITEM_COLORS_INVERSE.get("titleFont"));
      p.setBackgroundColor((Color) MAP_ITEM_COLORS_INVERSE.get("background"));
    }

    // build legend
    if ((showLegend) && (DRAW_LEGEND_ITEMS.size() > 0)) {
      // create legend
      legend = new Legend(p);
      legend.setAllowLabelClash(allowLabelClashLegend);
      legend.setBackgroundOpacity(0.5f);
      legend.setFont(legendFont);
      legend.setPosition(legendPosition);
      if (colorScheme == REGULAR) {
        legend.setBackgroundColor((Color) MAP_ITEM_COLORS.get("background"));
        legend.setFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));
      } else if (colorScheme == INVERSE) {
        legend.setBackgroundColor(
          (Color) MAP_ITEM_COLORS_INVERSE.get("background")
        );
        legend.setFontColor((Color) MAP_ITEM_COLORS_INVERSE.get("titleFont"));
      }

      LegendItem legendItem;

      Enumeration legendEntries = DRAW_LEGEND_ITEMS.keys();
      ArrayList list = new ArrayList();
      while (legendEntries.hasMoreElements()) {
        list.add(legendEntries.nextElement());
      }
      Collections.sort(list);
      Iterator i = list.iterator();

      while (i.hasNext()) {
        String key = (String) i.next();
        legendItem = new LegendItem(legend);
        legendItem.setDrawSwatch(SWATCH_SHOW);
        legendItem.setLabel((String) LEGEND_ITEM_NAMES.get(key));
        if (colorScheme == REGULAR) {
          legendItem.setSwatchColor((Color) FEATURE_COLORS.get(key));
        } else if (colorScheme == INVERSE) {
          legendItem.setSwatchColor((Color) FEATURE_COLORS_INVERSE.get(key));
        }
      }
    }

    // set message
    if (showMessage) {
      legend = new Legend(p);
      legend.setAllowLabelClash(false);
      legend.setBackgroundOpacity(0.5f);
      legend.setFont(messageFont);
      legend.setPosition(LEGEND_LOWER_RIGHT);
      LegendItem legendItem;

      if (colorScheme == REGULAR) {
        legend.setBackgroundColor((Color) MAP_ITEM_COLORS.get("background"));
        legend.setFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));
        legendItem = new LegendItem(legend);
        legendItem.setLabel(message);
        legendItem.setDrawSwatch(SWATCH_NO_SHOW);
      } else if (colorScheme == INVERSE) {
        legend.setBackgroundColor(
          (Color) MAP_ITEM_COLORS_INVERSE.get("background")
        );
        legend.setFontColor((Color) MAP_ITEM_COLORS_INVERSE.get("titleFont"));
        legendItem = new LegendItem(legend);
        legendItem.setLabel(message);
        legendItem.setDrawSwatch(SWATCH_NO_SHOW);
      }
    }
  }

  /**
   * Writes this map to a PNG file.
   *
   * @param filename the path and name of the file to create.
   */
  public void writeToPNGFile(String filename) throws IOException {
    this.prepareToDraw();

    BufferedImage buffImage = new BufferedImage(
      p.getWidth(),
      p.getHeight(),
      BufferedImage.TYPE_INT_RGB
    );
    Graphics2D graphics2D = buffImage.createGraphics();
    try {
      p.draw(graphics2D);
      System.out.println("Writing picture to " + filename);
      ImageIO.write(buffImage, "PNG", new File(filename));
    } finally {
      graphics2D.dispose();
    }
  }

  /**
   * Writes this map to a JPG file.
   *
   * @param filename the path and name of the file to create.
   */
  public void writeToJPGFile(String filename) throws IOException {
    this.prepareToDraw();

    BufferedImage buffImage = new BufferedImage(
      p.getWidth(),
      p.getHeight(),
      BufferedImage.TYPE_INT_RGB
    );
    Graphics2D graphics2D = buffImage.createGraphics();
    try {
      p.draw(graphics2D);
      System.out.println("Writing picture to " + filename);
      ImageIO.write(buffImage, "JPG", new File(filename));
    } finally {
      graphics2D.dispose();
    }
  }

  /**
   * Writes this map to an SVG file. You may use the simpler methods provided below, instead of this
   * method.
   *
   * @param filename the path and name of the file to create.
   * @param embedFonts whether to embed fonts. Embedded fonts give a nicer map but yield larger file
   *     sizes.
   * @param useCompression whether to write this as an SVGZ file.
   */
  public void writeToSVGFile(
    String filename,
    boolean embedFonts,
    boolean useCompression
  )
    throws FileNotFoundException, IOException, UnsupportedEncodingException, SVGGraphics2DIOException {
    this.prepareToDraw();

    DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();

    // Create an instance of org.w3c.dom.Document
    Document document = domImpl.createDocument(null, "svg", null);

    SVGGeneratorContext ctx = SVGGeneratorContext.createDefault(document);
    ctx.setComment("Generated by CG-view with the Batik SVG Generator");
    ctx.setPrecision(12);

    SVGGraphics2D graphics2D;

    if (embedFonts) {
      graphics2D = new SVGGraphics2D(ctx, true);
    } else {
      graphics2D = new SVGGraphics2D(ctx, false);
    }
    try {
      p.setMinimumFeatureLength(0.02d);
      p.draw(graphics2D);
      p.setMinimumFeatureLength(1.0d);

      System.out.println("Writing picture to " + filename);
      boolean useCSS = true;

      FileOutputStream fileOutputStream = new FileOutputStream(
        new File(filename)
      );

      if (useCompression) {
        GZIPOutputStream gzipOut = new GZIPOutputStream(fileOutputStream);
        Writer out = new OutputStreamWriter(gzipOut, "UTF-8");
        graphics2D.stream(out, useCSS);
        out.flush();
        gzipOut.flush();
        out.close();
        gzipOut.close();
      } else {
        Writer out = new OutputStreamWriter(fileOutputStream, "UTF-8");
        graphics2D.stream(out, useCSS);
        out.flush();
        out.close();
      }
    } finally {
      graphics2D.dispose();
    }
  }

  /**
   * Writes this map to an SVG file. No compression is used, and fonts are embedded.
   *
   * @param filename the path and name of the file to create.
   */
  public void writeToSVGFile(String filename)
    throws FileNotFoundException, IOException, UnsupportedEncodingException, SVGGraphics2DIOException {
    writeToSVGFile(filename, true, false);
  }

  /**
   * Writes this map to an SVGZ file (a zipped SVG file).
   *
   * @param filename the path and name of the file to create.
   */
  public void writeToSVGZFile(String filename)
    throws FileNotFoundException, IOException, UnsupportedEncodingException, SVGGraphics2DIOException {
    writeToSVGFile(filename, true, true);
  }

  public static void main(String ars[]) {
    BufferedImage bi;
    Graphics2D big;
    int no_strand = 0;
    int forward = 1;
    int reverse = 2;

    Plasmid plasmid = new Plasmid(4921);

    plasmid.setUseColoredLabelBackground(false);
    plasmid.setColorScheme(Plasmid.REGULAR);
    plasmid.setTitle("pGFP-1");
    plasmid.setShowTitle(true);
    // plasmid.setDrawTickMarks(false);
    // plasmid.setUseColoredLabels(false);
    plasmid.setAddCategoryInfo(true);
    // plasmid.setLegendPosition("none");
    // plasmid.setShowLabels(false);

    plasmid.setImageWidth(1200);
    plasmid.setImageHeight(1000);

    plasmid.addFeature("restriction_site", "KpnI", 4624, 4624, no_strand);
    plasmid.addFeature("restriction_site", "RsaI", 4622, 4622, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 4704, 4704, no_strand);
    plasmid.addFeature("restriction_site", "MboI", 4704, 4704, no_strand);
    plasmid.addFeature("restriction_site", "HinfI", 4756, 4756, no_strand);
    plasmid.addFeature("restriction_site", "HpaI", 4774, 4774, no_strand);
    plasmid.addFeature("restriction_site", "HincII", 4774, 4774, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 4793, 4793, no_strand);
    plasmid.addFeature("restriction_site", "MboI", 4793, 4793, no_strand);

    plasmid.addFeature("restriction_site", "SpeI", 4842, 4842, no_strand);
    plasmid.addFeature("restriction_site", "HinfI", 4888, 4888, no_strand);
    plasmid.addFeature("restriction_site", "AluI", 4905, 4905, no_strand);
    plasmid.addFeature("restriction_site", "TaqI", 4957, 4957, no_strand);
    plasmid.addFeature("restriction_site", "MspI", 4968, 4968, no_strand);
    plasmid.addFeature("restriction_site", "HpaII", 4968, 4968, no_strand);

    plasmid.addFeature("restriction_site", "BglII", 8, 8, no_strand);
    plasmid.addFeature("restriction_site", "MboI", 8, 8, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 8, 8, no_strand);
    plasmid.addFeature("restriction_site", "HinfI", 14, 14, no_strand);
    plasmid.addFeature("restriction_site", "HinfI", 41, 41, no_strand);
    plasmid.addFeature("restriction_site", "RsaI", 78, 78, no_strand);
    plasmid.addFeature("restriction_site", "MspI", 87, 87, no_strand);
    plasmid.addFeature("restriction_site", "HpaII", 87, 87, no_strand);
    plasmid.addFeature("restriction_site", "TaqI", 113, 113, no_strand);
    plasmid.addFeature("restriction_site", "MboI", 158, 158, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 158, 158, no_strand);

    plasmid.addFeature("restriction_site", "MboI", 247, 247, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 247, 247, no_strand);
    plasmid.addFeature("restriction_site", "TaqI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "ClaI", 250, 250, no_strand);

    plasmid.addFeature("restriction_site", "PvuI", 276, 276, no_strand);
    plasmid.addFeature("restriction_site", "MboI", 273, 273, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 273, 273, no_strand);
    plasmid.addFeature("restriction_site", "BglII", 288, 288, no_strand);
    plasmid.addFeature("restriction_site", "MboI", 288, 288, no_strand);
    plasmid.addFeature("restriction_site", "NdeII", 288, 288, no_strand);

    plasmid.addFeature("restriction_site", "PstI", 310, 310, no_strand);

    // plasmid.addFeature("open_reading_frame", "ORF", 310, 1010, reverse);

    // plasmid.addFeature("origin_of_replication", "my origin", 950, 1210, reverse);

    // plasmid.addFeature("origin_of_replication", "my origin", 950, 1210, reverse);

    // plasmid.addFeature("origin_of_replication", "my origin", 950, 1210, reverse);

    // plasmid.addFeature("origin_of_replication", "f1 replication origin", 400, 900, reverse);

    plasmid.addFeature("promoter", "the promoter", 4000, 4050, forward);

    plasmid.addFeature("terminator", "T7 term", 6780, 100, forward);

    plasmid.addFeature("selectable_marker", "AmpR", 1500, 2500, forward);

    plasmid.addFeature(
      "selectable_marker",
      "a longer marker label a longg label extending long",
      700,
      750,
      reverse
    );

    plasmid.addFeature("reporter_gene", "GFP", 750, 800, forward);

    plasmid.addFeature("regulatory_sequence", "regulatory", 300, 342, reverse);

    plasmid.addFeature("tag", "tag", 30, 50, forward);

    plasmid.addFeature("other_gene", "other gene", 70, 150, reverse);

    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);

    plasmid.addFeature("restriction_site", "EcoRI", 32, 32, no_strand);

    plasmid.addFeature("unique_restriction_site", "XmaI", 578, 578, no_strand);

    plasmid.addFeature("unique_restriction_site", "SmaI", 578, 578, no_strand);

    plasmid.addFeature("unique_restriction_site", "XmeII", 578, 578, no_strand);

    // label testing
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 20, 20, no_strand);

    // more label testing
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 250, 250, no_strand);

    // more label testing
    plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 500, 500, no_strand);

    // and more label testing
    plasmid.addFeature("restriction_site", "BamHI", 750, 750, no_strand);
    plasmid.addFeature("restriction_site", "XhoI", 757, 757, no_strand);
    plasmid.addFeature("restriction_site", "BglII", 777, 777, no_strand);
    plasmid.addFeature("restriction_site", "NotI", 785, 785, no_strand);
    plasmid.addFeature("restriction_site", "PstI", 792, 792, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 750, 750, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 750, 750, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 750, 750, no_strand);
    // plasmid.addFeature("restriction_site", "BamHI", 750, 750, no_strand);
    plasmid.setMessage("This is a message");
    try {
      plasmid.writeToPNGFile("output.png");
    } catch (Exception e) {
      System.err.println("Exception thrown in main");
    }
  }
}
