:: Simple Cytoscape batch script for windows/dos
:: (c) Trey Ideker June 21, 2002; Owen Ozier March 06, 2003
::
:: Runs Cytoscape from its jar file with GO data loaded

@echo off

:: Create the Cytoscape.vmoptions file, if it doesn't exist.
IF EXIST "%HOMEPATH%\.cytoscape\Cytoscape.vmoptions" GOTO vmoptionsFileExists
CALL gen_vmoptions.bat
:vmoptionsFileExists

:: Read vmoptions, one per line.
setLocal EnableDelayedExpansion
for /f "tokens=* delims= " %%a in (%HOMEPATH%\.cytoscape\Cytoscape.vmoptions) do (
set /a N+=1
set opt!N!=%%a
)

java !opt1! !opt2! !opt3! !opt4! !opt5! !opt6! !opt7! !opt8! !opt9! -jar cytoscape.jar -p plugins %*


