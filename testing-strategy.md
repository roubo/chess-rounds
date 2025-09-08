# 测试策略文档

## 概述

本文档详细说明"回合"项目的测试策略，包括单元测试、集成测试、性能测试和小程序测试的完整方案，确保系统质量和稳定性。

## 第一部分：测试架构

### 1.1 测试金字塔

```
        E2E Tests (10%)
      ┌─────────────────┐
     │  小程序端到端测试  │
    └─────────────────┘
   Integration Tests (20%)
  ┌─────────────────────────┐
 │    API集成测试 + 数据库测试   │
└─────────────────────────┘
      Unit Tests (70%)
┌─────────────────────────────────┐
│  Service层 + Repository层 + Utils │
└─────────────────────────────────┘
```

### 1.2 测试环境配置

```yaml
# application-test.yml
spring:
  profiles:
    active: test
  
  # 测试数据库配置
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  
  # JPA配置
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect
  
  # Redis测试配置
  data:
    redis:
      host: localhost
      port: 6379
      database: 15  # 使用独立的测试数据库
  
  # 微信API Mock配置
  wechat:
    app-id: test-app-id
    app-secret: test-app-secret
    mock-enabled: true

# 日志配置
logging:
  level:
    com.chessrounds: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG

# 测试配置
app:
  jwt:
    secret: test-jwt-secret-key-for-testing-only-not-for-production
    expiration: 3600
  
  ai:
    mock-enabled: true
    mock-delay: 100  # 模拟AI响应延迟
```

## 第二部分：单元测试

### 2.1 Service层测试

#### 2.1.1 用户服务测试

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private WechatApiClient wechatApiClient;
    
    @Mock
    private JwtTokenProvider tokenProvider;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    @DisplayName("微信登录 - 新用户注册")
    void testWechatLogin_NewUser() {
        // Given
        String code = "test-wx-code";
        String openId = "test-open-id";
        String sessionKey = "test-session-key";
        
        WechatLoginResponse wxResponse = WechatLoginResponse.builder()
                .openId(openId)
                .sessionKey(sessionKey)
                .build();
        
        when(wechatApiClient.code2Session(code)).thenReturn(wxResponse);
        when(userRepository.findByOpenId(openId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("test-user-id");
            return user;
        });
        when(tokenProvider.generateToken(any())).thenReturn("test-jwt-token");
        
        // When
        LoginResponse response = userService.wechatLogin(code, null);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getUser().getOpenId()).isEqualTo(openId);
        assertThat(response.isNewUser()).isTrue();
        
        verify(userRepository).save(argThat(user -> 
            user.getOpenId().equals(openId) && 
            user.getStatus() == UserStatus.ACTIVE
        ));
    }
    
    @Test
    @DisplayName("微信登录 - 已存在用户")
    void testWechatLogin_ExistingUser() {
        // Given
        String code = "test-wx-code";
        String openId = "test-open-id";
        
        User existingUser = User.builder()
                .id("existing-user-id")
                .openId(openId)
                .nickname("测试用户")
                .status(UserStatus.ACTIVE)
                .build();
        
        WechatLoginResponse wxResponse = WechatLoginResponse.builder()
                .openId(openId)
                .sessionKey("test-session-key")
                .build();
        
        when(wechatApiClient.code2Session(code)).thenReturn(wxResponse);
        when(userRepository.findByOpenId(openId)).thenReturn(Optional.of(existingUser));
        when(tokenProvider.generateToken(any())).thenReturn("test-jwt-token");
        
        // When
        LoginResponse response = userService.wechatLogin(code, null);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("test-jwt-token");
        assertThat(response.getUser().getId()).isEqualTo("existing-user-id");
        assertThat(response.isNewUser()).isFalse();
        
        verify(userRepository).save(argThat(user -> 
            user.getLastLoginAt() != null
        ));
    }
    
    @Test
    @DisplayName("微信登录 - 微信API异常")
    void testWechatLogin_WechatApiException() {
        // Given
        String code = "invalid-code";
        
        when(wechatApiClient.code2Session(code))
                .thenThrow(new WechatApiException("invalid code"));
        
        // When & Then
        assertThatThrownBy(() -> userService.wechatLogin(code, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("微信登录失败");
    }
    
    @Test
    @DisplayName("获取用户统计数据")
    void testGetUserStatistics() {
        // Given
        String userId = "test-user-id";
        StatisticsPeriod period = StatisticsPeriod.MONTHLY;
        
        UserStatistics stats = UserStatistics.builder()
                .totalRounds(10)
                .totalRecords(50)
                .winRate(0.6)
                .totalPoints(1000)
                .build();
        
        when(userRepository.getUserStatistics(eq(userId), any(LocalDateTime.class)))
                .thenReturn(stats);
        
        // When
        UserStatisticsVO result = userService.getUserStatistics(userId, period);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalRounds()).isEqualTo(10);
        assertThat(result.getTotalRecords()).isEqualTo(50);
        assertThat(result.getWinRate()).isEqualTo(60.0); // 转换为百分比
        assertThat(result.getTotalPoints()).isEqualTo(1000);
    }
}
```

#### 2.1.2 回合服务测试

```java
@ExtendWith(MockitoExtension.class)
class RoundServiceTest {
    
    @Mock
    private RoundRepository roundRepository;
    
    @Mock
    private RoundParticipantRepository participantRepository;
    
    @Mock
    private RoundRecordRepository recordRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AiAnalysisService aiAnalysisService;
    
    @InjectMocks
    private RoundService roundService;
    
