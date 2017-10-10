@echo off 
for /R . %%s in (.,*) do ( 
	svn add %%s 
) 
pause 