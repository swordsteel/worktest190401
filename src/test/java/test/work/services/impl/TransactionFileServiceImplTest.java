package test.work.services.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import test.work.services.TransactionFileService;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@Getter
@PropertySource("classpath:transaction.properties")
@SpringBootTest
class TransactionFileServiceImplTest {

	@TestConfiguration
	static class TransactionFileServiceImplTestContextConfiguration {

		@Bean
		public TransactionFileService transactionFileService() {
			return new TransactionFileServiceImpl() {

				@Override
				public Path getWatchFolder() {
					return TransactionFileServiceImplTest.jimfsWatchFolder;
				}

			};
		}

	}

	private static Path jimfsWatchFolder;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
	@Mock
	private Appender<ILoggingEvent> mockAppender;
	private FileSystem fileSystem;
	private Logger logger;
	@Value("${folder.watch}")
	private String watchFolder;
	@Autowired
	private TransactionFileService transactionFileService;

	@BeforeEach
	void setUp() throws IOException {
		logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.addAppender(mockAppender);
		fileSystem = Jimfs.newFileSystem(Configuration.unix());
		jimfsWatchFolder = fileSystem.getPath(watchFolder);
		Files.createDirectory(jimfsWatchFolder);
	}

	@AfterEach
	void tearDown() throws IOException {
		logger.detachAppender(mockAppender);
		fileSystem.close();
	}

	@Test
	void whenReadingFolder_thenFindFile() throws IOException {
		Files.copy(new ClassPathResource("files/img.jpg").getFile().toPath(), jimfsWatchFolder.resolve("img.jpg"));
		long noFiles = ((TransactionFileServiceImpl) transactionFileService).getFilesInWatchFolder().get().count();
		assertEquals(1, noFiles);
	}

	@Test
	void whenPathIsBad_thenErrorLogged() {
		jimfsWatchFolder = fileSystem.getPath("/bad-folder/");
		((TransactionFileServiceImpl) transactionFileService).getFilesInWatchFolder().get();
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Error occurred while listing file in: /bad-folder", loggingEvent.getFormattedMessage());
	}

}
