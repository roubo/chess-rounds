#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re

def fix_toast_comments(file_path):
    """修复文件中不完整的uni.showToast注释"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        # 查找所有 // uni.showToast( 开始的不完整注释块
        pattern = r'(\s*)// uni\.showToast\(\{([^}]*?)\}\)'
        
        def replace_match(match):
            indent = match.group(1)
            params = match.group(2)
            
            # 分割参数行
            param_lines = [line.strip() for line in params.split('\n') if line.strip()]
            
            # 重新构建完整的注释块
            result = f"{indent}// uni.showToast({{\n"
            for param_line in param_lines:
                result += f"{indent}//\t{param_line}\n"
            result += f"{indent}// }})"
            
            return result
        
        # 应用替换
        new_content = re.sub(pattern, replace_match, content, flags=re.DOTALL)
        
        # 如果内容有变化，写回文件
        if new_content != content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"修复文件: {file_path}")
            return True
        
    except Exception as e:
        print(f"处理文件 {file_path} 时出错: {e}")
    
    return False

def main():
    """主函数"""
    print("开始修复uni.showToast注释...")
    
    fixed_count = 0
    
    # 遍历src目录下的所有文件
    for root, dirs, files in os.walk('src'):
        for file in files:
            if file.endswith(('.vue', '.js', '.ts')):
                file_path = os.path.join(root, file)
                if fix_toast_comments(file_path):
                    fixed_count += 1
    
    print(f"修复完成！共修复了 {fixed_count} 个文件")

if __name__ == '__main__':
    main()