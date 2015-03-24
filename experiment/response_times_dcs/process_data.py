import glob
import matplotlib.pyplot as plt
import numpy as np

path = "C:/Users/Laurens/Desktop/response_times_dcs/*.txt"

responses = []
for fname in glob.glob(path):
    with open(fname) as f:
		for line in f:
			if line[2] == "response":
				response.append(line[3])

plt.boxplot(response)

plt.show()