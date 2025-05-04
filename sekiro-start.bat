@echo off
setlocal enabledelayedexpansion

REM 设置控制台为UTF-8编码
chcp 65001 > nul

REM 默认端口
set PORT=5612
set STRICT_CHECK=true

REM 解析命令行参数
if not "%~1" == "" (
  set PORT=%~1
)

if not "%~2" == "" (
  set STRICT_CHECK=%~2
)

echo 正在启动Sekiro服务，端口=%PORT% 严格绑定客户端检查=%STRICT_CHECK%

REM 设置Java参数
set JAVA_OPTS=-Dfile.encoding=UTF-8 -Duser.language=zh -Duser.country=CN -Dlog4j.skipJansiCheck=false

REM 尝试启动Sekiro
java %JAVA_OPTS% -jar target/sekiro-open-0.0.1.jar --sekiro.port=%PORT% --sekiro.strict.bindClientCheck=%STRICT_CHECK%

if %ERRORLEVEL% NEQ 0 (
  echo 启动失败，尝试其他方式启动...
  java -jar target/sekiro-open-0.0.1.jar --sekiro.port=%PORT% --sekiro.strict.bindClientCheck=%STRICT_CHECK%
)

pause 