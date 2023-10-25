import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.beans.factory.config.BeanDefinition;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;

@Configuration
@ComponentScan(basePackages = "your.base.package") // Replace with your application's base package
public class LogbackConfig {

	// Define the controller log level. Default is INFO if not specified in configuration.
	@Value("${logLevel.controller:INFO}")
	private String controllerLogLevel;

	// Create a Spring bean for managing controller classes and their log levels.
	@Bean
	public LogControllerClasses logControllerClasses() {
		return new LogControllerClasses();
	}

	// Scan for controller classes in the specified base package.
	@Bean
	public Set<Class<?>> scanControllerClasses() {
		// Create a component scanner with a custom filter for classes ending with "Controller."
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new ControllerTypeFilter());

		// Create a set to store discovered controller classes.
		Set<Class<?>> controllerClasses = new HashSet<>();

		// Iterate through discovered bean definitions and collect the corresponding classes.
		for (BeanDefinition beanDefinition : scanner.findCandidateComponents("your.base.package")) {
			try {
				Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
				controllerClasses.add(clazz);
			} catch (ClassNotFoundException e) {
				// Handle the exception if needed.
			}
		}

		return controllerClasses;
	}

	// Configure log levels for controllers and the rest of the application.
	@Bean
	public Logger logger() {
		// Obtain the LogControllerClasses bean for managing controller log levels.
		LogControllerClasses logControllerClasses = logControllerClasses();

		// Scan for controller classes.
		Set<Class<?>> controllerClasses = scanControllerClasses();

		// Set log levels for controller classes to the specified controllerLogLevel.
		for (Class<?> controllerClass : controllerClasses) {
			logControllerClasses.setLoggerLevel(controllerClass, controllerLogLevel);
		}

		// Set the log level to ERROR for the rest of the application.
		Logger rootLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		ch.qos.logback.classic.Logger classicRootLogger = (ch.qos.logback.classic.Logger) rootLogger;
		classicRootLogger.setLevel(ch.qos.logback.classic.Level.ERROR);

		// Return a logger for this configuration class (you can replace with your desired logger).
		return LoggerFactory.getLogger(this.getClass());
	}

	// Custom filter to identify classes ending with "Controller."
	private static class ControllerTypeFilter implements TypeFilter {

		@Override
		public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
			String className = metadataReader.getClassMetadata().getClassName();
			return className.endsWith("Controller");
		}
	}

	// Helper class for managing controller log levels.
	@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
	private class LogControllerClasses {
		private Set<ch.qos.logback.classic.Logger> controllers = new HashSet<>();

		public void setLoggerLevel(Class<?> controllerClass, String level) {
			Logger logger = LoggerFactory.getLogger(controllerClass);
			if (logger instanceof ch.qos.logback.classic.Logger) {
				ch.qos.logback.classic.Logger classicLogger = (ch.qos.logback.classic.Logger) logger;
				classicLogger.setLevel(ch.qos.logback.classic.Level.toLevel(level));
				controllers.add(classicLogger);
			}
		}
	}
}
