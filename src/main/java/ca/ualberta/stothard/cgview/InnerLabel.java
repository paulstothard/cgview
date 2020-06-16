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
import java.util.*;

/**
 * This class is used by Cgview objects to facilitate label layout and drawing. The labels
 * represented by this class are drawn on the inside of the sequence backbone.
 *
 * @author Paul Stothard
 */
public class InnerLabel extends Label implements CgviewConstants {

  /**
   * Constructs a new InnerLabel object.
   *
   * @param cgview the Cgview object to contain this Label.
   * @param labelText the text that is to be drawn.
   * @param hyperlink a hyperlink to be associated with this Label.
   * @param mouseover mouseover information to be associated with this Label.
   * @param font the font to use when drawing this Label.
   * @param color the color to use when drawing the Label.
   * @param forceLabel whether or not this Label should be drawn even if it cannot be placed such
   *     that it does not clash with other labels.
   * @param lineStartRadians the angle in radians of the line extending from the feature to the
   *     label.
   * @param strand the strand of this Label ({@link CgviewConstants#DIRECT_STRAND} or {@link
   *     CgviewConstants#REVERSE_STRAND}).
   */
  protected InnerLabel(
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
    super(
      cgview,
      labelText,
      hyperlink,
      mouseover,
      font,
      color,
      forceLabel,
      lineStartRadians,
      strand
    );
    cgview.addInnerLabel(this);
  }

