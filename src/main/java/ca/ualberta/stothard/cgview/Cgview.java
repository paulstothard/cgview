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
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;

/**
 * Represents a circular map of a DNA sequence. Numerous methods are available for controlling the
 * overall appearance of the map (width, height, background color, etc). The contents of the map are
 * described in terms of {@link FeatureSlot}, {@link Feature}, {@link FeatureRange}, {@link Legend},
 * and {@link LegendItem} objects.
 *
 * @author Paul Stothard
 */
public class Cgview implements CgviewConstants {
  private Graphics2D gg;

  private int sequenceLength;
  private int width = 700;
  private int height = 700;
  private int smallestDimension = 700;
  private Color backgroundColor = new Color(255, 255, 255); // white

  private double backboneRadius = 190.0d;
  private float backboneThickness = 5.0f;
  private Color backboneColor = new Color(128, 128, 128); // gray

  private double origin = 90.0d;

  private float featureThickness = 8.0f;
  private double featureSlotSpacing = 4.0d;
  private double arrowheadLength = 5.0d;
  private boolean shiftSmallFeatures = true;
  private double minimumFeatureLength = 0.02d;

  private boolean showShading = true;
  private float shadingProportion = 0.2f;
  private float highlightOpacity = 0.3f;
  private float shadowOpacity = 0.3f;

  // private int desiredNumberOfTicks = 30;
  private int desiredNumberOfTicks = 25;
  private float tickThickness = 2.0f;
  private Color longTickColor = new Color(0, 0, 0); // black
  private Color shortTickColor = new Color(0, 0, 0); // black
  private float shortTickThickness = 2.0f;
  private Color zeroTickColor = new Color(0, 0, 0); // black
  private float tickLength = 7.0f;
  private boolean drawTickMarks = true;
  private double tickDensity = 1.0d;

  private int rulerUnits = BASES;
  private Font rulerFont = new Font("SansSerif", Font.PLAIN, 8);
  private double rulerTextPadding = 10.0d;
  private Color rulerFontColor = new Color(0, 0, 0); // black

  private String title = "";
  private Font titleFont = new Font("SansSerif", Font.PLAIN, 12);
  private Color titleFontColor = new Color(0, 0, 0); // black

  private boolean drawLegends = true;
  private Font legendFont = new Font("SansSerif", Font.PLAIN, 8);
  private Color legendTextColor = new Color(0, 0, 0); // black

  private Font labelFont = new Font("SansSerif", Font.PLAIN, 10);
  private int globalLabel = LABEL;
  private Color globalLabelColor;
  private float labelLineThickness = 1.0f;
  private double labelLineLength = 50.0d;
  private double zoomShift = 1.01d;
  private int useInnerLabels = INNER_LABELS_AUTO;
  private boolean moveInnerLabelsToOuter = true;
  private int giveFeaturePositions = POSITIONS_NO_SHOW;
  private int labelsToKeep = 8000;
  private boolean labelShuffle = true;
  private boolean useColoredLabelBackgrounds = false;
  private int clashSpan = 100;
  private int spreadIterations = 100;
  private double radiusShiftAmount = 10.0d;
  private double radiansShiftConstant = 0.20d;
  private int labelPlacementQuality = 8;
  private boolean keepLastLabels = false;

  private boolean isLinear = false;
  private String linearBreakText = "3'   5'";
  private double zigzagWidth = 0.0d;

  private ArrayList featureSlots = new ArrayList();
  private ArrayList outerLabels = new ArrayList();
  private ArrayList innerLabels = new ArrayList();
  private ArrayList legends = new ArrayList();
  private Font warningFont = new Font("SansSerif", Font.PLAIN, 8);
  private Color warningFontColor = new Color(0, 0, 0); // black
  private String warningText = "";
  private boolean showWarning = false;

  private Point2D centerPoint;

  private int totalLabels = 0;
  private int clashLabels = 0;
  // these values are used for zooming and label placement
  private boolean drawEntirePlasmid = true;
  private int zoomRangeOneStart;
  private int zoomRangeOneStop;
  private int zoomRangeTwoStart;
  private int zoomRangeTwoStop;
  private int centerBase;
  private double zoomMultiplier = 1.0d;
  private double virtualZoomMultiplier = 1.0d;
  private double virtualBackboneRadius = backboneRadius;
  private Rectangle2D backgroundRectangle;
  private Rectangle2D titleRectangle;
  private Rectangle2D lengthRectangle;
  private Arc2D outerArc;
  private Arc2D innerArc;

  private double desiredZoom = 1.0d;
  private int desiredZoomCenter = 1;

  private Color borderColor = new Color(0, 0, 0); // black
  private boolean showBorder = true;

  private ArrayList labelBounds = new ArrayList();

  private Legend infoLegend;

  // some limits
  private int MAX_DNA_LENGTH = 200000000;
  // private double ZOOM_MULTIPLIER_MAX = 30000.0d;
  private double ZOOM_MULTIPLIER_MAX = 30.0d;
  private double VIRTUAL_ZOOM_MULTIPLIER_MAX = 500000.0d;

  /**
   * Constructs a new Cgview object.
   *
   * @param sequenceLength the length of the sequence to be mapped.
   */
  public Cgview(int sequenceLength) {
    setSequenceLength(sequenceLength);
  }

  /**
   * Sets the length of the sequence to be mapped.
   *
   * @param bases the sequence length.
   */
  public void setSequenceLength(int bases) {
    if (bases < 0) {
      bases = 0;
    } else if (bases > MAX_DNA_LENGTH) {
      // generate an error
    }
    sequenceLength = bases;
  }

  /**
   * Returns the length of the sequence to be mapped.
   *
   * @return the length of the sequence.
   */
  public int getSequenceLength() {
    return sequenceLength;
  }

  /**
   * Returns the Graphics2D object for this Cgview.
   *
   * @return the Graphics2D object.
   */
  protected Graphics2D getGraphics() {
    return gg;
  }

  /**
   * Sets the height of the map.
   *
   * @param height the height of the map.
   */
  public void setHeight(int height) {
    if (height < 0) {
      height = 0;
    }
    this.height = height;
  }

  /**
   * Returns the height of the map.
   *
   * @return the height of the map.
   */
  public int getHeight() {
    return height;
  }

  /**
   * Adjusts the tick density, with 0 being the minimum, 1 being the maximum.
   *
   * @param density a double between 0.0 and 1.0.
   */
  public void setTickDensity(double density) {
    if (density < 0.0d) {
      density = 0.0d;
    } else if (density > 1.0d) {
      density = 1.0d;
    }
    this.tickDensity = density;
  }

  /**
   * Returns the tick density, with 0 being the minimum, 1 being the maximum.
   *
   * @return a double between 0.0 and 1.0 representing the tick density.
   */
  public double getTickDensity() {
    return tickDensity;
  }

  /**
   * Sets the width of the map.
   *
   * @param width the width of the map.
   */
  public void setWidth(int width) {
    if (width < 0) {
      width = 0;
    }
    this.width = width;
  }

  /**
   * Returns the width of the map.
   *
   * @return the width of the map.
   */
  public int getWidth() {
    return width;
  }

  /**
   * Returns the width or the height of the map, whichever is smaller.
   *
   * @return the width or the height of the map, whichever is smaller.
   */
  public int getSmallestDimension() {
    return Math.min(width, height);
  }

  /**
   * Sets the background color of the map.
   *
   * @param color the background color.
   */
  public void setBackgroundColor(Color color) {
    backgroundColor = color;
  }

  /**
   * Returns the background color of the map.
   *
   * @return the background color.
   */
  public Color getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Sets the color of the map border.
   *
   * @param color the border color.
   */
  public void setBorderColor(Color color) {
    borderColor = color;
  }

  /**
   * Returns the color of the map border.
   *
   * @return the border color.
   */
  public Color getBorderColor() {
    return borderColor;
  }

  /**
   * Sets the radius of the circle that represents the DNA sequence.
   *
   * @param radius the backbone radius.
   */
  public void setBackboneRadius(double radius) {
    if (radius < 0) {
      radius = 0;
    }
    backboneRadius = radius;
  }

  /**
   * Returns the radius of the circle that represents the DNA sequence.
   *
   * @return the backbone radius.
   */
  public double getBackboneRadius() {
    return backboneRadius;
  }

  /**
   * Sets the thickness of the line used to draw the circle that represents the DNA sequence.
   *
   * @param thickness the thickness of the line.
   */
  public void setBackboneThickness(float thickness) {
    if (thickness < 0) {
      thickness = 0.0f;
    }
    backboneThickness = thickness;
  }

  /**
   * Returns the thickness of the line used to draw the circle that represents the DNA sequence.
   *
   * @return the thickness of the line.
   */
  public double getBackboneThickness() {
    return backboneThickness;
  }

  /**
   * Sets the color of the text used for the sequence ruler.
   *
   * @param color the font color.
   */
  public void setRulerFontColor(Color color) {
    rulerFontColor = color;
  }

  /**
   * Returns the color of the text used for the sequence ruler.
   *
   * @return the font color.
   */
  public Color getRulerFontColor() {
    return rulerFontColor;
  }

  /**
   * Sets the color of the text used for the sequence title.
   *
   * @param color the color of the font.
   */
  public void setTitleFontColor(Color color) {
    titleFontColor = color;
  }

  /**
   * Returns the color of the text used for the sequence title.
   *
   * @return the font color.
   */
  public Color getTitleFontColor() {
    return titleFontColor;
  }

  /**
   * Returns the default color of the text used for {@link Legend} objects.
   *
   * @return the font color.
   */
  public Color getLegendTextColor() {
    return legendTextColor;
  }

  /**
   * Sets the default color of the text used for {@link Legend} objects.
   *
   * @param color the font color.
   */
  public void setLegendTextColor(Color color) {
    legendTextColor = color;
  }

  /**
   * Sets the color of the text used for warnings that appear at the bottom of the figure.
   *
   * @param color the font color.
   */
  public void setWarningFontColor(Color color) {
    warningFontColor = color;
  }

  /**
   * Returns the color of the text used for warnings that appear at the bottom of the figure.
   *
   * @return the font color.
   */
  public Color getWarningFontColor() {
    return warningFontColor;
  }

  /**
   * By default, feature labels are colored to match the color of the feature they represent. The
   * setGlobalLabelColor() method can be used to specify a single color for all labels.
   *
   * @param color the color of all labels.
   */
  public void setGlobalLabelColor(Color color) {
    globalLabelColor = color;
  }

  /**
   * Returns the color of all labels, or null if the labels are colored based on the feature they
   * represent.
   *
   * @return the color of all labels.
   */
  public Color getGlobalLabelColor() {
    return globalLabelColor;
  }

  /**
   * Sets whether or not the message set using {@link #setWarningText(String) setWarningText()}
   * should be drawn at the bottom of the figure.
   *
   * @param showWarning whether or not the message should be drawn.
   */
  public void setShowWarning(boolean showWarning) {
    this.showWarning = showWarning;
  }

  /**
   * Returns whether or not the message set using {@link #setWarningText(String) setWarningText()}
   * should be drawn at the bottom of the figure.
   *
   * @return whether or not the message should be drawn.
   */
  public boolean getShowWarning() {
    return showWarning;
  }

  /**
   * Sets whether or not the map should be drawn with a broken backbone line to indicate a linear
   * molecule.
   *
   * @param isLinear whether or not the map should be drawn with a broken backbone line.
   */
  public void setIsLinear(boolean isLinear) {
    this.isLinear = isLinear;
  }

  /**
   * Returns whether or not the map should be drawn with a broken backbone line to indicate a linear
   * molecule.
   *
   * @return whether or not the map should be drawn with a broken backbone.
   */
  public boolean getIsLinear() {
    return this.isLinear;
  }

  /**
   * Sets the text to draw at the sequence start/end boundary if this is a linear molecule.
   *
   * @param text the text to draw at the start/end boundary.
   */
  public void setLinearBreakText(String text) {
    linearBreakText = text;
  }

  /**
   * Returns the text to draw at the sequence start/end boundary if this is a linear molecule.
   *
   * @return the text to draw at the start/end boundary.
   */
  public String getLinearBreakText() {
    return linearBreakText;
  }

  /**
   * Sets whether or not a border should be drawn around the map.
   *
   * @param showBorder whether or not a border should be drawn.
   */
  public void setShowBorder(boolean showBorder) {
    this.showBorder = showBorder;
  }

  /**
   * Returns whether or not a border should be drawn around the map.
   *
   * @return whether or not a border should be drawn.
   */
  public boolean getShowBorder() {
    return showBorder;
  }

  /**
   * Sets whether or not inner labels should be moved from the inside of the backbone circle to the
   * outside of the backbone circle when there is insufficient room on the inside of the backbone
   * circle.
   *
   * @param moveInnerLabelsToOuter whether or not labels should be moved from the inside to the
   *     outside.
   */
  public void setMoveInnerLabelsToOuter(boolean moveInnerLabelsToOuter) {
    this.moveInnerLabelsToOuter = moveInnerLabelsToOuter;
  }

  /**
   * Returns whether or not inner labels should be moved from the inside of the backbone circle to
   * the outside of the backbone circle when there is insufficient room on the inside of the
   * backbone circle.
   *
   * @return whether or not labels should be moved from the inside to the outside.
   */
  public boolean getMoveInnerLabelsToOuter() {
    return moveInnerLabelsToOuter;
  }

  /**
   * Sets whether or not items on the map should be drawn with shading. This setting can be
   * overridden by individual {@link FeatureSlot} objects.
   *
   * @param showShading whether or not items on the map should be drawn with shading.
   */
  public void setShowShading(boolean showShading) {
    this.showShading = showShading;
  }

  /**
   * Returns whether or not items on the map should be drawn with shading.
   *
   * @return whether or not items on the map should be drawn with shading.
   */
  public boolean getShowShading() {
    return showShading;
  }

  /**
   * Sets the color of the circle that represents the DNA sequence.
   *
   * @param color the backbone color.
   */
  public void setBackboneColor(Color color) {
    backboneColor = color;
  }

  /**
   * Returns the color of the circle that represents the DNA sequence.
   *
   * @return the backbone color.
   */
  public Color getBackboneColor() {
    return backboneColor;
  }

  /**
   * Sets the default thickness of the line used to represent sequence features. This value can be
   * changed for individual featureSlots using the {@link FeatureSlot#setFeatureThickness(float)
   * FeatureSlot.setFeatureThickness()} method.
   *
   * @param thickness the default feature thickness.
   */
  public void setFeatureThickness(float thickness) {
    if (thickness < 0) {
      thickness = 0.0f;
    }
    featureThickness = thickness;
  }

  /**
   * Returns the default thickness of the line used to represent sequence features. This value can
   * be changed for individual featureSlots using the {@link FeatureSlot#setFeatureThickness(float)
   * FeatureSlot.setFeatureThickness()} method.
   *
   * @return the default feature thickness.
   */
  public float getFeatureThickness() {
    return featureThickness;
  }

  /**
   * Sets the minimum feature length to use when drawing sequence features. Features smaller than
   * this setting are artificially increased in length, to make them more visible.
   *
   * @param length the minimum feature length.
   */
  public void setMinimumFeatureLength(double length) {
    if (length < 0) {
      length = 0.0d;
    }
    minimumFeatureLength = length;
  }

  /**
   * Returns the minimum feature length to use when drawing sequence features. Features smaller than
   * this setting are artificially increased in length, to make them more visible.
   *
   * @return the minimum feature length.
   */
  public double getMinimumFeatureLength() {
    return minimumFeatureLength;
  }

  /**
   * Sets the amount of blank space placed between the concentric feature rings ({@link FeatureSlot}
   * objects).
   *
   * @param spacing the spacing between FeatureSlot objects.
   */
  public void setFeatureSlotSpacing(double spacing) {
    if (spacing < 0) {
      spacing = 0.0d;
    }
    featureSlotSpacing = spacing;
  }

  /**
   * Returns the amount of blank space placed between the concentric feature rings ({@link
   * FeatureSlot} objects).
   *
   * @return the spacing between FeatureSlot objects.
   */
  public double getFeatureSlotSpacing() {
    return featureSlotSpacing;
  }

  /**
   * Sets the length of the arrowheads used for features that are drawn as arrows.
   *
   * @param length the arrowhead length.
   */
  public void setArrowheadLength(double length) {
    if (length < 0) {
      length = 0.0d;
    }
    arrowheadLength = length;
  }

  /**
   * Returns the length of the arrowheads used for features that are drawn as arrows.
   *
   * @return the arrowhead length.
   */
  public double getArrowheadLength() {
    return arrowheadLength;
  }

