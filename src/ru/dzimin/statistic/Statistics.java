package ru.dzimin.statistic;

public class Statistics {

    private Integer fileCounter = 0;
    private Long totalSizeInBytes = 0L;
    private Long lineCounter = 0L;
    private Long notEmptyLineCounter = 0L;
    private Long commentLineCounter = 0L;

    public synchronized void updateStatistics(FileStatistics fileStatistics) {
        this.fileCounter++;
        this.totalSizeInBytes += fileStatistics.getSize();
        this.lineCounter += fileStatistics.getLineCounter();
        this.notEmptyLineCounter += fileStatistics.getNotEmptyLineCounter();
        this.commentLineCounter += fileStatistics.getCommentLineCounter();
    }

    @Override
    public String toString() {
        return """
                Количество_файлов: %d
                Размер_в_байтах: %d
                Количество_строк_всего: %d
                Количество_не_пустых_строк: %d
                Количество_строк_с_комментариями: %d
                """
                .formatted(fileCounter, totalSizeInBytes, lineCounter, notEmptyLineCounter, commentLineCounter);
    }

    public String toJson() {
        return """
                {
                "Количество_файлов":%d,
                "Размер_в_байтах":%d,
                "Количество_строк_всего":%d,
                "Количество_не_пустых_строк":%d,
                "Количество_строк_с_комментариями":%d
                }
                """
                .formatted(fileCounter, totalSizeInBytes, lineCounter, notEmptyLineCounter, commentLineCounter);
    }

    public String toXml() {
        return """
                <Statistics>
                    <Количество_файлов>%d</Количество_файлов>
                    <Размер_в_байтах>%d</Размер_в_байтах>
                    <Количество_строк_всего>%d</Количество_строк_всего>
                    <Количество_не_пустых_строк>%d</Количество_не_пустых_строк>
                    <Количество_строк_с_комментариями>%d</Количество_строк_с_комментариями>
                </Statistics>
                """
                .formatted(fileCounter, totalSizeInBytes, lineCounter, notEmptyLineCounter, commentLineCounter);
    }
}