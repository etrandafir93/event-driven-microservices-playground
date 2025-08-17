@echo off

REM Check if a parameter was passed; default to 30 if not
set count=%1
if "%count%"=="" set count=30

for /L %%i in (1,1,%count%) do (
    call .\post-order.cmd
    echo.
)
