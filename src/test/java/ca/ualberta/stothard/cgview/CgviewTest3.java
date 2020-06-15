package ca.ualberta.stothard.cgview;

import ca.ualberta.stothard.cgview.*;
import java.awt.*;
import java.io.*;

public class CgviewTest3 implements CgviewConstants {

  public static void main(String args[]) {
    int length = 9000;
    Cgview cgview = new Cgview(length);

    //some optional settings
    cgview.setWidth(750);
    cgview.setHeight(750);
    cgview.setBackboneRadius(150.0f);
    cgview.setBackboneColor(Color.blue);
    cgview.setTitle("Example");
    cgview.setLabelPlacementQuality(5);
    cgview.setShowWarning(true);
    cgview.setLabelLineThickness(1.5f);
    cgview.setRulerTextPadding(6.0f);

    Legend legend = new Legend(cgview);
    legend.setPosition(LEGEND_UPPER_CENTER);
    LegendItem legendItem = new LegendItem(legend);
    legendItem.setLabel("Point to labels to view mouseover information.");
    legendItem.setFont(new Font("SansSerif", Font.PLAIN, 20));
    legendItem.setTextAlignment(LEGEND_ITEM_ALIGN_CENTER);

    legendItem = new LegendItem(legend);
    legendItem.setLabel("Click on labels to test hyperlinks.");
    legendItem.setFont(new Font("SansSerif", Font.PLAIN, 20));
    legendItem.setTextAlignment(LEGEND_ITEM_ALIGN_CENTER);

    //create a FeatureSlot to hold sequence features
    FeatureSlot featureSlot = new FeatureSlot(cgview, DIRECT_STRAND);

    //create random sequence features
    for (int i = 1; i <= 50; i = i + 1) {
      int j = Math.round((float) ((float) (length - 2) * Math.random())) + 1;

      //a Feature to add to our FeatureSlot
      Feature feature = new Feature(featureSlot, "label");
      feature.setColor(Color.gray);

      //a single FeatureRange to add the Feature
      FeatureRange featureRange = new FeatureRange(feature, j, j + 1);
      featureRange.setDecoration(DECORATION_CLOCKWISE_ARROW);
      //add a link and mouseover. Normally you would use a link to a specific
      //gene or protein
      featureRange.setHyperlink(
        "https://www.ncbi.nlm.nih.gov/entrez/query.fcgi"
      );
      featureRange.setMouseover("Start = " + j + ", stop = " + (j + 1));
    }

    try {
      //create a PNG map.
      CgviewIO.writeToPNGFile(cgview, "test_maps/CgviewTest3.png");
      //for PNG and JPG files the mouseover and link information
      //can be placed in an image map. To generate the image map,
      //use the CgviewIO.writeHTMLFile() method after the image
      //has been written to file.

      //the overLIB javascript library (https://github.com/overlib/overlib)
      //can be used for displaying mouseovers. You will need to make overlib.js
      //available to the html file by placing overlib.js inside an 'includes'
      //directory
      boolean useOverlib = true;
      CgviewIO.writeHTMLFile(
        cgview,
        "CgviewTest3.png",
        "png",
        "test_maps/CgviewTest3.html",
        useOverlib
      );

      //create an SVG file. The mouseover and links are included in the SVG.
      boolean useCompression = false;

      //The previous call to writeToPNGFile generated and positioned
      //the feature labels in a non-overlapping arrangement.
      //Label positioning can be slow. Supplying usePreviousDrawnLabels
      //set to true tells CgviewIO to use the previously arranged labels
      //instead of generating and positioning new labels.
      boolean usePreviouslyDrawnLabels = true;
      CgviewIO.writeToSVGFile(
        cgview,
        "test_maps/CgviewTest3.svg",
        useCompression,
        usePreviouslyDrawnLabels
      );
      System.exit(0);
    } catch (IOException e) {
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }
}
