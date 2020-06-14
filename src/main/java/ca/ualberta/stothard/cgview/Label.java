package ca.ualberta.stothard.cgview;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.util.*;
import java.util.regex.*;

/**
 * This class is used by Cgview objects to facilitate label layout and drawing.
 *
 * @author Paul Stothard
 */
public abstract class Label implements CgviewConstants {
  protected String labelText;
  protected Color color;
  protected int strand;

  protected String hyperlink;
  protected String mouseover;

  protected double lineStartRadians;
  protected double lineStartRadius;
  protected double lineEndRadians;
  protected double lineEndRadius;

  protected boolean extendedRadius;
  protected double extendedLineStartRadius;
  protected double extendedLineEndRadius;

  protected boolean fixedInPlace;
  protected double allowedRadiansDelta;

  protected static double radiusShiftAmount;
  protected static double radiansShiftAmount;

  protected Rectangle2D unplacedBounds;
  protected Rectangle2D placedBounds;

  protected Font font;
  protected float descent;
  protected float ascent;

  protected boolean forceLabel;

  protected Cgview cgview;

  protected static double smallestDimension = 0;

  protected static double RADIAN_SHIFT_PADDING = 3.0d;

  /**
   * Constructs a new Label object.
   *
   * @param cgview the Cgview object to contain this Label.
   * @param labelText the text that is to be drawn.
   * @param hyperlink a hyperlink to be associated with this Label.
   * @param mouseover mouseover information to be associated with this Label.
   * @param font the font to use when drawing this Label.
   * @param color the color to use when drawing this Label.
   * @param forceLabel whether or not this Label should be drawn even if it cannot be placed such
   *     that it does not clash with other labels.
   * @param lineStartRadians the angle in radians of the line extending from the feature to this
   *     Label.
   * @param strand the strand of this Label ({@link CgviewConstants#DIRECT_STRAND} or {@link
   *     CgviewConstants#REVERSE_STRAND}).
   */
  protected Label(
    Cgview cgview,
    String labelText,
    String hyperlink,
    String mouseover,
    Font font,
    Color color,
    boolean forceLabel,
    double lineStartRadians,
    int strand
  ) {
    this.cgview = cgview;
    this.labelText = labelText;
    this.hyperlink = hyperlink;
    this.mouseover = mouseover;
    this.color = color;
    this.forceLabel = forceLabel;
    this.lineStartRadians = lineStartRadians;
    this.strand = strand;

    if (this.labelText == null) {
      this.labelText = "Untitled";
    }

    Pattern p = Pattern.compile("\\S");
    Matcher m = p.matcher(this.labelText);

    if (!(m.find())) {
      this.labelText = "Untitled";
    }

    if (font != null) {
      this.font = font;
    } else {
      this.font = cgview.getLabelFont();
    }

    lineEndRadians = lineStartRadians;

    // if close to vertical
    if (Math.abs(Math.sin(lineStartRadians)) > 0.70d) {
      allowedRadiansDelta = (1.0d / 16.0d) * (2.0d * Math.PI);
    } else {
      allowedRadiansDelta = (1.0d / 10.0d) * (2.0d * Math.PI);
    }
    extendedRadius = false;
    fixedInPlace = false;

    // create bounds
    Graphics2D gg = cgview.getGraphics();
    FontRenderContext frc = gg.getFontRenderContext();
    TextLayout layout = new TextLayout(this.labelText, this.font, frc);

    unplacedBounds = layout.getBounds();
    placedBounds = layout.getBounds();
    descent = layout.getDescent();
    ascent = layout.getAscent();

    if (smallestDimension == 0) {
      if (unplacedBounds.getWidth() > unplacedBounds.getHeight()) {
        smallestDimension = unplacedBounds.getHeight();
      } else {
        smallestDimension = unplacedBounds.getWidth();
      }
    } else {
      if (unplacedBounds.getWidth() < smallestDimension) {
        smallestDimension = unplacedBounds.getWidth();
      }
      if (unplacedBounds.getHeight() < smallestDimension) {
        smallestDimension = unplacedBounds.getHeight();
      }
    }

    radiusShiftAmount = cgview.getRadiusShiftAmount();

    // radiansShiftConstant may need to be made smaller if label lines are crossing
    // radiansShiftAmount = cgview.getRadiansShiftConstant() / cgview.getLastOuterFeatureRadius();
    radiansShiftAmount =
      ((smallestDimension / 2) / (cgview.getLastOuterFeatureRadius()));
    // need to adjust some values for zooming
    // radiansShiftAmount = radiansShiftAmount / cgview.getZoomMultiplier();
    // allowedRadiansDelta = allowedRadiansDelta / cgview.getZoomMultiplier();
  }

