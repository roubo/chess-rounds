#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import os
import re

def clean_toast_calls(file_path):
    """清理文件中的uni.showToast调用"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        original_content = content
        
        # 匹配完整的uni.showToast调用块（包括多行参数）
        pattern = r'(\s*)(?://\s*)?uni\.showToast\s*\(\s*\{[^}]*?\}\s*\)'
        
        def replace_toast(match):
            indent = match.group(1)
            return f"{indent}// uni.showToast() - 已屏蔽"
        
        # 替换所有uni.showToast调用
        content = re.sub(pattern, replace_toast, content, flags=re.DOTALL)
        
        # 清理多余的注释行
        lines = content.split('\n')
        cleaned_lines = []
        
        for line in lines:
            # 跳过只包含注释符号和空白的行
            if re.match(r'^\s*//\s*$', line):
                continue
            # 跳过只包含参数但没有uni.showToast的行
            if re.match(r'^\s*//\s*(title:|icon:|duration:|mask:)', line):
                continue
            cleaned_lines.append(line)
        
        new_content = '\n'.join(cleaned_lines)
        
        # 如果内容有变化，写回文件
        if new_content != original_content:
            with open(file_path, 'w', encoding='utf-8') as f:
                f.write(new_content)
            print(f"清理文件: {file_path}")
            return True
        
    except Exception as e:
        print(f"处理文件 {file_path} 时出错: {e}")
    
    return False

def main():
    """主函数"""
    print("开始清理uni.showToast调用...")
    
    cleaned_count = 0
    
    # 遍历src目录下的所有文件
    for root, dirs, files in os.walk('src'):
        for file in files:
            if file.endswith(('.vue', '.js', '.ts')):
                file_path = os.path.join(root, file)
                if clean_toast_calls(file_path):
                    cleaned_count += 1
    
    print(f"清理完成！共处理了 {cleaned_count} 个文件")

if __name__ == '__main__':
    main()