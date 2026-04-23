# 🔧 Implementação Prática - Exemplo Completo

## 1. Setup Inicial do Projeto

### `pom.xml` - Dependências Maven

```xml
<project>
  <properties>
    <java.version>21</java.version>
    <spring-boot.version>3.4.0</spring-boot.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Spring Boot Web -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Spring Boot Actuator (Micrometer incl.) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>

    <!-- Micrometer Prometheus Registry -->
    <dependency>
      <groupId>io.micrometer</groupId>
      <artifactId>micrometer-registry-prometheus</artifactId>
    </dependency>

    <!-- JPA + Database (exemplo) -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>42.7.1</version>
      <scope>runtime</scope>
    </dependency>

    <!-- Cache -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>
    <dependency>
      <groupId>com.github.ben-manes.caffeine</groupId>
      <artifactId>caffeine</artifactId>
    </dependency>

    <!-- Logging -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-logging</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

### `build.gradle` - Dependências Gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.0'
    id 'io.spring.dependency-management' version '1.1.4'
}

java {
    sourceCompatibility = '21'
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Web
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // Actuator + Micrometer
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'io.micrometer:micrometer-registry-prometheus'
    
    // Data
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'org.postgresql:postgresql:42.7.1'
    
    // Cache
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## 2. Configuração (application.yaml)

```yaml
spring:
  application:
    name: benefix-api
  
  # JPA/Database
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
          fetch_size: 50
        order_inserts: true
        order_updates: true
  
  datasource:
    url: jdbc:postgresql://localhost:5432/benefix
    username: postgres
    password: password
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 20000
  
  # Cache
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=500,expireAfterWrite=10m

server:
  port: 8080
  servlet:
    context-path: /

# Management (Actuator + Metrics)
management:
  server:
    port: 8081  # Porta separada para metrics (recomendado)
    
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
    
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
    metrics:
      enabled: true
    prometheus:
      enabled: true
  
  # Health checks
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
    diskspace:
      enabled: true
      threshold: 1GB
    db:
      enabled: true
  
  # Metrics Configuration
  metrics:
    enable:
      all: true
      # Desabilitar se necessário:
      # jvm.memory.max: false
    
    distribution:
      # Configurar buckets para HTTP requests (em segundos)
      percentiles-histogram:
        http.server.requests: true
      slo:
        http.server.requests: 0.050,0.1,0.5,1.0,2.0,5.0
      buckets:
        http.server.requests: 0.001,0.005,0.01,0.05,0.1,0.5,1.0,2.0,5.0,10.0
      minimum-expected-value:
        http.server.requests: 0.001
      maximum-expected-value:
        http.server.requests: 10
    
    tags:
      # Tags padrão em todas as métricas
      application: ${spring.application.name}
      environment: ${APP_ENV:development}
      version: ${APP_VERSION:1.0.0}
      region: ${APP_REGION:us-east-1}
    
    # Export para Prometheus
    export:
      prometheus:
        enabled: true
        step: 1m
        descriptions: true
        histogram-flavor: prometheus

# Logging
logging:
  level:
    root: INFO
    org.springframework.web: INFO
    org.springframework.security: INFO
    org.hibernate: WARN
    com.benefix: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

# Aplicação
app:
  env: development
  version: 1.0.0
  region: us-east-1
