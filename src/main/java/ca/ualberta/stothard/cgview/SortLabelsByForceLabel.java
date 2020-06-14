package ca.ualberta.stothard.cgview;

import java.util.*;

/**
 * Sorts Label objects so that those where {@link Label#getForceLabel()} returns <code>true</code>
 * are last.
 */
public class SortLabelsByForceLabel implements Comparator {

  public int compare(Object o1, Object o2) {
    Label label1 = (Label) o1;
    Label label2 = (Label) o2;

    if (label1.getForceLabel() == label2.getForceLabel()) {
      return 0;
    } else if (label1.getForceLabel()) {
      return 1;
    } else {
      return -1;
    }
  }

  public boolean equals(Object obj) {
    return obj.equals(this);
  }
}
