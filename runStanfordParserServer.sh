#/usr/bin/env bash

#java -Xmx1000m -cp question-generation.jar edu.cmu.ark.StanfordParserServer --grammar config/englishPCFG.ser.gz --port 5556 --maxLength 60
java -Xmx1200m -cp question-generation.jar edu.cmu.ark.StanfordParserServer --grammar config/englishFactored.ser.gz --port 5556 --maxLength 40



