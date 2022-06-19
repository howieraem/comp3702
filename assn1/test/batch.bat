@echo off

:create_map
	python testGen.py 10 10 8
	for /F "tokens=*" %%o in ('java test.CollisionTester input0.txt') do set res=%%o
	if "%res%"=="Collision" (goto create_map)

copy input0.txt .\res\input0.txt
pause

@echo finished
