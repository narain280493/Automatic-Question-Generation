import sys
import json
from summary import *
from VideoCropper import *

timestamp = []
paragraph = []
content = " "
count =0

with open("/home/narain/workspace/questiongeneration/transcript.json") as json_file:
    json_data = json.load(json_file)
   
for item in json_data:
	for attribute, value in item.iteritems():
		paragraph.append(value)
		timestamp.append(attribute)
		content = content + " "+ value

#print content
st = SummaryTool()
sentences_dic = st.get_senteces_ranks(content)
summary = st.get_distractors(content,sentences_dic,1)
print summary
for p in paragraph:
    temp = p
    count = count + 1
    if temp.find(summary) != -1:
        print "Found the sentence in para -",count
    	start_time = timestamp[count-1]
    	end_time = timestamp[count]
    	clip(start_time,end_time)
        #print "Clipped."
    	break


