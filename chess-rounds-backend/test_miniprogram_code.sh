#!/bin/bash

# 测试小程序码生成API
echo "Testing mini program code generation..."

# 设置API端点
API_URL="http://localhost:8080/api/rounds/23/miniprogram-code"

# 发送GET请求
echo "Sending request to: $API_URL"
curl -X GET "$API_URL" \
  -H "Content-Type: application/json" \
  -v \
  --output miniprogram_code_response.png

echo "\nResponse saved to miniprogram_code_response.png"
echo "Check the application logs for detailed error information."