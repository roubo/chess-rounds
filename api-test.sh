#!/bin/bash

# API测试脚本 - 回合计数系统
# 使用方法: ./api-test.sh [BASE_URL]
# 示例: ./api-test.sh http://localhost:8080/api

set -e

# 配置
BASE_URL=${1:-"http://localhost:8080/api"}
CONTENT_TYPE="Content-Type: application/json"
TEST_RESULTS_FILE="api-test-results.log"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1" | tee -a $TEST_RESULTS_FILE
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" | tee -a $TEST_RESULTS_FILE
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" | tee -a $TEST_RESULTS_FILE
}

log_test() {
    echo -e "${BLUE}[TEST]${NC} $1" | tee -a $TEST_RESULTS_FILE
}

# 初始化测试
init_test() {
    log_info "开始API测试 - $(date)"
    log_info "测试目标: $BASE_URL"
    echo "" > $TEST_RESULTS_FILE
    
    # 检查服务是否可用
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null; then
        log_error "服务不可用，请确保应用已启动"
        exit 1
    fi
    
    log_info "服务健康检查通过"
}

# 测试健康检查接口
test_health_check() {
    log_test "测试健康检查接口"
    
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/actuator/health")
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        log_info "✓ 健康检查接口正常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    else
        log_error "✗ 健康检查接口异常 (HTTP $http_code)"
        return 1
    fi
}

# 测试微信登录接口
test_wechat_login() {
    log_test "测试微信登录接口"
    
    # 测试数据
    login_data='{
        "code": "test_code_123",
        "nickname": "测试用户",
        "avatar": "https://example.com/avatar.jpg"
    }'
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "$CONTENT_TYPE" \
        -d "$login_data" \
        "$BASE_URL/auth/wechat/login")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "400" ]; then
        log_info "✓ 微信登录接口响应正常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
        
        # 尝试提取token（如果登录成功）
        if [ "$http_code" = "200" ]; then
            ACCESS_TOKEN=$(echo "$body" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
            if [ -n "$ACCESS_TOKEN" ]; then
                log_info "获取到访问令牌: ${ACCESS_TOKEN:0:20}..."
            fi
        fi
    else
        log_error "✗ 微信登录接口异常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    fi
}

# 测试用户信息接口
test_user_profile() {
    log_test "测试用户信息接口"
    
    if [ -z "$ACCESS_TOKEN" ]; then
        log_warn "跳过用户信息测试 - 无有效token"
        return 0
    fi
    
    response=$(curl -s -w "\n%{http_code}" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        "$BASE_URL/users/profile")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "401" ]; then
        log_info "✓ 用户信息接口响应正常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    else
        log_error "✗ 用户信息接口异常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    fi
}

# 测试创建回合接口
test_create_round() {
    log_test "测试创建回合接口"
    
    if [ -z "$ACCESS_TOKEN" ]; then
        log_warn "跳过创建回合测试 - 无有效token"
        return 0
    fi
    
    round_data='{
        "roundName": "测试回合",
        "gameType": "MAHJONG",
        "maxParticipants": 4,
        "description": "API测试创建的回合"
    }'
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "$CONTENT_TYPE" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "$round_data" \
        "$BASE_URL/rounds")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "201" ] || [ "$http_code" = "401" ]; then
        log_info "✓ 创建回合接口响应正常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
        
        # 尝试提取回合ID
        if [ "$http_code" = "200" ] || [ "$http_code" = "201" ]; then
            ROUND_ID=$(echo "$body" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
            if [ -n "$ROUND_ID" ]; then
                log_info "创建回合成功，ID: $ROUND_ID"
            fi
        fi
    else
        log_error "✗ 创建回合接口异常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    fi
}

# 测试查询回合列表接口
test_list_rounds() {
    log_test "测试查询回合列表接口"
    
    response=$(curl -s -w "\n%{http_code}" \
        "$BASE_URL/rounds?page=0&size=10")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ]; then
        log_info "✓ 查询回合列表接口正常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    else
        log_error "✗ 查询回合列表接口异常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    fi
}

# 测试AI分析接口
test_ai_analysis() {
    log_test "测试AI分析接口"
    
    if [ -z "$ACCESS_TOKEN" ]; then
        log_warn "跳过AI分析测试 - 无有效token"
        return 0
    fi
    
    analysis_data='{
        "roundId": 1,
        "analysisType": "PERFORMANCE",
        "prompt": "分析这个回合的表现"
    }'
    
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "$CONTENT_TYPE" \
        -H "Authorization: Bearer $ACCESS_TOKEN" \
        -d "$analysis_data" \
        "$BASE_URL/ai/analysis")
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "200" ] || [ "$http_code" = "400" ] || [ "$http_code" = "401" ]; then
        log_info "✓ AI分析接口响应正常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    else
        log_error "✗ AI分析接口异常 (HTTP $http_code)"
        echo "响应: $body" >> $TEST_RESULTS_FILE
    fi
}

# 测试错误处理
test_error_handling() {
    log_test "测试错误处理"
    
    # 测试不存在的接口
    response=$(curl -s -w "\n%{http_code}" "$BASE_URL/nonexistent")
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "404" ]; then
        log_info "✓ 404错误处理正常"
    else
        log_warn "404错误处理可能有问题 (HTTP $http_code)"
    fi
    
    # 测试无效JSON
    response=$(curl -s -w "\n%{http_code}" -X POST \
        -H "$CONTENT_TYPE" \
        -d "invalid json" \
        "$BASE_URL/auth/wechat/login")
    
    http_code=$(echo "$response" | tail -n1)
    
    if [ "$http_code" = "400" ]; then
        log_info "✓ 无效JSON错误处理正常"
    else
        log_warn "无效JSON错误处理可能有问题 (HTTP $http_code)"
    fi
}

# 生成测试报告
generate_report() {
    log_info "测试完成 - $(date)"
    log_info "详细结果请查看: $TEST_RESULTS_FILE"
    
    echo ""
    echo "=== API测试报告 ==="
    echo "测试时间: $(date)"
    echo "测试目标: $BASE_URL"
    echo "结果文件: $TEST_RESULTS_FILE"
    echo ""
    
    # 统计测试结果
    total_tests=$(grep -c "\[TEST\]" $TEST_RESULTS_FILE || echo "0")
    passed_tests=$(grep -c "✓" $TEST_RESULTS_FILE || echo "0")
    failed_tests=$(grep -c "✗" $TEST_RESULTS_FILE || echo "0")
    
    echo "总测试数: $total_tests"
    echo "通过: $passed_tests"
    echo "失败: $failed_tests"
    
    if [ "$failed_tests" -eq 0 ]; then
        log_info "所有测试通过！"
    else
        log_warn "有 $failed_tests 个测试失败，请检查日志"
    fi
}

# 主函数
main() {
    echo "回合计数系统 - API测试脚本"
    echo "================================="
    
    init_test
    
    # 执行测试
    test_health_check
    test_wechat_login
    test_user_profile
    test_create_round
    test_list_rounds
    test_ai_analysis
    test_error_handling
    
    generate_report
}

# 执行主函数
main "$@"