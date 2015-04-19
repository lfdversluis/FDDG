# This script can be used to parse a WowAH directory with .txt files. The output will be a file with players connecting/disconnecting which can be simulated.
# The first argument of the script should be the name of the directory where the .txt files are located, i.e.
# python parse.py 2007-07-14
# Note: the directory with .txt files should live next to the parse.py script.

# Each .txt file contains information about the sample at a particular time (the time is indecated in the name of the file). The samples are taken every 10 minutes. This scripts finds out how many players there are online at a particular sample and how many connected/disconnected during the time between this sample and the last sample. Then, a connecting/disconnecting event is created at a random timestamp between these two samples.

import glob
import os
import sys
from random import randint

connectedPlayers = []
disconnectedPlayers = []
firstParsed = False
onlinePlayerIds = []
events = {}
lastFile = ""
directoryName = sys.argv[1]
initialOnline = []

def getTimestamp(file):
    parts = file.split(".")
    parts = parts[0].split("-")
    return int(parts[0]) * 3600 + int(parts[1]) * 60 + int(parts[2])

def getAvatarId(line):
    parts = line.split(",")
    return parts[3]

def addEvent(connecting, avatarId, timestamp):
    if timestamp not in events:
        events[timestamp] = []
    if connecting == True:
        events[timestamp].append('C' + str(avatarId))
    else:
        events[timestamp].append('D' + str(avatarId))

def parseFile(file):
    global firstParsed, onlinePlayerIds, lastFile
    f = open(file, 'r')
    content = f.readlines()
    
    # keep track of the newly connected/disconnected players this round
    connected = 0
    disconnected = 0
    
    # remove first two lines
    content.pop(0)
    content.pop(0)
    
    dataLines = []

    for line in content:
        if line[0] == '}':
            break
        dataLines.append(line)

    if firstParsed == False:
        
        # put the online players in a set
        for line in dataLines:
            onlinePlayerIds.append(getAvatarId(line))
            initialOnline.append(getAvatarId(line))
        
        firstParsed = True
    else:
        
        newOnlinePlayerIds = []
        for line in dataLines:
            newOnlinePlayerIds.append(getAvatarId(line))
        
        # get difference between the two measures
        lastTimestamp = getTimestamp(lastFile)
        curTimestamp = getTimestamp(file)
    
        # find out which players have disconnected
        for player in onlinePlayerIds:
            if player not in newOnlinePlayerIds:
                disconnectingTime = randint(lastTimestamp, curTimestamp)
                addEvent(False, player, disconnectingTime)
                disconnected = disconnected + 1
        
        # find out which players have connected
        for player in newOnlinePlayerIds:
            if player not in onlinePlayerIds:
                connectingTime = randint(lastTimestamp, curTimestamp)
                addEvent(True, player, connectingTime)
                connected = connected + 1
    
        # create a new online player set
        onlinePlayerIds = newOnlinePlayerIds

    connectedPlayers.append(connected)
    disconnectedPlayers.append(disconnected)
    lastFile = file

os.chdir(directoryName)
for file in glob.glob("*.txt"):
    parseFile(file)

# write the file out
outfile = open('../' + directoryName + '.sim', 'w')

outfile.write(str(len(initialOnline)) + "\n")

for playerId in initialOnline:
    outfile.write(playerId + " ")

outfile.write("\n")

#print connectedPlayers
#print disconnectedPlayers
for key in sorted(events):
    l = key
    arr = events[key]
    for event in arr:
        l = str(l) + " " + str(event)
    outfile.write(l + "\n")
