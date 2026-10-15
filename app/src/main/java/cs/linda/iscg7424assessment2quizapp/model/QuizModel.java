package cs.linda.iscg7424assessment2quizapp.model;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QuizModel {
    @DocumentId
    private String id;
    private String title;
    private String category;
    private String difficulty;
    private int questionCount;
    private Date startDate;
    private Date endDate;
    private int totalSubmissions;
    private int totalRatingScore;
    private double averageRating;
    private List<QuestionModel> questions = new ArrayList<>();

    public QuizModel() {}

    public String getStatus() {
        Date now = new Date();
        if (startDate != null && now.before(startDate)) {
            return "UPCOMING";
        } else if (endDate != null && now.after(endDate)) {
            return "ENDED";
        } else {
            return "ACTIVE";
        }
    }

    // ================= Getters =================
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }
    public int getQuestionCount() { return questionCount; }
    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
    public int getTotalSubmissions() { return totalSubmissions; }
    public int getTotalRatingScore() { return totalRatingScore; }
    public double getAverageRating() { return averageRating; }
    public List<QuestionModel> getQuestions() { return questions; }

    // ================= Setters =================
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setCategory(String category) { this.category = category; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }
    public void setTotalSubmissions(int totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    public void setTotalRatingScore(int totalRatingScore) { this.totalRatingScore = totalRatingScore; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }
    public void setQuestions(List<QuestionModel> questions) { this.questions = questions; }
}