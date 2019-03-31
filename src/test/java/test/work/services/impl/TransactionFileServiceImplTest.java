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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import test.work.services.TransactionFileService;
import test.work.services.TransactionImportService;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
				public Comparator<Path> getSortByTimeModified() {
					// Bug jimfs UnsupportedOperationException with Path::toFile
					return Comparator.comparingLong(path -> new File(path.toString()).lastModified());
				}

				@Override
				public Path getArchiveFolder() {
					return TransactionFileServiceImplTest.jimfsArchiveFolder;
				}

				@Override
				public Path getInvalidFolder() {
					return TransactionFileServiceImplTest.jimfsInvalidFolder;
				}

				@Override
				public Path getProcessFolder() {
					return TransactionFileServiceImplTest.jimfsProcessFolder;
				}

				@Override
				public Path getWatchFolder() {
					return TransactionFileServiceImplTest.jimfsWatchFolder;
				}

				@Override
				public Supplier<String> getPrefix() {
					return () -> "";
				}

			};
		}

	}

	private static Path jimfsArchiveFolder;
	private static Path jimfsInvalidFolder;
	private static Path jimfsProcessFolder;
	private static Path jimfsWatchFolder;

	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
	@Mock
	private Appender<ILoggingEvent> mockAppender;
	private FileSystem fileSystem;
	private Logger logger;
	@Value("${folder.archive}")
	private String archiveFolder;
	@Value("${folder.invalid}")
	private String invalidFolder;
	@Value("${folder.process}")
	private String processFolder;
	@Value("${folder.watch}")
	private String watchFolder;
	@Autowired
	private TransactionFileService transactionFileService;
	@MockBean
	private TransactionImportService transactionImportService;

	@BeforeEach
	void setUp() throws IOException {
		logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.addAppender(mockAppender);
		fileSystem = Jimfs.newFileSystem(Configuration.unix());
		jimfsWatchFolder = fileSystem.getPath(watchFolder);
		Files.createDirectory(jimfsWatchFolder);
		jimfsProcessFolder = fileSystem.getPath(processFolder);
		Files.createDirectory(jimfsProcessFolder);
		jimfsArchiveFolder = fileSystem.getPath(archiveFolder);
		Files.createDirectory(jimfsArchiveFolder);
		jimfsInvalidFolder = fileSystem.getPath(invalidFolder);
		Files.createDirectory(jimfsInvalidFolder);
	}

	@AfterEach
	void tearDown() throws IOException {
		logger.detachAppender(mockAppender);
		fileSystem.close();
	}

	private Path copyResourceFile(String filename, Path target) throws IOException {
		Path newPath = target.resolve(filename);
		Files.copy(new ClassPathResource("files/"+filename).getFile().toPath(), newPath);
		return newPath;
	}

	@Test
	void whenReadingFolder_thenFindFile() throws IOException {
		copyResourceFile("img.jpg", jimfsWatchFolder);
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

	@Test
	void whenPathIsNull_thenErrorLogged() {
		jimfsWatchFolder = null;
		((TransactionFileServiceImpl) transactionFileService).getFilesInWatchFolder().get();
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Path cannot be null", loggingEvent.getFormattedMessage());
	}

	@Test
	void whenWatchFolderProcessing_thenFileIsMoved() throws IOException {
		Path source = copyResourceFile("img.jpg", jimfsWatchFolder);
		Path target = jimfsProcessFolder.resolve("img.jpg");
		Path newPath = ((TransactionFileServiceImpl) transactionFileService).getFileProcessing().apply(source);
		assertEquals(target, newPath);
	}

	@Test
	void whenWatchFolderProcessingBadPath_thenExpectNull_andErrorLogged() {
		Path source = jimfsWatchFolder.resolve("img.jpg");
		assertNull(((TransactionFileServiceImpl) transactionFileService).getFileProcessing().apply(source));
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Error occurred while moving file /watch/img.jpg to folder /process", loggingEvent.getFormattedMessage());
	}

	@Test
	void whenWatchFolderProcessingNullPath_thenExpectNull_andErrorLogged() {
		assertNull(((TransactionFileServiceImpl) transactionFileService).getFileProcessing().apply(null));
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Path cannot be null", loggingEvent.getFormattedMessage());
	}

	@Test
	void whenArchivingGoodImport_thenFileIsMovedToArchiveFolder() throws IOException {
		when(transactionImportService.process(any())).thenReturn(true);
		Path source = copyResourceFile("img.jpg", jimfsProcessFolder);
		((TransactionFileServiceImpl) transactionFileService).getFileArchiving().accept(source);
		assertEquals(0, Files.list(jimfsProcessFolder).count());
		assertEquals(1, Files.list(jimfsArchiveFolder).count());
	}

}
