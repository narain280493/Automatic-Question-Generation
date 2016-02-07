# coding=UTF-8
from __future__ import division
import re
import socket  
import sys
import random
from tf import *


class SummaryTool(object):

    # Naive method for splitting a text into sentences
    def split_content_to_sentences(self, content):
        content = content.replace("\n", ". ")
        return content.split(". ")

    # Naive method for splitting a text into paragraphs
    def split_content_to_paragraphs(self, content):
        return content.split("\n\n")

    # Caculate the intersection between 2 sentences
    def sentences_intersection(self, sent1, sent2, content):

        # split the sentence into words/tokens
        total_score= 0
        s1 = set(sent1.split(" "))
        s2 = set(sent2.split(" "))
        score = 0 
        # If there is not intersection, just return 0
        if (len(s1) + len(s2)) == 0:
            return 0

        common_words = s1.intersection(s2) 

        list_words= list(common_words)
        for words in list_words:
            score = idf(words,content) * tf(words,content)
            total_score += score
            #print score

        
        return total_score

    # Format a sentence - remove all non-alphbetic chars from the sentence
    # We'll use the formatted sentence as a key in our sentences dictionary
    def format_sentence(self, sentence):
        sentence = re.sub(r'\W+', '', sentence)
        return sentence

    # Convert the content into a dictionary <K, V>
    # k = The formatted sentence
    # V = The rank of the sentence


    def get_senteces_ranks(self, content):

        # Split the content into sentences
        sentences = self.split_content_to_sentences(content)

        # Calculate the intersection of every two sentences
        n = len(sentences)
        values = [[0 for x in xrange(n)] for x in xrange(n)]
        for i in range(0, n):
            for j in range(0, n):
                values[i][j] = self.sentences_intersection(sentences[i], sentences[j],content)
        # Build the sentences dictionary
        # The score of a sentences is the sum of all its intersection
        sentences_dic = {}
        for i in range(0, n):
            score = 0
            for j in range(0, n):
                if i == j:
                    continue
                score += values[i][j]
            sentences_dic[sentences[i]] = score
            #sentences_dic[self.format_sentence(sentences[i])] = score
        return sentences_dic

    # Return the best sentence in a paragraph
    def get_distractors(self, paragraph, sentences_dic,flag):

        # Split the paragraph into sentences
        sentences = self.split_content_to_sentences(paragraph)

        paragraph_dic = {}
        # Ignore short paragraphs
        if len(sentences) < 3:
            return " Short paragraph  < 3 sentences"

        for s in sentences:
            paragraph_dic[sentences_dic[s]] = s 

       # print paragraph_dic
        scorelist = paragraph_dic.keys()
        scorelist.sort(reverse=True)

        answers = []
        
       
        answers.append(paragraph_dic[scorelist[0]])
        
        answers.append(paragraph_dic[scorelist[1]])

        answers.append(paragraph_dic[scorelist[2]])


        if flag == 2:
            best_sentence = paragraph_dic[scorelist[0]]
            return best_sentence
        elif flag == 1:
            return answers

   



    def distractors_for_entire_paragraph(self, content, sentences_dic):
        
        distractors = [] 
        para_distractors = []
        paragraphs = self.split_content_to_paragraphs(content)
        for p in paragraphs:
            #sentence = self.get_distractors(p, sentences_dic,2).strip()
            para_distractors = self.get_distractors(p,sentences_dic,1)
            #call para specific question and print sentence as answer for that
            question = self.generate_questions(1)
            distractors.append(para_distractors)
        return distractors


    def generate_questions(self,flag):

        # TO-DO: Maybe read the question templates from a file (If there are a lot of templates)
        question_list_specific_para = ['The paragraph primarily talks about:', 'The main purpose of this passage is:','Select the sentence which highlights the message conveyed in this paragraphs']
        question_list_passage = ['The author’s primary purpose is to talk about:', 'Select the sentence which specifically illustrates what the author is trying to convey:','What can best be inferred from the passage?' ]
        if flag == 1:
            return random.choice(question_list_passage)
        else:
            return random.choice(question_list_specific_para)

        
    # Not used.
    def get_summary(self, title, content, sentences_dic):

        # Split the content into paragraphs
        paragraphs = self.split_content_to_paragraphs(content)
        distractors = []
        distractors.append("")
        print "Number of paragraphs:"
        number_of_para = len(paragraphs)
        print number_of_para

        print "\n"

        if number_of_para == 1:
            for p in paragraphs:
                question = self.generate_questions(1)
                print('Q: %s ' %question )
                print "\n"
                #distractors.append(question)
                distractors = self.get_distractors(p, sentences_dic,1)
               

        elif number_of_para > 1:
            
            distractors = self.distractors_for_entire_paragraph(content, sentences_dic)
            print distractors
            question = self.generate_questions(2)
            print('Q: %s ' %question )
            print "\n"
            
            

        else:
            print "Invalid Input"

        return distractors

    def readFile(self,filepath):
        f= open(filepath,"r")
        return f.read()

    def question_printer(self,question):
         print('Q: %s \n' %question )
       
         

