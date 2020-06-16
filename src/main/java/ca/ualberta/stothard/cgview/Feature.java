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
import java.util.*;

/**
 * This class represents a sequence feature. Feature objects (along with the FeatureRange objects)
 * are used to describe individual sequence features. Feature objects can specify, for example, the
 * color, opacity, and thickness of a sequence feature. They can also be used to provide a text
 * label. The Feature object does not dictate which bases contain the sequence feature. The position
 * information is instead provided by FeatureRange objects contained by the Feature objects.
 *
 * @author Paul Stothard
 */
public class Feature implements CgviewConstants {
  private String label;
  private String hyperlink;
  private String mouseover;
  private ArrayList ranges = new ArrayList();
  private Color color = new Color(0, 0, 255); // blue
  private int strand;
  private Font font;
  private boolean forceLabel = false;
  private int showLabel = LABEL;

  private int decoration = DECORATION_STANDARD;
  private float radiusAdjustment = 0.0f;
  private float proportionOfThickness = 1.0f;
  private float opacity = 1.0f;

  private boolean showShading;

  /**
   * Constructs a new Feature object.
   *
   * @param featureSlot the FeatureSlot object to contain this Feature.
   * @param label the text label for this Feature. This label will be used in any labels generated
   *     for the Feature when it is drawn.
   */
  public Feature(FeatureSlot featureSlot, String label) {
    this.label = label;
    this.strand = featureSlot.getStrand();
    this.showShading = featureSlot.getShowShading();
    // add this feature to the FeatureSlot.
    featureSlot.addFeature(this);
  }

  /**
   * Constructs a new Feature object.
   *
   * @param featureSlot the FeatureSlot object to contain this Feature.
   */
  public Feature(FeatureSlot featureSlot) {
    this.strand = featureSlot.getStrand();
    this.showShading = featureSlot.getShowShading();
    // add this feature to the FeatureSlot.
    featureSlot.addFeature(this);
  }

  /**
   * Constructs a new Feature object. The Feature object must be added to a FeatureSlot using the
   * {@link #setFeatureSlot(FeatureSlot) setFeatureSlot()} method. method.
   */
  public Feature() {}

  /**
   * Constructs a new Feature object. The Feature object must be added to a FeatureSlot using the
   * {@link #setFeatureSlot(FeatureSlot) setFeatureSlot()} method. method.
   *
   * @param showShading whether or not this Feature should be drawn with shading.
   */
  public Feature(boolean showShading) {
    this.showShading = showShading;
  }

  /**
   * Places this Feature into a FeatureSlot.
   *
   * @param featureSlot the FeatureSlot to contain this Feature.
   */
  public void setFeatureSlot(FeatureSlot featureSlot) {
    this.strand = featureSlot.getStrand();
    featureSlot.addFeature(this);
  }

  /**
   * Adds a FeatureRange to this Feature.
   *
   * @param featureRange the FeatureRange object to add to this Feature.
   */
  protected void addRange(FeatureRange featureRange) {
    ranges.add(featureRange);
  }

  /**
   * Returns an ArrayList of FeatureRange objects contained by this Feature.
   *
   * @return an ArrayList of FeatureRange objects.
   */
  protected ArrayList getRanges() {
    return ranges;
  }

  /**
   * Sets the default color that will be assigned to FeatureRange objects added to this Feature.
   * This color can be changed for individual FeatureRange objects using {@link
   * FeatureRange#setColor(Color) FeatureRange.setColor()}.
   *
   * @param color the default FeatureRange color.
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Returns the default color that will be assigned to FeatureRange objects added to this Feature.
   *
   * @return the default FeatureRange color.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Specifies whether or not labels created for this Feature should be drawn even if they cannot be
   * placed such that they do not clash with other labels. This behaviour can be set for individual
   * FeatureRange objects using {@link FeatureRange#setForceLabel(boolean)
   * FeatureRange.setForceLabel()}.
   *
   * @param forceLabel a boolean specifying whether or not to draw labels for this Feature even if
   *     they cannot be placed such that they do not clash with other labels.
   */
  public void setForceLabel(boolean forceLabel) {
    this.forceLabel = forceLabel;
    if (this.forceLabel == true) {
      showLabel = LABEL_FORCE;
    }
  }

