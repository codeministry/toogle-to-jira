package codeministry.mococonnect.service;

import codeministry.mococonnect.client.MocoClient;
import codeministry.mococonnect.dto.MocoTimeEntryDTO;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class MocoConnectService {
    @Value("${application.settings.backup.path}")
    private String backupDirectory;

    private Resource importCsvResource;

    private final ResourceLoader resourceLoader;
    private final MocoClient mocoClient;

    @PostConstruct
    public void setUp() throws IOException, CsvException {
        this.importCsvResource = resourceLoader.getResource("classpath:csv/time-entries.csv");

        importTimeEntries();
    }

    private void importTimeEntries() throws IOException, CsvException {
        final CSVReader reader = createCsvReader();
        List<String[]> rows = reader.readAll();

        Double sum = 0.0;

        for (String[] row : rows) {
            MocoTimeEntryDTO mocoTimeEntryDTO = createMocoTimeEntryDTO(row);
            if (mocoTimeEntryDTO != null) {
                log.info(mocoTimeEntryDTO.toString());
                sum += mocoTimeEntryDTO.getHours();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BigDecimal roundedDuration = BigDecimal.valueOf(sum).setScale(2, RoundingMode.HALF_UP);
        log.info("Duration: {} h", roundedDuration);

        makeBackup();
    }

    private void makeBackup() throws IOException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")
                .withLocale(Locale.GERMANY)
                .withZone(ZoneId.systemDefault());
        String filePrefix = formatter.format(Instant.now());

        Path backupPath = Paths.get(this.backupDirectory + "/" + filePrefix + ".csv");
        Path originalPath = this.importCsvResource.getFile().toPath();

        Files.move(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
    }

    private MocoTimeEntryDTO createMocoTimeEntryDTO(final String[] row) {
        String description = row[5].trim();
        String date = row[7].trim();
        String durationRaw = row[11].trim();

        Duration duration = Duration.between(LocalTime.MIN, LocalTime.parse(durationRaw));
        if (duration.getSeconds() <= 0) {
            return null;
        }

        BigDecimal roundedDuration = BigDecimal.valueOf((double) duration.toMinutes() / 60).setScale(2, RoundingMode.HALF_UP);

        MocoTimeEntryDTO mocoTimeEntryDTO = MocoTimeEntryDTO.builder()
                .date(date)
                .description(description)
                .hours(roundedDuration.doubleValue())
                .build();

        mocoClient.createTimeEntry(mocoTimeEntryDTO);

        return mocoTimeEntryDTO;
    }

    private CSVReader createCsvReader() throws IOException {
        final CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(true)
                .build();

        return new CSVReaderBuilder(new InputStreamReader(importCsvResource.getInputStream()))
                .withSkipLines(1)
                .withCSVParser(parser)
                .build();
    }
}
