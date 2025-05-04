@echo off
setlocal enabledelayedexpansion

REM 设置控制台编码为UTF-8
chcp 65001 > nul

echo [信息] 准备启动Sekiro服务...

REM 获取命令行参数
set PORT=23000
set STRICT_CHECK=true

if not "%~1" == "" (
  set PORT=%~1
)

if not "%~2" == "" (
  set STRICT_CHECK=%~2
)

echo [信息] 启动参数:
echo   端口: %PORT%
echo   严格绑定客户端检查: %STRICT_CHECK%

REM 设置JAVA_OPTS环境变量
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Duser.language=zh -Duser.country=CN

echo [信息] 正在启动Sekiro服务...
java %JAVA_OPTS% -jar target\sekiro-open-0.0.1.jar --sekiro.port=%PORT% --sekiro.strict.bindClientCheck=%STRICT_CHECK%

if %ERRORLEVEL% NEQ 0 (
  echo [错误] 启动失败，请检查日志
) else (
  echo [信息] 服务已停止
)

pause 