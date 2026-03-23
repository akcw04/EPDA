package epda.bean;

import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import java.io.Serializable;

@Named(value = "testBean")
@RequestScoped
public class TestBean implements Serializable {

    private String message = "Waiting for button click...";

    public String getMessage() {
        return message;
    }

    public void checkConnection() {
        this.message = "🚀 SUCCESS! JSF and Java are talking via CDI!";
    }
}