    @Test
    @DisplayName("创建回合 - 成功")
    void testCreateRound_Success() {
        // Given
        String creatorId = "creator-id";
        CreateRoundRequest request = CreateRoundRequest.builder()
                .multiplier(2.0)
                .hasDealer(true)
                .maxParticipants(4)
                .build();
        
        User creator = User.builder()
                .id(creatorId)
                .nickname("创建者")
                .build();
        
        when(userRepository.findById(creatorId)).thenReturn(Optional.of(creator));
        when(roundRepository.save(any(Round.class))).thenAnswer(invocation -> {
            Round round = invocation.getArgument(0);
            round.setId("round-id");
            round.setRoundCode("ABC123");
            return round;
        });
        
        // When
        RoundVO result = roundService.createRound(creatorId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("round-id");
        assertThat(result.getRoundCode()).isEqualTo("ABC123");
        assertThat(result.getStatus()).isEqualTo(RoundStatus.WAITING);
        assertThat(result.getMultiplier()).isEqualTo(2.0);
        assertThat(result.isHasDealer()).isTrue();
        
        verify(roundRepository).save(argThat(round -> 
            round.getCreatorId().equals(creatorId) &&
            round.getStatus() == RoundStatus.WAITING &&
            round.getMultiplier().equals(2.0)
        ));
    }
    
    @Test
    @DisplayName("加入回合 - 成功")
    void testJoinRound_Success() {
        // Given
        String roundCode = "ABC123";
        String userId = "user-id";
        
        Round round = Round.builder()
                .id("round-id")
                .roundCode(roundCode)
                .status(RoundStatus.WAITING)
                .maxParticipants(4)
                .build();
        
        User user = User.builder()
                .id(userId)
                .nickname("参与者")
                .build();
        
        when(roundRepository.findByRoundCode(roundCode)).thenReturn(Optional.of(round));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(participantRepository.countByRoundIdAndRole("round-id", ParticipantRole.PLAYER))
                .thenReturn(2L);
        when(participantRepository.existsByRoundIdAndUserId("round-id", userId))
                .thenReturn(false);
        
        // When
        RoundVO result = roundService.joinRound(roundCode, userId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("round-id");
        
        verify(participantRepository).save(argThat(participant -> 
            participant.getRoundId().equals("round-id") &&
            participant.getUserId().equals(userId) &&
            participant.getRole() == ParticipantRole.PLAYER
        ));
    }
    
    @Test
    @DisplayName("加入回合 - 回合已满")
    void testJoinRound_RoundFull() {
        // Given
        String roundCode = "ABC123";
        String userId = "user-id";
        
        Round round = Round.builder()
                .id("round-id")
                .roundCode(roundCode)
                .status(RoundStatus.WAITING)
                .maxParticipants(4)
                .build();
        
        when(roundRepository.findByRoundCode(roundCode)).thenReturn(Optional.of(round));
        when(participantRepository.countByRoundIdAndRole("round-id", ParticipantRole.PLAYER))
                .thenReturn(4L);
        
        // When & Then
        assertThatThrownBy(() -> roundService.joinRound(roundCode, userId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("回合已满");
    }
    
    @Test
    @DisplayName("添加记录 - 成功")
    void testAddRecord_Success() {
        // Given
        String roundId = "round-id";
        AddRecordRequest request = AddRecordRequest.builder()
                .winnerId("winner-id")
                .loserId("loser-id")
                .basePoints(100)
                .actualPoints(200)
                .build();
        
        Round round = Round.builder()
                .id(roundId)
                .status(RoundStatus.IN_PROGRESS)
                .multiplier(2.0)
                .build();
        
        when(roundRepository.findById(roundId)).thenReturn(Optional.of(round));
        when(participantRepository.existsByRoundIdAndUserId(roundId, "winner-id"))
                .thenReturn(true);
        when(participantRepository.existsByRoundIdAndUserId(roundId, "loser-id"))
                .thenReturn(true);
        when(recordRepository.save(any(RoundRecord.class))).thenAnswer(invocation -> {
            RoundRecord record = invocation.getArgument(0);
            record.setId("record-id");
            return record;
        });
        
        // When
        RoundRecordVO result = roundService.addRecord(roundId, request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("record-id");
        assertThat(result.getWinnerId()).isEqualTo("winner-id");
        assertThat(result.getLoserId()).isEqualTo("loser-id");
        assertThat(result.getBasePoints()).isEqualTo(100);
        assertThat(result.getActualPoints()).isEqualTo(200);
        
        verify(recordRepository).save(argThat(record -> 
            record.getRoundId().equals(roundId) &&
            record.getWinnerId().equals("winner-id") &&
            record.getLoserId().equals("loser-id")
        ));
        
        // 验证AI分析被调用
        verify(aiAnalysisService).analyzeRecord(any(RoundRecord.class));
    }
}
```

### 2.2 Repository层测试

```java
@DataJpaTest
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb"
})
class RoundRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private RoundRepository roundRepository;
    
    @Test
    @DisplayName("根据回合码查找回合")
    void testFindByRoundCode() {
        // Given
        Round round = Round.builder()
                .roundCode("ABC123")
                .creatorId("creator-id")
                .status(RoundStatus.WAITING)
                .multiplier(1.0)
                .maxParticipants(4)
                .hasDealer(false)
                .build();
        
        entityManager.persistAndFlush(round);
        
        // When
        Optional<Round> result = roundRepository.findByRoundCode("ABC123");
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getRoundCode()).isEqualTo("ABC123");
        assertThat(result.get().getCreatorId()).isEqualTo("creator-id");
    }
    
    @Test
    @DisplayName("查找用户的回合列表")
    void testFindUserRounds() {
        // Given
        String userId = "user-id";
        
        // 创建测试数据
        Round round1 = createTestRound("ABC123", userId, RoundStatus.IN_PROGRESS);
        Round round2 = createTestRound("DEF456", userId, RoundStatus.FINISHED);
        Round round3 = createTestRound("GHI789", "other-user", RoundStatus.IN_PROGRESS);
        
        entityManager.persistAndFlush(round1);
        entityManager.persistAndFlush(round2);
        entityManager.persistAndFlush(round3);
        
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        
        // When
        Page<Round> result = roundRepository.findUserRounds(userId, RoundStatus.IN_PROGRESS, pageable);
        
        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getRoundCode()).isEqualTo("ABC123");
    }
    
