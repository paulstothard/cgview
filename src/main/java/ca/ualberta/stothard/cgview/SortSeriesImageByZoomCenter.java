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
