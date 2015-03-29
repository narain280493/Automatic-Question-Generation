# coding=UTF-8
import sys
from moviepy.editor import *
def cropVideo(start_time,end_time):
#start_time = sys.argv[1]
#end_time = sys.argv[2]
	#print start_time
	#print end_time
	sub = start_time.split(":")
	start_time_min = sub[0]
	start_time_sec = sub[1]
	sub = end_time.split(":")
	end_time_min = sub[0]
	end_time_sec = sub[1]
	result = VideoFileClip("/home/narain/fyp_resources/Video/ted_video.mp4").subclip(t_start =(int(start_time_min),int(start_time_sec)), t_end= (int(end_time_min),int(end_time_sec)))
	result.write_videofile("/home/narain/fyp_resources/Video/ted_cut.mp4",fps=25) # Many options...
	        


