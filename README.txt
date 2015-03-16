Question Generation via Overgenerating Transformations and Ranking
Michael Heilman and Noah A. Smith

A software package for generating questions about the factual information
present in a given text.


-------------------------
Disclaimer

This is research code.  Though we have attempted to 
make it fairly well organized and reasonably robust to messy text input,
it is not perfectly engineered.
If you decide to use it, do not expect support,
either with getting it running or with making modifications.
Use at your own risk :-)


-------------------------
Licensing Information

This software is distributed under the GNU GPL license.  
See licenses/LICENSE.txt for details.
Licenses for libraries used by the system are also included 
in the "licenses" directory.

-Apache Commons Lang (http://commons.apache.org/lang/)
-Apahce Commons Logging (http://commons.apache.org/logging/)
-JUnit (http://www.junit.org/)
-JWNL (http://sourceforge.net/projects/jwordnet/)
-Stanford NLP tools (http://www-nlp.stanford.edu/software/)
-WordNet (http://wordnet.princeton.edu/)
-The sst-light-0.4 release of the SuperSenseTagger, from which we used the SemCor data for training the supersense tagger (http://sourceforge.net/projects/supersensetag/)
-The Semcor corpus, used for training the supersense tagger (http://www.cse.unt.edu/~rada/downloads.html#semcor)
-The WEKA toolkit, version 3.6.0 (http://www.cs.waikato.ac.nz/ml/weka/)

-----------------------
Running the system

To run the program, execute the script run.sh 
(or just run the command it includes).  It takes plain text on standard input
and prints questions on standard output.  You may want to
run the parsing and tagging servers first (see next section).

Version 1.6.0_07 of Java was used in developing the system.
The code is packaged up for use on UNIX systems,
or for use in the Eclipse IDE.


-----------------------
Running the parsing and tagging servers

Before running the program, you may want to 
start the socket servers for the parser and supersense tagger
(if you do this, then the actual question generation program
will require less memory).
If the socket server is running, then the system
will not load up the Stanford Parser, which takes 
a lot of time and memory.

To start the parser socket server, execute
runStanfordParserServer.sh

You can change whether the Stanford Parser uses
a lexicalized (englishFactored.ser.gz, the default) 
or and unlexicalized grammar (englishPCFG.ser.gz).

To change what port the socket server uses, modify
the runStanfordParserServer.sh script and the file
config/QuestionTransducer.properties.

To change which grammar the system will load if the socket server is not running,
modify the parserGrammarFile property in 
config/factual-statement-extractor.properties.

There is also a supersense tagging server for deciding WH words 
from high-level word senses (see below).  To run it, execute the script runSSTServer.sh.


-----------------------
Running the program without the script:

The main class for generating questions is edu.cmu.ark.QuestionAsker.
You can create questions from a text given on STDIN by 
calling this class's main method, as in the run.sh script.
There are several command-line arguments:

--debug
tell the program to print out debugging information about 
the step-by-step process of making questions

--verbose
tell the program to print out questions in tab-delimited verbose format,
with source sentences, scores, etc.

--model PATH
load the ranking model at PATH 
(e.g., models/linear-regression-ranker-reg500.ser.gz)

--keep-pro
Tell the program to keep questions with unresolved pronouns 
(e.g., What did he like?), which are excluded from the output by
default.  Questions with answers that are unresolved pronouns
(e.g., Who liked pizza?) are not dropped.

--downweight-pro
Tell the program to keep questions with unresolved pronouns
but also downweight them so they appear towards the end of the ranked list.

--downweight-frequent-answers
Tell the program to downweight questions whose answers
are noun phrases that appear very frequently in the input text
(5+ times and constituting 5+% of non-stop word nouns).

--properties PATH
Set the properties file that tells the program
where to look for resources 
(config/QuestionTransducer.properties by default).

--prefer-wh
Tell the program to downweight yes-no questions so
that they appear mostly behind WH questions in the ranked list

--just-wh
Tell the program to exclude yes-no questions from 
the output (i.e., to only output WH questions)

--full-npc
Perform full noun phrase clarification, replacing coreferent noun
phrases with their first mentions in the text (as identified using
the ARKref coreference tool).  
By default, the system only resolves (i.e., replaces) pronouns.

--no-npc
Do not replace any pronouns or other noun phrases with antecedent mentions.
By default, pronouns are replaced.

--max-length N
Skip any questions longer than N tokens.



-----------------------
Ranking model

A question ranking model trained on human judgments of output from stages 1 and 2 is provided here: 
models/linear-regression-ranker-reg500.ser.gz.


------------------------
Unit Testing

A suite of JUnit tests is provided to ensure that the system is working properly. 
See TestQuestions.java, TestSentenceSimplifier.java, and TestWhPhraseGenerator.java
(under src/edu/cmu/ark).  These unit tests were developed with JUnit 3.8.2 (http://www.junit.org/).



--------------------------
Several other packages are incorporated into the system.
See the "licenses" subdirectory for licensing information.


--------------------------
Stanford Parser, Tregex and Tsurgeon

See the distributions of the Stanford Parser and NER system for more information about how they work,
their source code, licenses (also GNU), etc.

http://nlp.stanford.edu/software



---------------------------
WEKA

See the webpage or distribution of the WEKA machine learning package for more information
about how it works, its license (also GNU), etc.  The system uses WEKA version 3.6.

http://www.cs.waikato.ac.nz/ml/weka/



---------------------------
JWNL

See the webpage of the Java WordNet Library for more information about 
how it works, its license, etc.  The system uses version 1.4.1 rc2.

http://sourceforge.net/projects/jwordnet/


---------------------------
ARKref and Supersense Tagging libraries

ARKref (lib/arkref.jar) is a simple noun phrase coreference resolution module.
It is based on the syntactic module described in Haghighi & Klein, EMNLP 2009.
This package includes version 20110321 of ARKref.
For more details, see http://www.ark.cs.cmu.edu/ARKref/.

The system also uses a supersense tagger (lib/supersense-tagger.jar) to decide appropriate WH words.  The tagger labels word tokens with high-level semantic types (e.g., noun.person, noun.time, etc.).  It is a java reimplementation of the system described in the following paper:

M. Ciaramita and Y. Altun.  2006.  Broad-Coverage Sense Disambiguation and Information Extraction with a Supersense Sequence Tagger.  In Proc. of EMNLP.

For a standalone version of the SST, see
http://www.ark.cs.cmu.edu/mheilman/SST.

---------------------------
Language model

The language model config/anc-v2-written.lm.gz
was created from the written portion of the  American National Corpus 
Second Release (http://www.americannationalcorpus.org/)
using the SRILM toolkit.


---------------------------
Papers and website

The system is described in the following work:
-M. Heilman and N. A. Smith. 2010.  M. Heilman and N. A. Smith. 2010. Good Question! Statistical Ranking for Question Generation. In Proc. of NAACL/HLT.
-M. Heilman and N. A. Smith. 2009. Question Generation via Overgenerating Transformations and Ranking. Language Technologies Institute, Carnegie Mellon University Technical Report CMU-LTI-09-013.
-Michael Heilman's Ph.D. dissertation.

Website for the system:
http://www.ark.cs.cmu.edu/mheilman/questions/



