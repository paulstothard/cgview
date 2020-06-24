#   CGView - a Java package for generating high-quality, zoomable maps of
#   circular genomes.
#   Copyright (C) 2005 Paul Stothard stothard@ualberta.ca
#
#   This program is free software: you can redistribute it and/or modify
#   it under the terms of the GNU General Public License as published by
#   the Free Software Foundation, either version 3 of the License, or
#   (at your option) any later version.
#
#   This program is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#   GNU General Public License for more details.
#
#   You should have received a copy of the GNU General Public License
#   along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
#
#FILE: cgview_xml_builder.pl
#AUTH: Paul Stothard <stothard@ualberta.ca>
#DATE: June 21, 2020
#VERS: 1.5

use strict;
use warnings;
use Bio::SeqIO;
use Bio::SeqUtils;
use Getopt::Long qw(:config pass_through);
use Data::Dumper;
use Tie::IxHash;
use File::Temp qw/ tempfile tempdir /;

my $MIN_ORF_SIZE = 15;
my $MAX_ORF_SIZE = 1000;
my $MIN_STEP     = 1;
my $MAX_STEP     = 100;
my $MIN_WINDOW   = 1000;
my $MAX_WINDOW   = 10000;
my $MIN_LENGTH   = 1000;
my $MAX_LENGTH   = 20000000;

my %options = (
    sequence               => undef,
    output                 => undef,
    reading_frames         => 'F',
    orfs                   => 'F',
    combined_orfs          => 'F',
    orf_size               => 100,
    starts                 => 'atg|ttg|att|gtg|ctg',
    stops                  => 'taa|tag|tga',
    gc_content             => 'T',
    gc_skew                => 'T',
    at_content             => 'F',
    at_skew                => 'F',
    average                => 'T',
    scale                  => 'T',
    step                   => undef,
    window                 => undef,
    size                   => "medium",
    tick_density           => 0.5,
    linear                 => undef,
    title                  => undef,
    details                => 'T',
    legend                 => 'T',
    parse_reading_frame    => 'F',
    show_queries           => 'F',
    condensed              => 'F',
    feature_labels         => 'F',
    gene_labels            => 'F',
    hit_labels             => 'F',
    orf_labels             => 'F',
    global_label           => 'T',
    use_opacity            => 'F',
    scale_blast            => 'T',
    show_sequence_features => 'T',
    show_contigs           => 'T',
    draw_divider_rings     => 'F',
    font_size              => undef,
    gene_decoration        => 'arrow',
    genes                  => undef,
    analysis               => undef,
    blast                  => undef,
    blast_list             => undef,
    verbose                => 'T',
    log                    => undef,
    labels_to_show         => undef,
    custom                 => undef,
    cct                    => 0,
    blast_divider_ruler    => 'F',
    help                   => undef
);

tie( my %cogColors, 'Tie::IxHash' );

#The descriptions below are based on NCBI COGs
%cogColors = (    #Oranges (Information storage and processing)
    A => "rgb(255,0,0)",      #red - RNA processing and modification
    B => "rgb(255,99,71)",    #tomato - Chromatin structure and dynamics
    J => "rgb(240,128,128)"
    ,    #light coral - Translation, ribosomal structure and biogenesis
    K => "rgb(255,140,0)",    #dark orange - Transcription
    L => "rgb(255,20,147)",   #deep pink - Replication, recombination and repair
                              #Greens/Yellows (Cellular processes and signaling)
    D => "rgb(240,230,140)"
    ,    #khaki - Cell cycle control, cell division, chromosome partitioning
    O => "rgb(189,183,107)"
    , #dark khaki - Post-translational modification, protein turnover, and chaperones
    M => "rgb(107,142,35)", #olive drab - Cell wall/membrane/envelope biogenesis
    N => "rgb(34,139,34)",  #forest green - Cell motility
    P =>
      "rgb(154,205,50)",  #yellow green - Inorganic ion transport and metabolism
    T => "rgb(50,205,50)",    #lime green - Signal transduction mechanisms
    U => "rgb(173,255,47)"
    , #green yellow - Intracellular trafficking, secretion, and vesicular transport
    V => "rgb(0,250,154)",    #medium spring green - Defense mechanisms
    W => "rgb(143,188,143)"
    , #dark sea green - Extracellular structures (this doesn't appear in reference database)
    Y => "rgb(60,179,113)"
    , #medium sea green - Nuclear structure (this appears once in reference database)
    Z => "rgb(255,255,0)",    #yellow - Cytoskeleton
                              #Blues/Purples (Metabolism)
    C => "rgb(0,255,255)",    #cyan - Energy production and conversion
    G =>
      "rgb(0,206,209)",  #dark turquoise - Carbohydrate transport and metabolism
    E => "rgb(70,130,180)", #steel blue - Amino acid transport and metabolism
    F => "rgb(0,191,255)",  #deep sky blue - Nucleotide transport and metabolism
    H => "rgb(0,0,255)",    #blue - Coenzyme transport and metabolism
    I => "rgb(106,90,205)", #slate blue - Lipid transport and metabolism
    Q => "rgb(0,0,128)"
    ,    #navy - Secondary metabolites biosynthesis, transport, and catabolism
         #Grays (Poorly characterized)
    R => "rgb(190,190,190)"
    , #gray - General function prediction only (examples include "Predicted thioesterase", "Predicted ATPase")
    S => "rgb(105,105,105)"
    , #dark gray - Function unknown (examples include "Uncharacterized conserved protein", "Predicted small secreted protein")
    Unknown =>
      "rgb(255,255,255)" #white - not assigned COG letter because protein is not similar to any COG
);

my %settings = (
    cogColors => \%cogColors,

    blastColors => [
        "rgb(139,0,0)",    #dark red
        "rgb(0,100,0)",    #dark green
        "rgb(0,0,139)"     #dark blue
    ],

    analysisColors => [
        "rgb(0,128,0)",        #green
        "rgb(241,199,200)",    #light pink
        "rgb(255,0,255)",      #fuchsia
        "rgb(0,153,153)",      #light blue
        "rgb(0,153,0)",        #green
        "rgb(153,153,0)",      #yellow
        "rgb(0,200,0)",        #green
        "rgb(54,54,0)"         #yellow
                               #"rgb(0,128,0)",               #green
                               #"rgb(241,199,200)",           #light pink
                               #"rgb(255,0,255)",             #fuchsia
                               #"rgb(0,153,153)",             #light blue
                               #"rgb(0,153,0)",               #green
                               #"rgb(153,153,0)"              #yellow
    ],

    width                => "3000",
    height               => "3000",
    backboneRadius       => "820",
    backboneColor        => "rgb(102,102,102)",    #dark gray
    backboneThickness    => "20",
    blastRulerColor      => "rgb(255,165,0)",      #orange
    featureThickness     => "30",                  #was 40
    featureThicknessPlot => "50",                  #was 45
    featureSlotSpacing   => "2",
    rulerFontSize        => "30",
    rulerFontColor       => "rgb(0,0,0)",
    titleFontSize        => "80",
    labelFontSize        => "30",
    legendFontSize       => "30",
    maxTitleLength       => "90",
    maxLabelLength       => "20",
    maxLegendLength      => "30",
    plotLineThickness    => "0.02",
    proteinColor         => "rgb(0,0,153)",        #dark blue
    tRNAColor            => "rgb(153,0,0)",        #dark red
    rRNAColor            => "rgb(153,0,153)",      #dark purple
    otherColor           => "rgb(51,51,51)",       #dark gray
    featureOpacity =>
      "0.5",  #features are drawn with transparency so that overlaps can be seen
    featureOpacityOther => "0.5"
    , #features of type 'other' are drawn with transparency so that underlying CDS can be seen
    gcColorPos             => "rgb(0,0,0)",        #black
    gcColorNeg             => "rgb(0,0,0)",        #black
    atColorPos             => "rgb(51,51,51)",     #dark gray
    atColorNeg             => "rgb(51,51,51)",     #dark gray
    gcSkewColorPos         => "rgb(0,153,0)",      #dark green
    gcSkewColorNeg         => "rgb(153,0,153)",    #dark purple
    atSkewColorPos         => "rgb(153,153,0)",    #dark yellow
    atSkewColorNeg         => "rgb(0,0,153)",      #dark blue
    orfColor               => "rgb(204,0,0)",      #dark red
    startColor             => "rgb(153,0,153)",    #dark purple
    stopColor              => "rgb(204,0,0)",      #dark red
    backgroundColor        => "white",
    borderColor            => "black",
    tickColor              => "rgb(0,51,0)",       #dark green
    labelLineLength        => "200",
    labelPlacementQuality  => "good",              #good, better, best
    labelLineThickness     => "4",
    rulerPadding           => "40",
    tickThickness          => "5",
    arrowheadLength        => "6",
    minimumFeatureLength   => "1.0",
    moveInnerLabelsToOuter => "false",
    tickLength             => "20",
    useInnerLabels         => "true",
    showBorder             => "true",
    isLinear               => "false",
    autoPlotSlotIncrease   => 1,
    maxFeatureSize         => 50000
  )
  ; #maxFeatureSize is used to prevent large features ('source' for example) from obscuring other features

my %global = (
    orfCount => 0,
    ncbiGiLink =>
"https://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Text\&amp;db=Protein\&amp;dopt=genpept\&amp;dispmax=20\&amp;uid=",
    ncbiGeneLink =>
"https://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene\&amp;cmd=Retrieve\&amp;dopt=Graphics\&amp;list_uids=",
    format    => undef,
    length    => undef,
    accession => undef,
    topology  => "circular",
    contigs   => []
);

my %param = (
    options  => \%options,
    settings => \%settings,
    global   => \%global
);

#read user options
GetOptions(
    'sequence=s'               => \$options{sequence},
    'output=s'                 => \$options{output},
    'reading_frames=s'         => \$options{reading_frames},
    'orfs=s'                   => \$options{orfs},
    'combined_orfs=s'          => \$options{combined_orfs},
    'orf_size=i'               => \$options{orf_size},
    'starts=s'                 => \$options{starts},
    'stops=s'                  => \$options{stops},
    'gc_content=s'             => \$options{gc_content},
    'gc_skew=s'                => \$options{gc_skew},
    'at_content=s'             => \$options{at_content},
    'at_skew=s'                => \$options{at_skew},
    'average=s'                => \$options{average},
    'scale=s'                  => \$options{scale},
    'step=i'                   => \$options{step},
    'window=i'                 => \$options{window},
    'size=s'                   => \$options{size},
    'tick_density=f'           => \$options{tick_density},
    'linear=s'                 => \$options{linear},
    'title=s'                  => \$options{title},
    'details=s'                => \$options{details},
    'legend=s'                 => \$options{legend},
    'parse_reading_frame=s'    => \$options{parse_reading_frame},
    'show_queries=s'           => \$options{show_queries},
    'condensed=s'              => \$options{condensed},
    'feature_labels=s'         => \$options{feature_labels},
    'gene_labels=s'            => \$options{gene_labels},
    'hit_labels=s'             => \$options{hit_labels},
    'orf_labels=s'             => \$options{orf_labels},
    'use_opacity=s'            => \$options{use_opacity},
    'scale_blast=s'            => \$options{scale_blast},
    'show_sequence_features=s' => \$options{show_sequence_features},
    'show_contigs=s'           => \$options{show_contigs},
    'draw_divider_rings=s'     => \$options{draw_divider_rings},
    'global_label=s'           => \$options{global_label},
    'font_size=s'              => \$options{font_size},
    'gene_decoration=s'        => \$options{gene_decoration},
    'genes=s@{,}'              => \$options{genes},
    'analysis=s@{,}'           => \$options{analysis},
    'blast=s@{,}'              => \$options{blast},
    'blast_list=s'             => \$options{blast_list},
    'verbose=s'                => \$options{verbose},
    'log=s'                    => \$options{log},
    'labels_to_show=s'         => \$options{labels_to_show},
    'custom=s@{,}'             => \$options{custom},
    'cct=i'                    => \$options{cct},
    'h|help'                   => \$options{help}
);

if ( defined( $options{help} ) ) {
    print_usage();
    exit(0);
}

#2012-01-16
#Check for unparsed options (requires 'pass_through' option to be set.)
#(e.g. use Getopt::Long qw(:config pass_through); )
if ( @ARGV > 0 ) {
    foreach my $extra_option (@ARGV) {
        print "Unknown Option: $extra_option\n";
    }
    die("Could not parse options");
}

#2011-06-25
#Check for file containing list of BLAST results files
if ( defined( $options{blast_list} ) ) {

    #read file names into array
    open( my $BLASTLIST, $options{blast_list} )
      or die("Cannot open file '$options{blast_list}': $!");
    my @files = ();
    while ( my $line = <$BLASTLIST> ) {
        chomp $line;
        if ( ( $line =~ m/^\s*$/ ) || ( $line =~ m/^\#/ ) ) {
            next;
        }
        push( @files, $line );
    }
    $options{'blast'} = \@files;
    close($BLASTLIST) or die("Cannot close file : $!");
}

#used for CGView Comparison Tool
if ( $options{cct} == 1 ) {

    #use this hash to specify BLAST colors based on percent identity
    #comment out three lines below to use same color for all BLAST results
    #and to give accessions of comparison genomes in legend.
    tie( my %blast_heat_map, 'Tie::IxHash' );

    #2011-02-16
    #check blast files to decide whether all represent DNA-based comparisons
    my $all_dna_blast = 1;
    foreach my $blast_file ( @{ $options{'blast'} } ) {
        my $blast_program = _getBlastProgram($blast_file);
        if ( ( !defined($blast_program) ) || ( $blast_program ne 'blastn' ) ) {
            $all_dna_blast = 0;
            last;
        }
    }

    if ($all_dna_blast) {

        #this heat map is appropriate for DNA-level comparisons
        %blast_heat_map = (
            100 => "rgb(0,0,0)",
            98  => "rgb(165,15,21)",
            96  => "rgb(203,24,29)",
            94  => "rgb(239,59,44)",
            92  => "rgb(251,106,74)",
            90  => "rgb(252,146,114)",
            88  => "rgb(66,146,198)",
            86  => "rgb(107,174,214)",
            84  => "rgb(158,202,225)",
            82  => "rgb(198,219,239)",
            0   => "rgb(222,235,247)"
        );

    }
    else {

        #this heat map is appropriate for protein-level comparisons
        %blast_heat_map = (
            100 => "rgb(0,0,0)",
            90  => "rgb(165,15,21)",
            80  => "rgb(203,24,29)",
            70  => "rgb(239,59,44)",
            60  => "rgb(251,106,74)",
            50  => "rgb(252,146,114)",
            40  => "rgb(66,146,198)",
            30  => "rgb(107,174,214)",
            20  => "rgb(158,202,225)",
            10  => "rgb(198,219,239)",
            0   => "rgb(222,235,247)"
        );
    }

    $settings{blast_heat_map} = \%blast_heat_map;

    if ( $options{size} eq 'x-large' ) {
        $options{global_label}   = 'T';
        $options{feature_labels} = 'T';
        $options{legend}         = 'T';
    }
    elsif ( $options{size} eq 'large' ) {
        $options{global_label}   = 'auto';
        $options{feature_labels} = 'F';
        $options{legend}         = 'T';
    }
    elsif ( $options{size} eq 'large-v2' ) {
        $options{global_label}   = 'auto';
        $options{feature_labels} = 'F';
        $options{legend}         = 'T';
    }
    elsif ( $options{size} eq 'medium' ) {
        $options{global_label}   = 'auto';
        $options{feature_labels} = 'F';
        $options{legend}         = 'T';
    }
    else {
        $options{global_label}   = 'auto';
        $options{feature_labels} = 'F';
        $options{legend}         = 'F';
    }

    ##added 2012-05-30 to allow -labels_to_show option to function as expected
    ##when -cct option used
    if ( defined( $options{labels_to_show} ) ) {
        $options{global_label} = 'T';
    }
}

#check for required options
if ( !( defined( $param{options}->{sequence} ) ) ) {
    die("Please specify a sequence using the '-sequence' option.\n");
}
if ( !( defined( $param{options}->{output} ) ) ) {
    die("Please specify an output file using the '-output' option.\n");
}

#start log file
if ( defined( $param{options}->{log} ) ) {
    _createLog( $param{options}->{log} );
}

#check some important values
#-orf_size
if ( $options{orf_size} =~ m/(\d+)/ ) {
    $options{orf_size} = $1;
}
else {
    _message( $param{options}, "-orf_size must be an integer value." );
    die("-orf_size must be an integer value.");
}
if ( $options{orf_size} < $MIN_ORF_SIZE ) {
    _message( $param{options},
        "-orf_size must be greater than or equal to $MIN_ORF_SIZE." );
    die("-orf_size must be greater than or equal to $MIN_ORF_SIZE.");
}
if ( $options{orf_size} > $MAX_ORF_SIZE ) {
    _message( $param{options},
        "-orf_size must be less than or equal to $MAX_ORF_SIZE." );
    die("-orf_size must be less than or equal to $MAX_ORF_SIZE.");
}

#-step
if ( defined( $options{step} ) ) {
    if ( $options{step} =~ m/(\d+)/ ) {
        $options{step} = $1;
    }
    else {
        _message( $param{options}, "-step must be an integer value." );
        die("-step must be an integer value.");
    }
    if ( $options{step} < $MIN_STEP ) {
        _message( $param{options},
            "-step must be greater than or equal to $MIN_STEP." );
        die("-step must be greater than or equal to $MIN_STEP.");
    }
    if ( $options{step} > $MAX_STEP ) {
        _message( $param{options},
            "-step must be less than or equal to $MAX_STEP." );
        die("-step must be less than or equal to $MAX_STEP.");
    }
}

#-window
if ( defined( $options{window} ) ) {
    if ( $options{window} =~ m/(\d+)/ ) {
        $options{window} = $1;
    }
    else {
        _message( $param{options}, "-window must be an integer value." );
        die("-window must be an integer value.");
    }
    if ( $options{window} < $MIN_WINDOW ) {
        _message( $param{options},
            "-window must be greater than or equal to $MIN_WINDOW." );
        die("-window must be greater than or equal to $MIN_WINDOW.");
    }
    if ( $options{window} > $MAX_WINDOW ) {
        _message( $param{options},
            "-window must be less than or equal to $MAX_WINDOW." );
        die("-window must be less than or equal to $MAX_WINDOW.");
    }
}

#-tick_density
if ( $options{tick_density} =~ m/([\d\.]+)/ ) {
    $options{tick_density} = $1;
}
else {
    _message( $param{options}, "-tick_density must be a real value." );
    die("-tick_density must be a real value.");
}
if ( $options{tick_density} < 0 ) {
    _message( $param{options},
        "-tick_density must be greater than or equal to 0." );
    die("-tick_density must be greater than or equal to 0.");
}
if ( $options{tick_density} > 1 ) {
    _message( $param{options},
        "-tick_density must be less than or equal to 1." );
    die("-tick_density must be less than or equal to 1.");
}

#-global_label
if ( $options{global_label} =~ m/auto/i ) {
    $options{global_label} = "auto";
}
elsif ( $options{global_label} =~ m/t/i ) {
    $options{global_label} = "true";
}
else {
    $options{global_label} = "false";
}

#-starts
if ( defined( $options{starts} ) ) {
    my @starts = split( /\|/, $options{starts} );
    foreach (@starts) {
        if ( !( $_ =~ m/[a-z]{3}/ ) ) {
            _message( $param{options},
"-starts must be given as codons separated by the '|', eg 'atg|ttg|att|gtg|ctg'. Be sure to include single quotes when passing as a command line argument."
            );
            die(
"-starts must be given as codons separated by the '|', eg 'atg|ttg|att|gtg|ctg'. Be sure to include single quotes when passing as a command line argument."
            );
        }
    }
}

#-stops
if ( defined( $options{stops} ) ) {
    my @stops = split( /\|/, $options{stops} );
    foreach (@stops) {
        if ( !( $_ =~ m/[a-z]{3}/ ) ) {
            _message( $param{options},
"-stops must be given as codons separated by the '|', eg 'taa|tag|tga'. Be sure to include single quotes when passing as a command line argument."
            );
            die(
"-stops must be given as codons separated by the '|', eg 'taa|tag|tga'. Be sure to include single quotes when passing as a command line argument."
            );
        }
    }
}

#obtain BioPerl Bio::Seq sequence object
#_getSeqObject also sets value of $global{format} to 'genbank', 'embl', 'raw', or 'fasta'
my $seqObject = _getSeqObject( \%param );

#get feature objects, go through them, find complex features, and divide them into new features.
_expand_complex_features($seqObject);

#determine the length of the genome
$global{length} = $seqObject->length();
if ( !( defined( $global{length} ) ) ) {
    _message( $param{options},
"The sequence length could not be determined from the -sequence file $options{sequence}."
    );
    die(
"The sequence length could not be determined from the -sequence file $options{sequence}."
    );
}

if ( $global{length} < $MIN_LENGTH ) {
    _message( $param{options},
        "The sequence must be longer than $MIN_LENGTH bases." );
    die("The sequence must be longer than $MIN_LENGTH bases.");
}

if ( $global{length} > $MAX_LENGTH ) {
    _message( $param{options},
        "The sequence must be shorter than $MAX_LENGTH bases." );
    die("The sequence must be shorter than $MAX_LENGTH bases.");
}

#set window
#these values may need to be adjusted
if ( !( defined( $options{window} ) ) ) {
    if ( $global{length} < 1000 ) {
        $options{window} = 10;
    }
    elsif ( $global{length} < 10000 ) {
        $options{window} = 50;
    }
    elsif ( $global{length} < 100000 ) {
        $options{window} = 500;
    }
    elsif ( $global{length} < 1000000 ) {
        $options{window} = 1000;
    }
    elsif ( $global{length} < 10000000 ) {
        $options{window} = 10000;
    }
    else {
        $options{window} = 10000;
    }
}

#set step.
#these values may need to be adjusted
#the step may need to be smaller when a larger map is drawn
if ( !( defined( $options{step} ) ) ) {
    if ( $global{length} < 1000 ) {
        $options{step} = 1;
    }
    elsif ( $global{length} < 10000 ) {
        $options{step} = 1;
    }
    elsif ( $global{length} < 100000 ) {
        $options{step} = 1;
    }
    elsif ( $global{length} < 1000000 ) {
        $options{step} = 10;
    }
    elsif ( $global{length} < 10000000 ) {
        $options{step} = 100;
    }
    else {
        $options{step} = 100;
    }

    #adjust based on map size
    if ( $options{size} eq 'x-large' ) {
        if ( $options{step} == 10 ) {
            $options{step} = 1;
        }
        elsif ( $options{step} == 100 ) {
            $options{step} = 10;
        }
    }

    if ( $options{size} eq 'large' ) {
        if ( $options{step} == 10 ) {
            $options{step} = 5;
        }
        elsif ( $options{step} == 100 ) {
            $options{step} = 50;
        }
    }

    if ( $options{size} eq 'large-v2' ) {
        if ( $options{step} == 10 ) {
            $options{step} = 5;
        }
        elsif ( $options{step} == 100 ) {
            $options{step} = 50;
        }
    }

}

#determine some global settings from sequence file
if ( ( $global{format} eq "embl" ) || ( $global{format} eq "genbank" ) ) {
    $global{"accession"} = $seqObject->accession_number;
    if ( !( defined( $options{title} ) ) ) {
        $options{title} = $seqObject->description();
    }
}
if ( $global{format} eq "fasta" ) {
    if ( !( defined( $options{title} ) ) ) {
        $options{title} = $seqObject->description();
    }
}

#try to determine topology from genbank or embl record
#if ( ( $global{format} eq "embl" ) || ( $global{format} eq "genbank" ) ) {
#    if ( $seqObject->is_circular ) {
#        $settings{isLinear} = "false";
#        $global{topology}   = "circular";
#    }
#    else {
#        $settings{isLinear} = "true";
#        $global{topology}   = "linear";
#    }
#}

#user-supplied values take precedence
if ( _isTrue( $options{linear} ) ) {
    $settings{isLinear} = "true";
    $global{topology}   = "linear";
}
elsif ( ( defined( $options{linear} ) ) && ( $options{linear} =~ m/f/i ) ) {
    $settings{isLinear} = "false";
    $global{topology}   = "circular";
}

#adjust settings based on size of map
_adjustSettingsBasedOnSize( $param{options}, $param{settings}, $param{global} );

#2011-06-26
#use the custom option to override map appearance settings
#2012-01-16
#check for extra options
my @extra_options = ();
foreach my $override ( @{ $options{custom} } ) {
    if ( $override =~ m/([a-zA-Z_]+)\=([\d\.A-Za-z\(\),]+)/ ) {
        my $key   = $1;
        my $value = $2;

        if ( defined( $settings{$key} ) ) {
            $settings{$key} = $value;
        }
        elsif ( exists( $options{$key} ) ) {
            $options{$key} = $value;
        }
        else {
            push( @extra_options, $key );
        }
    }
}
if (@extra_options) {
    foreach my $extra_option (@extra_options) {
        print "Unknown custom option: $extra_option\n";
    }
    die("Unknown custom options given!");
}

#2011_06_18
#parse labels_to_show file if one is provided
if ( defined( $options{labels_to_show} ) ) {
    $options{labels_to_show} = parse_labels_to_show( \%options );
}

#start building XML file
_writeHeader( \%options, \%settings, \%global );

#write title legend
if ( ( defined( $options{title} ) ) && ( $options{title} =~ m/\S/ ) ) {
    _writeTitleLegend( \%options, \%settings );
}

#write details legend
if ( _isTrue( $options{details} ) ) {
    _writeDetailsLegend( \%param );
}

#write features legend
if ( _isTrue( $options{legend} ) ) {
    _writeFeatureLegend( \%param, $seqObject );
}

#write legend for BLAST subjects
if (   ( _isTrue( $options{legend} ) )
    && ( defined( $settings{blast_heat_map} ) ) )
{
    _writeBlastSubjectLegend( \%param );
}

#forward strand features drawn on outside of backbone circle. Those appearing first in the XML are drawn closest to the backbone.
_message( \%options,
    "Creating XML for feature sets on the outside of the backbone circle." );
_message( \%options,
    "Feature sets written first are drawn closest to the backbone circle." );

#write starts and stops for three reading frames
if ( _isTrue( $options{reading_frames} ) ) {
    _message( \%options,
        "Creating XML for starts and stops that are in reading frame +1." );
    _writeStopsAndStarts( \%options, \%settings, $seqObject, 1, 1 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
    _message( \%options,
        "Creating XML for starts and stops that are in reading frame +2." );
    _writeStopsAndStarts( \%options, \%settings, $seqObject, 1, 2 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
    _message( \%options,
        "Creating XML for starts and stops that are in reading frame +3." );
    _writeStopsAndStarts( \%options, \%settings, $seqObject, 1, 3 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
}

#write ORFs for three reading frames
if ( _isTrue( $options{orfs} ) ) {
    _message( \%options,
        "Creating XML for ORFs that are in reading frame +1." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, 1, 1 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
    _message( \%options,
        "Creating XML for ORFs that are in reading frame +2." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, 1, 2 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
    _message( \%options,
        "Creating XML for ORFs that are in reading frame +3." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, 1, 3 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
}

#write combined orfs
if ( _isTrue( $options{combined_orfs} ) ) {
    _message( \%options, "Creating XML for ORFs that are on the plus strand." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, 1, "all" );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, 1, 0.25 );
    }
}

#write features in GenBank or EMBL file
if ( _isTrue( $options{show_sequence_features} ) ) {
    if ( ( defined( $global{format} ) ) && ( $global{format} eq "genbank" ) ) {
        _message( \%options,
"Creating XML for features in the GenBank file that are on the plus strand."
        );
        _writeGenBankGenes( \%options, \%settings, \%global, $seqObject, 1,
            undef );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, 1,
                0.25 );
        }
    }
    elsif ( ( defined( $global{format} ) ) && ( $global{format} eq "embl" ) ) {
        _message( \%options,
"Creating XML for features in the EMBL file that are on the plus strand."
        );
        _writeEmblGenes( \%options, \%settings, \%global, $seqObject, 1,
            undef );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, 1,
                0.25 );
        }
    }
}

#write features info from -genes files if available
if ( defined( $options{genes} ) ) {
    foreach ( reverse( @{ $options{genes} } ) ) {
        _message( \%options,
"Creating XML for features in the genes file $_ that are on the plus strand."
        );
        _writeGenes(
            \%options, \%settings, \%global, $seqObject,
            1,         undef,      undef,    undef,
            $_,        undef
        );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, 1,
                0.25 );
        }
    }
}

