package ca.ualberta.stothard.cgview;

import java.awt.geom.*;

/**
 * This class is used to store bounds information about text already drawn on a Cgview map. The
 * information stored in this class can be used to implement label mouseovers and hyperlinks.
 *
 * @author Paul Stothard
 */
public class LabelBounds implements CgviewConstants {
  private String labelText;
  private String hyperlink;
  private String mouseover;
  private Rectangle2D bounds;
  private int type;
  private boolean use;
  private int base;
  private Cgview cgview;

  /**
   * Constructs a new LabelBounds object.
   *
   * @param cgview the Cgview object to contain this LabelBounds.
   */
  protected LabelBounds(Cgview cgview) {
    this.cgview = cgview;
    cgview.getLabelBounds().add(this);
  }

  /**
   * Specifies the bounds of this LabelBounds.
   *
   * @param bounds the bounds of the label.
   */
  protected void setBounds(Rectangle2D bounds) {
    this.bounds = bounds;
  }

  /**
   * Returns a rectangle that represents the bounds of this LabelBounds.
   *
   * @return the bounds of the label.
   */
  public Rectangle2D getBounds() {
    return bounds;
  }

  /**
   * Specifies the text that gave rise to this LabelBounds.
   *
   * @param label the text that gave rise to this LabelBounds.
   */
  protected void setLabel(String label) {
    this.labelText = label;
  }

  /**
   * Returns the text that gave rise to this LabelBounds.
   *
   * @return the text gave rise to this LabelBounds.
   */
  public String getLabel() {
    return labelText;
  }

  /**
   * Specifies a hyperlink to be associated with this LabelBounds.
   *
   * @param hyperlink a hyperlink to be associated with this LabelBounds.
   */
  protected void setHyperlink(String hyperlink) {
    this.hyperlink = hyperlink;
  }

  /**
   * Returns the hyperlink associated with this LabelBounds.
   *
   * @return the hyperlink associated with this LabelBounds.
   */
  public String getHyperlink() {
    return hyperlink;
  }

  /**
   * Specifies a mouseover to be associated with this LabelBounds.
   *
   * @param mouseover a mouseover to be associated with this LabelBounds.
   */
  protected void setMouseover(String mouseover) {
    this.mouseover = mouseover;
  }

  /**
   * Returns the mouseover associated with this LabelBounds.
   *
   * @return the mouseover associated with this LabelBounds.
   */
  public String getMouseover() {
    return mouseover;
  }

  /**
   * Sets whether or not this LabelBounds should be used.
   *
   * @param use whether or not this LabelBounds should be used.
   */
  public void setUse(boolean use) {
    this.use = use;
  }

  /**
   * Returns whether or not this LabelBounds should be used.
   *
   * @return whether or not this LabelBounds should be used.
   */
  public boolean getUse() {
    return use;
  }

  /**
   * Specifies what map object this LabelBounds represents.
   *
   * @param type {@link CgviewConstants#BOUNDS_RULER CgviewConstants.BOUNDS_RULER}, {@link
   *     CgviewConstants#BOUNDS_FEATURE CgviewConstants.BOUNDS_FEATURE}, of {@link
   *     CgviewConstants#BOUNDS_BUTTON CgviewConstants.BOUNDS_BUTTON}.
   */
  protected void setType(int type) {
    this.type = type;
  }

  /**
   * Returns the type of this LabelBounds.
   *
   * @return {@link CgviewConstants#BOUNDS_RULER CgviewConstants.BOUNDS_RULER}, {@link
   *     CgviewConstants#BOUNDS_FEATURE CgviewConstants.BOUNDS_FEATURE}, of {@link
   *     CgviewConstants#BOUNDS_BUTTON CgviewConstants.BOUNDS_BUTTON}.
   */
  public int getType() {
    return type;
  }

  /**
   * Specifies which sequence position this LabelBounds is closest to.
   *
   * @param base the sequence position.
   */
  protected void setBase(int base) {
    this.base = base;
  }

  /**
   * Returns the sequence position closest to this LabelBounds.
   *
   * @return the sequence position closest to this LabelBounds.
   */
  public int getBase() {
    return base;
  }
}
