# coding=UTF-8
import sys
import re

def contraction():
	def expand_contractions(s, contractions_dict):
		def replace(match):
			return contractions_dict[match.group(0)]
		return contractions_re.sub(replace, s)
	
	contraction_list = []
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
	print contraction_dict["ain't"]
	print contraction_dict["don't"]
	inputtext = "I don't play. I ain't dhoni."
	contractions_re = re.compile('(%s)' % '|'.join(contraction_dict.keys()))
	inputtext =expand_contractions(inputtext,contraction_dict)
	print inputtext
	

contraction()