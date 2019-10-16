import numpy as np
import matplotlib.pyplot as plt
import csv

time = []
latency = []
with open('/Users/Chenxi/Documents/client1/output/output-1000thread.csv') as csvfile:
    readCSV = csv.reader(csvfile, delimiter=',')
    print("in")
    read = False
    for row in readCSV:
    	if (read):
	    	print(row)
	    	time.append(int(row[0]))
	    	latency.append(int(row[2]))
    	else:
    		read = True
    
fig = plt.figure()    
plt.plot(time, latency, 'ko', markersize=1)
fig.suptitle('1000-thread Plot')
plt.xlabel('Start time (Epoch millisecs)')
plt.ylabel('Latency (millisecs)')
plt.show()