  /**
   * Sets the alignment behavior of small features when they are drawn. Small features are those
   * features drawn larger than they actually are, in order to make them visible on the map. If
   * <code>shift</code> is set to <code>true</code>, the drawn representation of the feature is
   * shifted so that its center coincides with the true center of the feature. If shift is set to
   * false, the drawing begins at the true beginning of the feature, and extends the length
   * specified using the {@link #setMinimumFeatureLength(double) setMinimumFeatureLength()} method.
   *
   * @param shift whether or not to shift small features so that their center coincides with the
   *     true center of the feature.
   * @see #setMinimumFeatureLength(double)
   */
  public void setShiftSmallFeatures(boolean shift) {
    shiftSmallFeatures = shift;
  }

  /**
   * Returns the alignment behavior of small features when they are drawn. Small features are those
   * features drawn larger than they actually are, in order to make them visible on the map. If
   * <code>shift</code> is set to <code>true</code>, the drawing of the feature is shifted so that
   * its center coincides with the true center of the feature. If shift is set to false, the drawing
   * begins at the true beginning of the feature, and extends the length specified using the {@link
   * #setMinimumFeatureLength(double) setMinimumFeatureLength()}.
   *
   * @return whether or not to shift small features so that their center coincides with the true
   *     center of the feature.
   */
  public boolean getShiftSmallFeatures() {
    return shiftSmallFeatures;
  }

  /**
   * Sets the proportion of the width of the features to be redrawn for highlighting and shadowing
   * purposes.
   *
   * @param proportion the proportion (between <code>0</code> and <code>1</code>).
   */
  public void setShadingProportion(float proportion) {
    if (proportion < 0) {
      proportion = 0.0f;
    }
    if (proportion > 1) {
      proportion = 1.0f;
    }
    // divide by 2.0f because half will be used for shadows and half for highlights.
    shadingProportion = proportion / 2.0f;
  }

  /**
   * Returns the proportion of the width of the features to be redrawn for highlighting and
   * shadowing purposes.
   *
   * @return the proportion (between <code>0</code> and <code>1</code>).
   */
  public float getShadingProportion() {
    return shadingProportion;
  }

  /**
   * Sets the opacity of the highlighting added to map items. The higher the opacity, the more
   * obvious the highlighting.
   *
   * @param opacity the opacity (between <code>0</code> and <code>1</code>).
   */
  public void setHighlightOpacity(float opacity) {
    if (opacity < 0) {
      opacity = 0.0f;
    }
    if (opacity > 1) {
      opacity = 1.0f;
    }
    highlightOpacity = opacity;
  }

  /**
   * Returns the opacity of the highlighting added to map items. The higher the opacity, the more
   * obvious the highlighting.
   *
   * @return the opacity (between <code>0</code> and <code>1</code>).
   */
  public float getHighlightOpacity() {
    return highlightOpacity;
  }

  /**
   * Sets the opacity of the shadowing added to map items. The higher the opacity, the more obvious
   * the shadowing.
   *
   * @param opacity the opacity (between <code>0</code> and <code>1</code>).
   */
  public void setShadowOpacity(float opacity) {
    if (opacity < 0) {
      opacity = 0.0f;
    }
    if (opacity > 1) {
      opacity = 1.0f;
    }
    shadowOpacity = opacity;
  }

  /**
   * Returns the opacity of the shadowing added to map items. The higher the opacity, the more
   * obvious the shadowing.
   *
   * @return the opacity (between <code>0</code> and <code>1</code>).
   */
  public float getShadowOpacity() {
    return shadowOpacity;
  }

  /**
   * Sets the thickness of the tick marks in the sequence ruler.
   *
   * @param thickness the thickness of the tick marks.
   */
  public void setTickThickness(float thickness) {
    if (thickness < 0) {
      thickness = 0.0f;
    }
    tickThickness = thickness;
  }

  /**
   * Returns the thickness of the tick marks in the sequence ruler.
   *
   * @return the thickness of the tick marks.
   */
  public float getTickThickness() {
    return tickThickness;
  }

  /**
   * Sets the length of the tick marks in the sequence ruler.
   *
   * @param length the length of the tick marks.
   */
  public void setTickLength(float length) {
    if (length < 0) {
      length = 0.0f;
    }
    tickLength = length;
  }

  /**
   * Returns the length of the tick marks in the sequence ruler.
   *
   * @return the length of the tick marks.
   */
  public float getTickLength() {
    return tickLength;
  }

  /**
   * Sets the thickness of the short tick marks in the sequence ruler.
   *
   * @param thickness the thickness of the short tick marks.
   */
  public void setShortTickThickness(float thickness) {
    if (thickness < 0) {
      thickness = 0.0f;
    }
    shortTickThickness = thickness;
  }

  /**
   * Returns the thickness of the short tick marks in the sequence ruler.
   *
   * @return the thickness of the short tick marks.
   */
  public float getShortTickThickness() {
    return shortTickThickness;
  }

  /**
   * Sets the color of the tick marks in the sequence ruler.
   *
   * @param color the color of the tick marks.
   */
  public void setLongTickColor(Color color) {
    longTickColor = color;
  }

  /**
   * Returns the color of the tick marks in the sequence ruler.
   *
   * @return the color of the tick marks.
   */
  public Color getLongTickColor() {
    return longTickColor;
  }

  /**
   * Sets the color of the short tick marks in the sequence ruler.
   *
   * @param color the color of the short tick marks.
   */
  public void setShortTickColor(Color color) {
    shortTickColor = color;
  }

  /**
   * Returns the color of the short tick marks in the sequence ruler.
   *
   * @return the color of the short tick marks.
   */
  public Color getShortTickColor() {
    return shortTickColor;
  }

  /**
   * Sets the color of the tick mark drawn at the boundary between the end and beginning of the
   * sequence.
   *
   * @param color the color of the tick mark drawn at the boundary between the end and beginning of
   *     the sequence.
   */
  public void setZeroTickColor(Color color) {
    zeroTickColor = color;
  }

  /**
   * Returns the color of the tick mark drawn at the boundary between the end and beginning of the
   * sequence.
   *
   * @return the color of the tick mark drawn at the boundary between the end and beginning of the
   *     sequence.
   */
  public Color getZeroTickColor() {
    return zeroTickColor;
  }

  /**
   * Sets the approximate number of long tick marks to be drawn in the sequence ruler.
   *
   * @param ticks the approximate number of tick marks.
   */
  public void setDesiredNumberOfTicks(int ticks) {
    if (ticks < 0) {
      ticks = 0;
    }
    desiredNumberOfTicks = ticks;
  }

  /**
   * Returns the approximate number of long tick marks to be drawn in the sequence ruler.
   *
   * @return the approximate number of tick marks.
   */
  public int getDesiredNumberOfTicks() {
    return desiredNumberOfTicks;
  }

  /**
   * Can be used to store a zoom center value when a Cgview object is read from XML data. This value
   * does not alter drawing directly.
   *
   * @param zoomCenter a base position to center the map on.
   */
  public void setDesiredZoomCenter(int zoomCenter) {
    desiredZoomCenter = zoomCenter;
  }

  /**
   * Returns the zoomCenter value stored using the setDesiredZoomCenter() method.
   *
   * @return a base position to center the map on.
   */
  public int getDesiredZoomCenter() {
    return desiredZoomCenter;
  }

  /**
   * Can be used to store a zoom value when a Cgview object is read from XML data. This value does
   * not alter drawing directly.
   *
   * @param zoom a factor to zoom in by.
   */
  public void setDesiredZoom(double zoom) {
    desiredZoom = zoom;
  }

  /**
   * Returns the zoom value stored using the setDesiredZoom() method.
   *
   * @return a factor to zoom in by.
   */
  public double getDesiredZoom() {
    return desiredZoom;
  }

  /**
   * Sets the font of the number labels in the sequence ruler.
   *
   * @param font the font of the number labels.
   */
  public void setRulerFont(Font font) {
    rulerFont = font;
  }

  /**
   * Returns the font of the number labels in the sequence ruler.
   *
   * @return the font of the number labels.
   */
  public Font getRulerFont() {
    return rulerFont;
  }

  /**
   * Sets the default font used for text in {@link Legend} objects added to this Cgview.
   *
   * @param font the fault font for Legend objects.
   */
  public void setLegendFont(Font font) {
    legendFont = font;
  }

  /**
   * Returns the default font used for text in {@link Legend} objects added to this Cgview.
   *
   * @return the default font for Legend objects.
   */
  public Font getLegendFont() {
    return legendFont;
  }

  /**
   * Returns an arrayList of the Legend objects in this Cgview.
   *
   * @return the Legend objects in this Cgview.
   */
  protected ArrayList getLegends() {
    return legends;
  }

  /**
   * Sets the font used for any warnings that appear at the bottom of the map.
   *
   * @param font the font used for warning messages.
   */
  public void setWarningFont(Font font) {
    warningFont = font;
  }

  /**
   * Returns the font used for any warnings that appear at the bottom of the map.
   *
   * @return the font used for warning messages.
   */
  public Font getWarningFont() {
    return warningFont;
  }

  /**
   * Specifies a warning message to appear at the bottom of the map.
   *
   * @param message the contents of the message.
   */
  public void setWarningText(String message) {
    warningText = message;
  }

  /**
   * Returns a warning message to appear at the bottom of the map.
   *
   * @return the warning message.
   */
  public String getWarningText() {
    return warningText;
  }

  /**
   * Sets the units to be used for the sequence ruler.
   *
   * @param rulerUnits {@link CgviewConstants#BASES CgviewConstants.BASES} or {@link
   *     CgviewConstants#CENTISOMES CgviewConstants.CENTISOMES}.
   */
  public void setRulerUnits(int rulerUnits) {
    this.rulerUnits = rulerUnits;
  }

  /**
   * Returns the units to be used for the sequence ruler.
   *
   * @return {@link CgviewConstants#BASES CgviewConstants.BASES} or {@link
   *     CgviewConstants#CENTISOMES CgviewConstants.CENTISOMES}.
   */
  public int getRulerUnits() {
    return rulerUnits;
  }

  /**
   * Sets whether or not feature labels should be shown on this map. This setting is not overridden
   * by Feature and FeatureRange objects.
   *
   * @param labelType {@link CgviewConstants#LABEL_NONE CgviewConstants.LABEL_NONE}, {@link
   *     CgviewConstants#LABEL CgviewConstants.LABEL}, or {@link CgviewConstants#LABEL_ZOOMED
   *     CgviewConstants.LABEL_ZOOMED}.
   */
  public void setGlobalLabel(int labelType) {
    this.globalLabel = labelType;
  }

  /**
   * Returns whether or not feature labels should be shown on this map. This setting is not
   * overridden by Feature and FeatureRange objects.
   *
   * @return {@link CgviewConstants#LABEL_NONE CgviewConstants.LABEL_NONE}, {@link
   *     CgviewConstants#LABEL CgviewConstants.LABEL}, or {@link CgviewConstants#LABEL_ZOOMED
   *     CgviewConstants.LABEL_ZOOMED}.
   */
  public int getGlobalLabel() {
    return globalLabel;
  }

  /**
   * Sets whether or not legends should be drawn on this map.
   *
   * @param drawLegends whether or not to draw legends.
   */
  public void setDrawLegends(boolean drawLegends) {
    this.drawLegends = drawLegends;
  }

  /**
   * Returns boolean indicating whether or not legends should be drawn on this map.
   *
   * @return whether or not legends should be drawn on this map.
   */
  public boolean getDrawLegends() {
    return this.drawLegends;
  }

  /**
   * Sets the font of the map title.
   *
   * @param font the font of the map title.
   */
  public void setTitleFont(Font font) {
    titleFont = font;
  }

  /**
   * Returns the font of the map title.
   *
   * @return the font of the map title.
   */
  public Font getTitleFont() {
    return titleFont;
  }

  /**
   * Sets the default font of the feature labels. This font selection will be ignored if a new font
   * is specified in the Feature or FeatureRange objects.
   *
   * @param font the font of the feature labels.
   * @see Feature#setFont(Font) Feature.setFont()
   * @see FeatureRange#setFont(Font) FeatureRange.setFont()
   */
  public void setLabelFont(Font font) {
    labelFont = font;
  }

  /**
   * Returns the font of the feature labels. This font selection will be ignored if a font is
   * specified in the Feature or FeatureRange objects.
   *
   * @return the font of the feature labels.
   */
  public Font getLabelFont() {
    return labelFont;
  }

  /**
   * Sets the spacing between the ruler number labels and the tick marks.
   *
   * @param padding the spacing between the ruler number labels and the tick marks.
   */
  public void setRulerTextPadding(double padding) {
    if (padding < 0) {
      padding = 0.0d;
    }
    rulerTextPadding = padding;
  }

  /**
   * Returns the spacing between the ruler number labels and the tick marks.
   *
   * @return the spacing between the ruler number labels and the tick marks.
   */
  public double getRulerTextPadding() {
    return rulerTextPadding;
  }

  /**
   * Sets the origin of the sequence in relation to the backbone drawing. By default the sequence
   * begins at the twelve o'clock position.
   *
   * @param degrees the number of degrees (between <code>-360.0</code> and <code>360.0d</code>) to
   *     advance the sequence origin in the counterclockwise direction from the three o'clock
   *     position.
   */
  public void setOrigin(double degrees) {
    if (degrees < -360) {
      degrees = -360.0d;
    }
    if (degrees > 360) {
      degrees = 360.0d;
    }
    origin = degrees;
  }

  /**
   * Returns the number of degrees that the origin is to be moved in the counterclockwise direction
   * from the three o'clock position.
   *
   * @return the number of degrees.
   */
  public double getOrigin() {
    return origin;
  }

  /**
   * Sets the thickness of the line that extends from features to feature labels.
   *
   * @param thickness the thickness of the line.
   */
  public void setLabelLineThickness(float thickness) {
    if (thickness < 0) {
      thickness = 0.0f;
    }
    labelLineThickness = thickness;
  }

  /**
   * Returns the thickness of the line that extends from features to feature labels.
   *
   * @return the thickness of the line.
   */
  public float getLabelLineThickness() {
    return labelLineThickness;
  }

  /**
   * Sets the length of the line that extends from the feature to the feature label. If feature
   * labels clash with the number labels in the sequence ruler, the length of the label line should
   * be increased using this method.
   *
   * @param length the length of the label line.
   */
  public void setLabelLineLength(double length) {
    if (length < 10) {
      length = 10.0d;
    }
    labelLineLength = length;
  }

  /**
   * Returns the length of the line that extends from the feature to the feature label.
   *
   * @return the length of the label line.
   */
  public double getLabelLineLength() {
    return labelLineLength;
  }

  /**
   * Sets the minimum zoom value necessary for labels to be drawn below the backbone. When the map
   * is drawn using a zoom value less than the value set using this method, all the labels are drawn
   * on the outside of the backbone circle. When the map is drawn using a zoom value greater than
   * the value set using this method, the labels for features on the reverse strand are drawn on the
   * inside of the backbone circle. Also, when the zoom value is greater than the value set using
   * this method, only the features in the visible portion of the map are drawn.
   *
   * @param zoom the minimum zoom value necessary for labels to be drawn below the backbone.
   */
  protected void setZoomShift(double zoom) {
    if (zoom < 0) {
      zoom = 0.0d;
    }
    zoomShift = zoom;
  }

  /**
   * Returns the minimum zoom value necessary for labels to be drawn below the backbone. When the
   * map is drawn using a zoom value less than the value set using this method, all the labels are
   * drawn on the outside of the backbone circle. When the map is drawn using a zoom value greater
   * than the value set using this method, the labels for features on the reverse strand are drawn
   * on the inside of the backbone circle. Also, when the zoom value is greater than the value set
   * using this method, only the features in the visible portion of the map are drawn.
   *
   * @return the minimum zoom value necessary for labels to be drawn below the backbone.
   */
  protected double getZoomShift() {
    return zoomShift;
  }

  /**
   * Sets the maximum number of labels to attempt to arrange for display. Additional labels are
   * discarded prior to the arrangement process.
   *
   * @param number the maximum number of labels to attempt to arrange for display.
   */
  public void setLabelsToKeep(int number) {
    if (number < 0) {
      number = 0;
    }
    labelsToKeep = number;
  }

  /**
   * Returns the maximum number of labels to attempt to arrange for display. Additional labels are
   * discarded prior to the arrangement process.
   *
   * @return the maximum number of labels to attempt to arrange for display.
   */
  public int getLabelsToKeep() {
    return labelsToKeep;
  }

  /**
   * Sets whether or not labels should be randomly shuffled before removing excess labels.
   *
   * @param shuffle whether or not labels should be randomly shuffled before removing excess labels.
   */
  public void setLabelShuffle(boolean shuffle) {
    labelShuffle = shuffle;
  }

  /**
   * Returns true if labels are to be randomly shuffled before removing excess labels.
   *
   * @return whether or not labels should be randomly shuffled before removing excess labels.
   */
  public boolean getLabelShuffle() {
    return labelShuffle;
  }

  /**
   * Sets whether or not labels should be drawn with a colored background.
   *
   * @param coloredBackground whether or not labels should be drawn with a colored background.
   */
  public void setUseColoredLabelBackgrounds(boolean coloredBackground) {
    useColoredLabelBackgrounds = coloredBackground;
  }

