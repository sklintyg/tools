### Generate plot of screen resolution of the users of Webcert. This code is tested with python 3 and matplotlib
### Example usage is:
### python plot.py <input-file>
### Input-file need to be formatted as following:
###  * First row is discarded as header row.
###  * The columns are expected to be: number of entries, origin of the logged in user, width, height

import matplotlib.pyplot as plt
import csv
import sys
from operator import itemgetter

max_size = 500 # Max size of the circles in the resulting plot
color = ['r', 'g', 'b'] # The colors of the cirles in the resulting plot. Ideally should be as large as there are alternatives.

if len(sys.argv) < 2:
    print("Example usage: python plot.py <input-file>")
    sys.exit(1)

input_file = sys.argv[1]

# To show the information in a useful way we want to transpose the csv-file. This is a quick and dirty way of doing so.
c0 = []
c1 = []
c2 = []
c3 = []
with open(input_file) as csvfile:
     rows = csv.reader(csvfile, delimiter=',')
     next(rows) # Skip header row
     for row in rows:
         c0.append(row[0])
         c1.append(row[1])
         c2.append(row[2])
         c3.append(row[3])

keys = list(set(c1)) # The unique origin values contained in the file

# In the indices array we want to save the indices in the transposed csv-file grouped on the origin of the webcert user
indices = [] 
for key in keys:
    indices.append([i for i, x in enumerate(c1) if x == key])

# Do the actual plotting. Since some resolutions are far and away more common than others we need to set a ceiling on the size
# of the circle in the resulting scatter plot.
for i, val in enumerate(indices):
    plt.scatter((itemgetter(*val)(c2)),(itemgetter(*val)(c3)), s=[min(int(x),500) for x in itemgetter(*val)(c0)], c=color[i % len(color)], label=keys[i])

plt.xlabel('width')
plt.ylabel('height')
plt.legend()
plt.show()