  /**
   * Returns a boolean specifying whether or not this Label currently clashes with other Label
   * objects in the Cgview object containing this Label.
   *
   * @return a <code>boolean</code> specifying whether or not this Label clashes with other Labels.
   */
  protected boolean clashesWithAny() {
    // need to restrict comparison to Labels of the same class.
    ArrayList labels = cgview.getInnerLabels();
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

  /** Draws a line joining the feature to this Label. */
  protected void drawLabelLine() {
    double extension = descent;
    double lineShiftAmount = 0.01d;
    double tempRadius;

    double lineX2;
    double lineY2;
    Point2D checkPoint = new Point2D.Double(0.0d, 0.0d);

    double textPositionX;
    double textPositionY;

    // Graphics2D gg = cgview.getGraphics();

    // FontRenderContext frc = gg.getFontRenderContext();
    // TextLayout layout = new TextLayout(labelText, font, frc);

    double textHeight = unplacedBounds.getHeight();
    double textWidth = unplacedBounds.getWidth();

    if (extendedRadius) {
      textPositionX = (Math.cos(lineEndRadians) * (extendedLineEndRadius));
      textPositionY = (Math.sin(lineEndRadians) * (extendedLineEndRadius));
    } else {
      textPositionX = (Math.cos(lineEndRadians) * (lineEndRadius));
      textPositionY = (Math.sin(lineEndRadians) * (lineEndRadius));
    }

    // double textHeight = bounds.getHeight();
    // double textWidth = bounds.getWidth();
    // adjust text position based on radians for label.
    if (
      (Math.sin(lineEndRadians) <= 1) &&
      (Math.sin(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) <= 1)
    ) { // 0 to 90 degrees
      // if close to 90 degrees
      if (Math.sin(lineEndRadians) > 0.90d) {
        textPositionX =
          textPositionX -
          textWidth +
          Math.sin(lineEndRadians) *
          0.5d *
          textWidth;
        // textPositionY = textPositionY;
      } else {
        textPositionX = textPositionX - textWidth;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else if (
      (Math.sin(lineEndRadians) <= 1) &&
      (Math.sin(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) <= 0) &&
      (Math.cos(lineEndRadians) >= -1)
    ) { // 90 to 180 degrees
      // if close to 90 degrees
      if (Math.sin(lineEndRadians) > 0.90d) {
        textPositionX =
          textPositionX - Math.sin(lineEndRadians) * 0.5d * textWidth;
        // textPositionY = textPositionY;
      } else {
        // textPositionX = textPositionX;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else if (
      (Math.sin(lineEndRadians) <= 0) &&
      (Math.sin(lineEndRadians) >= -1) &&
      (Math.cos(lineEndRadians) <= 0) &&
      (Math.cos(lineEndRadians) >= -1)
    ) { // 180 to 270 degrees
      // if close to 270 degrees
      if (Math.sin(lineEndRadians) < -0.90d) {
        textPositionX =
          textPositionX -
          textWidth -
          Math.sin(lineEndRadians) *
          0.5 *
          textWidth;
        textPositionY = textPositionY + textHeight;
      } else {
        // textPositionX = textPositionX;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else { // 270 to 360
      // if close to 270 degrees
      if (Math.sin(lineEndRadians) < -0.90d) {
        textPositionX =
          textPositionX -
          textWidth -
          Math.sin(lineEndRadians) *
          0.5d *
          textWidth;
        textPositionY = textPositionY + textHeight;
      } else {
        textPositionX = textPositionX - textWidth;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    }

    double compensate = 0.0d;

    if (cgview.getDrawEntirePlasmid()) {
      compensate = 0.0d;
    }

    placedBounds.setRect(
      unplacedBounds.getX() + textPositionX - 1.5d - compensate,
      unplacedBounds.getY() + textPositionY - descent - 1.5d - compensate,
      unplacedBounds.getWidth() + 3.0d,
      unplacedBounds.getHeight() + 3.0d
    );

    // gg.setColor(Color.red);
    // gg.draw(placedBounds);

    // now move label line end until it no longer intersects placedBounds
    if (cgview.getUseColoredLabelBackgrounds()) {
      if (extendedRadius) {
        // draw first line
        Area area =
          this.getLineAsArea(
              lineStartRadius,
              lineStartRadians,
              lineEndRadius,
              lineEndRadians
            );
        // draw second line
        this.drawLine(
            lineEndRadius,
            lineEndRadians,
            extendedLineEndRadius + extension,
            lineEndRadians,
            area
          );
      } else {
        this.drawLine(
            lineStartRadius,
            lineStartRadians,
            lineEndRadius + extension,
            lineEndRadians
          );
      }
    } else {
      if (extendedRadius) {
        // draw first line
        Area area =
          this.getLineAsArea(
              lineStartRadius,
              lineStartRadians,
              lineEndRadius,
              lineEndRadians
            );

        tempRadius = extendedLineEndRadius - extension;
        lineX2 = Math.cos(lineEndRadians) * (tempRadius);
        lineY2 = Math.sin(lineEndRadians) * (tempRadius);
        checkPoint.setLocation(lineX2, lineY2);

        while (placedBounds.contains(checkPoint)) {
          tempRadius = tempRadius + lineShiftAmount;
          lineX2 = Math.cos(lineEndRadians) * (tempRadius);
          lineY2 = Math.sin(lineEndRadians) * (tempRadius);
          checkPoint.setLocation(lineX2, lineY2);
        }

        // now remove some radius for the line end cap
        tempRadius = tempRadius + cgview.getLabelLineThickness();

        // draw second line
        this.drawLine(
            lineEndRadius,
            lineEndRadians,
            tempRadius,
            lineEndRadians,
            area
          );
      } else {
        tempRadius = lineEndRadius - extension;
        lineX2 = Math.cos(lineEndRadians) * (tempRadius);
        lineY2 = Math.sin(lineEndRadians) * (tempRadius);
        checkPoint.setLocation(lineX2, lineY2);

        while (placedBounds.contains(checkPoint)) {
          tempRadius = tempRadius + lineShiftAmount;
          lineX2 = Math.cos(lineEndRadians) * (tempRadius);
          lineY2 = Math.sin(lineEndRadians) * (tempRadius);
          checkPoint.setLocation(lineX2, lineY2);
        }

        // now remove some radius for the line end cap
        tempRadius = tempRadius + cgview.getLabelLineThickness();
        this.drawLine(
            lineStartRadius,
            lineStartRadians,
            tempRadius,
            lineEndRadians
          );
      }
    }
    // 	if (extendedRadius) {
    // 	    this.drawLine(lineStartRadius, lineStartRadians, lineEndRadius, lineEndRadians);
    // 	}
    // 	else {
    // 	    this.drawLine(lineStartRadius, lineStartRadians, lineEndRadius - extension,
    // lineEndRadians);
    // 	}

    // 	//now check to see if there is an extended line for this label and if there is one draw it.
    // 	if (extendedRadius) {

    // 	    this.drawLine(lineEndRadius, lineEndRadians, extendedLineEndRadius - extension,
    // lineEndRadians);

    // 	}
  }

  /** Draws the text portion of this Label. */
  protected void drawLabelText() {
    double textPositionX;
    double textPositionY;

    Graphics2D gg = cgview.getGraphics();
    Color backgroundColor = cgview.getBackgroundColor();

    FontRenderContext frc = gg.getFontRenderContext();
    TextLayout layout = new TextLayout(labelText, font, frc);

    double textHeight = unplacedBounds.getHeight();
    double textWidth = unplacedBounds.getWidth();

    if (extendedRadius) {
      textPositionX = (Math.cos(lineEndRadians) * (extendedLineEndRadius));
      textPositionY = (Math.sin(lineEndRadians) * (extendedLineEndRadius));
    } else {
      textPositionX = (Math.cos(lineEndRadians) * (lineEndRadius));
      textPositionY = (Math.sin(lineEndRadians) * (lineEndRadius));
    }

    // double textHeight = bounds.getHeight();
    // double textWidth = bounds.getWidth();
    // adjust text position based on radians for label.
    if (
      (Math.sin(lineEndRadians) <= 1) &&
      (Math.sin(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) <= 1)
    ) { // 0 to 90 degrees
      // if close to 90 degrees
      if (Math.sin(lineEndRadians) > 0.90d) {
        textPositionX =
          textPositionX -
          textWidth +
          Math.sin(lineEndRadians) *
          0.5d *
          textWidth;
        // textPositionY = textPositionY;
      } else {
        textPositionX = textPositionX - textWidth;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else if (
      (Math.sin(lineEndRadians) <= 1) &&
      (Math.sin(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) <= 0) &&
      (Math.cos(lineEndRadians) >= -1)
    ) { // 90 to 180 degrees
      // if close to 90 degrees
      if (Math.sin(lineEndRadians) > 0.90d) {
        textPositionX =
          textPositionX - Math.sin(lineEndRadians) * 0.5d * textWidth;
        // textPositionY = textPositionY;
      } else {
        // textPositionX = textPositionX;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else if (
      (Math.sin(lineEndRadians) <= 0) &&
      (Math.sin(lineEndRadians) >= -1) &&
      (Math.cos(lineEndRadians) <= 0) &&
      (Math.cos(lineEndRadians) >= -1)
    ) { // 180 to 270 degrees
      // if close to 270 degrees
      if (Math.sin(lineEndRadians) < -0.90d) {
        textPositionX =
          textPositionX -
          textWidth -
          Math.sin(lineEndRadians) *
          0.5 *
          textWidth;
        textPositionY = textPositionY + textHeight;
      } else {
        // textPositionX = textPositionX;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else { // 270 to 360
      // if close to 270 degrees
      if (Math.sin(lineEndRadians) < -0.90d) {
        textPositionX =
          textPositionX -
          textWidth -
          Math.sin(lineEndRadians) *
          0.5d *
          textWidth;
        textPositionY = textPositionY + textHeight;
      } else {
        textPositionX = textPositionX - textWidth;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    }

    // adjust text position for (0,0) in upper left

    textPositionX =
      textPositionX + cgview.getWidth() / 2 - cgview.getCenter().getX();
    textPositionY =
      textPositionY + cgview.getHeight() / 2 - cgview.getCenter().getY();

    double compensate = 0.0d;

    if (cgview.getDrawEntirePlasmid()) {
      compensate = 0.0d;
    }

    placedBounds.setRect(
      unplacedBounds.getX() + textPositionX - 1.5d - compensate,
      unplacedBounds.getY() + textPositionY - descent - 1.5d - compensate,
      unplacedBounds.getWidth() + 3.0d,
      unplacedBounds.getHeight() + 3.0d
    );

    // placedBounds.setRect(unplacedBounds.getX()+textPositionX, unplacedBounds.getY() +
    // textPositionY - descent, unplacedBounds.getWidth(), unplacedBounds.getHeight());

    if (cgview.getUseColoredLabelBackgrounds()) {
      if (cgview.getGlobalLabelColor() != null) {
        gg.setPaint(cgview.getGlobalLabelColor());
      } else {
        gg.setPaint(color);
      }
      gg.fill(placedBounds);

      // gg.setPaint(Color.blue);
      // gg.draw(placedBounds);

      gg.setPaint(backgroundColor);
      layout.draw(gg, (float) textPositionX, (float) textPositionY - descent);
    } else {
      gg.setPaint(backgroundColor);
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)
      );
      gg.fill(placedBounds);
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
      );
      if (cgview.getGlobalLabelColor() != null) {
        gg.setPaint(cgview.getGlobalLabelColor());
      } else {
        gg.setPaint(color);
      }
      layout.draw(gg, (float) textPositionX, (float) textPositionY - descent);
    }
  }

  /** Recalculates the rectangle that represents the bounds of this Label. */
  protected void updateBounds() {
    this.updateBounds(2.0d);
  }

  /**
   * Recalculates the rectangle that represents the bounds of this Label.
   *
   * @param padding the amount of padding to add to the bounds box.
   */
  protected void updateBounds(double padding) {
    double textPositionX;
    double textPositionY;

    float labelLineThickness = cgview.getLabelLineThickness();

    // now check to see if there is an extended line for this label and if there is adjust the
    // bounds.
    if (extendedRadius) {
      textPositionX =
        (
          Math.cos(lineEndRadians) *
          (extendedLineEndRadius + labelLineThickness)
        );
      textPositionY =
        (
          Math.sin(lineEndRadians) *
          (extendedLineEndRadius + labelLineThickness)
        );
    } else {
      textPositionX =
        (Math.cos(lineEndRadians) * (lineEndRadius + labelLineThickness));
      textPositionY =
        (Math.sin(lineEndRadians) * (lineEndRadius + labelLineThickness));
    }

    double textHeight = unplacedBounds.getHeight();
    double textWidth = unplacedBounds.getWidth();

    if (
      (Math.sin(lineEndRadians) <= 1) &&
      (Math.sin(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) <= 1)
    ) { // 0 to 90 degrees
      // if close to 90 degrees
      if (Math.sin(lineEndRadians) > 0.90d) {
        textPositionX =
          textPositionX -
          textWidth +
          Math.sin(lineEndRadians) *
          0.5d *
          textWidth;
        // textPositionY = textPositionY;
      } else {
        textPositionX = textPositionX - textWidth;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else if (
      (Math.sin(lineEndRadians) <= 1) &&
      (Math.sin(lineEndRadians) >= 0) &&
      (Math.cos(lineEndRadians) <= 0) &&
      (Math.cos(lineEndRadians) >= -1)
    ) { // 90 to 180 degrees
      // if close to 90 degrees
      if (Math.sin(lineEndRadians) > 0.90d) {
        textPositionX =
          textPositionX - Math.sin(lineEndRadians) * 0.5d * textWidth;
        // textPositionY = textPositionY;
      } else {
        // textPositionX = textPositionX;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else if (
      (Math.sin(lineEndRadians) <= 0) &&
      (Math.sin(lineEndRadians) >= -1) &&
      (Math.cos(lineEndRadians) <= 0) &&
      (Math.cos(lineEndRadians) >= -1)
    ) { // 180 to 270 degrees
      // if close to 270 degrees
      if (Math.sin(lineEndRadians) < -0.90d) {
        textPositionX =
          textPositionX -
          textWidth -
          Math.sin(lineEndRadians) *
          0.5 *
          textWidth;
        textPositionY = textPositionY + textHeight;
      } else {
        // textPositionX = textPositionX;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    } else { // 270 to 360
      // if close to 270 degrees
      if (Math.sin(lineEndRadians) < -0.90d) {
        textPositionX =
          textPositionX -
          textWidth -
          Math.sin(lineEndRadians) *
          0.5d *
          textWidth;
        textPositionY = textPositionY + textHeight;
      } else {
        textPositionX = textPositionX - textWidth;
        textPositionY = textPositionY + 0.5d * textHeight;
      }
    }

    double compensate = 0.0d;

    if (cgview.getDrawEntirePlasmid()) {
      compensate = 0.0d;
    }

    placedBounds.setRect(
      unplacedBounds.getX() + textPositionX - padding - compensate,
      unplacedBounds.getY() + textPositionY - descent - padding - compensate,
      unplacedBounds.getWidth() + 2.0d * padding,
      unplacedBounds.getHeight() + 2.0d * padding
    );
    // placedBounds.setRect(unplacedBounds.getX()+textPositionX, unplacedBounds.getY() +
    // textPositionY - descent, unplacedBounds.getWidth(), unplacedBounds.getHeight());

  }

  /**
   * Attempts to move this Label by increasing its radius value. If this Label cannot be moved
   * without introducing a new conflict for space, it is not moved.
   *
   * @return a <code>boolean</code> specifing whether or not this Label was moved.
   */
  protected boolean extendRadius() {
    if (extendedRadius) {
      extendedLineEndRadius = extendedLineEndRadius - radiusShiftAmount;
      placedBounds.setRect(
        placedBounds.getX() - Math.cos(lineEndRadians) * radiusShiftAmount,
        placedBounds.getY() - Math.sin(lineEndRadians) * radiusShiftAmount,
        placedBounds.getWidth(),
        placedBounds.getHeight()
      );
      if (fitsInBackground()) {
        return true;
      } else {
        extendedLineEndRadius = extendedLineEndRadius + radiusShiftAmount;
        updateBounds();
        return false;
      }
    } else {
      extendedRadius = true;
      extendedLineStartRadius = lineEndRadius;
      extendedLineEndRadius = lineEndRadius - radiusShiftAmount;
      placedBounds.setRect(
        placedBounds.getX() - Math.cos(lineEndRadians) * radiusShiftAmount,
        placedBounds.getY() - Math.sin(lineEndRadians) * radiusShiftAmount,
        placedBounds.getWidth(),
        placedBounds.getHeight()
      );
      return true;
    }
  }

  /**
   * Specifies radius of the innermost point in the line extending from the feature to this Label.
   *
   * @param lineStartRadius the radius of the innermost point in the line extending from the feature
   *     to this Label.
   */
  protected void setLineStartRadius(double lineStartRadius) {
    this.lineStartRadius = lineStartRadius;
    lineEndRadius = lineStartRadius - cgview.getLabelLineLength();
    updateBounds();
  }

  /**
   * Returns a boolean specifying whether or not this Label can fit inside of the map canvas.
   *
   * @return a <code>boolean</code> specifying whether or not this Label can fit inside of the map
   *     canvas.
   */
  protected boolean fitsInBackground() {
    if (cgview.getDrawEntirePlasmid()) {
      return (
        (cgview.getBackgroundRectangle().contains(this.getBounds())) &&
        !(cgview.getTitleRectangle().intersects(this.getBounds())) &&
        !(cgview.getLengthRectangle().intersects(this.getBounds()))
      );
    } else {
      return cgview.getBackgroundRectangle().contains(this.getBounds());
    }
  }
}
