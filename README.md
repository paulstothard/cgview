<img src="sample1.png" alt="drawing" width="200"/>

# CGView
CGView is a Java package for generating high quality, zoomable maps of circular genomes.

Sample maps created using CGView are available [here](https://paulstothard.github.io/cgview/gallery.html).

CGView was written and is maintained by Paul Stothard <stothard@ualberta.ca>.

## CGView citation

[Stothard P, Wishart DS (2005) Circular genome visualization and exploration using CGView. Bioinformatics 21:537-539.](https://pubmed.ncbi.nlm.nih.gov/15479716/)

## Using the CGView Docker image

Pull the Docker image:

```bash
docker pull pstothard/cgview
```

Download a test file:

```bash
wget https://paulstothard.github.io/cgview/downloads/prokka_multicontig.gbk
```

Run the Docker image and use [cgview\_xml\_builder.pl](scripts/cgview_xml_builder/README.md) to create a [CGView XML](https://paulstothard.github.io/cgview/xml_overview.html) file:

```bash
docker run --rm -v "$(pwd)":/dir -u "$(id -u)":"$(id -g)" -w /dir pstothard/cgview perl /usr/bin/cgview_xml_builder.pl -sequence prokka_multicontig.gbk -gc_content T -gc_skew T -size large-v2 -tick_density 0.05 -draw_divider_rings T -custom showBorder=false title="Example map" titleFontSize="200" -output map.xml
```

Run the Docker image and use `cgview.jar` to create a graphical map from the XML file:

```bash
docker run --rm -v "$(pwd)":/dir -u "$(id -u)":"$(id -g)" -w /dir pstothard/cgview java -jar /usr/bin/cgview.jar -i map.xml -o map.png
```

The `map.png` file is written to the current directory on the host system (as is `map.xml` from the first command).

Here is the map generated by the above commands:

![CGView map](prokka_map.png)

The following commands generate a similar map but with feature labels and zoomed in on a region of interest:

```bash
docker run --rm -v "$(pwd)":/dir -u "$(id -u)":"$(id -g)" -w /dir pstothard/cgview perl /usr/bin/cgview_xml_builder.pl -sequence prokka_multicontig.gbk -gc_content T -gc_skew T -size large-v2 -tick_density 0.05 -draw_divider_rings T -custom showBorder=false title="Example map" titleFontSize=200 labelFontSize=60 -feature_labels T -output map_labels.xml
docker run --rm -v "$(pwd)":/dir -u "$(id -u)":"$(id -g)" -w /dir pstothard/cgview java -jar /usr/bin/cgview.jar -i map_labels.xml -f png -o map_zoomed.png -z 10 -c 6600000
```

Here is the map generated by the above commands:

![CGView map](prokka_map_labels_zoom.png)

The staggered divider lines show the boundaries of contigs--to use regular divider lines add `-show_contigs F` to the end of the `cgview_xml_builder.pl` command.

The `cgview_xml_builder.pl` program has many [options](scripts/cgview_xml_builder/README.md) for altering the contents of the map. For details on what key-value pairs can be supplied using the `-custom` option of `cgview_xml_builder.pl` see [this page](https://paulstothard.github.io/cgview_comparison_tool/customization_keys.html). The [XML output](https://paulstothard.github.io/cgview/xml_overview.html) from `cgview_xml_builder.pl` can also be manually edited prior to running `cgview.jar` to further adjust map appearance. The `cgview.jar` program has [options](#cgview-options) for changing some aspects of the map and for specifying the format of the image that is produced.

To generate maps that include the results of comparisons with other genomes, use the [CGView Comparison Tool (CCT)](https://github.com/paulstothard/cgview_comparison_tool).

## CGView options

The command-line interface is described in detail in the [CGView documentation](https://paulstothard.github.io/cgview/application.html). The following information can be obtained using `java -jar cgview.jar --help`: 

```
CGView - drawing circular genome maps.

DISPLAY HELP AND EXIT:

  usage:

    java -jar cgview.jar --help

DISPLAY VERSION AND EXIT:

  usage:

    java -jar cgview.jar --version

CREATE A SINGLE MAP IMAGE:

  usage:

    java -jar cgview.jar -i <file> -o <file> [Options]

    required arguments:

      -i  Input file in CGView XML or TAB format.
      -o  Output file to create.

    optional arguments (when used these override corresponding values specified in XML input):

      -A  Font size for feature labels (default 10).
      -c  Base position to center on when using -z option (default 1).
      -D  Font size for legends (default 8).
      -d  Density of tick marks, between 0 and 1.0 (default 1.0).
      -f  Output file format: png, jpg, svg, or svgz.
      -H  Height of map (default 700).
      -h  HTML file to create for image map functionality.
      -I  Allow labels to be drawn on inside of circle, T or F (default is T for zoomed maps and F for unzoomed).
      -L  Width of user-supplied legend png file (legend.png) to be referenced in html output.
      -p  Path to image file in HTML file created using -h (default is -o value).
      -r  Remove legends, T or F (default F).
      -R  Remove feature labels, T or F (default F).
      -U  Font size for sequence ruler (default 8).
      -u  Include overlib.js calls for mouseover labels for png and jpg image maps in html output, T or F (default T).
      -W  Width of map (default 700).
      -z  Zoom multiplier (default 1).

    example usage:

      java -jar cgview.jar -i test.xml -o map.png -f png

CREATE A NAVIGABLE SERIES OF LINKED MAP IMAGES:

  usage:

    java -jar cgview.jar -i <file> -s <directory> [Options]

    required arguments:

      -i  Input file in CGView XML or TAB format.
      -s  Output directory for image series.

    optional arguments (when used these override corresponding values specified in XML input):

      -A  Font size for feature labels (default 10).
      -D  Font size for legends (default 8).
      -e  Exclude SVG output from image series, T or F (default F).
      -H  Height of map (default 700).
      -I  Allow labels to be drawn on inside of circle, T or F (default is T for zoomed maps and F for unzoomed maps).
      -L  Width of user-supplied legend png file (legend.png) to be referenced in html output.
      -r  Remove legends, T or F (default F).
      -U  Font size for sequence ruler (default 8).
      -u  Include overlib.js for mouseover labels for png and jpg image maps in html output, T or F (default T).
      -W  Width of map (default 700).
      -x  Zoom multipliers to use, comma-separated (default is 1,6,36).

    example usage:

      java -jar cgview.jar -i test.xml -s image_series
```

If `cgview.jar` exits with `Exception in thread "main" java.lang.OutOfMemoryError: Java heap space`, use the `-Xmx` option to increase the memory allocation pool. For example, use the following command:

```bash
java -jar -Xmx2000m cgview.jar -i test.xml -o map.png -f png
```

## Downloading CGView

The executable `cgview.jar` with dependencies included can be downloaded [here](https://github.com/paulstothard/cgview/releases/).

To test `cgview.jar` on your system you can try the following:

```bash
wget https://paulstothard.github.io/cgview/xml_sample/overview.xml
java -jar cgview.jar -i overview.xml -o map.png
```

## Building CGView

`cgview.jar` can be built from project source code using [Apache Maven](https://maven.apache.org).

First clone the cgview repository and switch to the project directory:

```bash
git clone git@github.com:paulstothard/cgview.git
cd cgview
```

Build `cgview.jar` using **mvn package**:

```bash
mvn package
```

Alternatively, an included `build.sh` script can be used to execute **mvn package** and to run additional tests:

```bash
./build.sh
```

The build process should create several CGView maps in the `test_maps` directory and updated API documentation and jar files in the `targets` directory.

## cgview\_xml\_builder.pl

The [cgview\_xml\_builder.pl](scripts/cgview_xml_builder/README.md) script can be used to generate XML input for `cgview.jar` from DNA sequence files. See the included [README.md](scripts/cgview_xml_builder/README.md) file for information on usage and required Perl modules.