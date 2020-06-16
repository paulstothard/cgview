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
import java.awt.geom.*;
import java.io.*;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.ArrayList;
import java.util.regex.*;

/**
 * This class reads a tab-delimited feature file and creates a Cgview object.
 *
 * @author Paul Stothard
 */
public class CgviewFactoryPtt implements CgviewConstants {
  private Cgview cgview;

  private Hashtable COLORS = new Hashtable();
  private Hashtable MAP_ITEM_COLORS = new Hashtable();
  private Hashtable FEATURE_COLORS = new Hashtable();
  private Hashtable FEATURE_DECORATIONS_DIRECT = new Hashtable();
  private Hashtable FEATURE_DECORATIONS_REVERSE = new Hashtable();
  private Hashtable LEGEND_ITEM_NAMES_LONG = new Hashtable();
  private Hashtable LEGEND_ITEM_NAMES_SHORT = new Hashtable();
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
  private int MAX_TITLE_LENGTH = 80;
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
  private boolean showLabels = true;
  private float shadingProportion = 0.4f;
  private float shadingOpacity = 0.5f;

  private boolean containsCogs = false;

  private String ncbiLink =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Text&db=Protein&dopt=genpept&dispmax=20&uid=";

  /** Constructs a new CgviewFactoryPtt object. */
  public CgviewFactoryPtt() {
    cgview = new Cgview(1);
  }

