package bean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.Locale;

@Named("formatBean")
@ApplicationScoped
public class FormatBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public String money(Number amount) {
        double value = amount != null ? amount.doubleValue() : 0.0;
        return String.format(Locale.US, "%.2f", value);
    }
}
