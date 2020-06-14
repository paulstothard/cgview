package ca.ualberta.stothard.cgview;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.ArrayList;

/**
 * This class reads a tab delimited feature file and creates a Cgview object.
 *
 * @author Paul Stothard
 */
public class CgviewFactoryTab implements CgviewConstants {
  private Cgview cgview;

  private Hashtable COLORS = new Hashtable();
  private Hashtable MAP_ITEM_COLORS = new Hashtable();
  private Hashtable FEATURE_COLORS = new Hashtable();
  private Hashtable FEATURE_DECORATIONS_DIRECT = new Hashtable();
  private Hashtable FEATURE_DECORATIONS_REVERSE = new Hashtable();
  private Hashtable LEGEND_ITEM_NAMES = new Hashtable();
  private Hashtable DRAW_LEGEND_ITEMS = new Hashtable();

  private Hashtable DEFAULT_MAP_SIZES = new Hashtable();
  private Hashtable MIN_MAP_SIZES = new Hashtable();
  private Hashtable MAX_MAP_SIZES = new Hashtable();

  private Hashtable DEFAULT_MAP_MODES = new Hashtable();
  private Hashtable SMALL_MAP_MODES = new Hashtable();
  private Hashtable LARGE_MAP_MODES = new Hashtable();

  private int LARGE_MAP_MODE_DIMENSION = 2000;
  private int SMALL_MAP_MODE_DIMENSION = 800;

  private FeatureSlot forwardSlot1;
  private FeatureSlot forwardSlot2;
  private FeatureSlot forwardSlot3;
  private FeatureSlot forwardSlot4;
  private FeatureSlot forwardSlot5;
  private FeatureSlot forwardSlot6;

  private FeatureSlot reverseSlot1;
  private FeatureSlot reverseSlot2;
  private FeatureSlot reverseSlot3;
  private FeatureSlot reverseSlot4;
  private FeatureSlot reverseSlot5;
  private FeatureSlot reverseSlot6;

  private Legend legend;

  private int zoomCenter = 1;
  private int length = 0;
  private String title = "";

  private int mapWidth = 900;
  private int mapHeight = 600;
  private int mapSmallest = 0;

  private float mapItemSizeAdjustment = 0.0f;

  private int labelFontSize = -1;
  private int rulerFontSize = -1;
  private int legendFontSize = -1;

  private double tickDensity = 1.0d;

  private boolean readDimension = true;

  private NumberFormat nf = NumberFormat.getInstance();

  private int MAX_MOUSEOVER_LENGTH = 100;
  private int MAX_LABEL_LENGTH = 50;
  private int MAX_TITLE_LENGTH = 50;
  private int MAX_SEQUENCE_LENGTH = 200000000;

  private int MAX_IMAGE_WIDTH;
  private int MIN_IMAGE_WIDTH;

  private int MAX_IMAGE_HEIGHT;
  private int MIN_IMAGE_HEIGHT;

  private float opacity = 1.0f;
  private boolean useColoredLabelBackground = false;
  private boolean showTitle = true;
  private boolean showShading = true;
  private boolean showBorder = false;
  private boolean allowLabelClashLegend = false;
  private boolean moveInnerLabelsToOuter = true;
  private int labelPlacementQuality = 9;
  private int legendPosition = LEGEND_UPPER_RIGHT;
  private boolean useColoredLabels = true;
  private boolean drawTickMarks = true;
  private boolean showLegend = true;
  private boolean showWarning = false;
  private boolean showLabels = true;
  private float shadingProportion = 0.4f;
  private float shadingOpacity = 0.5f;

  /** Constructs a new CgviewFactoryTab object. */
  public CgviewFactoryTab() {
    cgview = new Cgview(1);
  }

  /**
   * Generates a Cgview object from an tab delimited feature file.
   *
   * @param filename the file to read.
   * @return the newly created Cgview object.
   * @throws Exception
   * @throws IOException
   */
  public Cgview createCgviewFromFile(String filename)
    throws Exception, IOException {
    File file = new File(filename);
    URL url = null;
    url = file.toURL();
    return createCgviewFromURL(url);
  }