  protected abstract void drawLabelText();

  protected abstract void drawLabelLine();

  /**
   * Draws a line between two points described by radius and radian values.
   *
   * @param startRadius the radius of the first point.
   * @param startRadians the radians of the first point.
   * @param endRadius the radius of the second point.
   * @param endRadians the radians of the second point.
   */
  protected void drawLine(
    double startRadius,
    double startRadians,
    double endRadius,
    double endRadians
  ) {
    Graphics2D gg = cgview.getGraphics();

    float labelLineThickness = cgview.getLabelLineThickness();

    double lineX1 = Math.cos(startRadians) * startRadius;
    double lineY1 = Math.sin(startRadians) * startRadius;
    double lineX2 = Math.cos(endRadians) * endRadius;
    double lineY2 = Math.sin(endRadians) * endRadius;

    if (cgview.getGlobalLabelColor() != null) {
      gg.setPaint(cgview.getGlobalLabelColor());
    } else {
      gg.setPaint(color);
    }

    gg.setStroke(
      new BasicStroke(
        labelLineThickness,
        BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_MITER
      )
    );
    gg.draw(new Line2D.Double(lineX1, lineY1, lineX2, lineY2));
  }

  /**
   * Draws a line between two points described by radius and radian values.
   *
   * @param startRadius the radius of the first point.
   * @param startRadians the radians of the first point.
   * @param endRadius the radius of the second point.
   * @param endRadians the radians of the second point.
   * @param area an area to add to the line.
   */
  protected void drawLine(
    double startRadius,
    double startRadians,
    double endRadius,
    double endRadians,
    Area area
  ) {
    Graphics2D gg = cgview.getGraphics();

    float labelLineThickness = cgview.getLabelLineThickness();

    double lineX1 = Math.cos(startRadians) * startRadius;
    double lineY1 = Math.sin(startRadians) * startRadius;
    double lineX2 = Math.cos(endRadians) * endRadius;
    double lineY2 = Math.sin(endRadians) * endRadius;

    if (cgview.getGlobalLabelColor() != null) {
      gg.setPaint(cgview.getGlobalLabelColor());
    } else {
      gg.setPaint(color);
    }

    BasicStroke lineStroke = new BasicStroke(
      labelLineThickness,
      BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_MITER
    );
    area.add(
      new Area(
        lineStroke.createStrokedShape(
          new Line2D.Double(lineX1, lineY1, lineX2, lineY2)
        )
      )
    );

    gg.fill(area);
  }

  /**
   * Returns an Area representing a line between two points described by radius and radian values.
   *
   * @param startRadius the radius of the first point.
   * @param startRadians the radians of the first point.
   * @param endRadius the radius of the second point.
   * @param endRadians the radians of the second point.
   * @return an Area to add to the line.
   */
  protected Area getLineAsArea(
    double startRadius,
    double startRadians,
    double endRadius,
    double endRadians
  ) {
    Graphics2D gg = cgview.getGraphics();

    float labelLineThickness = cgview.getLabelLineThickness();

    double lineX1 = Math.cos(startRadians) * startRadius;
    double lineY1 = Math.sin(startRadians) * startRadius;
    double lineX2 = Math.cos(endRadians) * endRadius;
    double lineY2 = Math.sin(endRadians) * endRadius;

    Area area = new Area();

    BasicStroke lineStroke = new BasicStroke(
      labelLineThickness,
      BasicStroke.CAP_ROUND,
      BasicStroke.JOIN_MITER
    );
    area.add(
      new Area(
        lineStroke.createStrokedShape(
          new Line2D.Double(lineX1, lineY1, lineX2, lineY2)
        )
      )
    );

    return area;
  }