  /**
   * Returns true if labels are to be drawn with a colored background.
   *
   * @return whether or not labels are to be drawn with a colored background.
   */
  public boolean getUseColoredLabelBackgrounds() {
    return useColoredLabelBackgrounds;
  }

  /**
   * Sets whether or not tick marks are drawn.
   *
   * @param draw whether or not tick marks are drawn.
   */
  public void setDrawTickMarks(boolean draw) {
    drawTickMarks = draw;
  }

  /**
   * Returns true if tick marks are to be drawn.
   *
   * @return whether or not tick marks are to be drawn.
   */
  public boolean getDrawTickMarks() {
    return drawTickMarks;
  }

  /**
   * Sets the title of of the map. This title is drawn in the center of the backbone circle when an
   * unzoomed map is drawn.
   *
   * @param title the title of the map.
   */
  public void setTitle(String title) {
    this.title = title.trim();
  }

  /**
   * Returns the title of the map. This title is drawn in the center of the backbone circle when an
   * unzoomed map is drawn.
   *
   * @return the title of the map.
   */
  public String getTitle() {
    return title;
  }

  /**
   * Returns an arrayList containing the FeatureSlot objects associated with this Cgview.
   *
   * @return an arrayList of FeatureSlot objects.
   */
  protected ArrayList getFeatureSlots() {
    return featureSlots;
  }

  /** Removes legends from this Cgview. */
  protected void removeLegends() {
    this.legends.clear();
  }

  /** Removes labels from this Cgview. */
  protected void removeLabels() {
    this.outerLabels.clear();
    this.innerLabels.clear();
  }

  /**
   * Returns an arrayList of OuterLabel objects associated with this Cgview.
   *
   * @return an arrayList of OuterLabel objects.
   */
  protected ArrayList getOuterLabels() {
    return outerLabels;
  }

  /**
   * Returns an arrayList of InnerLabel objects associated with this Cgview.
   *
   * @return an arrayList of InnerLabel objects.
   */
  protected ArrayList getInnerLabels() {
    return innerLabels;
  }

  /**
   * Returns an arrayList of LabelBounds objects associated with this Cgview.
   *
   * @return an arrayList of LabelBounds objects.
   */
  public ArrayList getLabelBounds() {
    return labelBounds;
  }

  /**
   * Returns the radius of the first (nearest to the backbone) direct strand FeatureSlot in this
   * Cgview.
   *
   * @return the radius.
   */
  protected double getFirstOuterFeatureRadius() {
    return backboneRadius + 0.5d * backboneThickness + featureSlotSpacing;
  }

  /**
   * Returns the radius of the first (nearest to the backbone) reverse strand FeatureSlot in this
   * Cgview.
   *
   * @return the radius.
   */
  protected double getFirstInnerFeatureRadius() {
    return backboneRadius - 0.5d * backboneThickness - featureSlotSpacing;
  }

  /**
   * Returns the radius of the last (furthest from the backbone) direct strand FeatureSlot in this
   * Cgview.
   *
   * @return the radius.
   */
  protected double getLastOuterFeatureRadius() {
    double radius = this.getFirstOuterFeatureRadius();
    Iterator i = featureSlots.iterator();
    while (i.hasNext()) {
      FeatureSlot currentFeatureSlot = (FeatureSlot) i.next();
      if (currentFeatureSlot.getStrand() == DIRECT_STRAND) {
        radius =
          radius +
          featureSlotSpacing +
          currentFeatureSlot.getFeatureThickness();
      }
    }
    return radius;
  }

  /**
   * Returns the radius of the last (furthest from the backbone) reverse strand FeatureSlot in this
   * Cgview.
   *
   * @return the radius.
   */
  protected double getLastInnerFeatureRadius() {
    double radius = this.getFirstInnerFeatureRadius();
    Iterator i = featureSlots.iterator();
    while (i.hasNext()) {
      FeatureSlot currentFeatureSlot = (FeatureSlot) i.next();
      if (currentFeatureSlot.getStrand() == REVERSE_STRAND) {
        radius =
          radius -
          featureSlotSpacing -
          currentFeatureSlot.getFeatureThickness();
      }
    }
    return radius;
  }

  /**
   * Returns a rectangle corresponding to the visible portion of the map.
   *
   * @return a rectangle.
   */
  protected Rectangle2D getBackgroundRectangle() {
    return backgroundRectangle;
  }

  /**
   * Returns a rectangle that covers the map title.
   *
   * @return a rectangle.
   */
  protected Rectangle2D getTitleRectangle() {
    return titleRectangle;
  }

  /**
   * Returns a rectangle that covers the length portion of the map title.
   *
   * @return a rectangle.
   */
  protected Rectangle2D getLengthRectangle() {
    return lengthRectangle;
  }