    private Round createTestRound(String code, String creatorId, RoundStatus status) {
        return Round.builder()
                .roundCode(code)
                .creatorId(creatorId)
                .status(status)
                .multiplier(1.0)
                .maxParticipants(4)
                .hasDealer(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
```

### 2.3 工具类测试

```java
class CodeGeneratorTest {
    
    @Test
    @DisplayName("生成回合码 - 格式正确")
    void testGenerateRoundCode_Format() {
        // When
        String code = CodeGenerator.generateRoundCode();
        
        // Then
        assertThat(code).hasSize(6);
        assertThat(code).matches("[A-Z0-9]{6}");
    }
    
    @Test
    @DisplayName("生成回合码 - 唯一性")
    void testGenerateRoundCode_Uniqueness() {
        // Given
        Set<String> codes = new HashSet<>();
        
        // When
        for (int i = 0; i < 1000; i++) {
            codes.add(CodeGenerator.generateRoundCode());
        }
        
        // Then
        assertThat(codes).hasSize(1000); // 所有生成的码都是唯一的
    }
}

class EncryptionUtilTest {
    
    @Test
    @DisplayName("AES加密解密")
    void testAesEncryptDecrypt() {
        // Given
        String plainText = "sensitive-data";
        String key = "test-encryption-key-32-characters";
        
        // When
        String encrypted = EncryptionUtil.encrypt(plainText, key);
        String decrypted = EncryptionUtil.decrypt(encrypted, key);
        
        // Then
        assertThat(encrypted).isNotEqualTo(plainText);
        assertThat(decrypted).isEqualTo(plainText);
    }
    
    @Test
    @DisplayName("密码哈希验证")
    void testPasswordHash() {
        // Given
        String password = "test-password";
        
        // When
        String hash1 = EncryptionUtil.hashPassword(password);
        String hash2 = EncryptionUtil.hashPassword(password);
        
        // Then
        assertThat(hash1).isNotEqualTo(hash2); // 每次哈希结果不同（因为salt）
        assertThat(EncryptionUtil.verifyPassword(password, hash1)).isTrue();
        assertThat(EncryptionUtil.verifyPassword(password, hash2)).isTrue();
        assertThat(EncryptionUtil.verifyPassword("wrong-password", hash1)).isFalse();
    }
}
```

## 第三部分：集成测试

### 3.1 API集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class UserControllerIntegrationTest {
    
    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("chess_rounds_test")
            .withUsername("test")
            .withPassword("test");
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private UserRepository userRepository;
    
    @MockBean
    private WechatApiClient wechatApiClient;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
    
    @Test
    @DisplayName("微信登录接口 - 集成测试")
    void testWechatLoginEndpoint() {
        // Given
        String code = "test-wx-code";
        WechatLoginRequest request = new WechatLoginRequest(code, null);
        
        WechatLoginResponse mockResponse = WechatLoginResponse.builder()
                .openId("test-open-id")
                .sessionKey("test-session-key")
                .build();
        
        when(wechatApiClient.code2Session(code)).thenReturn(mockResponse);
        
        // When
        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.postForEntity(
                "/api/v1/auth/wechat/login",
                request,
                new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getCode()).isEqualTo(200);
        assertThat(response.getBody().getData().getToken()).isNotBlank();
        assertThat(response.getBody().getData().isNewUser()).isTrue();
        
        // 验证数据库中用户已创建
        Optional<User> savedUser = userRepository.findByOpenId("test-open-id");
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
    
    @Test
    @DisplayName("获取用户信息接口 - 需要认证")
    void testGetUserProfile_RequiresAuth() {
        // Given - 不提供认证头
        
        // When
        ResponseEntity<ApiResponse> response = restTemplate.getForEntity(
                "/api/v1/users/profile",
                ApiResponse.class
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
    
    @Test
    @DisplayName("获取用户信息接口 - 认证成功")
    void testGetUserProfile_WithAuth() {
        // Given
        User testUser = createTestUser();
        String token = generateTestToken(testUser);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        // When
        ResponseEntity<ApiResponse<UserProfileVO>> response = restTemplate.exchange(
                "/api/v1/users/profile",
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<ApiResponse<UserProfileVO>>() {}
        );
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData().getId()).isEqualTo(testUser.getId());
        assertThat(response.getBody().getData().getNickname()).isEqualTo(testUser.getNickname());
    }
    
    private User createTestUser() {
        User user = User.builder()
                .openId("test-open-id-" + System.currentTimeMillis())
                .nickname("测试用户")
                .avatarUrl("https://example.com/avatar.jpg")
                .status(UserStatus.ACTIVE)
                .build();
        return userRepository.save(user);
    }
    
    private String generateTestToken(User user) {
        // 使用实际的JWT生成逻辑
        UserPrincipal principal = UserPrincipal.create(user);
        return jwtTokenProvider.generateToken(principal);
    }
}
```

### 3.2 数据库集成测试

```java
@SpringBootTest
@Transactional
@Rollback
class DatabaseIntegrationTest {
    
    @Autowired
    private RoundService roundService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    @DisplayName("完整回合流程测试")
    void testCompleteRoundFlow() {
        // 1. 创建用户
        User creator = createTestUser("creator");
        User player1 = createTestUser("player1");
        User player2 = createTestUser("player2");
        
        // 2. 创建回合
        CreateRoundRequest createRequest = CreateRoundRequest.builder()
                .multiplier(2.0)
                .hasDealer(false)
                .maxParticipants(4)
                .build();
        
        RoundVO round = roundService.createRound(creator.getId(), createRequest);
        assertThat(round.getStatus()).isEqualTo(RoundStatus.WAITING);
        
        // 3. 玩家加入回合
        roundService.joinRound(round.getRoundCode(), player1.getId());
        roundService.joinRound(round.getRoundCode(), player2.getId());
        
        // 4. 开始回合
        roundService.startRound(round.getId());
        
        RoundVO startedRound = roundService.getRoundDetails(round.getId());
        assertThat(startedRound.getStatus()).isEqualTo(RoundStatus.IN_PROGRESS);
        assertThat(startedRound.getParticipants()).hasSize(3); // 创建者 + 2个玩家
        
        // 5. 添加记录
        AddRecordRequest recordRequest = AddRecordRequest.builder()
                .winnerId(creator.getId())
                .loserId(player1.getId())
                .basePoints(100)
                .actualPoints(200)
                .build();
        
        RoundRecordVO record = roundService.addRecord(round.getId(), recordRequest);
        assertThat(record.getWinnerId()).isEqualTo(creator.getId());
        assertThat(record.getActualPoints()).isEqualTo(200);
        
        // 6. 结束回合
        roundService.finishRound(round.getId());
        
        RoundVO finishedRound = roundService.getRoundDetails(round.getId());
        assertThat(finishedRound.getStatus()).isEqualTo(RoundStatus.FINISHED);
        assertThat(finishedRound.getRecords()).hasSize(1);
        
        // 7. 验证统计数据更新
        UserStatisticsVO creatorStats = userService.getUserStatistics(
                creator.getId(), StatisticsPeriod.ALL_TIME);
        assertThat(creatorStats.getTotalRounds()).isEqualTo(1);
        assertThat(creatorStats.getTotalRecords()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("并发加入回合测试")
    void testConcurrentJoinRound() throws InterruptedException {
        // Given
        User creator = createTestUser("creator");
        Round round = createTestRound(creator.getId(), 2); // 最多2个参与者
        
        List<User> players = IntStream.range(0, 5)
                .mapToObj(i -> createTestUser("player" + i))
                .collect(Collectors.toList());
        
        CountDownLatch latch = new CountDownLatch(5);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());
        List<String> successfulJoins = Collections.synchronizedList(new ArrayList<>());
        
        // When - 5个用户同时尝试加入只能容纳2人的回合
        ExecutorService executor = Executors.newFixedThreadPool(5);
        
        players.forEach(player -> {
            executor.submit(() -> {
                try {
                    roundService.joinRound(round.getRoundCode(), player.getId());
                    successfulJoins.add(player.getId());
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        });
        
        latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();
        
        // Then
        assertThat(successfulJoins).hasSize(2); // 只有2个用户成功加入
        assertThat(exceptions).hasSize(3); // 3个用户失败
        
        // 验证数据库状态
        long participantCount = participantRepository.countByRoundIdAndRole(
                round.getId(), ParticipantRole.PLAYER);
        assertThat(participantCount).isEqualTo(2);
    }
    
    private User createTestUser(String prefix) {
        User user = User.builder()
                .openId(prefix + "-" + System.currentTimeMillis() + "-" + Math.random())
                .nickname(prefix + "用户")
                .status(UserStatus.ACTIVE)
                .build();
        return entityManager.persistAndFlush(user);
    }
    
    private Round createTestRound(String creatorId, int maxParticipants) {
        Round round = Round.builder()
                .roundCode(CodeGenerator.generateRoundCode())
                .creatorId(creatorId)
                .status(RoundStatus.WAITING)
                .multiplier(1.0)
                .maxParticipants(maxParticipants)
                .hasDealer(false)
                .build();
        return entityManager.persistAndFlush(round);
    }
}
```

## 第四部分：性能测试

### 4.1 JMeter测试计划

```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0" jmeter="5.4.1">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="回合性能测试" enabled="true">
      <stringProp name="TestPlan.comments">回合系统性能测试计划</stringProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
      <elementProp name="TestPlan.arguments" elementType="Arguments" guiclass="ArgumentsPanel" testclass="Arguments" testname="用户定义的变量" enabled="true">
        <collectionProp name="Arguments.arguments">
          <elementProp name="BASE_URL" elementType="Argument">
            <stringProp name="Argument.name">BASE_URL</stringProp>
            <stringProp name="Argument.value">https://api.airoubo.com</stringProp>
          </elementProp>
          <elementProp name="CONCURRENT_USERS" elementType="Argument">
            <stringProp name="Argument.name">CONCURRENT_USERS</stringProp>
            <stringProp name="Argument.value">100</stringProp>
          </elementProp>
        </collectionProp>
      </elementProp>
    </TestPlan>
    <hashTree>
      <!-- 用户登录测试 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="用户登录压力测试" enabled="true">
        <stringProp name="ThreadGroup.on_sample_error">continue</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController" guiclass="LoopControlGui" testclass="LoopController" testname="循环控制器" enabled="true">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">10</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">${CONCURRENT_USERS}</stringProp>
        <stringProp name="ThreadGroup.ramp_time">60</stringProp>
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy" testname="微信登录" enabled="true">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments" guiclass="HTTPArgumentsPanel" testclass="Arguments" testname="用户定义的变量" enabled="true">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{"code":"test-code-${__threadNum}","userInfo":null}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.domain">${BASE_URL}</stringProp>
          <stringProp name="HTTPSampler.port"></stringProp>
          <stringProp name="HTTPSampler.protocol">https</stringProp>
          <stringProp name="HTTPSampler.contentEncoding"></stringProp>
          <stringProp name="HTTPSampler.path">/api/v1/auth/wechat/login</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.embedded_url_re"></stringProp>
          <stringProp name="HTTPSampler.connect_timeout"></stringProp>
          <stringProp name="HTTPSampler.response_timeout"></stringProp>
        </HTTPSamplerProxy>
      </hashTree>
      
      <!-- 回合操作测试 -->
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="回合操作压力测试" enabled="true">
        <stringProp name="ThreadGroup.num_threads">50</stringProp>
        <stringProp name="ThreadGroup.ramp_time">30</stringProp>
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <stringProp name="LoopController.loops">5</stringProp>
        </elementProp>
      </ThreadGroup>
      <hashTree>
        <!-- 创建回合 -->
        <HTTPSamplerProxy testname="创建回合" enabled="true">
          <stringProp name="HTTPSampler.path">/api/v1/rounds</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <stringProp name="Argument.value">{"multiplier":2.0,"hasDealer":false,"maxParticipants":4}</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
        </HTTPSamplerProxy>
        
        <!-- 加入回合 -->
        <HTTPSamplerProxy testname="加入回合" enabled="true">
          <stringProp name="HTTPSampler.path">/api/v1/rounds/${roundCode}/join</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
      
      <!-- 结果监听器 -->
      <ResultCollector guiclass="SummaryReport" testclass="ResultCollector" testname="汇总报告" enabled="true">
        <boolProp name="ResultCollector.error_logging">false</boolProp>
        <objProp>
          <name>saveConfig</name>
          <value class="SampleSaveConfiguration">
            <time>true</time>
            <latency>true</latency>
            <timestamp>true</timestamp>
            <success>true</success>
            <label>true</label>
            <code>true</code>
            <message>true</message>
            <threadName>true</threadName>
            <dataType>true</dataType>
            <encoding>false</encoding>
            <assertions>true</assertions>
            <subresults>true</subresults>
            <responseData>false</responseData>
            <samplerData>false</samplerData>
            <xml>false</xml>
            <fieldNames>true</fieldNames>
            <responseHeaders>false</responseHeaders>
            <requestHeaders>false</requestHeaders>
            <responseDataOnError>false</responseDataOnError>
            <saveAssertionResultsFailureMessage>true</saveAssertionResultsFailureMessage>
            <assertionsResultsToSave>0</assertionsResultsToSave>
            <bytes>true</bytes>
            <sentBytes>true</sentBytes>
            <url>true</url>
            <threadCounts>true</threadCounts>
            <idleTime>true</idleTime>
            <connectTime>true</connectTime>
          </value>
        </objProp>
        <stringProp name="filename">/opt/chess-rounds/test-results/performance-test-results.jtl</stringProp>
      </ResultCollector>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

### 4.2 性能测试脚本

```bash
#!/bin/bash
# 性能测试执行脚本

TEST_DIR="/opt/chess-rounds/performance-tests"
RESULT_DIR="/opt/chess-rounds/test-results"
JMETER_HOME="/opt/apache-jmeter"

# 创建结果目录
mkdir -p "$RESULT_DIR"

# 执行性能测试
echo "开始执行性能测试..."

# 1. 基准性能测试
echo "执行基准性能测试"
"$JMETER_HOME/bin/jmeter" -n -t "$TEST_DIR/baseline-test.jmx" \
  -l "$RESULT_DIR/baseline-results.jtl" \
  -e -o "$RESULT_DIR/baseline-report" \
  -JCONCURRENT_USERS=10 \
  -JRAMP_TIME=10 \
  -JLOOP_COUNT=5

# 2. 负载测试
echo "执行负载测试"
"$JMETER_HOME/bin/jmeter" -n -t "$TEST_DIR/load-test.jmx" \
  -l "$RESULT_DIR/load-results.jtl" \
  -e -o "$RESULT_DIR/load-report" \
  -JCONCURRENT_USERS=100 \
  -JRAMP_TIME=60 \
  -JLOOP_COUNT=10

# 3. 压力测试
echo "执行压力测试"
"$JMETER_HOME/bin/jmeter" -n -t "$TEST_DIR/stress-test.jmx" \
  -l "$RESULT_DIR/stress-results.jtl" \
  -e -o "$RESULT_DIR/stress-report" \
  -JCONCURRENT_USERS=500 \
  -JRAMP_TIME=300 \
  -JLOOP_COUNT=20

# 4. 峰值测试
echo "执行峰值测试"
"$JMETER_HOME/bin/jmeter" -n -t "$TEST_DIR/spike-test.jmx" \
  -l "$RESULT_DIR/spike-results.jtl" \
  -e -o "$RESULT_DIR/spike-report" \
  -JCONCURRENT_USERS=1000 \
  -JRAMP_TIME=60 \
  -JLOOP_COUNT=5

# 生成性能测试报告
echo "生成性能测试汇总报告"
python3 "$TEST_DIR/generate-performance-report.py" "$RESULT_DIR"

echo "性能测试完成，报告已生成到 $RESULT_DIR"
```

### 4.3 数据库性能测试

```sql
-- 数据库性能测试脚本

-- 1. 创建测试数据
DELIMITER //
CREATE PROCEDURE GenerateTestData(IN user_count INT, IN round_count INT, IN record_count INT)
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT DEFAULT 1;
    DECLARE k INT DEFAULT 1;
    DECLARE user_id VARCHAR(36);
    DECLARE round_id VARCHAR(36);
    
    -- 生成用户数据
    WHILE i <= user_count DO
        SET user_id = UUID();
        INSERT INTO users (id, open_id, nickname, avatar_url, status, created_at, updated_at)
        VALUES (user_id, CONCAT('test_open_id_', i), CONCAT('测试用户', i), 
                'https://example.com/avatar.jpg', 'ACTIVE', NOW(), NOW());
        SET i = i + 1;
    END WHILE;
    
    -- 生成回合数据
    SET i = 1;
    WHILE i <= round_count DO
        SET round_id = UUID();
        SET user_id = (SELECT id FROM users ORDER BY RAND() LIMIT 1);
        
        INSERT INTO rounds (id, round_code, creator_id, status, multiplier, max_participants, 
                           has_dealer, created_at, updated_at)
        VALUES (round_id, CONCAT('R', LPAD(i, 6, '0')), user_id, 'FINISHED', 
                1.0 + (RAND() * 2), 4, RAND() > 0.5, 
                DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY), NOW());
        
        -- 为每个回合生成参与者
        SET j = 1;
        WHILE j <= 3 DO
            SET user_id = (SELECT id FROM users WHERE id != 
                          (SELECT creator_id FROM rounds WHERE id = round_id) 
                          ORDER BY RAND() LIMIT 1);
            
            INSERT IGNORE INTO round_participants (id, round_id, user_id, role, joined_at)
            VALUES (UUID(), round_id, user_id, 'PLAYER', NOW());
            SET j = j + 1;
        END WHILE;
        
        SET i = i + 1;
    END WHILE;
    
    -- 生成记录数据
    SET i = 1;
    WHILE i <= record_count DO
        SET round_id = (SELECT id FROM rounds ORDER BY RAND() LIMIT 1);
        
        INSERT INTO round_records (id, round_id, winner_id, loser_id, base_points, 
                                  actual_points, created_at)
        SELECT UUID(), round_id, 
               (SELECT user_id FROM round_participants WHERE round_id = round_id ORDER BY RAND() LIMIT 1),
               (SELECT user_id FROM round_participants WHERE round_id = round_id ORDER BY RAND() LIMIT 1),
               FLOOR(50 + (RAND() * 200)),
               FLOOR(100 + (RAND() * 400)),
               DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY);
        
