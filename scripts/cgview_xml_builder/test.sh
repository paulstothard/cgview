#!/bin/bash
set -e
cgview=../../target/cgview.jar

if [ ! -d ./test_output ]; then
    mkdir ./test_output
fi

#test contig merging and new size setting large-v2
perl cgview_xml_builder.pl -sequence test_input/prokka_multicontig.gbk \
-output test_output/prokka_multicontig.xml \
-gc_content T -gc_skew T -size large-v2 \
-tick_density 0.05 -draw_divider_rings T -custom showBorder=false

java -jar -Xmx2000m $cgview -i test_output/prokka_multicontig.xml \
-o test_output/prokka_multicontig.png -f png

for size in small medium large x-large; do
    #simple
    perl cgview_xml_builder.pl -sequence test_input/R_denitrificans.gbk \
        -genes test_input/R_denitrificans.cogs \
        -output test_output/${size}_simple.xml -gc_content T -gc_skew T \
        -size $size -title 'Roseobacter denitrificans' -draw_divider_rings T

    java -jar -Xmx2000m $cgview -i test_output/${size}_simple.xml \
        -o test_output/${size}_simple.png -f png

    #complex
    perl cgview_xml_builder.pl -sequence test_input/R_denitrificans.gbk \
        -genes test_input/R_denitrificans.cogs \
        -output test_output/${size}_complex.xml -reading_frames T -orfs T \
        -combined_orfs T -gc_content T -gc_skew T -at_content T -at_skew T \
        -size $size -title 'Roseobacter denitrificans' -draw_divider_rings T

    java -jar -Xmx2000m $cgview -i test_output/${size}_complex.xml \
        -o test_output/${size}_complex.png -f png
done

#create a medium map with subset of feature labels
size=medium
perl cgview_xml_builder.pl -sequence test_input/R_denitrificans.gbk \
    -genes test_input/R_denitrificans.cogs \
    -output test_output/${size}_simple_labels_subset.xml -size $size \
    -title 'Roseobacter denitrificans' -feature_labels T \
    -draw_divider_rings T -labels_to_show test_input/labels_to_show.txt

java -jar -Xmx2000m $cgview -i test_output/${size}_simple_labels_subset.xml \
    -o test_output/${size}_simple_labels_subset.png -f png

#create zoomed with starts and stops
size=large
perl cgview_xml_builder.pl -sequence test_input/R_denitrificans.gbk \
    -genes test_input/R_denitrificans.cogs \
    -output test_output/${size}_no_labels.xml -reading_frames T \
    -combined_orfs T -gc_content F -gc_skew F -at_content F -at_skew F \
    -size $size -title 'Roseobacter denitrificans' -draw_divider_rings T

java -jar -Xmx1000m $cgview -i test_output/${size}_no_labels.xml \
    -o test_output/${size}_no_labels_zoomed.png -f png -z 50 -c 10000

#test ability to handle multiple 'genes' files
size=medium
perl cgview_xml_builder.pl -sequence test_input/R_denitrificans.gbk \
    -genes test_input/R_denitrificans.cogs test_input/R_denitrificans.cogs \
    test_input/R_denitrificans.cogs \
    -output test_output/${size}_multiple_genes_files.xml -reading_frames T \
    -combined_orfs T -gc_content F -gc_skew F -at_content F -at_skew F \
    -size $size -title 'Roseobacter denitrificans' -draw_divider_rings T

java -jar -Xmx1000m $cgview -i test_output/${size}_multiple_genes_files.xml \
    -o test_output/${size}_multiple_genes_files_zoomed.png -f png -z 50 \
    -c 10000

#create an x-large with feature labels
size=x-large
perl cgview_xml_builder.pl -sequence test_input/R_denitrificans.gbk \
    -genes test_input/R_denitrificans.cogs \
    -output test_output/${size}_complex_labels.xml -reading_frames T -orfs T \
    -combined_orfs T -gc_content T -gc_skew T -at_content T -at_skew T \
    -size $size -title 'Roseobacter denitrificans' -feature_labels T \
    -draw_divider_rings T

java -jar -Xmx2000m $cgview -i test_output/${size}_complex_labels.xml \
    -o test_output/${size}_complex_labels.png -f png