  /**
   * Returns the starting point of the line drawn to this Label.
   *
   * @return the starting point of the line drawn to this Label.
   */
  protected Point2D getLineStart() {
    return new Point2D.Double(
      Math.cos(this.lineStartRadians) * this.lineStartRadius,
      Math.sin(this.lineStartRadians) * this.lineStartRadius
    );
  }

  /**
   * Returns the radians of the innermost point in the line extending from the feature to this
   * Label.
   *
   * @return the radians of the innermost point in the label line.
   */
  protected double getLineStartRadians() {
    return lineStartRadians;
  }

  /**
   * Returns the radians of the outermost point in the line extending from the feature to this
   * Label.
   *
   * @return the radians of the outermost point in the label line.
   */
  protected double getLineEndRadians() {
    return lineEndRadians;
  }

  /**
   * Returns a boolean specifying whether or not the line between the feature and this Label has
   * been extended to prevent label clashes.
   *
   * @return a <code>boolean</code> representing whether or not the line between the feature and
   *     this Label has been extended to prevent label clashes.
   */
  protected boolean isExtendedRadius() {
    return extendedRadius;
  }

  /**
   * Returns the radius of the innermost point in the line extending from the feature to this Label.
   *
   * @return the radius of the innermost point in the label line.
   */
  protected double getLineStartRadius() {
    return lineStartRadius;
  }

  /**
   * Returns the radius of the outermost point in the line extending from the feature to this Label.
   *
   * @return the radius of the outermost point in the label line.
   */
  protected double getLineEndRadius() {
    return lineEndRadius;
  }

  /**
   * Returns the radius of the outermost point in the second line (the extended line) extending from
   * the feature to this Label.
   *
   * @return the radius of the outermost point in the second label line.
   */
  protected double getExtendedLineEndRadius() {
    return extendedLineEndRadius;
  }

  /**
   * Returns a boolean specifying whether or not this label should be drawn even if it cannot be
   * placed such that it does not clash with other labels.
   *
   * @return a <code>boolean</code> specifying whether or not this Label should be drawn even if it
   *     cannot be placed such that it does not clash with other labels.
   */
  protected boolean getForceLabel() {
    return forceLabel;
  }

  protected abstract void updateBounds();

  protected abstract void updateBounds(double padding);

  /**
   * Returns a rectangle that represents the bounds of this Label. {@link #updateBounds()} should be
   * used to calculate the bounds if the Label has been moved since the last call to {@link
   * #updateBounds()}.
   *
   * @return the bounds of this Label.
   */
  protected Rectangle2D getBounds() {
    return placedBounds;
  }

  /**
   * Returns a boolean specifying whether or not this Label clashes with the supplied Label.
   *
   * @param testLabel a Label object.
   * @return a <code>boolean</code> specifying whether this label clashes with the supplied label.
   */
  protected boolean clashes(Label testLabel) {
    return this.getBounds().intersects(testLabel.getBounds());
  }

  protected abstract boolean clashesWithAny();

  /**
   * Attempts to move this Label by decreasing its radians value. If the angle between the the start
   * of the label line and the label text is at the maximum acceptable value the label is not moved.
   *
   * @return a <code>boolean</code> specifing whether or not this Label was moved.
   */
  protected final boolean shiftRadiansLower() {
    if (
      Math.abs(lineStartRadians - (lineEndRadians - radiansShiftAmount)) <
      (allowedRadiansDelta)
    ) {
      lineEndRadians = lineEndRadians - radiansShiftAmount;
      updateBounds(3.0d);
      return true;
    } else {
      // System.out.println("failed to shift radians lower");
      return false;
    }
  }