        SET i = i + 1;
    END WHILE;
END //
DELIMITER ;

-- 执行数据生成
CALL GenerateTestData(1000, 5000, 20000);

-- 2. 性能测试查询

-- 测试用户回合列表查询性能
SET @user_id = (SELECT id FROM users ORDER BY RAND() LIMIT 1);

EXPLAIN ANALYZE
SELECT 
    r.id,
    r.round_code,
    r.status,
    r.created_at,
    COUNT(DISTINCT rp.user_id) as participant_count,
    COUNT(DISTINCT rr.id) as record_count
FROM 
    rounds r
    LEFT JOIN round_participants rp ON r.id = rp.round_id AND rp.role = 'PLAYER'
    LEFT JOIN round_records rr ON r.id = rr.round_id
WHERE 
    r.creator_id = @user_id
    AND r.status IN ('IN_PROGRESS', 'FINISHED')
GROUP BY 
    r.id, r.round_code, r.status, r.created_at
ORDER BY 
    r.created_at DESC
LIMIT 20;

-- 测试统计查询性能
EXPLAIN ANALYZE
SELECT 
    DATE(rr.created_at) as date,
    COUNT(*) as total_records,
    SUM(CASE WHEN rr.winner_id = @user_id THEN 1 ELSE 0 END) as wins,
    SUM(CASE WHEN rr.loser_id = @user_id THEN 1 ELSE 0 END) as losses,
    SUM(CASE WHEN rr.winner_id = @user_id THEN rr.actual_points 
             WHEN rr.loser_id = @user_id THEN -rr.actual_points 
             ELSE 0 END) as total_points
FROM 
    round_records rr
WHERE 
    (rr.winner_id = @user_id OR rr.loser_id = @user_id)
    AND rr.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY 
    DATE(rr.created_at)
ORDER BY 
    date DESC;

-- 测试复杂统计查询性能
EXPLAIN ANALYZE
SELECT 
    u.id,
    u.nickname,
    COUNT(*) as total_games,
    SUM(CASE WHEN rr.winner_id = u.id THEN 1 ELSE 0 END) as wins,
    ROUND(SUM(CASE WHEN rr.winner_id = u.id THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2) as win_rate
FROM 
    users u
    JOIN round_records rr ON (u.id = rr.winner_id OR u.id = rr.loser_id)
WHERE 
    rr.created_at >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY 
    u.id, u.nickname
HAVING 
    total_games >= 10
ORDER BY 
    win_rate DESC, total_games DESC
LIMIT 10;

-- 3. 清理测试数据
-- DELETE FROM round_records WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
-- DELETE FROM round_participants WHERE joined_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
-- DELETE FROM rounds WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
-- DELETE FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR);
```

## 第五部分：小程序测试

### 5.1 小程序自动化测试

```javascript
// test/miniprogram/login.test.js
const automator = require('miniprogram-automator')

describe('登录功能测试', () => {
  let miniProgram
  let page

  beforeAll(async () => {
    miniProgram = await automator.launch({
      cliPath: '/Applications/wechatwebdevtools.app/Contents/MacOS/cli', // 工具cli位置
      projectPath: '/path/to/chess-rounds-miniprogram', // 项目文件地址
      account: 'test-account' // 测试账号
    })
    
    page = await miniProgram.reLaunch('/pages/index/index')
    await page.waitFor(500)
  })

  afterAll(async () => {
    await miniProgram.close()
  })

  test('应用启动正常', async () => {
    const title = await page.$('.page-title')
    expect(await title.text()).toBe('回合')
  })

  test('未登录状态显示登录按钮', async () => {
    const loginBtn = await page.$('.login-btn')
    expect(await loginBtn.attribute('disabled')).toBeFalsy()
  })

  test('点击登录按钮触发微信授权', async () => {
    const loginBtn = await page.$('.login-btn')
    await loginBtn.tap()
    
    // 等待授权弹窗
    await page.waitFor(1000)
    
    // 模拟用户同意授权
    const allowBtn = await page.$('.wx-authorize-allow')
    if (allowBtn) {
      await allowBtn.tap()
    }
    
    // 验证登录成功后的状态
    await page.waitFor(2000)
    const userInfo = await page.$('.user-info')
    expect(userInfo).toBeTruthy()
  })
})

// test/miniprogram/round.test.js
describe('回合功能测试', () => {
  let miniProgram
  let page

  beforeAll(async () => {
    miniProgram = await automator.launch({
      cliPath: '/Applications/wechatwebdevtools.app/Contents/MacOS/cli',
      projectPath: '/path/to/chess-rounds-miniprogram'
    })
    
    // 模拟登录状态
    await miniProgram.mockWxMethod('getStorageSync', (key) => {
      if (key === 'token') return 'mock-jwt-token'
      if (key === 'userInfo') return {
        id: 'mock-user-id',
        nickname: '测试用户',
        avatarUrl: 'https://example.com/avatar.jpg'
      }
      return null
    })
    
    page = await miniProgram.reLaunch('/pages/rounds/rounds')
    await page.waitFor(500)
  })

  afterAll(async () => {
    await miniProgram.close()
  })

  test('创建回合功能', async () => {
    // 点击创建回合按钮
    const createBtn = await page.$('.create-round-btn')
    await createBtn.tap()
    
    // 等待创建回合页面加载
    await page.waitFor(1000)
    
    // 设置倍率
    const multiplierInput = await page.$('.multiplier-input')
    await multiplierInput.input('2')
    
    // 设置最大参与者数量
    const maxParticipantsInput = await page.$('.max-participants-input')
    await maxParticipantsInput.input('4')
    
    // 提交创建
    const submitBtn = await page.$('.submit-btn')
    await submitBtn.tap()
    
    // 验证创建成功
    await page.waitFor(2000)
    const roundCode = await page.$('.round-code')
    expect(await roundCode.text()).toMatch(/^[A-Z0-9]{6}$/)
  })

  test('扫码加入回合功能', async () => {
    // 模拟扫码结果
    await miniProgram.mockWxMethod('scanCode', () => {
      return Promise.resolve({
        result: 'ABC123' // 模拟扫到的回合码
      })
    })
    
    // 点击扫码加入按钮
    const scanBtn = await page.$('.scan-join-btn')
    await scanBtn.tap()
    
    // 验证加入成功
    await page.waitFor(2000)
    const successMsg = await page.$('.success-message')
    expect(await successMsg.text()).toContain('加入成功')
  })

  test('回合列表显示', async () => {
    // 模拟API响应
    await miniProgram.mockWxMethod('request', (options) => {
      if (options.url.includes('/api/v1/rounds/my')) {
        return Promise.resolve({
          data: {
            code: 200,
            data: {
              content: [
                {
                  id: 'round-1',
                  roundCode: 'ABC123',
                  status: 'IN_PROGRESS',
                  participantCount: 3,
                  recordCount: 5,
                  createdAt: '2024-01-01T10:00:00'
                }
              ],
              totalElements: 1
            }
          }
        })
      }
    })
    
    // 下拉刷新
    await page.pullDownRefresh()
    await page.waitFor(1000)
    
    // 验证回合列表显示
    const roundItems = await page.$$('.round-item')
    expect(roundItems.length).toBe(1)
    
    const roundCode = await page.$('.round-code')
    expect(await roundCode.text()).toBe('ABC123')
  })
})
```

### 5.2 小程序性能测试

```javascript
// test/performance/miniprogram-performance.test.js
const automator = require('miniprogram-automator')

describe('小程序性能测试', () => {
  let miniProgram
  let page

  beforeAll(async () => {
    miniProgram = await automator.launch({
      cliPath: '/Applications/wechatwebdevtools.app/Contents/MacOS/cli',
      projectPath: '/path/to/chess-rounds-miniprogram'
    })
  })

  afterAll(async () => {
    await miniProgram.close()
  })

  test('页面启动性能', async () => {
    const startTime = Date.now()
    
    page = await miniProgram.reLaunch('/pages/index/index')
    await page.waitFor(500)
    
    const endTime = Date.now()
    const loadTime = endTime - startTime
    
    // 页面启动时间应小于2秒
    expect(loadTime).toBeLessThan(2000)
    console.log(`页面启动时间: ${loadTime}ms`)
  })

  test('列表滚动性能', async () => {
    // 模拟大量数据
    const mockData = Array.from({length: 100}, (_, i) => ({
      id: `round-${i}`,
      roundCode: `R${String(i).padStart(6, '0')}`,
      status: 'FINISHED',
      participantCount: 4,
      recordCount: Math.floor(Math.random() * 20) + 1
    }))
    
    await miniProgram.mockWxMethod('request', () => {
      return Promise.resolve({
        data: {
          code: 200,
          data: {
            content: mockData,
            totalElements: mockData.length
          }
        }
      })
    })
    
    page = await miniProgram.reLaunch('/pages/rounds/rounds')
    await page.waitFor(1000)
    
    // 测试滚动性能
    const startTime = Date.now()
    
    for (let i = 0; i < 10; i++) {
      await page.scrollTo(0, i * 100)
      await page.waitFor(50)
    }
    
    const endTime = Date.now()
    const scrollTime = endTime - startTime
    
    // 滚动操作应该流畅，总时间小于3秒
    expect(scrollTime).toBeLessThan(3000)
    console.log(`滚动性能测试时间: ${scrollTime}ms`)
  })

  test('内存使用监控', async () => {
    page = await miniProgram.reLaunch('/pages/index/index')
    
    // 获取初始内存使用
    const initialMemory = await miniProgram.evaluate(() => {
      return wx.getSystemInfoSync().memorySize
    })
    
    // 执行一系列操作
    await page.waitFor(500)
    await miniProgram.navigateTo('/pages/rounds/rounds')
    await page.waitFor(500)
    await miniProgram.navigateTo('/pages/profile/profile')
    await page.waitFor(500)
    
    // 获取操作后内存使用
    const finalMemory = await miniProgram.evaluate(() => {
      return wx.getSystemInfoSync().memorySize
    })
    
    const memoryIncrease = finalMemory - initialMemory
    
    // 内存增长应该在合理范围内（小于50MB）
    expect(memoryIncrease).toBeLessThan(50)
    console.log(`内存增长: ${memoryIncrease}MB`)
  })
})
```

### 5.3 小程序兼容性测试

```javascript
// test/compatibility/device-compatibility.test.js
const devices = [
  { name: 'iPhone 12', system: 'iOS 14.0' },
  { name: 'iPhone 8', system: 'iOS 13.0' },
  { name: 'Huawei P40', system: 'Android 10' },
  { name: 'Xiaomi 10', system: 'Android 11' },
  { name: 'Samsung Galaxy S21', system: 'Android 11' }
]

describe('设备兼容性测试', () => {
  devices.forEach(device => {
    describe(`${device.name} (${device.system})`, () => {
      let miniProgram
      let page

      beforeAll(async () => {
        miniProgram = await automator.launch({
          cliPath: '/Applications/wechatwebdevtools.app/Contents/MacOS/cli',
          projectPath: '/path/to/chess-rounds-miniprogram',
          device: device.name
        })
      })

      afterAll(async () => {
        await miniProgram.close()
      })

      test('基础功能正常', async () => {
        page = await miniProgram.reLaunch('/pages/index/index')
        await page.waitFor(1000)
        
        // 检查页面元素是否正常显示
        const title = await page.$('.page-title')
        expect(title).toBeTruthy()
        
        const tabBar = await page.$('.tab-bar')
        expect(tabBar).toBeTruthy()
      })

      test('API调用正常', async () => {
        // 模拟网络请求
        let requestCalled = false
        
        await miniProgram.mockWxMethod('request', (options) => {
          requestCalled = true
          return Promise.resolve({
            data: { code: 200, data: {} }
          })
        })
        
        page = await miniProgram.reLaunch('/pages/rounds/rounds')
        await page.waitFor(2000)
        
        expect(requestCalled).toBe(true)
      })
    })
  })
})
```

## 第六部分：测试执行计划

### 6.1 测试环境配置

```yaml
# docker-compose.test.yml
version: '3.8'

services:
  # 测试数据库
  test-mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: test_root_password
      MYSQL_DATABASE: chess_rounds_test
      MYSQL_USER: test_user
      MYSQL_PASSWORD: test_password
    ports:
      - "3307:3306"
    volumes:
      - ./test-data/mysql:/docker-entrypoint-initdb.d
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci

