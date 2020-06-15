package ca.ualberta.stothard.cgview;

import ca.ualberta.stothard.cgview.*;
import java.awt.*;
import java.io.*;

public class CgviewTest1 implements CgviewConstants {

  public static void main(String args[]) {
    int length = 12078;
    Cgview cgview = new Cgview(length);

    //some optional settings
    cgview.setWidth(600);
    cgview.setHeight(600);
    cgview.setBackboneRadius(140.0f);
    cgview.setTitle("Example 2");
    cgview.setLabelPlacementQuality(5);
    cgview.setShowWarning(false);
    cgview.setLabelLineLength(15.0d);
    cgview.setLabelLineThickness(1.0f);
    cgview.setUseInnerLabels(INNER_LABELS_SHOW);
    cgview.setMoveInnerLabelsToOuter(true);
    cgview.setMinimumFeatureLength(1.0d);

    Legend legend = new Legend(cgview);
    legend.setPosition(LEGEND_UPPER_CENTER);
    LegendItem legendItem = new LegendItem(legend);
    legendItem.setLabel("An example");
    legendItem.setFont(new Font("SansSerif", Font.BOLD + Font.ITALIC, 22));

    //create FeatureSlots to hold sequence features
    FeatureSlot directSlot0 = new FeatureSlot(cgview, DIRECT_STRAND);
    FeatureSlot directSlot1 = new FeatureSlot(cgview, DIRECT_STRAND);
    FeatureSlot reverseSlot0 = new FeatureSlot(cgview, REVERSE_STRAND);

    //Features to add to the FeatureSlots
    Feature feature0 = new Feature(directSlot0, "A");
    feature0.setColor(Color.blue);

    Feature feature1 = new Feature(directSlot1, "B");
    feature1.setColor(Color.red);

    Feature feature2 = new Feature(reverseSlot0, "C");
    feature2.setColor(Color.green);

    //create random sequence features
    for (int i = 1; i <= 100; i = i + 1) {
      int j = Math.round((float) ((float) (length - 2) * Math.random())) + 1;
      int k = Math.round((float) ((float) (length - 2) * Math.random())) + 1;
      int l = Math.round((float) ((float) (length - 2) * Math.random())) + 1;

      //a single FeatureRange to add the Feature
      FeatureRange featureRange0 = new FeatureRange(feature0, j, j + 1);
      FeatureRange featureRange1 = new FeatureRange(feature1, k, k + 1);
      FeatureRange featureRange2 = new FeatureRange(feature2, l, l + 1);
    }
    try {
      //create a PNG file
      CgviewIO.writeToPNGFile(cgview, "test_maps/CgviewTest1.png");
      //create an SVG file with embedded fonts
      CgviewIO.writeToSVGFile(cgview, "test_maps/CgviewTest1.svg", false);
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