  /**
   * Attempts to move this Label by increasing its radians value. If the angle between the the start
   * of the label line and the label text is at the maximum acceptable value the label is not moved.
   *
   * @return a <code>boolean</code> specifing whether or not this Label was moved.
   */
  protected final boolean shiftRadiansHigher() {
    if (
      Math.abs(lineStartRadians - (lineEndRadians + radiansShiftAmount)) <
      (allowedRadiansDelta)
    ) {
      lineEndRadians = lineEndRadians + radiansShiftAmount;
      updateBounds(3.0d);
      return true;
    } else {
      // System.out.println("failed to shift radians higher");
      return false;
    }
  }

  /**
   * Attempts to move this Label by adjusting its radians value such that it is closer to its
   * original radians value. If the adjustment introduces a conflict for space with other labels,
   * this Label is not moved.
   *
   * @return a <code>boolean</code> specifing whether or not this Label was moved.
   */
  protected boolean shiftRadiansToOriginal(ArrayList labels) {
    if (lineEndRadians == lineStartRadians) {
      // they are already the same
      return false;
    } else if (
      Math.abs(lineStartRadians - lineEndRadians) <= (radiansShiftAmount)
    ) {
      // already close enough, make them the same if possible
      double tempRadians = lineEndRadians;
      lineEndRadians = lineStartRadians;
      updateBounds(3.0d);
      if (clashesWithAny(labels)) {
        lineEndRadians = tempRadians;
        updateBounds(3.0d);
        return false;
      } else {
        return false;
      }
    } else if (lineStartRadians < lineEndRadians) {
      shiftRadiansLower();
      if (clashesWithAny(labels)) {
        shiftRadiansHigher();
        return false;
      } else {
        return true;
      }
    } else if (lineStartRadians > lineEndRadians) {
      this.shiftRadiansHigher();
      if (clashesWithAny(labels)) {
        shiftRadiansLower();
        return false;
      } else {
        return true;
      }
    }
    return false;
  }

  protected abstract boolean extendRadius();

  protected abstract void setLineStartRadius(double lineStartRadius);

  /**
   * Returns a boolean specifying whether or not this Label clashes with any of the labels in the
   * supplied ArrayList.
   *
   * @param labels a list of Label objects.
   * @return a <code>boolean</code> specifying whether or not this Label clashes with any of the
   *     Label objects in the supplied list.
   */
  protected boolean clashesWithAny(ArrayList labels) {
    if (labels.size() < 2) {
      return false;
    }
    for (int outer = 0; outer < labels.size(); outer++) {
      Label outerLabel = (Label) (labels.get(outer));
      if (labels.indexOf(this) == outer) {
        continue;
      } else if (this.clashes(outerLabel)) {
        return true;
      }
    }
    return false;
  }

  protected abstract boolean fitsInBackground();

  /**
   * Specifies the hyperlink to be associated with this Label.
   *
   * @param hyperlink the hyperlink to be associated with this Label.
   */
  protected void setHyperlink(String hyperlink) {
    this.hyperlink = hyperlink;
  }

  /**
   * Returns the hyperlink to be associated with this Label.
   *
   * @return the hyperlink to be associated with this Label.
   */
  protected String getHyperlink() {
    return hyperlink;
  }

  /**
   * Returns the text of this Label.
   *
   * @return the text of this Label.
   */
  protected String getLabelText() {
    return labelText;
  }

  /**
   * The mouseover to be associated with this Label.
   *
   * @param mouseover the mouseover to be associated with this Label.
   */
  protected void setMouseover(String mouseover) {
    this.mouseover = mouseover;
  }

  /**
   * Returns the mouseover to be associated with this Label.
   *
   * @return the mouseover to be associated with this Label.
   */
  protected String getMouseover() {
    return mouseover;
  }
}
