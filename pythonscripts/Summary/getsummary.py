import sys
import json
import nltk.data
from summary import *
from regex import *

timestamp = []
paragraph = []
videoQuestionInput = []
content = " "

count =0
inputText =" "

with open("/home/narain/workspace/questiongeneration/transcript.json") as json_file:
    json_data = json.load(json_file)
   
for item in json_data:
	for attribute, value in item.iteritems():
		paragraph.append(value)
		timestamp.append(attribute)
		content = content + " "+ value


#print "Before processing: \n",content
content = contractions(content)
#print "After expanding contractions:\n",content
content = regex(content)
#print "After processing: \n",content
st = SummaryTool()
sentences_dic = st.get_senteces_ranks(content)
summary = st.get_distractors(content,sentences_dic,1)
#print "Summary:\n",summary
for p in paragraph:
    temp = p
    count = count + 1
    if temp.find(summary) != -1:
 #       print "Found the sentence in para -",count
  #      print "\n"
        start_time = timestamp[count-1]
        end_time = timestamp[count]
        print start_time
        print end_time
    	inputText = paragraph[count-1]
        break
#print "paragraph:",inputText
outputList = []
tokenizer = nltk.data.load('tokenizers/punkt/english.pickle')
sentences = tokenizer.tokenize(inputText)
#print sentences
for sentence in sentences:
    tokens = nltk.word_tokenize(sentence)
    #print len(tokens)
    if len(tokens) < 55:
   #     print "Less than 55"
        outputList.append(sentence)
for sentence in outputList:
    print sentence



