# This script can be used to post process a simulation file created with the parse.py script if the server can support less players. The first argument to this script should be the .sim file that should be processed. The second argument is the maximum number of players there should be supported. I.e.
# python postprocess.py 2007-07-14.sim 100
# This will create a file with a filtered event file, with less players connecting and disconnecting.

import os
import sys
import random

f = open(sys.argv[1], 'r')
newInitialPlayers = int(sys.argv[2])
avatarIds = []

# read the file and get a list of all avatar IDs
content = f.readlines()
initialPlayers = int(content[0])
content.pop(0)
initialPlayersData = content[0]
for playerId in initialPlayersData.split(" "):
    avatarIds.append(int(playerId))
content.pop(0)

for line in content:
    parts = line.split(" ")
    parts.pop(0)
    for event in parts:
        avatarId = int(event[1:])
        if avatarId not in avatarIds:
            avatarIds.append(avatarId)

# find out how many avatars we should remove from the simulation
avatarsToRemove = len(avatarIds) - (newInitialPlayers * len(avatarIds)) / initialPlayers
for x in range(0, avatarsToRemove):
    avatarIds.pop(random.randrange(len(avatarIds)))

# write out a new file
filename = sys.argv[1].split(".")[0]
outfile = open(filename + "-" + str(newInitialPlayers) + ".sim", 'w')

newInitialPlayersData = []
for playerId in initialPlayersData.split(" "):
    if int(playerId) in avatarIds:
        newInitialPlayersData.append(playerId)

outfile.write(str(len(newInitialPlayersData)) + "\n")

for playerId in newInitialPlayersData:
    outfile.write(playerId + " ")

outfile.write("\n")

# write out the new events
for line in content:
    parts = line.split(" ")
    l = parts[0]
    parts.pop(0)
    containsSomething = False
    for event in parts:
        cmd = event[0]
        avatarId = int(event[1:])
        if avatarId in avatarIds:
            containsSomething = True
            l = l + " " + cmd + str(avatarId)
    if containsSomething == True:
        outfile.write(l + "\n")
