package cs.linda.iscg7424assessment2quizapp.model;

import java.util.List;

public class QuestionModel {
    private String question;
    private List<String> shuffledOptions;
    private String correctAnswer;
    private String category;
    private String difficulty;

    private String userAnswer;
    private boolean isAnswered;

    public QuestionModel() {}

    // Getter & Setter
    public String getQuestion() { return question; }
    public void setQuestionText(String questionText) { this.question = question; }

    public List<String> getShuffledOptions() { return shuffledOptions; }
    public void setShuffledOptions(List<String> shuffledOptions) { this.shuffledOptions = shuffledOptions; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getUserAnswer() { return userAnswer; }
    public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }

    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }
}