  # 测试Redis
  test-redis:
    image: redis:7-alpine
    ports:
      - "6380:6379"
    command: redis-server --appendonly yes

  # 测试应用
  test-app:
    build:
      context: .
      dockerfile: Dockerfile.test
    environment:
      SPRING_PROFILES_ACTIVE: test
      SPRING_DATASOURCE_URL: jdbc:mysql://test-mysql:3306/chess_rounds_test
      SPRING_DATASOURCE_USERNAME: test_user
      SPRING_DATASOURCE_PASSWORD: test_password
      SPRING_DATA_REDIS_HOST: test-redis
      SPRING_DATA_REDIS_PORT: 6379
    ports:
      - "8081:8080"
    depends_on:
      - test-mysql
      - test-redis
    volumes:
      - ./test-results:/app/test-results

  # JMeter性能测试
  jmeter:
    image: justb4/jmeter:5.4.1
    volumes:
      - ./performance-tests:/tests
      - ./test-results:/results
    command: >
      sh -c "jmeter -n -t /tests/load-test.jmx 
             -l /results/load-test-results.jtl 
             -e -o /results/load-test-report"
    depends_on:
      - test-app
```

### 6.2 CI/CD集成

```yaml
# .github/workflows/test.yml
name: 测试流水线

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  unit-tests:
    runs-on: ubuntu-latest
    
    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: test_password
          MYSQL_DATABASE: chess_rounds_test
        ports:
          - 3306:3306
        options: --health-cmd="mysqladmin ping" --health-interval=10s --health-timeout=5s --health-retries=3
      
      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: --health-cmd="redis-cli ping" --health-interval=10s --health-timeout=5s --health-retries=3
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 设置JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: 缓存Maven依赖
      uses: actions/cache@v3
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
        restore-keys: ${{ runner.os }}-m2
    
