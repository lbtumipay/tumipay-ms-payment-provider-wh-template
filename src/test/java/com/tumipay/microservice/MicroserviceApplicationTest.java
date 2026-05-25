package com.tumipay.microservice;

import com.tumipay.microservice.infrastructure.component.config.MicroserviceBaseApplication;
import com.tumipay.microservice.infrastructure.component.properties.MicroServiceProperties;
import com.tumipay.microservice.infrastructure.component.util.ContextUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * MicroserviceApplicationTest2
 * <p>
 * MicroserviceApplicationTest2 class.
 * <p>
 * <p>
 * THIS COMPONENT WAS BUILT FOLLOWING TUMIPAY'S APPLICATION DEVELOPMENT STANDARDS AND PROCEDURE
 * AND IS PROTECTED BY INTELLECTUAL PROPERTY AND COPYRIGHT LAWS.
 *
 * @author TumiPay SAS.
 * @since 20/04/2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MicroserviceApplication Unit Tests")
class MicroserviceApplicationTest {

    @Mock
    private MicroServiceProperties microServiceProperties;


    @Test
    @DisplayName("constructor - should instantiate without throwing")
    void constructor_shouldInstantiateWithoutThrowing() {
        assertDoesNotThrow(() -> new MicroserviceApplication(microServiceProperties));
    }
    @Test
    @DisplayName("constructor - should extend MicroserviceBaseApplication")
    void constructor_shouldExtendMicroserviceBaseApplication() {
        MicroserviceApplication app = new MicroserviceApplication(microServiceProperties);
        assertInstanceOf(MicroserviceBaseApplication.class, app);
    }

    @Test
    @DisplayName("main - should call enableAutomaticContextPropagation before SpringApplication.run")
    void main_shouldCallEnableContextPropagationBeforeRun() {
        try (MockedStatic<ContextUtils> contextUtils = mockStatic(ContextUtils.class);
             MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                .thenReturn(null);
            MicroserviceApplication.main(new String[]{});
            contextUtils.verify(ContextUtils::enableAutomaticContextPropagation);
            springApp.verify(() -> SpringApplication.run(
                eq(MicroserviceApplication.class), any(String[].class)));
        }
    }

    @Test
    @DisplayName("main - should invoke SpringApplication.run with MicroserviceApplication class")
    void main_shouldInvokeSpringApplicationRunWithCorrectClass() {
        try (MockedStatic<ContextUtils> contextUtils = mockStatic(ContextUtils.class);
             MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                .thenReturn(null);
            String[] args = {"--spring.profiles.active=dev"};
            MicroserviceApplication.main(args);
            springApp.verify(() -> SpringApplication.run(MicroserviceApplication.class, args));
        }
    }

    @Test
    @DisplayName("main - should call enableAutomaticContextPropagation exactly once")
    void main_shouldCallEnableContextPropagationExactlyOnce() {
        try (MockedStatic<ContextUtils> contextUtils = mockStatic(ContextUtils.class);
             MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                .thenReturn(null);
            MicroserviceApplication.main(new String[]{});
            contextUtils.verify(ContextUtils::enableAutomaticContextPropagation, times(1));
        }
    }

    @Test
    @DisplayName("main - should accept empty args array without throwing")
    void main_shouldAcceptEmptyArgsArrayWithoutThrowing() {
        try (MockedStatic<ContextUtils> contextUtils = mockStatic(ContextUtils.class);
             MockedStatic<SpringApplication> springApp = mockStatic(SpringApplication.class)) {
            springApp.when(() -> SpringApplication.run(any(Class.class), any(String[].class)))
                .thenReturn(null);
            assertDoesNotThrow(() -> MicroserviceApplication.main(new String[]{}));
        }
    }

    @Test
    @DisplayName("class - should be annotated with @SpringBootApplication")
    void class_shouldBeAnnotatedWithSpringBootApplication() {
        assertNotNull(
            MicroserviceApplication.class.getAnnotation(SpringBootApplication.class),
            "@SpringBootApplication must be present"
        );
    }

    @Test
    @DisplayName("class - @SpringBootApplication scanBasePackages should include com.tumipay.microservice")
    void class_springBootApplicationScanBasePackagesShouldIncludeRoot() {
        SpringBootApplication annotation =
            MicroserviceApplication.class.getAnnotation(SpringBootApplication.class);
        assertNotNull(annotation);
        assertArrayEquals(
            new String[]{"com.tumipay.microservice"},
            annotation.scanBasePackages()
        );
    }

    @Test
    @DisplayName("class - should be annotated with @EnableScheduling")
    void class_shouldBeAnnotatedWithEnableScheduling() {
        assertNotNull(
            MicroserviceApplication.class.getAnnotation(EnableScheduling.class),
            "@EnableScheduling must be present"
        );
    }

    @Test
    @DisplayName("class - should be annotated with @ConfigurationPropertiesScan")
    void class_shouldBeAnnotatedWithConfigurationPropertiesScan() {
        assertNotNull(
            MicroserviceApplication.class.getAnnotation(ConfigurationPropertiesScan.class),
            "@ConfigurationPropertiesScan must be present"
        );
    }

    @Test
    @DisplayName("class - @ConfigurationPropertiesScan should target the properties package")
    void class_configurationPropertiesScanShouldTargetPropertiesPackage() {
        ConfigurationPropertiesScan annotation =
            MicroserviceApplication.class.getAnnotation(ConfigurationPropertiesScan.class);
        assertNotNull(annotation);
        var scannedPackages = java.util.Arrays.asList(annotation.value());
        assertTrue(
            scannedPackages.contains("com.tumipay.microservice.infrastructure.component.properties"),
            "com.tumipay.microservice.infrastructure.component.properties must be scanned"
        );
    }

    @Test
    @DisplayName("onApplicationEvent - should not throw when context refresh event is received")
    void onApplicationEvent_shouldNotThrowOnContextRefreshEvent() {
        MicroserviceApplication app = new MicroserviceApplication(microServiceProperties);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
        assertDoesNotThrow(() -> app.onApplicationEvent(event));
    }

    @Test
    @DisplayName("onApplicationEvent - should invoke microservice properties getters for logging")
    void onApplicationEvent_shouldInvokeMicroservicePropertyGetters() {
        MicroserviceApplication app = new MicroserviceApplication(microServiceProperties);
        ContextRefreshedEvent event = mock(ContextRefreshedEvent.class);
        app.onApplicationEvent(event);
        verify(microServiceProperties, atLeastOnce()).getName();
        verify(microServiceProperties, atLeastOnce()).getPort();
        verify(microServiceProperties, atLeastOnce()).getVersion();
        verify(microServiceProperties, atLeastOnce()).getContextPath();
        verify(microServiceProperties, atLeastOnce()).getLogsPath();
        verify(microServiceProperties, atLeastOnce()).getSpringVersion();
    }
}