  /**
   * Returns a boolean specifying whether or not labels created for this Feature should be drawn
   * even if they cannot be placed such that they do not clash with other labels.
   *
   * @return whether or not to draw labels for this Feature even if they cannot be placed such that
   *     they do not clash with other labels.
   */
  public boolean getForceLabel() {
    return forceLabel;
  }

  /**
   * Sets the label text for this Feature. This label will be used in any labels generated for this
   * Feature. This label can be changed for individual FeatureRange objects using {@link
   * FeatureRange#setLabel(String) FeatureRange.setLabel()}.
   *
   * @param label the label for this Feature.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Returns the label text for this Feature. This label will be used in any labels generated for
   * this Feature. This label can be changed for individual featureRanges using {@link
   * FeatureRange#setLabel(String) FeatureRange.setLabel()}.
   *
   * @return the label for this Feature.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Specifies a hyperlink to be associated with this Feature. Hyperlinks are included in SVG output
   * generated using {@link CgviewIO#writeToSVGFile(ca.ualberta.stothard.cgview.Cgview,
   * java.lang.String, boolean, boolean)} or in image maps for PNG and JPG images generated using
   * {@link CgviewIO#writeHTMLFile(ca.ualberta.stothard.cgview.Cgview, java.lang.String,
   * java.lang.String, java.lang.String)}.
   *
   * @param hyperlink a hyperlink for this Feature.
   */
  public void setHyperlink(String hyperlink) {
    this.hyperlink = hyperlink;
  }

  /**
   * Returns the hyperlink to be associated with this Feature.
   *
   * @return the hyperlink for this Feature.
   */
  public String getHyperlink() {
    return hyperlink;
  }

  /**
   * Specifies a mouseover to be associated with this Feature. Mouseovers are included in SVG output
   * generated using {@link CgviewIO#writeToSVGFile(ca.ualberta.stothard.cgview.Cgview,
   * java.lang.String, boolean, boolean)} or in image maps for PNG and JPG images generated using
   * {@link CgviewIO#writeHTMLFile(ca.ualberta.stothard.cgview.Cgview, java.lang.String,
   * java.lang.String, java.lang.String)}.
   *
   * @param mouseover the mouseover for this feature.
   */
  public void setMouseover(String mouseover) {
    this.mouseover = mouseover;
  }

  /**
   * Returns the mouseover to be associated with this Feature.
   *
   * @return the mouseover for this Feature.
   */
  public String getMouseover() {
    return mouseover;
  }

  /**
   * Sets the font used for labels generated for this Feature. This font can be changed for
   * individual FeatureRange objects using {@link FeatureRange#setFont(Font)
   * FeatureRange.setFont()}.
   *
   * @param font the font used for labels generated for this Feature.
   */
  public void setFont(Font font) {
    this.font = font;
  }

  /**
   * Returns the font used for labels generated for this Feature. This font can be changed for
   * individual FeatureRanges using {@link FeatureRange#setFont(Font) FeatureRange.setFont()}.
   *
   * @return the font used for labels generated for this Feature.
   */
  public Font getFont() {
    return font;
  }

  /**
   * Returns the strand of this Feature, which is determined by the FeatureSlot that contains this
   * Feature.
   *
   * @return an <code>int</code> representing the strand of this feature.
   */
  protected int getStrand() {
    return strand;
  }

  /**
   * Sets whether or not a label should be drawn for this Feature. This setting can be changed for
   * individual FeatureRange objects using {@link FeatureRange#setShowLabel(int)
   * FeatureRange.setShowLabel()}.
   *
   * @param showLabel {@link CgviewConstants#LABEL CgviewConstants.LABEL}, {@link
   *     CgviewConstants#LABEL_NONE CgviewConstants.NO_LABEL}, or {@link CgviewConstants#LABEL_FORCE
   *     CgviewConstants.LABEL_FORCE}.
   */
  public void setShowLabel(int showLabel) {
    this.showLabel = showLabel;
    if (this.showLabel == LABEL_FORCE) {
      forceLabel = true;
    } else {
      forceLabel = false;
    }
  }

