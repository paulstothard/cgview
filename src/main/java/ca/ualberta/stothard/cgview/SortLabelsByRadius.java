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
