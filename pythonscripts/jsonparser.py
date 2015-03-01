import json

with open("/home/vishnu/workspace/QuestionGeneration/transcript.json") as json_file:
    json_data = json.load(json_file)
   # print(json_data)
for item in json_data:
    # now song is a dictionary
    for attribute, value in item.iteritems():
        print attribute, value 