    - name: 运行单元测试
      run: |
        cd backend
        mvn clean test -Dspring.profiles.active=test
    
    - name: 生成测试报告
      run: |
        cd backend
        mvn jacoco:report
    
    - name: 上传测试覆盖率
      uses: codecov/codecov-action@v3
      with:
        file: ./backend/target/site/jacoco/jacoco.xml
        flags: unittests
        name: codecov-umbrella

  integration-tests:
    runs-on: ubuntu-latest
    needs: unit-tests
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 设置JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: 启动测试环境
      run: |
        docker-compose -f docker-compose.test.yml up -d
        sleep 30
    
    - name: 运行集成测试
      run: |
        cd backend
        mvn test -Dtest="**/*IntegrationTest" -Dspring.profiles.active=test
    
    - name: 清理测试环境
      run: |
        docker-compose -f docker-compose.test.yml down

  performance-tests:
    runs-on: ubuntu-latest
    needs: integration-tests
    if: github.ref == 'refs/heads/main'
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 启动应用
      run: |
        docker-compose -f docker-compose.test.yml up -d test-app
        sleep 60
    
    - name: 运行性能测试
      run: |
        docker-compose -f docker-compose.test.yml run --rm jmeter
    
    - name: 上传性能测试报告
      uses: actions/upload-artifact@v3
      with:
        name: performance-test-results
        path: test-results/
    