#write info from analysis file if available
#commented out on 2007-12-11 so that all analysis results shown on inside of backbone
#if (defined($options{"analysis"})) {
#    my @colors = @{$settings{'analysisColors'}};
#    foreach(reverse(@{$options{"analysis"}})) {
#        my $colorPos = shift(@colors);
#        my $colorNeg = shift(@colors);
#   push(@colors, $colorPos);
#   push(@colors, $colorNeg);
#        _message(\%options, "Creating XML for analysis values from the analysis file $_ that are on the plus strand.");
#        _writeGenes(\%options, \%settings, \%global, $seqObject, 1, undef, $colorPos, $colorNeg, $_, 1);
#        if (_isTrue($options{draw_divider_rings})) {
#            _drawDivider(\%options, \%settings, \%global, $seqObject, 1, 0.25);
#        }
#    }
#}

#Reverse strand features drawn on inside of backbone circle. Those appearing first
#in the XML are drawn closest to the backbone.
_message( \%options,
    "Creating XML for feature sets on the inside of the backbone circle." );
_message( \%options,
    "Feature sets written first are drawn closest to the backbone circle." );

#write starts and stops for three reading frames
if ( _isTrue( $options{reading_frames} ) ) {
    _message( \%options,
        "Creating XML for starts and stops that are in reading frame -1." );
    _writeStopsAndStarts( \%options, \%settings, $seqObject, -1, 1 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
    _message( \%options,
        "Creating XML for starts and stops that are in reading frame -2." );
    _writeStopsAndStarts( \%options, \%settings, $seqObject, -1, 2 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
    _message( \%options,
        "Creating XML for starts and stops that are in reading frame -3." );
    _writeStopsAndStarts( \%options, \%settings, $seqObject, -1, 3 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}

#write ORFs for three reading frames
if ( _isTrue( $options{orfs} ) ) {
    _message( \%options,
        "Creating XML for ORFs that are in reading frame -1." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, -1, 1 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
    _message( \%options,
        "Creating XML for ORFs that are in reading frame -2." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, -1, 2 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
    _message( \%options,
        "Creating XML for ORFs that are in reading frame -3." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, -1, 3 );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}

#write combined orfs
if ( _isTrue( $options{combined_orfs} ) ) {
    _message( \%options,
        "Creating XML for ORFs that are on the reverse strand." );
    _writeOrfs( \%options, \%settings, \%global, $seqObject, -1, "all" );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}

#write features in GenBank or EMBL file
if ( _isTrue( $options{show_sequence_features} ) ) {
    if ( ( defined( $global{format} ) ) && ( $global{format} eq "genbank" ) ) {
        _message( \%options,
"Creating XML for features in the GenBank file that are on the reverse strand."
        );
        _writeGenBankGenes( \%options, \%settings, \%global, $seqObject, -1,
            undef );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, -1,
                0.25 );
        }
    }
    elsif ( ( defined( $global{format} ) ) && ( $global{format} eq "embl" ) ) {
        _message( \%options,
"Creating XML for features in the EMBL file that are on the reverse strand."
        );
        _writeEmblGenes( \%options, \%settings, \%global, $seqObject, -1,
            undef );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, -1,
                0.25 );
        }
    }
}

#write features info from -genes files if available
if ( defined( $options{genes} ) ) {
    foreach ( reverse( @{ $options{genes} } ) ) {
        _message( \%options,
"Creating XML for features in the genes file $_ that are on the reverse strand."
        );
        _writeGenes(
            \%options, \%settings, \%global, $seqObject,
            -1,        undef,      undef,    undef,
            $_,        undef
        );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, -1,
                0.25 );
        }
    }
}

#write info from analysis file if available
if ( defined( $options{"analysis"} ) ) {
    my @colors = @{ $settings{'analysisColors'} };
    foreach ( reverse( @{ $options{"analysis"} } ) ) {
        my $colorPos = shift(@colors);
        my $colorNeg = shift(@colors);
        push( @colors, $colorPos );
        push( @colors, $colorNeg );
        _message( \%options,
            "Creating XML for analysis values from the analysis file $_." );
        _writeGenes(
            \%options, \%settings, \%global,  $seqObject,
            -1,        undef,      $colorPos, $colorNeg,
            $_,        1
        );
        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, -1,
                0.25 );
        }
    }
}

#write blast results
if ( defined( $options{"blast"} ) ) {
    my @colors       = @{ $settings{'blastColors'} };
    my $blast_number = 1;
    foreach ( @{ $options{"blast"} } ) {
        my $color = shift(@colors);
        push( @colors, $color );
        _message( \%options,
            "Creating XML for BLAST hits from the BLAST file $_." );
        _writeBlast( \%options, \%settings, \%global, $seqObject, -1, $_,
            $color, $settings{'featureThickness'}, undef );

        my $divider_color     = undef;    # use the default color
        my $divider_thickness = 0.25;

        # Set the divider color for every tenth ring
        if ( _isTrue( $options{blast_divider_ruler} ) ) {
            my $blast_mod = $blast_number % 10;

#$divider_color = $settings{'tickColor'} if ( ($blast_mod == 0) || ($blast_mod == 9) );
            if ( ( $blast_mod == 0 ) || ( $blast_mod == 9 ) ) {
                $divider_color = $settings{'blastRulerColor'};
                $divider_thickness *= 2;
            }
        }

        if ( _isTrue( $options{draw_divider_rings} ) ) {
            _drawDivider( \%options, \%settings, \%global, $seqObject, -1,
                $divider_thickness, $divider_color );
        }
        $blast_number++;
    }
}

#draw base content graphs.
if ( _isTrue( $options{'at_content'} ) ) {
    _message( \%options, "Creating XML for AT content information." );
    _writeBaseContent( \%options, \%settings, $seqObject, -1, 'at_content' );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}
if ( _isTrue( $options{'at_skew'} ) ) {
    _message( \%options, "Creating XML for AT skew information." );
    _writeBaseContent( \%options, \%settings, $seqObject, -1, 'at_skew' );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}
if ( _isTrue( $options{'gc_content'} ) ) {
    _message( \%options, "Creating XML for GC content information." );
    _writeBaseContent( \%options, \%settings, $seqObject, -1, 'gc_content' );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}
if ( _isTrue( $options{'gc_skew'} ) ) {
    _message( \%options, "Creating XML for GC skew information." );
    _writeBaseContent( \%options, \%settings, $seqObject, -1, 'gc_skew' );
    if ( _isTrue( $options{draw_divider_rings} ) ) {
        _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
    }
}

#2011-06-25 draw ring next to tick marks if one isn't present
if ( !( _isTrue( $options{draw_divider_rings} ) ) ) {
    _drawDivider( \%options, \%settings, \%global, $seqObject, -1, 0.25 );
}

#write footer
_writeFooter( \%options );

#give information about running CGView and improving XML
_message( \%options, "CGView XML file complete." );
_message( \%options, "The recommended CGView command is:" );
_message( \%options, "----------------------------------" );
_message( \%options,
    "java -jar -Xmx1500m cgview.jar -i $options{output} -o map.png" );
_message( \%options, "Success!" );

#write information about settings used so that users can modify using -custom option
my @appearance_keys = (
    'arrowheadLength',        'average',
    'atColorNeg',             'atColorPos',
    'at_content',             'atSkewColorNeg',
    'atSkewColorPos',         'at_skew',
    'backboneColor',          'backboneRadius',
    'backboneThickness',      'backgroundColor',
    'blast_divider_ruler',    'blastRulerColor',
    'borderColor',            'combined_orfs',
    'details',                'draw_divider_rings',
    'featureOpacity',         'featureOpacityOther',
    'featureSlotSpacing',     'featureThickness',
    'featureThicknessPlot',   'feature_labels',
    'gcColorNeg',             'gcColorPos',
    'gc_content',             'gcSkewColorNeg',
    'gcSkewColorPos',         'gc_skew',
    'gene_decoration',        'gene_labels',
    'global_label',           'height',
    'hit_labels',             'labelFontSize',
    'labelPlacementQuality',  'labelLineLength',
    'labelLineThickness',     'legend',
    'legendFontSize',         'orf_labels',
    'maxLabelLength',         'maxLegendLength',
    'maxTitleLength',         'minimumFeatureLength',
    'moveInnerLabelsToOuter', 'orfColor',
    'orfs',                   'orf_size',
    'otherColor',             'parse_reading_frame',
    'proteinColor',           'reading_frames',
    'rRNAColor',              'rulerFontColor',
    'rulerFontSize',          'rulerPadding',
    'scale',                  'scale_blast',
    'show_sequence_features', 'show_queries',
    'startColor',             'starts',
    'step',                   'stopColor',
    'stops',                  'title',
    'titleFontSize',          'tickColor',
    'tickLength',             'tickThickness',
    'tick_density',           'tRNAColor',
    'useInnerLabels',         'use_opacity',
    'width',                  'window',
    '_cct_blast_thickness',   '_cct_blast_opacity'
);
_message( \%options, "The following key-values were used:" );
_message( \%options, "-----------------------------------" );

foreach my $appearance_key (@appearance_keys) {
    if ( defined( $options{$appearance_key} ) ) {
        _message( \%options,
            $appearance_key . '=' . $options{$appearance_key} );
    }
    elsif ( defined( $settings{$appearance_key} ) ) {
        _message( \%options,
            $appearance_key . '=' . $settings{$appearance_key} );
    }
}
_message( \%options, "-----------------------------------" );

##############################

