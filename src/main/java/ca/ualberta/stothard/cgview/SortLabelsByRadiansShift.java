package ca.ualberta.stothard.cgview;

import java.util.*;

/**
 * Sorts Label objects so that those that those with the smallest absolute difference between {@link
 * Label#getLineStartRadians()} and {@link Label#getLineEndRadians()} are first.
 */
public class SortLabelsByRadiansShift implements Comparator {

  public int compare(Object o1, Object o2) {
    Label label1 = (Label) o1;
    Label label2 = (Label) o2;

    if (
      Math.abs(label1.getLineStartRadians() - label1.getLineEndRadians()) ==
      Math.abs(label2.getLineStartRadians() - label2.getLineEndRadians())
    ) {
      return 0;
    } else if (
      Math.abs(label1.getLineStartRadians() - label1.getLineEndRadians()) <
      Math.abs(label2.getLineStartRadians() - label2.getLineEndRadians())
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
