package ru.dzimin;

import ru.dzimin.exception.ValidationException;
import ru.dzimin.statistic.FileStatistics;
import ru.dzimin.statistic.Statistics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static ru.dzimin.util.Constant.*;
import static ru.dzimin.util.FileUtils.getExtensionByFileName;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    private static final HashMap<String, String> commands = new HashMap<>();

    private static final Set<String> includeSet = new HashSet<>();
    private static final Set<String> excludeSet = new HashSet<>();
    private static final Set<String> gitignoreSet = new HashSet<>();

    private static final Statistics statistics = new Statistics();

    public static void main(String[] args) {

        if (args.length == 0) {
            logger.severe("Не указан путь до каталога!");
            return;
        }

        final Path basePath = Path.of(args[0]);

        if (Files.notExists(basePath) || !Files.isDirectory(basePath)) {
            logger.severe("Указанный каталог не найден!");
            return;
        }

        try {
            initCommands(args, basePath);
            visitFiles(basePath);
        } catch (RuntimeException exception) {
            logger.severe(exception.getMessage());
            return;
        }

        printStatistics(commands.getOrDefault(OUTPUT_KEY, "plain"));
    }

    private static void initCommands(String[] args, Path basePath) {
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                continue;
            }

            String[] split = arg.split(EQUALS_SEPARATOR);
            commands.put(split[0], split.length > 1 ? split[1] : STUB);
        }

        checkValueIsNotStub(MAX_DEPTH_KEY, THREAD_KEY, INCLUDE_EXT_KEY, EXCLUDE_EXT_KEY, OUTPUT_KEY);

        if (commands.containsKey(INCLUDE_EXT_KEY)) {
            includeSet.addAll(Arrays.asList(commands.get(INCLUDE_EXT_KEY).split(COMMA_SEPARATOR)));
        }
        if (commands.containsKey(EXCLUDE_EXT_KEY)) {
            excludeSet.addAll(Arrays.asList(commands.get(EXCLUDE_EXT_KEY).split(COMMA_SEPARATOR)));
            includeSet.removeAll(excludeSet);
        }

        excludeSet.add("gitignore");

        if (commands.containsKey(GIT_IGNORE_KEY)) {
            Path gitignorePath = Path.of(basePath.toAbsolutePath().toString(), GITIGNORE_FILE_NAME);
            try {
                gitignoreSet.addAll(Files.readAllLines(gitignorePath));
            } catch (IOException e) {
                throw new ValidationException("Не удалось считать файл .gitignore");
            }
        }

        if (commands.containsKey(RECURSIVE_KEY)) {
            commands.put(MAX_DEPTH_KEY, commands.getOrDefault(MAX_DEPTH_KEY, String.valueOf(Integer.MAX_VALUE)));
        } else {
            commands.put(MAX_DEPTH_KEY, commands.getOrDefault(MAX_DEPTH_KEY, "1"));
        }
    }

    private static void checkValueIsNotStub(String... commandKeys) {
        for (String commandKey : commandKeys) {
            if (STUB.equals(commands.get(commandKey))) {
                throw new ValidationException("Параметр %s указан неверно".formatted(commandKey));
            }
        }
    }

    private static void visitFiles(Path basePath) {
        try (Stream<Path> stream = Files.walk(basePath, Integer.parseInt(commands.get(MAX_DEPTH_KEY)))) {
            if (commands.containsKey(THREAD_KEY)) {
                visitFilesMultithreaded(stream);
            } else {
                stream.forEach(Main::processFile);
            }
        } catch (IOException e) {
            throw new RuntimeException("Возникла ошибка при обходе");
        }
    }

    private static void visitFilesMultithreaded(Stream<Path> stream) {
        ExecutorService executorService = Executors.newFixedThreadPool(Integer.parseInt(commands.get(THREAD_KEY)));
        try {
            List<Future<?>> futures = new ArrayList<>();
            stream.forEach(file -> futures.add(executorService.submit(() ->
                    processFile(file))
            ));
            for (Future<?> future : futures) {
                future.get();
            }
        } catch (ExecutionException | InterruptedException ignored) {
            logger.severe("Возникла ошибка при многопоточном обходе");
        } finally {
            executorService.shutdown();
        }
    }

    public static void processFile(Path file) {
        if (!Files.isRegularFile(file) || gitignoreSet.contains(file.getFileName().toString())) {
            return;
        }

        String fileExtension = getExtensionByFileName(file.toString());

        if (!includeSet.isEmpty() && !includeSet.contains(fileExtension) || excludeSet.contains(fileExtension)) {
            return;
        }

        analyzeFile(file, fileExtension);
    }

    private static void analyzeFile(Path file, String fileExtension) {
        FileStatistics fileStatistics = new FileStatistics();

        try (Stream<String> stringStream = Files.lines(file)) {
            fileStatistics.setSize(Files.size(file));
            stringStream.forEach(str -> {
                fileStatistics.incLineCounter();
                if (!str.isBlank()) {
                    fileStatistics.incNotEmptyLineCounter();
                }
                if (JAVA_EXTENSION.equalsIgnoreCase(fileExtension) && str.startsWith("//")
                        || BASH_EXTENSION.equalsIgnoreCase(fileExtension) && str.startsWith("#")) {
                    fileStatistics.incCommentLineCounter();
                }
            });
        } catch (IOException e) {
            logger.warning("Не удалось обработать файл %s, он не будет учтен в статистике");
        }
        statistics.updateStatistics(fileStatistics);
    }

    private static void printStatistics(String format) {
        switch (format.trim().toLowerCase()) {
            case "plain" -> System.out.println(Main.statistics);
            case "json" -> System.out.println(Main.statistics.toJson());
            case "xml" -> System.out.println(Main.statistics.toXml());
            default -> logger.warning("Указан неверный формат для экспорта статистики");
        }
    }
}