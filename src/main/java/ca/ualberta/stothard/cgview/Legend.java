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
import java.util.*;

/**
 * This class is used add legends to a Cgview map. Individual legend entries are represented using
 * the {@link LegendItem} class.
 *
 * @author Paul Stothard
 */
public class Legend implements CgviewConstants {
  private Font font;
  private Color fontColor;
  private Color backgroundColor;
  private float backgroundOpacity = 1.0f;
  private ArrayList legendItems = new ArrayList();
  private Cgview cgview;
  private int drawWhenZoomed = LEGEND_DRAW_ZOOMED;
  private int position = LEGEND_UPPER_RIGHT;
  private Rectangle2D bounds;
  private double swatchHeight;
  private int textAlignment = LEGEND_ITEM_ALIGN_LEFT;
  private boolean allowLabelClash = false;

  // gives padding to legend
  protected static final double PADDING = 5.0d;

  /**
   * Constructs a new Legend object.
   *
   * @param cgview the Cgview object to contain this Legend.
   */
  public Legend(Cgview cgview) {
    this.cgview = cgview;
    font = cgview.getLegendFont();
    fontColor = cgview.getLegendTextColor();
    ArrayList legends = cgview.getLegends();
    legends.add(this);
  }

  /**
   * Adds a LegendItem object to this Legend.
   *
   * @param legendItem the LegendItem object to add to this Legend.
   */
  protected void addLegendItem(LegendItem legendItem) {
    legendItems.add(legendItem);
  }

  /** Draws the contents of this Legend. */
  protected void draw() {
    if (bounds == null) {
      this.setBounds();
    }

    if (
      (
        (cgview.getZoomMultiplier() == 1.0d) ||
        (drawWhenZoomed == LEGEND_DRAW_ZOOMED)
      ) &&
      (legendItems.size() > 0) &&
      (bounds.getHeight() <= cgview.getHeight()) &&
      (bounds.getWidth() <= cgview.getWidth())
    ) {
      Graphics2D gg = cgview.getGraphics();

      if (this.backgroundColor == null) {
        gg.setPaint(cgview.getBackgroundColor());
      } else {
        gg.setPaint(this.backgroundColor);
      }

      gg.setComposite(
        AlphaComposite.getInstance(
          AlphaComposite.SRC_OVER,
          this.backgroundOpacity
        )
      );

      gg.fill(bounds);
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
      );

      // go through legendItems and draw them.
      // space items with swatches according to swatch height
      Collections.reverse(legendItems);
      double startX = bounds.getX() + PADDING;
      double startY = bounds.getY() + bounds.getHeight() - PADDING;

      Iterator i;
      i = legendItems.iterator();
      double height;
      while (i.hasNext()) {
        LegendItem currentLegendItem = (LegendItem) i.next();
        height = currentLegendItem.getHeight(cgview);

        if (currentLegendItem.getDrawSwatch() == SWATCH_SHOW) {
          currentLegendItem.draw(cgview, startX, startY, swatchHeight);
          startY = startY - swatchHeight - swatchHeight / 2.0d;
        } else {
          currentLegendItem.draw(cgview, startX, startY, 0.0d);
          startY = startY - height - height / 2.0d;
        }
      }

      Collections.reverse(legendItems);
    } else if (
      (bounds.getHeight() > cgview.getHeight()) ||
      (bounds.getWidth() > cgview.getWidth())
    ) {
      System.err.println(
        "[warning] a legend was removed because it is too large for the canvas."
      );
      this.allowLabelClash = true;
    }

