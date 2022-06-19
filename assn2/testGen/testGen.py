import sys
import random as rand
import numpy as np
import math as m

def main(argv):

    if len(argv) == 0:
        level = "4"
        fileName = "input0.txt"
        print("No level input - Default parameter used")
        print("usage: testGen.py Level")
    elif len(argv) == 1:
        level = argv[0]
        fileName = "input0.txt"
    else:
        level = argv[0]
        fileName = argv[1]

    terrainNames = ['dirt-straight-hilly', 'dirt-straight-flat', 'dirt-slalom-hilly', 'dirt-slalom-flat', \
        'asphalt-straight-hilly', 'asphalt-straight-flat', 'asphalt-slalom-hilly', 'asphalt-slalom-flat']
    carNames = ['mazda', 'toyota', 'ferarri', 'humvee', 'go-kart']
    driverNames = ['stig', 'schumacher', 'anakin', 'mushroom', 'crash']
    tyreNames = ['all-terrain', 'mud', 'low-profile', 'performance']


    if level == "1":
        carTypes = 2
        driverTypes = 2
        terrainTypes = 2
        totalLength = 10
    elif level == "2":
        carTypes = 3
        driverTypes = 2
        terrainTypes = 4
        totalLength = 10
    elif (level == "3" or level == "4"):
        carTypes = 5
        driverTypes = 5
        terrainTypes = 8
        totalLength = 30
    else:
        print("error - level out of range")
        print(level)
        exit()

    maxTime = 3 * totalLength

    f = open(fileName, "w+")

    discountFactor = rand.randrange(50, 100, 1) / 100
    slipRecoverTime = rand.randrange(1, 3, 1)
    repairTime = rand.randrange(1, 3, 1)

    ## Writes header information
    f.write(str(level) + "\n")
    f.write(str(discountFactor) + " " + str(slipRecoverTime) + " " + str(repairTime) + "\n")
    f.write(str(totalLength) + " " + str(maxTime) + "\n")

    ## Writes tarrains
    tarrain = []
    for i in range(totalLength):
        tarrain.append(rand.randrange(0, terrainTypes, 1))

    for i in range(0, terrainTypes):
        first = True
        f.write(terrainNames[i] + ":")
        for j in range(0, totalLength):
            if tarrain[j] == i:
                if first:
                    f.write(str(j+1))
                    first = False
                else:
                    f.write(',' + str(j+1))
        f.write("\n")

    ## Writes cars
    f.write(str(carTypes) + "\n")

    for i in range(0, carTypes):
        f.write(carNames[i] + ":")
        d = np.random.dirichlet(np.ones(12), size=1)
        for j in range(0, 12):
            if j < 11:
                f.write(str(d[0][j]) + " ")
            else:
                f.write(str(d[0][j]) + "\n")
    
    ## Writes drivers
    f.write(str(driverTypes) + "\n")

    for i in range(0, driverTypes):
        f.write(driverNames[i] + ":")
        d = np.random.dirichlet(np.ones(12), size=1)
        for j in range(0, 12):
            if j < 11:
                f.write(str(d[0][j]) + " ")
            else:
                f.write(str(d[0][j]) + "\n")

    ## Writes tyres
    for i in range(0, 4):
        f.write(tyreNames[i] + ":")
        d = np.random.dirichlet(np.ones(12), size=1)
        for j in range(0, 12):
            if j < 11:
                f.write(str(d[0][j]) + " ")
            else:
                f.write(str(d[0][j]) + "\n")

    ## Writes fuels
    for i in range(carTypes * terrainTypes):
        f.write(str(rand.randrange(1, 6, 1)) + " ")
    f.write(str(rand.randrange(1, 6, 1)) + "\n")

    ## Writes slips
    d = np.random.dirichlet(np.ones(terrainTypes), size=1)
    for i in range(0, terrainTypes - 1):
        f.write(str(d[0][i]) + " ")
    f.write(str(d[0][terrainTypes - 1]) + "\n")

    print("Test input generated")
    f.close()

if __name__ == "__main__":
   main(sys.argv[1:])
