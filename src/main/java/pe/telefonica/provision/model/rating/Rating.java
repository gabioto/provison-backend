package pe.telefonica.provision.model.rating;

import java.io.Serializable;

import org.springframework.data.mongodb.core.mapping.Field;

public class Rating implements Serializable {

	private static final long serialVersionUID = 4845729030347835498L;

	@Field("key_name")
	private String keyName;

	@Field("title")
	private String title;

	@Field("rating")
	private Integer rating;

	@Field("question")
	private String question;

	@Field("answer")
	private String answer;

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

}