```

---

## 3. Estrutura de Código

### `src/main/java/com/benefix/api/config/MetricsConfig.java`

```java
package com.benefix.api.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class MetricsConfig {
    
    /**
     * Habilita @Timed em methods
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }
}
```

### `src/main/java/com/benefix/api/service/BenefitService.java`

```java
package com.benefix.api.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class BenefitService {
    
    private final BenefitRepository benefitRepository;
    private final MeterRegistry meterRegistry;
    
    // Métricas customizadas
    private final Counter benefitsCreatedCounter;
    private final Counter benefitsDeletedCounter;
    private final Timer benefitsRetrievalTimer;
    
    public BenefitService(BenefitRepository benefitRepository, 
                         MeterRegistry meterRegistry) {
        this.benefitRepository = benefitRepository;
        this.meterRegistry = meterRegistry;
        
        // Registrar counters
        this.benefitsCreatedCounter = Counter.builder("benefits.created")
            .description("Total de benefícios criados")
            .tag("service", "benefit-service")
            .register(meterRegistry);
        
        this.benefitsDeletedCounter = Counter.builder("benefits.deleted")
            .description("Total de benefícios deletados")
            .tag("service", "benefit-service")
            .register(meterRegistry);
        
        // Registrar timer
        this.benefitsRetrievalTimer = Timer.builder("benefits.retrieval")
            .description("Tempo para buscar benefícios")
            .tag("service", "benefit-service")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
    }
    
    /**
     * Usando @Timed para instrumentação automática
     */
    @Timed(value = "benefits.create.time", 
            description = "Tempo para criar benefício")
    public Benefit createBenefit(BenefitRequest request) {
        Benefit benefit = new Benefit(request);
        Benefit saved = benefitRepository.save(benefit);
        
        benefitsCreatedCounter.increment();
        
        return saved;
    }
    
    /**
     * Usando Timer.record() para controle manual
     */
    @Cacheable(value = "benefits", key = "#id")
    public Benefit getBenefitById(Long id) {
        return benefitsRetrievalTimer.recordCallable(() ->
            benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException("ID: " + id))
        );
    }
    
    /**
     * Usando Timer.wrap() para Supplier
     */
    public List<Benefit> getAllBenefits() {
        return benefitsRetrievalTimer.wrap(() ->
            benefitRepository.findAll()
        ).get();
    }
    
    @Timed(value = "benefits.delete.time")
    public void deleteBenefit(Long id) {
        benefitRepository.deleteById(id);
        benefitsDeletedCounter.increment();
    }
    
    /**
     * Gauge para monitorar estado em tempo real
     */
    public void setupGauges() {
        // Registrar gauge para total de benefícios no DB
        meterRegistry.gauge("benefits.total",
            () -> benefitRepository.count(),
            meter -> meter.tag("service", "benefit-service")
        );
        
        // Registrar gauge para benefícios ativos
        meterRegistry.gauge("benefits.active",
            () -> benefitRepository.countActive(),
            meter -> meter.tag("status", "active")
        );
    }
}
```

### `src/main/java/com/benefix/api/controller/BenefitController.java`

```java
package com.benefix.api.controller;

import com.benefix.api.service.BenefitService;
import io.micrometer.core.annotation.Timed;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/benefits")
public class BenefitController {
    
    private final BenefitService benefitService;
    
    public BenefitController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }
    
    @PostMapping
    @Timed(value = "benefits.http.create", 
            description = "HTTP POST para criar benefício")
    public ResponseEntity<BenefitResponse> create(@RequestBody BenefitRequest request) {
        Benefit created = benefitService.createBenefit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BenefitResponse.from(created));
    }
    
    @GetMapping("/{id}")
    @Timed(value = "benefits.http.get", 
            description = "HTTP GET para buscar benefício")
    public ResponseEntity<BenefitResponse> getById(@PathVariable Long id) {
        Benefit benefit = benefitService.getBenefitById(id);
        return ResponseEntity.ok(BenefitResponse.from(benefit));
    }
    
    @GetMapping
    @Timed(value = "benefits.http.list", 
            description = "HTTP GET para listar benefícios")
    public ResponseEntity<List<BenefitResponse>> getAll() {
        List<Benefit> benefits = benefitService.getAllBenefits();
        return ResponseEntity.ok(
            benefits.stream()
                .map(BenefitResponse::from)
                .toList()
        );
    }
    
    @DeleteMapping("/{id}")
    @Timed(value = "benefits.http.delete", 
            description = "HTTP DELETE para deletar benefício")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        benefitService.deleteBenefit(id);
        return ResponseEntity.noContent().build();
    }
}
```

### `src/main/java/com/benefix/api/controller/HealthController.java`

```java
package com.benefix.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }
    
    public record HealthResponse(String status) {}
}
```

---

## 4. Docker Compose para Prometheus

### `docker-compose.yml`

```yaml
version: '3.8'

services:
  # PostgreSQL
  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: benefix
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    depends_on:
      - postgres

  # Grafana (opcional)
  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
      GF_SECURITY_ADMIN_USER: admin
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus

