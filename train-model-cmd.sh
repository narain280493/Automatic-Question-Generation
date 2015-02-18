java -server -Xmx800m -cp question-generation.jar \
 edu/cmu/ark/QuestionRanker \
 --type linear-regression --regularizer 500 \
 --trainfile models/qg-training-31-aug-2010.dat \
 --save-model models/my-model.gz
