package ca.ualberta.stothard.cgview;

import java.awt.*;
import java.awt.geom.*;
import java.util.regex.*;

/**
 * This class is used to assign sequence features, which are described using the Feature object, to
 * specific bases of a DNA sequence. FeatureRange objects inherit most of their attributes from the
 * parent Feature object.
 *
 * @author Paul Stothard
 */
public class FeatureRange implements CgviewConstants {
  private int start;
  private int stop;

  private int decoration;
  private int showLabel;

  private String hyperlink;
  private String mouseover;

  private Color color;
  private Feature feature;

  private String label;
  private Font font;
  private boolean forceLabel;

  private float radiusAdjustment;
  private float proportionOfThickness;
  private float opacity;

  private boolean showShading;

  /**
   * Constructs a new <code>FeatureRange</code> object. The <code>start</code> and <code>stop</code>
   * positions refer to the direct strand. If the <code>start</code> is greater than the <code>stop
   * </code>, the feature is assumed to extend from the start, across the end of the sequence, to
   * the stop. If <code>start</code> equals <code>stop</code>, the FeatureRange is assumed to refer
   * to a single base.
   *
   * @param feature the Feature object to contain this FeatureRange.
   * @param start the first base in this FeatureRange.
   * @param stop the last base in this FeatureRange.
   */
  public FeatureRange(Feature feature, int start, int stop) {
    this.start = start;
    this.stop = stop;
    feature.addRange(this);

    decoration = feature.getDecoration();
    showLabel = feature.getShowLabel();

    color = feature.getColor();
    this.feature = feature;

    this.hyperlink = feature.getHyperlink();
    this.mouseover = feature.getMouseover();

    label = feature.getLabel();
    font = feature.getFont();
    forceLabel = feature.getForceLabel();

    radiusAdjustment = feature.getRadiusAdjustment();
    proportionOfThickness = feature.getProportionOfThickness();
    opacity = feature.getOpacity();

    showShading = feature.getShowShading();
  }

  /**
   * Returns the position of the first base in this FeatureRange.
   *
   * @return the position of the first base in this FeatureRange.
   */
  public int getStart() {
    return start;
  }

  /**
   * Returns the position of the last base in this FeatureRange.
   *
   * @return the position of the last base in this FeatureRange.
   */
  public int getStop() {
    return stop;
  }

  /**
   * Specifies whether or not labels created for this FeatureRange should be drawn even if they
   * cannot be placed such that they do not clash with other labels.
   *
   * @param forceLabel a boolean specifying whether or not to draw labels for this FeatureRange even
   *     if they cannot be placed such that they do not clash with other labels.
   */
  public void setForceLabel(boolean forceLabel) {
    this.forceLabel = forceLabel;
    if (this.forceLabel == true) {
      showLabel = LABEL_FORCE;
    }
  }

  /**
   * Returns a boolean specifying whether or not labels created for this FeatureRange should be
   * drawn even if they cannot be placed such that they do not clash with other labels.
   *
   * @return whether or not to draw labels for this FeatureRange even if they cannot be placed such
   *     that they do not clash with other labels.
   */
  public boolean getForceLabel() {
    return forceLabel;
  }

