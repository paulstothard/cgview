package ca.ualberta.stothard.cgview;

import java.util.*;

/**
 * This class is used to encapsulate Cgview map information when a series of images is linked
 * together.
 *
 * @author Paul Stothard
 */
public class SeriesImage implements CgviewConstants {
  int zoomValue;
  int zoomCenter;

  /**
   * Constructs a SeriesImage object.
   *
   * @param zoomValue the zoom value of the Cgview map.
   * @param zoomCenter the zoom center value of the Cgview map.
   */
  public SeriesImage(int zoomValue, int zoomCenter) {
    this.zoomValue = zoomValue;
    this.zoomCenter = zoomCenter;
  }

  /**
   * Returns the zoom value of this SeriesImage.
   *
   * @return the zoom value.
   */
  public int getZoomValue() {
    return zoomValue;
  }

  /**
   * Returns the zoom center value of this SeriesImage.
   *
   * @return the zoom center value.
   */
  public int getZoomCenter() {
    return zoomCenter;
  }

  /**
   * Compares a SeriesImage object to this SeriesImage and returns true if they are equal.
   *
   * @param seriesImage the SeriesImage to compare with this SeriesImage.
   * @return whether or not the SeriesImage objects are equal.
   */
  public boolean isEqual(SeriesImage seriesImage) {
    if (
      (seriesImage.getZoomValue() == zoomValue) &&
      (seriesImage.getZoomCenter() == zoomCenter)
    ) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Examines the ArrayList of LabelBounds objects of type {@link CgviewConstants#BOUNDS_RULER} and
   * determines which is the middle one. A string intended to serve as part of a file name is
   * constructed from the middle item's zoom value and zoom center value.
   *
   * @param labelBounds the collection of LabelBounds objects to examine.
   * @param nextZoomValue the zoom value of the next zoomed Cgview map.
   * @return a file name prefix consisting of the next zoom value and the zoom center value from the
   *     middle LabelBounds entry of type {@link CgviewConstants#BOUNDS_RULER}.
   */
  protected String getZoomInFilePrefix(
    ArrayList labelBounds,
    int nextZoomValue
  ) {
    if (labelBounds.size() == 0) {
      return null;
    } else {
      // if seriesImage is centered on base 1, it should zoom to the next base 1 image
      if (this.zoomCenter == 1) {
        return Integer.toString(nextZoomValue) + "_" + Integer.toString(1);
      }

      // need to determine middle RULER_BOUNDS entry
      ArrayList rulerBounds = new ArrayList();
      LabelBounds currentLabelBounds;
      for (int k = 0; k < labelBounds.size(); k++) {
        currentLabelBounds = (LabelBounds) labelBounds.get(k);
        if (currentLabelBounds.getType() == BOUNDS_RULER) {
          rulerBounds.add(currentLabelBounds);
        }
      }
      if (rulerBounds.size() == 0) {
        return null;
      } else {
        // go through each rulerBounds and find one with closest base value to this.
        int closestDistance = Math.abs(
          ((LabelBounds) rulerBounds.get(0)).getBase() - this.zoomCenter
        );
        int closestBase = ((LabelBounds) rulerBounds.get(0)).getBase();

        for (int k = 0; k < rulerBounds.size(); k++) {
          currentLabelBounds = (LabelBounds) rulerBounds.get(k);
          if (
            Math.abs(currentLabelBounds.getBase() - this.zoomCenter) <
            closestDistance
          ) {
            closestDistance =
              Math.abs(currentLabelBounds.getBase() - this.zoomCenter);
            closestBase = currentLabelBounds.getBase();
          }
        }
        return (
          Integer.toString(nextZoomValue) + "_" + Integer.toString(closestBase)
        );
      }
    }
  }

  /**
   * Determines which SeriesImage should be linked to this SeriesImage using a zoom out button.
   *
   * @param previousSeriesImages a collection of SeriesImage objects.
   * @param previousZoom the zoom value used for the previous set of Cgview images.
   * @return a file name prefix to be used for linking purposes.
   */
  protected String getZoomOutFilePrefix(
    ArrayList previousSeriesImages,
    int previousZoom
  ) {
    int bestDistance = 0;
    int bestDistanceZoomCenter = 0;

    SeriesImage currentSeriesImage;

    if (previousSeriesImages.size() == 0) {
      return null;
    } else {
      // if seriesImage is centered on base 1, it should zoom out to the previous base 1 image
      if (this.zoomCenter == 1) {
        return Integer.toString(previousZoom) + "_" + Integer.toString(1);
      }

      // find previous SeriesImage with zoomCenter closest to this one.
      for (int k = 0; k < previousSeriesImages.size(); k++) {
        currentSeriesImage = (SeriesImage) previousSeriesImages.get(k);
        if (k == 0) {
          bestDistance =
            Math.abs(currentSeriesImage.getZoomCenter() - this.getZoomCenter());
          bestDistanceZoomCenter = currentSeriesImage.getZoomCenter();
          previousZoom = currentSeriesImage.getZoomValue();
        } else {
          if (
            Math.abs(
              currentSeriesImage.getZoomCenter() - this.getZoomCenter()
            ) <
            bestDistance
          ) {
            bestDistance =
              Math.abs(
                currentSeriesImage.getZoomCenter() - this.getZoomCenter()
              );
            bestDistanceZoomCenter = currentSeriesImage.getZoomCenter();
          }
        }
      }
    }
    return (
      Integer.toString(previousZoom) +
      "_" +
      Integer.toString(bestDistanceZoomCenter)
    );
  }

  /**
   * Determines which SeriesImage is adjacent to this SeriesImage in the clockwise direction.
   *
   * @param toDrawCurrentZoom a collection of SeriesImages drawn at the same zoom level as this
   *     SeriesImage.
   * @return a file name prefix to be used for linking purposes.
   */
  public String getClockwiseFilePrefix(ArrayList toDrawCurrentZoom) {
    if (toDrawCurrentZoom.size() <= 1) {
      return (
        Integer.toString(this.zoomValue) +
        "_" +
        Integer.toString(this.zoomCenter)
      );
    } else {
      // get the index of this object
      int index = toDrawCurrentZoom.indexOf(this);
      SeriesImage imageToLink;
      if (index == toDrawCurrentZoom.size() - 1) {
        imageToLink = (SeriesImage) toDrawCurrentZoom.get(0);
      } else {
        imageToLink = (SeriesImage) toDrawCurrentZoom.get(index + 1);
      }

      return (
        Integer.toString(imageToLink.getZoomValue()) +
        "_" +
        Integer.toString(imageToLink.getZoomCenter())
      );
    }
  }

  /**
   * Determines which SeriesImage is adjacent to this SeriesImage in the counterclockwise direction.
   *
   * @param toDrawCurrentZoom a collection of SeriesImages drawn at the same zoom level as this
   *     SeriesImage.
   * @return a file name prefix to be used for linking purposes.
   */
  public String getCounterclockwiseFilePrefix(ArrayList toDrawCurrentZoom) {
    if (toDrawCurrentZoom.size() <= 1) {
      return (
        Integer.toString(this.zoomValue) +
        "_" +
        Integer.toString(this.zoomCenter)
      );
    } else {
      // get the index of this object
      int index = toDrawCurrentZoom.indexOf(this);
      SeriesImage imageToLink;
      if (index == 0) {
        imageToLink =
          (SeriesImage) toDrawCurrentZoom.get(toDrawCurrentZoom.size() - 1);
      } else {
        imageToLink = (SeriesImage) toDrawCurrentZoom.get(index - 1);
      }

      return (
        Integer.toString(imageToLink.getZoomValue()) +
        "_" +
        Integer.toString(imageToLink.getZoomCenter())
      );
    }
  }
}
