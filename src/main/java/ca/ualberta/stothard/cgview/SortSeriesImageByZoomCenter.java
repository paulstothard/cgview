package ca.ualberta.stothard.cgview;

import java.util.*;

/** Sorts SeriesImage objects so that those with the lowest zoom center value are first. */
public class SortSeriesImageByZoomCenter implements Comparator {

  public int compare(Object o1, Object o2) {
    SeriesImage seriesImage1 = (SeriesImage) o1;
    SeriesImage seriesImage2 = (SeriesImage) o2;

    if (seriesImage1.getZoomCenter() == seriesImage2.getZoomCenter()) {
      return 0;
    } else if (seriesImage2.getZoomCenter() < seriesImage1.getZoomCenter()) {
      return 1;
    } else {
      return -1;
    }
  }

  public boolean equals(Object obj) {
    return obj.equals(this);
  }
}