sub _createLog {
    my $file = shift;
    open( OUTFILE, ">" . $file ) or die("Cannot open file : $!");
    print(  OUTFILE "#Results of cgview_xml_builder.pl run started on "
          . _getTime()
          . ".\n" );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _getTime {
    my ( $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdst ) =
      localtime(time);
    $year += 1900;

    my @days = (
        'Sunday',   'Monday', 'Tuesday', 'Wednesday',
        'Thursday', 'Friday', 'Saturday'
    );
    my @months = (
        'January',   'February', 'March',    'April',
        'May',       'June',     'July',     'August',
        'September', 'October',  'November', 'December'
    );
    my $time =
        $days[$wday] . " "
      . $months[$mon] . " "
      . sprintf( "%02d", $mday ) . " "
      . sprintf( "%02d", $hour ) . ":"
      . sprintf( "%02d", $min ) . ":"
      . sprintf( "%02d", $sec ) . " "
      . sprintf( "%04d", $year );
    return $time;
}

sub _adjustSettingsBasedOnSize {
    my $options  = shift;
    my $settings = shift;
    my $global   = shift;

    if ( $options->{size} eq "small" ) {
        $settings->{width}                = "1000";
        $settings->{height}               = "1000";
        $settings->{featureSlotSpacing}   = "4";
        $settings->{backboneRadius}       = "300";
        $settings->{backboneThickness}    = "2";
        $settings->{featureThickness}     = "8";
        $settings->{featureThicknessPlot} = "15";
        $settings->{rulerFontSize}        = "8";
        $settings->{titleFontSize}        = "30";
        $settings->{labelFontSize}        = "10";
        $settings->{legendFontSize}       = "8";
        $settings->{maxTitleLength}       = "50";
        $settings->{maxLabelLength}       = "20";
        $settings->{maxLegendLength}      = "20";
        $settings->{plotLineThickness}    = "0.02";
        $settings->{labelLineLength}      = "60";
        $settings->{labelLineThickness}   = "1";
        $settings->{rulerPadding}         = "14";
        $settings->{tickThickness}        = "1";
        $settings->{arrowheadLength}      = "4";
        $settings->{minimumFeatureLength} = "0.2";
        $settings->{tickLength}           = "5";
    }
    elsif ( $options->{size} eq "medium" ) {
        $settings->{width}                = "3000";
        $settings->{height}               = "3000";
        $settings->{featureSlotSpacing}   = "6";
        $settings->{backboneRadius}       = "1000";
        $settings->{backboneThickness}    = "8";
        $settings->{featureThickness}     = "30";
        $settings->{featureThicknessPlot} = "80";
        $settings->{rulerFontSize}        = "30";
        $settings->{titleFontSize}        = "80";
        $settings->{labelFontSize}        = "15";
        $settings->{legendFontSize}       = "20";
        $settings->{maxTitleLength}       = "50";
        $settings->{maxLabelLength}       = "20";
        $settings->{maxLegendLength}      = "30";
        $settings->{plotLineThickness}    = "0.02";
        $settings->{labelLineLength}      = "200";
        $settings->{labelLineThickness}   = "4";
        $settings->{rulerPadding}         = "40";
        $settings->{tickThickness}        = "6";
        $settings->{arrowheadLength}      = "6";
        $settings->{minimumFeatureLength} = "0.2";
        $settings->{tickLength}           = "15";
        $options->{tick_density}          = $options->{tick_density} / 3.0;
    }

    #setting for the CGView server
    elsif ( $options->{size} eq "cgview_server_full" ) {
        $settings->{width}                  = "3000";
        $settings->{height}                 = "3000";
        $settings->{featureSlotSpacing}     = "1";
        $settings->{backboneRadius}         = "1000";
        $settings->{backboneThickness}      = "8";
        $settings->{featureThickness}       = "60";
        $settings->{featureThicknessPlot}   = "80";
        $settings->{rulerFontSize}          = "30";
        $settings->{titleFontSize}          = "80";
        $settings->{labelFontSize}          = "15";
        $settings->{legendFontSize}         = "20";
        $settings->{maxTitleLength}         = "90";
        $settings->{maxLabelLength}         = "20";
        $settings->{maxLegendLength}        = "30";
        $settings->{plotLineThickness}      = "0.02";
        $settings->{labelLineLength}        = "200";
        $settings->{labelLineThickness}     = "4";
        $settings->{rulerPadding}           = "40";
        $settings->{tickThickness}          = "6";
        $settings->{arrowheadLength}        = "10";
        $settings->{minimumFeatureLength}   = "0.5";    #was 0.2
        $settings->{tickLength}             = "15";
        $settings->{moveInnerLabelsToOuter} = "true";
        $options->{tick_density} = $options->{tick_density} / 3.0;

        #if using labels, make rings thinner for more label space
        #and don't allow plot slots to grow
        if ( _isTrue( $options->{feature_labels} ) ) {
            $settings->{backboneRadius}       = "900";
            $settings->{featureThickness}     = "50";
            $settings->{featureThicknessPlot} = "80";
            $settings->{autoPlotSlotIncrease} = 0;
        }
    }

    #setting for the CGView server
    elsif ( $options->{size} eq "cgview_server_zoom" ) {
        $settings->{width}                = "3000";
        $settings->{height}               = "3000";
        $settings->{featureSlotSpacing}   = "1";
        $settings->{backboneRadius}       = "1000";
        $settings->{backboneThickness}    = "16";
        $settings->{featureThickness}     = "260";
        $settings->{featureThicknessPlot} = "260";
        $settings->{rulerFontSize}        = "30";
        $settings->{titleFontSize}        = "80";
        $settings->{labelFontSize}        = "15";
        $settings->{legendFontSize}       = "20";
        $settings->{maxTitleLength}       = "90";
        $settings->{maxLabelLength}       = "20";
        $settings->{maxLegendLength}      = "30";
        $settings->{plotLineThickness}    = "0.02";
        $settings->{labelLineLength}      = "200";
        $settings->{labelLineThickness}   = "4";
        $settings->{rulerPadding}         = "40";
        $settings->{tickThickness}        = "6";
        $settings->{arrowheadLength}      = "10";
        $settings->{minimumFeatureLength} = "0.5";    #was 0.2
        $settings->{tickLength}           = "15";
        $options->{tick_density} = $options->{tick_density} / 3.0;
    }
    elsif ( $options->{size} eq "large" ) {
        $settings->{width}                 = "9000";
        $settings->{height}                = "9000";
        $settings->{featureSlotSpacing}    = "6";
        $settings->{backboneRadius}        = "3500";
        $settings->{backboneThickness}     = "8";
        $settings->{featureThickness}      = "60";
        $settings->{featureThicknessPlot}  = "120";
        $settings->{rulerFontSize}         = "60";
        $settings->{titleFontSize}         = "100";
        $settings->{labelFontSize}         = "40";
        $settings->{legendFontSize}        = "50";
        $settings->{maxTitleLength}        = "90";
        $settings->{maxLabelLength}        = "20";
        $settings->{maxLegendLength}       = "30";
        $settings->{plotLineThickness}     = "0.02";
        $settings->{labelLineLength}       = "200";
        $settings->{labelLineThickness}    = "4";
        $settings->{rulerPadding}          = "100";
        $settings->{tickThickness}         = "10";
        $settings->{arrowheadLength}       = "12";
        $settings->{minimumFeatureLength}  = "0.2";
        $settings->{tickLength}            = "25";
        $settings->{labelPlacementQuality} = "better";
        $options->{tick_density}           = $options->{tick_density} / 9.0;
    }
    elsif ( $options->{size} eq "large-v2" ) {
        $settings->{width}                 = "10000";
        $settings->{height}                = "10000";
        $settings->{featureSlotSpacing}    = "10";
        $settings->{backboneRadius}        = "4000";
        $settings->{backboneThickness}     = "40";
        $settings->{featureThickness}      = "220";
        $settings->{featureThicknessPlot}  = "700";
        $settings->{rulerFontSize}         = "130";
        $settings->{titleFontSize}         = "100";
        $settings->{labelFontSize}         = "130";
        $settings->{legendFontSize}        = "110";
        $settings->{maxTitleLength}        = "90";
        $settings->{maxLabelLength}        = "20";
        $settings->{maxLegendLength}       = "30";
        $settings->{plotLineThickness}     = "0.02";
        $settings->{labelLineLength}       = "450";
        $settings->{labelLineThickness}    = "12";
        $settings->{rulerPadding}          = "130";
        $settings->{tickThickness}         = "18";
        $settings->{arrowheadLength}       = "60";
        $settings->{minimumFeatureLength}  = "1.0";
        $settings->{tickLength}            = "45";
        $settings->{labelPlacementQuality} = "better";
        $settings->{featureOpacity}        = "0.9";
        $settings->{featureOpacityOther}   = "0.9";

        #custom colors for this size type
        $settings->{proteinColor}   = "rgb(55,126,184)";
        $settings->{tRNAColor}      = "rgb(228,26,28)";
        $settings->{rRNAColor}      = "rgb(204,204,0)";
        $settings->{otherColor}     = "rgb(153,153,153)";
        $settings->{orfColor}       = "rgb(255,127,1)";
        $settings->{gcColorPos}     = "rgb(166,86,40)";
        $settings->{gcColorNeg}     = "rgb(166,86,40)";
        $settings->{gcSkewColorPos} = "rgb(255,51,255)";
        $settings->{gcSkewColorNeg} = "rgb(153,51,255)";
        $settings->{backboneColor}  = "rgb(102,102,102)";
        $settings->{rulerFontColor} = "rgb(0,0,0)";
        $settings->{tickColor}      = "rgb(0,51,0)";
        $settings->{rulerFontColor} = "rgb(0,0,0)";
    }
    elsif ( $options->{size} eq "x-large" ) {
        $settings->{width}                 = "12000";
        $settings->{height}                = "12000";
        $settings->{featureSlotSpacing}    = "2";
        $settings->{backboneRadius}        = "4000";
        $settings->{featureThickness}      = "100";
        $settings->{featureThicknessPlot}  = "150";
        $settings->{rulerFontSize}         = "60";
        $settings->{titleFontSize}         = "80";
        $settings->{labelFontSize}         = "15";
        $settings->{legendFontSize}        = "60";
        $settings->{maxTitleLength}        = "90";
        $settings->{maxLabelLength}        = "20";
        $settings->{maxLegendLength}       = "30";
        $settings->{plotLineThickness}     = "0.02";
        $settings->{labelLineLength}       = "200";
        $settings->{labelLineThickness}    = "4";
        $settings->{rulerPadding}          = "40";
        $settings->{tickThickness}         = "5";
        $settings->{arrowheadLength}       = "6";
        $settings->{minimumFeatureLength}  = "0.2";
        $settings->{tickLength}            = "20";
        $settings->{labelPlacementQuality} = "better";
        $options->{tick_density}           = $options->{tick_density} / 12.0;
    }
    else {
        _message( $options, "-size setting $options->{size} not recognized." );
        die("-size setting $options->{size} not recognized");
    }

    #override the fontsize settings if -font_size specified
    #these are for the CGView server
    if (   ( defined( $options->{font_size} ) )
        && ( $options->{size} eq "cgview_server_zoom" ) )
    {
        if ( $options->{font_size} eq 'xx-small' ) {
            $settings->{rulerFontSize}      = '8';
            $settings->{titleFontSize}      = '50';
            $settings->{labelFontSize}      = '8';
            $settings->{legendFontSize}     = '12';
            $settings->{labelLineLength}    = "50";
            $settings->{labelLineThickness} = "1";
        }
        elsif ( $options->{font_size} eq 'x-small' ) {
            $settings->{rulerFontSize}      = '10';
            $settings->{titleFontSize}      = '60';
            $settings->{labelFontSize}      = '10';
            $settings->{legendFontSize}     = '16';
            $settings->{labelLineLength}    = "100";
            $settings->{labelLineThickness} = "2";
        }
        elsif ( $options->{font_size} eq 'small' ) {
            $settings->{rulerFontSize}      = '12';
            $settings->{titleFontSize}      = '70';
            $settings->{labelFontSize}      = '12';
            $settings->{legendFontSize}     = '18';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "2";
        }
        elsif ( $options->{font_size} eq 'medium' ) {
            $settings->{rulerFontSize}      = '15';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '15';
            $settings->{legendFontSize}     = '20';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "2";
        }
        elsif ( $options->{font_size} eq 'large' ) {
            $settings->{rulerFontSize}      = '20';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '20';
            $settings->{legendFontSize}     = '22';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "4";
        }
        elsif ( $options->{font_size} eq 'x-large' ) {
            $settings->{rulerFontSize}      = '32';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '32';
            $settings->{legendFontSize}     = '24';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "4";
        }
        elsif ( $options->{font_size} eq 'xx-large' ) {
            $settings->{rulerFontSize}      = '36';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '36';
            $settings->{legendFontSize}     = '30';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "4";
        }
    }

    #these are for the CGView server
    if (   ( defined( $options->{font_size} ) )
        && ( $options->{size} eq "cgview_server_full" ) )
    {
        if ( $options->{font_size} eq 'xx-small' ) {
            $settings->{rulerFontSize}      = '8';
            $settings->{titleFontSize}      = '50';
            $settings->{labelFontSize}      = '8';
            $settings->{legendFontSize}     = '12';
            $settings->{labelLineLength}    = "50";
            $settings->{labelLineThickness} = "1";
        }
        elsif ( $options->{font_size} eq 'x-small' ) {
            $settings->{rulerFontSize}      = '10';
            $settings->{titleFontSize}      = '60';
            $settings->{labelFontSize}      = '10';
            $settings->{legendFontSize}     = '16';
            $settings->{labelLineLength}    = "100";
            $settings->{labelLineThickness} = "2";
        }
        elsif ( $options->{font_size} eq 'small' ) {
            $settings->{rulerFontSize}      = '12';
            $settings->{titleFontSize}      = '70';
            $settings->{labelFontSize}      = '12';
            $settings->{legendFontSize}     = '18';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "2";
        }
        elsif ( $options->{font_size} eq 'medium' ) {
            $settings->{rulerFontSize}      = '15';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '15';
            $settings->{legendFontSize}     = '20';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "2";
        }
        elsif ( $options->{font_size} eq 'large' ) {
            $settings->{rulerFontSize}      = '16';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '20';
            $settings->{legendFontSize}     = '22';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "4";
        }
        elsif ( $options->{font_size} eq 'x-large' ) {
            $settings->{rulerFontSize}      = '17';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '32';
            $settings->{legendFontSize}     = '24';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "4";
        }
        elsif ( $options->{font_size} eq 'xx-large' ) {
            $settings->{rulerFontSize}      = '18';
            $settings->{titleFontSize}      = '80';
            $settings->{labelFontSize}      = '36';
            $settings->{legendFontSize}     = '30';
            $settings->{labelLineLength}    = "150";
            $settings->{labelLineThickness} = "4";
        }

    }

    #count the number of featureSlots
    my $plotSlotsOuter  = 0;
    my $plotSlotsInner  = 0;
    my $otherSlotsOuter = 0;
    my $otherSlotsInner = 0;

    #non plot slots such as genes
    if (   ( $global->{format} eq "embl" )
        || ( $global->{format} eq "genbank" ) )
    {
        if ( $options->{show_sequence_features} ) {
            $otherSlotsOuter++;
            $otherSlotsInner++;
        }
    }
    if ( _isTrue( $options->{reading_frames} ) ) {
        $otherSlotsOuter = $otherSlotsOuter + 3;
        $otherSlotsInner = $otherSlotsInner + 3;
    }
    if ( _isTrue( $options->{orfs} ) ) {
        $otherSlotsOuter = $otherSlotsOuter + 3;
        $otherSlotsInner = $otherSlotsInner + 3;
    }
    if ( _isTrue( $options->{combined_orfs} ) ) {
        $otherSlotsOuter = $otherSlotsOuter + 3;
        $otherSlotsInner = $otherSlotsInner + 3;
    }
    if ( defined( $options->{genes} ) ) {
        $otherSlotsOuter = $otherSlotsOuter + scalar( @{ $options->{genes} } );
        $otherSlotsInner = $otherSlotsInner + scalar( @{ $options->{genes} } );
    }
    if ( defined( $options->{analysis} ) ) {

     #$plotSlotsOuter = $plotSlotsOuter + 2 * (scalar(@{$options->{analysis}}));
        $plotSlotsInner =
          $plotSlotsInner + 2 * ( scalar( @{ $options->{analysis} } ) );
    }

  #This estimation could be improved, since maps can now show some blast results
  #in a single track and others in six tracks, depending on whether reading
  #frame information is read by _parseBLAST
    if ( defined( $options->{blast} ) ) {
        if ( _isTrue( $options->{parse_reading_frame} ) ) {
            foreach ( @{ $options->{blast} } ) {
                if (
                    _containsReadingFrameInfo(
                        $options, $settings, $global, $_
                    )
                  )
                {
                    $otherSlotsInner = $otherSlotsInner + 6;
                }
                else {
                    $otherSlotsInner = $otherSlotsInner + 1;
                }
            }
        }
        else {
            $otherSlotsInner =
              $otherSlotsInner + scalar( @{ $options->{blast} } );
        }
    }

    #plot slots such as gc skew
    if ( _isTrue( $options->{at_content} ) ) {
        $plotSlotsInner++;
    }
    if ( _isTrue( $options->{at_skew} ) ) {
        $plotSlotsInner++;
    }
    if ( _isTrue( $options->{gc_content} ) ) {
        $plotSlotsInner++;
    }
    if ( _isTrue( $options->{gc_skew} ) ) {
        $plotSlotsInner++;
    }

    #adjust featureSlotThickness based on number of slots used
    #want plotSlots to be six times wider than other slots
    my $availableSpace =
      $settings->{backboneRadius} * 0.60 -
      $settings->{featureSlotSpacing} *
      ( $plotSlotsInner +
          $plotSlotsOuter +
          $otherSlotsInner +
          $otherSlotsOuter -
          1 );
    my $slotUnits =
      6.0 * ( $plotSlotsInner + $plotSlotsOuter ) +
      $otherSlotsInner +
      $otherSlotsOuter;

    if ( $slotUnits == 0 ) {
        $slotUnits++;
    }

    my $slotWidths = $availableSpace / $slotUnits;

    #changed 2007-01-14 so that feature widths don't get too big
    #$settings->{featureThickness} = sprintf("%.2f", $slotWidths);
    if ( sprintf( "%.2f", $slotWidths ) < $settings->{featureThickness} ) {
        $settings->{featureThickness} = sprintf( "%.2f", $slotWidths );
    }

    if (
        sprintf( "%.2f", $slotWidths * 6.0 ) <
        $settings->{featureThicknessPlot} )
    {
        $settings->{featureThicknessPlot} =
          sprintf( "%.2f", $slotWidths * 6.0 );
    }
    else {
        if ( $settings->{autoPlotSlotIncrease} ) {
            $settings->{featureThicknessPlot} =
              sprintf( "%.2f", $slotWidths * 6.0 );
        }
    }

    #check condensed setting.
    if ( _isTrue( $options->{condensed} ) ) {
        my $newWidth;
        if ( $options->{size} eq "small" ) {
            $newWidth = 8;
        }
        if ( $options->{size} eq "medium" ) {
            $newWidth = 10;
        }
        if ( $options->{size} eq "large" ) {
            $newWidth = 10;
        }
        if ( $options->{size} eq "x-large" ) {
            $newWidth = 10;
        }

        if ( $newWidth < $settings->{featureThickness} ) {
            $settings->{featureThickness}  = $newWidth;
            $settings->{backboneThickness} = "2";
        }
        if ( ( $newWidth * 2 ) < $settings->{featureThicknessPlot} ) {
            $settings->{featureThicknessPlot} = $newWidth * 2;
            $settings->{backboneThickness}    = "2";
        }
    }

    #to avoid any auto-adjusting redefine settings here
    if ( $options->{cct} == 1 ) {
        if ( $options->{size} eq "x-large" ) {
            $settings->{width}              = "12000";
            $settings->{height}             = "12000";
            $settings->{featureSlotSpacing} = "1";
            $settings->{backboneRadius}     = "4000";
            $settings->{backboneThickness}  = "10";
            $settings->{featureThickness}   = "60";
            $settings->{featureThicknessPlot} =
              "300";    #changed from 60 to 300 2011-02-16
            $settings->{rulerFontSize}          = "60";
            $settings->{titleFontSize}          = "200";
            $settings->{labelFontSize}          = "15";
            $settings->{legendFontSize}         = "40";
            $settings->{maxTitleLength}         = "90";
            $settings->{maxLabelLength}         = "20";
            $settings->{maxLegendLength}        = "30";
            $settings->{plotLineThickness}      = "0.02";
            $settings->{labelLineLength}        = "200";
            $settings->{labelLineThickness}     = "4";
            $settings->{rulerPadding}           = "40";
            $settings->{tickThickness}          = "5";
            $settings->{arrowheadLength}        = "6";
            $settings->{minimumFeatureLength}   = "0.2";
            $settings->{tickLength}             = "20";
            $settings->{labelPlacementQuality}  = "best";
            $settings->{moveInnerLabelsToOuter} = "true";
            $settings->{useInnerLabels}         = "false";
            $settings->{_cct_blast_thickness}   = "20";
            $settings->{_cct_blast_opacity}     = "1.0";
        }
        elsif ( $options->{size} eq "large" ) {
            $settings->{width}              = "9000";
            $settings->{height}             = "9000";
            $settings->{featureSlotSpacing} = "2";
            $settings->{backboneRadius}     = "3500";
            $settings->{backboneThickness}  = "6";
            $settings->{featureThickness}   = "60";
            $settings->{featureThicknessPlot} =
              "500";    #changed from 120 to 500 2011-02-16
            $settings->{rulerFontSize}          = "60";
            $settings->{titleFontSize}          = "100";
            $settings->{labelFontSize}          = "40";
            $settings->{legendFontSize}         = "30";
            $settings->{maxTitleLength}         = "90";
            $settings->{maxLabelLength}         = "20";
            $settings->{maxLegendLength}        = "30";
            $settings->{plotLineThickness}      = "0.02";
            $settings->{labelLineLength}        = "200";
            $settings->{labelLineThickness}     = "4";
            $settings->{rulerPadding}           = "100";
            $settings->{tickThickness}          = "10";
            $settings->{arrowheadLength}        = "12";
            $settings->{minimumFeatureLength}   = "0.2";
            $settings->{tickLength}             = "25";
            $settings->{labelPlacementQuality}  = "best";
            $settings->{moveInnerLabelsToOuter} = "true";
            $settings->{useInnerLabels}         = "false";
            $settings->{_cct_blast_thickness}   = "10";
            $settings->{_cct_blast_opacity}     = "1.0";
        }
        elsif ( $options->{size} eq "medium" ) {
            $settings->{width}                  = "3000";
            $settings->{height}                 = "3000";
            $settings->{featureSlotSpacing}     = "1";
            $settings->{backboneRadius}         = "1000";
            $settings->{backboneThickness}      = "4";
            $settings->{featureThickness}       = "30";
            $settings->{featureThicknessPlot}   = "80";
            $settings->{rulerFontSize}          = "30";
            $settings->{titleFontSize}          = "80";
            $settings->{labelFontSize}          = "15";
            $settings->{legendFontSize}         = "15";
            $settings->{maxTitleLength}         = "50";
            $settings->{maxLabelLength}         = "20";
            $settings->{maxLegendLength}        = "30";
            $settings->{plotLineThickness}      = "0.02";
            $settings->{labelLineLength}        = "200";
            $settings->{labelLineThickness}     = "4";
            $settings->{rulerPadding}           = "40";
            $settings->{tickThickness}          = "6";
            $settings->{arrowheadLength}        = "6";
            $settings->{minimumFeatureLength}   = "0.2";
            $settings->{tickLength}             = "15";
            $settings->{labelPlacementQuality}  = "best";
            $settings->{moveInnerLabelsToOuter} = "true";
            $settings->{useInnerLabels}         = "false";
            $settings->{_cct_blast_thickness}   = "4.5";
            $settings->{_cct_blast_opacity}     = "1.0";
        }

        #this is heuristic approach to adjusting widths of blast slots
        my $max_blast_comparisons    = 100;
        my $actual_blast_comparisons = scalar( @{ $options->{blast} } );
        my $blast_width_multiplier;
        if (   ( defined($actual_blast_comparisons) )
            && ( $actual_blast_comparisons > 0 ) )
        {
            $blast_width_multiplier =
              $max_blast_comparisons / $actual_blast_comparisons;
        }
        else {
            $blast_width_multiplier = 1;
        }

        $settings->{_cct_blast_thickness} = sprintf( "%.5f",
            ( $settings->{_cct_blast_thickness} * $blast_width_multiplier ) );
    }
}

sub _message {
    my $options = shift;
    my $message = shift;

    if ( _isTrue( $options->{verbose} ) ) {
        print "$message\n";
    }

    if ( defined( $options->{log} ) ) {
        _writeLog( $options->{log}, "$message\n" );
    }
}

sub _isTrue {
    my $string = shift;
    if ( ( defined($string) ) && ( $string =~ m/t/i ) ) {
        return 1;
    }
    return 0;
}

sub _writeLog {
    my $file    = shift;
    my $message = shift;
    open( OUTFILE, "+>>" . $file ) or die("Cannot open file : $!");
    print( OUTFILE $message );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _getSeqObject {
    my $param = shift;
    my $file  = $param->{options}->{sequence};

    open( INFILE, $file ) or die("Cannot open input file: $!");

    while ( my $line = <INFILE> ) {
        if ( !( $line =~ m/\S/ ) ) {
            next;
        }

        #guess file format from first line
        if ( $line =~ m/^LOCUS\s+/ ) {
            $param->{global}->{format} = "genbank";
        }
        elsif ( $line =~ m/^ID\s+/ ) {
            $param->{global}->{format} = "embl";
        }
        elsif ( $line =~ m/^>/ ) {
            $param->{global}->{format} = "fasta";
        }
        else {
            $param->{global}->{format} = "raw";
        }
        last;
    }

    close(INFILE) or die("Cannot close input file: $!");

    #get seqobj
    my $in = Bio::SeqIO->new(
        -format => $param->{global}->{format},
        -file   => $file
    );

    my @seqs = ();

    my $start = 1;

    while ( my $seq = $in->next_seq() ) {

        my $contig_start = $start;
        my $contig_end   = $start + $seq->length() - 1;

        my %contig = (
            start => $contig_start,
            end   => $contig_end
        );

        push( @{ $param->{global}->{contigs} }, \%contig );

        push( @seqs, $seq );

        $start = $contig_end + 1;
    }

    #merge multi-contig sequences
    Bio::SeqUtils->cat(@seqs);

    return $seqs[0];

}

sub _writeHeader {
    my $options  = shift;
    my $settings = shift;
    my $global   = shift;

    my $header =
"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n<cgview backboneRadius=\"$settings->{backboneRadius}\" backboneColor=\"$settings->{backboneColor}\" backboneThickness=\"$settings->{backboneThickness}\" featureSlotSpacing=\"$settings->{featureSlotSpacing}\" labelLineLength=\"$settings->{labelLineLength}\" labelPlacementQuality=\"$settings->{labelPlacementQuality}\" labelLineThickness=\"$settings->{labelLineThickness}\" rulerPadding=\"$settings->{rulerPadding}\" tickThickness=\"$settings->{tickThickness}\" shortTickThickness=\"$settings->{tickThickness}\" arrowheadLength=\"$settings->{arrowheadLength}\" rulerFont=\"SansSerif, plain, $settings->{rulerFontSize}\" rulerFontColor=\"$settings->{rulerFontColor}\" labelFont=\"SansSerif, plain, $settings->{labelFontSize}\" isLinear=\"$settings->{isLinear}\" minimumFeatureLength=\"$settings->{minimumFeatureLength}\" sequenceLength=\"$global->{length}\" height=\"$settings->{height}\" width=\"$settings->{width}\" globalLabel=\"$options->{global_label}\" moveInnerLabelsToOuter=\"$settings->{moveInnerLabelsToOuter}\" featureThickness=\"$settings->{featureThickness}\" tickLength=\"$settings->{tickLength}\" useInnerLabels=\"$settings->{useInnerLabels}\" shortTickColor=\"$settings->{tickColor}\" longTickColor=\"$settings->{tickColor}\" zeroTickColor=\"$settings->{tickColor}\" showBorder=\"$settings->{showBorder}\" borderColor=\"$settings->{borderColor}\" backgroundColor=\"$settings->{backgroundColor}\" tickDensity=\"$options->{tick_density}\">\n";

    open( OUTFILE, ">$options->{output}" ) or die("Cannot open file : $!");
    print( OUTFILE $header );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _writeTitleLegend {
    my $options  = shift;
    my $settings = shift;
    my $title    = $options->{title};
    $title =~ s/[\.\,]//g;
    if ( length($title) > $settings->{maxTitleLength} - 3 ) {
        if ( $options->{verbose} ) {
            _message( $options,
"The sequence title was shortened because it is longer than $settings->{maxTitleLength} characters."
            );
        }
        $title = substr( $title, 0, $settings->{maxTitleLength} - 3 ) . "...";
    }
    $title = _escapeText($title);

    my $legend =
"<legend position=\"lower-center\" backgroundOpacity=\"0.8\">\n<legendItem textAlignment=\"center\" font=\"SansSerif, plain, $settings->{titleFontSize}\" text=\"$title\" />\n</legend>\n";

    open( OUTFILE, "+>>" . $options->{output} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE "<!-- title -->\n" );
    print( OUTFILE $legend );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _escapeText {
    my $text = shift;
    if ( !defined($text) ) {
        return undef;
    }
    $text =~ s/&/&amp;/g;
    $text =~ s/\'/&apos;/g;
    $text =~ s/\"/&quot;/g;
    $text =~ s/</&lt;/g;
    $text =~ s/>/&gt;/g;
    return $text;
}

sub _writeDetailsLegend {
    my $param = shift;

    my $accession = _escapeText( $param->{global}->{accession} );
    my $length    = _escapeText( $param->{global}->{length} );
    my $topology  = _escapeText( $param->{global}->{topology} );

    my $legend =
"<legend position=\"upper-left\" font=\"SansSerif, plain, $param->{settings}->{titleFontSize}\" backgroundOpacity=\"0.8\">\n";
    if ( defined($accession) ) {
        $legend = $legend . "<legendItem text=\"Accession: $accession\" />\n";
    }

    while ( $length =~ s/^(-?\d+)(\d\d\d)/$1,$2/ ) {
        1;
    }
    $length = $length . " bp";
    $legend = $legend . "<legendItem text=\"Length: $length\" />\n";

    if ( $topology =~ m/linear/i ) {
        $legend =
          $legend . "<legendItem text=\"" . "Topology: $topology\" />\n";
    }

    $legend = $legend . "</legend>\n";

    open( OUTFILE, "+>>" . $param->{options}->{output} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE "<!-- details legend -->\n" );
    print( OUTFILE $legend );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _writeBlastSubjectLegend {
    my $param    = shift;
    my $options  = $param->{options};
    my $settings = $param->{settings};
    my $global   = $param->{global};

    if (   ( !defined( $options->{'blast'} ) )
        || ( scalar( @{ $options->{'blast'} } ) == 0 ) )
    {
        return;
    }

    my $legend =
"<legend position=\"lower-left\" textAlignment=\"left\" backgroundOpacity=\"0.8\" font=\"SansSerif, plain, "
      . $settings->{'legendFontSize'} . "\">\n";

    my $blast_ring = 1;
    foreach ( @{ $options->{'blast'} } ) {
        my $title = "BLAST " . _createLegendName($_);
        if ( _isTrue( $options->{blast_divider_ruler} ) ) {
            $title = $blast_ring . ": " . $title;
        }
        $legend =
          $legend . "<legendItem text=\"$title\" drawSwatch=\"false\" />\n";
        $blast_ring++;
    }

    $legend = $legend . "</legend>\n";

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE "<!-- legend -->\n" );
    print( OUTFILE $legend );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _writeFeatureLegend {
    my $param     = shift;
    my $options   = $param->{options};
    my $settings  = $param->{settings};
    my $global    = $param->{global};
    my $seqObject = shift;

#check -genes files to see if they contain COG information, or features such as 'other' 'tRNA' 'rRNA'
    my $hasCogs   = 0;
    my $hasOther  = 0;
    my $hastRNA   = 0;
    my $hasrRNA   = 0;
    my $hasCoding = 0;
    if ( defined( $options->{genes} ) ) {
        foreach ( @{ $options->{genes} } ) {
            my @features =
              @{ _parseGFF( $_, $options, $settings, $global, $seqObject ) };

            foreach (@features) {
                my $feat = $_;
                my $type = lc( $feat->{'feature'} );

                if ( $type eq "cds" ) {
                    $hasCoding = 1;
                }
                elsif ( $type eq "rrna" ) {
                    $hasrRNA = 1;
                }
                elsif ( $type eq "trna" ) {
                    $hastRNA = 1;
                }
                elsif ( $type eq "other" ) {
                    $hasOther = 1;
                } #it is possible that there are COGs but all of the multiple category type, e.g. 'AJ'
                elsif ( defined( $settings->{'cogColors'}->{ uc($type) } ) ) {
                    $hasCogs = 1;
                }
            }
        }
    }

    my $legend =
"<legend position=\"upper-right\" textAlignment=\"left\" backgroundOpacity=\"0.8\" font=\"SansSerif, plain, "
      . $settings->{'legendFontSize'} . "\">\n";

    #legend for COGs if genes file supplied and COGs were found
    if ($hasCogs) {
        if ( defined( $options->{'genes'} ) ) {
            my @cogs = keys( %{ $settings->{'cogColors'} } );
            foreach (@cogs) {
                $legend =
                    $legend
                  . "<legendItem text=\""
                  . $_ . " COG"
                  . "\" drawSwatch=\"true\" swatchOpacity=\""
                  . $settings->{'featureOpacity'}
                  . "\" swatchColor=\""
                  . $settings->{'cogColors'}->{$_}
                  . "\" />\n";
            }
        }
    }

    #protein encoding genes legend
    if (
        (
            (
                   ( $global->{'format'} eq "genbank" )
                || ( $global->{'format'} eq "embl" )
            )
        )
        || ($hasCoding)
      )
    {
        $legend =
            $legend
          . "<legendItem text=\"CDS\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacity'}
          . "\" swatchColor=\""
          . $settings->{'proteinColor'}
          . "\" />\n";
    }

    if ( !( defined( $global->{'format'} ) ) ) {
        die(
"_writeFeatureLegend requires that the sequence format be set by _getSeqObject."
        );
    }

#legend for tRNA, rRNA and other genes if input file is GenBank or EMBL, or if these features were found in the -genes files
    if (
        (
            (
                (
                       ( $global->{'format'} eq "genbank" )
                    || ( $global->{'format'} eq "embl" )
                )
            )
            && ( $options->{show_sequence_features} )
        )
        || ($hastRNA)
      )
    {
        $legend =
            $legend
          . "<legendItem text=\"tRNA\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacity'}
          . "\" swatchColor=\""
          . $settings->{'tRNAColor'}
          . "\" />\n";
    }
    if (
        (
            (
                (
                       ( $global->{'format'} eq "genbank" )
                    || ( $global->{'format'} eq "embl" )
                )
            )
            && ( $options->{show_sequence_features} )
        )
        || ($hasrRNA)
      )
    {
        $legend =
            $legend
          . "<legendItem text=\"rRNA\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacity'}
          . "\" swatchColor=\""
          . $settings->{'rRNAColor'}
          . "\" />\n";
    }
    if (
        (
            (
                (
                       ( $global->{'format'} eq "genbank" )
                    || ( $global->{'format'} eq "embl" )
                )
            )
            && ( $options->{show_sequence_features} )
        )
        || ($hasOther)
      )
    {
        $legend =
            $legend
          . "<legendItem text=\"Other\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacityOther'}
          . "\" swatchColor=\""
          . $settings->{'otherColor'}
          . "\" />\n";
    }

    #orfs legend
    if (   ( _isTrue( $options->{'orfs'} ) )
        || ( _isTrue( $options->{'combined_orfs'} ) ) )
    {
        $legend =
            $legend
          . "<legendItem text=\"ORF\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacity'}
          . "\" swatchColor=\""
          . $settings->{'orfColor'}
          . "\" />\n";
    }

    #reading frames legend
    if ( _isTrue( $options->{'reading_frames'} ) ) {
        $legend =
            $legend
          . "<legendItem text=\"Start\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacity'}
          . "\" swatchColor=\""
          . $settings->{'startColor'}
          . "\" />\n";
        $legend =
            $legend
          . "<legendItem text=\"Stop\" drawSwatch=\"true\" swatchOpacity=\""
          . $settings->{'featureOpacity'}
          . "\" swatchColor=\""
          . $settings->{'stopColor'}
          . "\" />\n";
    }

    #analysis legends
    if ( defined( $options->{'analysis'} ) ) {
        my @colors    = @{ $settings->{'analysisColors'} };
        my @color_set = ();
        foreach ( @{ $options->{'analysis'} } ) {
            my $colorPos = shift(@colors);
            my $colorNeg = shift(@colors);
            push( @color_set, $colorPos );
            push( @color_set, $colorNeg );
        }
        @color_set = reverse(@color_set);

        foreach ( @{ $options->{'analysis'} } ) {
            my $title    = _createLegendName($_);
            my $colorNeg = shift(@color_set);
            my $colorPos = shift(@color_set);
            $legend =
                $legend
              . "<legendItem text=\"$title+\" drawSwatch=\"true\" swatchOpacity=\""
              . $settings->{'featureOpacity'}
              . "\" swatchColor=\""
              . $colorPos
              . "\" />\n";
            $legend =
                $legend
              . "<legendItem text=\"$title-\" drawSwatch=\"true\" swatchOpacity=\""
              . $settings->{'featureOpacity'}
              . "\" swatchColor=\""
              . $colorNeg
              . "\" />\n";
        }
    }

    #blast legends
    if ( defined( $options->{'blast'} ) ) {
        my @colors = @{ $settings->{'blastColors'} };
        if ( defined( $settings->{blast_heat_map} ) ) {
            my @heat_map_keys = keys( %{ $settings->{blast_heat_map} } );
            foreach my $cutoff (@heat_map_keys) {
                my $op;
                if ( $cutoff == 100 ) {
                    $op = '=';
                }
                else {
                    $op = '>=';
                }
                $legend =
                    $legend
                  . "<legendItem text=\"BLAST hit $op $cutoff \% identical\" drawSwatch=\"true\" swatchOpacity=\""
                  . $settings->{_cct_blast_opacity}
                  . "\" swatchColor=\""
                  . $settings->{blast_heat_map}->{$cutoff}
                  . "\" />\n";
            }
        }
        else {
            my $blast_ring = 1;
            foreach ( @{ $options->{'blast'} } ) {
                my $title = "BLAST " . _createLegendName($_);
                if ( _isTrue( $options->{blast_divider_ruler} ) ) {
                    $title = $blast_ring . ": " . $title;
                }
                my $color = shift(@colors);
                push( @colors, $color );
                if ( defined( $settings->{_cct_blast_opacity} ) ) {
                    $legend =
                        $legend
                      . "<legendItem text=\"$title\" drawSwatch=\"true\" swatchOpacity=\""
                      . $settings->{_cct_blast_opacity}
                      . "\" swatchColor=\""
                      . $color
                      . "\" />\n";
                }
                else {
                    if ( _isTrue( $options->{use_opacity} ) ) {
                        $legend =
                            $legend
                          . "<legendItem text=\"$title\" drawSwatch=\"true\" swatchOpacity=\""
                          . '0.2'
                          . "\" swatchColor=\""
                          . $color
                          . "\" />\n";
                    }
                    else {
                        $legend =
                            $legend
                          . "<legendItem text=\"$title\" drawSwatch=\"true\" swatchOpacity=\""
                          . '1.0'
                          . "\" swatchColor=\""
                          . $color
                          . "\" />\n";
                    }
                }
                $blast_ring++;
            }
        }
    }

    #legend for various graphs
    #at
    if ( _isTrue( $options->{'at_content'} ) ) {
        if ( $settings->{'atColorPos'} eq $settings->{'atColorNeg'} ) {
            $legend =
                $legend
              . "<legendItem text=\"AT content\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'atColorPos'}
              . "\" />\n";
        }
        else {
            $legend =
                $legend
              . "<legendItem text=\"AT content+\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'atColorPos'}
              . "\" />\n";
            $legend =
                $legend
              . "<legendItem text=\"AT content-\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'atColorNeg'}
              . "\" />\n";
        }
    }

    #at_skew
    if ( _isTrue( $options->{'at_skew'} ) ) {
        if ( $settings->{'atSkewColorPos'} eq $settings->{'atSkewColorNeg'} ) {
            $legend =
                $legend
              . "<legendItem text=\"AT skew\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'atSkewColorPos'}
              . "\" />\n";
        }
        else {
            $legend =
                $legend
              . "<legendItem text=\"AT skew+\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'atSkewColorPos'}
              . "\" />\n";
            $legend =
                $legend
              . "<legendItem text=\"AT skew-\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'atSkewColorNeg'}
              . "\" />\n";
        }
    }

    #gc
    if ( _isTrue( $options->{'gc_content'} ) ) {
        if ( $settings->{'gcColorPos'} eq $settings->{'gcColorNeg'} ) {
            $legend =
                $legend
              . "<legendItem text=\"GC content\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'gcColorPos'}
              . "\" />\n";
        }
        else {
            $legend =
                $legend
              . "<legendItem text=\"GC content+\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'gcColorPos'}
              . "\" />\n";
            $legend =
                $legend
              . "<legendItem text=\"GC content-\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'gcColorNeg'}
              . "\" />\n";
        }
    }

    #gc_skew
    if ( _isTrue( $options->{'gc_skew'} ) ) {
        if ( $settings->{'gcSkewColorPos'} eq $settings->{'gcSkewColorNeg'} ) {
            $legend =
                $legend
              . "<legendItem text=\"GC skew\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'gcSkewColorPos'}
              . "\" />\n";
        }
        else {
            $legend =
                $legend
              . "<legendItem text=\"GC skew+\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'gcSkewColorPos'}
              . "\" />\n";
            $legend =
                $legend
              . "<legendItem text=\"GC skew-\" drawSwatch=\"true\" swatchColor=\""
              . $settings->{'gcSkewColorNeg'}
              . "\" />\n";
        }
    }

    $legend = $legend . "</legend>\n";

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE "<!-- legend -->\n" );
    print( OUTFILE $legend );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _createLegendName {
    my $file = shift;
    my $name;
    if ( $file =~ m/\/([^\/]+)$/ ) {
        $name = $1;

        #remove extension
        $name =~ s/\.[^\.]*$//;
        $name =~ s/_/ /g;

        #add underscores back to NCBI accession numbers (NM_181029 for example).
        $name =~ s/([A-Z][A-Z])\s(\d{4,})/$1_$2/g;
    }
    if ( defined($name) ) {
        return $name;
    }
    return $file;
}

sub _writeStopsAndStarts {
    my $options   = shift;
    my $settings  = shift;
    my $seqObject = shift;
    my $strand    = shift;    #1 or -1
    my $rf        = shift;    #1,2, or 3

    my $opacity     = $settings->{'featureOpacity'};
    my $startCodons = $options->{'starts'};
    my $stopCodons  = $options->{'stops'};

    my @outputArray = ();
    if ( $strand == 1 ) {
        push( @outputArray,
            "<featureSlot showShading=\"false\" strand=\"direct\">\n" );
    }
    else {
        push( @outputArray,
            "<featureSlot showShading=\"false\" strand=\"reverse\">\n" );
    }

    #add start and stop codons
    my $dna;
    if ( $strand == 1 ) {

        if ( $rf == 1 ) {
            $dna = substr( $seqObject->seq(), 0 );
        }
        elsif ( $rf == 2 ) {
            $dna = substr( $seqObject->seq(), 1 );
        }
        else {
            $dna = substr( $seqObject->seq(), 2 );
        }

        my $length = length($dna);
        my $codon;
        my $start;
        my $stop;
        my $feature;
        my $featureRange;

        #for start
        for ( my $i = 0 ; $i < $length - 2 ; $i = $i + 3 ) {
            $codon = substr( $dna, $i, 3 );
            if ( $codon =~ m/$startCodons/i ) {
                $start = $i + $rf;
                $stop  = $start + 2;
                $feature =
                    "<feature color=\""
                  . $settings->{'startColor'}
                  . "\" decoration=\"arc\" opacity=\"$opacity\">\n";
                $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"0.5\" />\n";
                push( @outputArray, $feature . $featureRange . "</feature>\n" );
            }
        }

        #for stop
        for ( my $i = 0 ; $i < $length - 2 ; $i = $i + 3 ) {
            $codon = substr( $dna, $i, 3 );
            if ( $codon =~ m/$stopCodons/i ) {
                $start = $i + $rf;
                $stop  = $start + 2;
                $feature =
                    "<feature color=\""
                  . $settings->{'stopColor'}
                  . "\" decoration=\"arc\" opacity=\"$opacity\">\n";
                $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"1.0\" />\n";
                push( @outputArray, $feature . $featureRange . "</feature>\n" );
            }
        }
    }
    elsif ( $strand == -1 ) {
        my $rev = $seqObject->revcom;
        $dna = $rev->seq();

        if ( $rf == 1 ) {
            $dna = substr( $dna, 0 );
        }
        elsif ( $rf == 2 ) {
            $dna = substr( $dna, 1 );
        }
        else {
            $dna = substr( $dna, 2 );
        }

        my $length = length($dna);
        my $codon;
        my $start;
        my $stop;
        my $feature;
        my $featureRange;

        #for start
        for ( my $i = 0 ; $i < $length - 2 ; $i = $i + 3 ) {
            $codon = substr( $dna, $i, 3 );
            if ( $codon =~ m/$startCodons/i ) {

                #bug fix on 2009-09-01
                #$start = $length - $i - 1 - $rf;
                $start = $length - $i - 2;
                $stop  = $start + 2;
                $feature =
                    "<feature color=\""
                  . $settings->{'startColor'}
                  . "\" decoration=\"arc\" opacity=\"$opacity\">\n";
                $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"0.5\" />\n";
                push( @outputArray, $feature . $featureRange . "</feature>\n" );
            }
        }

        #for stop
        for ( my $i = 0 ; $i < $length - 2 ; $i = $i + 3 ) {
            $codon = substr( $dna, $i, 3 );
            if ( $codon =~ m/$stopCodons/i ) {

                #bug fix on 2009-09-01
                #$start = $length - $i - 1 - $rf;
                $start = $length - $i - 2;
                $stop  = $start + 2;
                $feature =
                    "<feature color=\""
                  . $settings->{'stopColor'}
                  . "\" decoration=\"arc\" opacity=\"$opacity\">\n";
                $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" proportionOfThickness=\"1.0\" />\n";
                push( @outputArray, $feature . $featureRange . "</feature>\n" );
            }
        }
    }

    push( @outputArray, "</featureSlot>\n" );
    my $strandTerm = undef;
    if ( $strand == 1 ) {
        $strandTerm = "forward";
    }
    if ( $strand == -1 ) {
        $strandTerm = "reverse";
    }

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE
          "<!-- stops and starts in rf $rf on $strandTerm strand -->\n" );
    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _writeOrfs {
    my $options   = shift;
    my $settings  = shift;
    my $global    = shift;
    my $seqObject = shift;
    my $strand    = shift;    #1 or -1
    my $rf        = shift;    #1,2, 3, or all

    my $opacity     = $settings->{'featureOpacity'};
    my $startCodons = $options->{'starts'};
    my $stopCodons  = $options->{'stops'};
    my $orfLength   = $options->{'orf_size'};
    my $color       = $settings->{'orfColor'};
    my $decoration;

    my @outputArray = ();

    #for CGView server
    my $shading = 'false';
    if (   ( defined( $options->{gene_decoration} ) )
        && ( $options->{gene_decoration} eq 'arrow' ) )
    {
        $shading = 'true';
    }

    if ( $strand == 1 ) {
        push( @outputArray,
            "<featureSlot showShading=\"$shading\" strand=\"direct\">\n" );
    }
    else {
        push( @outputArray,
            "<featureSlot showShading=\"$shading\" strand=\"reverse\">\n" );
    }

    my @orfs = ();

    if ( $rf eq "all" ) {
        push(
            @orfs,
            @{
                _getOrfs(
                    $options, $settings, $global, $seqObject, $strand, 1
                )
            }
        );
        push(
            @orfs,
            @{
                _getOrfs(
                    $options, $settings, $global, $seqObject, $strand, 2
                )
            }
        );
        push(
            @orfs,
            @{
                _getOrfs(
                    $options, $settings, $global, $seqObject, $strand, 3
                )
            }
        );
    }
    else {
        push(
            @orfs,
            @{
                _getOrfs( $options, $settings, $global,
                    $seqObject, $strand, $rf )
            }
        );
    }

    #sort orfs for visualization
    if ( $strand == 1 ) {
        @orfs = @{ _sortOrfs( \@orfs, 'start' ) };
    }
    elsif ( $strand == -1 ) {

        #@orfs = @{_sortOrfs(\@orfs, 'stop')};

        @orfs = @{ _sortOrfs( \@orfs, 'start' ) };
        @orfs = reverse(@orfs);
    }

    push( @outputArray, @orfs );

    push( @outputArray, "</featureSlot>\n" );
    my $strandTerm = undef;
    if ( $strand == 1 ) {
        $strandTerm = "forward";
    }
    if ( $strand == -1 ) {
        $strandTerm = "reverse";
    }

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE "<!-- ORFs in rf $rf on $strandTerm strand -->\n" );
    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _sortOrfs {
    my $orfs  = shift;
    my $field = shift;

    @$orfs = map { $_->[1] }
      sort { $b->[0] <=> $a->[0] }
      map { [ _getSortValueOrfs( $_, $field ), $_ ] } @$orfs;

    return $orfs;
}

sub _getSortValueOrfs {
    my $orf   = shift;
    my $field = shift;

    $orf =~ m/$field=\"(\d+)\"/;
    return $1;
}

sub _getOrfs {
    my $options    = shift;
    my $settings   = shift;
    my $global     = shift;
    my $seqObject  = shift;
    my $strand     = shift;    #1 or -1
    my $rf         = shift;    #1,2, or 3
    my $rfForLabel = $rf;

    my $opacity     = $settings->{'featureOpacity'};
    my $startCodons = $options->{'starts'};
    my $stopCodons  = $options->{'stops'};
    my $orfLength   = $options->{'orf_size'};
    my $color       = $settings->{'orfColor'};
    my $decoration;

    if ( $strand == 1 ) {
        $decoration = "clockwise-arrow";
    }
    else {
        $decoration = "counterclockwise-arrow";
    }

    #for CGView server
    if (   ( defined( $options->{gene_decoration} ) )
        && ( $options->{gene_decoration} eq 'arc' ) )
    {
        $decoration = "arc";
    }

    my @orfs = ();

    my $dna;
    if ( $strand == 1 ) {
        $dna = $seqObject->seq();
    }
    else {
        my $rev = $seqObject->revcom;
        $dna = $rev->seq();
    }
    my $length = length($dna);
    my $i      = 0;
    my $codon;
    my $foundStart    = 0;
    my $proteinLength = 0;
    my $foundStop     = 0;
    my $startPos      = $rf - 1;
    my $feature;
    my $featureRange;
    my $firstBase;
    my $lastBase;
    my $temp;

    my @dna = ();

    while ( $i <= $length - 3 ) {
        for ( $i = $startPos ; $i <= $length - 3 ; $i = $i + 3 ) {
            $codon = substr( $dna, $i, 3 );
            if (   ( $startCodons ne "any" )
                && ( $foundStart == 0 )
                && ( !( $codon =~ m/$startCodons/i ) ) )
            {
                last;
            }
            $foundStart = 1;

            if ( $codon =~ m/$stopCodons/i ) {
                $foundStop = 1;
            }

            $proteinLength++;
            push( @dna, $codon );

            if ( ($foundStop) && ( $proteinLength < $orfLength ) ) {
                last;
            }
            if (
                ( ($foundStop) && ( $proteinLength >= $orfLength ) )
                || (   ( $i >= $length - 5 )
                    && ( $proteinLength >= $orfLength ) )
              )
            {
                $firstBase = $startPos + 1;
                $lastBase  = $i + 3;

                if ( $strand == -1 ) {
                    $temp      = $length - $lastBase + 1;
                    $lastBase  = $length - $firstBase + 1;
                    $firstBase = $temp;
                }

                $global->{orfCount}++;

                $feature =
"<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";

                if ( _isTrue( $options->{orf_labels} ) ) {
                    $feature = $feature . "label=\"orf_$global->{orfCount}\" ";
                    $feature =
                        $feature
                      . "mouseover=\""
                      . _escapeText(
"orf_$global->{orfCount}; $firstBase to $lastBase; strand=$strand; rf=$rfForLabel"
                      ) . "\" ";
                }
                $feature = $feature . ">\n";

                $featureRange =
                  "<featureRange start=\"$firstBase\" stop=\"$lastBase\" />\n";
                push( @orfs, $feature . $featureRange . "</feature>\n" );

                last;
            }
        }
        $startPos      = $i + 3;
        $i             = $startPos;
        $foundStart    = 0;
        $foundStop     = 0;
        $proteinLength = 0;
        @dna           = ();
    }

    return \@orfs;

}

sub _expand_complex_features {
    my $seq_object = shift();
    my @features   = $seq_object->get_SeqFeatures();
    for my $feature (@features) {

        #check to see of the location is split (ie. complex feature)
        if ( $feature->location->isa('Bio::Location::SplitLocationI') ) {
            for my $sub_location ( $feature->location->sub_Location ) {

                #create a new split feature based on the original feature
                my $split_feature = new Bio::SeqFeature::Generic(
                    -display_name => $feature->display_name,
                    -strand       => $feature->strand,
                    -primary_tag  => $feature->primary_tag,
                    -frame        => $feature->frame,
                );

                #copy all the tags
                for my $tag ( $feature->all_tags ) {
                    for my $value ( $feature->each_tag_value($tag) ) {
                        $split_feature->set_attributes(
                            -tag => { $tag => $value } );
                    }
                }

                #set the location of the split feature to the sub location
                $split_feature->location($sub_location);
                $seq_object->add_SeqFeature($split_feature);
            }

        #change original feature type to 'del_feat' which will be excluded later
        #therefore the original complex feature will be excluded from drawing
            $feature->primary_tag('del_feat');
        }
    }
}

sub _writeEmblGenes {
    my $options   = shift;
    my $settings  = shift;
    my $global    = shift;
    my $seqObject = shift;
    my $strand    = shift;    #1 or -1
    my $rf        = shift;    #1,2,3, or undefined for all reading frames
    _writeGenBankGenes( $options, $settings, $global, $seqObject, $strand,
        $rf );
}

sub _writeGenBankGenes {

    my $options   = shift;
    my $settings  = shift;
    my $global    = shift;
    my $seqObject = shift;
    my $strand    = shift;    #1 or -1
    my $rf        = shift;    #1,2,3, or undefined for all reading frames

    my $opacity;

    #make rf
    if ( ( defined($rf) ) && ( $rf == 3 ) ) {
        $rf = 0;
    }

    my @outputArray = ();

    #for CGView server
    my $shading = 'false';
    if (   ( defined( $options->{gene_decoration} ) )
        && ( $options->{gene_decoration} eq 'arrow' ) )
    {
        $shading = 'true';
    }

    my $decoration;
    if ( $strand == 1 ) {
        push( @outputArray,
            "<featureSlot showShading=\"$shading\" strand=\"direct\">\n" );
        $decoration = "clockwise-arrow";
    }
    else {
        push( @outputArray,
            "<featureSlot showShading=\"$shading\" strand=\"reverse\">\n" );
        $decoration = "counterclockwise-arrow";
    }

    #for CGView server
    if (   ( defined( $options->{gene_decoration} ) )
        && ( $options->{gene_decoration} eq 'arc' ) )
    {
        $decoration = "arc";
    }

    #need to get the features from from the GenBank record.
    my @features = $seqObject->get_SeqFeatures();
    @features = @{ _sortFeaturesByStart( \@features ) };

    if ( $strand == 1 ) {
        @features = reverse(@features);
    }

    foreach (@features) {
        my $feat          = $_;
        my $type          = lc( $feat->primary_tag );
        my $is_type_other = 0;
        my $color;
        my $force_label;

        if ( $type eq "cds" ) {
            $color   = $settings->{'proteinColor'};
            $opacity = $settings->{'featureOpacity'};
        }

        #   elsif ($type eq "exon") {
        #       $color = $settings->{'proteinColor'};
        #       $opacity = $settings->{'featureOpacity'};
        #   }

        elsif ( $type eq "rrna" ) {
            $color   = $settings->{'rRNAColor'};
            $opacity = $settings->{'featureOpacity'};
        }
        elsif ( $type eq "trna" ) {
            $color   = $settings->{'tRNAColor'};
            $opacity = $settings->{'featureOpacity'};
        }
        else {
            $is_type_other = 1;
            $color         = $settings->{'otherColor'};
            $opacity       = $settings->{'featureOpacityOther'};

            #       $decoration = "arc";
        }

        #skip certain feature types
        if ( $type eq "source" ) {
            next;
        }
        if ( $type eq "gene" ) {
            next;
        }
        if ( $type eq "exon" ) {
            next;
        }
        if ( $type eq "del_feat" ) {
            next;
        }

        #   if ($type eq "misc_feature") {
        #       next;
        #   }

        my $st = $feat->strand;
        unless ( $st == $strand ) {
            next;
        }

        my $start = $feat->start;
        my $stop  = $feat->end;

        #this handles feature that spans start/stop
        #UPDATE: all features with more than 1 entry in @loc are skipped below

        my $location  = $feat->location;
        my $locString = $location->to_FTstring;
        my @loc       = split( /,/, $locString );

        if ( $loc[0] =~ m/(\d+)\.\.(\d+)/ ) {
            $start = $1;
        }

        if ( $loc[ scalar(@loc) - 1 ] =~ m/(\d+)\.\.(\d+)/ ) {
            $stop = $2;
        }

        if ( defined($rf) ) {
            if ( $strand == 1 ) {
                unless ( $rf == $start % 3 ) {
                    next;
                }
            }
            elsif ( $strand == -1 ) {
                unless ( $rf == ( $seqObject->length() - $stop + 1 ) % 3 ) {
                    next;
                }
            }
        }

        my $label;
        if ( $feat->has_tag('gene') ) {
            $label = join( "", $feat->get_tag_values('gene') );
        }
        elsif ( $feat->has_tag('locus_tag') ) {
            $label = join( "", $feat->get_tag_values('locus_tag') );
        }
        elsif ( $feat->has_tag('note') ) {
            $label = join( "", $feat->get_tag_values('note') );
        }
        else {
            $label = $feat->primary_tag;
        }

        if ( $feat->primary_tag eq 'intron' ) {
            $label .= " (intron)";
        }

        #2011_06_18; 2012-05-30
        if ( defined( $options->{labels_to_show} ) ) {
            if (
                ( $feat->has_tag('gene') )
                && (
                    defined(
                        $options->{labels_to_show}
                          ->{ join( "", $feat->get_tag_values('gene') ) }
                    )
                )
              )
            {
                $label = $options->{labels_to_show}
                  ->{ join( "", $feat->get_tag_values('gene') ) };
                $force_label = 1;
            }
            elsif (
                ( $feat->has_tag('locus_tag') )
                && (
                    defined(
                        $options->{labels_to_show}
                          ->{ join( "", $feat->get_tag_values('locus_tag') ) }
                    )
                )
              )
            {
                $label = $options->{labels_to_show}
                  ->{ join( "", $feat->get_tag_values('locus_tag') ) };
                $force_label = 1;
            }
            else {
                $force_label = 0;
            }
        }

        if ( length($label) > $settings->{'maxLabelLength'} - 3 ) {
            $label =
              substr( $label, 0, $settings->{'maxLabelLength'} - 3 ) . "...";
        }
        $label = _escapeText($label);

        #/db_xref="GI:11497049"
        my $hyperlink;
        if ( $feat->has_tag('db_xref') ) {
            my $dbXref = join( "", $feat->get_tag_values('db_xref') );
            if ( $dbXref =~ m/GI\:(\d+)/ ) {
                $hyperlink = $global->{'ncbiGiLink'} . $1;
            }

            #/db_xref="GeneID:1132092"
            elsif ( $dbXref =~ m/GeneID\:(\d+)/ ) {
                $hyperlink = $global->{'ncbiGeneLink'} . $1;
            }
        }

        #/product="conserved hypothetical protein"
        my $mouseover = $label . "; " . $start . " to " . $stop;
        if ( $feat->has_tag('product') ) {
            my $product = join( "", $feat->get_tag_values('product') );
            $mouseover = $mouseover . "; " . $product;
        }
        if ( $feat->has_tag('db_xref') ) {
            my $dbXref = join( "", $feat->get_tag_values('db_xref') );
            if ( $dbXref =~ m/(GI\:\d+)/ ) {
                $mouseover = $mouseover . "; " . $1;
            }

            #/db_xref="GeneID:1132092"
            elsif ( $dbXref =~ m/(GeneID\:\d+)/ ) {
                $mouseover = $mouseover . "; " . $1;
            }
        }
        $mouseover = _escapeText($mouseover);

        #check length of feature
        my $featLength = $stop - $start + 1;
        if ( scalar(@loc) > 1 ) {
            _message( $options,
"The following feature of type '$type' has a complex location string and will be ignored: label: $label"
            );
            next;
        }
        if ( $featLength == 0 ) {
            _message( $options,
"The following feature of type '$type' has length equal to zero and will be ignored: label: $label; start: $start; end: $stop"
            );
            next;
        }
        if ( $featLength < 0 ) {
            _message( $options,
"The following feature of type '$type' has 'start' less than 'end' and will be ignored: label: $label; start: $start; end: $stop"
            );
            next;
        }
        if ( $featLength > $settings->{maxFeatureSize} ) {
            _message( $options,
"The following feature of type '$type' has length greater than $settings->{maxFeatureSize} and will be ignored: label: $label; start: $start; end: $stop"
            );
            next;
        }
        if ( $featLength == $seqObject->length() ) {
            _message( $options,
"The following feature of type '$type' has length equal to the length of the entire sequence and will be ignored: label: $label; start: $start; end: $stop"
            );
            next;
        }

        #now check entries and make tags
        my $feature =
"<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";

        if ( _containsText($label) ) {
            $feature = $feature . "label=\"$label\" ";
        }
        if ( _containsText($hyperlink) ) {
            $feature = $feature . "hyperlink=\"$hyperlink\" ";
        }
        if ( _containsText($mouseover) ) {
            $feature = $feature . "mouseover=\"$mouseover\" ";
        }

        #2011_06_18
        if ( defined($force_label) ) {
            if ($force_label) {
                $feature = $feature . "showLabel=\"true\" ";
            }
            else {
                $feature = $feature . "showLabel=\"false\" ";
            }
        }

        #2011-02-16
        #don't show label if feature_labels isn't true and ctt != 1
        elsif ( !( _isTrue( $options->{feature_labels} ) )
            && ( !( $options->{cct} == 1 ) ) )
        {
            $feature = $feature . "showLabel=\"false\" ";
        }

        $feature = $feature . ">\n";

        my $featureRange = "<featureRange start=\"$start\" stop=\"$stop\" />\n";

        #now add $feature and $featureRange to @outputArray
        push( @outputArray, $feature . $featureRange . "</feature>\n" );

    }

    push( @outputArray, "</featureSlot>\n" );
    my $strandTerm = undef;
    if ( $strand == 1 ) {
        $strandTerm = "forward";
    }
    if ( $strand == -1 ) {
        $strandTerm = "reverse";
    }

    my $rfTerm = undef;
    if ( !( defined($rf) ) ) {
        $rfTerm = "1,2,3";
    }
    else {
        $rfTerm = $rf;
    }

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE
          "<!-- GenBank or EMBL genes on strand $strandTerm in rf $rfTerm -->\n"
    );
    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _sortFeaturesByStart {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $a->[0] <=> $b->[0] }
      map { [ _getSortValueFeature($_), $_ ] } @$features;

    return $features;
}

sub _getSortValueFeature {
    my $feature = shift;
    return $feature->start;
}

sub _containsText {
    my $text = shift;

    if ( !( defined($text) ) ) {
        return 0;
    }
    if ( $text =~ m/[A-Za-z0-9]/g ) {
        return 1;
    }
    else {
        return 0;
    }
}

sub _writeGenes {

    my $options          = shift;
    my $settings         = shift;
    my $global           = shift;
    my $seqObject        = shift;
    my $strand           = shift;    #1 or -1
    my $rf               = shift;    #1,2,3, or undefined for all reading frames
    my $analysisColorPos = shift;
    my $analysisColorNeg = shift;
    my $file             = shift;
    my $useScore         = shift;
    my $standardBarGraph = 0;

    my $opacity = $settings->{'featureOpacity'};

    if ( ( defined($rf) ) && ( $rf == 3 ) ) {
        $rf = 0;
    }

    my @outputArray = ();

    #for CGView server
    my $shading = 'false';
    if (   ( defined( $options->{gene_decoration} ) )
        && ( $options->{gene_decoration} eq 'arrow' ) )
    {
        $shading = 'true';
    }

    my $decoration;
    if ( !($useScore) ) {
        if ( $strand == 1 ) {
            push( @outputArray,
                "<featureSlot showShading=\"$shading\" strand=\"direct\">\n" );
            $decoration = "clockwise-arrow";
        }
        else {
            push( @outputArray,
                "<featureSlot showShading=\"$shading\" strand=\"reverse\">\n" );
            $decoration = "counterclockwise-arrow";
        }
    }
    else {
        if ( $strand == 1 ) {
            push( @outputArray,
"<featureSlot showShading=\"false\" strand=\"direct\" featureThickness=\""
                  . $settings->{'featureThicknessPlot'}
                  . "\">\n" );
        }
        else {
            push( @outputArray,
"<featureSlot showShading=\"false\" strand=\"reverse\" featureThickness=\""
                  . $settings->{'featureThicknessPlot'}
                  . "\">\n" );
        }
        $decoration = "arc";
    }

    #for CGView server
    if (   ( defined( $options->{gene_decoration} ) )
        && ( $options->{gene_decoration} eq 'arc' ) )
    {
        $decoration = "arc";
    }

    #need to get the features from the file.
    my @features = ();
    @features =
      @{ _parseGFF( $file, $options, $settings, $global, $seqObject ) };

    #check to see if these features look like COGs
    my $hasCogs = 0;
    foreach (@features) {
        my $feat = $_;
        my $type = lc( $feat->{'feature'} );
        if ( defined( $settings->{'cogColors'}->{ uc($type) } ) ) {
            $hasCogs = 1;
        }
    }

    if ( ($hasCogs) && ( !( _isTrue( $settings->{scale_blast} ) ) ) ) {
        if ( $strand == -1 ) {
            @features = @{ _sortGFFByStart( \@features ) };
        }

        if ( $strand == 1 ) {
            @features = @{ _sortGFFByStartRev( \@features ) };
        }
    }
    else {
        if ( $strand == -1 ) {
            @features = @{ _sortGFFByStartAndScore( \@features ) };
        }

        if ( $strand == 1 ) {
            @features = @{ _sortGFFByStartRevAndScore( \@features ) };
        }
    }

#This section was added 2012-05-24 to handle case where all score values are '.' or '-'
#
    if ( defined($useScore) ) {
        my $has_non_missing_score = 0;
        foreach (@features) {
            if ( ( $_->{score} eq '.' ) || ( $_->{score} eq '-' ) ) {
                next;
            }
            else {
                $has_non_missing_score = 1;
                last;
            }
        }
        if ( !($has_non_missing_score) ) {
            $useScore = undef;
        }
    }

    #This section was added 2007-05-30 to normalize score values for plotting
    #
    if ( defined($useScore) ) {
        my $min     = undef;
        my $max     = undef;
        my $average = undef;
        my $sum     = 0;
        my $count   = 0;
        foreach (@features) {
            my $score;

            if (   ( $_->{score} =~ m/\d/ )
                && ( $_->{score} =~ m/(\-?[\d\.e\-]+)/ ) )
            {
       #bug found on 2015-02-07: $1 undef
       #if (( $_->{score} =~ m/(\-?[\d\.e\-]+)/ ) && ( $_->{score} =~ m/\d/ )) {
                $score = $1;
                $sum   = $sum + $score;
                if ( ( !( defined($max) ) ) || ( $score > $max ) ) {
                    $max = $score;
                }
                if ( ( !( defined($min) ) ) || ( $score < $min ) ) {
                    $min = $score;
                }
                $count++;
            }
        }

#Added 2009-02-22 to allow data sets consisting of all positive values to be plotted as a standard bar graph.
        if ( ( defined($min) ) && ( $min >= 0 ) ) {
            $standardBarGraph = 1;
            $max              = sprintf( "%.4f", $max );
            $min              = sprintf( "%.4f", $min );

            _message( $options,
                "Scaling score values in the file $file to between 0 and 1." );
            _message( $options, "The maximum score value is $max." );
            _message( $options, "The minimum score value is $min." );

     #adjust the score values so that the full width of the featureSlot is used.
            foreach (@features) {
                my $score;
                if ( $_->{score} =~ m/(\-?[\d\.e\-]+)/ ) {
                    $score = $1;
                    $_->{score} = sprintf( "%.4f", $score / $max );
                }
            }
        }

#Modified 2011-11-27
#If there are negative values, the x-axis is positioned in the middle of the featureSlot
        elsif ( ( defined($min) ) && ( $min < 0 ) ) {
            $average = $sum / $count;

            $average = sprintf( "%.4f", $average );
            $max     = sprintf( "%.4f", $max );
            $min     = sprintf( "%.4f", $min );

            _message( $options,
                "Scaling score values in the file $file to between 1 and -1." );
            _message( $options, "The maximum score value is $max." );
            _message( $options, "The minimum score value is $min." );
            _message( $options, "The average score value is $average." );

            my $maxDeviation;
            if ( abs($max) > abs($min) ) {
                $maxDeviation = abs($max);
            }
            else {
                $maxDeviation = abs($min);
            }

     #adjust the score values so that the full width of the featureSlot is used.
            foreach (@features) {
                my $score;
                if ( $_->{score} =~ m/(\-?[\d\.e\-]+)/ ) {
                    $score = $1;
                    $_->{score} = sprintf( "%.4f", $score / $maxDeviation );
                }
            }
        }

    }

    foreach (@features) {
        my $feat = $_;
        my $type = lc( $feat->{'feature'} );

        my $feature_decoration;
        my $is_cog  = 0;
        my $not_cog = 0
          ; #used to prevent CDS etc from being drawn as three separate COG features
        my $force_label;

#the genes in the GFF file can be coloured by the 'feature' column, based on the
#type of gene (CDS, rRNA, tRNA, or other) or by COG (J, K, L etc).
#colour based on feature type first
        my $color;
        if (   ( defined($analysisColorPos) )
            && ( defined($analysisColorNeg) ) )
        {
            $color = undef;
        }
        elsif ( $type eq "cds" ) {
            $color   = $settings->{'proteinColor'};
            $opacity = $settings->{'featureOpacity'};
            $not_cog = 1;
        }
        elsif ( $type eq "rrna" ) {
            $color   = $settings->{'rRNAColor'};
            $opacity = $settings->{'featureOpacity'};
            $not_cog = 1;
        }
        elsif ( $type eq "trna" ) {
            $color   = $settings->{'tRNAColor'};
            $opacity = $settings->{'featureOpacity'};
            $not_cog = 1;
        }
        elsif ( $type eq "other" ) {
            $color              = $settings->{'otherColor'};
            $opacity            = $settings->{'featureOpacityOther'};
            $feature_decoration = 'arc';
            $not_cog            = 1;
        }
        elsif ( defined( $settings->{'cogColors'}->{ uc($type) } ) ) {
            $color   = $settings->{'cogColors'}->{ uc($type) };
            $opacity = $settings->{'featureOpacity'};
            $is_cog  = 1;
        }
        else {
            $color   = $settings->{'otherColor'};
            $opacity = $settings->{'featureOpacityOther'};
        }

        my $st;
        if (   ( !defined( $feat->{'strand'} ) )
            || ( $feat->{'strand'} eq "." ) )
        {
            $st                 = 1;
            $feature_decoration = 'arc';
        }
        elsif ( ( $feat->{'strand'} eq "+" ) || ( $feat->{'strand'} eq "1" ) ) {
            $st = 1;
        }
        elsif (( $feat->{'strand'} eq "-" )
            || ( $feat->{'strand'} eq "-1" ) )
        {
            $st = -1;
        }

        #when using score keep all features regardless of strand
        if ( ( $st ne $strand ) && ( !$useScore ) ) {
            next;
        }

        my $start = $feat->{'start'};
        my $stop  = $feat->{'end'};

        if ( defined($rf) ) {
            if ( $strand == 1 ) {
                unless ( $rf == $start % 3 ) {
                    next;
                }
            }
            elsif ( $strand == -1 ) {
                unless ( $rf == ( $seqObject->length() - $stop + 1 ) % 3 ) {
                    next;
                }
            }
        }

        my $label;
        if ( _containsText( $feat->{'seqname'} ) ) {
            $label = $feat->{'seqname'};

            #2011_06_18

            if ( defined( $options->{labels_to_show} ) ) {
                if ( defined( $options->{labels_to_show}->{$label} ) ) {
                    $label       = $options->{labels_to_show}->{$label};
                    $force_label = 1;
                }
                else {
                    $force_label = 0;
                }
            }

            if ( length($label) > $settings->{'maxLabelLength'} - 3 ) {
                $label = substr( $label, 0, $settings->{'maxLabelLength'} - 3 )
                  . "...";
            }
            $label = _escapeText($label);
        }

        my $mouseover;
        if ( _containsText($label) ) {
            $mouseover = $label . "; " . $start . " to " . $stop;
        }
        else {
            $mouseover = $start . " to " . $stop;
        }
        if ( _containsText( $feat->{'feature'} ) ) {
            $mouseover = $mouseover . "; " . $feat->{'feature'};
            $mouseover = _escapeText($mouseover);
        }

        if ( !( defined($feature_decoration) ) ) {
            $feature_decoration = $decoration;
        }

        #now check entries and make tags
        my $feature;
        if ( defined($color) ) {
            $feature =
"<feature color=\"$color\" decoration=\"$feature_decoration\" opacity=\"$opacity\" ";
        }
        else {
            $feature =
"<feature decoration=\"$feature_decoration\" opacity=\"$opacity\" ";
        }

        #2011_06_18
        if ( defined($force_label) ) {
            if ($force_label) {
                $feature = $feature . "showLabel=\"true\" ";
            }
            else {
                $feature = $feature . "showLabel=\"false\" ";
            }
        }

        #don't label genes when $useScore defined
        #or if score is given
        if (   ( _isTrue( $options->{gene_labels} ) )
            && ( !$useScore )
            && ( ( $feat->{'score'} eq '.' ) || ( $feat->{'score'} eq '-' ) ) )
        {
            if ( _containsText($label) ) {
                $feature = $feature . "label=\"$label\" ";
            }
            if ( _containsText($mouseover) ) {
                $feature = $feature . "mouseover=\"$mouseover\" ";
            }
        }

        $feature = $feature . ">\n";

        my $featureRange;
        if ( defined($useScore) ) {

            #score should be between -1 and 1 at this point
            my $score;
            if ( defined( $feat->{'score'} ) ) {
                if ( $feat->{'score'} =~ m/(\-?[\d\.e\-]+)/ ) {
                    $score = $1;
                }
            }
            if ( defined($score) ) {
                if ( $score > 1 ) {
                    $score = 1;
                }
                if ( $score < -1 ) {
                    $score = -1;
                }
            }
            else {
                $score = 0;
            }

            #scale_blast must be T for COGs to be scaled
            if ( ( !( _isTrue( $settings->{scale_blast} ) ) ) && ($is_cog) ) {
                $score = 1;
            }

#Want to draw as bars with positive values extending outwards and negative values extending inwards.
#If $standardBarGraph == 1, draw as bars with all values extending outwards.
            my $barHeight;
            my $radiusShift;

            if ($standardBarGraph) {
                $barHeight   = $score;
                $radiusShift = $barHeight / 2.0;
                $color       = $analysisColorPos;
            }
            elsif ( $score > 0 ) {
                $barHeight   = $score;
                $barHeight   = $barHeight * 0.5;
                $radiusShift = 0.5 + $barHeight / 2.0;
                $color       = $analysisColorPos;
            }
            elsif ( $score < 0 ) {
                $barHeight   = 0 - $score;
                $barHeight   = $barHeight * 0.5;
                $radiusShift = 0.5 - $barHeight / 2;
                $color       = $analysisColorNeg;
            }
            else {
                $radiusShift = 0.5;
                $barHeight   = $settings->{'plotLineThickness'};
                $color       = $analysisColorPos;
            }

            $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" radiusAdjustment=\"$radiusShift\" proportionOfThickness=\"$barHeight\" color=\"$color\" />\n";
            push( @outputArray, $feature . $featureRange . "</feature>\n" );
        }

#Added 2010-06-07 to handle case where multiple COG categories specified, for example 'LKJ'
#and to draw COGs with score as arcs

        #Need to make sure this isn't used for feature types like CDS or tRNA
        else {

#check to see if $feat->{'feature'} contains all uppercase letters that are known COG categories
            my @letters      = split( //, $feat->{'feature'} );
            my $is_cog_count = 0;
            foreach my $letter (@letters) {
                if ( defined( $settings->{'cogColors'}->{$letter} ) ) {
                    $is_cog_count++;
                }
            }

#if there are multiple cogs, draw each as smaller feature taking up a portion of the feature slot
            if (
                ( !$not_cog )
                && (
                    (
                           ( $is_cog_count > 1 )
                        && ( $is_cog_count == scalar(@letters) )
                    )
                    || ($is_cog)
                )
              )
            {

                my $cog_decoration  = $feature_decoration;
                my $full_proportion = 1.0;
                my $cog_opacity     = $settings->{'featureOpacity'};
                my $cog_shading     = $shading;

                #if cog has identity proportion of score then scale height
                #unless scale_blast is F
                if (   ( _isTrue( $settings->{scale_blast} ) )
                    && ( defined( $feat->{'score'} ) )
                    && ( $feat->{'score'} ne '.' )
                    && ( $feat->{'score'} >= 0 )
                    && ( $feat->{'score'} <= 1.0 ) )
                {
                    $full_proportion = $feat->{'score'};
                    $cog_decoration  = 'arc';
                    $cog_shading     = 'false';

                    #if (_isTrue($options->{use_opacity})) {
                    #    $cog_opacity = "0.2";
                    #}
                    #else {
                    #    $cog_opacity = "1.0";
                    #}

                }

                my $prop_of_thickness =
                  sprintf( "%.5f", ( $full_proportion / $is_cog_count ) );
                my $radius_adjustment =
                  sprintf( "%.5f", $prop_of_thickness / 2 );
                push( @outputArray, $feature );
                foreach my $letter (@letters) {
                    my $cog_color = $settings->{'cogColors'}->{$letter};
                    if ( $radius_adjustment < 0 ) {
                        $radius_adjustment = 0;
                    }
                    elsif ( $radius_adjustment > 1 ) {
                        $radius_adjustment = 1;
                    }

                    $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" radiusAdjustment=\"$radius_adjustment\" proportionOfThickness=\"$prop_of_thickness\" color=\"$cog_color\" decoration=\"$cog_decoration\" opacity=\"$cog_opacity\" showShading=\"$cog_shading\" />\n";
                    push( @outputArray, $featureRange );
                    $radius_adjustment =
                      $radius_adjustment + $prop_of_thickness;
                }
                push( @outputArray, "</feature>\n" );
            }
            else {
                $featureRange =
                  "<featureRange start=\"$start\" stop=\"$stop\" />\n";
                push( @outputArray, $feature . $featureRange . "</feature>\n" );
            }
        }
    }

    #Create dashed line to indicate zero
    if ( defined($useScore) ) {

        #here a dash is a dash and the space following it.
        #   if (_isTrue($options->{draw_divider_rings})) {
        my $length           = $seqObject->length();
        my $number_of_dashes = 500;
        my $bases_per_dash   = $length / $number_of_dashes;
        $bases_per_dash = sprintf( "%.0f", $bases_per_dash );
        if ( $bases_per_dash < 2 ) {
            $bases_per_dash = 2;
        }
        my $i = 1;

        #make the dashed line the same thickness as the divider_ring
        my $dashed_line_proportion = ( $settings->{backboneThickness} * 0.25 ) /
          $settings->{featureThicknessPlot};
        if ( $dashed_line_proportion > 0.05 ) {
            $dashed_line_proportion = 0.05;
        }
        my $dashed_line_shift = 0.5;

        push( @outputArray,
"<feature radiusAdjustment=\"$dashed_line_shift\" proportionOfThickness=\"$dashed_line_proportion\" color=\"$settings->{backboneColor}\">\n"
        );

        while ( $i < $length ) {
            my $start = $i;
            my $stop  = $start + sprintf( "%.0f", $bases_per_dash / 2 );
            if ( $stop > $length ) {
                $stop = $length;
            }
            push( @outputArray,
                "<featureRange start=\"$start\" stop=\"$stop\" />\n" );
            $i = $i + $bases_per_dash;
        }

        push( @outputArray, "</feature>\n" );

        #   }
    }

    push( @outputArray, "</featureSlot>\n" );
    my $strandTerm = undef;
    if ( $strand == 1 ) {
        $strandTerm = "forward";
    }
    if ( $strand == -1 ) {
        $strandTerm = "reverse";
    }

    my $rfTerm = undef;
    if ( !( defined($rf) ) ) {
        $rfTerm = "1,2,3";
    }
    else {
        $rfTerm = $rf;
    }

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE
          "<!-- genes or analysis on $strandTerm strand in rf $rfTerm -->\n" );
    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _parseGFF {
    my $file         = shift;
    my $options      = shift;
    my $settings     = shift;
    my $global       = shift;
    my $seqObject    = shift;
    my $lineCount    = 0;
    my @columnTitles = ();
    my $columnsRead  = 0;

    adjust_newlines( $file, 'GFF' );

    open( INFILE, $file ) or die("Cannot open the GFF file $file");

    #check for column titles
    while ( my $line = <INFILE> ) {
        $line =~ s/\cM|\n//g;
        $lineCount++;
        if ( $line =~ m/^\#/ ) {
            next;
        }
        if ( $line =~ m/\S/ ) {
            $columnsRead  = 1;
            @columnTitles = @{ _split($line) };
            last;
        }
    }

    #print Dumper(@columnTitles);
    my @gffColumns = (
        "seqname", "source", "feature", "start",
        "end",     "score",  "strand",  "frame"
    );

    for ( my $i = 0 ; $i < scalar(@gffColumns) ; $i++ ) {
        if ( !( defined( $columnTitles[$i] ) ) ) {
            _message( $options,
"Column $i in GFF file $file was not defined - must be titled \"$gffColumns[$i]\"."
            );
            die(
"Column $i in GFF file $file was not defined - must be titled \"$gffColumns[$i]\""
            );
        }
        elsif ( $gffColumns[$i] ne lc( $columnTitles[$i] ) ) {
            _message( $options,
"Column $i in GFF file $file was titled $columnTitles[$i] - must be titled \"$gffColumns[$i]\"."
            );
            die(
"Column $i in GFF file $file was titled $columnTitles[$i] - must be titled \"$gffColumns[$i]\""
            );
        }
    }

#To allow for 'start', 'end', 'strand', and 'frame' values to be read from the '-sequence' file
#build a hash of genes.
    my $sequenceGenes = _getFeatures( $global, $seqObject );

    #print (Dumper($sequenceGenes));

    my @entries = ();
    while ( my $line = <INFILE> ) {
        $line =~ s/\cM|\n//g;
        $lineCount++;
        if ( $line =~ m/\S/ ) {
            my @values = @{ _split($line) };
            my %entry  = ();
            for ( my $i = 0 ; $i < scalar(@gffColumns) ; $i++ ) {
                $entry{ $gffColumns[$i] } = $values[$i];
            }

            #try to add to entry if needed
            _addToEntry( \%entry, $sequenceGenes );

            if ( !( defined( $entry{'start'} ) ) ) {
                if ( defined( $entry{'seqname'} ) ) {
                    _message( $options,
"Warning: unable to obtain 'start' value for seqname $entry{seqname} in $file line $lineCount."
                    );
                }
                else {
                    _message( $options,
"Warning: unable to obtain 'start' value for $file line $lineCount."
                    );
                }
                next;
            }

            if ( !( defined( $entry{'end'} ) ) ) {
                if ( defined( $entry{'seqname'} ) ) {
                    _message( $options,
"Warning: unable to obtain 'end' value for seqname $entry{seqname} in $file line $lineCount."
                    );
                }
                else {
                    _message( $options,
"Warning: unable to obtain 'end' value for $file line $lineCount."
                    );
                }
                next;
            }

            if ( !( defined( $entry{'strand'} ) ) ) {
                if ( defined( $entry{'seqname'} ) ) {
                    _message( $options,
"Warning: unable to obtain 'strand' value for seqname $entry{seqname} in $file line $lineCount."
                    );
                }
                else {
                    _message( $options,
"Warning: unable to obtain 'strand' value for $file line $lineCount."
                    );
                }
                next;
            }

            if ( !( $entry{'start'} =~ m/^\d+$/ ) ) {
                _message( $options,
"Warning: value in 'start' column must be a positive integer in $file line $lineCount."
                );
                next;

#die ("Value in 'start' column must be a positive integer in $file line $lineCount");
            }
            if ( !( $entry{'end'} =~ m/^\d+$/ ) ) {
                _message( $options,
"Warning: value in 'end' column must be a positive integer in $file line $lineCount."
                );
                next;

#die ("Value in 'end' column must be a positive integer in $file line $lineCount");
            }
            if ( !( $entry{'strand'} =~ m/^(\+|\-|\.|\s)$/ ) ) {
                _message( $options,
"Warning: value in 'strand' column must be '+', '-', or '.' in $file line $lineCount."
                );
                next;

#die ("Value in 'strand' column must be '+', '-', or '.' in $file line $lineCount");
            }

            #check start and end
            if ( $entry{'start'} > $global->{length} ) {
                _message( $options,
"Warning: value in 'start' column must be less that the sequence length in $file line $lineCount."
                );
                next;

#die ("Value in 'start' column must be less that the sequence length in $file line $lineCount");
            }
            if ( $entry{'end'} > $global->{length} ) {
                _message( $options,
"Warning: value in 'end' column must be less that the sequence length in $file line $lineCount."
                );
                next;

#die ("Value in 'end' column must be less that the sequence length in $file line $lineCount");
            }

#Added 2008_07_31
#users often swap the start and end values for features located on the negative strand.
#check for start > end and swap the values if it makes the feature shorter
            if ( $entry{'start'} > $entry{'end'} ) {
                my $around_origin_length = ( $entry{'end'} - 1 + 1 ) +
                  ( $global->{length} - $entry{'start'} + 1 );
                my $direct_length = $entry{'start'} - $entry{'end'} + 1;
                if ( $direct_length < $around_origin_length ) {
                    ( $entry{'start'}, $entry{'end'} ) =
                      ( $entry{'end'}, $entry{'start'} );
                    _message( $options,
"Warning: swapping values in 'start' and 'end' column so that 'start' is less than 'end' in $file line $lineCount."
                    );
                }
            }

            push( @entries, \%entry );
        }
    }
    close(INFILE) or die("Cannot close file : $!");
    return \@entries;
}

sub _addToEntry {
    my $entry           = shift;
    my $genBankFeatures = shift;

    if (
           ( defined( $entry->{'seqname'} ) )
        && ( defined( $genBankFeatures->{ $entry->{'seqname'} } ) )
        && (
            !(
                defined(
                    $genBankFeatures->{ $entry->{'seqname'} }->{multiples}
                )
            )
        )
      )
    {

        #will try to determine start, end, strand
        if (   ( !( defined( $entry->{'start'} ) ) )
            || ( !( $entry->{'start'} =~ m/^\d+$/ ) ) )
        {
            $entry->{'start'} =
              $genBankFeatures->{ $entry->{'seqname'} }->{start};
        }
        if (   ( !( defined( $entry->{'end'} ) ) )
            || ( !( $entry->{'end'} =~ m/^\d+$/ ) ) )
        {
            $entry->{'end'} = $genBankFeatures->{ $entry->{'seqname'} }->{end};
        }
        if (   ( !( defined( $entry->{'strand'} ) ) )
            || ( !( $entry->{'strand'} =~ m/^(\+|\-|\.)$/ ) ) )
        {
            $entry->{'strand'} =
              $genBankFeatures->{ $entry->{'seqname'} }->{strand};
        }
    }
}

sub _getFeatures {
    my $global    = shift;
    my $seqObject = shift;

    if (   ( $global->{format} ne "genbank" )
        && ( $global->{format} ne "embl" ) )
    {
        return undef;
    }

    #need to get the features from from the GenBank record.
    my @features = $seqObject->get_SeqFeatures();
    @features = @{ _sortFeaturesByStart( \@features ) };

    my %featureHash = ();
    foreach (@features) {
        my $feat = $_;

        my $type = lc( $feat->primary_tag );

        ################
        #added 2007-10-10
        #
        #Some GenBank files may contain entries like the following:
        #
        #     gene            complement(1646..1774)
        #                     /locus_tag="MM1_0006"
        #     CDS             complement(1646..1774)
        #                     /locus_tag="MM1_0006"
        #
        #These features share the same /locus_tag,
        #which was causing this script to declare
        #the /locus_tag as having multiple positions.
        #This new code block skips several feature types
        #that are not drawn on the map anyway, and
        #this avoids the problem of having multiple
        #positions in many cases.
        if ( $type eq "source" ) {
            next;
        }
        if ( $type eq "gene" ) {
            next;
        }
        if ( $type eq "exon" ) {
            next;
        }
        if ( $type eq "del_feat" ) {
            next;
        }
        #################

        my $strand = $feat->strand;
        my $start  = $feat->start;
        my $stop   = $feat->end;

        #this handles feature that spans start/stop
        my $location  = $feat->location;
        my $locString = $location->to_FTstring;
        my @loc       = split( /,/, $locString );

        if ( $loc[0] =~ m/(\d+)\.\.(\d+)/ ) {
            $start = $1;
        }

        if ( $loc[ scalar(@loc) - 1 ] =~ m/(\d+)\.\.(\d+)/ ) {
            $stop = $2;
        }

        my $rf;
        if ( $strand == 1 ) {
            $rf = $start % 3;
        }
        elsif ( $strand == -1 ) {
            $rf = ( $seqObject->length() - $stop + 1 ) % 3;
        }

        if ( $rf == 0 ) {
            $rf = 3;
        }

        my $geneName;
        if ( $feat->has_tag('locus_tag') ) {
            $geneName = join( "", $feat->get_tag_values('locus_tag') );
        }

        if ( !( defined($geneName) ) ) {
            next;
        }

        #change some values
        if ( $strand == -1 ) {
            $strand = "-";
        }
        else {
            $strand = "+";
        }

        #add this info to the geneHash
        my %geneHash = (
            start  => undef,
            end    => undef,
            strand => undef,
            rf     => undef
        );

        $geneHash{start}  = $start;
        $geneHash{end}    = $stop;
        $geneHash{rf}     = $rf;
        $geneHash{strand} = $strand;

        if ( defined( $featureHash{$geneName} ) ) {

     #if multiple genes have the same tag, mark this gene with the multiples key
            $featureHash{$geneName}->{multiples} = 1;
        }
        else {
            $featureHash{$geneName} = \%geneHash;
        }
    }
    return \%featureHash;
}

sub _split {
    my $line   = shift;
    my @values = ();
    if ( $line =~ m/\t/ ) {
        @values = split( /\t/, $line );
    }
    elsif ( $line =~ m/\,/ ) {
        @values = split( /\,/, $line );
    }
    else {
        @values = split( /\s/, $line );
    }
    foreach (@values) {
        $_ = _cleanValue($_);
    }
    return \@values;
}

sub _sortGFFByStartAndScore {
    my $features = shift;

    @$features = map { $_->[2] }

      #      sort { $a->[0] <=> $b->[0] || $b->[1] <=> $a->[1]}
      sort { $b->[1] <=> $a->[1] || $a->[0] <=> $b->[0] }
      map  { [ ( $_->{'start'} || 1 ), _getScore( $_->{'score'} ), $_ ] }
      @$features;

    return $features;
}

sub _sortGFFByStart {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $a->[0] <=> $b->[0] }
      map { [ ( $_->{'start'} || 1 ), $_ ] } @$features;

    return $features;
}

sub _sortGFFByStartRev {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $b->[0] <=> $a->[0] }
      map { [ ( $_->{'start'} || 1 ), $_ ] } @$features;

    return $features;
}

sub _sortGFFByStartRevAndScore {
    my $features = shift;

    @$features = map { $_->[2] }

      #     sort { $b->[0] <=> $a->[0] || $b->[1] <=> $a->[1]}
      sort { $b->[1] <=> $a->[1] || $b->[0] <=> $a->[0] }
      map  { [ ( $_->{'start'} || 1 ), _getScore( $_->{'score'} ), $_ ] }
      @$features;

    return $features;
}

sub _getScore {
    my $score = shift;
    if ( ( !defined($score) ) || ( $score eq '.' ) || ( $score eq '-' ) ) {
        return 0;
    }
    return $score;
}

sub _cleanValue {
    my $value = shift;
    if ( !defined($value) ) {
        return ".";
    }
    if ( $value =~ m/^\s*$/ ) {
        return ".";
    }
    $value =~ s/^\s+//g;
    $value =~ s/\s+$//g;
    $value =~ s/\"|\'//g;
    return $value;
}

sub _writeFooter {
    my $options = shift;
    my $footer  = "</cgview>\n";
    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE $footer );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _writeBlast {
    my $options   = shift;
    my $settings  = shift;
    my $global    = shift;
    my $seqObject = shift;
    my $strand    = shift;
    my $file      = shift;
    my $color     = shift;
    my $thickness = shift;
    my $rf        = shift;    #1,2,3, or undefined for all reading frames

    if (   ( _isTrue( $options->{parse_reading_frame} ) )
        && ( _containsReadingFrameInfo( $options, $settings, $global, $file ) )
      )
    {

#The order needs to be 3, 2, 1 because BLAST results are always drawn on the inside
#of the backbone, and the slots are drawn closet to the backbone first.
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, 1, $file,
            $color,   $thickness, 3
        );
        if ( _isTrue( $options->{draw_divider_rings} ) ) {
            _drawDivider( $options, $settings, $global, $seqObject, $strand,
                0.25 );
        }
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, 1, $file,
            $color,   $thickness, 2
        );
        if ( _isTrue( $options->{draw_divider_rings} ) ) {
            _drawDivider( $options, $settings, $global, $seqObject, $strand,
                0.25 );
        }
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, 1, $file,
            $color,   $thickness, 1
        );
        _drawDivider( $options, $settings, $global, $seqObject, $strand, 1 );
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, -1, $file,
            $color,   $thickness, 1
        );
        if ( _isTrue( $options->{draw_divider_rings} ) ) {
            _drawDivider( $options, $settings, $global, $seqObject, $strand,
                0.25 );
        }
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, -1, $file,
            $color,   $thickness, 2
        );
        if ( _isTrue( $options->{draw_divider_rings} ) ) {
            _drawDivider( $options, $settings, $global, $seqObject, $strand,
                0.25 );
        }
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, -1, $file,
            $color,   $thickness, 3
        );
    }
    else {
        _writeBlastResults(
            $options, $settings,  $global, $seqObject, 1, $file,
            $color,   $thickness, undef
        );
    }
}