volumes:
  postgres_data:
  prometheus_data:
  grafana_data:
```

### `prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'local'
    environment: 'development'

alerting:
  alertmanagers:
    - static_configs:
        - targets: []

rule_files:
  - 'alerts.yml'

scrape_configs:
  - job_name: 'spring-boot'
    static_configs:
      - targets: ['localhost:8081']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    scrape_timeout: 10s
```

### `alerts.yml`

```yaml
groups:
  - name: spring-boot-alerts
    interval: 30s
    rules:
      - alert: HighErrorRate
        expr: |
          (sum(rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[5m])) /
           sum(rate(http.server.requests_seconds_count[5m]))) > 0.05
        for: 2m
        annotations:
          summary: "Taxa de erro > 5%"
          
      - alert: HighMemoryUsage
        expr: |
          (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) > 0.9
        for: 5m
        annotations:
          summary: "Memória heap > 90%"
```

---

## 5. Executar e Testar

### Iniciar tudo

```bash
# Terminal 1: Docker Compose
docker-compose up -d

# Terminal 2: Aplicação Spring Boot
mvn spring-boot:run
# ou
./gradlew bootRun
```

### Verificar endpoints

```bash
# Health
curl http://localhost:8080/health

# Metrics list
curl http://localhost:8081/actuator/metrics

# Prometheus format
curl http://localhost:8081/actuator/prometheus

# Prometheus UI
open http://localhost:9090

# Grafana UI
open http://localhost:3000
# Login: admin / admin
```

### Query de exemplo no Prometheus

```promql
# Taxa de requisições
rate(http.server.requests_seconds_count[1m])

# Benefícios criados
rate(benefits.created_total[5m])

# P95 latência
histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m]))
```

---

## 6. Testing (JUnit + Micrometer)

### `src/test/java/com/benefix/api/service/BenefitServiceTest.java`

```java
package com.benefix.api.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BenefitServiceTest {
    
    private BenefitService benefitService;
    private MeterRegistry meterRegistry;
    private BenefitRepository benefitRepository;
    
    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        benefitRepository = mock(BenefitRepository.class);
        benefitService = new BenefitService(benefitRepository, meterRegistry);
    }
    
    @Test
    void testBenefitCreationIncrementsCounter() {
        // Arrange
        BenefitRequest request = new BenefitRequest("Test");
        Benefit benefit = new Benefit(request);
        when(benefitRepository.save(any())).thenReturn(benefit);
        
        // Act
        benefitService.createBenefit(request);
        
        // Assert
        assertEquals(1.0, 
            meterRegistry.counter("benefits.created").count()
        );
    }
    
    @Test
    void testBenefitRetrievalTimerRecordsTime() {
        // Arrange
        Benefit benefit = new Benefit("Test");
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        
        // Act
        benefitService.getBenefitById(1L);
        
        // Assert
        assertNotNull(
            meterRegistry.find("benefits.retrieval").timer()
        );
        assertEquals(1, 
            meterRegistry.find("benefits.retrieval").timer().count()
        );
    }
}
```

---

## 7. CI/CD com GitHub Actions

### `.github/workflows/metrics.yml`

```yaml
name: Build and Deploy

on: [push, pull_request]

jobs:
  build:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: benefix_test
          POSTGRES_PASSWORD: password
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Java
        uses: actions/setup-java@v3
        with:
          java-version: 21
          distribution: 'temurin'
      
      - name: Build
        run: mvn clean package
      
      - name: Test
        run: mvn test
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/benefix_test
      
      - name: Archive metrics
        if: always()
        uses: actions/upload-artifact@v3
        with:
          name: metrics
          path: target/
```

---

## 8. Checklist de Implementação

- [ ] Dependências Maven/Gradle adicionadas
- [ ] `application.yaml` configurado
- [ ] `/actuator/prometheus` retornando métricas
- [ ] Custom metrics criadas (`BenefitService`)
- [ ] Controller instrumentado com `@Timed`
- [ ] Docker Compose funcionando
- [ ] Prometheus scrapeando dados
- [ ] Alertas configurados
- [ ] Testes de métrica passando
- [ ] CI/CD pipeline rodando

