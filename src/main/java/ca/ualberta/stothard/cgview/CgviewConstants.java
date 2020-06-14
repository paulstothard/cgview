package ca.ualberta.stothard.cgview;

/**
 * The <code>CgviewConstants</code> interface provides constants for use in the
 * ca.ualberta.stothard.cgview package.
 *
 * @author Paul Stothard
 */
public interface CgviewConstants {
  /** Represents the direct strand. */
  public static final int DIRECT_STRAND = 1;

  /** Represents the reverse strand. */
  public static final int REVERSE_STRAND = 2;

  /** Indicates that no labels are to be created. */
  public static final int LABEL_NONE = 1;

  /** Indicates that labels are to be created. */
  public static final int LABEL = 2;

  /** Indicates that a label is to be drawn even if there is insufficient space. */
  public static final int LABEL_FORCE = 3;

  /** Indicates that labels are to be drawn only when a zoomed map is generated. */
  public static final int LABEL_ZOOMED = 4;

  /** Indicates that labels should be shown with position information. */
  public static final int POSITIONS_SHOW = 1;

  /** Indicates that labels should not be shown with position information. */
  public static final int POSITIONS_NO_SHOW = 2;

  /**
   * Indicates that labels should be shown with position information only when a zoomed map is
   * generated.
   */
  public static final int POSITIONS_AUTO = 3;

  /**
   * Indicates that labels should be drawn on the inside of the backbone if they are in a
   * reverse-strand FeatureSlot.
   */
  public static final int INNER_LABELS_SHOW = 1;

  /** Indicates that all labels should be drawn on the outside of the backbone. */
  public static final int INNER_LABELS_NO_SHOW = 2;

  /**
   * Indicates that labels should be shown be drawn on the inside of the backbone if they are in a
   * reverse-strand FeatureSlot and a zoomed map is generated.
   */
  public static final int INNER_LABELS_AUTO = 3;

  /** Indicates that the Cgview sequence ruler should be shown in base pair units. */
  public static final int BASES = 1;

  /** Indicates that the Cgview sequence ruler should be shown in centisome units. */
  public static final int CENTISOMES = 2;

  /** Indicates that the Feature or FeatureRange should be drawn as a simple arc. */
  public static final int DECORATION_STANDARD = 1;

  /**
   * Indicates that the Feature or FeatureRange should be drawn as an arc with an arrowhead at the
   * beginning.
   */
  public static final int DECORATION_COUNTERCLOCKWISE_ARROW = 2;

  /**
   * Indicates that the Feature or FeatureRange should be drawn as an arc with an arrowhead at the
   * end.
   */
  public static final int DECORATION_CLOCKWISE_ARROW = 3;

  /**
   * Indicates that the Feature or FeatureRange should not be drawn. This setting is typically used
   * when only a label is needed (a restriction site for example).
   */
  public static final int DECORATION_HIDDEN = 4;

  /** Indicates that the LegendItem should be drawn with a color swatch. */
  public static final int SWATCH_SHOW = 1;

  /** Indicates that the LegendItem should be drawn without a color swatch. */
  public static final int SWATCH_NO_SHOW = 2;

  /** Indicates that the Legend should be drawn in the upper left portion of the map. */
  public static final int LEGEND_UPPER_LEFT = 1;

  /** Indicates that the Legend should be drawn in the upper center portion of the map. */
  public static final int LEGEND_UPPER_CENTER = 2;

  /** Indicates that the Legend should be drawn in the upper right portion of the map. */
  public static final int LEGEND_UPPER_RIGHT = 3;

  /** Indicates that the Legend should be drawn in the middle left portion of the map. */
  public static final int LEGEND_MIDDLE_LEFT = 4;

  /** Indicates that the Legend should be drawn left of the middle left portion of the map. */
  public static final int LEGEND_MIDDLE_LEFT_OF_CENTER = 5;

  /** Indicates that the Legend should be drawn in the middle center portion of the map. */
  public static final int LEGEND_MIDDLE_CENTER = 6;

  /** Indicates that the Legend should be drawn right of the middle right portion of the map. */
  public static final int LEGEND_MIDDLE_RIGHT_OF_CENTER = 7;

  /** Indicates that the Legend should be drawn in the middle right portion of the map. */
  public static final int LEGEND_MIDDLE_RIGHT = 8;

  /** Indicates that the Legend should be drawn in the lower left portion of the map. */
  public static final int LEGEND_LOWER_LEFT = 9;

  /** Indicates that the Legend should be drawn in the lower center portion of the map. */
  public static final int LEGEND_LOWER_CENTER = 10;

  /** Indicates that the Legend should be drawn in the lower right portion of the map. */
  public static final int LEGEND_LOWER_RIGHT = 11;

  /** Indicates that the Legend should be drawn when zoomed. */
  public static final int LEGEND_DRAW_ZOOMED = 1;

  /** Indicates that the Legend should not be drawn when zoomed. */
  public static final int LEGEND_NO_DRAW_ZOOMED = 2;

  /** Indicates that the LegendItem text should be aligned to the left. */
  public static final int LEGEND_ITEM_ALIGN_LEFT = 1;

  /** Indicates that the LegendItem text should be aligned to the center. */
  public static final int LEGEND_ITEM_ALIGN_CENTER = 2;

  /** Indicates that the LegendItem text should be aligned to the right. */
  public static final int LEGEND_ITEM_ALIGN_RIGHT = 3;

  /** Indicates that the LabelBounds encloses a tick mark. */
  public static final int BOUNDS_RULER = 1;

  /** Indicates that the labelBounds encloses a feature label. */
  public static final int BOUNDS_FEATURE = 2;

  /** Indicates that the labelBounds encloses a button. */
  public static final int BOUNDS_BUTTON = 3;
}
