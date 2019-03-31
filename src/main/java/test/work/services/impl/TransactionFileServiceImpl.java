package test.work.services.impl;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import test.work.services.TransactionFileService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Getter
@PropertySource("classpath:transaction.properties")
@Service
public class TransactionFileServiceImpl implements TransactionFileService {

	private Path watchFolder;

	@Value("${folder.watch}")
	private void setWatchFolder(String path) {
		watchFolder = Paths.get(path);
	}

	private Supplier<Stream<Path>> filesInWatchFolder = () -> {
		try {
			return Files.list(getWatchFolder());
		}
		catch(IOException e) {
			return Stream.of();
		}
	};

	@Override
	public void scanFolder() {
	}

}