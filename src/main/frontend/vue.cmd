@echo off
setlocal
set PATH=%~dp0node/;%PATH%
node node/node_modules/@vue/cli/bin/vue.js %*
@echo on