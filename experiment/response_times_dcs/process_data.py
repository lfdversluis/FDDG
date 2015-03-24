import glob
import matplotlib.pyplot as plt
import numpy as np
import pylab

path = "/home/laurens/Documents/FDDG/experiment/response_times_dcs/*.txt"

responses = []
for fname in glob.glob(path):
    print fname
    with open(fname) as f:
        for line in f:
            if line.split()[1] == "response":
                responses.append(int(line.split()[2]))
                
fig = plt.figure()
fig.suptitle('Server-client response time', fontsize=14, fontweight='bold')

ax1 = fig.add_subplot(111)

ax1.set_xlabel('')
ax1.set_ylabel('Resonse time (ms)')
pylab.xticks([1], [''])

plt.boxplot(responses)

plt.show()