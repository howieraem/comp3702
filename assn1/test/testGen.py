import sys
import random as rand
import math as m



def main(argv):

    if len(argv) != 3:
        movingBoxNum = 10
        movingObstacleNum = 7
        staticObstacleNum = 12
        print("Default parameter used")
        print("usage: testGen.py movingBoxNum movingObstacleNum staticObstacleNum")
    else:
        movingBoxNum = argv[0]
        movingObstacleNum = argv[1]
        staticObstacleNum = argv[2]
    
    f = open("input0.txt", "w+")



    #robotLength = rand.randrange(5, 15, 1)
    robotLength = 5
    #moWidth = (rand.randrange(robotLength * 10, robotLength * 15, 10) / 10)
    moWidth = rand.randrange(5, 7, 1)

    initialRX = rand.randrange(0, 100, 1)
    initialRY = rand.randrange(0, 100, 1)
    initialRA = rand.randrange(0, int(200 * m.pi), 1)

    f.write(str(robotLength / 100) + " " + str(initialRX / 100) + " " + str(initialRY / 100) + " " + str(initialRA / 100) + "\n")
    f.write(str(movingBoxNum) + " " + str(movingObstacleNum) + " " + str(staticObstacleNum) + "\n")

    initialBoxPositions = []
    goalBoxPositions = []
    movingObstacles = []
    staticObstacles = []

    for i in range(0, int(movingBoxNum)):
        
        validIB = 1
        validGB = 1
        
        initialBX = rand.randrange(robotLength, 100 - robotLength, 1)
        initialBY = rand.randrange(robotLength, 100 - robotLength, 1)
        goalBX = rand.randrange(robotLength, 100 - robotLength, 1)
        goalBY = rand.randrange(robotLength, 100 - robotLength, 1)
        
        while (validIB < len(initialBoxPositions) or validGB < len(goalBoxPositions)):
            initialBX = rand.randrange(robotLength, 100 - robotLength, 1)
            initialBY = rand.randrange(robotLength, 100 - robotLength, 1)
            goalBX = rand.randrange(robotLength, 100 - robotLength, 1)
            goalBY = rand.randrange(robotLength, 100 - robotLength, 1)

            validIB = 1
            validGB = 1
            
            for item in initialBoxPositions:
                if ((initialBX + robotLength) < item[0]) or ((initialBX - robotLength) > item[0]) \
                   or ((initialBY + robotLength) < item[1]) or ((initialBY - robotLength) > item[1]):
                    validIB += 1
                    
            for item in goalBoxPositions:
                if ((goalBX + robotLength) < item[0]) or ((goalBX - robotLength) > item[0]) \
                   or ((goalBY + robotLength) < item[1]) or ((goalBY - robotLength) > item[1]):
                    validGB += 1
        
        initialBoxPositions.append([initialBX, initialBY])
        goalBoxPositions.append([goalBX, goalBY])
        f.write(str(initialBX / 100) + " " + str(initialBY / 100) + " " + str(goalBX / 100) + " " + str(goalBY / 100) + "\n")

    for i in range(0, int(movingObstacleNum)):
        
        validIB = 1
        validMO = 1
        
        while (validIB < len(initialBoxPositions) or validMO < len(movingObstacles)):
            validIB = 1
            validMO = 1
            
            initialOX = rand.randrange(robotLength, 100 - robotLength, 1)
            initialOY = rand.randrange(robotLength, 100 - robotLength, 1)
            for item in initialBoxPositions:
                if ((initialOX + moWidth) < (item[0] - robotLength)) or ((initialOX - moWidth) > (item[0] + robotLength)) \
                   or ((initialOY + moWidth) < (item[1] - robotLength)) or ((initialOY - moWidth) > (item[1] + robotLength)):
                    validIB += 1

            for item in movingObstacles:
                if ((initialOX + moWidth) < (item[0] - moWidth)) or ((initialOX - moWidth) > (item[0] + moWidth)) \
                   or ((initialOY + moWidth) < (item[1] - moWidth)) or ((initialOY - moWidth) > (item[1] + moWidth)):
                    validMO += 1
        movingObstacles.append([initialOX, initialOY])
        f.write(str(initialOX / 100) + " " + str(initialOY / 100) + " " + str(moWidth / 100) + "\n")

    for i in range(0, int(staticObstacleNum)):

        validIB = 1
        validGB = 1
        validMO = 1
        validSA = 1
        while (validIB < len(initialBoxPositions) or validGB < len(goalBoxPositions) or validMO < len(movingObstacles) or validSA < len(staticObstacles)):
            initialOX1 = rand.randrange(0, 100, 1)
            initialOY1 = rand.randrange(0, 100, 1)
            initialOX2 = rand.randrange(initialOX1, 100, 1)
            initialOY2 = rand.randrange(initialOY1, 100, 1)

            validIB = 1
            validGB = 1
            validMO = 1
            validSA = 1
            for item in initialBoxPositions:
                if (initialOX2 < (item[0] - robotLength)) or (initialOX1 > (item[0] + robotLength)) \
                   or (initialOY2 < (item[1] - robotLength)) or (initialOY1 > (item[1] + robotLength)):
                    validIB += 1

            for item in goalBoxPositions:
                if (initialOX2 < (item[0] - robotLength)) or (initialOX1 > (item[0] + robotLength)) \
                   or (initialOY2 < (item[1] - robotLength)) or (initialOY1 > (item[1] + robotLength)):
                    validGB += 1

            for item in movingObstacles:
                if (initialOX2 < (item[0] - moWidth)) or (initialOX1 > (item[0] + moWidth)) \
                   or (initialOY2 < (item[1] - moWidth)) or (initialOY1 > (item[1] + moWidth)):
                    validMO += 1

            if (len(staticObstacles) < 1):
                validSA += 1
            else:
                for item in staticObstacles:
                    if (initialOX2 < item[0]) or (initialOX1 > item[2]) or (initialOY2 < item[1]) or (initialOY1 > item[3]):
                        validSA += 1

        staticObstacles.append([initialOX1, initialOY1, initialOX2, initialOY2])
        f.write(str(initialOX1 / 100) + " " + str(initialOY1 / 100) + " " + str(initialOX2 / 100) + " " + str(initialOY2 / 100) + "\n")

    f.write("\n")
    print("Test input generated")
    f.close()

if __name__ == "__main__":
   main(sys.argv[1:])
