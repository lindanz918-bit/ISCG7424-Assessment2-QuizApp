package cs.linda.iscg7424assessment2quizapp;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizResponse {
    @SerializedName("response_code")
    public int responseCode;

    @SerializedName("results")
    public List<QuestionItem> results;
}