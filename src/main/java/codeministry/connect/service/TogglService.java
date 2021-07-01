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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TogglService {
    @Value("${toggl-to-jira.settings..active}")
    private boolean active;

    @Value("${toggl-to-jira.settings.prefix}")
    private String descriptionPrefix;

    @Value("${toggl-to-jira.settings.skip-lines}")
    private int skipLines;

    @Value("${toggl-to-jira.settings.rate}")
    private int rateLimiting;

    @Value("${toggl-to-jira.settings.backup.active}")
    private boolean makeBackup;

    @Value("${toggl-to-jira.settings.backup.path}")
    private String backupDirectory;

    private Resource importCsvResource;

    private final JiraService jiraService;
    private final ResourceLoader resourceLoader;

    public void importTimeEntries() throws IOException, CsvException {
        this.splash();

        Duration duration = this.importFromCsv();
        log.info("Duration: {} h {} m", duration.toHours(), duration.toMinutesPart());

        File backup = this.makeBackup();
        if (backup == null) {
            log.warn("No Backup created");
            return;
        }

        log.info("Backup: {}", backup.getAbsolutePath());
    }

    private Duration importFromCsv() throws IOException, CsvException {
        this.importCsvResource = resourceLoader.getResource("classpath:csv/time-entries.csv");
        final CSVReader reader = createCsvReader();
        List<String[]> rows = reader.readAll();

        List<Duration> durations = rows.stream()
                .map(this::createTimeEntry)
                .collect(Collectors.toList());

        Duration sum = Duration.ZERO;
        for (Duration duration : durations) {
            sum = sum.plus(duration);
        }

        return sum;
    }

    private Duration createTimeEntry(final String[] row) {
        String description = row[5].trim();
        if (description.isBlank()) {
            return Duration.ZERO;
        }

        Duration duration = this.extractDuration(row[11]);
        if (duration.getSeconds() <= 0) {
            return Duration.ZERO;
        }

        if (description.startsWith(descriptionPrefix)) {
            this.createTimeEntry(row, description, duration);
        }

        return duration;
    }

    private void createTimeEntry(final String[] row, final String description, final Duration duration) {
        String issueId = this.extractIssueId(description);
        String started = this.extractStartedDate(row);

        TimeEntry timeEntry = this.mapTimeEntry(description, started, duration);
        log.info("Issue {}: {} h {} m | {}", issueId, duration.toHoursPart(), duration.toMinutesPart(), row[11]);
        if (!active) {
            return;
        }

        jiraService.createTimeEntry(issueId, timeEntry);

        try {
            // Some kind of "rate limiting"
            Thread.sleep(rateLimiting);
        } catch (Exception ignore) {
        }
    }

    private Duration extractDuration(final String duration) {
        String durationRaw = duration.trim();

        return Duration.between(LocalTime.MIN, LocalTime.parse(durationRaw));
    }

    private String extractStartedDate(final String[] row) {
        String date = row[7].trim();
        String time = row[8].trim();
        String started = date.concat(" ").concat(time);
        started = this.convertDate(started);

        return started;
    }

    private String extractIssueId(final String description) {
        String issueId = description.substring(0, description.indexOf(" "));

        return issueId.replace(":", "");
    }

    private TimeEntry mapTimeEntry(final String description, final String started, final Duration duration) {
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
                .started(started)
                .timeSpentSeconds(duration.toSeconds())
                .build();
    }

    private File makeBackup() throws IOException {
        if (!makeBackup) {
            return null;
        }

        String filePrefix = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withLocale(Locale.GERMANY)
                .withZone(ZoneId.systemDefault())
                .format(Instant.now());

        Path backupPath = Paths.get(this.backupDirectory
                .concat("/")
                .concat(filePrefix)
                .concat(".csv"));

        Path originalPath = this.importCsvResource.getFile().toPath();

        Path movedPath = Files.move(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        return movedPath.toFile();
    }

    private CSVReader createCsvReader() throws IOException {
        final CSVParser parser = new CSVParserBuilder()
                .withSeparator(',')
                .withIgnoreQuotations(false)
                .build();

        return new CSVReaderBuilder(new InputStreamReader(importCsvResource.getInputStream()))
                .withSkipLines(skipLines)
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

    private void splash() {
        log.info("");
        log.info("######################################################################");
        log.info("Dry Run? => {}", !active);
        log.info("Make backup? => {}", makeBackup);
        log.info("######################################################################");
        log.info("");
    }
}