  /**
   * Draws this FeatureRange and creates a label if necessary.
   *
   * @param cgview the Cgview object that contains this FeatureRange.
   * @param radius the radius of the FeatureSlot that contains this FeatureRange.
   * @param thickness the thickness of the FeatureSlot that contains this FeatureRange.
   */
  protected void draw(
    Cgview cgview,
    double radius,
    float thickness,
    double minimumFeatureLength
  ) {
    boolean keepLastLabels = cgview.getKeepLastLabels();

    // case -1: if start or stop are < 1 or greater than the length of the plasmid, do not draw
    if (
      (start < 1) ||
      (stop < 1) ||
      (start > cgview.getSequenceLength()) ||
      (stop > cgview.getSequenceLength())
    ) {
      System.err.println(
        "[warning] Invalid feature position encountered: start = " +
        start +
        ", stop = " +
        stop +
        "."
      );
    }
    // case 0: if drawing the entire plasmid just draw the feature
    else if (cgview.getDrawEntirePlasmid()) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawClockwiseArrow(cgview, radius, start, stop, thickness);
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(cgview, radius, start, stop, thickness);
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, stop);
      }
      // System.out.println ("case0");
    }
    // case 1: both bases are equal and they are in one of the zoomRanges
    else if ((start == stop) && (cgview.inZoomRange(start))) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawClockwiseArrow(cgview, radius, start, stop, thickness);
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(cgview, radius, start, stop, thickness);
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, stop);
      }
      // System.out.println ("case1");
    }
    // case 2: start is in zoomRangeOne and stop is in zoomRangeTwo
    else if ((cgview.inZoomRangeOne(start)) && (cgview.inZoomRangeTwo(stop))) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawClockwiseArrow(cgview, radius, start, stop, thickness);
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(cgview, radius, start, stop, thickness);
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, stop);
      }
      // System.out.println ("case2");
    }
    // case 3: start and stop are in zoomRangeOne and start is less than stop
    else if (
      (cgview.inZoomRangeOne(start)) &&
      (cgview.inZoomRangeOne(stop)) &&
      (start < stop)
    ) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawClockwiseArrow(cgview, radius, start, stop, thickness);
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(cgview, radius, start, stop, thickness);
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, stop);
      }
      // System.out.println ("case3");
    }
    // case 4: start and stop are in zoomRangeTwo and start is less than stop
    else if (
      (cgview.inZoomRangeTwo(start)) &&
      (cgview.inZoomRangeTwo(stop)) &&
      (start < stop)
    ) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawClockwiseArrow(cgview, radius, start, stop, thickness);
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(cgview, radius, start, stop, thickness);
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, stop);
      }
      // System.out.println ("case4");
    }
    // case 5: start is in zoomRangeTwo and stop is in zoomRangeOne
    else if ((cgview.inZoomRangeTwo(start)) && (cgview.inZoomRangeOne(stop))) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        drawClockwiseArrow(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness
        );
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness
        );
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, cgview.getZoomRangeTwoStop());
        labelStandard(cgview, cgview.getZoomRangeOneStart(), stop);
      }
      // System.out.println ("case5");
    }
    // case 6: start is in zoomRangeOne and stop is in zoomRangeOne and start > stop
    else if (
      (cgview.inZoomRangeOne(start)) &&
      (cgview.inZoomRangeOne(stop)) &&
      (start > stop)
    ) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        drawClockwiseArrow(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness
        );
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness
        );
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, cgview.getZoomRangeTwoStop());
        labelStandard(cgview, cgview.getZoomRangeOneStart(), stop);
      }
      // System.out.println ("case6");
    }
    // case 7: start is in zoomRangeTwo and stop is in zoomRangeTwo and start > stop
    else if (
      (cgview.inZoomRangeTwo(start)) &&
      (cgview.inZoomRangeTwo(stop)) &&
      (start > stop)
    ) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        drawClockwiseArrow(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness
        );
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness
        );
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, cgview.getZoomRangeTwoStop());
        labelStandard(cgview, cgview.getZoomRangeOneStart(), stop);
      }
      // System.out.println ("case7");
    }
    // case 8: stop is in zoomRange
    else if (cgview.inZoomRange(stop)) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawClockwiseArrow(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness
        );
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          stop,
          thickness,
          minimumFeatureLength
        );
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, cgview.getZoomRangeOneStart(), stop);
      }
      // System.out.println ("case8");
    }
    // case 9: start is in zoomRange
    else if (cgview.inZoomRange(start)) {
      if (decoration == DECORATION_STANDARD) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_CLOCKWISE_ARROW) {
        drawStandard(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
      } else if (decoration == DECORATION_COUNTERCLOCKWISE_ARROW) {
        drawCounterclockwiseArrow(
          cgview,
          radius,
          start,
          cgview.getZoomRangeTwoStop(),
          thickness
        );
      }

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(cgview, start, cgview.getZoomRangeTwoStop());
      }
      // System.out.println ("case9");
    }
    // case 10: range spans zoomRange
    else if (
      (start < cgview.getZoomRangeOneStart()) &&
      (stop > cgview.getZoomRangeTwoStop()) &&
      (start != stop)
    ) {
      if (cgview.getZoomRangeTwoStart() == 0) {
        if (start > stop) {
          drawStandard(
            cgview,
            radius,
            cgview.getZoomRangeOneStart(),
            cgview.getZoomRangeTwoStop(),
            thickness,
            minimumFeatureLength
          );

          if ((showLabel == LABEL) && (!keepLastLabels)) {
            labelStandard(
              cgview,
              cgview.getZoomRangeOneStart(),
              cgview.getZoomRangeTwoStop()
            );
          }
          // System.out.println ("case10");
        }
      } else {
        drawStandard(
          cgview,
          radius,
          cgview.getZoomRangeOneStart(),
          cgview.getZoomRangeTwoStop(),
          thickness,
          minimumFeatureLength
        );
        if ((showLabel == LABEL) && (!keepLastLabels)) {
          labelStandard(
            cgview,
            cgview.getZoomRangeOneStart(),
            cgview.getZoomRangeTwoStop()
          );
        }
        // System.out.println ("case10.5");
      }
    }
    // case 11: range spans zoomRange
    else if ((stop > cgview.getZoomRangeTwoStop()) && (start > stop)) {
      drawStandard(
        cgview,
        radius,
        cgview.getZoomRangeOneStart(),
        cgview.getZoomRangeTwoStop(),
        thickness,
        minimumFeatureLength
      );

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(
          cgview,
          cgview.getZoomRangeOneStart(),
          cgview.getZoomRangeTwoStop()
        );
      }
      // System.out.println ("case11");
    }
    // case 12: range spans zoomRange
    else if ((start < cgview.getZoomRangeOneStart()) && (start > stop)) {
      drawStandard(
        cgview,
        radius,
        cgview.getZoomRangeOneStart(),
        cgview.getZoomRangeTwoStop(),
        thickness,
        minimumFeatureLength
      );

      if ((showLabel == LABEL) && (!keepLastLabels)) {
        labelStandard(
          cgview,
          cgview.getZoomRangeOneStart(),
          cgview.getZoomRangeTwoStop()
        );
      }
      // System.out.println ("case12");
    } else {
      // don't draw anything
      // System.out.println ("no label");
    }
  }

  /**
   * Draws this FeatureRange as a simple arc.
   *
   * @param cgview the Cgview object that contains this FeatureRange.
   * @param radius the radius of the FeatureSlot that contains this FeatureRange.
   * @param startBase the adjusted position of the first base in this FeatureRange.
   * @param stopBase the adjusted position of the last base in this FeatureRange.
   * @param thickness the thickness of the FeatureSlot that contains this FeatureRange.
   */
  private void drawStandard(
    Cgview cgview,
    double radius,
    int startBase,
    int stopBase,
    float thickness,
    double minimumFeatureLength
  ) {
    float featureThickness = thickness;
    double shadingProportion = cgview.getShadingProportion();
    float highlightOpacity = cgview.getHighlightOpacity();
    float shadowOpacity = cgview.getShadowOpacity();
    double originOffset = cgview.getOrigin();
    Graphics2D gg = cgview.getGraphics();
    int totalBases = cgview.getSequenceLength();
    boolean shiftSmallFeatures = cgview.getShiftSmallFeatures();

    // adjust radius to take into account proportionOfThickness and radiusAdjustment values;
    // radius = radius - 0.5d * featureThickness + 0.5d * (proportionOfThickness * featureThickness)
    // + radiusAdjustment * (featureThickness - (proportionOfThickness * featureThickness));
    double newRadius = radius;
    if (proportionOfThickness < 1.0f) {
      newRadius = radius - 0.5d * featureThickness;
      newRadius = newRadius + featureThickness * radiusAdjustment;
    }
    // prevent drawing outside of feature slot for this feature
    double maxRadiusForThisFeature =
      radius +
      featureThickness *
      0.5d -
      (0.5d * featureThickness * proportionOfThickness);
    double minRadiusForThisFeature =
      radius -
      featureThickness *
      0.5d +
      (0.5d * featureThickness * proportionOfThickness);

    if (newRadius > maxRadiusForThisFeature) {
      newRadius = maxRadiusForThisFeature;
    }
    if (newRadius < minRadiusForThisFeature) {
      newRadius = minRadiusForThisFeature;
    }
    radius = newRadius;

    featureThickness = proportionOfThickness * featureThickness;

    double startOfArc;
    double extentOfArc;

    // typical case where start is less than stop
    if (startBase <= stopBase) {
      startOfArc = cgview.getDegrees(startBase - 1);
      extentOfArc = cgview.getDegrees(stopBase) - startOfArc;
    }
    // case where feature spans junction
    else {
      startOfArc = cgview.getDegrees(startBase - 1);
      extentOfArc = cgview.getDegrees(totalBases) - startOfArc;

      double startOfArcB = cgview.getDegrees(1 - 1);
      double extentOfArcB = cgview.getDegrees(stopBase) - startOfArcB;

      extentOfArc = extentOfArc + extentOfArcB;
    }

    // check to see if the arc is below cgview.getMinimunFeatureLength() and if it is make
    // adjustments
    if ((extentOfArc * (Math.PI / 180.0d) * radius) < minimumFeatureLength) {
      extentOfArc = (minimumFeatureLength / radius) * (180.0d / Math.PI);
      if (shiftSmallFeatures) {
        startOfArc = startOfArc - 0.5d * extentOfArc;
      }
    }

    gg.setComposite(
      AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
    );
    gg.setPaint(color);
    BasicStroke arcStroke = new BasicStroke(
      featureThickness,
      BasicStroke.CAP_BUTT,
      BasicStroke.JOIN_BEVEL
    );
    Area centralArc = new Area();
    centralArc.add(
      new Area(
        arcStroke.createStrokedShape(
          new Arc2D.Double(
            -radius,
            -radius,
            radius * 2.0d,
            radius * 2.0d,
            -startOfArc - extentOfArc + originOffset,
            extentOfArc,
            Arc2D.OPEN
          )
        )
      )
    );

    // to prevent drawing off canvas
    centralArc.intersect(new Area(cgview.getBackgroundRectangle()));
    gg.fill(centralArc);

    // if (cgview.getShowShading()) {
    // if (this.feature.getShowShading()) {
    if (showShading) {
      // draw highlight
      gg.setComposite(
        AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER,
          highlightOpacity * opacity
        )
      );
      gg.setPaint(Color.white);

      double radiusIncrease =
        0.5d * featureThickness - 0.5d * (featureThickness * shadingProportion);
      BasicStroke highlightArcStroke = new BasicStroke(
        featureThickness * (float) (shadingProportion),
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL
      );
      Area highlightArc = new Area();
      highlightArc.add(
        new Area(
          highlightArcStroke.createStrokedShape(
            new Arc2D.Double(
              -radius - radiusIncrease,
              -radius - radiusIncrease,
              (radius + radiusIncrease) * 2.0d,
              (radius + radiusIncrease) * 2.0d,
              -startOfArc - extentOfArc + originOffset,
              extentOfArc,
              Arc2D.OPEN
            )
          )
        )
      );
      highlightArc.intersect(new Area(cgview.getBackgroundRectangle()));
      gg.fill(highlightArc);

      // draw shadow
      gg.setComposite(
        AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER,
          shadowOpacity * opacity
        )
      );
      gg.setPaint(Color.black);
      double radiusDecrease =
        -0.5d *
        featureThickness +
        0.5d *
        (featureThickness * shadingProportion);
      BasicStroke shadowArcStroke = new BasicStroke(
        featureThickness * (float) (shadingProportion),
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL
      );
      Area shadowArc = new Area();
      shadowArc.add(
        new Area(
          shadowArcStroke.createStrokedShape(
            new Arc2D.Double(
              -radius - radiusDecrease,
              -radius - radiusDecrease,
              (radius + radiusDecrease) * 2.0d,
              (radius + radiusDecrease) * 2.0d,
              -startOfArc - extentOfArc + originOffset,
              extentOfArc,
              Arc2D.OPEN
            )
          )
        )
      );
      shadowArc.intersect(new Area(cgview.getBackgroundRectangle()));
      gg.fill(shadowArc);
    }

    // set back to 1.0f
    gg.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
  }

  /**
   * Draws this FeatureRange as an arrow pointing in the clockwise direction.
   *
   * @param cgview the Cgview object that contains this FeatureRange.
   * @param radius the radius of the FeatureSlot that contains this FeatureRange.
   * @param startBase the adjusted position of the first base in this FeatureRange.
   * @param stopBase the adjusted position of the last base in this FeatureRange.
   * @param thickness the thickness of the FeatureSlot that contains this FeatureRange.
   */
  private void drawClockwiseArrow(
    Cgview cgview,
    double radius,
    int startBase,
    int stopBase,
    float thickness
  ) {
    float featureThickness = thickness;
    double shadingProportion = cgview.getShadingProportion();
    float highlightOpacity = cgview.getHighlightOpacity();
    float shadowOpacity = cgview.getShadowOpacity();
    double originOffset = cgview.getOrigin();
    Graphics2D gg = cgview.getGraphics();
    int totalBases = cgview.getSequenceLength();
    boolean shiftSmallFeatures = cgview.getShiftSmallFeatures();
    double arrowLength = cgview.getArrowheadLength();

    // adjust radius to take into account proportionOfThickness and radiusAdjustment values;
    // radius = radius - 0.5d * featureThickness + 0.5d * (proportionOfThickness * featureThickness)
    // + radiusAdjustment * (featureThickness - (proportionOfThickness * featureThickness));
    double newRadius = radius;
    if (proportionOfThickness < 1.0f) {
      newRadius = radius - 0.5d * featureThickness;
      newRadius = newRadius + featureThickness * radiusAdjustment;
    }
    // prevent drawing outside of feature slot for this feature
    double maxRadiusForThisFeature =
      radius +
      featureThickness *
      0.5d -
      (0.5d * featureThickness * proportionOfThickness);
    double minRadiusForThisFeature =
      radius -
      featureThickness *
      0.5d +
      (0.5d * featureThickness * proportionOfThickness);

    if (newRadius > maxRadiusForThisFeature) {
      newRadius = maxRadiusForThisFeature;
    }
    if (newRadius < minRadiusForThisFeature) {
      newRadius = minRadiusForThisFeature;
    }
    radius = newRadius;

    featureThickness = proportionOfThickness * featureThickness;

    double startOfArc;
    double extentOfArc;

    // typical case where start is less than stop
    if (startBase <= stopBase) {
      startOfArc = cgview.getDegrees(startBase - 1);
      extentOfArc = cgview.getDegrees(stopBase) - startOfArc;
    }
    // case where feature spans junction
    else {
      startOfArc = cgview.getDegrees(startBase - 1);
      extentOfArc = cgview.getDegrees(totalBases) - startOfArc;

      double startOfArcB = cgview.getDegrees(1 - 1);
      double extentOfArcB = cgview.getDegrees(stopBase) - startOfArcB;

      extentOfArc = extentOfArc + extentOfArcB;
    }

    int arrowBase;
    double extentOfArcShift;
    double arrowPointRadians;
    double arrowBaseRadians;

    arrowBase = stopBase;
    extentOfArcShift = (arrowLength / radius) * (180.0d / Math.PI);

    if (
      ((extentOfArc * (Math.PI / 180.0d) * radius) < (arrowLength)) &&
      (shiftSmallFeatures)
    ) {
      /// extentOfArc = extentOfArc - 0.5d * extentOfArcShift;

      // now determine the position in radians of the arrow head base
      // arrowPointRadians = -(-cgview.getDegrees(arrowBase) + originOffset - 0.5d * ((arrowLength /
      // radius) * (180.0d / Math.PI))) * (Math.PI / 180.0d);
      arrowPointRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset +
          0.5d *
          extentOfArc -
          0.5d *
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);

      // now determine the position in radians of the arrow head point
      // arrowBaseRadians = -(-cgview.getDegrees(arrowBase) + originOffset + ((arrowLength / radius)
      // * (180.0d / Math.PI)) - 0.5d * ((arrowLength / radius) * (180.0d / Math.PI))) * (Math.PI /
      // 180.0d);
      arrowBaseRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset +
          ((arrowLength / radius) * (180.0d / Math.PI)) +
          0.5d *
          extentOfArc -
          0.5d *
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);

      extentOfArc = 0.0d;
    } else {
      extentOfArc = extentOfArc - extentOfArcShift;

      // now determine the position in radians of the arrow head base
      arrowPointRadians =
        -(-cgview.getDegrees(arrowBase) + originOffset) * (Math.PI / 180.0d);

      // now determine the position in radians of the arrow head point
      arrowBaseRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset +
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);
    }

    // determine the radius of the outer edge of the arrow
    double outerEdgeRadius = radius + 0.5d * featureThickness;

    // determine the radius of the inner edge of the arrow
    double innerEdgeRadius = radius - 0.5d * featureThickness;

    // create areas for drawing the arc
    Area centralArrow = new Area();
    Area highlightArrow = new Area();
    Area shadowArrow = new Area();

    // create a GeneralPath to describe the arrow head
    GeneralPath arrow = new GeneralPath(GeneralPath.WIND_NON_ZERO);
    arrow.moveTo(
      (float) (Math.cos(arrowBaseRadians) * outerEdgeRadius),
      (float) (Math.sin(arrowBaseRadians) * outerEdgeRadius)
    );
    arrow.lineTo(
      (float) (Math.cos(arrowBaseRadians) * innerEdgeRadius),
      (float) (Math.sin(arrowBaseRadians) * innerEdgeRadius)
    );
    arrow.lineTo(
      (float) (Math.cos(arrowPointRadians) * radius),
      (float) (Math.sin(arrowPointRadians) * radius)
    );
    arrow.closePath();
    centralArrow.add(new Area(arrow));

    if (showShading) {
      // create a GeneralPath to draw a highlight on the arrow
      GeneralPath arrowHighlight = new GeneralPath(GeneralPath.WIND_NON_ZERO);
      arrowHighlight.moveTo(
        (float) (Math.cos(arrowBaseRadians) * outerEdgeRadius),
        (float) (Math.sin(arrowBaseRadians) * outerEdgeRadius)
      );
      arrowHighlight.lineTo(
        (float) (Math.cos(arrowPointRadians) * radius),
        (float) (Math.sin(arrowPointRadians) * radius)
      );
      arrowHighlight.lineTo(
        (float) (
          Math.cos(arrowBaseRadians) *
          (outerEdgeRadius - featureThickness * shadingProportion)
        ),
        (float) (
          Math.sin(arrowBaseRadians) *
          (outerEdgeRadius - featureThickness * shadingProportion)
        )
      );
      highlightArrow.add(new Area(arrowHighlight));

      // create a GeneralPath to draw a shadow on the arrow
      GeneralPath arrowShadow = new GeneralPath(GeneralPath.WIND_NON_ZERO);
      arrowShadow.moveTo(
        (float) (Math.cos(arrowBaseRadians) * innerEdgeRadius),
        (float) (Math.sin(arrowBaseRadians) * innerEdgeRadius)
      );
      arrowShadow.lineTo(
        (float) (Math.cos(arrowPointRadians) * radius),
        (float) (Math.sin(arrowPointRadians) * radius)
      );
      arrowShadow.lineTo(
        (float) (
          Math.cos(arrowBaseRadians) *
          (innerEdgeRadius + featureThickness * shadingProportion)
        ),
        (float) (
          Math.sin(arrowBaseRadians) *
          (innerEdgeRadius + featureThickness * shadingProportion)
        )
      );
      shadowArrow.add(new Area(arrowShadow));
    }

    // now draw the arc
    if (extentOfArc > 0.0d) {
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
      );
      gg.setPaint(color);
      BasicStroke arcStroke = new BasicStroke(
        featureThickness,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL
      );
      centralArrow.add(
        new Area(
          arcStroke.createStrokedShape(
            new Arc2D.Double(
              -radius,
              -radius,
              radius * 2.0d,
              radius * 2.0d,
              -startOfArc - extentOfArc + originOffset,
              extentOfArc,
              Arc2D.OPEN
            )
          )
        )
      );

      // to prevent drawing off canvas
      centralArrow.intersect(new Area(cgview.getBackgroundRectangle()));
      gg.fill(centralArrow);

      if (showShading) {
        // draw highlight
        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            highlightOpacity * opacity
          )
        );
        gg.setPaint(Color.white);
        double radiusIncrease =
          0.5d *
          featureThickness -
          0.5d *
          (featureThickness * shadingProportion);
        BasicStroke highlightArcStroke = new BasicStroke(
          featureThickness * (float) (shadingProportion),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL
        );
        highlightArrow.add(
          new Area(
            highlightArcStroke.createStrokedShape(
              new Arc2D.Double(
                -radius - radiusIncrease,
                -radius - radiusIncrease,
                (radius + radiusIncrease) * 2.0d,
                (radius + radiusIncrease) * 2.0d,
                -startOfArc - extentOfArc + originOffset,
                extentOfArc,
                Arc2D.OPEN
              )
            )
          )
        );
        highlightArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(highlightArrow);

        // draw shadow
        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            shadowOpacity * opacity
          )
        );
        gg.setPaint(Color.black);
        double radiusDecrease =
          -0.5d *
          featureThickness +
          0.5d *
          (featureThickness * shadingProportion);
        BasicStroke shadowArcStroke = new BasicStroke(
          featureThickness * (float) (shadingProportion),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL
        );
        shadowArrow.add(
          new Area(
            shadowArcStroke.createStrokedShape(
              new Arc2D.Double(
                -radius - radiusDecrease,
                -radius - radiusDecrease,
                (radius + radiusDecrease) * 2.0d,
                (radius + radiusDecrease) * 2.0d,
                -startOfArc - extentOfArc + originOffset,
                extentOfArc,
                Arc2D.OPEN
              )
            )
          )
        );
        shadowArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(shadowArrow);

        // set back to 1.0f
        gg.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
        );
      }
    } else {
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
      );
      gg.setPaint(color);
      centralArrow.intersect(new Area(cgview.getBackgroundRectangle()));
      gg.fill(centralArrow);

      if (showShading) {
        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            highlightOpacity * opacity
          )
        );
        gg.setPaint(Color.white);
        highlightArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(highlightArrow);

        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            shadowOpacity * opacity
          )
        );
        gg.setPaint(Color.black);
        shadowArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(shadowArrow);

        // set back to 1.0f
        gg.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
        );
      }
    }
  }

  /**
   * Draws this FeatureRange as an arrow pointing in the counterclockwise direction.
   *
   * @param cgview the Cgview object that contains this FeatureRange.
   * @param radius the radius of the FeatureSlot that contains this FeatureRange.
   * @param startBase the adjusted position of the first base in this FeatureRange.
   * @param stopBase the adjusted position of the last base in this FeatureRange.
   * @param thickness the thickness of the FeatureSlot that contains this FeatureRange.
   */
  private void drawCounterclockwiseArrow(
    Cgview cgview,
    double radius,
    int startBase,
    int stopBase,
    float thickness
  ) {
    float featureThickness = thickness;
    double shadingProportion = cgview.getShadingProportion();
    float highlightOpacity = cgview.getHighlightOpacity();
    float shadowOpacity = cgview.getShadowOpacity();
    double originOffset = cgview.getOrigin();
    Graphics2D gg = cgview.getGraphics();
    int totalBases = cgview.getSequenceLength();
    boolean shiftSmallFeatures = cgview.getShiftSmallFeatures();
    double arrowLength = cgview.getArrowheadLength();

    // adjust radius to take into account proportionOfThickness and radiusAdjustment values;
    // radius = radius - 0.5d * featureThickness + 0.5d * (proportionOfThickness * featureThickness)
    // + radiusAdjustment * (featureThickness - (proportionOfThickness * featureThickness));
    double newRadius = radius;
    if (proportionOfThickness < 1.0f) {
      newRadius = radius - 0.5d * featureThickness;
      newRadius = newRadius + featureThickness * radiusAdjustment;
    }
    // prevent drawing outside of feature slot for this feature
    double maxRadiusForThisFeature =
      radius +
      featureThickness *
      0.5d -
      (0.5d * featureThickness * proportionOfThickness);
    double minRadiusForThisFeature =
      radius -
      featureThickness *
      0.5d +
      (0.5d * featureThickness * proportionOfThickness);

    if (newRadius > maxRadiusForThisFeature) {
      newRadius = maxRadiusForThisFeature;
    }
    if (newRadius < minRadiusForThisFeature) {
      newRadius = minRadiusForThisFeature;
    }
    radius = newRadius;

    featureThickness = proportionOfThickness * featureThickness;

    double startOfArc;
    double extentOfArc;

    // typical case where start is less than stop
    if (startBase <= stopBase) {
      startOfArc = cgview.getDegrees(startBase - 1);
      extentOfArc = cgview.getDegrees(stopBase) - startOfArc;
    }
    // case where feature spans junction
    else {
      startOfArc = cgview.getDegrees(startBase - 1);
      extentOfArc = cgview.getDegrees(totalBases) - startOfArc;

      double startOfArcB = cgview.getDegrees(1 - 1);
      double extentOfArcB = cgview.getDegrees(stopBase) - startOfArcB;

      extentOfArc = extentOfArc + extentOfArcB;
    }

    int arrowBase;
    double extentOfArcShift;
    double arrowPointRadians;
    double arrowBaseRadians;

    arrowBase = startBase - 1;
    extentOfArcShift = (arrowLength / radius) * (180.0d / Math.PI);

    // this centers the arrow head when there is no arc
    if (
      ((extentOfArc * (Math.PI / 180.0d) * radius) < (arrowLength)) &&
      (shiftSmallFeatures)
    ) {
      // extentOfArc = extentOfArc - 0.5d * extentOfArcShift;
      // startOfArc = startOfArc + 0.5d * extentOfArcShift;

      // now determine the position in radians of the arrow head base
      // arrowBaseRadians = -(-cgview.getDegrees(arrowBase) + originOffset - 0.5d * ((arrowLength /
      // radius) * (180.0d / Math.PI))) * (Math.PI / 180.0d);
      arrowBaseRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset -
          0.5d *
          extentOfArc -
          0.5d *
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);

      // now determine the position in radians of the arrow head point
      // arrowPointRadians = -(-cgview.getDegrees(arrowBase) + originOffset + ((arrowLength /
      // radius) * (180.0d / Math.PI)) - 0.5d * ((arrowLength / radius) * (180.0d / Math.PI))) *
      // (Math.PI / 180.0d);
      arrowPointRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset -
          0.5d *
          extentOfArc +
          ((arrowLength / radius) * (180.0d / Math.PI)) -
          0.5d *
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);

      extentOfArc = 0.0d;
    } else {
      // startOfArc = startOfArc;
      extentOfArc = extentOfArc - extentOfArcShift;
      startOfArc = startOfArc + extentOfArcShift;

      // now determine the position in radians of the arrow head base
      arrowBaseRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset -
          1.0d *
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);

      // now determine the position in radians of the arrow head point
      arrowPointRadians =
        -(
          -cgview.getDegrees(arrowBase) +
          originOffset +
          ((arrowLength / radius) * (180.0d / Math.PI)) -
          1.0d *
          ((arrowLength / radius) * (180.0d / Math.PI))
        ) *
        (Math.PI / 180.0d);
    }

    // create areas for drawing the arc
    Area centralArrow = new Area();
    Area highlightArrow = new Area();
    Area shadowArrow = new Area();

    // determine the radius of the outer edge of the arrow
    double outerEdgeRadius = radius + 0.5d * featureThickness;

    // determine the radius of the inner edge of the arrow
    double innerEdgeRadius = radius - 0.5d * featureThickness;

    // create a GeneralPath to describe the arrow head
    GeneralPath arrow = new GeneralPath(GeneralPath.WIND_NON_ZERO);
    arrow.moveTo(
      (float) (Math.cos(arrowBaseRadians) * outerEdgeRadius),
      (float) (Math.sin(arrowBaseRadians) * outerEdgeRadius)
    );
    arrow.lineTo(
      (float) (Math.cos(arrowBaseRadians) * innerEdgeRadius),
      (float) (Math.sin(arrowBaseRadians) * innerEdgeRadius)
    );
    arrow.lineTo(
      (float) (Math.cos(arrowPointRadians) * radius),
      (float) (Math.sin(arrowPointRadians) * radius)
    );
    arrow.closePath();
    centralArrow.add(new Area(arrow));

    if (showShading) {
      // create a GeneralPath to draw a highlight on the arrow
      GeneralPath arrowHighlight = new GeneralPath(GeneralPath.WIND_NON_ZERO);
      arrowHighlight.moveTo(
        (float) (Math.cos(arrowBaseRadians) * outerEdgeRadius),
        (float) (Math.sin(arrowBaseRadians) * outerEdgeRadius)
      );
      arrowHighlight.lineTo(
        (float) (Math.cos(arrowPointRadians) * radius),
        (float) (Math.sin(arrowPointRadians) * radius)
      );
      arrowHighlight.lineTo(
        (float) (
          Math.cos(arrowBaseRadians) *
          (outerEdgeRadius - featureThickness * shadingProportion)
        ),
        (float) (
          Math.sin(arrowBaseRadians) *
          (outerEdgeRadius - featureThickness * shadingProportion)
        )
      );
      highlightArrow.add(new Area(arrowHighlight));

      // create a GeneralPath to draw a shadow on the arrow
      GeneralPath arrowShadow = new GeneralPath(GeneralPath.WIND_NON_ZERO);
      arrowShadow.moveTo(
        (float) (Math.cos(arrowBaseRadians) * innerEdgeRadius),
        (float) (Math.sin(arrowBaseRadians) * innerEdgeRadius)
      );
      arrowShadow.lineTo(
        (float) (Math.cos(arrowPointRadians) * radius),
        (float) (Math.sin(arrowPointRadians) * radius)
      );
      arrowShadow.lineTo(
        (float) (
          Math.cos(arrowBaseRadians) *
          (innerEdgeRadius + featureThickness * shadingProportion)
        ),
        (float) (
          Math.sin(arrowBaseRadians) *
          (innerEdgeRadius + featureThickness * shadingProportion)
        )
      );
      shadowArrow.add(new Area(arrowShadow));
    }

    // now draw the arc

    if (extentOfArc > 0.0d) {
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
      );
      gg.setPaint(color);
      BasicStroke arcStroke = new BasicStroke(
        featureThickness,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL
      );
      centralArrow.add(
        new Area(
          arcStroke.createStrokedShape(
            new Arc2D.Double(
              -radius,
              -radius,
              radius * 2.0d,
              radius * 2.0d,
              -startOfArc - extentOfArc + originOffset,
              extentOfArc,
              Arc2D.OPEN
            )
          )
        )
      );
      // to prevent drawing off canvas
      centralArrow.intersect(new Area(cgview.getBackgroundRectangle()));
      gg.fill(centralArrow);

      if (showShading) {
        // draw highlight
        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            highlightOpacity * opacity
          )
        );
        gg.setPaint(Color.white);
        double radiusIncrease =
          0.5d *
          featureThickness -
          0.5d *
          (featureThickness * shadingProportion);
        BasicStroke highlightArcStroke = new BasicStroke(
          featureThickness * (float) (shadingProportion),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL
        );
        highlightArrow.add(
          new Area(
            highlightArcStroke.createStrokedShape(
              new Arc2D.Double(
                -radius - radiusIncrease,
                -radius - radiusIncrease,
                (radius + radiusIncrease) * 2.0d,
                (radius + radiusIncrease) * 2.0d,
                -startOfArc - extentOfArc + originOffset,
                extentOfArc,
                Arc2D.OPEN
              )
            )
          )
        );
        highlightArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(highlightArrow);

        // draw shadow
        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            shadowOpacity * opacity
          )
        );
        gg.setPaint(Color.black);
        double radiusDecrease =
          -0.5d *
          featureThickness +
          0.5d *
          (featureThickness * shadingProportion);
        BasicStroke shadowArcStroke = new BasicStroke(
          featureThickness * (float) (shadingProportion),
          BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL
        );
        shadowArrow.add(
          new Area(
            shadowArcStroke.createStrokedShape(
              new Arc2D.Double(
                -radius - radiusDecrease,
                -radius - radiusDecrease,
                (radius + radiusDecrease) * 2.0d,
                (radius + radiusDecrease) * 2.0d,
                -startOfArc - extentOfArc + originOffset,
                extentOfArc,
                Arc2D.OPEN
              )
            )
          )
        );
        shadowArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(shadowArrow);

        // set back to 1.0f
        gg.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
        );
      }
    } else {
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity)
      );
      gg.setPaint(color);
      centralArrow.intersect(new Area(cgview.getBackgroundRectangle()));
      gg.fill(centralArrow);

      if (showShading) {
        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            highlightOpacity * opacity
          )
        );
        gg.setPaint(Color.white);
        highlightArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(highlightArrow);

        gg.setComposite(
          AlphaComposite.getInstance(
            AlphaComposite.SRC_OVER,
            shadowOpacity * opacity
          )
        );
        gg.setPaint(Color.black);
        shadowArrow.intersect(new Area(cgview.getBackgroundRectangle()));
        gg.fill(shadowArrow);

        // set back to 1.0f
        gg.setComposite(
          AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
        );
      }
    }
  }

  /**
   * Creates a Label object for this FeatureRange.
   *
   * @param cgview the Cgview object that contains this FeatureRange.
   * @param startBase the adjusted position of the first base in this FeatureRange.
   * @param stopBase the adjusted position of the last base in this FeatureRange.
   */
  private void labelStandard(Cgview cgview, int startBase, int stopBase) {
    double originOffset = cgview.getOrigin();
    int totalBases = cgview.getSequenceLength();
    double startOfArc;
    double extentOfArc;

    boolean drawLabel = true;

    // decide whether to create a label
    if (this.label == null) {
      drawLabel = false;
    } else {
      Pattern p = Pattern.compile("\\S");
      Matcher m = p.matcher(this.label);

      if (!(m.find())) {
        drawLabel = false;
      }
    }

    if ((this.mouseover != null) || (this.hyperlink != null)) {
      drawLabel = true;
    }

    if (drawLabel) {
      // typical case where start is less than stop
      if (startBase <= stopBase) {
        startOfArc = cgview.getDegrees(startBase - 1);
        extentOfArc = cgview.getDegrees(stopBase) - startOfArc;
      }
      // case where feature spans junction
      else {
        startOfArc = cgview.getDegrees(startBase - 1);
        extentOfArc = cgview.getDegrees(totalBases) - startOfArc;

        double startOfArcB = cgview.getDegrees(1 - 1);
        double extentOfArcB = cgview.getDegrees(stopBase) - startOfArcB;

        extentOfArc = extentOfArc + extentOfArcB;
      }

      // create the label
      double arcMidPoint =
        -((-startOfArc - extentOfArc + originOffset) + (extentOfArc / 2.0d)) *
        Math.PI /
        180.0d;
      if (arcMidPoint < 0.0d) {
        arcMidPoint = 2.0d * Math.PI + arcMidPoint;
      }

      String theLabel;
      Label createdLabel;

      // first decide what text to use in the label
      // if not zoomed in just show the label name
      if (
        (cgview.getGiveFeaturePositions() == POSITIONS_NO_SHOW) ||
        (
          (cgview.getZoomMultiplier() < cgview.getZoomShift()) &&
          (cgview.getGiveFeaturePositions() == POSITIONS_AUTO)
        )
      ) {
        theLabel = label;
      }
      // if zoomed in add position information
      else {
        if (startBase == stopBase) {
          theLabel = label + " " + Integer.toString(this.start);
        } else if (feature.getStrand() == REVERSE_STRAND) {
          if (
            (decoration == DECORATION_STANDARD) ||
            (decoration == DECORATION_CLOCKWISE_ARROW)
          ) {
            // theLabel = label + " " + Integer.toString(this.start) + "-" +
            // Integer.toString(this.stop);
            theLabel =
              label +
              " " +
              Integer.toString(this.start) +
              "-" +
              Integer.toString(this.stop);
          } else {
            // theLabel = label + " " + Integer.toString(this.stop) + "-" +
            // Integer.toString(this.start);
            theLabel =
              label +
              " " +
              Integer.toString(this.start) +
              "-" +
              Integer.toString(this.stop);
          }
        } else {
          if (
            (decoration == DECORATION_STANDARD) ||
            (decoration == DECORATION_CLOCKWISE_ARROW)
          ) {
            theLabel =
              label +
              " " +
              Integer.toString(this.start) +
              "-" +
              Integer.toString(this.stop);
          } else {
            theLabel =
              label +
              " " +
              Integer.toString(this.stop) +
              "-" +
              Integer.toString(this.start);
          }
        }
      }

      // now decide whether to make the label outside of the backbone or inside
      // if not zoomed, make OuterLabels only
      if (
        (cgview.getUseInnerLabels() == INNER_LABELS_NO_SHOW) ||
        (
          (cgview.getZoomMultiplier() < cgview.getZoomShift()) &&
          (cgview.getUseInnerLabels() == INNER_LABELS_AUTO)
        )
      ) {
        createdLabel =
          new OuterLabel(
            cgview,
            theLabel,
            hyperlink,
            mouseover,
            font,
            color,
            forceLabel,
            arcMidPoint,
            feature.getStrand()
          );
      } else {
        if (feature.getStrand() == DIRECT_STRAND) {
          createdLabel =
            new OuterLabel(
              cgview,
              theLabel,
              hyperlink,
              mouseover,
              font,
              color,
              forceLabel,
              arcMidPoint,
              feature.getStrand()
            );
        } else {
          createdLabel =
            new InnerLabel(
              cgview,
              theLabel,
              hyperlink,
              mouseover,
              font,
              color,
              forceLabel,
              arcMidPoint,
              feature.getStrand()
            );
        }
      }
    }
  }

  /**
   * Sets whether or not a label a should be drawn for this FeatureRange.
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
   * Returns whether or not a label should be generated for this FeatureRange when drawn.
   *
   * @return {@link CgviewConstants#LABEL CgviewConstants.LABEL}, {@link CgviewConstants#LABEL_NONE
   *     CgviewConstants.NO_LABEL}, or {@link CgviewConstants#LABEL_FORCE
   *     CgviewConstants.LABEL_FORCE}.
   */
  public int getShowLabel() {
    return showLabel;
  }

  /**
   * Sets the type of decoration added to this FeatureRange when drawn.
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
   * Returns an integer indicating what type of decoration will be added to this FeatureRange when
   * drawn.
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
   * Sets the label text for this FeatureRange.
   *
   * @param label the label for this FeatureRange.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Returns the label text for this FeatureRange.
   *
   * @return the label for this FeatureRange.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the position of this FeatureRange relative to the FeatureSlot object that contains it.
   * This value is only applied when the thickness of this FeatureRange is adjusted using {@link
   * #setProportionOfThickness(float)} or {@link Feature#setProportionOfThickness(float)}.
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
   * Returns the position of this FeatureRange relative to the FeatureSlot object that contains it.
   * This value is only applied when the thickness of this FeatureRange is adjusted using {@link
   * #setProportionOfThickness(float)} or {@link Feature#setProportionOfThickness(float)}.
   *
   * @return a <code>float</code> between <code>0</code> and <code>1</code>, with <code>1</code>
   *     being near the edge furthest from the map center.
   * @see #setProportionOfThickness(float)
   */
  public float getRadiusAdjustment() {
    return radiusAdjustment;
  }

  /**
   * Sets the thickness of this FeatureRange when drawn, as a proportion of the thickness of the
   * FeatureSlot containing this Feature.
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
   * Returns the thickness of this FeatureRange when drawn, as a proportion of the thickness of the
   * FeatureSlot containing this FeatureRange.
   *
   * @return a <code>float</code> between <code>0</code> and <code>1</code>, with <code>1</code>
   *     being full thickness.
   */
  public float getProportionOfThickness() {
    return proportionOfThickness;
  }

  /**
   * Sets the opacity of this FeatureRange when drawn.
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
   * Returns the opacity of this FeatureRange when drawn.
   *
   * @return the opacity between <code>0</code> and <code>1</code>, with <code>1</code> being the
   *     most opaque.
   */
  public float getOpacity() {
    return opacity;
  }

  /**
   * Sets the color of this FeatureRange when drawn.
   *
   * @param color the color of this FeatureRange when drawn.
   */
  public void setColor(Color color) {
    this.color = color;
  }

  /**
   * Returns the color of this FeatureRange when drawn.
   *
   * @return the color of this FeatureRange when drawn.
   */
  public Color getColor() {
    return color;
  }

  /**
   * Sets the font used for the label generated for this FeatureRange.
   *
   * @param font the font used for the label generated for this FeatureRange.
   */
  public void setFont(Font font) {
    this.font = font;
  }

  /**
   * Returns the font used for the label generated for this FeatureRange.
   *
   * @return the font used for the label generated for this FeatureRange.
   */
  public Font getFont() {
    return font;
  }

  /**
   * Sets whether or not this FeatureRange should be drawn with shading.
   *
   * @param showShading whether or not this FeatureRange should be drawn with shading.
   */
  public void setShowShading(boolean showShading) {
    this.showShading = showShading;
  }

  /**
   * Returns whether or not this FeatureRange should be drawn with shading.
   *
   * @return whether or not this FeatureRange should be drawn with shading.
   */
  public boolean getShowShading() {
    return showShading;
  }

  /**
   * Specifies a hyperlink to be associated with this FeatureRange. Hyperlinks are included in SVG
   * output generated using {@link CgviewIO#writeToSVGFile(ca.ualberta.stothard.cgview.Cgview,
   * java.lang.String, boolean, boolean)} or in image maps for PNG and JPG images generated using
   * {@link CgviewIO#writeHTMLFile(ca.ualberta.stothard.cgview.Cgview, java.lang.String,
   * java.lang.String, java.lang.String)}.
   *
   * @param hyperlink a hyperlink for this FeatureRange.
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
   * Specifies a mouseover to be associated with this FeatureRange. Mouseovers are included in SVG
   * output generated using {@link CgviewIO#writeToSVGFile(ca.ualberta.stothard.cgview.Cgview,
   * java.lang.String, boolean, boolean)} or in image maps for PNG and JPG images generated using
   * {@link CgviewIO#writeHTMLFile(ca.ualberta.stothard.cgview.Cgview, java.lang.String,
   * java.lang.String, java.lang.String)}.
   *
   * @param mouseover the mouseover for this featureRange.
   */
  public void setMouseover(String mouseover) {
    this.mouseover = mouseover;
  }

  /**
   * Returns the mouseover to be associated with this FeatureRange.
   *
   * @return the mouseover for this FeatureRange.
   */
  public String getMouseover() {
    return mouseover;
  }
}
