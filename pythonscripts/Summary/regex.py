# coding=UTF-8
import sys
import re
def regex(text):
	#text = " (Applause)My name is Narain -- a big surprise -- and I am from CEG. (Laughter)"
	
	#print text
	question_removal_filter1 = re.sub('(\?)?(\s)?[a-zA-Z0-9\s]*\?',"",text)
	#print "AFter q1:\n",question_removal_filter1
	question_removal_filter2 = re.sub('(\.)?(\s)?[a-zA-Z0-9\s]*\?',".",question_removal_filter1)
	#print "After q2:\n",question_removal_filter2
	dash_removal_filter = re.sub('(\s)?--(\s)?[\(\)a-zA-Z0-9\s]*(\s)?--(\s)?',",",question_removal_filter2)
	#print "After dash:\n",dash_removal_filter
	bracket_removal_filter = re.sub('\([a-zA-Z0-9]*\)',"",dash_removal_filter)
	output = bracket_removal_filter
	#print "AFter bracket:\n",output
	return output

def contractions(text):
	def expand_contractions(s, contractions_dict):
		def replace(match):
			return contractions_dict[match.group(0)]
		return contractions_re.sub(replace, s)
	
	
	splitter = []
	remove_linespace = []
	contraction_dict = {}
	with open('contractions.txt') as f:
		sentences = f.readlines()
	#print sentences
	for sentence in sentences:
		splitter = sentence.split("-")
		remove_linespace = splitter[1].split("\n")
		contraction_dict[splitter[0]]=remove_linespace[0]
	#print contraction_dict["ain't"]
	#print contraction_dict["don't"]
	#inputtext = "I don't play. I ain't dhoni."
	contractions_re = re.compile('(%s)' % '|'.join(contraction_dict.keys()))
	output =expand_contractions(text,contraction_dict)
	#print "Output:",output
	return output

def firstToThirdPerson(text):
	def change_persons(s, rules_dict):
		def replace(match):
			return rules_dict[match.group(0)]
		return rules_re.sub(replace, s)
	
	
	splitter = []
	remove_linespace = []
	rules_dict = {}
	with open('personchange.txt') as f:
		sentences = f.readlines()
	#print sentences
	for sentence in sentences:
		splitter = sentence.split(":")
		remove_linespace = splitter[1].split("\n")
		rules_dict[splitter[0]]=remove_linespace[0]

	rules_re = re.compile(r'\b(%s)\b' % '|'.join(rules_dict.keys()),re.U)
	output =change_persons(text,rules_dict)
	#print "Output:",output
	return output

text = " So what I did during my job is grow plants in the greenhouse, different ones, different milkweeds. Some were toxic, including the tropical milkweed, with very high concentrations of these cardenolides. And some were not toxic. And then I fed them to monarchs. Some of the monarchs were healthy. They had no disease. But some of the monarchs were sick, and what I found is that some of these milkweeds are medicinal, meaning they reduce the disease symptoms in the monarch butterflies, meaning these monarchs can live longer when they are infected when feeding on these medicinal plants."

#print firstToThirdPerson(text)