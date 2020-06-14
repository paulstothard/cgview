package ca.ualberta.stothard.cgview;

import java.util.*;

/**
 * Sorts Label objects so that those with smaller {@link Label#getLineStartRadians()} values are
 * first.
 */
public class SortLabelsByRadians implements Comparator {

  public int compare(Object o1, Object o2) {
    Label label1 = (Label) o1;
    Label label2 = (Label) o2;

    if (label1.getLineStartRadians() == label2.getLineStartRadians()) {
      return 0;
    } else if (label2.getLineStartRadians() < label1.getLineStartRadians()) {
      return 1;
    } else {
      return -1;
    }
  }

  public boolean equals(Object obj) {
    return obj.equals(this);
  }
}
