import sys
from nltk.corpus import wordnet
from bottle import route, run, template, error, abort, response

@error(404)
def custom500(error):
    response.status=200
    return "NO_RESPONSE"

@error(500)
def custom500(error):
    response.status=200
    return "NO_RESPONSE"

@route('/<choice>/<word>/<sst_tag>')
def index(choice,word,sst_tag):
	synsets = []
	response = ""
	if choice == "definition":
		synsets = wordnet.synsets( str(word) )
		for synset in synsets:
			if( synset.lexname() == str(sst_tag)) :
				response += synset.definition()
				response += ";"

	elif choice == "synonym":
		synsets = wordnet.synsets( str(word) )
		for synset in synsets:
			if( synset.lexname() == str(sst_tag)) :
				response += synset.name().split('.')[0]
				response += ";"

	elif choice == "antonym":
		synsets= wordnet.synsets(str(word))
		for synset in synsets:
			if( synset.lexname() == str(sst_tag)) :
				name = synset.name()
				good = wordnet.synset(name)
				if len(good.lemmas()[0].antonyms())  > 0:
					response += good.lemmas()[0].antonyms()[0].name()
					response += ";"
	if response == "":
		response = "NO_RESPONSE"
	return template('{{response}}', response=response)


run(host='localhost', port=8030)
