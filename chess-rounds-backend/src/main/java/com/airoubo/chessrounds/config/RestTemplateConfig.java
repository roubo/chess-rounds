package com.airoubo.chessrounds.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Arrays;

/**
 * RestTemplate配置
 * 
 * @author AiRoubo
 * @version 1.0.0
 */
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 连接超时10秒
        factory.setReadTimeout(30000);    // 读取超时30秒
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // 添加支持text/plain和带编码的application/json格式的JSON转换器
        // 解决微信API返回text/plain或application/json;encoding=utf-8但内容是JSON的问题
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Arrays.asList(
            MediaType.APPLICATION_JSON,
            MediaType.TEXT_PLAIN,  // 支持text/plain格式
            new MediaType("application", "json", java.nio.charset.StandardCharsets.UTF_8)  // 支持带编码的JSON格式
        ));
        
        // 设置默认字符集为UTF-8
        converter.setDefaultCharset(java.nio.charset.StandardCharsets.UTF_8);
        
        restTemplate.getMessageConverters().add(0, converter);
        
        // 设置自定义错误处理器
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                HttpStatus statusCode = HttpStatus.resolve(response.getRawStatusCode());
                // 对于微信API，某些4xx错误可能包含有用信息，不应该抛出异常
                if (statusCode != null && statusCode.series() == HttpStatus.Series.CLIENT_ERROR) {
                    // 412 Precondition Failed 可能包含微信API的错误信息
                    if (statusCode == HttpStatus.PRECONDITION_FAILED) {
                        return false; // 不当作错误处理，让业务代码处理响应
                    }
                }
                return super.hasError(response);
            }
        });
        
        return restTemplate;
    }
}