    - name: 清理环境
      run: |
        docker-compose -f docker-compose.test.yml down

  miniprogram-tests:
    runs-on: macos-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: 设置Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '16'
        cache: 'npm'
        cache-dependency-path: frontend/package-lock.json
    
    - name: 安装依赖
      run: |
        cd frontend
        npm ci
    
    - name: 运行小程序测试
      run: |
        cd frontend
        npm run test:miniprogram
    
    - name: 上传测试报告
      uses: actions/upload-artifact@v3
      with:
        name: miniprogram-test-results
        path: frontend/test-results/
```

### 6.3 测试执行脚本

```bash
#!/bin/bash
# run-tests.sh - 测试执行脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查依赖
check_dependencies() {
    log_info "检查测试依赖..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose未安装"
        exit 1
    fi
    
    if ! command -v mvn &> /dev/null; then
        log_error "Maven未安装"
        exit 1
    fi
    
    log_info "依赖检查完成"
}

# 启动测试环境
start_test_env() {
    log_info "启动测试环境..."
    
    docker-compose -f docker-compose.test.yml down
    docker-compose -f docker-compose.test.yml up -d test-mysql test-redis
    
    log_info "等待数据库启动..."
    sleep 30
    
    # 检查数据库连接
    until docker-compose -f docker-compose.test.yml exec -T test-mysql mysqladmin ping -h localhost --silent; do
        log_warn "等待MySQL启动..."
        sleep 5
    done
    
    log_info "测试环境启动完成"
}

# 运行单元测试
run_unit_tests() {
    log_info "运行单元测试..."
    
    cd backend
    mvn clean test -Dspring.profiles.active=test
    
    if [ $? -eq 0 ]; then
        log_info "单元测试通过"
    else
        log_error "单元测试失败"
        return 1
    fi
    
    cd ..
}

# 运行集成测试
run_integration_tests() {
    log_info "运行集成测试..."
    
    cd backend
    mvn test -Dtest="**/*IntegrationTest" -Dspring.profiles.active=test
    
    if [ $? -eq 0 ]; then
        log_info "集成测试通过"
    else
        log_error "集成测试失败"
        return 1
    fi
    
    cd ..
}

# 运行性能测试
run_performance_tests() {
    log_info "运行性能测试..."
    
    # 启动应用
    docker-compose -f docker-compose.test.yml up -d test-app
    
    log_info "等待应用启动..."
    sleep 60
    
    # 健康检查
    until curl -f http://localhost:8081/actuator/health; do
        log_warn "等待应用启动..."
        sleep 10
    done
    
    # 运行JMeter测试
    docker-compose -f docker-compose.test.yml run --rm jmeter
    
    if [ $? -eq 0 ]; then
        log_info "性能测试完成"
    else
        log_error "性能测试失败"
        return 1
    fi
}

# 运行小程序测试
run_miniprogram_tests() {
    log_info "运行小程序测试..."
    
    cd frontend
    
    # 安装依赖
    npm ci
    
    # 运行测试
    npm run test:miniprogram
    
    if [ $? -eq 0 ]; then
        log_info "小程序测试通过"
    else
        log_error "小程序测试失败"
        return 1
    fi
    
    cd ..
}

# 生成测试报告
generate_reports() {
    log_info "生成测试报告..."
    
    # 创建报告目录
    mkdir -p test-reports
    
    # 复制测试结果
    if [ -d "backend/target/surefire-reports" ]; then
        cp -r backend/target/surefire-reports test-reports/unit-tests
    fi
    
    if [ -d "backend/target/site/jacoco" ]; then
        cp -r backend/target/site/jacoco test-reports/coverage
    fi
    
    if [ -d "test-results" ]; then
        cp -r test-results test-reports/performance
    fi
    
    if [ -d "frontend/test-results" ]; then
        cp -r frontend/test-results test-reports/miniprogram
    fi
    
    log_info "测试报告生成完成，位置: test-reports/"
}

# 清理测试环境
cleanup() {
    log_info "清理测试环境..."
    docker-compose -f docker-compose.test.yml down
    log_info "清理完成"
}

# 主函数
main() {
    local test_type="${1:-all}"
    
    log_info "开始执行测试，类型: $test_type"
    
    # 设置错误处理
    trap cleanup EXIT
    
    check_dependencies
    
    case $test_type in
        "unit")
            start_test_env
            run_unit_tests
            ;;
        "integration")
            start_test_env
            run_integration_tests
            ;;
        "performance")
            start_test_env
            run_performance_tests
            ;;
        "miniprogram")
            run_miniprogram_tests
            ;;
        "all")
            start_test_env
            run_unit_tests
            run_integration_tests
            run_performance_tests
            run_miniprogram_tests
            ;;
        *)
            log_error "未知的测试类型: $test_type"
            log_info "支持的类型: unit, integration, performance, miniprogram, all"
            exit 1
            ;;
    esac
    
    generate_reports
    
    log_info "所有测试执行完成"
}

# 执行主函数
main "$@"
```

### 6.4 测试质量标准

```markdown
## 测试质量标准

### 代码覆盖率要求
- 单元测试覆盖率 >= 80%
- 集成测试覆盖率 >= 60%
- 关键业务逻辑覆盖率 >= 95%

### 性能标准
- API响应时间 < 500ms (95%)
- 数据库查询时间 < 200ms
- 页面加载时间 < 2s
- 并发用户数 >= 1000

### 质量门禁
- 所有单元测试必须通过
- 代码覆盖率达标
- 性能测试通过
- 安全扫描无高危漏洞
- 代码质量评分 >= B

### 测试数据管理
- 使用独立的测试数据库
- 每次测试后清理数据
- 敏感数据脱敏处理
- 测试数据版本控制
```

这个测试策略文档提供了完整的测试方案，包括单元测试、集成测试、性能测试和小程序测试，确保系统质量和稳定性。