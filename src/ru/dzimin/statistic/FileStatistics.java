package ru.dzimin.statistic;

public class FileStatistics {

    private Long size = 0L;
    private Long lineCounter = 0L;
    private Long notEmptyLineCounter = 0L;
    private Long commentLineCounter = 0L;

    public void setSize(Long size) {
        this.size = size;
    }

    public void incLineCounter() {
        this.lineCounter++;
    }

    public void incNotEmptyLineCounter() {
        this.notEmptyLineCounter++;
    }

    public void incCommentLineCounter() {
        this.commentLineCounter++;
    }

    public Long getSize() {
        return size;
    }

    public Long getNotEmptyLineCounter() {
        return notEmptyLineCounter;
    }

    public Long getLineCounter() {
        return lineCounter;
    }

    public Long getCommentLineCounter() {
        return commentLineCounter;
    }
}
