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

import java.util.*;

/**
 * Represents a slot of space on the Cgview map. When the map is drawn, the FeatureSlot objects form
 * concentric rings around the backbone circle. Individual features, which are described using
 * {@link Feature} objects, are drawn in the FeatureSlot objects. FeatureSlot objects are allocated
 * space in the order that they are added to the Cgview object, such that those added first receive
 * space closest to the backbone circle.
 *
 * @author Paul Stothard
 */
public class FeatureSlot implements CgviewConstants {
  private int strand;
  private ArrayList featuresInSlot = new ArrayList();
  private double radius = 0.0d;
  private Cgview cgview;
  private float featureThickness;
  private boolean showShading;
  private double minimumFeatureLength;

  /**
   * Constructs a new FeatureSlot object.
   *
   * @param cgview the Cgview object to contain this FeatureSlot.
   * @param strand the strand of this FeatureSlot, {@link CgviewConstants#DIRECT_STRAND
   *     CgviewConstants.DIRECT_STRAND} or {@link CgviewConstants#REVERSE_STRAND
   *     CgviewConstants.REVERSE_STRAND}. If strand is set to DIRECT_STRAND this FeatureSlot is
   *     placed on the outside of the backbone circle. If strand is set to REVERSE_STRAND this
   *     FeatureSlot is placed on the inside of the backbone circle.
   */
  public FeatureSlot(Cgview cgview, int strand) {
    this.strand = strand;
    this.cgview = cgview;
    this.featureThickness = cgview.getFeatureThickness();
    this.showShading = cgview.getShowShading();
    this.minimumFeatureLength = cgview.getMinimumFeatureLength();

    // go through the existing FeatureSlots to determine which position this one will occupy.
    ArrayList featureSlots = cgview.getFeatureSlots();

    // add this FeatureSlot to the Cgview featureSlots.
    featureSlots.add(this);
  }

  /**
   * Constructs a new <code>FeatureSlot</code> object. It can be added to a Cgview object using the
   * setCgview() method.
   *
   * @param strand the strand of this FeatureSlot, {@link CgviewConstants#DIRECT_STRAND
   *     CgviewConstants.DIRECT_STRAND} or {@link CgviewConstants#REVERSE_STRAND
   *     CgviewConstants.REVERSE_STRAND}. If strand is set to DIRECT_STRAND this FeatureSlot is
   *     placed on the outside of the backbone circle. If strand is set to REVERSE_STRAND this
   *     FeatureSlot is placed on the inside of the backbone circle.
   */
  public FeatureSlot(int strand) {
    this.strand = strand;
  }

  /**
   * Constructs a new <code>FeatureSlot</code> object. It can be added to a cgview object using the
   * setCgview() method.
   *
   * @param strand the strand of this featureSlot, {@link CgviewConstants#DIRECT_STRAND
   *     CgviewConstants.DIRECT_STRAND} or {@link CgviewConstants#REVERSE_STRAND
   *     CgviewConstants.REVERSE_STRAND}. If strand is set to DIRECT_STRAND this FeatureSlot is
   *     placed on the outside of the backbone circle. If strand is set to REVERSE_STRAND this
   *     FeatureSlot is placed on the inside of the backbone circle.
   * @param showShading whether or not items this FeatureSlot should be drawn with shading.
   */
  public FeatureSlot(int strand, boolean showShading) {
    this.strand = strand;
    this.showShading = showShading;
  }

  /**
   * Adds this FeatureSlot to a Cgview object.
   *
   * @param cgview the Cgview object to contain this FeatureSlot.
   */
  public void setCgview(Cgview cgview) {
    this.cgview = cgview;
    this.featureThickness = cgview.getFeatureThickness();
    this.showShading = cgview.getShowShading();
    this.minimumFeatureLength = cgview.getMinimumFeatureLength();

    // go through the existing FeatureSlots to determine which position this one will occupy.
    ArrayList featureSlots = cgview.getFeatureSlots();

    // add this FeatureSlot to the Cgview featureSlots.
    featureSlots.add(this);
  }

