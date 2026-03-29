package bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;
import entity.Service;
import facade.ServiceFacade;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import java.math.BigDecimal;
import java.util.ArrayList;

@Named("SetServicePriceBean")
@ViewScoped
public class SetServicePriceBean implements Serializable {

    @EJB
    private ServiceFacade ServiceFacade;

    private List<Service> services;
    private String price;
    private String selectedServiceId;

    private String serviceError;
    private String priceError;

    // variables to store values after setting price
    private String serviceName;
    private String serviceOriPrice;
    private String serviceSetPrice;

    private String setPriceSuccessMessage;

    public String getSelectedServiceId() {
        return selectedServiceId;
    }

    public void setSelectedServiceId(String selectedServiceId) {
        this.selectedServiceId = selectedServiceId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getServiceError() {
        return serviceError;
    }

    public void setServiceError(String serviceError) {
        this.serviceError = serviceError;
    }

    public String getPriceError() {
        return priceError;
    }

    public void setPriceError(String priceError) {
        this.priceError = priceError;
    }

    public String getSetPriceSuccessMessage() {
        return setPriceSuccessMessage;
    }

    public void setSetPriceSuccessMessage(String setPriceSuccessMessage) {
        this.setPriceSuccessMessage = setPriceSuccessMessage;
    }

    @PostConstruct
    public void init() {
        services = ServiceFacade.findAll();

        serviceName = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("serviceName");

        serviceOriPrice = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("serviceOriPrice");
        
        serviceSetPrice = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("price");

        if (serviceName == null && serviceOriPrice == null && serviceSetPrice == null) {
            setPriceSuccessMessage = "";
        } else {
            setPriceSuccessMessage = serviceName + " has been updated from " + serviceOriPrice + " to " + serviceSetPrice;
        }
    }

    public List<Service> getServices() {
        return services;
    }

    public List<SelectItem> getServiceSelectItems() {
        List<SelectItem> items = new ArrayList<>();
        for (Service service : services) {
            items.add(new SelectItem(service.getId(), service.getName()));
        }
        return items;
    }

    private String processPrice(String value) {
        String result = "1";
        if (value == null || value.trim().isEmpty()) {
            result = "0";
        } else if (!value.matches("-?\\d+")) {
            result = "2";
        }
        if (!result.equals("0") && !result.equals("2")) {
            int valueInt = Integer.parseInt(value);
            if (valueInt < 1) {
                result = "3";
            }
        }

        return result;
    }

    public String setServicePrice() {
        boolean valuesCorrect = false;

        if (selectedServiceId == null) {
            serviceError = "Please select a service";
        } else {
            serviceError = "";
        }

        if (processPrice(price).equals("0")) {
            priceError = "Please enter a value.";
        } else if (processPrice(price).equals("2")) {
            priceError = "Input value must be number.";
        } else if (processPrice(price).equals("3")) {
            priceError = "Number must be greater than 0";
        } else {
            priceError = "";
        }

        if (priceError.equals("") && serviceError.equals("")) {
            valuesCorrect = true;
        }

        if (valuesCorrect) {
            Long serviceID = Long.parseLong(selectedServiceId);
            Service serviceFound = ServiceFacade.find(serviceID);
            if (serviceFound != null) {
                BigDecimal servicePrice = new BigDecimal(price);
                String serviceName = serviceFound.getName();
                String serviceOriPrice = serviceFound.getPrice().toString();
                serviceFound.setPrice(servicePrice);
                ServiceFacade.edit(serviceFound);
                return "/manager/set_service_price.xhtml?faces-redirect=true&serviceName=" + serviceName
                        + "&serviceOriPrice=" + serviceOriPrice + "&price=" + price;
            }
        }

        return null;
    }
}