  /**
   * Generates a Cgview object from an tab-delimited feature file.
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
   * Generates a Cgview object from an tab-delimited feature file.
   *
   * @param url the URL of the tab-delimited file to read.
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

    FEATURE_COLORS.put("forward_gene", new Color(249, 0, 0));
    FEATURE_COLORS.put("reverse_gene", new Color(19, 19, 255));
    FEATURE_COLORS.put("J", new Color(152, 0, 0));
    FEATURE_COLORS.put("K", new Color(255, 175, 100));
    FEATURE_COLORS.put("L", new Color(245, 222, 188));
    FEATURE_COLORS.put("D", new Color(51, 255, 153));
    FEATURE_COLORS.put("O", new Color(150, 199, 35));
    FEATURE_COLORS.put("M", new Color(240, 245, 60));
    FEATURE_COLORS.put("N", new Color(62, 217, 157));
    FEATURE_COLORS.put("P", new Color(185, 255, 134));
    FEATURE_COLORS.put("T", new Color(0, 128, 0));
    FEATURE_COLORS.put("C", new Color(185, 74, 125));
    FEATURE_COLORS.put("G", new Color(152, 26, 206));
    FEATURE_COLORS.put("E", new Color(255, 79, 255));
    FEATURE_COLORS.put("F", new Color(255, 204, 204));
    FEATURE_COLORS.put("H", new Color(179, 225, 234));
    FEATURE_COLORS.put("I", new Color(100, 255, 250));
    FEATURE_COLORS.put("Q", new Color(0, 0, 139));
    FEATURE_COLORS.put("R", new Color(179, 179, 179));
    FEATURE_COLORS.put("S", new Color(240, 240, 240));
    FEATURE_COLORS.put("no_cog", new Color(96, 96, 96));

    FEATURE_DECORATIONS_DIRECT.put(
      "forward_gene",
      new Integer(DECORATION_CLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_DIRECT.put("J", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("K", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("L", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("D", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("O", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("M", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("N", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("P", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("T", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("C", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("G", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("E", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("F", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("H", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("I", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("Q", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("R", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("S", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_DIRECT.put("no_cog", new Integer(DECORATION_STANDARD));

    FEATURE_DECORATIONS_REVERSE.put(
      "reverse_gene",
      new Integer(DECORATION_COUNTERCLOCKWISE_ARROW)
    );
    FEATURE_DECORATIONS_REVERSE.put("J", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("K", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("L", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("D", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("O", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("M", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("N", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("P", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("T", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("C", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("G", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("E", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("F", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("H", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("I", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("Q", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("R", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("S", new Integer(DECORATION_STANDARD));
    FEATURE_DECORATIONS_REVERSE.put("no_cog", new Integer(DECORATION_STANDARD));

    LEGEND_ITEM_NAMES_LONG.put("forward_gene", "Forward strand gene");
    LEGEND_ITEM_NAMES_LONG.put("reverse_gene", "Reverse strand gene");
    LEGEND_ITEM_NAMES_LONG.put(
      "J",
      "Translation, ribosomal structure and biogenesis"
    );
    LEGEND_ITEM_NAMES_LONG.put("K", "Transcription");
    LEGEND_ITEM_NAMES_LONG.put(
      "L",
      "DNA replication, recombination and repair"
    );
    LEGEND_ITEM_NAMES_LONG.put(
      "D",
      "Cell division and chromosome partitioning"
    );
    LEGEND_ITEM_NAMES_LONG.put(
      "O",
      "Posttranslational modification, protein turnover, chaperones"
    );
    LEGEND_ITEM_NAMES_LONG.put("M", "Cell envelope biogenesis, outer membrane");
    LEGEND_ITEM_NAMES_LONG.put("N", "Cell motility and secretion");
    LEGEND_ITEM_NAMES_LONG.put("P", "Inorganic ion transport and metabolism");
    LEGEND_ITEM_NAMES_LONG.put("T", "Signal transduction mechanisms");
    LEGEND_ITEM_NAMES_LONG.put("C", "Energy production and conversion");
    LEGEND_ITEM_NAMES_LONG.put("G", "Carbohydrate transport and metabolism");
    LEGEND_ITEM_NAMES_LONG.put("E", "Amino acid transport and metabolism");
    LEGEND_ITEM_NAMES_LONG.put("F", "Nucleotide transport and metabolism");
    LEGEND_ITEM_NAMES_LONG.put("H", "Coenzyme metabolism");
    LEGEND_ITEM_NAMES_LONG.put("I", "Lipid metabolism");
    LEGEND_ITEM_NAMES_LONG.put(
      "Q",
      "Secondary metabolites biosynthesis, transport and catabolism"
    );
    LEGEND_ITEM_NAMES_LONG.put("R", "General function prediction only");
    LEGEND_ITEM_NAMES_LONG.put("S", "Function unknown");
    LEGEND_ITEM_NAMES_LONG.put("no_cog", "No COG information");

    LEGEND_ITEM_NAMES_SHORT.put("forward_gene", "Forward");
    LEGEND_ITEM_NAMES_SHORT.put("reverse_gene", "Reverse");
    LEGEND_ITEM_NAMES_SHORT.put("J", "COG J");
    LEGEND_ITEM_NAMES_SHORT.put("K", "COG K");
    LEGEND_ITEM_NAMES_SHORT.put("L", "COG L");
    LEGEND_ITEM_NAMES_SHORT.put("D", "COG D");
    LEGEND_ITEM_NAMES_SHORT.put("O", "COG O");
    LEGEND_ITEM_NAMES_SHORT.put("M", "COG M");
    LEGEND_ITEM_NAMES_SHORT.put("N", "COG N");
    LEGEND_ITEM_NAMES_SHORT.put("P", "COG P");
    LEGEND_ITEM_NAMES_SHORT.put("T", "COG T");
    LEGEND_ITEM_NAMES_SHORT.put("C", "COG C");
    LEGEND_ITEM_NAMES_SHORT.put("G", "COG G");
    LEGEND_ITEM_NAMES_SHORT.put("E", "COG E");
    LEGEND_ITEM_NAMES_SHORT.put("F", "COG F");
    LEGEND_ITEM_NAMES_SHORT.put("H", "COG H");
    LEGEND_ITEM_NAMES_SHORT.put("I", "COG I");
    LEGEND_ITEM_NAMES_SHORT.put("Q", "COG Q");
    LEGEND_ITEM_NAMES_SHORT.put("R", "COG R");
    LEGEND_ITEM_NAMES_SHORT.put("S", "COG S");
    LEGEND_ITEM_NAMES_SHORT.put("no_cog", "No COG");

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

    // intermediate values
    String location;
    String strand;
    String length;
    String pid;
    String gene;
    String synonym;
    String code;
    String cog;
    String product;

    String strandB;
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

    boolean hasLocationColumn = false;
    boolean hasStrandColumn = false;
    boolean hasLengthColumn = false;
    boolean hasPIDColumn = false;
    boolean hasGeneColumn = false;
    boolean hasSynonymColumn = false;
    boolean hasCodeColumn = false;
    boolean hasCOGColumn = false;
    boolean hasProductColumn = false;

    int locationColumnIndex = -1;
    int strandColumnIndex = -1;
    int lengthColumnIndex = -1;
    int pidColumnIndex = -1;
    int geneColumnIndex = -1;
    int synonymColumnIndex = -1;
    int codeColumnIndex = -1;
    int cogColumnIndex = -1;
    int productColumnIndex = -1;

    // pattern to find line with length value
    Pattern pLength = Pattern.compile("[01]\\.\\.(\\d+)\\s*$");
    Matcher m;

    // pattern to find column names
    Pattern pColumn = Pattern.compile("Location\\s*\\t+\\s*Strand");

    // pattern to find start and stop
    Pattern pStart = Pattern.compile("(\\d+)\\.\\.(\\d+)");

    in = url.openStream();

    System.out.println("Parsing ptt input.");

    buf = new BufferedReader(new InputStreamReader(in));
    while ((line = buf.readLine()) != null) {
      lineCount++;
      // System.out.println ("doing line " + line);
      // try to read length and title from first few lines of ptt file.
      // usually first line, sometimes second line or third line
      // usually looks something like:
      // Acinetobacter sp. ADP1, complete geneome - 0..3598621

      // look for title and DNA length
      if (lineCount <= 5) {
        m = pLength.matcher(line);
        if (m.find()) {
          title = line;
          try {
            this.length = Integer.parseInt(m.group(1));
          } catch (Exception e) {
            throw new Exception(
              "There is a problem with the length value on line " +
              lineCount +
              " in the data file."
            );
          }
        }
      }

      // look for column names
      if ((lineCount <= 8) && (!hasLocationColumn)) {
        m = pColumn.matcher(line);
        if (m.find()) {
          lineItems = line.split("(?:\\s*\\t+\\s*)|(?:\\s{2,})");
          // lineItems = line.split("\\s*\\t+\\s*");
          columnNumber = lineItems.length;
          for (int i = 0; i < lineItems.length; i = i + 1) {
            String lineItem = lineItems[i].trim();
            if (lineItem.equalsIgnoreCase("Location")) {
              hasLocationColumn = true;
              locationColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("Strand")) {
              hasStrandColumn = true;
              strandColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("Length")) {
              hasLengthColumn = true;
              lengthColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("PID")) {
              hasPIDColumn = true;
              pidColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("Gene")) {
              hasGeneColumn = true;
              geneColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("Synonym")) {
              hasSynonymColumn = true;
              synonymColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("Code")) {
              hasCodeColumn = true;
              codeColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("COG")) {
              hasCOGColumn = true;
              cogColumnIndex = i;
            } else if (lineItem.equalsIgnoreCase("Product")) {
              hasProductColumn = true;
              productColumnIndex = i;
            }
          }
        }
        continue;
      }

      if (columnNumber > 0) {
        // some text editors may use spaces instead of tabs.
        // try to handle this.
        lineItems = line.split("\\s*\\t+\\s*");
        if (lineItems.length != columnNumber) {
          lineItems = line.split("(?:\\s*\\t+\\s*)|(?:\\s{2,})");
        }

        if (lineItems.length >= 5) {
          if (hasLocationColumn == false) {
            throw new Exception(
              "A \"Location\" column has not been defined in the data file."
            );
          }
          if (hasStrandColumn == false) {
            throw new Exception(
              "A \"Strand\" column has not been defined in the data file."
            );
          }
          if (hasPIDColumn == false) {
            throw new Exception(
              "A \"PID\" column has not been defined in the data file."
            );
          }

          // location
          try {
            location = lineItems[locationColumnIndex];
            location = location.trim();
          } catch (Exception e) {
            continue;
            // throw new Exception("There is a problem with line " + lineCount + " in the data
            // file.");
          }

          // strand
          try {
            strand = lineItems[strandColumnIndex];
            strand = strand.trim();
          } catch (Exception e) {
            continue;
            // throw new Exception("There is a problem with line " + lineCount + " in the data
            // file.");
          }

          // pid
          try {
            pid = lineItems[pidColumnIndex];
            pid = pid.trim();
          } catch (Exception e) {
            continue;
            // throw new Exception("There is a problem with line " + lineCount + " in the data
            // file.");
          }

          // length
          if (hasLengthColumn) {
            try {
              length = lineItems[lengthColumnIndex];
              length = length.trim();
            } catch (Exception e) {
              continue;
              // throw new Exception("There is a problem with line " + lineCount + " in the data
              // file.");
            }
          } else {
            length = "";
          }

          // gene
          if (hasGeneColumn) {
            try {
              gene = lineItems[geneColumnIndex];
              gene = gene.trim();
            } catch (Exception e) {
              continue;
              // throw new Exception("There is a problem with line " + lineCount + " in the data
              // file.");
            }
          } else {
            gene = "";
          }

          // synonym
          if (hasSynonymColumn) {
            try {
              synonym = lineItems[synonymColumnIndex];
              synonym = synonym.trim();
            } catch (Exception e) {
              continue;
              // throw new Exception("There is a problem with line " + lineCount + " in the data
              // file.");
            }
          } else {
            synonym = "";
          }

          // code
          if (hasCodeColumn) {
            try {
              code = lineItems[codeColumnIndex];
              code = code.trim();
            } catch (Exception e) {
              continue;
              // throw new Exception("There is a problem with line " + lineCount + " in the data
              // file.");
            }
          } else {
            code = "";
          }

          // cog
          if (hasCOGColumn) {
            try {
              cog = lineItems[cogColumnIndex];
              cog = cog.trim();
            } catch (Exception e) {
              continue;
              // throw new Exception("There is a problem with line " + lineCount + " in the data
              // file.");
            }
          } else {
            cog = "";
          }

          // product
          if (hasProductColumn) {
            try {
              product = lineItems[productColumnIndex];
              product = product.trim();
            } catch (Exception e) {
              continue;
              // throw new Exception("There is a problem with line " + lineCount + " in the data
              // file.");
            }
          } else {
            product = "";
          }

          // build start and stop
          m = pStart.matcher(location);
          if (m.find()) {
            start = Integer.parseInt(m.group(1));
            stop = Integer.parseInt(m.group(2));
          } else {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          // build slot, strandB,  and type
          if (strand.equalsIgnoreCase("+")) {
            slot = 1;
            type = "forward_gene";
            strandB = "forward";
          } else if (strand.equalsIgnoreCase("-")) {
            slot = 1;
            type = "reverse_gene";
            strandB = "reverse";
          } else {
            throw new Exception(
              "There is a problem with line " + lineCount + " in the data file."
            );
          }

          // build label
          if ((!(gene.matches("^\\s*$"))) && (!(gene.equals("-")))) {
            label = gene;
          } else if (
            (!(synonym.matches("^\\s*$"))) && (!(synonym.equals("-")))
          ) {
            label = synonym;
          } else if ((!(pid.matches("^\\s*$"))) && (!(pid.equals("-")))) {
            label = pid;
          }

          // build mouseover
          mouseover = location + ";";

          if ((!(cog.matches("^\\s*$"))) && (!(cog.equals("-")))) {
            mouseover = mouseover + " " + cog + ";";
          }

          if ((!(code.matches("^\\s*$"))) && (!(code.equals("-")))) {
            mouseover = mouseover + " (code=" + code + ");";
          }

          if ((!(product.matches("^\\s*$"))) && (!(product.equals("-")))) {
            mouseover = mouseover + " " + product + ";";
          }

          // build hyperlink
          if ((!(pid.matches("^\\s*$"))) && (!(pid.equals("-")))) {
            hyperlink = ncbiLink + pid;
          }

          // add the gene feature
          try {
            addFeature(
              strandB,
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

          // create COG feature
          if ((!(code.matches("^\\s*$"))) && (!(code.equals("-")))) {
            slot = 2;
            label = "";
            mouseover = "";
            hyperlink = "";
            type = code;
            containsCogs = true;
            try {
              addFeature(
                strandB,
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

    if (this.length > MAX_SEQUENCE_LENGTH) {
      throw new Exception(
        "Maximum sequence length is " + MAX_SEQUENCE_LENGTH + "."
      );
    } else if (this.length < 1) {
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
   *     predicted_gene, sequence_similarity, score, primer.
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
    if (strand.equalsIgnoreCase("forward")) {
      intStrand = DIRECT_STRAND;
    } else if (strand.equalsIgnoreCase("reverse")) {
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
      System.err.println(
        "Warning: feature type \"" +
        type +
        "\" was not recognized. Feature skipped."
      );
      return;
      // throw new Exception ("The feature type \"" + type + "\" was not recognized.");
    }

    // obtain the decoration
    try {
      decoration = getFeatureDecoration(type, intStrand);
    } catch (NullPointerException e) {
      System.err.println(
        "Warning: feature type \"" +
        type +
        "\" was not recognized. Feature skipped."
      );
      return;
      // throw new Exception ("The feature type \"" + type + "\" was not recognized.");
    }

    // add item to the legend
    try {
      addItemToLegend(type);
    } catch (NullPointerException e) {
      System.err.println(
        "Warning: feature type \"" +
        type +
        "\" was not recognized. Feature skipped."
      );
      return;
      // throw new Exception ("The feature type \"" + type + "\" was not recognized.");
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

      // legend.setFont(labelFont);

      legend.setFont(legendFont);

      legend.setPosition(legendPosition);

      legend.setBackgroundColor((Color) MAP_ITEM_COLORS.get("background"));
      legend.setFontColor((Color) MAP_ITEM_COLORS.get("titleFont"));

      LegendItem legendItem;

      // legendItem = new LegendItem(legend);
      // legendItem.setLabel("Feature legend");
      // legendItem.setDrawSwatch(SWATCH_NO_SHOW);

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
        if (mapSmallest >= LARGE_MAP_MODE_DIMENSION) {
          legendItem.setLabel((String) LEGEND_ITEM_NAMES_LONG.get(key));
        } else {
          legendItem.setLabel((String) LEGEND_ITEM_NAMES_SHORT.get(key));
        }
        legendItem.setSwatchColor((Color) FEATURE_COLORS.get(key));
        legendItem.setDrawSwatch(SWATCH_SHOW);
      }
    }

    // add title legend
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