  /**
   * Adds a Feature object to this FeatureSlot.
   *
   * @param feature the Feature to add to this FeatureSlot.
   */
  protected void addFeature(Feature feature) {
    featuresInSlot.add(feature);
  }

  /**
   * Returns the strand of this FeatureSlot.
   *
   * @return an <code>int</code> representing the strand of this FeatureSlot.
   */
  public int getStrand() {
    return strand;
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
    this.minimumFeatureLength = length;
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
   * Sets the thickness of the arc used to represent sequence features in this FeatureSlot.
   *
   * @param featureThickness the feature thickness.
   */
  public void setFeatureThickness(float featureThickness) {
    if (featureThickness < 0) {
      featureThickness = 0.0f;
    }
    this.featureThickness = featureThickness;
  }

  /**
   * Returns the thickness of the arc used to represent sequence features in this FeatureSlot.
   *
   * @return the feature thickness.
   */
  public float getFeatureThickness() {
    return featureThickness;
  }

  /**
   * Sets whether or not items in this FeatureSlot should be drawn with shading.
   *
   * @param showShading whether or not items this FeatureSlot should be drawn with shading.
   */
  public void setShowShading(boolean showShading) {
    this.showShading = showShading;
  }

  /**
   * Returns whether or not items in this FeatureSlot should be drawn with shading.
   *
   * @return whether or not items in this featureSlot should be drawn with shading.
   */
  public boolean getShowShading() {
    return showShading;
  }

  /** Sets the radius of this FeatureSlot. */
  protected void setRadius() {
    if (strand == DIRECT_STRAND) {
      radius = cgview.getFirstOuterFeatureRadius();
      ArrayList featureSlots = cgview.getFeatureSlots();
      Iterator i = featureSlots.iterator();
      while (i.hasNext()) {
        FeatureSlot currentFeatureSlot = (FeatureSlot) i.next();
        if (currentFeatureSlot.getStrand() == DIRECT_STRAND) {
          if (currentFeatureSlot.equals(this)) {
            radius = radius + 0.5d * featureThickness;
            break;
          } else {
            radius =
              radius +
              currentFeatureSlot.getFeatureThickness() +
              cgview.getFeatureSlotSpacing();
          }
        }
      }
    } else {
      radius = cgview.getFirstInnerFeatureRadius();
      ArrayList featureSlots = cgview.getFeatureSlots();
      Iterator i = featureSlots.iterator();
      while (i.hasNext()) {
        FeatureSlot currentFeatureSlot = (FeatureSlot) i.next();
        if (currentFeatureSlot.getStrand() == REVERSE_STRAND) {
          if (currentFeatureSlot.equals(this)) {
            radius = radius - 0.5d * featureThickness;
            break;
          } else {
            radius =
              radius -
              currentFeatureSlot.getFeatureThickness() -
              cgview.getFeatureSlotSpacing();
          }
        }
      }
    }
  }

  /**
   * Returns the minimum feature length in bases that can be drawn accurately in this FeatureSlot,
   * at the given zoom value. Feature lengths smaller than this length are automatically drawn
   * larger so that they are visible. This method should be used when deciding on the appropriate
   * window size (length of bases) to use when calculating a value to present on the map. For
   * example, if you plan to display percent GC content on the map, and this method returns <code>
   * 98.0003</code>, you should choose a window size of 99 or greater when calculating percent GC.
   * In other words, you would create a Feature object containing a FeatureRange object spanning
   * bases 1 to 99, a second FeatureRange spanning bases 100 to 199, and so on. Each FeatureRange
   * would be decorated in some way to convey the %GC value calculated for the segment of bases it
   * contains (using {@link FeatureRange#setProportionOfThickness(float)} for example).
   *
   * @param zoom the zoom value to be used when drawing the Cgview map.
   */
  public double getBasesPerMinFeature(double zoom) {
    this.setRadius();
    double adjustedZoom = cgview.adjustZoom(zoom);
    double basePerCircum = (double) (cgview.getSequenceLength()) /
    (2.0d * Math.PI * radius * adjustedZoom);
    return this.getMinimumFeatureLength() * basePerCircum;
  }

  /** Draws the contents of this FeatureSlot. */
  protected void draw() {
    this.setRadius();
    Iterator i = featuresInSlot.iterator();
    while (i.hasNext()) {
      Feature currentFeature = (Feature) i.next();
      currentFeature.draw(
        cgview,
        radius,
        featureThickness,
        minimumFeatureLength
      );
      // remove once drawn
      // i.remove();
    }
  }

  /**
   * Returns a boolean specifying whether or not the supplied Feature object can fit in this
   * FeatureSlot without overlapping with Feature objects already present in this FeatureSlot.
   *
   * @param feature the Feature to check.
   * @return whether or not the Feature object can fit without overlapping with other Feature
   *     objects.
   */
  public boolean isRoom(Feature feature) {
    // go through each feature and each featureRange.
    Iterator i = featuresInSlot.iterator();
    while (i.hasNext()) {
      Feature currentFeature = (Feature) i.next();
      Iterator j = currentFeature.getRanges().iterator();

      while (j.hasNext()) {
        FeatureRange currentFeatureRange = (FeatureRange) j.next();
        if (currentFeatureRange.getDecoration() == DECORATION_HIDDEN) {
          continue;
        }

        int startOne = currentFeatureRange.getStart();
        int stopOne = currentFeatureRange.getStop();

        // now examine the featureRanges in the submitted feature.
        Iterator k = feature.getRanges().iterator();

        while (k.hasNext()) {
          FeatureRange innerFeatureRange = (FeatureRange) k.next();

          if (innerFeatureRange.getDecoration() == DECORATION_HIDDEN) {
            continue;
          }

          int startTwo = innerFeatureRange.getStart();
          int stopTwo = innerFeatureRange.getStop();

          if ((startOne < stopOne) && (startTwo < stopTwo)) {
            if ((startTwo > stopOne) || (stopTwo < startOne)) {
              continue;
            } else {
              // System.out.println ("label overlap 1: " + startOne + "-" + stopOne + ", " +
              // startTwo + "-" + stopTwo + ".");
              return false;
            }
          } else if ((startOne > stopOne) && (startTwo > stopTwo)) {
            // System.out.println ("label overlap 2: " + startOne + "-" + stopOne + ", " + startTwo
            // + "-" + stopTwo + ".");
            return false;
          } else if (
            (startOne < stopOne) && (startTwo > stopOne) && (stopTwo < startOne)
          ) {
            continue;
          } else if (
            (startTwo < stopTwo) && (startOne > stopTwo) && (stopOne < startTwo)
          ) {
            continue;
          } else {
            // System.out.println ("label overlap 3: " + startOne + "-" + stopOne + ", " + startTwo
            // + "-" + stopTwo + ".");
            return false;
          }
        }
      } // end of while
    }
    return true;
  }

  /**
   * Sorts the features in this FeatureSlot by start base. If this is a DIRECT_STRAND slot the
   * features are sorted in ascending order. If this is a REVERSE_STRAND slot the features are
   * sorted in descending order. This sorting produces a more visually pleasing feature appearance
   * when arrows are used.
   */
  public void sortFeaturesByStart() {
    Comparator comparator = new SortFeaturesByStart();
    Collections.sort(featuresInSlot, comparator);

    if (this.strand == DIRECT_STRAND) {
      Collections.reverse(featuresInSlot);
    }
  }

  // sort so that smallest start is first.
  public class SortFeaturesByStart implements Comparator {

    public int compare(Object o1, Object o2) {
      Feature feature1 = (Feature) o1;
      Feature feature2 = (Feature) o2;

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
