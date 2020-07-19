@echo off
setlocal
set PATH=%~dp0node/;%PATH%
node node/node_modules/npm/bin/npx-cli.js %*
@echo on