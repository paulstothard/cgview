# cgview\_xml\_builder

FILE: cgview\_xml\_builder.pl  
AUTH: Paul Stothard <stothard@ualberta.ca>  
DATE: June 21, 2020  
VERS: 1.5  

This script accepts a variety of input files pertaining to circular genomes and generates an XML file for the [CGView genome drawing program](https://github.com/paulstothard/cgview). There are several command line options for specifying which input files should be used, and for controlling which features should be drawn.

This script requires the `Tie::IxHash`, `Bio::SeqIO`, and `Bio::SeqUtils` Perl modules. These can be installed using CPAN, e.g.:

```
sudo perl -MCPAN -e "install Tie::IxHash"
sudo perl -MCPAN -e "install Bio::SeqIO"
sudo perl -MCPAN -e "install Bio::SeqUtils"
```

## Creating a map using cgview\_xml\_builder.pl and cgview.jar

Create an XML file from a GenBank file using `cgview_xml_builder.pl`:

```bash
perl cgview_xml_builder.pl -sequence sample_input/R_denitrificans.gbk \
-output R_denitrificans.xml -tick_density 0.7
```

Convert the XML file to a graphical map using `cgview.jar`:

```bash
java -jar -Xmx1500m cgview.jar -i R_denitrificans.xml \
-o R_denitrificans.png -f png
```

## Tips

* There are numerous options for displaying additional information on the maps created using this script. These options are described in detail below.

* If CGView runs out of memory, increase the memory available to java using the `-Xmx` option. For example, try `-Xmx2000m`.

* To see sample output, run the `test.sh` script. It may take more than an hour for all the maps to be created. You may need to edit the `cgview` variable near the beginning of the script to point to `cgview.jar` on your system.

* To create smoother base composition plots, specify a larger sliding window value using the `-window` option in `cgview_xml_builder.pl`.

* If the base composition plots appear to be comprised of lines, specify a smaller step value using the `-step` option in `cgview_xml_builder.pl`.

* To reduce the number of tick marks, specify a smaller tick density value using the `-tick_density` option in `cgview_xml_builder.pl`.


## Usage

```
cgview_xml_builder.pl - generate XML files for cgview.jar.

DISPLAY HELP AND EXIT:

usage:

  perl cgview_xml_builder.pl -help

CREATING AN XML FILE FOR CGVIEW

usage:

  perl cgview_xml_builder.pl -sequence <file> -output <file> [Options]

required arguments:

-sequence - Input file in FASTA, EMBL, or GenBank format.

-output - The CGView XML file to create.

optional arguments:

open reading frame arguments:

-reading_frames - Whether the positions of start and stop codons should be
drawn. [T/F]. Default is F.

-orfs - Whether open reading frames (ORFs) should be drawn, with each reading
frame (1, 2, 3, -1, -2, -3) represented by a separate ring. [T/F]. Default is
F.

-combined_orfs - Whether open reading frames (ORFs) should be drawn, with each
strand (forward, reverse) represented by a separate ring. [T/F]. Default is F.

-orf_size - The minimum length of ORFs (in codons) to show when using the
'-orfs' or '-combined_orfs' options. [Integer]. Default is
100.

-starts - The start codons to use when plotting open reading frames or stop and
start codons. [String]. The default value is 'atg|ttg|att|gtg|ctg'.

-stops - The stop codons to use when plotting open reading frames or stop and
start codons. [String]. The default value is 'taa|tag|tga'.

base composition plot arguments:

-gc_content - Whether GC content should be shown. [T/F]. Default is T.

-gc_skew - Whether GC skew should be shown. [T/F]. Default is F.

-at_content - Whether AT content should be shown. [T/F]. Default is F.

-at_skew - Whether AT skew should be shown. [T/F]. Default is F.

-average - Whether the GC, GC skew, AT, and AT skew plots should show the
deviation of each value from the average for the entire genome. The default
method of plotting shows each value as calculated. Specifying '-average' allows
plots to better show regions that differ from the rest of the genome, but the
results cannot be easily compared between genomes. [T/F]. Default is T.

-scale - Whether the GC, GC skew, AT, and AT skew plots should be scaled to
fill the available Y-axis space on the map. This scaling allows differences to
be observed more easily, but the results cannot be easily compared between
genomes. [T/F]. Default is T.

-step - The step value to use when generating the GC, GC skew, AT, and AT skew
plots. This value should be decreased (to a minimum of 1) if in the final map
the lines comprising the plots can be seen. [Integer]. Default is to let
program choose step value.

-window - The size of the sliding window for base composition plots. Using a
larger window size gives smoother graphs, but may hide details. [Integer].
Default is to let program choose step value.

map appearance arguments:

-size - The size of the map. [small/medium/large/large-v2/x-large]. Default is
medium. large-v2 is recommended for most figures for publication.

-linear - Whether this genome is linear. Linear genomes are drawn as a circle
with a line drawn between the start and end of the sequence. [T/F]. Default is
to read this setting from the GenBank or EMBL file, if available, otherwise the
value is set to F.

-tick_density - The density of the tick marks on the map. Use a smaller value
to make the ticks less dense. [Real between 0 and 1]. Default is 0.5.

-title - A title for the sequence, to appear on the map. [String]. Default is
to obtain a title from the input sequence file (for FASTA, GenBank, and EMBL
input).

-details - Whether a sequence information legend should be drawn. [T/F].
Default is T.

-legend - Whether a feature legend should be drawn. [T/F]. Default is T.

-parse_reading_frame - Whether BLAST results should be split into separate
feature rings on the map, based on the reading frame and strand of the query.
Requires specially formatted BLAST results. [T/F]. Default is F.

-show_queries - Whether faint boxes should be drawn to indicate the positions
of the BLAST queries relative to the genome sequence. [T/F]. Default is F.

-condensed - Whether thin feature rings should be used regardless of the size
of the map. This option may be useful when numerous feature rings are drawn.
[T/F]. Default is F.

-feature_labels - Whether feature labels read from the GenBank or EMBL file
should be drawn. [T/F]. Default is F.

-gene_labels - Whether labels read using the '-genes' or '-expression' option
should be drawn. [T/F]. Default is F.

-hit_labels - Whether labels for BLAST hits read using the '-blast' option
should be drawn. [T/F]. Default is F.

-labels_to_show - A tab-delimited or comma-delimited text file specifying which
genes should be labeled. Each row must consist of a gene identifier followed by
the text that is to be used for the label. When using a GenBank or EMBL file as
input the gene identifier should match the value of the '/gene' qualifier (or
the value of the '/locus_tag' qualifier if there isn't a '/gene' qualifier
given for a particular gene). When using the '-genes' option to supply gene
information, the gene identifier should match the 'seqname' value. Note that
the '-gene_labels' and '-feature_labels' options are ignored when a file is
provided using '-labels_to_show'. However, the '-global_label' option can still
prevent any labels from being drawn, depending on the value it is given.
[FILE].

-orf_labels - Whether labels for ORFs that are drawn using the '-orfs' or
'-combined_orfs' should be shown. [T/F]. Default is F.

-gene_decoration - Whether genes should be drawn as an arc or as an arrow.
[String]. Default is 'arrow'.

-global_label - Can be used to override other labeling settings. Set to T to
always show labels, F to always show no labels, or 'auto' to show labels when a
zoomed map is drawn. When set to F, no labels are shown, regardless of the
other label settings ('-feature_labels', '-gene_labels', '-hit_labels', and
'-orf_labels'). When set to T, labels are shown for those label settings that
are set to T. When set to auto, labels are shown for those label settings that
are T only when a zoomed map is drawn. [T/F/auto]. Default is T.

-use_opacity - Whether BLAST hits should be drawn with partial opacity, so that
overlapping hits can be visualized. [T/F]. Default is F.

-scale_blast - Whether BLAST hits should be drawn with height proportional to
percent identity of hit. Also determines whether COGs supplied using '-genes'
option are drawn with height proportional to score [T/F]. Default is T.

-show_sequence_features - Whether to draw features contained in the supplied
'-sequence' file, if it is a GenBank or EMBL file. [T/F]. Default is T.

-draw_divider_rings - Whether to draw divider rings between feature rings.
[T/F]. Default is F.

-custom - Settings used to customize the appearance of the map. These settings
override those calculated by the script. [STRINGS]. Multiple settings
can be supplied using this option, as in the following example:

perl cgview_xml_builder.pl \
-sequence sample_input/R_denitrificans.gbk \
-output R_denitrificans.xml \
-tick_density 0.7 -custom tickLength=20 labelFontSize=15

data source arguments:

-genes - One or more files containing gene position information for the genes
in the genome. Each file should be tab-delimited or comma-delimited and should
have the following column titles, in the following order: 'seqname', 'source',
'feature', 'start', 'end', 'score', 'strand', 'frame'. The first line in the
file must be the column titles. For a given entry, 'seqname' should be the name
of the gene, 'feature' should be the type of gene (CDS, rRNA, tRNA, other) or
the single letter COG category (J for example). 'start' and 'end' should be
integers between 1 and the length of the sequence, and the 'start' value should
be less than or equal to the 'end' regardless of the 'strand' value. The
'strand' value should be '+' for the forward strand and '-' for the reverse
strand. All other values can be given as '.' or left blank, since they are
ignored. These column titles are based on the specification of the GFF file
format. If 'start' and 'end' values are not supplied, but a 'seqname' is given,
this script will attempt to get the 'start' and 'end' values from the sequence
file. [Files]. Multiple files can be supplied using the '-genes'
option, as in the following example:

perl cgview_xml_builder.pl \
-sequence sample_input/R_denitrificans.gbk \
-output R_denitrificans.xml \
-tick_density 0.7 -genes file1.txt file2.txt

-analysis - One or more files containing gene expression information for the
genes in the genome. Each file should be tab-delimited or comma-delimited and
should have the following column titles, in the following order: 'seqname',
'source', 'feature', 'start', 'end', 'score', 'strand', 'frame'. The first line
in the file must be the column titles. For a given entry, only the 'start',
'end', 'strand', and 'score' values are required. 'start' and 'end' should be
integers between 1 and the length of the sequence, and the 'start' value should
be less than or equal to the 'end' regardless of the 'strand' value. The
'strand' value should be '+' for the forward strand and '-' for the reverse
strand. The 'score' value should be a real number, positive or negative. The
other values can be given as '.' or left blank. These column titles are based
on the specification of the GFF file format. If 'start' and 'end' values are
not supplied, but a 'seqname' is given, this script will attempt to get the
'start' and 'end' values from the sequence file. [Files]. Multiple
files can be supplied using the '-analysis' option, as in the following
example:

perl cgview_xml_builder.pl \
-sequence sample_input/R_denitrificans.gbk \
-output R_denitrificans.xml \
-tick_density 0.7 -analysis file1.txt file2.txt

-blast - One or more files of BLAST results to display. This option is used in
the CGView Comparison Tool. The percent identity of each hit is plotted.
[Files]. Multiple files canbe supplied using the '-blast' option, as
in the following example:

perl cgview_xml_builder.pl \
-sequence sample_input/R_denitrificans.gbk \
-output R_denitrificans.xml \
-tick_density 0.7 -blast file1.txt file2.txt

-blast_list - A text file consisting of the names of BLAST results files, one
per line. This option is useful when there are too many BLAST results files to
pass as values to the '-blast' option. [File].

other arguments:

-verbose - Whether program progress should be written to standard output.
[T/F]. Default is T.

-log - A log file for recording progress and error messages. [File]. Default is
to not write a log file.

example usage:

  perl cgview_xml_builder.pl -sequence test_input/prokka_multicontig.gbk \
  -output prokka_map.xml -gc_content T -gc_skew T -size large-v2
```
