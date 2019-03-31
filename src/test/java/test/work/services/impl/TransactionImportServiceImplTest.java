package test.work.services.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import lombok.Getter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import test.work.entities.Batch;
import test.work.repositories.BatchRepository;
import test.work.services.TransactionImportService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Getter
@SpringBootTest
@Transactional(propagation = Propagation.REQUIRED)
class TransactionImportServiceImplTest {

	@TestConfiguration
	static class TransactionImportServiceImplTestContextConfiguration {

		@Bean
		public TransactionImportService transactionImportService() {
			return new TransactionImportServiceImpl() {
			};
		}

	}

	@Mock
	private Appender<ILoggingEvent> mockAppender;
	@Captor
	private ArgumentCaptor<LoggingEvent> captorLoggingEvent;
	@MockBean
	private BatchRepository batchRepository;
	private Logger logger;
	@Autowired
	private TransactionImportService transactionImportService;

	@BeforeEach
	void setUp() throws Exception {
		logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		logger.addAppender(mockAppender);
	}

	@AfterEach
	void tearDown() throws Exception {
		logger.detachAppender(mockAppender);
	}

	@Test
	void whenImportingFile_thenExpectTrue() throws IOException {
		Path processFile = new ClassPathResource("files/tiny.csv").getFile().toPath();
		assertTrue(getTransactionImportService().process(processFile));
	}

	@Test
	void whenPathIsBad_thenExpectFalse_andErrorIsLogged() throws IOException {
		Path processFile = Paths.get("files/bad_file.csv");
		assertFalse(getTransactionImportService().process(processFile));

		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Error occurred while loading file: bad_file.csv", loggingEvent.getFormattedMessage());
	}

	@Test
	void whenPathIsNull_thenExpectFalse_andErrorIsLogged() throws IOException {
		Path processFile = null;
		assertFalse(getTransactionImportService().process(processFile));

		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Import path cannot be null", loggingEvent.getFormattedMessage());
	}

	@Test
	void whenFileProcess_thenBatchIsCreatedAndSaved() throws IOException {
		Path processFile = new ClassPathResource("files/tiny.csv").getFile().toPath();
		getTransactionImportService().process(processFile);
		verify(batchRepository, atLeastOnce()).save(any(Batch.class));
	}

	@Test
	void whenFileAlreadyProcessed_thenErrorIsLogged() throws IOException {
		when(batchRepository.findByNK(any())).thenReturn(Optional.of(new Batch()));
		Path processFile = new ClassPathResource("files/tiny.csv").getFile().toPath();
		getTransactionImportService().process(processFile);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("File by name tiny.csv are already processed", loggingEvent.getFormattedMessage());
	}

	@Test
	void whenFileIsNotCSV_thenErrorIsLogged() throws IOException {
		Path processFile = new ClassPathResource("files/img.jpg").getFile().toPath();
		getTransactionImportService().process(processFile);
		verify(mockAppender).doAppend(captorLoggingEvent.capture());
		final LoggingEvent loggingEvent = captorLoggingEvent.getValue();
		assertEquals(loggingEvent.getLevel(), Level.ERROR);
		assertEquals("Not a valid CSV format in file: img.jpg", loggingEvent.getFormattedMessage());
	}

}
