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
import java.util.regex.*;

/**
 * This class is used to add text entries to Legend objects.
 *
 * @author Paul Stothard
 */
public class LegendItem implements CgviewConstants {
  private Color fontColor;
  private Color swatchColor = new Color(0, 0, 0); // black
  private String label;
  private Legend legend;
  private float swatchOpacity = 1.0f;
  private int drawSwatch = SWATCH_NO_SHOW;
  private Font font;
  private int textAlignment;

  /**
   * Constructs a new LegendItem object.
   *
   * @param legend the Legend object to contain this LegendItem.
   */
  public LegendItem(Legend legend) {
    this.legend = legend;
    fontColor = legend.getFontColor();
    textAlignment = legend.getAlignment();
    font = legend.getFont();
    legend.addLegendItem(this);
  }

  /**
   * Draws the contents of this LegendItem.
   *
   * @param cgview the Cgview object that contains this LegendItem.
   * @param x the x-coordinate for the upper left corner of this LegendItem.
   * @param y the y-coordinate for the upper left corner of this LegendItem.
   * @param swatchHeight the swatch height for this LegendItem.
   */
  protected void draw(Cgview cgview, double x, double y, double swatchHeight) {
    if (this.label == null) {
      this.label = "Untitled";
    }

    Pattern p = Pattern.compile("\\S");
    Matcher m = p.matcher(this.label);

    if (!(m.find())) {
      this.label = "Untitled";
    }

    Graphics2D gg = cgview.getGraphics();
    FontRenderContext frc = gg.getFontRenderContext();
    TextLayout layout = new TextLayout(this.label, this.font, frc);
    Rectangle2D bounds = layout.getBounds();
    double textWidth = bounds.getWidth();
    double textHeight = bounds.getHeight();

    double textPositionX = x;
    // the below adjustment accounts for text starting slightly right of draw point. Amount is
    // proportional to fontsize
    textPositionX = textPositionX - textHeight / 12.0d;

    double textPositionY = y;
    double internalLegendWidth =
      legend.getBounds().getWidth() - 2.0d * Legend.PADDING;

    if (swatchHeight > 0.0d) {
      // Rectangle2D swatchRectangle = new Rectangle2D.Double(x, y - height / 7.0d, height, height);
      Rectangle2D swatchRectangle = new Rectangle2D.Double(
        x,
        y - swatchHeight,
        swatchHeight,
        swatchHeight
      );
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, swatchOpacity)
      );
      gg.setPaint(swatchColor);
      gg.fill(swatchRectangle);
      gg.setComposite(
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)
      );

      internalLegendWidth =
        internalLegendWidth - swatchHeight - swatchHeight / 2.0d;
      textPositionX = x + swatchHeight + swatchHeight / 2.0d;
    }

    // adjust textAlignment of text within the legend
    if (this.textAlignment == LEGEND_ITEM_ALIGN_RIGHT) {
      textPositionX =
        textPositionX + internalLegendWidth - textWidth - textHeight / 12.0d;
    } else if (this.textAlignment == LEGEND_ITEM_ALIGN_CENTER) {
      textPositionX =
        textPositionX + internalLegendWidth / 2.0d - textWidth / 2.0d;
    }

    gg.setPaint(fontColor);
    layout.draw(
      gg,
      (float) textPositionX,
      (float) textPositionY - layout.getDescent()
    );
    // layout.draw(gg, (float)textPositionX, (float)textPositionY);

  }

  /**
   * Returns the width of the text in this LegendItem.
   *
   * @param cgview the Cgview object that contains this LegendItem.
   */
  protected double getWidth(Cgview cgview, double swatchWidth) {
    double width = 0.0d;

    Pattern p = Pattern.compile("\\S");
    Matcher m = p.matcher(this.label);

    if (!(m.find())) {
      this.label = "Untitled";
    }

    Graphics2D gg = cgview.getGraphics();
    FontRenderContext frc = gg.getFontRenderContext();
    TextLayout layout = new TextLayout(this.label, this.font, frc);
    Rectangle2D bounds = layout.getBounds();

    width = bounds.getWidth();

    width = width + swatchWidth + swatchWidth / 2.0d;

    return width;
  }

  /**
   * Returns the height of the text in this LegendItem.
   *
   * @param cgview the cgview object to draw on.
   */
  protected double getHeight(Cgview cgview) {
    Pattern p = Pattern.compile("\\S");
    Matcher m = p.matcher(this.label);

    if (!(m.find())) {
      this.label = "Untitled";
    }

    Graphics2D gg = cgview.getGraphics();
    FontRenderContext frc = gg.getFontRenderContext();
    TextLayout layout = new TextLayout(this.label, this.font, frc);
    // Rectangle2D bounds = layout.getBounds();

    // return bounds.getHeight();
    return layout.getDescent() + layout.getAscent();
  }

  /**
   * Sets the color that will be used for the text in this LegendItem.
   *
   * @param fontColor the color of the text in this LegendItem.
   */
  public void setFontColor(Color fontColor) {
    this.fontColor = fontColor;
  }

  /**
   * Returns the color that will be used for the text in this LegendItem.
   *
   * @return the color of the text in this LegendItem.
   */
  public Color getFontColor() {
    return fontColor;
  }

  /**
   * Sets the swatch color that will be used for this LegendItem.
   *
   * @param swatchColor the swatch color for this LegendItem.
   */
  public void setSwatchColor(Color swatchColor) {
    this.swatchColor = swatchColor;
  }

  /**
   * Returns the swatch color that will be used for this LegendItem.
   *
   * @return the swatch color for this LegendItem.
   */
  public Color getSwatchColor() {
    return swatchColor;
  }

  /**
   * Sets whether or not a color swatch should be drawn for this LegendItem.
   *
   * @param drawSwatch {@link CgviewConstants#SWATCH_SHOW CgviewConstants.SWATCH_SHOW} or {@link
   *     CgviewConstants#SWATCH_NO_SHOW CgviewConstants.SWATCH_NO_SHOW}.
   */
  public void setDrawSwatch(int drawSwatch) {
    this.drawSwatch = drawSwatch;
  }

  /**
   * Returns whether or not a color swatch should be drawn for this LegendItem.
   *
   * @return {@link CgviewConstants#SWATCH_SHOW CgviewConstants.SWATCH_SHOW} or {@link
   *     CgviewConstants#SWATCH_NO_SHOW CgviewConstants.SWATCH_NO_SHOW}.
   */
  public int getDrawSwatch() {
    return drawSwatch;
  }

  /**
   * Sets the text to be used for this LegendItem.
   *
   * @param label the text for this LegendItem.
   */
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   * Returns the text to be used for this LegendItem.
   *
   * @return the text for this LegendItem.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Sets the opacity of the swatch drawn for this LegendItem.
   *
   * @param swatchOpacity the opacity between <code>0</code> and <code>1</code>, with <code>1</code>
   *     being the most opaque.
   */
  public void setSwatchOpacity(float swatchOpacity) {
    if (swatchOpacity < 0) {
      swatchOpacity = 0.0f;
    } else if (swatchOpacity > 1) {
      swatchOpacity = 1.0f;
    }
    this.swatchOpacity = swatchOpacity;
  }

  /**
   * Returns the opacity of the swatch drawn for this LegendItem.
   *
   * @return the swatchOpacity between <code>0</code> and <code>1</code>, with <code>1</code> being
   *     the most opaque.
   */
  public float getSwatchOpacity() {
    return swatchOpacity;
  }

  /**
   * Sets the font used for this LegendItem.
   *
   * @param font the font used for this LegendItem.
   */
  public void setFont(Font font) {
    this.font = font;
  }

  /**
   * Returns the font used for this LegendItem.
   *
   * @return the font used for this legendItem.
   */
  public Font getFont() {
    return font;
  }

  /**
   * Sets the alignment of this LegendItem, relative to the Legend object that contains it.
   *
   * @param textAlignment {@link CgviewConstants#LEGEND_ITEM_ALIGN_LEFT
   *     CgviewConstants.LEGEND_ITEM_ALIGN_LEFT}, {@link CgviewConstants#LEGEND_ITEM_ALIGN_CENTER
   *     CgviewConstants.LEGEND_ITEM_ALIGN_CENTER}, or {@link
   *     CgviewConstants#LEGEND_ITEM_ALIGN_RIGHT CgviewConstants.LEGEND_ITEM_ALIGN_RIGHT}.
   */
  public void setTextAlignment(int textAlignment) {
    this.textAlignment = textAlignment;
  }

  /**
   * Returns the alignment of this legendItem, relative to the Legend object that contains it.
   *
   * @return {@link CgviewConstants#LEGEND_ITEM_ALIGN_LEFT CgviewConstants.LEGEND_ITEM_ALIGN_LEFT},
   *     {@link CgviewConstants#LEGEND_ITEM_ALIGN_CENTER CgviewConstants.LEGEND_ITEM_ALIGN_CENTER},
   *     or {@link CgviewConstants#LEGEND_ITEM_ALIGN_RIGHT CgviewConstants.LEGEND_ITEM_ALIGN_RIGHT}.
   */
  public int getTextAlignment() {
    return textAlignment;
  }
}
