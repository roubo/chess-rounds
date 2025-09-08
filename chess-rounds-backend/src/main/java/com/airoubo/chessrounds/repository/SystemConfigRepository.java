package com.airoubo.chessrounds.repository;

import com.airoubo.chessrounds.entity.SystemConfig;
import com.airoubo.chessrounds.enums.ConfigType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 系统配置Repository接口
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, Long> {
    
    /**
     * 根据配置键查询配置
     * 
     * @param configKey 配置键
     * @return 配置信息
     */
    Optional<SystemConfig> findByConfigKey(String configKey);
    
    /**
     * 根据配置类型查询配置
     * 
     * @param configType 配置类型
     * @return 配置列表
     */
    List<SystemConfig> findByConfigType(ConfigType configType);
    
    /**
     * 根据配置键和配置类型查询配置
     * 
     * @param configKey 配置键
     * @param configType 配置类型
     * @return 配置信息
     */
    Optional<SystemConfig> findByConfigKeyAndConfigType(String configKey, ConfigType configType);
    
    /**
     * 查询启用的配置
     * 
     * @param isActive 是否启用
     * @return 配置列表
     */
    List<SystemConfig> findByIsActive(Boolean isActive);
    
    /**
     * 根据配置类型和启用状态查询配置
     * 
     * @param configType 配置类型
     * @param isActive 是否启用
     * @return 配置列表
     */
    List<SystemConfig> findByConfigTypeAndIsActive(ConfigType configType, Boolean isActive);
    
    /**
     * 根据配置键前缀查询配置
     * 
     * @param keyPrefix 配置键前缀
     * @return 配置列表
     */
    List<SystemConfig> findByConfigKeyStartingWith(String keyPrefix);
    
    /**
     * 根据配置键包含关键字查询配置
     * 
     * @param keyword 关键字
     * @return 配置列表
     */
    List<SystemConfig> findByConfigKeyContaining(String keyword);
    
    /**
     * 根据描述包含关键字查询配置
     * 
     * @param keyword 关键字
     * @return 配置列表
     */
    List<SystemConfig> findByDescriptionContaining(String keyword);
    
    /**
     * 检查配置键是否存在
     * 
     * @param configKey 配置键
     * @return 是否存在
     */
    boolean existsByConfigKey(String configKey);
    
    /**
     * 统计配置数量
     * 
     * @param configType 配置类型
     * @return 配置数量
     */
    Long countByConfigType(ConfigType configType);
    
    /**
     * 统计启用的配置数量
     * 
     * @param isActive 是否启用
     * @return 配置数量
     */
    Long countByIsActive(Boolean isActive);
    
    /**
     * 查询所有配置键
     * 
     * @return 配置键列表
     */
    @Query("SELECT sc.configKey FROM SystemConfig sc")
    List<String> findAllConfigKeys();
    
    /**
     * 根据配置类型查询所有配置键
     * 
     * @param configType 配置类型
     * @return 配置键列表
     */
    @Query("SELECT sc.configKey FROM SystemConfig sc WHERE sc.configType = :configType")
    List<String> findConfigKeysByConfigType(@Param("configType") ConfigType configType);
    
    /**
     * 查询启用的配置键
     * 
     * @return 配置键列表
     */
    @Query("SELECT sc.configKey FROM SystemConfig sc WHERE sc.isActive = true")
    List<String> findEnabledConfigKeys();
    
    /**
     * 根据配置键获取配置值
     * 
     * @param configKey 配置键
     * @return 配置值
     */
    @Query("SELECT sc.configValue FROM SystemConfig sc WHERE sc.configKey = :configKey AND sc.isActive = true")
    Optional<String> findConfigValueByKey(@Param("configKey") String configKey);
    
    /**
     * 批量查询配置值
     * 
     * @param configKeys 配置键列表
     * @return 配置列表
     */
    @Query("SELECT sc FROM SystemConfig sc WHERE sc.configKey IN :configKeys AND sc.isActive = true")
    List<SystemConfig> findByConfigKeysAndIsActive(@Param("configKeys") List<String> configKeys);
}