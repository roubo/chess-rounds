#!/bin/bash
# 切换到Java 17环境的脚本
# 使用方法: source switch-to-java17.sh

echo "正在切换到Java 17环境..."

# 设置Java 17的环境变量
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"

echo "Java 17环境已设置完成！"
echo "当前Java版本:"
java -version
echo ""
echo "环境变量:"
echo "JAVA_HOME: $JAVA_HOME"
echo "Java路径: $(which java)"
echo ""
echo "注意: 这些设置只在当前终端会话中有效"
echo "要在新终端中使用Java 17，请重新运行: source switch-to-java17.sh"