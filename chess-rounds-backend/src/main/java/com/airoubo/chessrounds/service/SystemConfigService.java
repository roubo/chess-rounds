package com.airoubo.chessrounds.service;

import com.airoubo.chessrounds.entity.SystemConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 系统配置服务接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
public interface SystemConfigService {
    
    /**
     * 根据配置键获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    Optional<String> getConfigValue(String configKey);
    
    /**
     * 根据配置键获取配置值，如果不存在则返回默认值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);
    
    /**
     * 根据配置键获取整数配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 整数配置值
     */
    Integer getIntConfigValue(String configKey, Integer defaultValue);
    
    /**
     * 根据配置键获取长整数配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 长整数配置值
     */
    Long getLongConfigValue(String configKey, Long defaultValue);
    
    /**
     * 根据配置键获取布尔配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 布尔配置值
     */
    Boolean getBooleanConfigValue(String configKey, Boolean defaultValue);
    
    /**
     * 根据配置键获取双精度配置值
     * 
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 双精度配置值
     */
    Double getDoubleConfigValue(String configKey, Double defaultValue);
    
    /**
     * 设置配置值
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     * @param configType 配置类型
     * @param description 配置描述
     */
    void setConfigValue(String configKey, String configValue, String configType, String description);
    
    /**
     * 设置配置值（简化版本）
     * 
     * @param configKey 配置键
     * @param configValue 配置值
     */
    void setConfigValue(String configKey, String configValue);
    
    /**
     * 批量设置配置
     * 
     * @param configs 配置映射（键 -> 值）
     */
    void batchSetConfigs(Map<String, String> configs);
    
    /**
     * 删除配置
     * 
     * @param configKey 配置键
     */
    void deleteConfig(String configKey);
    
    /**
     * 批量删除配置
     * 
     * @param configKeys 配置键列表
     */
    void batchDeleteConfigs(List<String> configKeys);
    
    /**
     * 获取所有配置
     * 
     * @param pageable 分页参数
     * @return 配置列表
     */
    Page<SystemConfig> getAllConfigs(Pageable pageable);
    
    /**
     * 根据配置类型获取配置
     * 
     * @param configType 配置类型
     * @param pageable 分页参数
     * @return 配置列表
     */
    Page<SystemConfig> getConfigsByType(String configType, Pageable pageable);
    
    /**
     * 根据启用状态获取配置
     * 
     * @param enabled 是否启用
     * @param pageable 分页参数
     * @return 配置列表
     */
    Page<SystemConfig> getConfigsByEnabled(Boolean enabled, Pageable pageable);
    
    /**
     * 搜索配置（按键或描述模糊查询）
     * 
     * @param keyword 关键字
     * @param pageable 分页参数
     * @return 配置列表
     */
    Page<SystemConfig> searchConfigs(String keyword, Pageable pageable);
    
    /**
     * 获取配置映射
     * 
     * @param configKeys 配置键列表（可选，为空则获取所有）
     * @return 配置映射（键 -> 值）
     */
    Map<String, String> getConfigMap(List<String> configKeys);
    
    /**
     * 获取所有启用的配置映射
     * 
     * @return 配置映射（键 -> 值）
     */
    Map<String, String> getAllEnabledConfigMap();
    
    /**
     * 根据类型获取配置映射
     * 
     * @param configType 配置类型
     * @return 配置映射（键 -> 值）
     */
    Map<String, String> getConfigMapByType(String configType);
    
    /**
     * 启用配置
     * 
     * @param configKey 配置键
     */
    void enableConfig(String configKey);
    
    /**
     * 禁用配置
     * 
     * @param configKey 配置键
     */
    void disableConfig(String configKey);
    
    /**
     * 批量启用配置
     * 
     * @param configKeys 配置键列表
     */
    void batchEnableConfigs(List<String> configKeys);
    
    /**
     * 批量禁用配置
     * 
     * @param configKeys 配置键列表
     */
    void batchDisableConfigs(List<String> configKeys);
    
    /**
     * 检查配置是否存在
     * 
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean existsConfig(String configKey);
    
    /**
     * 检查配置是否启用
     * 
     * @param configKey 配置键
     * @return 是否启用
     */
    boolean isConfigEnabled(String configKey);
    
    /**
     * 统计配置数量
     * 
     * @param configType 配置类型（可选）
     * @param enabled 启用状态（可选）
     * @return 配置数量
     */
    Long countConfigs(String configType, Boolean enabled);
    
    /**
     * 获取所有配置类型
     * 
     * @return 配置类型列表
     */
    List<String> getAllConfigTypes();
    
    /**
     * 重置配置为默认值
     * 
     * @param configKey 配置键
     */
    void resetConfigToDefault(String configKey);
    
    /**
     * 批量重置配置为默认值
     * 
     * @param configKeys 配置键列表
     */
    void batchResetConfigsToDefault(List<String> configKeys);
    
    /**
     * 导出配置
     * 
     * @param configType 配置类型（可选）
     * @return 配置JSON字符串
     */
    String exportConfigs(String configType);
    
    /**
     * 导入配置
     * 
     * @param configJson 配置JSON字符串
     * @param overwrite 是否覆盖已存在的配置
     */
    void importConfigs(String configJson, boolean overwrite);
    
    /**
     * 刷新配置缓存
     */
    void refreshConfigCache();
    
    /**
     * 清空配置缓存
     */
    void clearConfigCache();
    
    /**
     * 获取配置实体（内部使用）
     * 
     * @param configKey 配置键
     * @return 配置实体
     */
    Optional<SystemConfig> getConfigEntity(String configKey);
    
    /**
     * 保存配置实体（内部使用）
     * 
     * @param config 配置实体
     * @return 保存后的配置实体
     */
    SystemConfig saveConfig(SystemConfig config);
    
    /**
     * 常用配置键常量
     */
    interface ConfigKeys {
        String SYSTEM_NAME = "system.name";
        String SYSTEM_VERSION = "system.version";
        String SYSTEM_DESCRIPTION = "system.description";
        
        String JWT_SECRET = "jwt.secret";
        String JWT_EXPIRATION = "jwt.expiration";
        String JWT_REFRESH_EXPIRATION = "jwt.refresh.expiration";
        
        String ROUND_CODE_LENGTH = "round.code.length";
        String ROUND_CODE_PREFIX = "round.code.prefix";
        String ROUND_MAX_PARTICIPANTS = "round.max.participants";
        
        String STATISTICS_RETENTION_DAYS = "statistics.retention.days";
        String STATISTICS_AUTO_UPDATE = "statistics.auto.update";
        
        String CACHE_ENABLED = "cache.enabled";
        String CACHE_TTL = "cache.ttl";
        
        String LOG_LEVEL = "log.level";
        String LOG_RETENTION_DAYS = "log.retention.days";
    }
    
    /**
     * 配置类型常量
     */
    interface ConfigTypes {
        String SYSTEM = "system";
        String SECURITY = "security";
        String BUSINESS = "business";
        String CACHE = "cache";
        String LOG = "log";
        String CUSTOM = "custom";
    }
}