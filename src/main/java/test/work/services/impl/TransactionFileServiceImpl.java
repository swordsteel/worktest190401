package test.work.services.impl;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import test.work.services.TransactionFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
@PropertySource("classpath:transaction.properties")
@Service
public class TransactionFileServiceImpl implements TransactionFileService {

	protected static final Logger LOGGER = LoggerFactory.getLogger(TransactionFileServiceImpl.class);

	private Path processFolder;
	private Path watchFolder;

	@Value("${folder.process}")
	private void setProcessFolder(String path) {
		processFolder = Paths.get(path);
	}

	@Value("${folder.watch}")
	private void setWatchFolder(String path) {
		watchFolder = Paths.get(path);
	}

	private Supplier<String> prefix = defaultPrefix;

	private Function<Path, Path> fileProcessing = source -> {
		try {
			return Files.move(source, getProcessTargetPath().apply(source));
		}
		catch(IOException e) {
			return null;
		}
	};

	private Function<Path, Path> processTargetPath = source -> getProcessFolder()
			.resolve(getPrefix().get() + source.getFileName().toString());

	private Supplier<Stream<Path>> filesInWatchFolder = () -> {
		try {
			return Files.list(getWatchFolder());
		}
		catch(IOException e) {
			LOGGER.error("Error occurred while listing file in: {}", getWatchFolder(), e);
		}
		catch(NullPointerException e) {
			LOGGER.error("Path cannot be null", e);
		}
		return Stream.of();
	};

	@Override
	public void scanFolder() {
	}

}