    bounds = null;
  }

  /** Returns the bounds of this Legend. */
  protected Rectangle2D getBounds() {
    if (bounds == null) {
      this.setBounds();
    }
    return bounds;
  }

  /** Calculates the bounds of this Legend. */
  protected void setBounds() {
    setBounds(cgview.getCenter().getX(), cgview.getCenter().getY());
  }

  /** Calculates the bounds of this Legend. */
  protected void setBounds(double cgviewCenterX, double cgviewCenterY) {
    double cgviewWidth = cgview.getWidth();
    double cgviewHeight = cgview.getHeight();

    // if (cgviewWidth > cgviewHeight) {
    //    cgviewCenterX = cgviewCenterX + (cgviewWidth - cgviewHeight) / 2.0d;
    //
    // } else if (cgviewHeight > cgviewWidth) {
    //    cgviewCenterY = cgviewCenterY + (cgviewHeight - cgviewWidth) / 2.0d;
    // }

    swatchHeight = this.getSwatchHeight();
    double widestItem = this.getWidestLegendItem(swatchHeight);

    double legendWidth = widestItem;
    double legendHeight = 0.0d;

    Iterator i;
    double height = 0;
    i = legendItems.iterator();
    boolean first = true;
    while (i.hasNext()) {
      LegendItem currentLegendItem = (LegendItem) i.next();
      height = currentLegendItem.getHeight(cgview);
      if (currentLegendItem.getDrawSwatch() == SWATCH_SHOW) {
        legendHeight = legendHeight + swatchHeight;
        if (!(first)) {
          legendHeight = legendHeight + swatchHeight / 2.0d;
        }
      } else {
        legendHeight = legendHeight + height;
        if (!(first)) {
          legendHeight = legendHeight + height / 2.0d;
        }
      }
      first = false;
    }

    legendHeight = legendHeight + 2.0d * PADDING;

    double upperX = 0.0d;
    double upperY = 0.0d;

    // adjust x and y using PADDING

    if (position == LEGEND_UPPER_LEFT) {
      upperX = cgviewCenterX - cgviewWidth / 2 + PADDING;
      upperY = cgviewCenterY - cgviewHeight / 2 + PADDING;
    } else if (position == LEGEND_UPPER_CENTER) {
      upperX = cgviewCenterX - legendWidth / 2;
      upperY = cgviewCenterY - cgviewHeight / 2 + PADDING;
    } else if (position == LEGEND_UPPER_RIGHT) {
      upperX = cgviewCenterX + cgviewWidth / 2 - legendWidth - PADDING;
      upperY = cgviewCenterY - cgviewHeight / 2 + PADDING;
    } else if (position == LEGEND_MIDDLE_LEFT) {
      upperX = cgviewCenterX - cgviewWidth / 2 + PADDING;
      upperY = cgviewCenterY - legendHeight / 2;
    } else if (position == LEGEND_MIDDLE_LEFT_OF_CENTER) {
      upperX = cgviewCenterX - legendWidth;
      upperY = cgviewCenterY - legendHeight / 2;
    } else if (position == LEGEND_MIDDLE_CENTER) {
      upperX = cgviewCenterX - legendWidth / 2;
      upperY = cgviewCenterY - legendHeight / 2;
    } else if (position == LEGEND_MIDDLE_RIGHT_OF_CENTER) {
      upperX = cgviewCenterX;
      upperY = cgviewCenterY - legendHeight / 2;
    } else if (position == LEGEND_MIDDLE_RIGHT) {
      upperX = cgviewCenterX + cgviewWidth / 2 - legendWidth - PADDING;
      upperY = cgviewCenterY - legendHeight / 2;
    } else if (position == LEGEND_LOWER_LEFT) {
      upperX = cgviewCenterX - cgviewWidth / 2 + PADDING;
      upperY = cgviewCenterY + cgviewHeight / 2 - legendHeight - PADDING;
    } else if (position == LEGEND_LOWER_CENTER) {
      upperX = cgviewCenterX - legendWidth / 2;
      upperY = cgviewCenterY + cgviewHeight / 2 - legendHeight - PADDING;
    } else if (position == LEGEND_LOWER_RIGHT) {
      upperX = cgviewCenterX + cgviewWidth / 2 - legendWidth - PADDING;
      upperY = cgviewCenterY + cgviewHeight / 2 - legendHeight - PADDING;
    } else { // (position == LEGEND_MIDDLE_RIGHT) {
      upperX = cgviewCenterX + cgviewWidth / 2 - legendWidth - PADDING;
      upperY = cgviewCenterY - legendHeight / 2;
    }

    bounds = new Rectangle2D.Double(upperX, upperY, legendWidth, legendHeight);
  }

  /** Returns the width of the widest LegendItem in this Legend. */
  protected double getWidestLegendItem(double swatchWidth) {
    Iterator i;
    double widest = 0.0d;
    double width;

    i = legendItems.iterator();
    while (i.hasNext()) {
      LegendItem currentLegendItem = (LegendItem) i.next();
      if (currentLegendItem.getDrawSwatch() == SWATCH_SHOW) {
        width = currentLegendItem.getWidth(cgview, swatchWidth);
      } else {
        width = currentLegendItem.getWidth(cgview, 0.0d);
      }
      if (width > widest) {
        widest = width;
      }
    }
    return widest + 2.0d * PADDING;
  }

  /** Returns the height that is suitable for any LegendItem swatches drawn in this Legend. */
  protected double getSwatchHeight() {
    Iterator i;
    double swatchHeight = 0.0d;
    double height = 0.0d;
    i = legendItems.iterator();
    while (i.hasNext()) {
      LegendItem currentLegendItem = (LegendItem) i.next();
      height = currentLegendItem.getHeight(cgview);
      if (currentLegendItem.getDrawSwatch() == SWATCH_SHOW) {
        if (height > swatchHeight) {
          swatchHeight = height;
        }
      }
    }
    return swatchHeight;
  }

  /**
   * Sets the default color that will be used for the text in this Legend. This color can be changed
   * for individual LegendItem objects using {@link LegendItem#setFontColor(Color)
   * LegendItem.setFontColor()}.
   *
   * @param fontColor the default text color for the text in this Legend.
   */
  public void setFontColor(Color fontColor) {
    this.fontColor = fontColor;
  }

  /**
   * Returns the default color that will be used for the text in this Legend. This color can be
   * changed for individual LegendItem objects using {@link LegendItem#setFontColor(Color)}.
   *
   * @return the default text color for the text in this Legend.
   */
  public Color getFontColor() {
    return fontColor;
  }

  /**
   * Sets the font used for text in this Legend. This font can be changed for individual LegendItem
   * objects using {@link LegendItem#setFont(Font)}.
   *
   * @param font the font used for text in this legend.
   */
  public void setFont(Font font) {
    this.font = font;
  }

  /**
   * Returns the font used for text in this Legend. This font can be changed for individual
   * LegendItem objects using {@link LegendItem#setFont(Font)}.
   *
   * @return the font used for text in this Legend.
   */
  public Font getFont() {
    return font;
  }

  /**
   * Sets whether or not this Legend is drawn when a zoomed Cgview map is generated.
   *
   * @param drawWhenZoomed {@link CgviewConstants#LEGEND_DRAW_ZOOMED
   *     CgviewConstants.LEGEND_DRAW_ZOOMED} or {@link CgviewConstants#LEGEND_NO_DRAW_ZOOMED
   *     CgviewConstants.LEGEND_NO_DRAW_ZOOMED}.
   */
  public void setDrawWhenZoomed(int drawWhenZoomed) {
    this.drawWhenZoomed = drawWhenZoomed;
  }

  /**
   * Returns whether or not this Legend is drawn when a zoomed Cgview map is generated.
   *
   * @return {@link CgviewConstants#LEGEND_DRAW_ZOOMED CgviewConstants.LEGEND_DRAW_ZOOMED} or {@link
   *     CgviewConstants#LEGEND_NO_DRAW_ZOOMED CgviewConstants.LEGEND_NO_DRAW_ZOOMED}.
   */
  public int getDrawWhenZoomed() {
    return drawWhenZoomed;
  }

  /**
   * Sets the position of this Legend relative to the Cgview map canvas.
   *
   * @param position {@link CgviewConstants#LEGEND_UPPER_LEFT CgviewConstants.LEGEND_UPPER_LEFT},
   *     {@link CgviewConstants#LEGEND_UPPER_CENTER CgviewConstants.LEGEND_UPPER_CENTER}, {@link
   *     CgviewConstants#LEGEND_UPPER_RIGHT CgviewConstants.LEGEND_UPPER_RIGHT}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_LEFT CgviewConstants.LEGEND_MIDDLE_LEFT}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_LEFT_OF_CENTER CgviewConstants.LEGEND_MIDDLE_LEFT_OF_CENTER},
   *     {@link CgviewConstants#LEGEND_MIDDLE_CENTER CgviewConstants.LEGEND_MIDDLE_CENTER}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_RIGHT CgviewConstants.LEGEND_MIDDLE_RIGHT}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_RIGHT_OF_CENTER
   *     CgviewConstants.LEGEND_MIDDLE_RIGHT_OF_CENTER}, {@link CgviewConstants#LEGEND_LOWER_LEFT
   *     CgviewConstants.LEGEND_LOWER_LEFT}, {@link CgviewConstants#LEGEND_LOWER_CENTER
   *     CgviewConstants.LEGEND_LOWER_CENTER}, or {@link CgviewConstants#LEGEND_LOWER_RIGHT
   *     CgviewConstants.LEGEND_LOWER_RIGHT}.
   */
  public void setPosition(int position) {
    this.position = position;
  }

  /**
   * Returns the position of this Legend relative to the Cgview map canvas.
   *
   * @return {@link CgviewConstants#LEGEND_UPPER_LEFT CgviewConstants.LEGEND_UPPER_LEFT}, {@link
   *     CgviewConstants#LEGEND_UPPER_CENTER CgviewConstants.LEGEND_UPPER_CENTER}, {@link
   *     CgviewConstants#LEGEND_UPPER_RIGHT CgviewConstants.LEGEND_UPPER_RIGHT}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_LEFT CgviewConstants.LEGEND_MIDDLE_LEFT}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_LEFT_OF_CENTER CgviewConstants.LEGEND_MIDDLE_LEFT_OF_CENTER},
   *     {@link CgviewConstants#LEGEND_MIDDLE_CENTER CgviewConstants.LEGEND_MIDDLE_CENTER}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_RIGHT CgviewConstants.LEGEND_MIDDLE_RIGHT}, {@link
   *     CgviewConstants#LEGEND_MIDDLE_RIGHT_OF_CENTER
   *     CgviewConstants.LEGEND_MIDDLE_RIGHT_OF_CENTER}, {@link CgviewConstants#LEGEND_LOWER_LEFT
   *     CgviewConstants.LEGEND_LOWER_LEFT}, {@link CgviewConstants#LEGEND_LOWER_CENTER
   *     CgviewConstants.LEGEND_LOWER_CENTER}, or {@link CgviewConstants#LEGEND_LOWER_RIGHT
   *     CgviewConstants.LEGEND_LOWER_RIGHT}.
   */
  public int getPosition() {
    return position;
  }

  /**
   * Sets the alignment of the LegendItems in this Legend.
   *
   * @param textAlignment {@link CgviewConstants#LEGEND_ITEM_ALIGN_LEFT
   *     CgviewConstants.LEGEND_ITEM_ALIGN_LEFT}, {@link CgviewConstants#LEGEND_ITEM_ALIGN_CENTER
   *     CgviewConstants.LEGEND_ITEM_ALIGN_CENTER}, or {@link
   *     CgviewConstants#LEGEND_ITEM_ALIGN_RIGHT CgviewConstants.LEGEND_ITEM_ALIGN_RIGHT}. This
   *     setting can be changed for individual legendItems using {@link
   *     LegendItem#setTextAlignment(int) LegendItem.setTextAlignment()}.
   */
  public void setAlignment(int textAlignment) {
    this.textAlignment = textAlignment;
  }

  /**
   * Returns the alignment of legendItems in this Legend.
   *
   * @return {@link CgviewConstants#LEGEND_ITEM_ALIGN_LEFT CgviewConstants.LEGEND_ITEM_ALIGN_LEFT},
   *     {@link CgviewConstants#LEGEND_ITEM_ALIGN_CENTER CgviewConstants.LEGEND_ITEM_ALIGN_CENTER},
   *     or {@link CgviewConstants#LEGEND_ITEM_ALIGN_RIGHT CgviewConstants.LEGEND_ITEM_ALIGN_RIGHT}.
   *     The alignment of legendItems can be changed for individual legendItems using {@link
   *     LegendItem#setTextAlignment(int) LegendItem.setTextAlignment()}.
   */
  public int getAlignment() {
    return textAlignment;
  }

  /**
   * Sets the opacity of the background of this Legend when drawn.
   *
   * @param opacity the opacity between <code>0</code> and <code>1</code>, with <code>1</code> being
   *     the most opaque.
   */
  public void setBackgroundOpacity(float opacity) {
    if (opacity < 0) {
      opacity = 0.0f;
    } else if (opacity > 1) {
      opacity = 1.0f;
    }
    backgroundOpacity = opacity;
  }

  /**
   * Returns the opacity of the background of this Legend when drawn.
   *
   * @return the opacity between <code>0</code> and <code>1</code>, with <code>1</code> being the
   *     most opaque.
   */
  public float getBackgroundOpacity() {
    return backgroundOpacity;
  }

  /**
   * Sets the color of the background of this Legend when drawn.
   *
   * @param color the color of the background.
   */
  public void setBackgroundColor(Color color) {
    backgroundColor = color;
  }

  /**
   * Returns the color of the background of this Legend when drawn.
   *
   * @return the color of the background.
   */
  public Color getBackgroundColor() {
    return backgroundColor;
  }

  /**
   * Sets whether or not feature labels are allowed to clash with this Legend. If false, feature
   * labels are removed if they clash with this Legend.
   *
   * @param allowClash whether or not feature labels are allowed to clash with this Legend.
   */
  public void setAllowLabelClash(boolean allowClash) {
    allowLabelClash = allowClash;
  }

  /**
   * Returns whether or not feature labels are allowed to clash with this Legend. If false, labels
   * are removed if they clash with this Legend.
   *
   * @return whether or not labels are allowed to clash with this Legend.
   */
  public boolean getAllowLabelClash() {
    return allowLabelClash;
  }
}