sub _drawDivider {

    my $options                = shift;
    my $settings               = shift;
    my $global                 = shift;
    my $seqObject              = shift;
    my $strand                 = shift;    #1 or -1
    my $proportion_of_backbone = shift;
    my $divider_color          = shift;    # optional

    $divider_color =
      defined($divider_color) ? $divider_color : $settings->{backboneColor};

    my $opacity     = $settings->{'featureOpacity'};
    my @outputArray = ();

    #if showing contigs increase thickness to double the backbone thickness
    if (   ( scalar( @{ $global->{contigs} } ) > 1 )
        && ( _isTrue( $options{'show_contigs'} ) ) )
    {
        $proportion_of_backbone = 2.0;
    }

    my $decoration = "arc";
    if ( $strand == 1 ) {
        push( @outputArray,
                "<featureSlot featureThickness=\""
              . $settings->{backboneThickness} * $proportion_of_backbone
              . "\" showShading=\"true\" strand=\"direct\">\n" );
    }
    else {
        push( @outputArray,
                "<featureSlot featureThickness=\""
              . $settings->{backboneThickness} * $proportion_of_backbone
              . "\" showShading=\"true\" strand=\"reverse\">\n" );
    }

    if (   ( scalar( @{ $global->{contigs} } ) == 1 )
        || ( !( _isTrue( $options{'show_contigs'} ) ) ) )
    {

        push( @outputArray,
            "<feature color=\"$divider_color\" decoration=\"$decoration\">\n" );
        push( @outputArray,
            "<featureRange start=\"1\" stop=\"$global->{length}\">\n" );
        push( @outputArray, "</featureRange>\n" );
        push( @outputArray, "</feature>\n" );

    }
    else {
        #even-number contigs
        push( @outputArray,
"<feature color=\"$divider_color\" decoration=\"$decoration\" proportionOfThickness=\"0.50\" radiusAdjustment=\"1.0\" >\n"
        );

        my $count = 0;
        foreach my $contig ( @{ $global->{contigs} } ) {
            $count++;
            if ( $count % 2 == 0 ) {
                push( @outputArray,
"<featureRange start=\"$contig->{start}\" stop=\"$contig->{end}\">\n"
                );
                push( @outputArray, "</featureRange>\n" );
            }
        }
        push( @outputArray, "</feature>\n" );

        #odd-number contigs
        push( @outputArray,
"<feature color=\"$divider_color\" decoration=\"$decoration\" proportionOfThickness=\"0.50\" radiusAdjustment=\"0.0\" >\n"
        );

        $count = 0;
        foreach my $contig ( @{ $global->{contigs} } ) {
            $count++;
            if ( $count % 2 == 1 ) {
                push( @outputArray,
"<featureRange start=\"$contig->{start}\" stop=\"$contig->{end}\">\n"
                );
                push( @outputArray, "</featureRange>\n" );
            }
        }
        push( @outputArray, "</feature>\n" );
    }

    push( @outputArray, "</featureSlot>\n" );
    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );
    print( OUTFILE "<!-- divider -->\n" );
    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _writeBlastResults {
    my $options     = shift;
    my $settings    = shift;
    my $global      = shift;
    my $seqObject   = shift;
    my $strand      = shift;
    my $file        = shift;
    my $blast_color = shift;
    my $thickness   = shift;
    my $rf          = shift;    #1,2,3, or undefined for all reading frames

    my $opacity_for_blast;

    if ( _isTrue( $options->{use_opacity} ) ) {
        $opacity_for_blast = "0.5";
    }
    else {
        $opacity_for_blast = "1.0";
    }

    #added 2010-10-25
    if ( defined( $settings->{_cct_blast_thickness} ) ) {
        $thickness = $settings->{_cct_blast_thickness};
    }
    if ( defined( $settings->{_cct_blast_opacity} ) ) {
        $opacity_for_blast = $settings->{_cct_blast_opacity};
    }

    my @outputArray = ();

#blast results are always drawn on the inside of the backbone. Strand is used when parse_reading_frame is specified
    my $decoration = "arc";
    if ( $strand == 1 ) {
        push( @outputArray,
"<featureSlot strand=\"reverse\" showShading=\"false\" featureThickness=\""
              . $thickness
              . "\">\n" );
    }
    else {
        push( @outputArray,
"<featureSlot strand=\"reverse\" showShading=\"false\" featureThickness=\""
              . $thickness
              . "\">\n" );
    }

    #may want to mark query positions on the map, regardless of whether they
    #produced hits.
    if ( _isTrue( $options->{'show_queries'} ) ) {
        my @queries = ();
        @queries = @{ _parseBlastQueries( $file, $settings ) };
        @queries = @{ _sortBLASTByStart( \@queries ) };

        if ( $strand == 1 ) {
            @queries = reverse(@queries);
        }

        #draw queries as faint features
        push( @outputArray, "<feature>\n" );
        foreach (@queries) {

            #if a reading frame is specified, only want to draw
            #queries from this reading frame and strand
            if ( ( defined($rf) ) && ( defined( $_->{q_rf} ) ) ) {
                unless ( ( $rf == $_->{q_rf} )
                    && ( $strand == $_->{q_strand} ) )
                {
                    next;
                }
            }
            push( @outputArray,
"<featureRange start=\"$_->{q_start}\" stop=\"$_->{q_end}\" opacity=\"0.1\" radiusAdjustment=\"0.0\" proportionOfThickness=\"1.0\" color=\"$settings->{orfColor}\" />\n"
            );
        }
        push( @outputArray, "</feature>\n" );
    }

    my @features = ();
    @features = @{ _parseBLAST( $file, $options, $settings, $global ) };

    if ( !( defined( $settings->{blast_heat_map} ) ) ) {
        @features = @{ _sortBLASTByStart( \@features ) };

        if ( $strand == 1 ) {
            @features = reverse(@features);
        }
    }
    else {

  #for BLAST results colored by % identity want most significant hits drawn last
  #so that they are visible. Most significant hits will appear first in list
  #so reverse list.
        @features = reverse(@features);
    }

    foreach (@features) {
        my $feat = $_;

        my $start = $feat->{'q_start'};
        my $stop  = $feat->{'q_end'};

        if ( ( defined($rf) ) && ( defined( $feat->{'q_rf'} ) ) ) {

            my $queryFrame  = $feat->{'q_rf'};
            my $queryStrand = $feat->{'q_strand'};

            unless ( ( $rf == $queryFrame ) && ( $strand == $queryStrand ) ) {
                next;
            }
        }

        my $label;
        if ( _containsText( $feat->{'match_id'} ) ) {
            $label = $feat->{'match_id'};
            if ( length($label) > $settings->{'maxLabelLength'} - 3 ) {
                $label = substr( $label, 0, $settings->{'maxLabelLength'} - 3 )
                  . "...";
            }
            $label = _escapeText($label);
        }

        #gi|15678261
        my $hyperlink;
        if ( _containsText( $feat->{'match_id'} ) ) {
            if ( $feat->{'match_id'} =~ m/gi\|(\d+)/ ) {
                $hyperlink = $global->{'ncbiGiLink'} . $1;
            }

            #GeneID:1132092"
            elsif ( $feat->{'match_id'} =~ m/geneid\|(\d+)/ ) {
                $hyperlink = $global->{'ncbiGeneLink'} . $1;
            }
        }

        my $mouseover;
        if ( _containsText($label) ) {
            $mouseover = $label . "; " . $start . " to " . $stop;
        }
        else {
            $mouseover = $start . " to " . $stop;
        }
        if ( _containsText( $feat->{'match_description'} ) ) {
            $mouseover = $mouseover . " " . $feat->{'match_description'};
        }

        $mouseover =
            $mouseover
          . "; percent identity="
          . $feat->{'%_identity'}
          . "; alignment length="
          . $feat->{alignment_length}
          . "; evalue="
          . $feat->{'evalue'};
        $mouseover = _escapeText($mouseover);

        #now check entries and make tags
        my $opacity = $opacity_for_blast;

#change the color based on % identity if $settings->{blast_heat_map} colors defined
        my $color;
        if ( defined( $settings->{blast_heat_map} ) ) {
            my @cutoffs = keys( %{ $settings->{blast_heat_map} } );
            foreach my $cutoff (@cutoffs) {
                if ( $feat->{'%_identity'} >= $cutoff ) {
                    $color = $settings->{blast_heat_map}->{$cutoff};
                    last;
                }
            }
        }
        else {
            $color = $blast_color;
        }

        my $feature =
"<feature color=\"$color\" decoration=\"$decoration\" opacity=\"$opacity\" ";

        if ( _isTrue( $options->{hit_labels} ) ) {
            if ( _containsText($label) ) {
                $feature = $feature . "label=\"$label\" ";
            }
            if ( _containsText($hyperlink) ) {
                $feature = $feature . "hyperlink=\"$hyperlink\" ";
            }
            if ( _containsText($mouseover) ) {
                $feature = $feature . "mouseover=\"$mouseover\" ";
            }
        }

        $feature = $feature . ">\n";

        my $thickness = "1.0";
        if ( _isTrue( $options->{scale_blast} ) ) {

            #use %_identity to determine thickness of drawn feature
            $thickness = $feat->{'%_identity'} / 100.0;
            $thickness = sprintf( "%.10f", ($thickness) );
        }

        my $featureRange =
"<featureRange start=\"$start\" stop=\"$stop\" radiusAdjustment=\"0.0\" proportionOfThickness=\"$thickness\" />\n";

        #now add $feature and $featureRange to @outputArray
        push( @outputArray, $feature . $featureRange . "</feature>\n" );
    }

    push( @outputArray, "</featureSlot>\n" );
    my $strandTerm = undef;
    if ( $strand == 1 ) {
        $strandTerm = "forward";
    }
    if ( $strand == -1 ) {
        $strandTerm = "reverse";
    }

    my $rfTerm = undef;
    if ( !( defined($rf) ) ) {
        $rfTerm = "1,2,3";
    }
    else {
        $rfTerm = $rf;
    }

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );

    if ( defined($rf) ) {
        print( OUTFILE
              "<!-- BLAST results on $strandTerm strand in rf $rfTerm -->\n" );
    }
    else {
        print( OUTFILE "<!-- BLAST results -->\n" );
    }

    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");
}

