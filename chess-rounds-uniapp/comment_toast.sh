#!/bin/bash

# 批量注释所有uni.showToast调用的脚本

echo "开始批量注释uni.showToast调用..."

# 查找所有包含uni.showToast的文件
find src -name "*.vue" -o -name "*.js" -o -name "*.ts" | while read file; do
    if grep -q "uni\.showToast" "$file"; then
        echo "处理文件: $file"
        # 使用sed替换uni.showToast为// uni.showToast
        sed -i '' 's/uni\.showToast(/\/\/ uni.showToast(/g' "$file"
    fi
done

echo "批量注释完成！"