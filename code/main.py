import sys

#handle command line arguments
if (len(sys.argv) != 3):
    print("Please execute the program with the following arguments: sampleNumber filePath")
    exit()

sampleNumber = sys.argv[1]
filePath = sys.argv[2]
try:
    inputFile = open(filePath, "r")
except FileNotFoundError:
    print("File not found. Please enter a valid file path.")
    exit()

#read data from file into structure
x = inputFile.read()
while x:
    #replace print with loading data into the structure
    print(x)

    x = inputFile.read()

inputFile.close()