  /**
   * Generates a Cgview object from an tab delimited feature file.
   *
   * @param url the URL of the tab delimited file to read.
   * @return the newly created Cgview object.
   * @throws Exception
   * @throws IOException
   */
  public Cgview createCgviewFromURL(URL url) throws Exception, IOException {
    COLORS.put("black", new Color(0, 0, 0));
    COLORS.put("silver", new Color(192, 192, 192));
    COLORS.put("gray", new Color(128, 128, 128));
    COLORS.put("white", new Color(255, 255, 255));
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
    MAP_ITEM_COLORS.put("zeroLine", COLORS.get("black"));
    MAP_ITEM_COLORS.put("background", COLORS.get("white"));

    FEATURE_COLORS.put("forward_gene", COLORS.get("red"));
    FEATURE_COLORS.put("reverse_gene", COLORS.get("blue"));
    FEATURE_COLORS.put("origin_of_replication", COLORS.get("black"));
    FEATURE_COLORS.put("promoter", COLORS.get("green"));
    FEATURE_COLORS.put("terminator", COLORS.get("maroon"));
    FEATURE_COLORS.put("regulatory_sequence", COLORS.get("olive"));
    FEATURE_COLORS.put("unique_restriction_site", COLORS.get("purple"));
    FEATURE_COLORS.put("restriction_site", COLORS.get("azure"));
    FEATURE_COLORS.put("open_reading_frame", COLORS.get("pink"));
    // FEATURE_COLORS.put("gene", COLORS.get("blue"));
    FEATURE_COLORS.put("predicted_gene", COLORS.get("orange"));
    FEATURE_COLORS.put("sequence_similarity", COLORS.get("silver"));
    FEATURE_COLORS.put("score", COLORS.get("fuchsia"));
    FEATURE_COLORS.put("score_II", COLORS.get("gray"));
    FEATURE_COLORS.put("misc", COLORS.get("gray"));
    FEATURE_COLORS.put("primer", COLORS.get("teal"));

    FEATURE_DECORATIONS_DIRECT.put(
      "forward_gene",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );
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
      "regulatory_sequence",
      new Integer(DECORATION_STANDARD)
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
    // FEATURE_DECORATIONS_DIRECT.put("gene", new Integer(DECORATION_CLOCKWISE_ARROW));
    FEATURE_DECORATIONS_DIRECT.put(
      "predicted_gene",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_DIRECT.put(
      "sequence_similarity",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put("score", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put(
      "score_II",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_DIRECT.put("misc", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put(
      "primer",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );

    FEATURE_DECORATIONS_REVERSE.put(
      "reverse_gene",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
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
      "regulatory_sequence",
      new Integer(DECORATION_STANDARD)
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
    // FEATURE_DECORATIONS_REVERSE.put("gene", new Integer(DECORATION_COUNTERCLOCKWISE_ARROW));
    FEATURE_DECORATIONS_REVERSE.put(
      "predicted_gene",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_REVERSE.put(
      "sequence_similarity",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put("score", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put(
      "score_II",
      new Integer(DECORATION_STANDARD)
    );
    FEATURE_DECORATIONS_REVERSE.put("misc", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put(
      "primer",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );

    LEGEND_ITEM_NAMES.put("forward_gene", "Forward gene");
    LEGEND_ITEM_NAMES.put("reverse_gene", "Reverse gene");
    LEGEND_ITEM_NAMES.put("origin_of_replication", "Origin of replication");
    LEGEND_ITEM_NAMES.put("promoter", "Promoter");
    LEGEND_ITEM_NAMES.put("terminator", "Terminator");
    LEGEND_ITEM_NAMES.put("regulatory_sequence", "Regulatory sequence");
    LEGEND_ITEM_NAMES.put("unique_restriction_site", "Unique restriction site");
    LEGEND_ITEM_NAMES.put("restriction_site", "Restriction site");
    LEGEND_ITEM_NAMES.put("open_reading_frame", "Open reading frame");
    // LEGEND_ITEM_NAMES.put("gene", "Gene");
    LEGEND_ITEM_NAMES.put("predicted_gene", "Predicted gene");
    LEGEND_ITEM_NAMES.put("sequence_similarity", "Sequence similarity");
    LEGEND_ITEM_NAMES.put("score", "Score");
    LEGEND_ITEM_NAMES.put("score_II", "Score II");
    LEGEND_ITEM_NAMES.put("misc", "Miscellaneous");
    LEGEND_ITEM_NAMES.put("primer", "Primer");

    DEFAULT_MAP_SIZES.put("mapWidth", new Integer(900));
    DEFAULT_MAP_SIZES.put("mapHeight", new Integer(900));
    DEFAULT_MAP_SIZES.put("labelFontSize", new Integer(10));
    DEFAULT_MAP_SIZES.put("titleFontSize", new Integer(15));
    DEFAULT_MAP_SIZES.put("legendFontSize", new Integer(13));
    DEFAULT_MAP_SIZES.put("rulerFontSize", new Integer(8));
    DEFAULT_MAP_SIZES.put("messageFontSize", new Integer(12));
    DEFAULT_MAP_SIZES.put("featureThickness", new Float(12.0f));
    DEFAULT_MAP_SIZES.put("backboneThickness", new Float(4.0f));
    DEFAULT_MAP_SIZES.put("featureSlotSpacing", new Float(2.0f));
    DEFAULT_MAP_SIZES.put("tickLength", new Float(6.0f));
    DEFAULT_MAP_SIZES.put("tickThickness", new Float(2.0f));
    DEFAULT_MAP_SIZES.put("shortTickThickness", new Float(2.0f));
    DEFAULT_MAP_SIZES.put("labelLineLength", new Double(50.0d));
    DEFAULT_MAP_SIZES.put("labelLineThickness", new Float(1.0f));
    DEFAULT_MAP_SIZES.put("arrowheadLength", new Double(5.0d));
    DEFAULT_MAP_SIZES.put("maxLabels", new Integer(5000));

    MIN_MAP_SIZES.put("mapWidth", new Integer(500));
    MIN_MAP_SIZES.put("mapHeight", new Integer(500));
    MIN_MAP_SIZES.put("labelFontSize", new Integer(1));
    MIN_MAP_SIZES.put("titleFontSize", new Integer(1));
    MIN_MAP_SIZES.put("legendFontSize", new Integer(1));
    MIN_MAP_SIZES.put("rulerFontSize", new Integer(1));
    MIN_MAP_SIZES.put("messageFontSize", new Integer(1));
    MIN_MAP_SIZES.put("featureThickness", new Float(0.5f));
    MIN_MAP_SIZES.put("backboneThickness", new Float(0.1f));
    MIN_MAP_SIZES.put("featureSlotSpacing", new Float(0.1f));
    MIN_MAP_SIZES.put("tickLength", new Float(1.0f));
    MIN_MAP_SIZES.put("tickThickness", new Float(0.5f));
    MIN_MAP_SIZES.put("shortTickThickness", new Float(0.5));
    MIN_MAP_SIZES.put("labelLineLength", new Double(2.0f));
    MIN_MAP_SIZES.put("labelLineThickness", new Float(0.5));
    MIN_MAP_SIZES.put("arrowheadLength", new Double(0.5d));
    MIN_MAP_SIZES.put("maxLabels", new Integer(10));

    MAX_MAP_SIZES.put("mapWidth", new Integer(30000));
    MAX_MAP_SIZES.put("mapHeight", new Integer(30000));
    MAX_MAP_SIZES.put("labelFontSize", new Integer(10));
    MAX_MAP_SIZES.put("titleFontSize", new Integer(100));
    MAX_MAP_SIZES.put("legendFontSize", new Integer(100));
    MAX_MAP_SIZES.put("rulerFontSize", new Integer(8));
    MAX_MAP_SIZES.put("messageFontSize", new Integer(100));
    MAX_MAP_SIZES.put("featureThickness", new Float(80.0f));
    MAX_MAP_SIZES.put("backboneThickness", new Float(5.0f));
    MAX_MAP_SIZES.put("featureSlotSpacing", new Float(5.0f));
    MAX_MAP_SIZES.put("tickLength", new Float(6.0f));
    MAX_MAP_SIZES.put("tickThickness", new Float(2.0f));
    MAX_MAP_SIZES.put("shortTickThickness", new Float(2.0f));
    MAX_MAP_SIZES.put("labelLineLength", new Double(80.0f));
    MAX_MAP_SIZES.put("labelLineThickness", new Float(1.0f));
    MAX_MAP_SIZES.put("arrowheadLength", new Double(18.0d));
    MAX_MAP_SIZES.put("maxLabels", new Integer(50000));

    DEFAULT_MAP_MODES.put(
      "giveFeaturePositions",
      new Integer(POSITIONS_NO_SHOW)
    );
    DEFAULT_MAP_MODES.put("useInnerLabels", new Integer(INNER_LABELS_AUTO));

    SMALL_MAP_MODES.put("giveFeaturePositions", new Integer(POSITIONS_NO_SHOW));
    SMALL_MAP_MODES.put("useInnerLabels", new Integer(INNER_LABELS_AUTO));

    LARGE_MAP_MODES.put("giveFeaturePositions", new Integer(POSITIONS_NO_SHOW));
    LARGE_MAP_MODES.put("useInnerLabels", new Integer(INNER_LABELS_SHOW));

    MAX_IMAGE_WIDTH = ((Integer) MAX_MAP_SIZES.get("mapWidth")).intValue();
    MIN_IMAGE_WIDTH = ((Integer) MIN_MAP_SIZES.get("mapWidth")).intValue();

    MAX_IMAGE_HEIGHT = ((Integer) MAX_MAP_SIZES.get("mapHeight")).intValue();
    MIN_IMAGE_HEIGHT = ((Integer) MIN_MAP_SIZES.get("mapHeight")).intValue();

    InputStream in;
    BufferedReader buf;
    int lineCount = 0;
    String line;
    String lineItems[];

    int columnNumber = 0;

    String strand;
    int slot = 0;
    int start = 0;
    int stop = 0;
    float opacity = 1.0f;
    float thickness = 1.0f;
    float radius = 0.0f;
    String type = "";
    String label = "";
    String mouseover = "";
    String hyperlink = "";

    boolean hasStrandColumn = false;
    boolean hasSlotColumn = false;
    boolean hasStartColumn = false;
    boolean hasStopColumn = false;
    boolean hasOpacityColumn = false;
    boolean hasThicknessColumn = false;
    boolean hasRadiusColumn = false;
    boolean hasTypeColumn = false;
    boolean hasLabelColumn = false;
    boolean hasMouseoverColumn = false;
    boolean hasHyperlinkColumn = false;

    int strandColumnIndex = -1;
    int slotColumnIndex = -1;
    int startColumnIndex = -1;
    int stopColumnIndex = -1;
    int opacityColumnIndex = -1;
    int thicknessColumnIndex = -1;
    int radiusColumnIndex = -1;
    int typeColumnIndex = -1;
    int labelColumnIndex = -1;
    int mouseoverColumnIndex = -1;
    int hyperlinkColumnIndex = -1;

    in = url.openStream();

    System.out.println("Parsing tab delimited input.");

    buf = new BufferedReader(new InputStreamReader(in));
    while ((line = buf.readLine()) != null) {
      lineCount++;
      if (line.startsWith("#")) {
        title = line.substring(1);
      } else if (line.startsWith("%")) {
        try {
          length = Integer.parseInt(line.substring(1));
        } catch (Exception e) {
          throw new Exception(
            "There is a problem with the length value on line " +
            lineCount +
            " in the data file."
          );
        }
      } else if (line.startsWith("!")) {
        line = line.substring(1);
        lineItems = line.split("(?:\\s*\\t+\\s*)|(?:\\s{2,})");
        // lineItems = line.split("\\s*\\t+\\s*");
        columnNumber = lineItems.length;
        for (int i = 0; i < lineItems.length; i = i + 1) {
          String lineItem = lineItems[i].trim();
          if (lineItem.equalsIgnoreCase("strand")) {
            hasStrandColumn = true;
            strandColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("slot")) {
            hasSlotColumn = true;
            slotColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("start")) {
            hasStartColumn = true;
            startColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("stop")) {
            hasStopColumn = true;
            stopColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("type")) {
            hasTypeColumn = true;
            typeColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("opacity")) {
            hasOpacityColumn = true;
            opacityColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("thickness")) {
            hasThicknessColumn = true;
            thicknessColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("radius")) {
            hasRadiusColumn = true;
            radiusColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("label")) {
            hasLabelColumn = true;
            labelColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("mouseover")) {
            hasMouseoverColumn = true;
            mouseoverColumnIndex = i;
          } else if (lineItem.equalsIgnoreCase("hyperlink")) {
            hasHyperlinkColumn = true;
            hyperlinkColumnIndex = i;
          }
        }
      } else {
        // some text editors may use spaces instead of tabs.
        // try to handle this.
        lineItems = line.split("\\s*\\t+\\s*");
        if (lineItems.length != columnNumber) {
          lineItems = line.split("(?:\\s*\\t+\\s*)|(?:\\s{2,})");
        }

        if (lineItems.length >= 5) {
          if (hasStrandColumn == false) {
            throw new Exception(
              "A \"strand\" column has not been defined in the data file."
            );
          }
          if (hasSlotColumn == false) {
            throw new Exception(
              "A \"slot\" column has not been defined in the data file."
            );
          }
          if (hasStartColumn == false) {
            throw new Exception(
              "A \"start\" column has not been defined in the data file."
            );
          }
          if (hasStopColumn == false) {
            throw new Exception(
              "A \"stop\" column has not been defined in the data file."
            );
          }
          if (hasTypeColumn == false) {
            throw new Exception(
              "A \"type\" column has not been defined in the data file."
            );
          }

          try {
            strand = lineItems[strandColumnIndex];
          } catch (Exception e) {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          try {
            slot = Integer.parseInt(lineItems[slotColumnIndex]);
          } catch (Exception e) {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          try {
            start = Integer.parseInt(lineItems[startColumnIndex]);
          } catch (Exception e) {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          try {
            stop = Integer.parseInt(lineItems[stopColumnIndex]);
          } catch (Exception e) {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          try {
            type = lineItems[typeColumnIndex];
          } catch (Exception e) {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          if (hasOpacityColumn) {
            try {
              if (
                (!(lineItems[opacityColumnIndex].matches("^\\s*$"))) &&
                (!(lineItems[opacityColumnIndex].equals("-")))
              ) {
                opacity = Float.parseFloat(lineItems[opacityColumnIndex]);
              } else {
                opacity = 1.0f;
              }
            } catch (Exception e) {
              throw new Exception(
                "There is a problem with line " +
                lineCount +
                " in the data file."
              );
            }
          } else {
            opacity = 1.0f;
          }

          if (hasThicknessColumn) {
            try {
              if (
                (!(lineItems[thicknessColumnIndex].matches("^\\s*$"))) &&
                (!(lineItems[thicknessColumnIndex].equals("-")))
              ) {
                thickness = Float.parseFloat(lineItems[thicknessColumnIndex]);
              } else {
                thickness = 1.0f;
              }
            } catch (Exception e) {
              throw new Exception(
                "There is a problem with line " +
                lineCount +
                " in the data file."
              );
            }
          } else {
            thickness = 1.0f;
          }

          if (hasRadiusColumn) {
            try {
              if (
                (!(lineItems[radiusColumnIndex].matches("^\\s*$"))) &&
                (!(lineItems[radiusColumnIndex].equals("-")))
              ) {
                radius = Float.parseFloat(lineItems[radiusColumnIndex]);
              } else {
                radius = 0.0f;
              }
            } catch (Exception e) {
              throw new Exception(
                "There is a problem with line " +
                lineCount +
                " in the data file."
              );
            }
          } else {
            radius = 0.0f;
          }

          if (hasLabelColumn) {
            try {
              if (
                (!(lineItems[labelColumnIndex].matches("^\\s*$"))) &&
                (!(lineItems[labelColumnIndex].equals("-")))
              ) {
                label = lineItems[labelColumnIndex];
              } else {
                label = "";
              }
            } catch (Exception e) {
              throw new Exception(
                "There is a problem with line " +
                lineCount +
                " in the data file."
              );
            }
          } else {
            label = "";
          }

          if (hasMouseoverColumn) {
            try {
              if (
                (!(lineItems[mouseoverColumnIndex].matches("^\\s*$"))) &&
                (!(lineItems[mouseoverColumnIndex].equals("-")))
              ) {
                mouseover = lineItems[mouseoverColumnIndex];
              } else {
                mouseover = "";
              }
            } catch (Exception e) {
              throw new Exception(
                "There is a problem with line " +
                lineCount +
                " in the data file."
              );
            }
          } else {
            mouseover = "";
          }

          if (hasHyperlinkColumn) {
            try {
              if (
                (!(lineItems[hyperlinkColumnIndex].matches("^\\s*$"))) &&
                (!(lineItems[hyperlinkColumnIndex].equals("-")))
              ) {
                hyperlink = lineItems[hyperlinkColumnIndex];
              } else {
                hyperlink = "";
              }
            } catch (Exception e) {
              throw new Exception(
                "There is a problem with line " +
                lineCount +
                " in the data file."
              );
            }
          } else {
            hyperlink = "";
          }

          try {
            // change type 'gene' to 'forward_gene' or 'reverse_gene' depending on strand
            if (type.equalsIgnoreCase("gene")) {
              if (
                (strand.equalsIgnoreCase("forward")) ||
                (strand.equalsIgnoreCase("+"))
              ) {
                type = "forward_gene";
              } else if (
                (strand.equalsIgnoreCase("reverse")) ||
                (strand.equalsIgnoreCase("-"))
              ) {
                type = "reverse_gene";
              }
            }

            addFeature(
              strand,
              slot,
              start,
              stop,
              opacity,
              thickness,
              radius,
              type,
              label,
              mouseover,
              hyperlink
            );
          } catch (Exception e) {
            throw new Exception(
              "Line " + lineCount + ": " + e.toString() + "."
            );
          }
        } else if (lineItems.length > 2) {
          throw new Exception(
            "The contents of line " +
            lineCount +
            " could not be parsed in the data file."
          );
        }
      }
    }

    if (length > MAX_SEQUENCE_LENGTH) {
      throw new Exception(
        "Maximum sequence length is " + MAX_SEQUENCE_LENGTH + "."
      );
    } else if (length < 1) {
      throw new Exception("Minimum sequence length is 1 base.");
    }

    if (mapWidth > MAX_IMAGE_WIDTH) {
      throw new Exception("Maximum image width is " + MAX_IMAGE_WIDTH + ".");
    } else if (mapWidth < MIN_IMAGE_WIDTH) {
      throw new Exception("Minimum image width is " + MIN_IMAGE_WIDTH + ".");
    }

    if (mapHeight > MAX_IMAGE_HEIGHT) {
      throw new Exception("Maximum image height is " + MAX_IMAGE_HEIGHT + ".");
    } else if (mapHeight < MIN_IMAGE_HEIGHT) {
      throw new Exception("Minimum image height is " + MIN_IMAGE_HEIGHT + ".");
    }

    prepareToDraw();
    return cgview;
  }

  /**
   * Add a feature to this map. Note that the start of the feature should be a smaller number than
   * the stop of the feature, regardless of the strand. The only case where start is larger than the
   * stop is when the feature runs across the start/stop boundary, for example 6899-10 on a 7000bp
   * plasmid.
   *
   * @param strand one of the following: forward, reverse.
   * @param slot one of the following: 1, 2, 3, 4, 5, 6.
   * @param start the start position of the feature. Must be between 1 and the length of the
   *     plasmid.
   * @param stop the end position of the feature. Must be between 1 and the length of the plasmid.
   * @param opacity the opacity of the feature.
   * @param thickness the thickness of the feature.
   * @param radius the radius of the feature.
   * @param type one of the following: origin_of_replication, promoter, terminator,
   *     regulatory_sequence, unique_restriction_site, restriction_site, open_reading_frame, gene,
   *     predicted_gene, sequence_similarity, score, score_II, misc, primer.
   * @param label the label to show or empty String.
   * @param mouseover the mouseover to show or empty String.
   * @param hyperlink the label hyperlink or empty String.
   * @throws Exception
   */
  public void addFeature(
    String strand,
    int slot,
    int start,
    int stop,
    float opacity,
    float thickness,
    float radius,
    String type,
    String label,
    String mouseover,
    String hyperlink
  )
    throws Exception {
    int decoration;
    Color color;
    int intStrand;
    String problem;

    // look for feature position problems
    if (start > length) {
      throw new Exception(
        "The start value " + start + " is greater than the sequence length."
      );
    }
    if (start < 1) {
      throw new Exception("The start value " + start + " is less than 1.");
    }

    if (stop > length) {
      throw new Exception(
        "The stop value " + stop + " is greater than the sequence length."
      );
    }
    if (stop < 1) {
      throw new Exception("The stop value " + stop + " is less than 1.");
    }

    // convert the strand
    if (
      (strand.equalsIgnoreCase("forward")) || (strand.equalsIgnoreCase("+"))
    ) {
      intStrand = DIRECT_STRAND;
    } else if (
      (strand.equalsIgnoreCase("reverse")) || (strand.equalsIgnoreCase("-"))
    ) {
      intStrand = REVERSE_STRAND;
    } else {
      throw new Exception(
        "The strand value must be \"forward\" or \"reverse\"."
      );
    }

    // obtain the color
    try {
      color = getFeatureColor(type);
    } catch (NullPointerException e) {
      throw new Exception(
        "The feature type \"" + type + "\" was not recognized."
      );
    }

    // obtain the decoration
    try {
      decoration = getFeatureDecoration(type, intStrand);
    } catch (NullPointerException e) {
      throw new Exception(
        "The feature type \"" + type + "\" was not recognized."
      );
    }

    // add item to the legend
    try {
      addItemToLegend(type);
    } catch (NullPointerException e) {
      throw new Exception(
        "The feature type \"" + type + "\" was not recognized."
      );
    }

    // create a feature and a feature range
    // then figure out which feature slot to put the feature in.
    Feature feature = new Feature(showShading);
    FeatureRange featureRange = new FeatureRange(feature, start, stop);
    featureRange.setDecoration(decoration);
    featureRange.setColor(color);

    featureRange.setOpacity(opacity);
    featureRange.setProportionOfThickness(thickness);
    featureRange.setRadiusAdjustment(radius);

    // if in slot 5 or 6 don't use shading
    if ((slot == 5) || (slot == 6)) {
      feature.setShowShading(false);
    }

    if (!(label.matches("^\\s*$"))) {
      // shorten long label
      if (label.length() > MAX_LABEL_LENGTH) {
        label = label.substring(0, MAX_LABEL_LENGTH) + "...";
      }
      featureRange.setLabel(label);
    }

    if (!(mouseover.matches("^\\s*$"))) {
      // shorten long mouseover
      if (mouseover.length() > MAX_MOUSEOVER_LENGTH) {
        mouseover = mouseover.substring(0, MAX_MOUSEOVER_LENGTH) + "...";
      }
      featureRange.setMouseover(mouseover);
    }

    if (!(hyperlink.matches("^\\s*$"))) {
      featureRange.setHyperlink(hyperlink);
    }

    if (intStrand == DIRECT_STRAND) {
      if (slot == 1) {
        if (forwardSlot1 == null) {
          forwardSlot1 = new FeatureSlot(DIRECT_STRAND, showShading);
          feature.setFeatureSlot(forwardSlot1);
        } else {
          feature.setFeatureSlot(forwardSlot1);
        }
      } else if (slot == 2) {
        if (forwardSlot2 == null) {
          forwardSlot2 = new FeatureSlot(DIRECT_STRAND, showShading);
          feature.setFeatureSlot(forwardSlot2);
        } else {
          feature.setFeatureSlot(forwardSlot2);
        }
      } else if (slot == 3) {
        if (forwardSlot3 == null) {
          forwardSlot3 = new FeatureSlot(DIRECT_STRAND, showShading);
          feature.setFeatureSlot(forwardSlot3);
        } else {
          feature.setFeatureSlot(forwardSlot3);
        }
      } else if (slot == 4) {
        if (forwardSlot4 == null) {
          forwardSlot4 = new FeatureSlot(DIRECT_STRAND, showShading);
          feature.setFeatureSlot(forwardSlot4);
        } else {
          feature.setFeatureSlot(forwardSlot4);
        }
      } else if (slot == 5) {
        if (forwardSlot5 == null) {
          forwardSlot5 = new FeatureSlot(DIRECT_STRAND, showShading);
          feature.setFeatureSlot(forwardSlot5);
        } else {
          feature.setFeatureSlot(forwardSlot5);
        }
      } else if (slot == 6) {
        if (forwardSlot6 == null) {
          forwardSlot6 = new FeatureSlot(DIRECT_STRAND, showShading);
          feature.setFeatureSlot(forwardSlot6);
        } else {
          feature.setFeatureSlot(forwardSlot6);
        }
      } else {
        throw new Exception("The slot value must be between 1 and 6");
      }
    } else {
      if (slot == 1) {
        if (reverseSlot1 == null) {
          reverseSlot1 = new FeatureSlot(REVERSE_STRAND, showShading);
          feature.setFeatureSlot(reverseSlot1);
        } else {
          feature.setFeatureSlot(reverseSlot1);
        }
      } else if (slot == 2) {
        if (reverseSlot2 == null) {
          reverseSlot2 = new FeatureSlot(REVERSE_STRAND, showShading);
          feature.setFeatureSlot(reverseSlot2);
        } else {
          feature.setFeatureSlot(reverseSlot2);
        }
      } else if (slot == 3) {
        if (reverseSlot3 == null) {
          reverseSlot3 = new FeatureSlot(REVERSE_STRAND, showShading);
          feature.setFeatureSlot(reverseSlot3);
        } else {
          feature.setFeatureSlot(reverseSlot3);
        }
      } else if (slot == 4) {
        if (reverseSlot4 == null) {
          reverseSlot4 = new FeatureSlot(REVERSE_STRAND, showShading);
          feature.setFeatureSlot(reverseSlot4);
        } else {
          feature.setFeatureSlot(reverseSlot4);
        }
      } else if (slot == 5) {
        if (reverseSlot5 == null) {
          reverseSlot5 = new FeatureSlot(REVERSE_STRAND, showShading);
          feature.setFeatureSlot(reverseSlot5);
        } else {
          feature.setFeatureSlot(reverseSlot5);
        }
      } else if (slot == 6) {
        if (reverseSlot6 == null) {
          reverseSlot6 = new FeatureSlot(REVERSE_STRAND, showShading);
          feature.setFeatureSlot(reverseSlot6);
        } else {
          feature.setFeatureSlot(reverseSlot6);
        }
      } else {
        throw new Exception("The slot value must be between 1 and 6");
      }
    }
  }

  private Color getFeatureColor(String type) throws NullPointerException {
    Color colorToReturn = (Color) FEATURE_COLORS.get(type);
    return colorToReturn;
  }

  private int getFeatureDecoration(String type, int strand)
    throws NullPointerException {
    int decoration = DECORATION_STANDARD;

    if (strand == DIRECT_STRAND) {
      decoration = ((Integer) FEATURE_DECORATIONS_DIRECT.get(type)).intValue();
    } else {
      decoration = ((Integer) FEATURE_DECORATIONS_REVERSE.get(type)).intValue();
    }

    return decoration;
  }

  private void addItemToLegend(String type) throws NullPointerException {
    if (
      !(
        (type.equalsIgnoreCase("forward_gene")) ||
        (type.equalsIgnoreCase("reverse_gene"))
      )
    ) {
      DRAW_LEGEND_ITEMS.put(type, new Boolean(true));
    }
  }

  /**
   * Sets whether or not canvas dimension information should be read from the input file.
   *
   * @param readDimension whether or not to read the canvas dimension from the input file.
   */
  public void setReadDimension(boolean readDimension) {
    this.readDimension = readDimension;
  }

  /**
   * Sets whether or not a warning is shown.
   *
   * @param showWarning whether or not a warning is shown.
   */
  public void setShowWarning(boolean showWarning) {
    this.showWarning = showWarning;
  }

  /**
   * Sets the width of the map. Use this method before calling createCgviewFromURL() or
   * createCgviewFromFile() to specify the default image width.
   *
   * @param width the width of the map.
   */
  public void setWidth(int width) {
    mapWidth = width;
  }

  /**
   * Sets the height of the map. Use this method before calling createCgviewFromURL() or
   * createCgviewFromFile() to specify the default image height.
   *
   * @param height the height of the map.
   */
  public void setHeight(int height) {
    mapHeight = height;
  }

  public void setMapItemSizeAdjustment(float adjustment) {
    mapItemSizeAdjustment = adjustment;
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

  /**
   * Sets the tick density.
   *
   * @param density a double between 0.0 and 1.0, with 1.0 being more dense.
   */
  public void setTickDensity(double density) {
    if (density < 0.0d) {
      density = 0.0d;
    } else if (density > 1.0d) {
      density = 1.0d;
    }

    this.tickDensity = density;
  }

  private void prepareToDraw() {
    Font labelFont;
    Font titleFont;
    Font legendFont;
    Font rulerFont;
    Font messageFont;

    float featureThickness;
    float maxFeatureThickness;
    float backboneThickness;
    float featureSlotSpacing;
    float tickLength;
    float tickThickness;
    float shortTickThickness;
    float labelLineThickness;
    double labelLineLength;
    double minimumFeatureLength = 1.0d;
    double arrowheadLength;

    int giveFeaturePositions;
    int useInnerLabels;
    int maxLabels;

    // make changes to the sizes of map items based on the map size
    // determine size of map compared to default map
    mapSmallest = Math.min(mapWidth, mapHeight);

    int defaultSmallest = Math.min(
      ((Integer) DEFAULT_MAP_SIZES.get("mapWidth")).intValue(),
      ((Integer) DEFAULT_MAP_SIZES.get("mapHeight")).intValue()
    );

    float sizeRatio = (float) mapSmallest / (float) defaultSmallest;

    // do fonts
    int labelFontSize = (int) Math.floor(
      (sizeRatio + mapItemSizeAdjustment) *
      (float) ((Integer) DEFAULT_MAP_SIZES.get("labelFontSize")).intValue() +
      0.5f
    );
    int titleFontSize = (int) Math.floor(
      sizeRatio *
      (float) ((Integer) DEFAULT_MAP_SIZES.get("titleFontSize")).intValue() +
      0.5f
    );
    int legendFontSize = (int) Math.floor(
      sizeRatio *
      (float) ((Integer) DEFAULT_MAP_SIZES.get("legendFontSize")).intValue() +
      0.5f
    );
    int rulerFontSize = (int) Math.floor(
      sizeRatio *
      (float) ((Integer) DEFAULT_MAP_SIZES.get("rulerFontSize")).intValue() +
      0.5f
    );
    int messageFontSize = (int) Math.floor(
      sizeRatio *
      (float) ((Integer) DEFAULT_MAP_SIZES.get("messageFontSize")).intValue() +
      0.5f
    );

    if (
      labelFontSize < ((Integer) MIN_MAP_SIZES.get("labelFontSize")).intValue()
    ) {
      labelFontSize = ((Integer) MIN_MAP_SIZES.get("labelFontSize")).intValue();
    } else if (
      labelFontSize > ((Integer) MAX_MAP_SIZES.get("labelFontSize")).intValue()
    ) {
      labelFontSize = ((Integer) MAX_MAP_SIZES.get("labelFontSize")).intValue();
    }

    if (
      titleFontSize < ((Integer) MIN_MAP_SIZES.get("titleFontSize")).intValue()
    ) {
      titleFontSize = ((Integer) MIN_MAP_SIZES.get("titleFontSize")).intValue();
    } else if (
      titleFontSize > ((Integer) MAX_MAP_SIZES.get("titleFontSize")).intValue()
    ) {
      titleFontSize = ((Integer) MAX_MAP_SIZES.get("titleFontSize")).intValue();
    }

    if (
      legendFontSize <
      ((Integer) MIN_MAP_SIZES.get("legendFontSize")).intValue()
    ) {
      legendFontSize =
        ((Integer) MIN_MAP_SIZES.get("legendFontSize")).intValue();
    } else if (
      legendFontSize >
      ((Integer) MAX_MAP_SIZES.get("legendFontSize")).intValue()
    ) {
      legendFontSize =
        ((Integer) MAX_MAP_SIZES.get("legendFontSize")).intValue();
    }

    if (
      rulerFontSize < ((Integer) MIN_MAP_SIZES.get("rulerFontSize")).intValue()
    ) {
      rulerFontSize = ((Integer) MIN_MAP_SIZES.get("rulerFontSize")).intValue();
    } else if (
      rulerFontSize > ((Integer) MAX_MAP_SIZES.get("rulerFontSize")).intValue()
    ) {
      rulerFontSize = ((Integer) MAX_MAP_SIZES.get("rulerFontSize")).intValue();
    }

    if (
      messageFontSize <
      ((Integer) MIN_MAP_SIZES.get("messageFontSize")).intValue()
    ) {
      messageFontSize =
        ((Integer) MIN_MAP_SIZES.get("messageFontSize")).intValue();
    } else if (
      messageFontSize >
      ((Integer) MAX_MAP_SIZES.get("messageFontSize")).intValue()
    ) {
      messageFontSize =
        ((Integer) MAX_MAP_SIZES.get("messageFontSize")).intValue();
    }

    labelFont = new Font("SansSerif", Font.PLAIN, labelFontSize);
    titleFont = new Font("SansSerif", Font.PLAIN, titleFontSize);
    legendFont = new Font("SansSerif", Font.PLAIN, legendFontSize);
    rulerFont = new Font("SansSerif", Font.PLAIN, rulerFontSize);
    messageFont = new Font("SansSerif", Font.PLAIN, messageFontSize);

    // check for overriding values
    if (this.labelFontSize != -1) {
      labelFont = new Font("SansSerif", Font.PLAIN, this.labelFontSize);
    }
    if (this.rulerFontSize != -1) {
      rulerFont = new Font("SansSerif", Font.PLAIN, this.rulerFontSize);
    }
    if (this.legendFontSize != -1) {
      legendFont = new Font("SansSerif", Font.PLAIN, this.legendFontSize);
    }

    // do int
    maxLabels =
      (int) Math.floor(
        sizeRatio *
        (float) ((Integer) DEFAULT_MAP_SIZES.get("maxLabels")).intValue() +
        0.5f
      );
    if (maxLabels < ((Integer) MIN_MAP_SIZES.get("maxLabels")).intValue()) {
      maxLabels = ((Integer) MIN_MAP_SIZES.get("maxLabels")).intValue();
    } else if (
      maxLabels > ((Integer) MAX_MAP_SIZES.get("maxLabels")).intValue()
    ) {
      maxLabels = ((Integer) MAX_MAP_SIZES.get("maxLabels")).intValue();
    }

    // do float
    featureThickness =
      (sizeRatio + mapItemSizeAdjustment) *
      ((Float) DEFAULT_MAP_SIZES.get("featureThickness")).floatValue();

    // done later
    // 	if (featureThickness < ((Float)MIN_MAP_SIZES.get("featureThickness")).floatValue()) {
    // 	    featureThickness = ((Float)MIN_MAP_SIZES.get("featureThickness")).floatValue();
    // 	}
    // 	else if (featureThickness > ((Float)MAX_MAP_SIZES.get("featureThickness")).floatValue()) {
    // 	    featureThickness = ((Float)MAX_MAP_SIZES.get("featureThickness")).floatValue();
    // 	}

    backboneThickness =
      (sizeRatio + mapItemSizeAdjustment) *
      ((Float) DEFAULT_MAP_SIZES.get("backboneThickness")).floatValue();
    if (
      backboneThickness <
      ((Float) MIN_MAP_SIZES.get("backboneThickness")).floatValue()
    ) {
      backboneThickness =
        ((Float) MIN_MAP_SIZES.get("backboneThickness")).floatValue();
    } else if (
      backboneThickness >
      ((Float) MAX_MAP_SIZES.get("backboneThickness")).floatValue()
    ) {
      backboneThickness =
        ((Float) MAX_MAP_SIZES.get("backboneThickness")).floatValue();
    }

    featureSlotSpacing =
      sizeRatio *
      ((Float) DEFAULT_MAP_SIZES.get("featureSlotSpacing")).floatValue();
    if (
      featureSlotSpacing <
      ((Float) MIN_MAP_SIZES.get("featureSlotSpacing")).floatValue()
    ) {
      featureSlotSpacing =
        ((Float) MIN_MAP_SIZES.get("featureSlotSpacing")).floatValue();
    } else if (
      featureSlotSpacing >
      ((Float) MAX_MAP_SIZES.get("featureSlotSpacing")).floatValue()
    ) {
      featureSlotSpacing =
        ((Float) MAX_MAP_SIZES.get("featureSlotSpacing")).floatValue();
    }

    tickLength =
      sizeRatio * ((Float) DEFAULT_MAP_SIZES.get("tickLength")).floatValue();
    if (tickLength < ((Float) MIN_MAP_SIZES.get("tickLength")).floatValue()) {
      tickLength = ((Float) MIN_MAP_SIZES.get("tickLength")).floatValue();
    } else if (
      tickLength > ((Float) MAX_MAP_SIZES.get("tickLength")).floatValue()
    ) {
      tickLength = ((Float) MAX_MAP_SIZES.get("tickLength")).floatValue();
    }

    tickThickness =
      sizeRatio * ((Float) DEFAULT_MAP_SIZES.get("tickThickness")).floatValue();
    if (
      tickThickness < ((Float) MIN_MAP_SIZES.get("tickThickness")).floatValue()
    ) {
      tickThickness = ((Float) MIN_MAP_SIZES.get("tickThickness")).floatValue();
    } else if (
      tickThickness > ((Float) MAX_MAP_SIZES.get("tickThickness")).floatValue()
    ) {
      tickThickness = ((Float) MAX_MAP_SIZES.get("tickThickness")).floatValue();
    }

    shortTickThickness =
      sizeRatio *
      ((Float) DEFAULT_MAP_SIZES.get("shortTickThickness")).floatValue();
    if (
      shortTickThickness <
      ((Float) MIN_MAP_SIZES.get("shortTickThickness")).floatValue()
    ) {
      shortTickThickness =
        ((Float) MIN_MAP_SIZES.get("shortTickThickness")).floatValue();
    } else if (
      shortTickThickness >
      ((Float) MAX_MAP_SIZES.get("shortTickThickness")).floatValue()
    ) {
      shortTickThickness =
        ((Float) MAX_MAP_SIZES.get("shortTickThickness")).floatValue();
    }

    labelLineThickness =
      sizeRatio *
      ((Float) DEFAULT_MAP_SIZES.get("labelLineThickness")).floatValue();
    if (
      labelLineThickness <
      ((Float) MIN_MAP_SIZES.get("labelLineThickness")).floatValue()
    ) {
      labelLineThickness =
        ((Float) MIN_MAP_SIZES.get("labelLineThickness")).floatValue();
    } else if (
      labelLineThickness >
      ((Float) MAX_MAP_SIZES.get("labelLineThickness")).floatValue()
    ) {
      labelLineThickness =
        ((Float) MAX_MAP_SIZES.get("labelLineThickness")).floatValue();
    }

    // do doubles
    labelLineLength =
      sizeRatio *
      ((Double) DEFAULT_MAP_SIZES.get("labelLineLength")).doubleValue();
    if (
      labelLineLength <
      ((Double) MIN_MAP_SIZES.get("labelLineLength")).doubleValue()
    ) {
      labelLineLength =
        ((Double) MIN_MAP_SIZES.get("labelLineLength")).doubleValue();
    } else if (
      labelLineLength >
      ((Double) MAX_MAP_SIZES.get("labelLineLength")).doubleValue()
    ) {
      labelLineLength =
        ((Double) MAX_MAP_SIZES.get("labelLineLength")).doubleValue();
    }

    arrowheadLength =
      (sizeRatio + mapItemSizeAdjustment) *
      ((Double) DEFAULT_MAP_SIZES.get("arrowheadLength")).doubleValue();
    if (
      arrowheadLength <
      ((Double) MIN_MAP_SIZES.get("arrowheadLength")).doubleValue()
    ) {
      arrowheadLength =
        ((Double) MIN_MAP_SIZES.get("arrowheadLength")).doubleValue();
    } else if (
      arrowheadLength >
      ((Double) MAX_MAP_SIZES.get("arrowheadLength")).doubleValue()
    ) {
      arrowheadLength =
        ((Double) MAX_MAP_SIZES.get("arrowheadLength")).doubleValue();
    }

    // do modal changes
    if (mapSmallest <= SMALL_MAP_MODE_DIMENSION) {
      giveFeaturePositions =
        ((Integer) SMALL_MAP_MODES.get("giveFeaturePositions")).intValue();
      useInnerLabels =
        ((Integer) SMALL_MAP_MODES.get("useInnerLabels")).intValue();
    } else if (mapSmallest >= LARGE_MAP_MODE_DIMENSION) {
      giveFeaturePositions =
        ((Integer) LARGE_MAP_MODES.get("giveFeaturePositions")).intValue();
      useInnerLabels =
        ((Integer) LARGE_MAP_MODES.get("useInnerLabels")).intValue();
    } else {
      giveFeaturePositions =
        ((Integer) DEFAULT_MAP_MODES.get("giveFeaturePositions")).intValue();
      useInnerLabels =
        ((Integer) DEFAULT_MAP_MODES.get("useInnerLabels")).intValue();
    }

    // adjust feature thickness based on how many slots are used
    float growthIncrement = 0.083f * featureThickness; // 1/12 = 0.083
    int emptySlots = 0;

    if (forwardSlot1 == null) {
      emptySlots++;
    }
    if (forwardSlot2 == null) {
      emptySlots++;
    }
    if (forwardSlot3 == null) {
      emptySlots++;
    }
    if (forwardSlot4 == null) {
      emptySlots++;
    }
    if (forwardSlot5 == null) {
      emptySlots++;
    }
    if (forwardSlot6 == null) {
      emptySlots++;
    }
    if (reverseSlot1 == null) {
      emptySlots++;
    }
    if (reverseSlot2 == null) {
      emptySlots++;
    }
    if (reverseSlot3 == null) {
      emptySlots++;
    }
    if (reverseSlot4 == null) {
      emptySlots++;
    }
    if (reverseSlot5 == null) {
      emptySlots++;
    }
    if (reverseSlot6 == null) {
      emptySlots++;
    }

    featureThickness = featureThickness + (float) emptySlots * growthIncrement;

    if (
      featureThickness <
      ((Float) MIN_MAP_SIZES.get("featureThickness")).floatValue()
    ) {
      featureThickness =
        ((Float) MIN_MAP_SIZES.get("featureThickness")).floatValue();
    } else if (
      featureThickness >
      ((Float) MAX_MAP_SIZES.get("featureThickness")).floatValue()
    ) {
      featureThickness =
        ((Float) MAX_MAP_SIZES.get("featureThickness")).floatValue();
    }

    if (forwardSlot1 != null) {
      forwardSlot1.sortFeaturesByStart();
      forwardSlot1.setCgview(cgview);
      forwardSlot1.setFeatureThickness(featureThickness);
    }
    if (forwardSlot2 != null) {
      forwardSlot2.sortFeaturesByStart();
      forwardSlot2.setCgview(cgview);
      forwardSlot2.setFeatureThickness(featureThickness);
    }
    if (forwardSlot3 != null) {
      forwardSlot3.sortFeaturesByStart();
      forwardSlot3.setCgview(cgview);
      forwardSlot3.setFeatureThickness(featureThickness);
    }
    if (forwardSlot4 != null) {
      forwardSlot4.sortFeaturesByStart();
      forwardSlot4.setCgview(cgview);
      forwardSlot4.setFeatureThickness(featureThickness);
    }
    if (forwardSlot5 != null) {
      forwardSlot5.sortFeaturesByStart();
      forwardSlot5.setCgview(cgview);
      forwardSlot5.setFeatureThickness(featureThickness);
    }
    if (forwardSlot6 != null) {
      forwardSlot6.sortFeaturesByStart();
      forwardSlot6.setCgview(cgview);
      forwardSlot6.setFeatureThickness(featureThickness);
    }

    if (reverseSlot1 != null) {
      reverseSlot1.sortFeaturesByStart();
      reverseSlot1.setCgview(cgview);
      reverseSlot1.setFeatureThickness(featureThickness);
    }
    if (reverseSlot2 != null) {
      reverseSlot2.sortFeaturesByStart();
      reverseSlot2.setCgview(cgview);
      reverseSlot2.setFeatureThickness(featureThickness);
    }
    if (reverseSlot3 != null) {
      reverseSlot3.sortFeaturesByStart();
      reverseSlot3.setCgview(cgview);
      reverseSlot3.setFeatureThickness(featureThickness);
    }
    if (reverseSlot4 != null) {
      reverseSlot4.sortFeaturesByStart();
      reverseSlot4.setCgview(cgview);
      reverseSlot4.setFeatureThickness(featureThickness);
    }
    if (reverseSlot5 != null) {
      reverseSlot5.sortFeaturesByStart();
      reverseSlot5.setCgview(cgview);
      reverseSlot5.setFeatureThickness(featureThickness);
    }
    if (reverseSlot6 != null) {
      reverseSlot6.sortFeaturesByStart();
      reverseSlot6.setCgview(cgview);
      reverseSlot6.setFeatureThickness(featureThickness);
    }

    cgview.setWidth(mapWidth);
    cgview.setHeight(mapHeight);
    cgview.setLabelsToKeep(maxLabels);
    cgview.setDrawTickMarks(drawTickMarks);
    cgview.setTitleFont(titleFont);
    cgview.setLabelFont(labelFont);
    cgview.setFeatureThickness(featureThickness);
    cgview.setBackboneThickness(backboneThickness);
    cgview.setFeatureSlotSpacing(featureSlotSpacing);
    cgview.setMinimumFeatureLength(minimumFeatureLength);
    cgview.setLegendFont(legendFont);
    cgview.setTickLength(tickLength);
    cgview.setTickThickness(tickThickness);
    cgview.setShortTickThickness(shortTickThickness);
    cgview.setLabelLineThickness(labelLineThickness);
    cgview.setLabelLineLength(labelLineLength);
    cgview.setLabelPlacementQuality(labelPlacementQuality);
    cgview.setUseColoredLabelBackgrounds(useColoredLabelBackground);
    cgview.setShowBorder(showBorder);
    cgview.setShowShading(showShading);
    cgview.setGiveFeaturePositions(giveFeaturePositions);
    cgview.setShadingProportion(shadingProportion);
    cgview.setUseInnerLabels(useInnerLabels);
    cgview.setMoveInnerLabelsToOuter(moveInnerLabelsToOuter);
    cgview.setWarningFont(rulerFont);
    cgview.setRulerFont(rulerFont);
    cgview.setArrowheadLength(arrowheadLength);
    cgview.setHighlightOpacity(shadingOpacity);
    cgview.setShadowOpacity(shadingOpacity);

    cgview.setTickDensity(tickDensity);

    // if not drawing labels, don't show message.
    if (!(showLabels)) {
      cgview.setShowWarning(false);
    }

    // set backboneRadius based on smallest image dimension
    cgview.setSequenceLength(length);
    mapSmallest = Math.min(mapWidth, mapHeight);
    cgview.setBackboneRadius(0.50d * (double) mapSmallest / 1.9d);

    // check coloredLabels
    if (!(useColoredLabels)) {
      cgview.setGlobalLabelColor((Color) MAP_ITEM_COLORS.get("titleFont"));
    }

    // set map item colors
    cgview.setLongTickColor((Color) MAP_ITEM_COLORS.get("tick"));
    cgview.setShortTickColor((Color) MAP_ITEM_COLORS.get("partialTick"));
    cgview.setZeroTickColor((Color) MAP_ITEM_COLORS.get("zeroLine"));
    cgview.setRulerFontColor((Color) MAP_ITEM_COLORS.get("rulerFont"));
    cgview.setBackboneColor((Color) MAP_ITEM_COLORS.get("backbone"));
    cgview.setTitleFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));
    cgview.setWarningFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));
    cgview.setBackgroundColor((Color) MAP_ITEM_COLORS.get("background"));

    // build legend
    if (showLegend) {
      // create legend
      legend = new Legend(cgview);
      legend.setAllowLabelClash(allowLabelClashLegend);
      legend.setBackgroundOpacity(0.5f);
      legend.setFont(legendFont);
      // legend.setFont(labelFont);
      legend.setPosition(legendPosition);

      legend.setBackgroundColor((Color) MAP_ITEM_COLORS.get("background"));
      legend.setFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));

      LegendItem legendItem;

      Enumeration legendEntries = DRAW_LEGEND_ITEMS.keys();
      ArrayList list = new ArrayList();
      while (legendEntries.hasMoreElements()) {
        list.add(legendEntries.nextElement());
      }
      Collections.sort(list);

      list.add(0, "reverse_gene");
      list.add(0, "forward_gene");

      Iterator i = list.iterator();

      while (i.hasNext()) {
        String key = (String) i.next();
        legendItem = new LegendItem(legend);
        legendItem.setLabel((String) LEGEND_ITEM_NAMES.get(key));
        legendItem.setSwatchColor((Color) FEATURE_COLORS.get(key));
        legendItem.setDrawSwatch(SWATCH_SHOW);
      }
    }

    // send settings to cgview
    if (showTitle) {
      if (title.length() > MAX_TITLE_LENGTH) {
        title = title.substring(0, MAX_TITLE_LENGTH) + "...";
      }
      legend = new Legend(cgview);
      legend.setAllowLabelClash(allowLabelClashLegend);
      legend.setBackgroundOpacity(0.5f);
      legend.setFont(legendFont);
      legend.setPosition(LEGEND_LOWER_CENTER);
      LegendItem legendItem = new LegendItem(legend);
      legendItem.setLabel(title);
      legendItem.setDrawSwatch(SWATCH_NO_SHOW);
    }
  }
}