  /**
   * Returns a radians representation of the given base.
   *
   * @return a radians representation of the given base.
   */
  protected double getRadians(double base) {
    if (virtualZoomMultiplier <= 1.0d) {
      return (
        (base * ((2.0d * Math.PI) / (double) (sequenceLength))) -
        ((Math.PI / 180.0d) * origin)
      );
    } else {
      double centerRadians =
        (centerBase * ((2.0d * Math.PI) / (double) (sequenceLength))) -
        ((Math.PI / 180.0d) * origin);
      double baseDiff;
      double baseDiffRadians;

      if ((inZoomRangeOne(centerBase)) && (inZoomRangeOne(base))) {
        baseDiff = (double) centerBase - base;
        baseDiffRadians =
          (baseDiff * ((2.0d * Math.PI) / (double) (sequenceLength)));
        return (
          centerRadians -
          baseDiffRadians *
          ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
        );
      } else if ((inZoomRangeTwo(centerBase)) && (inZoomRangeOne(base))) {
        if (zoomRangeTwoStart != 0) {
          baseDiff = (double) centerBase - base;
          baseDiffRadians =
            (baseDiff * ((2.0d * Math.PI) / (double) (sequenceLength)));
          return (
            centerRadians -
            baseDiffRadians *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        } else {
          baseDiff = (double) sequenceLength - base + (double) centerBase;
          baseDiffRadians =
            (baseDiff * ((2.0d * Math.PI) / (double) (sequenceLength)));
          return (
            centerRadians -
            baseDiffRadians *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        }
      } else if ((inZoomRangeOne(centerBase)) && (inZoomRangeTwo(base))) {
        if (zoomRangeTwoStart != 0) {
          baseDiff = (double) centerBase - base;
          baseDiffRadians =
            (baseDiff * ((2.0d * Math.PI) / (double) (sequenceLength)));
          return (
            centerRadians -
            baseDiffRadians *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        } else {
          baseDiff = (double) sequenceLength - (double) centerBase + base;
          baseDiffRadians =
            (baseDiff * ((2.0d * Math.PI) / (double) (sequenceLength)));
          return (
            centerRadians +
            baseDiffRadians *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        }
      } else { // if ((inZoomRangeTwo(centerBase)) && (inZoomRangeTwo(base))) {
        baseDiff = (double) centerBase - base;
        baseDiffRadians =
          (baseDiff * ((2.0d * Math.PI) / (double) (sequenceLength)));
        return (
          centerRadians -
          baseDiffRadians *
          ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
        );
      }
    }
  }

  /**
   * Returns a radians representation of the given base.
   *
   * @return a radians representation of the given base.
   */
  protected double getRadians(int base) {
    if ((virtualZoomMultiplier <= 1.0f) || (base == centerBase)) {
      return (
        (base * ((2.0d * Math.PI) / (double) (sequenceLength))) -
        ((Math.PI / 180.0d) * origin)
      );
    } else {
      return getRadians((double) base);
    }
  }

  /**
   * Returns a Point2D specifying the center of this cgview.
   *
   * @return the center of this cgview.
   */
  protected Point2D getCenter() {
    return centerPoint;
  }

  /**
   * Returns a degrees representation of the given base. The virtualZoomMultiplier stretches
   * features.
   *
   * @return a degrees representation of the given base.
   */
  protected double getDegrees(int base) {
    // System.out.print ("The base is " + base + " ");
    // System.out.println ("the degrees was " + (((double)base / (double)sequenceLength) * 360.0d));
    if ((virtualZoomMultiplier <= 1.0d) || (base == centerBase)) {
      return ((double) base / (double) sequenceLength) * 360.0d;
    } else {
      double centerDegrees =
        ((double) centerBase / (double) sequenceLength) * 360.0d;
      int baseDiff;
      double baseDiffDegrees;

      if ((inZoomRangeOne(centerBase)) && (almostInZoomRangeOne(base))) {
        baseDiff = centerBase - base;
        baseDiffDegrees =
          ((double) baseDiff / (double) sequenceLength) * 360.0d;
        // System.out.println ("the degrees is A " + (centerDegrees - baseDiffDegrees *
        // ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)));
        return (
          centerDegrees -
          baseDiffDegrees *
          ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
        );
      } else if ((inZoomRangeTwo(centerBase)) && (almostInZoomRangeOne(base))) {
        if (zoomRangeTwoStart != 0) {
          baseDiff = centerBase - base;
          baseDiffDegrees =
            ((double) baseDiff / (double) sequenceLength) * 360.0d;
          // System.out.println ("the degrees is B " + (centerDegrees - baseDiffDegrees *
          // ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)));
          return (
            centerDegrees -
            baseDiffDegrees *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        } else {
          baseDiff = sequenceLength - base + centerBase;
          baseDiffDegrees =
            ((double) baseDiff / (double) sequenceLength) * 360.0d;
          // System.out.println ("the degrees is C " + (centerDegrees - baseDiffDegrees *
          // ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)));
          return (
            centerDegrees -
            baseDiffDegrees *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        }
      } else if ((inZoomRangeOne(centerBase)) && (inZoomRangeTwo(base))) {
        if (zoomRangeTwoStart != 0) {
          baseDiff = centerBase - base;
          baseDiffDegrees =
            ((double) baseDiff / (double) sequenceLength) * 360.0d;
          // System.out.println ("the degrees is D " + (centerDegrees - baseDiffDegrees *
          // ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)));
          return (
            centerDegrees -
            baseDiffDegrees *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        } else {
          baseDiff = sequenceLength - centerBase + base;
          baseDiffDegrees =
            ((double) baseDiff / (double) sequenceLength) * 360.0d;
          // System.out.println ("the degrees is E " + (centerDegrees + baseDiffDegrees *
          // ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)));
          return (
            centerDegrees +
            baseDiffDegrees *
            ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
          );
        }
      } else { // if ((inZoomRangeTwo(centerBase)) && (inZoomRangeTwo(base))) {
        baseDiff = centerBase - base;
        baseDiffDegrees =
          ((double) baseDiff / (double) sequenceLength) * 360.0d;
        // System.out.println ("the degrees is F " + (centerDegrees - baseDiffDegrees *
        // ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)));
        return (
          centerDegrees -
          baseDiffDegrees *
          ((virtualZoomMultiplier + zoomMultiplier) / zoomMultiplier)
        );
      }
    }
  }

  /**
   * Specifies under what circumstances labels should be drawn on the inside of the backbone circle.
   * When set to {@link CgviewConstants#INNER_LABELS_NO_SHOW CgviewConstants.INNER_LABELS_NO_SHOW},
   * all the labels are drawn on the outside of the backbone circle. When set to {@link
   * CgviewConstants#INNER_LABELS_AUTO CgviewConstants.INNER_LABELS_AUTO}, all the labels are drawn
   * on the outside of the backbone circle, unless a zoomed map is drawn. When set to {@link
   * CgviewConstants#INNER_LABELS_SHOW CgviewConstants.INNER_LABELS_SHOW}, the labels for features
   * on the reverse strand are drawn on the inside of the backbone circle.
   *
   * @param useInnerLabels
   */
  public void setUseInnerLabels(int useInnerLabels) {
    this.useInnerLabels = useInnerLabels;
  }

  /**
   * Returns the inner label behaviour of this Cgview.
   *
   * @return {@link CgviewConstants#INNER_LABELS_NO_SHOW CgviewConstants.INNER_LABELS_NO_SHOW},
   *     {@link CgviewConstants#INNER_LABELS_AUTO CgviewConstants.INNER_LABELS_AUTO}, or {@link
   *     CgviewConstants#INNER_LABELS_SHOW CgviewConstants.INNER_LABELS_SHOW}.
   */
  public int getUseInnerLabels() {
    return useInnerLabels;
  }

  /**
   * Specifies under what circumstances feature position information should be added to the label
   * text. When set to {@link CgviewConstants#POSITIONS_NO_SHOW CgviewConstants.POSITIONS_NO_SHOW},
   * position information is not added to the label. When set to {@link
   * CgviewConstants#POSITIONS_AUTO CgviewConstants.POSITIONS_AUTO}, position information is added
   * only when a zoomed map is drawn. When set to {@link CgviewConstants#POSITIONS_SHOW
   * CgviewConstants.POSITIONS_SHOW}, position information is added to the labels.
   *
   * @param giveFeaturePositions
   */
  public void setGiveFeaturePositions(int giveFeaturePositions) {
    this.giveFeaturePositions = giveFeaturePositions;
  }

  /**
   * Returns the feature position labelling behaviour of this Cgview.
   *
   * @return {@link CgviewConstants#POSITIONS_NO_SHOW CgviewConstants.POSITIONS_NO_SHOW}, {@link
   *     CgviewConstants#POSITIONS_AUTO CgviewConstants.POSITIONS_AUTO}, or {@link
   *     CgviewConstants#POSITIONS_SHOW CgviewConstants.POSITIONS_SHOW}.
   */
  public int getGiveFeaturePositions() {
    return giveFeaturePositions;
  }

  /**
   * Returns the distance moved by the labels each time they are moved away from the map backbone
   * during label repositioning.
   *
   * @return the distance moved by the labels.
   */
  protected double getRadiusShiftAmount() {
    return radiusShiftAmount;
  }

  /**
   * Controls label movement along the map backbone. When labels are moved along the backbone during
   * label placement, the increment moved by labels is proportional to the <code>shiftValue</code>
   * constant. If the lines extending to labels are crossing over one another the <code>shiftValue
   * </code> constant should be set to a smaller value. The default value is <code>0.20</code>.
   *
   * @param shiftValue a value that controls label movement along the map backbone.
   */
  protected void setRadiansShiftConstant(double shiftValue) {
    if (shiftValue < 0) {
      radiansShiftConstant = 0.0f;
    } else {
      radiansShiftConstant = shiftValue;
    }
  }

  /**
   * Returns a constant that controls label movement along the map backbone. When labels are moved
   * along the backbone during label placement, the increment moved by labels is proportional to the
   * <code>RADIANS_SHIFT</code> constant. If the lines extending to labels are crossing over one
   * another the <code>RADIANS_SHIFT</code> constant should be set to a smaller value. The default
   * value is <code>0.20</code>.
   *
   * @return the <code>shiftValue</code>. It controls label movement along the map backbone.
   */
  protected double getRadiansShiftConstant() {
    return radiansShiftConstant;
  }

  /**
   * Specifies how carefully labels placed on the map. Higher values lead to more labels being
   * placed without overlaps. Higher values also lead to slower program execution. Hight quality
   * label placement can be used when there are fewer than 100 labels, but when there are more than
   * 100 a low quality setting is recommended.
   *
   * @param labelPlacementQuality a value between <code>0</code> and <code>10</code> specifying how
   *     carefully labels are placed on the map. The default value is <code>5</code>.
   * @see #setLabelsToKeep(int) setLabelsToKeep
   */
  public void setLabelPlacementQuality(int labelPlacementQuality) {
    if (labelPlacementQuality < 1) {
      this.labelPlacementQuality = 1;
    } else if (labelPlacementQuality > 10) {
      this.labelPlacementQuality = 10;
    } else {
      this.labelPlacementQuality = labelPlacementQuality;
    }

    if (this.labelPlacementQuality == 0) {
      spreadIterations = 0;
      clashSpan = 0;
    } else if (this.labelPlacementQuality == 1) {
      spreadIterations = 10;
      clashSpan = 10;
      radiusShiftAmount = 20.0d;
    } else if (this.labelPlacementQuality == 2) {
      spreadIterations = 20;
      clashSpan = 40;
      radiusShiftAmount = 15.0d;
    } else if (this.labelPlacementQuality == 3) {
      spreadIterations = 30;
      clashSpan = 60;
      radiusShiftAmount = 14.0d;
    } else if (this.labelPlacementQuality == 4) {
      spreadIterations = 40;
      clashSpan = 80;
      radiusShiftAmount = 12.0d;
    } else if (this.labelPlacementQuality == 5) {
      spreadIterations = 50;
      clashSpan = 100;
      radiusShiftAmount = 10.0d;
    } else if (this.labelPlacementQuality == 6) {
      spreadIterations = 60;
      clashSpan = 120;
      radiusShiftAmount = 8.0d;
    } else if (this.labelPlacementQuality == 7) {
      spreadIterations = 65;
      clashSpan = 130;
      radiusShiftAmount = 6.0d;
    } else if (this.labelPlacementQuality == 8) {
      spreadIterations = 70;
      clashSpan = 140;
      radiusShiftAmount = 4.0d;
    } else if (this.labelPlacementQuality == 9) {
      spreadIterations = 200;
      clashSpan = 150;
      radiusShiftAmount = 2.0d;
    } else if (this.labelPlacementQuality == 10) {
      spreadIterations = 500;
      clashSpan = 1000;
      radiusShiftAmount = 1.0d;
    }
  }

  /**
   * Returns the zoom multiplier for the map, as set using the {@link #drawZoomed(Graphics2D,
   * double, int, boolean) drawZoomed()} method.
   *
   * @return the zoom multiplier.
   */
  protected double getZoomMultiplier() {
    return zoomMultiplier;
  }

  /**
   * Returns true if the entire plasmid should be drawn.
   *
   * @return true if the entire plasmid should be drawn.
   */
  protected boolean getDrawEntirePlasmid() {
    return drawEntirePlasmid;
  }

  /**
   * Returns the number of the first base inside the first zoom range. When drawing a zoomed map,
   * two ranges are calculated. These ranges contain all the base positions that should be drawn.
   *
   * @return the number of the first base inside the first zoom range.
   */
  protected int getZoomRangeOneStart() {
    return zoomRangeOneStart;
  }

  /**
   * Returns the number of the last base inside the first zoom range. When drawing a zoomed map, two
   * ranges are calculated. These ranges contain all the base positions that should be drawn.
   *
   * @return the number of the last base inside the first zoom range.
   */
  protected int getZoomRangeOneStop() {
    return zoomRangeOneStop;
  }

  /**
   * Returns the number of the first base inside the second zoom range. When drawing a zoomed map,
   * two ranges are calculated. These ranges contain all the base positions that should be drawn.
   *
   * @return the number of the first base inside the second zoom range.
   */
  protected int getZoomRangeTwoStart() {
    return zoomRangeTwoStart;
  }

  /**
   * Returns the number of the last base inside the second zoom range. When drawing a zoomed map,
   * two ranges are calculated. These ranges contain all the base positions that should be drawn.
   *
   * @return the number of the last base inside the second zoom range.
   */
  protected int getZoomRangeTwoStop() {
    return zoomRangeTwoStop;
  }

  /**
   * Returns true if the existing labels (those generated by the previous draw operation) are to be
   * drawn. Returns false if a new set of labels is to be generated and positioned.
   *
   * @return whether or not the existing labels are to be drawn.
   */
  protected boolean getKeepLastLabels() {
    return keepLastLabels;
  }

  /**
   * Returns true if the base is located within the visible region of the map.
   *
   * @return whether or not the base is located within the visible region of the map.
   */
  protected boolean baseIsDrawable(int base) {
    if (zoomMultiplier >= zoomShift) {
      if ((base >= zoomRangeOneStart) && (base <= zoomRangeOneStop)) {
        return true;
      } else if ((base >= zoomRangeTwoStart) && (base <= zoomRangeTwoStop)) {
        return true;
      } else if (base < zoomRangeOneStart) {
        return false;
      } else if (base > zoomRangeTwoStop) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  /**
   * Returns true if the base is located within the first zoom range.
   *
   * @return whether or not the base is located within the first zoom range.
   */
  protected boolean inZoomRangeOne(int base) {
    if ((base >= zoomRangeOneStart) && (base <= zoomRangeOneStop)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the base is located within the first zoom range.
   *
   * @return whether or not the base is located within the first zoom range.
   */
  protected boolean inZoomRangeOne(double base) {
    if ((base >= zoomRangeOneStart) && (base <= zoomRangeOneStop)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the base is located within the first zoom range, or if it is one base less than
   * the start.
   *
   * @return whether or not the base is located within the first zoom range, or if it is one base
   *     less than the start.
   */
  protected boolean almostInZoomRangeOne(int base) {
    if ((base >= zoomRangeOneStart) && (base <= zoomRangeOneStop)) {
      return true;
    } else if (
      ((base + 1) >= zoomRangeOneStart) && ((base + 1) <= zoomRangeOneStop)
    ) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the base is located within the second zoom range.
   *
   * @return whether or not the base is located within the first zoom range.
   */
  protected boolean inZoomRangeTwo(int base) {
    if ((base >= zoomRangeTwoStart) && (base <= zoomRangeTwoStop)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the base is located within the second zoom range.
   *
   * @return whether or not the base is located within the first zoom range.
   */
  protected boolean inZoomRangeTwo(double base) {
    if ((base >= zoomRangeTwoStart) && (base <= zoomRangeTwoStop)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the base is located within the first zoom range or the second zoom range.
   *
   * @return whether or not the base is located within the first zoom range.
   */
  protected boolean inZoomRange(int base) {
    if ((inZoomRangeOne(base)) || (inZoomRangeTwo(base))) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if the base is located within the first zoom range or the second zoom range.
   *
   * @return whether or not the base is located within the first zoom range.
   */
  protected boolean inZoomRange(double base) {
    if ((inZoomRangeOne(base)) || (inZoomRangeTwo(base))) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Adds an outerLabel object to this cgview object.
   *
   * @param label an outerLabel object.
   */
  protected void addOuterLabel(Label label) {
    outerLabels.add(label);
  }

  /**
   * Adds an InnerLabel object to this cgview object.
   *
   * @param label an innerLabel object.
   */
  protected void addInnerLabel(Label label) {
    innerLabels.add(label);
  }

  /**
   * Returns an estimate of the maximum suitable zoom value for this map.
   *
   * @return an estimate of the maximum suitable zoom value for this map.
   */
  public double getZoomMax() {
    smallestDimension = Math.min(width, height);

    // this is an estimate of the viewing area
    double viewDiagonal = Math.sqrt(
      smallestDimension *
      smallestDimension +
      smallestDimension *
      smallestDimension
    );
    double basePerCircum = (double) (sequenceLength) /
    (2.0d * Math.PI * backboneRadius);

    // this limits zoom to the point where 10 bases are shown on the map
    double zoomMultiplierMaxForThisSequence =
      (viewDiagonal * basePerCircum) / 10;
    return zoomMultiplierMaxForThisSequence;
  }

  /** Translates the canvas. */
  protected void translateCanvas() {
    if (centerPoint != null) {
      AffineTransform at = new AffineTransform();
      at.setToTranslation(
        (width / 2) - centerPoint.getX(),
        (height / 2) - centerPoint.getY()
      );
      gg.transform(at);

      // create a rectangle for the background
      backgroundRectangle =
        new Rectangle2D.Double(
          centerPoint.getX() - (width / 2),
          centerPoint.getY() - (height / 2),
          width,
          height
        );
    }
  }

  /** Undoes the canvas translation, so that point(0,0) is in upper left. */
  protected void untranslateCanvas() {
    if (centerPoint != null) {
      AffineTransform at = new AffineTransform();

      at.setToTranslation(
        (-width / 2) + centerPoint.getX(),
        (-height / 2) + centerPoint.getY()
      );
      gg.transform(at);

      // create a rectangle for the background
      backgroundRectangle = new Rectangle2D.Double(0.0d, 0.0d, width, height);
    }
  }

  /** Translates the canvas so that point (0,0) is in the center. */
  protected void translateZeroCenter() {
    AffineTransform at = new AffineTransform();
    at.setToTranslation((width / 2), (height / 2));
    gg.transform(at);

    backgroundRectangle =
      new Rectangle2D.Double(-(width / 2), -(height / 2), width, height);
  }

  /**
   * Undoes the translation of the canvas that was done to place point (0,0) at the center, so that
   * point(0,0) becomes upper left.
   */
  protected void untranslateZeroCenter() {
    AffineTransform at = new AffineTransform();
    at.setToTranslation((-width / 2), (-height / 2));
    gg.transform(at);

    backgroundRectangle = new Rectangle2D.Double(0.0d, 0.0d, width, height);
  }

  /**
   * Checks the zoomMultiplier value and makes adjustments if necessary
   *
   * @param zoomMultiplier the factor to zoom in by.
   */
  protected double adjustZoom(double zoomMultiplier) {
    // determine a suitable maximum zoomMultiplier for the sequence

    smallestDimension = Math.min(width, height);

    // this is an estimate of the viewing area
    double viewDiagonal = Math.sqrt(
      smallestDimension *
      smallestDimension +
      smallestDimension *
      smallestDimension
    );
    double basePerCircum = (double) (sequenceLength) /
    (2.0d * Math.PI * backboneRadius);

    // this limits zoom to the point where 10 bases are shown on the map
    double zoomMultiplierMaxForThisSequence =
      (viewDiagonal * basePerCircum) / 10;

    // check the zoomMultiplier.
    if (zoomMultiplier < 1.0d) {
      zoomMultiplier = 1.0d;
    }

    if (zoomMultiplierMaxForThisSequence < 1.0d) {
      zoomMultiplierMaxForThisSequence = 1.0d;
    }

    if (zoomMultiplier > zoomMultiplierMaxForThisSequence) {
      zoomMultiplier = zoomMultiplierMaxForThisSequence;
    }

    if (zoomMultiplier > ZOOM_MULTIPLIER_MAX) {
      virtualZoomMultiplier = zoomMultiplier - ZOOM_MULTIPLIER_MAX;
      zoomMultiplier = ZOOM_MULTIPLIER_MAX;
    }

    if (virtualZoomMultiplier > VIRTUAL_ZOOM_MULTIPLIER_MAX) {
      virtualZoomMultiplier = VIRTUAL_ZOOM_MULTIPLIER_MAX;
    }
    return zoomMultiplier;
  }

  /**
   * Draws this Cgview map into the specified Graphics2D context. The map is drawn such that it is
   * zoomed in by a factor of <code>zoom</code>, and centered on the base at position <code>
   * centerBase</code>.
   *
   * @param gg the <code>Graphics2D</code> context for rendering.
   * @param zoom the factor to zoom in by.
   * @param center the base position to center the map on.
   * @param keepLastLabels <code>true</code> if the labels from the last draw operation should be
   *     redrawn without repositioning, or <code>false</code> if instead new labels should be
   *     generated and then positioned.
   */
  public void drawZoomed(
    Graphics2D gg,
    double zoom,
    int center,
    boolean keepLastLabels
  ) {
    this.keepLastLabels = keepLastLabels;
    drawZoomed(gg, zoom, center);
  }

  /**
   * Draws this Cgview map into the specified Graphics2D context. The map is drawn such that it is
   * zoomed in by a factor of <code>zoom</code>, and centered on the base at position <code>
   * centerBase</code>.
   *
   * @param gg the <code>graphics2D</code> context for rendering.
   * @param zoom the factor to zoom in by.
   * @param center the base position to center the map on.
   */
  public void drawZoomed(Graphics2D gg, double zoom, int center) {
    this.gg = gg;
    zoomMultiplier = adjustZoom(zoom);
    centerBase = center;

    smallestDimension = Math.min(width, height);
    if (backboneRadius > 0.80d * smallestDimension / 2.0d) {
      backboneRadius = 0.80d * smallestDimension / 2.0d;
      System.err.println(
        "[warning] backbone radius was adjusted to fit inside of canvas."
      );
    }
    if (backboneRadius < 10.0d) {
      backboneRadius = 10.0d;
      System.err.println("[warning] backbone radius was increased to 10.0.");
    }

    // use the zoomMultiplier to adjust the backboneRadius;
    double originalBackboneRadius = backboneRadius;
    backboneRadius = backboneRadius * zoomMultiplier;
    virtualBackboneRadius =
      originalBackboneRadius * (zoomMultiplier + virtualZoomMultiplier - 1.0d);

    if (centerBase < 0) {
      centerBase = 0;
    } else if (centerBase > sequenceLength) {
      centerBase = sequenceLength;
    }

    // determine the radians for the centerBase
    double radians = getRadians(centerBase);

    // now determine the x and y coordinates on the backbone
    double x = Math.cos(radians) * backboneRadius;
    double y = Math.sin(radians) * backboneRadius;

    // set centerPoint
    centerPoint = new Point2D.Double(x, y);

    // set render quality
    setRenderQuality();

    // now complete the translation
    translateCanvas();

    // fill the background
    gg.setPaint(backgroundColor);
    gg.fill(backgroundRectangle);

    // change background rectangle to a square
    // backgroundRectangle = new Rectangle2D.Double(x - (smallestDimension/2), y -
    // (smallestDimension/2), smallestDimension, smallestDimension);

    // now determine the length of the backbone arc that spans the viewing area by
    // shifting radians down, and then up
    // this is intended to determine which bases on the plasmid should be drawn. It doesn't have to
    // be completely accurate because the graphics2d clipping region is set so that things are not
    // drawn outside of the canvas.

    if (zoomMultiplier >= zoomShift) {
      double innerMostRadiusToDraw =
        this.getLastInnerFeatureRadius() - featureSlotSpacing - tickLength;
      double outerMostRadiusToDraw =
        this.getLastOuterFeatureRadius() + featureSlotSpacing + tickLength;

      double downshift = 0.0d;
      double upshift = 0.0d;

      double xInner = Math.cos(radians) * innerMostRadiusToDraw;
      double yInner = Math.sin(radians) * innerMostRadiusToDraw;

      double tempX1 = xInner;
      double tempY1 = yInner;
      double tempX2 = xInner;
      double tempY2 = yInner;

      double tempRadians = radians;
      double shiftAmount = ((1.0d / 2.0d) * Math.PI) / (backboneRadius);
      Point2D checkPointInner = new Point2D.Double(xInner, yInner);
      Point2D checkPointOuter = new Point2D.Double(xInner, yInner);
      drawEntirePlasmid = false;

      while (
        (
          (backgroundRectangle.contains(checkPointInner)) ||
          (backgroundRectangle.contains(checkPointOuter))
        ) &&
        ((radians - tempRadians) < 2.0d * Math.PI)
      ) {
        tempRadians = tempRadians - shiftAmount;
        tempX1 = Math.cos(tempRadians) * innerMostRadiusToDraw;
        tempY1 = Math.sin(tempRadians) * innerMostRadiusToDraw;
        tempX2 = Math.cos(tempRadians) * outerMostRadiusToDraw;
        tempY2 = Math.sin(tempRadians) * outerMostRadiusToDraw;
        checkPointInner.setLocation(tempX1, tempY1);
        checkPointOuter.setLocation(tempX2, tempY2);
      }

      if ((radians - tempRadians) >= 2.0d * Math.PI) {
        drawEntirePlasmid = true;
      }

      downshift = radians - tempRadians;
      // System.out.println ("shiftamount is " + shiftAmount);

      // System.out.println ("downshift is " + downshift);
      checkPointInner.setLocation(xInner, yInner);
      checkPointOuter.setLocation(xInner, yInner);

      // new
      tempRadians = radians;

      while (
        (
          (backgroundRectangle.contains(checkPointInner)) ||
          (backgroundRectangle.contains(checkPointOuter))
        ) &&
        ((tempRadians - radians) < 2.0d * Math.PI)
      ) {
        tempRadians = tempRadians + shiftAmount;
        tempX1 = Math.cos(tempRadians) * innerMostRadiusToDraw;
        tempY1 = Math.sin(tempRadians) * innerMostRadiusToDraw;
        tempX2 = Math.cos(tempRadians) * outerMostRadiusToDraw;
        tempY2 = Math.sin(tempRadians) * outerMostRadiusToDraw;
        checkPointInner.setLocation(tempX1, tempY1);
        checkPointOuter.setLocation(tempX2, tempY2);
      }

      if ((tempRadians - radians) >= 2.0d * Math.PI) {
        drawEntirePlasmid = true;
      }

      upshift = tempRadians - radians;
      // System.out.println ("upshift is " + upshift);

      double basePerCircum = (double) (sequenceLength) /
      (2.0d * Math.PI * backboneRadius);
      double baseSpanUpd = upshift * basePerCircum * backboneRadius;
      double baseSpanDownd = downshift * basePerCircum * backboneRadius;

      baseSpanUpd =
        baseSpanUpd *
        (zoomMultiplier / (zoomMultiplier + virtualZoomMultiplier - 1));
      baseSpanDownd =
        baseSpanDownd *
        (zoomMultiplier / (zoomMultiplier + virtualZoomMultiplier - 1));

      // add 20% to each to make sure they extend off of the canvas
      baseSpanUpd = baseSpanUpd + baseSpanUpd * 0.20d;
      baseSpanDownd = baseSpanDownd + baseSpanDownd * 0.20d;

      // System.out.println ("baseSpanUpd is " + baseSpanUpd);
      int baseSpanUp = Math.round((float) (baseSpanUpd));
      int baseSpanDown = Math.round((float) (baseSpanDownd));

      if (
        (!drawEntirePlasmid) && ((centerBase + baseSpanUp) > sequenceLength)
      ) {
        zoomRangeOneStart = centerBase - baseSpanDown;
        zoomRangeOneStop = sequenceLength;
        zoomRangeTwoStart = 0;
        zoomRangeTwoStop = baseSpanUp - (sequenceLength - centerBase);
      } else if ((!drawEntirePlasmid) && ((centerBase - baseSpanDown) < 1)) {
        zoomRangeOneStart = sequenceLength - (baseSpanDown - centerBase);
        zoomRangeOneStop = sequenceLength;
        zoomRangeTwoStart = 0;
        zoomRangeTwoStop = centerBase + baseSpanUp;
      } else if (!drawEntirePlasmid) {
        zoomRangeOneStart = centerBase - baseSpanDown;
        zoomRangeOneStop = centerBase;
        zoomRangeTwoStart = centerBase;
        zoomRangeTwoStop = centerBase + baseSpanUp;
      }
    }

    // System.out.println ("zoomRangeOneStart is " + zoomRangeOneStart);
    // System.out.println ("zoomRangeOneStop is " + zoomRangeOneStop);
    // System.out.println ("zoomRangeTwoStart is " + zoomRangeTwoStart);
    // System.out.println ("zoomRangeTwoStop is " + zoomRangeTwoStop);

    drawMain();

    // return the backboneRadius to its original value
    backboneRadius = originalBackboneRadius;
    virtualBackboneRadius = backboneRadius;
    drawEntirePlasmid = true;

    System.out.println("The map has been drawn.");
  }

  /**
   * Draws this Cgview map into the specified Graphics2D context
   *
   * @param gg the <code>graphics2D</code> context for rendering.
   * @param keepLastLabels <code>true</code> if the labels from the last draw operation should be
   *     redrawn without repositioning, or <code>false</code> if instead new labels should be
   *     generated and then positioned.
   */
  public void draw(Graphics2D gg, boolean keepLastLabels) {
    this.keepLastLabels = keepLastLabels;
    draw(gg);
  }

  /**
   * Draws this Cgview map into the specified Graphics2D context
   *
   * @param gg the Graphics2D context for rendering.
   */
  public void draw(Graphics2D gg) {
    this.gg = gg;
    zoomMultiplier = 1.0d;

    smallestDimension = Math.min(width, height);
    if (backboneRadius > 0.80d * smallestDimension / 2.0d) {
      backboneRadius = 0.80d * smallestDimension / 2.0d;
      System.err.println(
        "[warning] backbone radius was adjusted to fit inside of canvas."
      );
    }
    if (backboneRadius < 10.0d) {
      backboneRadius = 10.0d;
      System.err.println("[warning] backbone radius was increased to 10.0.");
    }

    virtualBackboneRadius = backboneRadius;

    setRenderQuality();

    centerPoint = new Point2D.Double(0, 0);

    translateCanvas();

    // fill the background
    gg.setPaint(backgroundColor);
    gg.fill(backgroundRectangle);

    // change background rectangle to a square
    // backgroundRectangle = new Rectangle2D.Double(-(smallestDimension/2), -(smallestDimension/2),
    // smallestDimension, smallestDimension);

    drawMain();

    System.out.println("The map has been drawn.");
  }

  /** Sets the render quality for the Graphics2D object. */
  private void setRenderQuality() {
    gg.setRenderingHint(
      RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON
    );
    gg.setRenderingHint(
      RenderingHints.KEY_DITHERING,
      RenderingHints.VALUE_DITHER_ENABLE
    );
    gg.setRenderingHint(
      RenderingHints.KEY_COLOR_RENDERING,
      RenderingHints.VALUE_COLOR_RENDER_QUALITY
    );
    gg.setRenderingHint(
      RenderingHints.KEY_RENDERING,
      RenderingHints.VALUE_RENDER_QUALITY
    );
    gg.setRenderingHint(
      RenderingHints.KEY_INTERPOLATION,
      RenderingHints.VALUE_INTERPOLATION_BICUBIC
    );
    gg.setRenderingHint(
      RenderingHints.KEY_ALPHA_INTERPOLATION,
      RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
    );
    gg.setRenderingHint(
      RenderingHints.KEY_STROKE_CONTROL,
      RenderingHints.VALUE_STROKE_PURE
    );
    gg.setRenderingHint(
      RenderingHints.KEY_TEXT_ANTIALIASING,
      RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    );
    // gg.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
    // RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }

  /** Calls more specialized drawing methods. */
  private void drawMain() {
    Iterator i;

    if (drawEntirePlasmid) {
      placeTitle();
    }

    // remove labels from last draw if necessary
    if (!keepLastLabels) {
      innerLabels.clear();
      outerLabels.clear();
      labelBounds.clear();
      clashLabels = 0;
      totalLabels = 0;
    }

    // draw the contens of the FeatureSlots
    System.out.print("Drawing features.");
    i = featureSlots.iterator();
    while (i.hasNext()) {
      FeatureSlot currentFeatureSlot = (FeatureSlot) i.next();
      currentFeatureSlot.draw();
      System.out.print(".");
    }
    System.out.println(".");

    drawBackbone();

    if (isLinear) {
      drawLinearDividerLine();
    }

    if (
      (globalLabel == LABEL) ||
      ((globalLabel == LABEL_ZOOMED) && (zoomMultiplier >= zoomShift))
    ) {
      totalLabels = outerLabels.size() + innerLabels.size();

      System.out.print("Positioning and drawing " + totalLabels + " labels.");

      // draw the new labels
      if (!keepLastLabels) {
        drawLabels(innerLabels);
        drawLabels(outerLabels);
      } else {
        // or draw the labels from the last draw operation.
        i = innerLabels.iterator();
        while (i.hasNext()) {
          Label currentLabel = (Label) i.next();
          currentLabel.drawLabelLine();
        }
        i = outerLabels.iterator();
        while (i.hasNext()) {
          Label currentLabel = (Label) i.next();
          currentLabel.drawLabelLine();
        }

        untranslateCanvas();
        i = innerLabels.iterator();
        while (i.hasNext()) {
          Label currentLabel = (Label) i.next();
          currentLabel.drawLabelText();
        }
        i = outerLabels.iterator();
        while (i.hasNext()) {
          Label currentLabel = (Label) i.next();
          currentLabel.drawLabelText();
        }
        translateCanvas();
      }

      System.out.println(".");

      System.out.println(clashLabels + " labels were removed.");
    }

    if (drawTickMarks) {
      System.out.println("Drawing tick marks.");

      drawTickMarks(
        DIRECT_STRAND,
        this.getLastOuterFeatureRadius() + 0.5d * tickThickness
      );

      drawTickMarks(
        REVERSE_STRAND,
        this.getLastInnerFeatureRadius() - 0.5d * tickThickness
      );
    }

    // undo the translation here
    untranslateCanvas();
    translateZeroCenter();

    drawWarningMessage();

    if (drawEntirePlasmid) {
      drawTitle();
    }

    // draw border
    if (showBorder) {
      double borderThickness = 2.0d;
      Rectangle2D border = new Rectangle2D.Double(
        backgroundRectangle.getX() + 0.5d * borderThickness,
        backgroundRectangle.getY() + 0.5d * borderThickness,
        width - borderThickness,
        height - borderThickness
      );
      gg.setPaint(borderColor);
      gg.setStroke(
        new BasicStroke(
          (float) borderThickness,
          BasicStroke.CAP_SQUARE,
          BasicStroke.JOIN_MITER
        )
      );
      gg.draw(border);
    }

    if (this.drawLegends) {
      System.out.println("Drawing legends.");
      // draw legends
      i = legends.iterator();
      while (i.hasNext()) {
        Legend currentLegend = (Legend) i.next();
        currentLegend.setBounds(0.0d, 0.0d);
        currentLegend.draw();
        // remove once drawn.
        // i.remove();
      }
    }

    untranslateZeroCenter();

    keepLastLabels = false;
    legends.remove(infoLegend);
    zoomMultiplier = 1.0f;
    virtualZoomMultiplier = 1.0f;
  }

  /** Draws the sequence backbone. */
  private void drawBackbone() {
    double startOfArc;
    double extentOfArc;

    int startBase;
    int stopBase;

    if (!drawEntirePlasmid) {
      startBase = zoomRangeOneStart;
      stopBase = zoomRangeTwoStop;
    } else {
      startBase = 1;
      stopBase = sequenceLength;
    }

    // typical case where start is less than stop
    if (startBase <= stopBase) {
      startOfArc = getDegrees(startBase - 1);
      extentOfArc = getDegrees(stopBase) - startOfArc;
    }
    // case where feature spans start/stop boundary
    else {
      startOfArc =
        ((double) (startBase - 1.0d) / (double) (sequenceLength)) * 360.0d;
      startOfArc = getDegrees(startBase - 1);
      extentOfArc = getDegrees(sequenceLength) - startOfArc;

      double startOfArcB = getDegrees(1 - 1);
      double extentOfArcB = getDegrees(stopBase) - startOfArcB;

      extentOfArc = extentOfArc + extentOfArcB;
    }

    // draw the plasmid backbone
    BasicStroke arcStroke = new BasicStroke(
      backboneThickness,
      BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_BEVEL
    );
    Area centralArc = new Area();
    centralArc.add(
      new Area(
        arcStroke.createStrokedShape(
          new Arc2D.Double(
            -backboneRadius,
            -backboneRadius,
            (backboneRadius * 2.0d),
            (backboneRadius * 2.0d),
            -startOfArc - extentOfArc + origin,
            extentOfArc,
            Arc2D.OPEN
          )
        )
      )
    );

    // create an Area to subtract from the backbone if this is a linear molecule
    Area blockArc = null;
    // restrict to case where origin is set to 90.0.
    if ((this.isLinear) && (this.origin == 90.0d)) {
      if ((drawEntirePlasmid) || (zoomRangeTwoStart == 0)) {
        // create a 3' 5' label to indicate molecule is linear
        FontRenderContext frc = gg.getFontRenderContext();
        TextLayout layout = new TextLayout(
          this.linearBreakText,
          rulerFont,
          frc
        );
        Rectangle2D bounds = layout.getBounds();
        double textHeight = bounds.getHeight();
        double textWidth = bounds.getWidth();

        double zeroLineRadians = getRadians(0);
        double zeroStartX = (Math.cos(zeroLineRadians) * backboneRadius);
        double zeroStartY = (Math.sin(zeroLineRadians) * backboneRadius);

        double textPositionX =
          zeroStartX - textWidth / 2.0d - layout.getDescent() * 0.4d;
        double textPositionY = zeroStartY + textHeight / 2.0d;

        // adjust because all caps
        textPositionY = textPositionY + layout.getDescent();

        // draw bounds
        gg.setPaint(backgroundColor);
        bounds.setRect(
          bounds.getX() + textPositionX - 1.5d,
          bounds.getY() + textPositionY - layout.getDescent() - 1.5d,
          bounds.getWidth() + 3.0d,
          bounds.getHeight() + 3.0d
        );

        // gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f));
        // gg.fill(bounds);
        // gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

        gg.setPaint(rulerFontColor);

        untranslateCanvas();

        layout.draw(
          gg,
          (float) (textPositionX + width / 2 - centerPoint.getX()),
          (float) (textPositionY + height / 2 - centerPoint.getY()) -
          layout.getDescent()
        );
        translateCanvas();

        // zero base is visible
        // blockLength should be changed to the number of degrees needed to create an arc of length
        // equals textWidth
        double arcInDegrees = Math.toDegrees(textWidth / backboneRadius);
        double blockLength = arcInDegrees;
        // add extra white space
        blockLength = blockLength + blockLength * 0.1d;
        this.zigzagWidth = blockLength / 2.0d;
        // double blockLength = 5.0d;
        double startOfArcBlock = getDegrees(1 - 1) - blockLength / 2.0d;
        double extentOfArcBlock = blockLength;

        BasicStroke blockArcStroke = new BasicStroke(
          backboneThickness + 1.0f,
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL
        );
        blockArc =
          new Area(
            blockArcStroke.createStrokedShape(
              new Arc2D.Double(
                -backboneRadius,
                -backboneRadius,
                (backboneRadius * 2.0d),
                (backboneRadius * 2.0d),
                -startOfArcBlock - extentOfArcBlock + origin,
                extentOfArcBlock,
                Arc2D.OPEN
              )
            )
          );
      }
    }

    // to prevent drawing off canvas
    if (blockArc != null) {
      centralArc.subtract(blockArc);
    }
    centralArc.intersect(new Area(this.getBackgroundRectangle()));
    gg.setPaint(backboneColor);
    gg.fill(centralArc);

    if (showShading) {
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, highlightOpacity)
      );
      gg.setPaint(Color.white);

      double radiusIncrease =
        0.5d *
        backboneThickness -
        0.5d *
        (backboneThickness * shadingProportion);

      BasicStroke highlightArcStroke = new BasicStroke(
        backboneThickness * shadingProportion,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL
      );
      Area highlightArc = new Area();
      highlightArc.add(
        new Area(
          highlightArcStroke.createStrokedShape(
            new Arc2D.Double(
              -backboneRadius - radiusIncrease,
              -backboneRadius - radiusIncrease,
              (backboneRadius + radiusIncrease) * 2.0d,
              (backboneRadius + radiusIncrease) * 2.0d,
              -startOfArc - extentOfArc + origin,
              extentOfArc,
              Arc2D.OPEN
            )
          )
        )
      );
      if (blockArc != null) {
        highlightArc.subtract(blockArc);
      }
      highlightArc.intersect(new Area(this.getBackgroundRectangle()));
      gg.fill(highlightArc);

      // draw a shadow arc on inner edge of backbone
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowOpacity)
      );
      gg.setPaint(Color.black);

      double radiusDecrease =
        -0.5d *
        backboneThickness +
        0.5d *
        (backboneThickness * shadingProportion);

      BasicStroke shadowArcStroke = new BasicStroke(
        backboneThickness * shadingProportion,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL
      );
      Area shadowArc = new Area();
      shadowArc.add(
        new Area(
          shadowArcStroke.createStrokedShape(
            new Arc2D.Double(
              -backboneRadius - radiusDecrease,
              -backboneRadius - radiusDecrease,
              (backboneRadius + radiusDecrease) * 2.0d,
              (backboneRadius + radiusDecrease) * 2.0d,
              -startOfArc - extentOfArc + origin,
              extentOfArc,
              Arc2D.OPEN
            )
          )
        )
      );
      if (blockArc != null) {
        shadowArc.subtract(blockArc);
      }
      shadowArc.intersect(new Area(this.getBackgroundRectangle()));
      gg.fill(shadowArc);

      // return to non transparent
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
      );
    }

    // set the values of innerArc and outerArc, which will be used for collision testing with labels
    double outerArcRadius = this.getLastOuterFeatureRadius();

    double innerArcRadius = this.getLastInnerFeatureRadius();

    outerArc =
      new Arc2D.Double(
        -outerArcRadius,
        -outerArcRadius,
        (outerArcRadius * 2.0d),
        (outerArcRadius * 2.0d),
        -startOfArc - extentOfArc + origin,
        extentOfArc,
        Arc2D.OPEN
      );

    innerArc =
      new Arc2D.Double(
        -innerArcRadius,
        -innerArcRadius,
        (innerArcRadius * 2.0d),
        (innerArcRadius * 2.0d),
        -startOfArc - extentOfArc + origin,
        extentOfArc,
        Arc2D.OPEN
      );
  }

  private void drawLinearDividerLine() {
    // only draw divider line if it should be visible.
    if (!((drawEntirePlasmid) || (zoomRangeTwoStart == 0))) {
      return;
    }

    double outerRadius = this.getLastOuterFeatureRadius() - featureSlotSpacing;
    double innerRadius = this.getLastInnerFeatureRadius() + featureSlotSpacing;
    double zeroLineRadians = getRadians(0);
    int zigzagNum = featureSlots.size() * 4;

    double zigzagRadiansShift = Math.toRadians(this.zigzagWidth / 8.0d);
    boolean shiftLeft = true;
    double zigzagRadius = innerRadius;
    double zigzagLength =
      ((outerRadius - innerRadius) / (double) zigzagNum) / 2.0d;
    double highlightArc =
      0.5f * tickThickness - 0.5f * (shadingProportion * tickThickness);

    ArrayList points = new ArrayList();
    ArrayList highlightPoints = new ArrayList();
    ArrayList shadowPoints = new ArrayList();

    points.add(
      new Point2D.Double(
        Math.cos(zeroLineRadians) * innerRadius,
        Math.sin(zeroLineRadians) * innerRadius
      )
    );
    highlightPoints.add(
      new Point2D.Double(
        Math.cos(zeroLineRadians + highlightArc / innerRadius) * innerRadius,
        Math.sin(zeroLineRadians + highlightArc / innerRadius) * innerRadius
      )
    );
    shadowPoints.add(
      new Point2D.Double(
        Math.cos(zeroLineRadians - highlightArc / innerRadius) * innerRadius,
        Math.sin(zeroLineRadians - highlightArc / innerRadius) * innerRadius
      )
    );

    for (int i = 0; i < zigzagNum; i++) {
      if (shiftLeft) {
        shiftLeft = false;
        zigzagRadius = zigzagRadius + zigzagLength;
        points.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians + zigzagRadiansShift) * (zigzagRadius),
            Math.sin(zeroLineRadians + zigzagRadiansShift) * (zigzagRadius)
          )
        );
        highlightPoints.add(
          new Point2D.Double(
            Math.cos(
              zeroLineRadians + zigzagRadiansShift + highlightArc / zigzagRadius
            ) *
            (zigzagRadius),
            Math.sin(
              zeroLineRadians + zigzagRadiansShift + highlightArc / zigzagRadius
            ) *
            (zigzagRadius)
          )
        );
        shadowPoints.add(
          new Point2D.Double(
            Math.cos(
              zeroLineRadians + zigzagRadiansShift - highlightArc / zigzagRadius
            ) *
            (zigzagRadius),
            Math.sin(
              zeroLineRadians + zigzagRadiansShift - highlightArc / zigzagRadius
            ) *
            (zigzagRadius)
          )
        );
        zigzagRadius = zigzagRadius + zigzagLength;
        points.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians) * (zigzagRadius),
            Math.sin(zeroLineRadians) * (zigzagRadius)
          )
        );
        highlightPoints.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians + highlightArc / zigzagRadius) *
            (zigzagRadius),
            Math.sin(zeroLineRadians + highlightArc / zigzagRadius) *
            (zigzagRadius)
          )
        );
        shadowPoints.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians - highlightArc / zigzagRadius) *
            (zigzagRadius),
            Math.sin(zeroLineRadians - highlightArc / zigzagRadius) *
            (zigzagRadius)
          )
        );
      } else {
        shiftLeft = true;
        zigzagRadius = zigzagRadius + zigzagLength;
        points.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians - zigzagRadiansShift) * (zigzagRadius),
            Math.sin(zeroLineRadians - zigzagRadiansShift) * (zigzagRadius)
          )
        );
        highlightPoints.add(
          new Point2D.Double(
            Math.cos(
              zeroLineRadians - zigzagRadiansShift + highlightArc / zigzagRadius
            ) *
            (zigzagRadius),
            Math.sin(
              zeroLineRadians - zigzagRadiansShift + highlightArc / zigzagRadius
            ) *
            (zigzagRadius)
          )
        );
        shadowPoints.add(
          new Point2D.Double(
            Math.cos(
              zeroLineRadians - zigzagRadiansShift - highlightArc / zigzagRadius
            ) *
            (zigzagRadius),
            Math.sin(
              zeroLineRadians - zigzagRadiansShift - highlightArc / zigzagRadius
            ) *
            (zigzagRadius)
          )
        );
        zigzagRadius = zigzagRadius + zigzagLength;
        points.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians) * (zigzagRadius),
            Math.sin(zeroLineRadians) * (zigzagRadius)
          )
        );
        highlightPoints.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians + highlightArc / zigzagRadius) *
            (zigzagRadius),
            Math.sin(zeroLineRadians + highlightArc / zigzagRadius) *
            (zigzagRadius)
          )
        );
        shadowPoints.add(
          new Point2D.Double(
            Math.cos(zeroLineRadians - highlightArc / zigzagRadius) *
            (zigzagRadius),
            Math.sin(zeroLineRadians - highlightArc / zigzagRadius) *
            (zigzagRadius)
          )
        );
      }
    }

    // add end points
    points.add(
      new Point2D.Double(
        Math.cos(zeroLineRadians) * outerRadius,
        Math.sin(zeroLineRadians) * outerRadius
      )
    );
    highlightPoints.add(
      new Point2D.Double(
        Math.cos(zeroLineRadians + highlightArc / outerRadius) * outerRadius,
        Math.sin(zeroLineRadians + highlightArc / outerRadius) * outerRadius
      )
    );
    shadowPoints.add(
      new Point2D.Double(
        Math.cos(zeroLineRadians - highlightArc / outerRadius) * outerRadius,
        Math.sin(zeroLineRadians - highlightArc / outerRadius) * outerRadius
      )
    );

    // now draw points
    Point2D previousPoint = null;
    Point2D currentPoint = null;
    Iterator i = points.iterator();
    Area zigzagArea = new Area();
    BasicStroke zigzagStroke = new BasicStroke(
      tickThickness,
      BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_BEVEL
    );
    if (i.hasNext()) {
      previousPoint = (Point2D) i.next();
    }
    while (i.hasNext()) {
      currentPoint = (Point2D) i.next();
      zigzagArea.add(
        new Area(
          zigzagStroke.createStrokedShape(
            new Line2D.Double(
              previousPoint.getX(),
              previousPoint.getY(),
              currentPoint.getX(),
              currentPoint.getY()
            )
          )
        )
      );
      previousPoint = currentPoint;
    }
    gg.setPaint(longTickColor);
    zigzagArea.intersect(new Area(this.getBackgroundRectangle()));
    gg.fill(zigzagArea);

    if (showShading) {
      // now draw highlight points
      previousPoint = null;
      currentPoint = null;
      i = highlightPoints.iterator();
      zigzagArea = new Area();
      zigzagStroke =
        new BasicStroke(
          tickThickness * shadingProportion,
          BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_BEVEL
        );
      if (i.hasNext()) {
        previousPoint = (Point2D) i.next();
      }
      while (i.hasNext()) {
        currentPoint = (Point2D) i.next();
        zigzagArea.add(
          new Area(
            zigzagStroke.createStrokedShape(
              new Line2D.Double(
                previousPoint.getX(),
                previousPoint.getY(),
                currentPoint.getX(),
                currentPoint.getY()
              )
            )
          )
        );
        previousPoint = currentPoint;
      }
      gg.setPaint(Color.white);
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, highlightOpacity)
      );
      zigzagArea.intersect(new Area(this.getBackgroundRectangle()));
      gg.fill(zigzagArea);

      // now draw shadow points
      previousPoint = null;
      currentPoint = null;
      i = shadowPoints.iterator();
      zigzagArea = new Area();
      zigzagStroke =
        new BasicStroke(
          tickThickness * shadingProportion,
          BasicStroke.CAP_ROUND,
          BasicStroke.JOIN_BEVEL
        );
      if (i.hasNext()) {
        previousPoint = (Point2D) i.next();
      }
      while (i.hasNext()) {
        currentPoint = (Point2D) i.next();
        zigzagArea.add(
          new Area(
            zigzagStroke.createStrokedShape(
              new Line2D.Double(
                previousPoint.getX(),
                previousPoint.getY(),
                currentPoint.getX(),
                currentPoint.getY()
              )
            )
          )
        );
        previousPoint = currentPoint;
      }
      gg.setPaint(Color.black);
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowOpacity)
      );
      zigzagArea.intersect(new Area(this.getBackgroundRectangle()));
      gg.fill(zigzagArea);

      // return to non transparent
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
      );
    }
  }

  /** Draws the tick marks. */
  private void drawTickMarks(int strand, double startRadius) {
    NumberFormat format = NumberFormat.getInstance();

    double approxBasesPerTick;
    double chosenBasesPerTick = 100000.0d;
    double chosenIncrement = 10.0d;
    double baseLabel = 0.0d;
    String chosenUnits = "mbp";
    int tickSig[];

    //         if ((strand == REVERSE_STRAND)) {
    //             tickSig = new int[5];
    //             tickSig[0] = 1;
    //             tickSig[1] = 5;
    //             tickSig[2] = 5;
    //             tickSig[3] = 5;
    //             tickSig[4] = 5;
    //         } else {
    //             tickSig = new int[4];
    //             tickSig[0] = 1;
    //             tickSig[1] = 4;
    //             tickSig[2] = 2;
    //             tickSig[3] = 4;
    //         }

    // 	tickSig = new int[8];
    // 	tickSig[0] = 1;
    // 	tickSig[1] = 6;
    // 	tickSig[2] = 4;
    // 	tickSig[3] = 6;
    // 	tickSig[4] = 2;
    // 	tickSig[5] = 6;
    // 	tickSig[6] = 4;
    // 	tickSig[7] = 6;

    tickSig = new int[10];
    tickSig[0] = 1;
    tickSig[1] = 5;
    tickSig[2] = 5;
    tickSig[3] = 5;
    tickSig[4] = 5;
    tickSig[5] = 2;
    tickSig[6] = 5;
    tickSig[7] = 5;
    tickSig[8] = 5;
    tickSig[9] = 5;

    int tickSigIndex = 0;
    // goodTicks is the actual number of bases covered per major tick mark
    int goodTicks[] = {
      1,
      2,
      5,
      10,
      20,
      50,
      100,
      200,
      500,
      1000,
      2000,
      5000,
      10000,
      20000,
      50000,
      100000,
      200000,
      500000,
      1000000,
      2000000,
      5000000,
      10000000,
    };
    // goodIncrements is the amount added to the number label placed next to the tick mark.
    double goodIncrements[] = {
      1.0d,
      2.0d,
      5.0d,
      10.0d,
      20.0d,
      50.0d,
      100.0d,
      200.0d,
      500.0d,
      1.0d,
      2.0d,
      5.0d,
      10.0d,
      20.0d,
      50.0d,
      100.0d,
      200.0d,
      500.0d,
      1.0d,
      2.0d,
      5.0d,
      10.0d,
    };
    // goodUnits is the base pair units used for the corresponding goodIncrements.
    String goodUnits[] = {
      " bp",
      " bp",
      " bp",
      " bp",
      " bp",
      " bp",
      " bp",
      " bp",
      " bp",
      " kbp",
      " kbp",
      " kbp",
      " kbp",
      " kbp",
      " kbp",
      " kbp",
      " kbp",
      " kbp",
      " mbp",
      " mbp",
      " mbp",
      " mbp",
    };
    int strandDirection;

    // goodCentisomeTickNumbers is an array of suitable numbers of ticks for marking centisome
    // position.
    double goodCentisomeTickNumbers[] = {
      1.0d,
      2.0d,
      5.0d,
      10.0d,
      20.0d,
      50.0d,
      100.0d,
      200.0d,
      500.0d,
      1000.0d,
      2000.0d,
      5000.0d,
      10000.0d,
      20000.0d,
      50000.0d,
      100000.0d,
      200000.0d,
      500000.0d,
      1000000.0d,
      2000000.0d,
      5000000.0d,
      10000000.0d,
    };
    // these are the increments that correspond to the goodCentisomeTickNumbers
    double goodCentisomeIncrements[] = {
      100.0d,
      50.0d,
      20.0d,
      10.0d,
      5.0d,
      2.0d,
      1.0d,
      0.5d,
      0.2d,
      0.1d,
      0.05d,
      0.02d,
      0.01d,
      0.005d,
      0.002d,
      0.001d,
      0.0005d,
      0.0002d,
      0.0001d,
      0.00005d,
      0.00002d,
      0.00001d,
    };
    // these are the number of decimal places to round the number label to
    int goodCentisomeRounds[] = {
      0,
      0,
      0,
      0,
      0,
      0,
      0,
      1,
      1,
      1,
      2,
      2,
      2,
      3,
      3,
      3,
      4,
      4,
      4,
      5,
      5,
      5,
    };
    int chosenCentisomeRound = 0;

    if (strand == DIRECT_STRAND) {
      strandDirection = 1;
    } else {
      strandDirection = -1;
    }

    // determine the base intervals for ticks.
    // may need to be reduced if there are not enough ticks or increased if there are too many ticks
    approxBasesPerTick =
      (double) (sequenceLength) /
      (
        (double) (desiredNumberOfTicks) *
        (
          (
            virtualBackboneRadius -
            (backboneRadius - this.getLastInnerFeatureRadius())
          ) /
          200.0d
        ) *
        tickDensity
      );

    if (rulerUnits == BASES) {
      for (int i = 0; i < goodTicks.length; i++) {
        if (
          (approxBasesPerTick < goodTicks[i]) || (i == (goodTicks.length - 1))
        ) {
          chosenBasesPerTick = goodTicks[i];
          chosenIncrement = goodIncrements[i];
          chosenUnits = goodUnits[i];
          break;
        }
      }
    } else if (rulerUnits == CENTISOMES) {
      for (int i = 0; i < goodCentisomeTickNumbers.length; i++) {
        // if ((((double) (desiredNumberOfTicks) * (virtualBackboneRadius / 200.0d)) <
        // goodCentisomeTickNumbers[i]) || (i == (goodCentisomeTickNumbers.length - 1))) {
        if (
          (
            (
              (double) (desiredNumberOfTicks) *
              (
                (
                  virtualBackboneRadius -
                  (backboneRadius - this.getLastInnerFeatureRadius())
                ) /
                200.0d
              ) *
              tickDensity
            ) <
            goodCentisomeTickNumbers[i]
          ) ||
          (i == (goodCentisomeTickNumbers.length - 1))
        ) {
          if (i > 0) {
            chosenBasesPerTick =
              sequenceLength / goodCentisomeTickNumbers[i - 1];
            chosenIncrement = goodCentisomeIncrements[i - 1];
            chosenCentisomeRound = goodCentisomeRounds[i - 1];
            chosenUnits = "centisome";

            format.setMaximumFractionDigits(chosenCentisomeRound);
            format.setMinimumFractionDigits(chosenCentisomeRound);
            break;
          } else {
            chosenBasesPerTick = sequenceLength / goodCentisomeTickNumbers[i];
            chosenIncrement = goodCentisomeIncrements[i];
            chosenCentisomeRound = goodCentisomeRounds[i];
            chosenUnits = "centisome";

            format.setMaximumFractionDigits(chosenCentisomeRound);
            format.setMinimumFractionDigits(chosenCentisomeRound);
            break;
          }
        }
      }
    }

    chosenBasesPerTick = chosenBasesPerTick / (double) (tickSig.length);

    double j = 0.0d;
    int endBase = sequenceLength;

    boolean finishedRanges = false;
    boolean finishedFirstRange = false;

    while (!(finishedRanges)) { // outer while loop
      if (drawEntirePlasmid) {
        finishedRanges = true;
      } else if (zoomRangeTwoStart != 0) {
        j = zoomRangeOneStart;
        // this next line makes j the nearest even multiple of chosenBasesPerTick
        j = (chosenBasesPerTick - j % chosenBasesPerTick) + j;
        endBase = zoomRangeTwoStop;
        finishedRanges = true;
      } else {
        if (finishedFirstRange) {
          j = zoomRangeTwoStart;
          // this next line makes j the nearest even multiple of chosenBasesPerTick
          j = (chosenBasesPerTick - j % chosenBasesPerTick) + j;
          endBase = zoomRangeTwoStop;
          finishedRanges = true;
        } else {
          j = zoomRangeOneStart;
          // this next line makes j the nearest even multiple of chosenBasesPerTick
          j = (chosenBasesPerTick - j % chosenBasesPerTick) + j;
          endBase = zoomRangeOneStop;
          finishedFirstRange = true;
        }
      }

      // while (j < endBase) {  //inner while loop
      while (j - 0.5d < endBase) {
        // the second part is the radians per base.
        double radians = getRadians(j);

        int iterations = (int) Math.floor(j / chosenBasesPerTick + 0.5f);

        tickSigIndex = iterations % tickSig.length;

        baseLabel = (iterations / tickSig.length) * chosenIncrement;

        double startX;
        double startY;

        double endX;
        double endY;

        double heightAdjust = (double) (tickSig[tickSigIndex]);

        Color currentTickColor = longTickColor;
        float currentTickThickness = tickThickness;

        if (tickSig[tickSigIndex] != 1) {
          currentTickColor = shortTickColor;
          currentTickThickness = shortTickThickness;
          //added 2018-03-30 to make short ticks longer
          //heightAdjust = (double) (tickSig[tickSigIndex]) / 2.0;
        }

        startX = (Math.cos(radians) * startRadius);
        startY = (Math.sin(radians) * startRadius);
        endX =
          (
            Math.cos(radians) *
            (startRadius + strandDirection * tickLength / heightAdjust)
          );
        endY =
          (
            Math.sin(radians) *
            (startRadius + strandDirection * tickLength / heightAdjust)
          );

        // check if tick marks are inside of canvas. Important for some output formats.

        if (
          (!backgroundRectangle.contains(startX, startY)) ||
          (!backgroundRectangle.contains(endX, endY))
        ) {
          j = j + chosenBasesPerTick;
          if ((strand == REVERSE_STRAND) && (tickSig[tickSigIndex] == 1)) {
            baseLabel = baseLabel + chosenIncrement;
          }
          continue;
        }

        gg.setPaint(currentTickColor);
        gg.setStroke(
          new BasicStroke(
            currentTickThickness,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_BEVEL
          )
        );
        gg.draw(new Line2D.Double(startX, startY, endX, endY));

        // below code creates labelBounds for every tick mark
        // if ((strand == REVERSE_STRAND)) {

        int tickBase;
        double TICK_BOUNDS_PADDING = 1.5d;

        double tickBoundsX =
          Math.cos(radians) *
          (startRadius + strandDirection * tickLength * 0.5d) -
          0.5d *
          (tickLength + currentTickThickness + TICK_BOUNDS_PADDING);
        double tickBoundsY =
          Math.sin(radians) *
          (startRadius + strandDirection * tickLength * 0.5d) -
          0.5d *
          (tickLength + currentTickThickness + TICK_BOUNDS_PADDING);

        if ((int) Math.floor(j + 0.5d) <= 0) {
          tickBase = 1;
        } else {
          tickBase = (int) Math.floor(j + 0.5d);
        }

        Rectangle2D tickBounds = new Rectangle2D.Double(
          tickBoundsX + width / 2 - centerPoint.getX(),
          tickBoundsY + height / 2 - centerPoint.getY(),
          tickLength + currentTickThickness + TICK_BOUNDS_PADDING,
          tickLength + currentTickThickness + TICK_BOUNDS_PADDING
        );

        LabelBounds rulerLabelBounds = new LabelBounds(this);
        rulerLabelBounds.setBounds(tickBounds);
        rulerLabelBounds.setLabel(Integer.toString(tickBase));
        rulerLabelBounds.setType(BOUNDS_RULER);
        rulerLabelBounds.setUse(true);
        rulerLabelBounds.setBase(tickBase);

        // draw bounds
        // gg.setPaint(Color.blue);
        // gg.setStroke( new BasicStroke( 0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL ));
        // gg.draw(tickBounds);
        // }

        if (showShading) {
          // for drawing highlights on ticks.
          double highlightArc =
            0.5d *
            currentTickThickness -
            0.5d *
            (shadingProportion * currentTickThickness);

          // now draw highlight line
          gg.setComposite(
            AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER,
              highlightOpacity
            )
          );
          gg.setPaint(Color.white);
          startX =
            (Math.cos(radians + highlightArc / startRadius) * startRadius);
          startY =
            (Math.sin(radians + highlightArc / startRadius) * startRadius);
          endX =
            (
              Math.cos(
                radians +
                highlightArc /
                (startRadius + strandDirection * tickLength / heightAdjust)
              ) *
              (startRadius + strandDirection * tickLength / heightAdjust)
            );
          endY =
            (
              Math.sin(
                radians +
                highlightArc /
                (startRadius + strandDirection * tickLength / heightAdjust)
              ) *
              (startRadius + strandDirection * tickLength / heightAdjust)
            );
          gg.setStroke(
            new BasicStroke(
              currentTickThickness * shadingProportion,
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_BEVEL
            )
          );
          gg.draw(new Line2D.Double(startX, startY, endX, endY));

          // draw shadow line
          gg.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowOpacity)
          );
          gg.setPaint(Color.black);
          startX =
            (Math.cos(radians - highlightArc / startRadius) * startRadius);
          startY =
            (Math.sin(radians - highlightArc / startRadius) * startRadius);
          endX =
            (
              Math.cos(
                radians -
                highlightArc /
                (startRadius + strandDirection * tickLength / heightAdjust)
              ) *
              (startRadius + strandDirection * tickLength / heightAdjust)
            );
          endY =
            (
              Math.sin(
                radians -
                highlightArc /
                (startRadius + strandDirection * tickLength / heightAdjust)
              ) *
              (startRadius + strandDirection * tickLength / heightAdjust)
            );
          gg.setStroke(
            new BasicStroke(
              currentTickThickness * shadingProportion,
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_BEVEL
            )
          );
          gg.draw(new Line2D.Double(startX, startY, endX, endY));

          // set the composite back to 1
          gg.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
          );
        }

        if ((strand == REVERSE_STRAND) && (tickSig[tickSigIndex] == 1)) {
          // add numbering to the inside of the figure.
          if (baseLabel > 0) {
            // recalculate startX and startY using rulerTextPadding.
            startX =
              (
                Math.cos(radians) *
                (
                  startRadius -
                  rulerTextPadding -
                  tickLength -
                  0.5d *
                  tickThickness
                )
              );
            startY =
              (
                Math.sin(radians) *
                (
                  startRadius -
                  rulerTextPadding -
                  tickLength -
                  0.5d *
                  tickThickness
                )
              );

            FontRenderContext frc = gg.getFontRenderContext();
            TextLayout layout;

            String rulerLabel;

            if (rulerUnits == BASES) {
              rulerLabel = Integer.toString((int) baseLabel) + chosenUnits;
              // layout = new TextLayout(Integer.toString((int)baseLabel) + chosenUnits, rulerFont,
              // frc);
            } else {
              rulerLabel = format.format(baseLabel);
              // String baseLabelString = format.format(baseLabel);
              // layout = new TextLayout(baseLabelString, rulerFont, frc);
            }

            layout = new TextLayout(rulerLabel, rulerFont, frc);
            Rectangle2D bounds = layout.getBounds();
            double textHeight = bounds.getHeight();
            double textWidth = bounds.getWidth();

            // double textHeight = layout.getAscent() + layout.getDescent();
            // double textWidth = layout.getAdvance();

            double textPositionX = startX;
            double textPositionY = startY;

            // adjust text position based on radians for label.
            if (
              (Math.sin(radians) <= 1.0d) &&
              (Math.sin(radians) >= 0.0d) &&
              (Math.cos(radians) >= 0.0d) &&
              (Math.cos(radians) <= 1.0d)
            ) { // 0 to 90 degrees
              textPositionX =
                textPositionX -
                textWidth +
                ((0.5d * textWidth) * (Math.sin(radians)));
              textPositionY =
                textPositionY +
                0.5d *
                textHeight -
                ((0.5d * textHeight) * (Math.sin(radians)));
            } else if (
              (Math.sin(radians) <= 1.0d) &&
              (Math.sin(radians) >= 0.0d) &&
              (Math.cos(radians) <= 0.0d) &&
              (Math.cos(radians) >= -1.0d)
            ) { // 90 to 180 degrees
              textPositionX =
                textPositionX - ((0.5d * textWidth) * (Math.sin(radians)));
              textPositionY =
                textPositionY +
                0.5d *
                textHeight -
                ((0.5d * textHeight) * (Math.sin(radians)));
            } else if (
              (Math.sin(radians) <= 0.0d) &&
              (Math.sin(radians) >= -1.0d) &&
              (Math.cos(radians) <= 0.0d) &&
              (Math.cos(radians) >= -1.0d)
            ) { // 180 to 270 degrees
              textPositionX =
                textPositionX + ((0.5d * textWidth) * (Math.sin(radians)));
              textPositionY =
                textPositionY +
                0.5d *
                textHeight -
                ((0.5d * textHeight) * (Math.sin(radians)));
            } else {
              textPositionX =
                textPositionX -
                textWidth -
                ((0.5d * textWidth) * (Math.sin(radians)));
              textPositionY =
                textPositionY +
                0.5d *
                textHeight -
                ((0.5d * textHeight) * (Math.sin(radians)));
            }

            // compensate is to correct for the way that java seems to shift the bound box down and
            // to the right.
            // double compensate = 0.5d;
            double compensate = 0.0d;
            gg.setPaint(backgroundColor);
            bounds.setRect(
              bounds.getX() + textPositionX - 1.5d - compensate,
              bounds.getY() +
              textPositionY -
              layout.getDescent() -
              1.5d -
              compensate,
              bounds.getWidth() + 3.0d,
              bounds.getHeight() + 3.0d
            );

            // check if label fits inside canvas

            if (backgroundRectangle.contains(bounds)) {
              // gg.setPaint(Color.blue);
              // gg.setStroke( new BasicStroke( 0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL
              // ));
              // gg.draw(bounds);

              gg.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)
              );
              gg.fill(bounds);
              gg.setComposite(
                AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
              );

              gg.setPaint(rulerFontColor);
              // layout.draw(gg, (float) textPositionX, (float) textPositionY -
              // layout.getDescent());

              // this is to avoid problem with imprecisely positioned text when using large
              // coordinates
              untranslateCanvas();
              layout.draw(
                gg,
                (float) (textPositionX + width / 2 - centerPoint.getX()),
                (float) (textPositionY + height / 2 - centerPoint.getY()) -
                layout.getDescent()
              );
              translateCanvas();
              /////

            }
          }
          baseLabel = baseLabel + chosenIncrement;
        }

        j = j + chosenBasesPerTick;
      } // end of inner while loop
    } // end of outer while loop

    // draw a zero line if it should be visible.
    if ((drawEntirePlasmid) || (zoomRangeTwoStart == 0)) {
      // now draw a zero line
      double zeroLineRadians = getRadians(0);
      double zeroStartX = (Math.cos(zeroLineRadians) * startRadius);
      double zeroStartY = (Math.sin(zeroLineRadians) * startRadius);
      double zeroEndX =
        (
          Math.cos(zeroLineRadians) *
          (startRadius + strandDirection * tickLength)
        );
      double zeroEndY =
        (
          Math.sin(zeroLineRadians) *
          (startRadius + strandDirection * tickLength)
        );

      // check whether zero line will fit in canvas
      if (
        (backgroundRectangle.contains(zeroStartX, zeroStartY)) ||
        (backgroundRectangle.contains(zeroEndX, zeroEndY))
      ) {
        gg.setPaint(zeroTickColor);
        gg.setStroke(
          new BasicStroke(
            tickThickness,
            BasicStroke.CAP_ROUND,
            BasicStroke.JOIN_BEVEL
          )
        );
        gg.draw(new Line2D.Double(zeroStartX, zeroStartY, zeroEndX, zeroEndY));

        double TICK_BOUNDS_PADDING = 1.5d;

        // double tickBoundsX = Math.cos(zeroLineRadians) * (startRadius + strandDirection *
        // tickLength * 0.5d) - 0.5d * (tickLength + tickThickness + TICK_BOUNDS_PADDING);
        // double tickBoundsY = Math.sin(zeroLineRadians) * (startRadius + strandDirection *
        // tickLength * 0.5d) - 0.5d * (tickLength + tickThickness + TICK_BOUNDS_PADDING);
        double heightAdjust = 1.0d;
        double tickBoundsX =
          Math.cos(zeroLineRadians) *
          (startRadius + strandDirection * tickLength * 0.5d) -
          0.5d *
          (tickLength + tickThickness + TICK_BOUNDS_PADDING);
        double tickBoundsY =
          Math.sin(zeroLineRadians) *
          (startRadius + strandDirection * tickLength * 0.5d) -
          0.5d *
          (tickLength + tickThickness + TICK_BOUNDS_PADDING);

        Rectangle2D tickBounds = new Rectangle2D.Double(
          tickBoundsX + width / 2 - centerPoint.getX(),
          tickBoundsY + height / 2 - centerPoint.getY(),
          tickLength + tickThickness + TICK_BOUNDS_PADDING,
          tickLength + tickThickness + TICK_BOUNDS_PADDING
        );

        // Rectangle2D tickBounds = new Rectangle2D.Double(tickBoundsX, tickBoundsY, tickLength +
        // tickThickness + TICK_BOUNDS_PADDING, tickLength + tickThickness + TICK_BOUNDS_PADDING);

        LabelBounds rulerLabelBounds = new LabelBounds(this);
        rulerLabelBounds.setBounds(tickBounds);
        rulerLabelBounds.setLabel(Integer.toString(1));
        rulerLabelBounds.setType(BOUNDS_RULER);
        rulerLabelBounds.setUse(true);
        rulerLabelBounds.setBase(1);

        gg.setPaint(Color.red);
        //// gg.setStroke( new BasicStroke( 0.1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL ));
        // gg.fill(tickBounds);

        if (showShading) {
          // for drawing highlights on ticks.
          double highlightArc =
            0.5f * tickThickness - 0.5f * (shadingProportion * tickThickness);

          // now draw highlight line
          gg.setComposite(
            AlphaComposite.getInstance(
              AlphaComposite.SRC_OVER,
              highlightOpacity
            )
          );
          gg.setPaint(Color.white);
          zeroStartX =
            (
              Math.cos(zeroLineRadians + highlightArc / startRadius) *
              startRadius
            );
          zeroStartY =
            (
              Math.sin(zeroLineRadians + highlightArc / startRadius) *
              startRadius
            );
          zeroEndX =
            (
              Math.cos(
                zeroLineRadians +
                highlightArc /
                (startRadius + strandDirection * tickLength)
              ) *
              (startRadius + strandDirection * tickLength)
            );
          zeroEndY =
            (
              Math.sin(
                zeroLineRadians +
                highlightArc /
                (startRadius + strandDirection * tickLength)
              ) *
              (startRadius + strandDirection * tickLength)
            );
          gg.setStroke(
            new BasicStroke(
              tickThickness * shadingProportion,
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_BEVEL
            )
          );
          gg.draw(
            new Line2D.Double(zeroStartX, zeroStartY, zeroEndX, zeroEndY)
          );

          // draw shadow line
          gg.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowOpacity)
          );
          gg.setPaint(Color.black);
          zeroStartX =
            (
              Math.cos(zeroLineRadians - highlightArc / startRadius) *
              startRadius
            );
          zeroStartY =
            (
              Math.sin(zeroLineRadians - highlightArc / startRadius) *
              startRadius
            );
          zeroEndX =
            (
              Math.cos(
                zeroLineRadians -
                highlightArc /
                (startRadius + strandDirection * tickLength)
              ) *
              (startRadius + strandDirection * tickLength)
            );
          zeroEndY =
            (
              Math.sin(
                zeroLineRadians -
                highlightArc /
                (startRadius + strandDirection * tickLength)
              ) *
              (startRadius + strandDirection * tickLength)
            );
          gg.setStroke(
            new BasicStroke(
              tickThickness * shadingProportion,
              BasicStroke.CAP_BUTT,
              BasicStroke.JOIN_BEVEL
            )
          );
          gg.draw(
            new Line2D.Double(zeroStartX, zeroStartY, zeroEndX, zeroEndY)
          );

          // set the composite back to 1
          gg.setComposite(
            AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
          );
        }
      }
    }
  }

  /** Places the map title. */
  private void placeTitle() {
    FontRenderContext frc;
    TextLayout layout;
    Rectangle2D bounds;
    titleRectangle = new Rectangle2D.Double();
    lengthRectangle = new Rectangle2D.Double();
    double titleTextPositionX = 0.0d;
    double titleTextPositionY = 0.0d;

    double numberTextPositionX = 0.0d;
    double numberTextPositionY = 0.0d;

    // draw the title of the plasmid if there is one.
    if (title == null) {
      title = "";
    }
    Pattern p = Pattern.compile("\\S");
    Matcher m = p.matcher(title);

    if ((m.find())) {
      frc = gg.getFontRenderContext();
      layout = new TextLayout(title, titleFont, frc);
      bounds = layout.getBounds();
      titleTextPositionX = 0.0d - 0.5d * layout.getAdvance();
      titleTextPositionY = 0.0d;
      titleRectangle.setRect(
        bounds.getX() + titleTextPositionX - 1.5d,
        bounds.getY() + titleTextPositionY - layout.getDescent() - 1.5d,
        bounds.getWidth() + 3.0d,
        bounds.getHeight() + 3.0d
      );

      // draw the length of the plasmid.
      frc = gg.getFontRenderContext();
      layout =
        new TextLayout(
          Integer.toString(sequenceLength) + " bp",
          titleFont,
          frc
        );
      bounds = layout.getBounds();
      numberTextPositionX = numberTextPositionX - 0.5d * layout.getAdvance();
      numberTextPositionY =
        titleTextPositionY +
        layout.getDescent() +
        layout.getLeading() +
        layout.getDescent() +
        layout.getAscent();
      lengthRectangle.setRect(
        bounds.getX() + numberTextPositionX - 1.5d,
        bounds.getY() + numberTextPositionY - layout.getDescent() - 1.5d,
        bounds.getWidth() + 3.0d,
        bounds.getHeight() + 3.0d
      );
    }
    // if no title, create a small titleRectangle to prevent innerLabels from crossing center
    else {
      titleRectangle.setRect(-5.0d, -5.0d, 10.0d, 10.0d);
      // gg.setPaint(Color.red);
      // gg.draw(titleRectangle);
    }
  }

  /** Draws the map title. */
  private void drawTitle() {
    FontRenderContext frc;
    TextLayout layout;
    Rectangle2D bounds;
    titleRectangle = new Rectangle2D.Double();
    lengthRectangle = new Rectangle2D.Double();
    double titleTextPositionX = 0.0d;
    double titleTextPositionY = 0.0d;

    double numberTextPositionX = 0.0d;
    double numberTextPositionY = 0.0d;

    // draw the title of the plasmid if there is one.
    if (title == null) {
      title = "";
    }
    Pattern p = Pattern.compile("\\S");
    Matcher m = p.matcher(title);

    if ((m.find())) {
      frc = gg.getFontRenderContext();
      layout = new TextLayout(title, titleFont, frc);
      bounds = layout.getBounds();
      titleTextPositionX = 0.0d - 0.5d * layout.getAdvance();
      titleTextPositionY = 0.0d;
      gg.setPaint(titleFontColor);
      titleRectangle.setRect(
        bounds.getX() + titleTextPositionX - 1.5d,
        bounds.getY() + titleTextPositionY - layout.getDescent() - 1.5d,
        bounds.getWidth() + 3.0d,
        bounds.getHeight() + 3.0d
      );
      if (backgroundRectangle.contains(titleRectangle)) {
        layout.draw(gg, (float) titleTextPositionX, (float) titleTextPositionY);
      } else {
        System.err.println(
          "[warning] the plasmid title was too big for the canvas and was removed."
        );
      }

      // draw the length of the plasmid.
      frc = gg.getFontRenderContext();
      layout =
        new TextLayout(
          Integer.toString(sequenceLength) + " bp",
          titleFont,
          frc
        );
      bounds = layout.getBounds();
      numberTextPositionX = numberTextPositionX - 0.5d * layout.getAdvance();
      numberTextPositionY =
        titleTextPositionY +
        layout.getDescent() +
        layout.getLeading() +
        layout.getDescent() +
        layout.getAscent();
      lengthRectangle.setRect(
        bounds.getX() + numberTextPositionX - 1.5d,
        bounds.getY() + numberTextPositionY - layout.getDescent() - 1.5d,
        bounds.getWidth() + 3.0d,
        bounds.getHeight() + 3.0d
      );
      gg.setPaint(titleFontColor);
      if (backgroundRectangle.contains(lengthRectangle)) {
        layout.draw(
          gg,
          (float) numberTextPositionX,
          (float) numberTextPositionY
        );
      } else {
        System.out.println(
          "[warning] the plasmid length was too big for the canvas and was removed."
        );
      }
    }
    // if no title, create a small titleRectangle to prevent innerLabels from crossing center
    else {
      titleRectangle.setRect(-5.0d, -5.0d, 10.0d, 10.0d);
      // gg.setPaint(Color.red);
      // gg.draw(titleRectangle);
    }
  }

  /** Draws a warning message at the bottom of the map. */
  private void drawWarningMessage() {
    if ((showWarning) && (totalLabels > 0)) {
      String textToShow = "";
      if (showWarning) {
        textToShow = textToShow + warningText;
      }

      if ((zoomMultiplier == 1.0d) && (clashLabels != 0)) {
        if (clashLabels == 1) {
          textToShow =
            textToShow +
            "Warning: " +
            clashLabels +
            " of the " +
            totalLabels +
            " labels is not shown. ";
        } else {
          textToShow =
            textToShow +
            "Warning: " +
            clashLabels +
            " of the " +
            totalLabels +
            " labels are not shown. ";
        }
      } else if ((zoomMultiplier == 1.0d) && (clashLabels == 0)) {
        textToShow =
          textToShow +
          totalLabels +
          " of the " +
          totalLabels +
          " labels are shown. ";
      }

      Pattern p = Pattern.compile("\\S");
      Matcher m = p.matcher(textToShow);

      if ((m.find())) {
        infoLegend = new Legend(this);
        infoLegend.setFont(warningFont);
        infoLegend.setBackgroundColor(backgroundColor);
        infoLegend.setBackgroundOpacity(0.2f);
        infoLegend.setAllowLabelClash(false);
        infoLegend.setPosition(LEGEND_LOWER_LEFT);
        LegendItem infoContent = new LegendItem(infoLegend);
        infoContent.setDrawSwatch(SWATCH_NO_SHOW);
        infoContent.setLabel(textToShow);
        infoContent.setFontColor(warningFontColor);
      }
    }
  }

  /**
   * Draws the feature labels. Call this method for InnerLabel objects, and then OuterLabel objects.
   *
   * @param labels a collection of labels. The collection must consist of OuterLabel objects, or
   *     InnerLabel objects, but not both.
   */
  private void drawLabels(ArrayList labels) {
    Iterator i;
    Comparator comparator;

    double outerLabelStart;
    double innerLabelStart;

    if (drawTickMarks) {
      outerLabelStart =
        this.getLastOuterFeatureRadius() +
        tickLength +
        tickThickness +
        featureSlotSpacing;

      innerLabelStart =
        this.getLastInnerFeatureRadius() -
        tickLength -
        tickThickness -
        featureSlotSpacing;
    } else {
      outerLabelStart = this.getLastOuterFeatureRadius() + featureSlotSpacing;

      innerLabelStart = this.getLastInnerFeatureRadius() - featureSlotSpacing;
    }

    int adjustedClashSpan = clashSpan;

    // the below code will keep labels in a square with width = min(canvas length, canvas width)
    // this will be undone automatically at the end of this method
    // because of a call to untranslateCanvas()
    // if (drawEntirePlasmid) {
    //    backgroundRectangle = new Rectangle2D.Double(backgroundRectangle.getX(),
    // backgroundRectangle.getY(), smallestDimension, smallestDimension);
    // }

    // System.out.println ("Set radius");
    System.out.print(".");
    i = labels.iterator();
    while (i.hasNext()) {
      Label currentLabel = (Label) i.next();
      if (currentLabel instanceof OuterLabel) {
        currentLabel.setLineStartRadius(outerLabelStart);
      } else {
        currentLabel.setLineStartRadius(innerLabelStart);
      }
    }

    // System.out.println ("Shuffle");
    System.out.print(".");
    if ((labelShuffle) && (labels.size() > 50)) {
      Collections.shuffle(labels);
    }

    // System.out.println ("Trim");
    // Remove labels until labelsToKeep labels remain. If a label does not clash with any labels, do
    // not remove it.
    System.out.print(".");
    if (labels.size() > labelsToKeep) {
      for (int j = 0; j < labels.size() - labelsToKeep; j++) {
        if (((Label) labels.get(j)).getForceLabel()) {
          continue;
        }
        for (int k = j; k < labels.size(); k++) {
          if (
            (((Label) labels.get(j)).getBounds()).intersects(
                ((Label) labels.get(k)).getBounds()
              )
          ) {
            labels.remove(j);
            j--;
            clashLabels++;
            break;
          }
        }
      }
    }

    // System.out.println ("Sort by radians");
    System.out.print(".");
    comparator = new SortLabelsByRadians();
    Collections.sort(labels, comparator);

    // set bounds with padding set to 3
    i = labels.iterator();
    while (i.hasNext()) {
      ((Label) i.next()).updateBounds(3.0d);
    }

    // System.out.println ("Radians shifting");
    System.out.print(".");
    if (labels.size() > 1) {
      boolean noClash;
      int upper;
      int lower;
      for (int outer = 0; outer < spreadIterations; outer++) {
        noClash = true;
        for (int inner = 0; inner < labels.size(); inner++) {
          if (inner == 0) {
            if (
              ((Label) labels.get(1)).getLineStartRadians() -
              ((Label) labels.get(0)).getLineStartRadians() >
              Math.PI
            ) {
              continue;
            } else {
              upper = 1;
              lower = 0;
            }
          }

          if (inner == (labels.size() - 1)) {
            if (
              ((Label) labels.get(inner)).getLineStartRadians() -
              ((Label) labels.get(0)).getLineStartRadians() <
              Math.PI
            ) {
              continue;
            } else {
              upper = inner;
              lower = 0;
            }
          } else {
            upper = inner;
            lower = inner + 1;
          }

          if (
            (((Label) labels.get(upper)).getBounds()).intersects(
                ((Label) labels.get(lower)).getBounds()
              )
          ) {
            noClash = false;
            ((Label) labels.get(upper)).shiftRadiansLower();
            ((Label) labels.get(lower)).shiftRadiansHigher();
          }
        } // inner loop
        if (noClash) {
          break;
        }
      } // outer loop
    }

    // set bounds with padding set to 2
    i = labels.iterator();
    while (i.hasNext()) {
      ((Label) i.next()).updateBounds(2.0d);
    }

    // System.out.println ("Radius shifting and removing");
    System.out.print(".");
    if (labels.size() > 1) {
      int j;
      int checked;

      for (int outer = 0; outer < labels.size(); outer++) {
        if (((Label) labels.get(outer)).getForceLabel()) {
          continue;
        }

        if ((adjustedClashSpan) > (labels.size() / 2)) {
          adjustedClashSpan = labels.size() / 2;
        }

        j = outer - adjustedClashSpan;
        checked = 0;
        while (checked < (adjustedClashSpan * 2)) {
          if (j == labels.size()) {
            j = 0;
          } else if (j < 0) {
            j = labels.size() + j;
          }

          if (j == outer) {
            j++;
            continue;
          }

          if (
            (((Label) labels.get(outer)).getBounds()).intersects(
                ((Label) labels.get(j)).getBounds()
              )
          ) {
            if (((Label) labels.get(outer)).extendRadius()) {
              j = outer - adjustedClashSpan;
              checked = 0;

              continue;
            } else {
              if (
                (!moveInnerLabelsToOuter) ||
                (((Label) labels.get(outer)) instanceof OuterLabel)
              ) {
                labels.remove(outer);
                clashLabels++;
                outer = outer - 1;
                break;
              } else {
                // convert innerLabel to an outerLabel
                new OuterLabel((InnerLabel) labels.get(outer));
                labels.remove(outer);
                outer = outer - 1;
                break;
              }
            }
          }
          checked++;
          j++;
        }
      }
    }

    // set bounds with padding set to 1.5
    i = labels.iterator();
    while (i.hasNext()) {
      ((Label) i.next()).updateBounds(1.5d);
    }

    // System.out.println ("Remove labels that clash with other labels");
    System.out.print(".");
    Collections.shuffle(labels);
    if (labels.size() > 1) {
      for (int outer = 0; outer < labels.size(); outer++) {
        if (((Label) labels.get(outer)).getForceLabel()) {
          continue;
        }
        for (int inner = outer + 1; inner < labels.size(); inner++) {
          if (
            (((Label) labels.get(outer)).getBounds()).intersects(
                ((Label) labels.get(inner)).getBounds()
              )
          ) {
            if (
              (!moveInnerLabelsToOuter) ||
              (((Label) labels.get(outer)) instanceof OuterLabel)
            ) {
              // ((Label)labels.get(outer)).draw();
              labels.remove(outer);
              clashLabels++;
              outer = outer - 1;
              break;
            } else {
              // convert innerLabel to an outerLabel
              new OuterLabel((InnerLabel) labels.get(outer));
              labels.remove(outer);
              outer = outer - 1;
              break;
            }
          }
        }
      }
    }

    // System.out.println ("Sort by radians");
    // sort labels by radius
    // System.out.print(".");
    // comparator = new SortLabelsByRadians();
    // Collections.sort(labels, comparator);

    // System.out.println ("Sort by radius");
    // sort labels by radius
    // System.out.print(".");
    // comparator = new SortLabelsByRadius();
    // Collections.sort(labels, comparator);

    // System.out.println ("Remove labels that still clash with map elements");
    // go through the labels again and remove those that clash with the map title, map length, inner
    // FeatureSlot, or outer FeatureSlot.
    System.out.print(".");
    if (labels.size() > 0) {
      Label currentLabel;
      for (int j = 0; j < labels.size(); j++) {
        currentLabel = (Label) (labels.get(j));
        if (currentLabel instanceof OuterLabel) {
          if (
            (outerArc.intersects(currentLabel.getBounds())) ||
            !(backgroundRectangle.contains(currentLabel.getBounds())) ||
            !(backgroundRectangle.contains(currentLabel.getLineStart()))
          ) {
            if (!currentLabel.getForceLabel()) {
              if (
                (!moveInnerLabelsToOuter) ||
                (currentLabel instanceof OuterLabel)
              ) {
                labels.remove(j);
                clashLabels++;
                j = j - 1;
              } else {
                // convert innerLabel to an outerLabel
                new OuterLabel((InnerLabel) currentLabel);
                labels.remove(j);
                j = j - 1;
              }
            }
          }
        } else {
          if (drawEntirePlasmid) {
            if (
              (titleRectangle.intersects(currentLabel.getBounds())) ||
              (lengthRectangle.intersects(currentLabel.getBounds())) ||
              !(innerArc.contains(currentLabel.getBounds())) ||
              (titleRectangle.contains(currentLabel.getBounds())) ||
              (lengthRectangle.contains(currentLabel.getBounds())) ||
              !(backgroundRectangle.contains(currentLabel.getBounds())) ||
              !(backgroundRectangle.contains(currentLabel.getLineStart()))
            ) {
              if (!currentLabel.getForceLabel()) {
                if (
                  (!moveInnerLabelsToOuter) ||
                  (currentLabel instanceof OuterLabel)
                ) {
                  labels.remove(j);
                  clashLabels++;
                  j = j - 1;
                } else {
                  // convert innerLabel to an outerLabel
                  new OuterLabel((InnerLabel) currentLabel);
                  labels.remove(j);
                  j = j - 1;
                }
              }
            }
          } else {
            // this may need fixing if labels on the inside of the backbone circle are being drawn
            // when the shouldn't
            if (
              !(backgroundRectangle.contains(currentLabel.getBounds())) ||
              !(backgroundRectangle.contains(currentLabel.getLineStart()))
            ) {
              if (!currentLabel.getForceLabel()) {
                if (
                  (!moveInnerLabelsToOuter) ||
                  (currentLabel instanceof OuterLabel)
                ) {
                  labels.remove(j);
                  clashLabels++;
                  j = j - 1;
                } else {
                  // convert innerLabel to an outerLabel
                  new OuterLabel((InnerLabel) currentLabel);
                  labels.remove(j);
                  j = j - 1;
                }
              }
            }
            // this wass a bug. with the code below lots of inner labels were not being drawn. new
            // code above
            //                         if (!(innerArc.contains(currentLabel.getBounds())) ||
            // !(backgroundRectangle.contains(currentLabel.getBounds())) ||
            // !(backgroundRectangle.contains(currentLabel.getLineStart()))) {
            //                             if (!currentLabel.getForceLabel()) {
            //                                 if ((!moveInnerLabelsToOuter) || (currentLabel
            // instanceof OuterLabel)) {
            //                                     labels.remove(j);
            //                                     clashLabels++;
            //                                     j = j - 1;
            //                                 } else {
            //                                     //convert innerLabel to an outerLabel
            //                                     new OuterLabel((InnerLabel) currentLabel);
            //                                     labels.remove(j);
            //                                     j = j - 1;
            //                                 }
            //                             }
            //                         }

          }
        }
      }
    }

    // System.out.println ("Remove labels that clash with legends");
    // go through the labels again and remove those that clash with legends.
    System.out.print(".");
    if (labels.size() > 0) {
      i = legends.iterator();
      Rectangle2D legendBounds;
      Label currentLabel;
      Legend currentLegend;
      while (i.hasNext()) {
        currentLegend = (Legend) i.next();
        if (!(currentLegend.getAllowLabelClash())) {
          legendBounds = currentLegend.getBounds();

          for (int j = 0; j < labels.size(); j++) {
            currentLabel = (Label) (labels.get(j));
            if (
              (!currentLabel.getForceLabel()) &&
              (
                (legendBounds.contains(currentLabel.getBounds())) ||
                (legendBounds.intersects(currentLabel.getBounds()))
              )
            ) {
              if (
                (!moveInnerLabelsToOuter) ||
                (currentLabel instanceof OuterLabel)
              ) {
                labels.remove(j);
                clashLabels++;
                j = j - 1;
              } else {
                // convert innerLabel to an outerLabel
                new OuterLabel((InnerLabel) currentLabel);
                labels.remove(j);
                j = j - 1;
              }
            }
          }
        }
      }
    }

    // System.out.println ("Sort by forceLabel");
    // sort labels by forceLabel so that those that have forceLabel = true are drawn last.
    System.out.print(".");
    comparator = new SortLabelsByForceLabel();
    Collections.sort(labels, comparator);

    // System.out.println ("Draw label lines");
    // draw label lines
    System.out.print(".");
    i = labels.iterator();
    while (i.hasNext()) {
      Label currentLabel = (Label) i.next();
      currentLabel.drawLabelLine();
    }

    // System.out.println ("Draw label text");
    // draw label text
    untranslateCanvas();
    System.out.print(".");
    i = labels.iterator();
    while (i.hasNext()) {
      Label currentLabel = (Label) i.next();
      currentLabel.drawLabelText();

      // now create a labelBounds object
      if (
        (currentLabel.getHyperlink() != null) ||
        (currentLabel.getMouseover() != null)
      ) {
        LabelBounds labelBounds = new LabelBounds(this);
        labelBounds.setBounds(currentLabel.getBounds());
        labelBounds.setLabel(currentLabel.getLabelText());
        labelBounds.setType(BOUNDS_FEATURE);
        labelBounds.setHyperlink(currentLabel.getHyperlink());
        labelBounds.setMouseover(currentLabel.getMouseover());
        labelBounds.setUse(true);
      }
      // remove once drawn
      // i.remove();
    }
    translateCanvas();
  }

  public static void main(String ars[]) {}
}
