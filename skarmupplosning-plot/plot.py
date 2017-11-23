### Generate plot of screen resolution of the users of Webcert. This code is tested with python 3 and matplotlib
### Example usage is:
### python plot.py <input-file>
### Input-file need to be formatted as following:
###  * First row is discarded as header row.
###  * The columns are expected to be: number of entries, origin of the logged in user, width, height

import matplotlib.pyplot as plt
import csv
import numpy
import sys
from operator import itemgetter

max_size = 10000 # Max size of the circles in the resulting plot
color = ['r', 'g', 'b'] # The colors of the cirles in the resulting plot. Ideally should be as large as there are alternatives.

if len(sys.argv) < 2:
    print("Example usage: python plot.py <input-file>")
    sys.exit(1)

input_file = sys.argv[1]

test = numpy.array(list(csv.reader(open(input_file), delimiter=',')))

keys = list(set(test[:,1])) # The unique origin values contained in the file

for i, val in enumerate(keys):
    tmp = test[test[:,1]==val,:]
    width = tmp[:,2].astype("int")
    height = tmp[:,3].astype("int")
    number = [min(x,max_size)/10 for x in tmp[:,0].astype("int")]
    plt.scatter(width, height, number, c=color[i], label=val, alpha=0.5)
# In the indices array we want to save the indices in the transposed csv-file grouped on the origin of the webcert user

plt.xlabel('width')
plt.ylabel('height')
lgnd = plt.legend()
for handle in lgnd.legendHandles:
    handle.set_sizes([10.0])
# lgnd.legendHandles[2]._legmarker.set_markersize(6)
plt.show()