sub _containsReadingFrameInfo {
    my $options  = shift;
    my $settings = shift;
    my $global   = shift;
    my $file     = shift;

    return _parseBLAST( $file, $options, $settings, $global, 1 );
}

#2011-02-16
sub _getBlastProgram {
    my $file    = shift;
    my $program = undef;
    open( INFILE, $file ) or die("Cannot open the BLAST results file $file");
    while ( my $line = <INFILE> ) {
        $line =~ s/\cM|\n//g;
        if ( $line =~ m/^\#PROGRAM\s*=\s*([^\s]+)/ ) {
            $program = $1;
            last;
        }
    }
    close(INFILE) or die("Cannot close file : $!");
    return $program;
}

sub _parseBLAST {
    my $file                         = shift;
    my $options                      = shift;
    my $settings                     = shift;
    my $global                       = shift;
    my $check_for_reading_frame_info = shift;

#The file can contain comments starting with'#'
#The file must have a line beginning with a 'query_id' and indicating the column names:
#query_id   match_id    match_description   %_identity  alignment_length    mismatches  gap_openings    q_start q_end   s_start s_end   evalue  bit_score

    my @required = (
        'query_id',         '%_identity', 'q_start', 'q_end',
        'alignment_length', 'evalue'
    );

    my $lineCount    = 0;
    my @columnTitles = ();
    my $columnsRead  = 0;

#program will be used to store the value of #PROGRAM in the blast results header.
#if it is blastp or tblastn then q_start and q_end are in residues and need to
#be converted to bases.
    my $program = undef;

    open( INFILE, $file ) or die("Cannot open the BLAST results file $file");

    #check for column titles
    while ( my $line = <INFILE> ) {
        $line =~ s/\cM|\n//g;
        $lineCount++;
        if ( $line =~ m/^\#PROGRAM\s*=\s*([^\s]+)/ ) {
            $program = $1;
        }
        if ( $line =~ m/^\#/ ) {
            next;
        }
        if ( $line =~ m/^query_id/ ) {
            $columnsRead  = 1;
            @columnTitles = @{ _split($line) };
            last;
        }
    }

    if ( !( defined($program) ) ) {
        die("Cannot parse the #PROGRAM field in the BLAST results file $file");
    }

    #print Dumper(@columnTitles);

    #now check for required columns
    foreach (@required) {
        my $req   = $_;
        my $match = 0;
        foreach (@columnTitles) {
            my $columnTitle = $_;
            if ( $columnTitle eq $req ) {
                $match = 1;
                last;
            }
        }
        if ( !($match) ) {
            die(
"The BLAST results in $file do not contain a column labeled $req"
            );
        }
    }

    #factor to convert amino acid scales to base scales
    my $scale;
    if ( ( $program =~ m/^blastp$/ ) || ( $program =~ m/^tblastn$/ ) ) {
        $scale = 3;
    }
    else {
        $scale = 1;
    }

    #read the remaining entries
    my @entries = ();
    while ( my $line = <INFILE> ) {
        $line =~ s/\cM|\n//g;
        $lineCount++;
        if ( $line =~ m/^\#/ ) {
            next;
        }
        if ( $line =~ m/\S/ ) {
            my @values = @{ _split($line) };

            #skip lines with missing values
            if ( scalar(@values) != scalar(@columnTitles) ) {
                next;
            }

            my %entry = ();
            for ( my $i = 0 ; $i < scalar(@columnTitles) ; $i++ ) {
                $entry{ $columnTitles[$i] } = $values[$i];
            }

            #do some error checking of values
            #check query_id, %_identity, q_start, and q_end
            #skip if no identity value
            if ( !( $entry{'%_identity'} =~ m/\d/ ) ) {
                die("No \%_identity value BLAST results $file line $lineCount");
            }

            if ( !( $entry{'q_start'} =~ m/\d/ ) ) {
                die("No q_start value BLAST results $file line $lineCount");
            }
            if ( !( $entry{'q_end'} =~ m/\d/ ) ) {
                die("No q_end value BLAST results $file line $lineCount");
            }

#Note that the following example blast result is not handled properly:
#tagA;pO157p01;_start=92527;end=2502;strand=1;rf=1       tagA;pO157p01;_start=92527;end=2502;strand=1;rf=1       -       100.00  898     0       0       1       898     1       898     0.0     1845
#This is because the feature spans the end/start boundary of the circular sequence.
#For now such features will be skipped.

    #try to add reading frame and strand information using information
    #in the query_id:
    #orf3_start=3691;end=3858;strand=1;rf=1
    #orf16_start=8095;end=8178;strand=1;rf=1
    #
    #The q_start and q_end values are to be the region of the query that matched
    #the hit. Depending on the search type, these can be in amino acids
    #or bases. The values in the query_id are always in bases. The
    #scale factor is used to convert the q_start and q_end values
    #so that they can be used to adjust the values in the query_id.
    #This allows hits to be mapped to the genomic sequence
            if ( $entry{'query_id'} =~
                m/start=(\d+);end=(\d+);strand=(\-*\d+);rf=(\d+)\s*$/ )
            {

                my $genome_start  = $1;
                my $genome_end    = $2;
                my $genome_strand = $3;
                my $genome_rf     = $4;
                if (   ( $program =~ m/^blastp$/ )
                    || ( $program =~ m/^tblastn$/ ) )
                {
                    $entry{'q_rf'} = $genome_rf;
                }

                my $match_length_bases;
                if ( $entry{'q_start'} > $entry{'q_end'} ) {
                    my $temp = $entry{'q_start'};
                    $entry{'q_start'} = $entry{'q_end'};
                    $entry{'q_end'}   = $temp;
                    $genome_strand    = $genome_strand * -1;
                    $entry{'q_rf'}    = undef;
                }

                $match_length_bases =
                  ( $entry{'q_end'} - $entry{'q_start'} + 1 ) * $scale;

                if (
                    ( $genome_strand == -1 )
                    && (   ( $program =~ m/^blastp$/ )
                        || ( $program =~ m/^tblastn$/ ) )
                  )
                {
                    $entry{'q_strand'} = -1;
                    $entry{'q_end'} =
                      $genome_end -
                      ( $entry{'q_start'} * $scale ) +
                      ( 1 * $scale );
                    $entry{'q_start'} =
                      $entry{'q_end'} - $match_length_bases + 1;
                }
                else {
                    $entry{'q_strand'} = 1;
                    $entry{'q_start'} =
                      $genome_start +
                      ( $entry{'q_start'} * $scale ) -
                      ( 1 * $scale );
                    $entry{'q_end'} =
                      $entry{'q_start'} + $match_length_bases - 1;
                }
            }

 #try to determine reading frame of query even if not explicitly given in title,
 #by using the position of the hit relative to the source sequence.
 #sequence_start=286;end=385;length=100;source_length=1000
 #start and end refer to direct strand
 #
            if ( $entry{'query_id'} =~
                m/start=(\d+);end=(\d+);length=\d+;source_length=(\d+)\s*$/ )
            {

                my $genome_start  = $1;
                my $genome_end    = $2;
                my $genome_length = $3;
                my $genome_strand = 1;

                my $match_length_bases;
                if ( $entry{'q_start'} > $entry{'q_end'} ) {
                    my $temp = $entry{'q_start'};
                    $entry{'q_start'}  = $entry{'q_end'};
                    $entry{'q_end'}    = $temp;
                    $genome_strand     = $genome_strand * -1;
                    $entry{'q_strand'} = -1;
                }
                else {
                    $entry{'q_strand'} = 1;
                }

                $match_length_bases =
                  ( $entry{'q_end'} - $entry{'q_start'} + 1 ) * $scale;

                $entry{'q_start'} =
                  $genome_start +
                  ( $entry{'q_start'} * $scale ) -
                  ( 1 * $scale );
                $entry{'q_end'} = $entry{'q_start'} + $match_length_bases - 1;

                if (   ( $program =~ m/^blastx$/ )
                    || ( $program =~ m/^tblastx$/ ) )
                {

                    if ( $entry{'q_strand'} == 1 ) {
                        $entry{'q_rf'} = $entry{'q_start'} % 3;
                    }
                    elsif ( $entry{'q_strand'} == -1 ) {
                        my $end = $entry{'q_end'};
                        $entry{'q_rf'} = ( $genome_length - $end + 1 ) % 3;
                    }

                    if ( $entry{'q_rf'} == 0 ) {
                        $entry{'q_rf'} = 3;
                    }
                }
                else {
                    $entry{'q_rf'} = undef;
                }

            }

#try to adjust q_start and q_end using information in title when rf and strand not specified
#start=X;end=Y
            if ( $entry{'query_id'} =~ m/start=(\d+);end=(\d+)\s*$/ ) {

                my $genome_start  = $1;
                my $genome_end    = $2;
                my $genome_strand = 1;

                my $match_length_bases;
                if ( $entry{'q_start'} > $entry{'q_end'} ) {
                    my $temp = $entry{'q_start'};
                    $entry{'q_start'} = $entry{'q_end'};
                    $entry{'q_end'}   = $temp;
                    $genome_strand    = $genome_strand * -1;
                }

                $match_length_bases =
                  ( $entry{'q_end'} - $entry{'q_start'} + 1 ) * $scale;

                $entry{'q_strand'} = 1;
                $entry{'q_start'} =
                  $genome_start +
                  ( $entry{'q_start'} * $scale ) -
                  ( 1 * $scale );
                $entry{'q_end'} = $entry{'q_start'} + $match_length_bases - 1;
            }

            if ( $entry{'q_start'} < 1 ) {
                _message( $options,
                        "Warning: q_start value "
                      . $entry{'q_start'}
                      . " is less than 1 BLAST results $file line $lineCount" );
                $entry{'q_start'} = $entry{'q_start'} + $global->{'length'};
            }
            if ( $entry{'q_end'} < 1 ) {

                _message( $options,
                        "Warning: q_end value "
                      . $entry{'q_end'}
                      . " is less than 1 BLAST results $file line $lineCount" );
                $entry{'q_end'} = 1;
            }
            if ( $entry{'q_start'} > $global->{'length'} ) {

                _message( $options,
                        "Warning: q_start value "
                      . $entry{'q_start'}
                      . " is greater than sequence length "
                      . $global->{'length'}
                      . " BLAST results $file line $lineCount" );
                $entry{'q_start'} = $global->{'length'};
            }
            if ( $entry{'q_end'} > $global->{'length'} ) {

                _message( $options,
                        "Warning: q_end value "
                      . $entry{'q_end'}
                      . " is greater than sequence length "
                      . $global->{'length'}
                      . " BLAST results $file line $lineCount" );
                $entry{'q_end'} = $entry{'q_end'} - $global->{'length'};
            }
            if ($check_for_reading_frame_info) {
                if ( defined( $entry{'q_rf'} ) ) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
            push( @entries, \%entry );
        }
    }
    close(INFILE) or die("Cannot close file : $!");
    return \@entries;
}

sub _sortBLASTByStart {
    my $features = shift;

    @$features = map { $_->[1] }
      sort { $a->[0] <=> $b->[0] }
      map { [ _getSortValueBLAST($_), $_ ] } @$features;

    return $features;
}

sub _getSortValueBLAST {
    my $feature = shift;
    return $feature->{'q_start'};
}

sub _parseBlastQueries {
    my $file      = shift;
    my $lineCount = 0;

    open( INFILE, $file ) or die("Cannot open the BLAST results file $file");

    #won't bother checking columns.
    my %entries = ();
    while ( my $line = <INFILE> ) {
        $line =~ s/\cM|\n//g;
        $lineCount++;

        #orf1_start=142;end=255;strand=1;rf=1
        #orf_900_start=142;end=255;strand=1;rf=1
        #some gene_start=142;end=255;strand=1;rf=1
        if ( $line =~
            m/^([^\t]+)_start=(\d+);end=(\d+);strand=(\-*\d+);rf=(\d+)/ )
        {
            my %entry = ();
            $entry{'q_start'}             = $2;
            $entry{'q_end'}               = $3;
            $entry{'q_strand'}            = $4;
            $entry{'q_rf'}                = $5;
            $entries{ $1 . $2 . $3 . $4 } = \%entry;
        }

        #some gene_start=142;end=255
        elsif ( $line =~ m/^([^\t]+)_start=(\d+);end=(\d+)/ ) {
            my %entry = ();
            $entry{'q_start'}        = $2;
            $entry{'q_end'}          = $3;
            $entry{'q_strand'}       = "1";
            $entry{'q_rf'}           = undef;
            $entries{ $1 . $2 . $3 } = \%entry;
        }
    }
    close(INFILE) or die("Cannot close file : $!");
    my @values = values(%entries);
    return \@values;
}

sub _writeBaseContent {
    my $options   = shift;
    my $settings  = shift;
    my $seqObject = shift;

#1 or -1. Strand here only determines whether graph is drawn on outside or inside of backbone.
    my $strand = shift;

    #type should be gc_content, at_content, gc_skew, or at_skew.
    my $type = shift;

    my $plotTerm;
    if ( $type eq 'gc_content' ) {
        $plotTerm = "GC content";
    }
    elsif ( $type eq 'gc_skew' ) {
        $plotTerm = "GC skew";
    }
    elsif ( $type eq 'at_content' ) {
        $plotTerm = "AT content";
    }
    elsif ( $type eq 'at_skew' ) {
        $plotTerm = "AT skew";
    }

    my $globalBaseContentInfo =
      _getGlobalBaseContentInfo( $options, $settings, $seqObject, $type );

#The max, min, and average have been scaled for some plot types to facilatate CGView plotting.
#When returning these to the user, adjust the values so that they are in the standard ranges.
    my $actualMax;
    my $actualMin;
    my $actualAverage;

    if ( ( $type eq 'gc_content' ) || ( $type eq 'at_content' ) ) {
        $actualMax     = $globalBaseContentInfo->{'max'};
        $actualMin     = $globalBaseContentInfo->{'min'};
        $actualAverage = $globalBaseContentInfo->{'average'};
    }
    elsif ( ( $type eq 'gc_skew' ) || ( $type eq 'at_skew' ) ) {
        $actualMax     = _skewCorrect( $globalBaseContentInfo->{'max'} );
        $actualMin     = _skewCorrect( $globalBaseContentInfo->{'min'} );
        $actualAverage = _skewCorrect( $globalBaseContentInfo->{'average'} );
    }

    $actualMax     = sprintf( "%.4f", $actualMax );
    $actualMin     = sprintf( "%.4f", $actualMin );
    $actualAverage = sprintf( "%.4f", $actualAverage );

    _message( $options,
"Plotting $plotTerm using a window size of $options->{window} and a step of $options->{step}."
    );
    _message( $options, "The maximum $plotTerm value is $actualMax." );
    _message( $options, "The minimum $plotTerm value is $actualMin." );
    _message( $options, "The average $plotTerm value is $actualAverage." );

    if ( _isTrue( $options->{scale} ) ) {
        _message( $options,
            "$plotTerm will be scaled based on the maximum and minimum values."
        );
    }

    if ( _isTrue( $options->{average} ) ) {
        _message( $options,
            "$plotTerm will be plotted as the deviation from the average value."
        );
    }

    my $opacity    = $settings->{'featureOpacity'};
    my $decoration = 'arc';
    my $positiveColor;
    my $negativeColor;
    if ( $type eq 'gc_content' ) {
        $positiveColor = $settings->{'gcColorPos'};
        $negativeColor = $settings->{'gcColorNeg'};
    }
    elsif ( $type eq 'at_content' ) {
        $positiveColor = $settings->{'atColorPos'};
        $negativeColor = $settings->{'atColorNeg'};
    }
    elsif ( $type eq 'gc_skew' ) {
        $positiveColor = $settings->{'gcSkewColorPos'};
        $negativeColor = $settings->{'gcSkewColorNeg'};
    }
    elsif ( $type eq 'at_skew' ) {
        $positiveColor = $settings->{'atSkewColorPos'};
        $negativeColor = $settings->{'atSkewColorNeg'};
    }

    my $upstreamLength   = sprintf( "%.f", $options->{'window'} / 2 );
    my $downstreamLength = $options->{'window'} - $upstreamLength;
    my $step             = $options->{'step'};
    my $isLinear         = undef;
    if ( $settings->{'isLinear'} eq "true" ) {
        $isLinear = 1;
    }
    else {
        $isLinear = 0;
    }

    my $dna            = $seqObject->seq();
    my $originalLength = length($dna);
    my $subseq;
    my $value;
    my $positionCorrection = 0;
    my $firstBase;
    my $lastBase;

    if ( !($isLinear) ) {
        my $prefix =
          substr( $dna, length($dna) - $upstreamLength, $upstreamLength );
        my $suffix = substr( $dna, 0, $downstreamLength );
        $dna                = $prefix . $dna . $suffix;
        $positionCorrection = length($prefix);
    }

    my $length = length($dna);
    my $maxDeviationUp =
      $globalBaseContentInfo->{'max'} - $globalBaseContentInfo->{'average'};
    my $maxDeviationDown =
      $globalBaseContentInfo->{'average'} - $globalBaseContentInfo->{'min'};
    my $average = $globalBaseContentInfo->{'average'};
    my $maxDeviation;
    if ( $maxDeviationUp > $maxDeviationDown ) {
        $maxDeviation = $maxDeviationUp;
    }
    else {
        $maxDeviation = $maxDeviationDown;
    }

    my @outputArray = ();
    if ( $strand == 1 ) {
        push( @outputArray,
"<featureSlot showShading=\"false\" minimumFeatureLength=\"0.1\" strand=\"direct\" featureThickness=\""
              . $settings->{'featureThicknessPlot'}
              . "\">\n" );
    }
    else {
        push( @outputArray,
"<featureSlot showShading=\"false\" minimumFeatureLength=\"0.1\" strand=\"reverse\" featureThickness=\""
              . $settings->{'featureThicknessPlot'}
              . "\">\n" );
    }

    push( @outputArray,
        "<feature decoration=\"$decoration\" opacity=\"$opacity\">\n" );

    for (
        my $i = 1 + $upstreamLength ;
        $i <= $length - $downstreamLength ;
        $i = $i + $step
      )
    {

        $subseq = substr(
            $dna,
            $i - $upstreamLength - 1,
            ( $i + $downstreamLength ) - ( $i - $upstreamLength - 1 )
        );

        #These set the width and position of the "point" on the map.
        #They are not the actual first base and last base in the sliding window.
        $firstBase = $i - $positionCorrection;
        $lastBase  = $firstBase + $step;

        ####2007-01-14
        ####This hack should prevent background from appearing in plots.
        if (   ( $originalLength > 10000 )
            || ( $settings->{backboneRadius} > 1000 ) )
        {
            $lastBase  = $lastBase + sprintf( "%.f", ( 2.0 * $step ) );
            $firstBase = $firstBase - sprintf( "%.f", ( 2.0 * $step ) );
        }
        ###

        if ( $firstBase < 1 ) {
            $firstBase = 1;
        }

        if ( $lastBase > $originalLength ) {
            $lastBase = $originalLength;
        }

        $value = _calc( $type, $subseq );

#want bars above middle line for values > 0.5 and below middle line for values < 0.5

        my $barHeight;
        my $radiusShift;
        my $color;

        if ( $value > $average ) {
            $color       = $positiveColor;
            $barHeight   = $value - $average;
            $barHeight   = $barHeight * 0.5 / $maxDeviation;
            $radiusShift = 0.5 + $barHeight / 2.0;
        }
        elsif ( $value < $average ) {
            $color       = $negativeColor;
            $barHeight   = $average - $value;
            $barHeight   = $barHeight * 0.5 / $maxDeviation;
            $radiusShift = 0.5 - $barHeight / 2;
        }
        else {
            $color       = $positiveColor;
            $radiusShift = 0.5;
            $barHeight   = $settings->{'plotLineThickness'};
        }

        push( @outputArray,
                "<featureRange color=\""
              . $color
              . "\" start=\"$firstBase\" stop=\"$lastBase\" proportionOfThickness=\""
              . $barHeight
              . "\" radiusAdjustment=\"$radiusShift\" />\n" );
    }

    push( @outputArray, "</feature>\n" );

    push( @outputArray, "</featureSlot>\n" );
    my $strandTerm = undef;
    if ( $strand == 1 ) {
        $strandTerm = "forward";
    }
    if ( $strand == -1 ) {
        $strandTerm = "reverse";
    }

    open( OUTFILE, "+>>" . $options->{"output"} )
      or die("Cannot open file : $!");
    print( OUTFILE "\n\n\n\n" );

    #    print (OUTFILE "<!-- $plotTerm for $strandTerm strand -->\n");
    print( OUTFILE "<!-- $plotTerm -->\n" );
    print( OUTFILE join( "", @outputArray ) );
    print( OUTFILE "\n" );
    close(OUTFILE) or die("Cannot close file : $!");

}

sub _getGlobalBaseContentInfo {
    my $options   = shift;
    my $settings  = shift;
    my $seqObject = shift;

    #type should be gc_content, at_content, gc_skew, or at_skew.
    my $type = shift;

    my %globalBaseContentInfo = (
        min     => 1,
        max     => 0,
        average => 0
    );

    my $upstreamLength   = sprintf( "%.f", $options->{'window'} / 2 );
    my $downstreamLength = $options->{'window'} - $upstreamLength;
    my $step             = $options->{'step'};
    my $isLinear         = undef;
    if ( $settings->{'isLinear'} eq "true" ) {
        $isLinear = 1;
    }
    else {
        $isLinear = 0;
    }

    my $dna = $seqObject->seq();

    if ( _isTrue( $options->{'average'} ) ) {
        $globalBaseContentInfo{'average'} = _calc( $type, $dna );
    }
    else {
        $globalBaseContentInfo{'average'} = 0.5;
    }

    if ( !( _isTrue( $options->{'scale'} ) ) ) {
        $globalBaseContentInfo{'min'} = 0;
        $globalBaseContentInfo{'max'} = 1;
        return \%globalBaseContentInfo;
    }

    my $subseq;
    my $value;

    if ( !($isLinear) ) {
        my $prefix =
          substr( $dna, length($dna) - $upstreamLength, $upstreamLength );
        my $suffix = substr( $dna, 0, $downstreamLength );
        $dna = $prefix . $dna . $suffix;
    }

    my $length = length($dna);

    for (
        my $i = 1 + $upstreamLength ;
        $i <= $length - $downstreamLength ;
        $i = $i + $step
      )
    {

        $subseq = substr(
            $dna,
            $i - $upstreamLength - 1,
            ( $i + $downstreamLength ) - ( $i - $upstreamLength - 1 )
        );

        $value = _calc( $type, $subseq );

        if ( $value > $globalBaseContentInfo{'max'} ) {
            $globalBaseContentInfo{'max'} = $value;
        }

        if ( $value < $globalBaseContentInfo{'min'} ) {
            $globalBaseContentInfo{'min'} = $value;
        }
    }

    return \%globalBaseContentInfo;

}

sub _calc {
    my $type = shift;
    my $dna  = shift;

    if ( $type eq "gc_content" ) {
        return _calcGCContent($dna);
    }
    elsif ( $type eq "at_content" ) {
        return _calcATContent($dna);
    }
    elsif ( $type eq "gc_skew" ) {
        return _calcGCSkew($dna);
    }
    elsif ( $type eq "at_skew" ) {
        return _calcATSkew($dna);
    }
    else {
        die("unknown calc type");
    }
}

sub _calcGCContent {
    my $dna   = lc(shift);
    my $total = length($dna);
    my $g     = ( $dna =~ tr/g/G/ );
    my $c     = ( $dna =~ tr/c/C/ );

    if ( $total == 0 ) {
        return 0.5;
    }

    return sprintf( "%.5f", ( ( $g + $c ) / $total ) );
}

sub _calcATContent {
    my $dna   = lc(shift);
    my $total = length($dna);
    my $a     = ( $dna =~ tr/a/A/ );
    my $t     = ( $dna =~ tr/t/T/ );

    if ( $total == 0 ) {
        return 0.5;
    }

    return sprintf( "%.5f", ( ( $a + $t ) / $total ) );
}

sub _calcGCSkew {
    my $dna = lc(shift);
    my $c   = ( $dna =~ tr/c/C/ );
    my $g   = ( $dna =~ tr/g/G/ );

    if ( ( $g + $c ) == 0 ) {
        return 0.5;
    }

    #gives value between -1 and 1
    my $value = ( $g - $c ) / ( $g + $c );

    #scale to a value between 0 and 1
    $value = 0.5 + $value / 2.0;

    return sprintf( "%.5f", ($value) );
}

sub _calcATSkew {
    my $dna = lc(shift);
    my $a   = ( $dna =~ tr/a/A/ );
    my $t   = ( $dna =~ tr/t/T/ );

    if ( ( $a + $t ) == 0 ) {
        return 0.5;
    }

    #gives value between -1 and 1
    my $value = ( $a - $t ) / ( $a + $t );

    #scale to a value between 0 and 1
    $value = 0.5 + $value / 2.0;

    return sprintf( "%.5f", ($value) );
}

#for reporting values to user
sub _skewCorrect {
    my $value = shift;
    return ( 2 * ( $value - 0.5 ) );
}

#2011_06_18
sub parse_labels_to_show {
    my $options = shift;
    my $file    = $options->{labels_to_show};
    my %hash    = ();

    adjust_newlines( $file, 'labels' );

    open( my $INFILE, $file ) or die("Cannot open the labels file '$file'");

    while ( my $line = <$INFILE> ) {
        if ( $line =~ m/^\#/ ) {
            next;
        }
        if ( $line =~ m/\S/ ) {
            chomp($line);
            my @values = @{ _split($line) };
            if ( scalar(@values) != 2 ) {
                _message( $options,
                    "Unexpected number of fields in label record '$line'." );
                next;
            }
            $hash{ $values[0] } = $values[1];
        }
    }

    close($INFILE) or die("Cannot close file : $!");
    return \%hash;
}

# Converts newlines in a file to the format used by local system
# This sub routine overwrites the original file
sub adjust_newlines {
    my ( $file_path, $file_type ) = @_;
    my $text;

    open( my $INFILE, '<', $file_path )
      or die("Cannot open $file_type file '$file_path': $!");
    {
        local $/ = undef;
        $text = <$INFILE>;
    }
    close($INFILE) or die("Cannot close $file_type file '$file_path': $!");

    $text =~ s/\012\015?|\015\012?/\n/g;

    open( my $OUTFILE, '>', $file_path )
      or die("Cannot open $file_type file '$file_path': $!");
    print {$OUTFILE} $text;
    close($OUTFILE) or die("Cannot close $file_type file: $!");
}

sub print_usage {
    print <<BLOCK;
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

-show_contigs - Whether to stagger divider rings to show the boundaries of
contigs in sequences consisting of multiple contigs. [T/F]. Default is T.

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
BLOCK
}
