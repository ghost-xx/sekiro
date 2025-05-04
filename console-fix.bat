@echo off
setlocal enabledelayedexpansion

REM 设置控制台编码为UTF-8
chcp 65001 > nul

echo 准备启动Sekiro服务...

REM 获取命令行参数
set PORT=23000
set STRICT_CHECK=true

if not "%~1" == "" (
  set PORT=%~1
)

if not "%~2" == "" (
  set STRICT_CHECK=%~2
)

REM 创建临时JVM选项文件
echo -Dfile.encoding=UTF-8 > jvm_opts.txt
echo -Dsun.jnu.encoding=UTF-8 >> jvm_opts.txt
echo -Duser.language=zh >> jvm_opts.txt
echo -Duser.region=CN >> jvm_opts.txt

echo 启动参数:
echo   端口: %PORT%
echo   严格绑定检查: %STRICT_CHECK%
echo   编码: UTF-8

REM 使用选项文件启动Java应用
java @jvm_opts.txt -jar target/sekiro-open-0.0.1.jar --sekiro.port=%PORT% --sekiro.strict.bindClientCheck=%STRICT_CHECK%

REM 清理临时文件
del /q jvm_opts.txt

pause 