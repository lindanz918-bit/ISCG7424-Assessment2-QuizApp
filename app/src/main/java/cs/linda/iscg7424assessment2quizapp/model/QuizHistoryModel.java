package cs.linda.iscg7424assessment2quizapp.model;

import java.util.List;

public class QuizHistoryModel {
    private String historyId;
    private String userId;
    private String quizId;
    private String quizTitle;
    private String quizCategory;
    private String quizDifficulty;
    private int score;
    private int totalQuestions;
    private long timestamp;
    private List<QuestionHistoryModel> questionResults;

    public QuizHistoryModel() {}

    // Getters and Setters
    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }
    public String getUserId() { return userId; }
    public String getQuizId() { return quizId; }
    public String getQuizTitle() { return quizTitle; }
    public String getQuizCategory() { return quizCategory; }
    public String getQuizDifficulty() { return quizDifficulty; }
    public int getScore() { return score; }
    public int getTotalQuestions() { return totalQuestions; }
    public long getTimestamp() { return timestamp; }
    public List<QuestionHistoryModel> getQuestionResults() { return questionResults; }
}