def main():


    st = SummaryTool()

    filepath = sys.argv[1]
    content = st.readFile(filepath)
    fo = open("summarizer_output.txt","wb")
   # print content
    complete_summary = [] 

    para_summary = [] 

     #Building the sentence dictionary
    sentences_dic = st.get_senteces_ranks(content)
    
   

  #testing purposes
    paragraphs = st.split_content_to_paragraphs(content)

    #for p in paragraphs:
     #   print p
      #  print "------------"

    
   

    if len(paragraphs) > 1:
        print "More than One Para -\n"
        print len(paragraphs)
        complete_summary = st.get_distractors(content,sentences_dic,1)
        question = st.generate_questions(1)
        #st.question_printer(question)
        #print('Q: %s \n' %question )
        fo.write("Passage Based Questions: \n")
      #  print "Passage Based Questions:\n"
        fo.write("Q:")
      #  print "Q:"
        fo.write(question)
       # print question
        fo.write("\n")
        #print "\n"
        for sentence in complete_summary:
            fo.write(sentence)
        #    print sentence
            fo.write("\n ---------- \n")
         #   print "---------- \n"
        
            #print sentence
            
        para_summary = st.distractors_for_entire_paragraph(content, sentences_dic) #List of List
        fo.write("Paragraph Based Q's: \n")
      #  print "Paragraph Based Q's: \n"
        for p in para_summary:
            question = st.generate_questions(2)
            #st.question_printer(question)
            fo.write("Q:")
           # print "Q:"
            fo.write(question)
           # print question
            fo.write("\n")
            #print "\n"
            for sentence in p:
                fo.write(sentence)
            #    print sentence
                fo.write("\n ---------- \n")
             #   print "----------\n"
        fo.close()
                



    elif len(paragraphs) == 1:
        print "One paragraph \n"
        complete_summary = st.get_distractors(content,sentences_dic,1)
        question = st.generate_questions(1)
        fo.write("Q:")
        fo.write(question)
        fo.write("\n")
        for sentence in complete_summary:
            fo.write("Q:")
            fo.write(question)
            fo.write(sentence)
            fo.write("\n ---------- \n")
        fo.close()
            #print sentence
            #print "\n"

            

            



   # s = socket.socket()         # Create a socket object
    #host = socket.gethostname() # Get local machine name
    #port = 12345                # Reserve a port for your service.
    #s.bind((host, port))        # Bind to the port
   

   # s.listen(5)                 # Now wait for client connection.
    #while True:
     #   c, addr = s.accept()     # Establish connection with client.
      #  print 'Got connection from', addr
       # c.send(summary)
        #c.send("Message over")
        #c.close() 

    
   
if __name__ == '__main__':
    main()








