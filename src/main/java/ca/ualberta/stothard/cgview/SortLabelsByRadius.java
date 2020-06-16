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

/** Sorts Label objects so that those that those furthest from the sequence backbone are first. */
public class SortLabelsByRadius implements Comparator {

  public int compare(Object o1, Object o2) {
    Label label1 = (Label) o1;
    Label label2 = (Label) o2;

    double label1Radius;
    double label2Radius;

    double label1StartRadius = label1.getLineStartRadius();
    double label2StartRadius = label2.getLineStartRadius();

    if (label1.isExtendedRadius()) {
      label1Radius = label1.getExtendedLineEndRadius();
    } else {
      label1Radius = label1.getLineEndRadius();
    }

    if (label2.isExtendedRadius()) {
      label2Radius = label2.getExtendedLineEndRadius();
    } else {
      label2Radius = label2.getLineEndRadius();
    }

    if (
      Math.abs(label1Radius - label1StartRadius) ==
      Math.abs(label2Radius - label2StartRadius)
    ) {
      return 0;
    } else if (
      Math.abs(label2Radius - label2StartRadius) <
      Math.abs(label1Radius - label1StartRadius)
    ) {
      return -1;
    } else {
      return 1;
    }
  }

  public boolean equals(Object obj) {
    return obj.equals(this);
  }
}
