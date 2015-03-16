import sys
from nltk.corpus import wordnet

synsets = []
choice = sys.argv[1]
if choice == "1":
	synsets = wordnet.synsets( str(sys.argv[2]) )
	for synset in synsets:
		if( synset.lexname() == str(sys.argv[3])) :
			print synset.definition()

elif choice == "2":
	synsets = wordnet.synsets( str(sys.argv[2]) )
	for synset in synsets:
		if( synset.lexname() == str(sys.argv[3])) :
			print synset.name()

elif choice == "3":
	synsets= wordnet.synsets(str(sys.argv[2]))
	for synset in synsets:
		if( synset.lexname() == str(sys.argv[3])) :
			name = synset.name()
			good = wordnet.synset(name)
			if len(good.lemmas()[0].antonyms())  > 0:
				print good.lemmas()[0].antonyms()[0].name()