  /**
   * Returns whether or not a label should be generated for this Feature when drawn. This setting
   * can be changed for individual FeatureRange objects using {@link FeatureRange#setShowLabel(int)
   * FeatureRange.setShowLabel()}.
   *
   * @return {@link CgviewConstants#LABEL CgviewConstants.LABEL}, {@link CgviewConstants#LABEL_NONE
   *     CgviewConstants.NO_LABEL}, or {@link CgviewConstants#LABEL_FORCE
   *     CgviewConstants.LABEL_FORCE}.
   */
  public int getShowLabel() {
    return showLabel;
  }

  /**
   * Sets the type of decoration added to this Feature when drawn. This decoration can be changed
   * for individual FeatureRange objects using {@link FeatureRange#setDecoration(int)
   * FeatureRange.setDecoration()}.
   *
   * @param decoration {@link CgviewConstants#DECORATION_STANDARD
   *     CgviewConstants.DECORATION_STANDARD}, {@link
   *     CgviewConstants#DECORATION_COUNTERCLOCKWISE_ARROW
   *     CgviewConstants.DECORATION_COUNTERCLOCKWISE_ARROW}, {@link
   *     CgviewConstants#DECORATION_CLOCKWISE_ARROW CgviewConstants.DECORATION_CLOCKWISE_ARROW},
   *     {@link CgviewConstants#DECORATION_HIDDEN CgviewConstants.DECORATION_HIDDEN}.
   */
  public void setDecoration(int decoration) {
    this.decoration = decoration;
  }

  /**
   * Returns an integer indicating what type of decoration will be added to this Feature when drawn.
   * This decoration can be changed for individual FeatureRange objects using {@link
   * FeatureRange#setDecoration(int) FeatureRange.setDecoration()}.
   *
   * @return {@link CgviewConstants#DECORATION_STANDARD CgviewConstants.DECORATION_STANDARD}, {@link
   *     CgviewConstants#DECORATION_COUNTERCLOCKWISE_ARROW
   *     CgviewConstants.DECORATION_COUNTERCLOCKWISE_ARROW}, {@link
   *     CgviewConstants#DECORATION_CLOCKWISE_ARROW CgviewConstants.DECORATION_CLOCKWISE_ARROW},
   *     {@link CgviewConstants#DECORATION_HIDDEN CgviewConstants.DECORATION_HIDDEN}.
   */
  public int getDecoration() {
    return decoration;
  }

  /**
   * Sets the position of this Feature relative to the FeatureSlot object that contains it. This
   * value is only applied when the thickness of this Feature is adjusted using {@link
   * #setProportionOfThickness(float)} so that it is less than 1.0. This setting can be changed for
   * individual FeatureRange objects using {@link FeatureRange#setRadiusAdjustment(float)
   * FeatureRange.setProportionOfThickness()}.
   *
   * @param radiusAdjustment between <code>0</code> and <code>1</code>, with <code>1</code> being
   *     near the edge furthest from the map center.
   */
  public void setRadiusAdjustment(float radiusAdjustment) {
    if (radiusAdjustment < 0) {
      radiusAdjustment = 0.0f;
    } else if (radiusAdjustment > 1) {
      radiusAdjustment = 1.0f;
    }
    this.radiusAdjustment = radiusAdjustment;
  }

  /**
   * Returns the position of this Feature relative to the FeatureSlot object that contains it. This
   * value is only applied when the thickness of this Feature is adjusted using {@link
   * #setProportionOfThickness(float)} so that it is less than 1.0. This setting can be changed for
   * individual FeatureRange objects using {@link FeatureRange#setRadiusAdjustment(float)
   * FeatureRange.setProportionOfThickness()}.
   *
   * @return a <code>float</code> between <code>0</code> and <code>1</code>, with <code>1</code>
   *     being near the edge furthest from the map center.
   * @see #setProportionOfThickness(float)
   */
  public float getRadiusAdjustment() {
    return radiusAdjustment;
  }

