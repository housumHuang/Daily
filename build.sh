#!/bin/bash
# 编译脚本 - 农历钟面项目
# 用法:
#   ./build.sh        仅编译
#   ./build.sh run    编译并运行钟面
set -e
cd "$(dirname "$0")" || exit 1

echo "==> 编译 LunarCalendar.java ClockFace.java MainFrame.java"
javac LunarCalendar.java ClockFace.java MainFrame.java
echo "==> 编译成功"

if [ "$1" = "run" ]; then
    echo "==> 启动钟面"
    java ClockFace
fi
