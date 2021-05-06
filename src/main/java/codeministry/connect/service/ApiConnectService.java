package codeministry.connect.service;

import codeministry.connect.dto.Comment;
import codeministry.connect.dto.Content;
import codeministry.connect.dto.TimeEntry;
import codeministry.connect.dto.Type;
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiConnectService {
    @Value("${application.settings.backup.path}")
    private String backupDirectory;

    private Resource importCsvResource;

    private final JiraService jiraService;
    private final ResourceLoader resourceLoader;

    public void importTimeEntries() throws IOException, CsvException {
        this.importCsvResource = resourceLoader.getResource("classpath:csv/time-entries.csv");

        final CSVReader reader = createCsvReader();
        List<String[]> rows = reader.readAll();

        int sum = 0;

        for (String[] row : rows) {
            long durationInSeconds = this.createTimeEntryDTO(row);
            if (durationInSeconds > 0) {
                sum += durationInSeconds;
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        BigDecimal roundedDuration = BigDecimal.valueOf(sum)
                .divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(60), RoundingMode.HALF_UP);

        log.info("Duration: {} h", roundedDuration);

        makeBackup();
    }

    private long createTimeEntryDTO(final String[] row) {
        String description = row[5].trim();
        if (description.isBlank() || !description.startsWith("STC-")) {
            return 0;
        }
        String issueId = description.substring(0, description.indexOf(" "));
        issueId = issueId.replace(":", "");

        String date = row[7].trim();
        String time = row[8].trim();
        String dateTime = date.concat(" ").concat(time);
        dateTime = this.convertDate(dateTime);

        String durationRaw = row[11].trim();
        Duration duration = Duration.between(LocalTime.MIN, LocalTime.parse(durationRaw));
        if (duration.getSeconds() <= 0) {
            return 0;
        }

        TimeEntry timeEntry = this.createTimeEntry(description, dateTime, duration);

        jiraService.addTimeEntry(issueId, timeEntry);

        log.info("{} : spent {}", issueId, duration);

        return duration.toSeconds();
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

    private TimeEntry createTimeEntry(final String description, final String dateTime, final Duration duration) {
        Content text = Content.builder()
                .type(Type.text)
                .text(description)
                .build();

        Content paragraph = Content.builder()
                .type(Type.paragraph)
                .content(List.of(text))
                .build();

        Comment comment = Comment.builder()
                .content(List.of(paragraph))
                .build();

        return TimeEntry.builder()
                .comment(comment)
                .started(dateTime)
                .timeSpentSeconds(duration.toSeconds())
                .build();
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

    private String convertDate(final String rawDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withLocale(Locale.GERMANY)
                .withZone(ZoneId.systemDefault());


        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .withLocale(Locale.GERMANY)
                .withZone(ZoneId.systemDefault());

        return ZonedDateTime
                .parse(rawDate, inputFormatter)
                .format(outputFormatter);
    }
}