  /**
   * Sets the thickness of this Feature when drawn, as a proportion of the thickness of the
   * FeatureSlot containing this Feature. This setting can be changed for individual FeatureRange
   * objects using {@link FeatureRange#setProportionOfThickness(float)
   * FeatureRange.setProportionOfThickness()}.
   *
   * @param proportionOfThickness between <code>0</code> and <code>1</code>, with <code>1</code>
   *     being full thickness.
   */
  public void setProportionOfThickness(float proportionOfThickness) {
    if (proportionOfThickness < 0) {
      proportionOfThickness = 0.0f;
    } else if (proportionOfThickness > 1) {
      proportionOfThickness = 1.0f;
    }
    this.proportionOfThickness = proportionOfThickness;
  }

  /**
   * Returns the thickness of this Feature when drawn, as a proportion of the thickness of the
   * FeatureSlot containing this Feature. This setting can be changed for individual FeatureRange
   * objects using {@link FeatureRange#setProportionOfThickness(float)
   * FeatureRange.setProportionOfThickness()}.
   *
   * @return a <code>float</code> between <code>0</code> and <code>1</code>, with <code>1</code>
   *     being full thickness.
   */
  public float getProportionOfThickness() {
    return proportionOfThickness;
  }

  /**
   * Sets the opacity of this Feature when drawn. This setting can be changed for individual
   * FeatureRange objects using {@link FeatureRange#setOpacity(float)
   * FeatureRange.setSwatchOpacity()}.
   *
   * @param opacity the opacity between <code>0</code> and <code>1</code>, with <code>1</code> being
   *     the most opaque.
   */
  public void setOpacity(float opacity) {
    if (opacity < 0) {
      opacity = 0.0f;
    } else if (opacity > 1) {
      opacity = 1.0f;
    }
    this.opacity = opacity;
  }

  /**
   * Returns the opacity of this Feature when drawn. This setting can be changed for individual
   * FeatureRange objects using {@link FeatureRange#setOpacity(float)
   * FeatureRange.setSwatchOpacity()}.
   *
   * @return the opacity between <code>0</code> and <code>1</code>, with <code>1</code> being the
   *     most opaque.
   */
  public float getOpacity() {
    return opacity;
  }

  /**
   * Sets whether or not this Feature should be drawn with shading.
   *
   * @param showShading whether or not this Feature should be drawn with shading.
   */
  public void setShowShading(boolean showShading) {
    this.showShading = showShading;
  }

  /**
   * Returns whether or not this Feature should be drawn with shading.
   *
   * @return whether or not this Feature should be drawn with shading.
   */
  public boolean getShowShading() {
    return showShading;
  }

  /**
   * Draws this Feature and creates labels if necessary.
   *
   * @param cgview the Cgview object containing this Feature.
   * @param radius the radius of the FeatureSlot containing this Feature.
   * @param thickness the thickness of the FeatureSlot containing this Feature.
   */
  protected void draw(
    Cgview cgview,
    double radius,
    float thickness,
    double minimumFeatureLength
  ) {
    Iterator i = ranges.iterator();
    while (i.hasNext()) {
      FeatureRange currentFeatureRange = (FeatureRange) i.next();
      currentFeatureRange.draw(cgview, radius, thickness, minimumFeatureLength);
      // remove once drawn
      // i.remove();
    }
  }

  /**
   * Returns the smallest start value from the featureRanges in this Feature, or -1 if there are no
   * featureRanges.
   *
   * @return the smallest featureRange start in this featureRange or -1.
   */
  protected int getStart() {
    if (ranges.size() == 0) {
      return -1;
    }

    Comparator comparator = new SortFeatureRangesByStart();
    Collections.sort(ranges, comparator);

    FeatureRange first = (FeatureRange) ranges.get(0);
    return first.getStart();
  }

  // sort so that smallest start is first.
  public class SortFeatureRangesByStart implements Comparator {

    public int compare(Object o1, Object o2) {
      FeatureRange feature1 = (FeatureRange) o1;
      FeatureRange feature2 = (FeatureRange) o2;

      if (feature1.getStart() == feature2.getStart()) {
        return 0;
      } else if (feature2.getStart() < feature1.getStart()) {
        return 1;
      } else {
        return -1;
      }
    }

    public boolean equals(Object obj) {
      return obj.equals(this);
    }
  }
}
