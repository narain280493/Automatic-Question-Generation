# coding=UTF-8
from __future__ import division
import re
import math
from operator import itemgetter

def printHello():
	print "hello"

def sentenceCount(content):
  return content.split('.')

def idf(word, content):
	sentences = sentenceCount(content)
	number_of_sentences = len(sentences)
	count=0
	for sentence in sentences:
		if sentence.count(word) > 0:
		  count += 1
	
	return math.log(number_of_sentences/(1+count))

def wordCount(content):
  return len(content.split(None))

def freq(word, content):
  return content.count(word)

def tf(word, content):
  return (freq(word,content) / float(wordCount(content)))

def main():

	content = """  New Delhi : It seems the common house sparrow has disappeared from the city. But ornithologists maintain that while sparrow numbers are dwindling, the bird has not disappeared entirely from the city and only shifted to more inhabitable parts. Conservationists are now trying to understand why some areas have managed to hold back sparrows and what has driven them away from others.

A recent countrywide survey initiative called Citizen Sparrow is now roping in residents who want to report about their experience with sparrows. So far this unique sparrow survey organized by the Bombay National History Society (BNHS) and ministry of environment and forests (MOEF) has received close to 410 responses from Delhi. Of these, 86 have claimed they have not seen any sparrows at all. But the majority seems to have sighted the bird, which gives new hope to conservationists.

I have seen lots of sparrows in Ghaziabad, in Sheikh Sarai where I live but hardly any in say the Greater Kailash area. What is different in GK and other parts is still a mystery. But certain factors drawing the birds have become clearer, such as they nest more around old buildings, houses or may be in houses where there are old electricity meters, kitchen gardens, shrubs, says co-in-vestigator, BNHS Citizen Sparrow Project, Koustubh Sharma. Another conservationist and birder, Ananda Banerjee says she has seen lots of sparrows in parts of Lutyens Delhi, Mayur Vihar, parts of old Delhi and parts of Noida. He cites urban landscape to be the reason behind the decline in the sparrow population.

"Urban architecture, tall glass buildings that lack nesting spaces for the sparrow, pesticides used in farming that kill the worms that sparrows feed  on are some of the reasons. Even our markets have changed. There arenot many open markets where they can get grains. But you can see lots of sparrows in Khari Baoli open grain market," he says. But there is no doubt among conservationists that sparrows are fast disappearing. Declining number of sparrows and their complete absence from some parts of the city isn't just about missing the tiny bird. It is an indicator of something much graver.

Ecologist and forestry expert, Neeraj Khera, who has been studying the sparrow population in Delhi, feels that sparrows are an important indicator species. "There is always a threshold level. Big changes like an epidemic outbreak for instance will not happen overnight, but when we cross the buffer line then changes take place in our ecosystem. Sparrows as an indicator species is very sensitive to change. So it's obvious that a lot must have changed in our ecology to have driven them away," says Khera.Some of the important factors responsible are air and water pollution, loss of native herbs and shrubs.

Another trend being noticed by experts is the increase of rock pigeons in most parts of Delhi. They seem to have almost replaced the sparrows that used to nest in the same places."Rock pigeons have almost grown out of proportion and taken up the space of sparrows. They can be seen nesting in houses and other buildings. Studies have shown that it is not a welcome change as the excreta of rock pigeon carries a lot bacterial pathogens," said Khera.

She says that sparrow population is moderate in places where there are old government buildings, water bodies or green spaces. Saving the sparrow, she says is not a lost cause yet. It is the right time to intervene and check further decline. It will take 'Citizen Sparrow' survey a couple of more months to come up with the preliminary analysis of the results from their - survey and may offer a stronger argument on why the sparrows are disappearing from the city."""

	word = """ Delhi """ 

	#print "hello"
	print tf(word,content)
	print idf(word,content)

if __name__ == '__main__':
    main()