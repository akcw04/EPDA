package bean;

import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.ejb.EJB;
import java.io.Serializable;
import entity.Manager;
import facade.ManagerFacade;
import entity.CounterStaff;
import facade.CounterStaffFacade;
import entity.Technician;
import facade.TechnicianFacade;
import jakarta.faces.context.FacesContext;

@Named("UpdateStaffInfoBean")
@ViewScoped
public class UpdateStaffInfoBean implements Serializable {
    private String updateId;
    private String originalIC;
    
    @PostConstruct
    public void init() {
        updateId = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("id");
        Long updateIdLong = Long.parseLong(updateId);
        
        Manager managerFound = ManagerFacade.find(updateIdLong);
        CounterStaff counterStaffFound = CounterStaffFacade.find(updateIdLong);
        Technician technicianFound = TechnicianFacade.find(updateIdLong);
        
        if (managerFound != null ) {
            role = "manager";
            name = managerFound.getName();
            password = managerFound.getPassword();
            gender = managerFound.getGender();
            phone = managerFound.getPhone();
            ic = managerFound.getIC();
            email = managerFound.getEmail();
            address = managerFound.getAddress();
            originalIC = managerFound.getIC();
        } else if (counterStaffFound != null) {
            role = "counter_staff";
            name = counterStaffFound.getName();
            password = counterStaffFound.getPassword();
            gender = counterStaffFound.getGender();
            phone = counterStaffFound.getPhone();
            ic = counterStaffFound.getIC();
            email = counterStaffFound.getEmail();
            address = counterStaffFound.getAddress();
            originalIC = counterStaffFound.getIC();
        } else if (technicianFound != null) {
            role = "technician";
            name = technicianFound.getName();
            password = technicianFound.getPassword();
            gender = technicianFound.getGender();
            phone = technicianFound.getPhone();
            ic = technicianFound.getIC();
            email = technicianFound.getEmail();
            address = technicianFound.getAddress();
            originalIC = technicianFound.getIC();
        }
    }

    public String getUpdateId() {
        return updateId;
    }

    public void setUpdateId(String updateId) {
        this.updateId = updateId;
    }

    // Form fields
    private String role;
    private String name;
    private String password;
    private String gender;
    private String phone;
    private String ic;
    private String email;
    private String address;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getIc() {
        return ic;
    }

    public void setIc(String ic) {
        this.ic = ic;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // Error and success messages
    private String roleError;
    private String nameError;
    private String passwordError1;
    private String passwordError2;
    private String passwordError3;
    private String genderError;
    private String phoneNumberError;
    private String icError;
    private String emailError;
    private String addressError;
    private String successMessage;

    public String getRoleError() {
        return roleError;
    }

    public void setRoleError(String roleError) {
        this.roleError = roleError;
    }

    public String getNameError() {
        return nameError;
    }

    public void setNameError(String nameError) {
        this.nameError = nameError;
    }

    public String getPasswordError1() {
        return passwordError1;
    }

    public void setPasswordError1(String passwordError1) {
        this.passwordError1 = passwordError1;
    }

    public String getPasswordError2() {
        return passwordError2;
    }

    public void setPasswordError2(String passwordError2) {
        this.passwordError2 = passwordError2;
    }

    public String getPasswordError3() {
        return passwordError3;
    }

    public void setPasswordError3(String passwordError3) {
        this.passwordError3 = passwordError3;
    }

    public String getGenderError() {
        return genderError;
    }

    public void setGenderError(String genderError) {
        this.genderError = genderError;
    }

    public String getPhoneNumberError() {
        return phoneNumberError;
    }

    public void setPhoneNumberError(String phoneNumberError) {
        this.phoneNumberError = phoneNumberError;
    }

    public String getIcError() {
        return icError;
    }

    public void setIcError(String icError) {
        this.icError = icError;
    }

    public String getEmailError() {
        return emailError;
    }

    public void setEmailError(String emailError) {
        this.emailError = emailError;
    }

    public String getAddressError() {
        return addressError;
    }

    public void setAddressError(String addressError) {
        this.addressError = addressError;
    }

    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String successMessage) {
        this.successMessage = successMessage;
    }

    @EJB
    private ManagerFacade ManagerFacade;
    @EJB
    private CounterStaffFacade CounterStaffFacade;
    @EJB
    private TechnicianFacade TechnicianFacade;

    private String processEmpty(String value) {
        String result = "1";
        if (value == null || value.trim().isEmpty()) {
            result = "0";
        }
        return result;
    }

    private String processOnlyInteger(String value) {
        String result = "1";
        if (!value.matches("\\d+")) {
            result = "0";
        }
        return result;
    }

    private String processName(String name) {
        String result = "1";
        if (processEmpty(name).equals("0")) {
            result = "0";
        } else if (!name.matches("[a-zA-Z '\\-]+")) {
            result = "2";
        }
        return result;
    }

    private String processPassword(String number) {
        String result = "1";
        if (number.length() < 7) {
            result = "0";
        }
        if (!number.matches(".*\\d.*")) {
            result = "2";
        }
        if (!number.matches(".*[^a-zA-Z0-9].*")) {
            result = "3";
        }
        return result;
    }

    private String processPhoneNumber(String number) {
        String result = "1";
        if (processEmpty(number).equals("0") || number.length() < 10) {
            result = "0";
        } else if (processOnlyInteger(number).equals("0")) {
            result = "2";
        }
        return result;
    }

    private String processIC(String number) {
        String result = "1";
        if (processEmpty(number).equals("0") || number.length() != 12) {
            result = "0";
        } else if (processOnlyInteger(number).equals("0")) {
            result = "2";
        }
        return result;
    }

    private String processEmail(String email) {
        String result = "1";
        if (processEmpty(email).equals("0")) {
            result = "0";
        } else if (!email.contains(".com")) {
            result = "2";
        }
        return result;
    }

    public String update() {
        successMessage = "";
        boolean valuesCorrect = false;

        // Role
        if (processEmpty(role).equals("0")) {
            roleError = "Role is required.";
        } else {
            roleError = "";
        }

        // Name
        if (processName(name).equals("0")) {
            nameError = "Name is required.";
        } else if (processName(name).equals("2")) {
            nameError = "Name should only contain alphabets.";
        } else {
            nameError = "";
        }

        // Password
        if (processPassword(password).equals("0")) {
            passwordError1 = "Password must be more than 7 characters.";
            passwordError2 = "";
            passwordError3 = "";
        } else if (processPassword(password).equals("2")) {
            passwordError1 = "";
            passwordError2 = "Password should contain at least 1 number.";
            passwordError3 = "";
        } else if (processPassword(password).equals("3")) {
            passwordError1 = "";
            passwordError2 = "";
            passwordError3 = "Password should contain at least 1 speacial character.";
        } else {
            passwordError1 = "";
            passwordError2 = "";
            passwordError3 = "";
        }

        // Gender
        if (processEmpty(gender).equals("0")) {
            genderError = "Gender is required.";
        } else {
            genderError = "";
        }

        // Phone Number
        if (processPhoneNumber(phone).equals("0")) {
            phoneNumberError = "Phone number must be within 10 - 11 numbers.";
        } else if (processPhoneNumber(phone).equals("2")) {
            phoneNumberError = "Phone number must have only numbers.";
        } else {
            phoneNumberError = "";
        }

        // IC
        if (processIC(ic).equals("0")) {
            icError = "IC number must have 12 numbers.";
        } else if (processIC(ic).equals("2")) {
            icError = "IC number must have only numbers.";
        } else {
            icError = "";
        }

        // Check Java DB for duplicate IC
        boolean icExists = false;
        if (icError.equals("")) {

            Manager managerFound = ManagerFacade.findByIC(ic);
            CounterStaff counterStaffFound = CounterStaffFacade.findByIC(ic);
            Technician technicianFound = TechnicianFacade.findByIC(ic);
            
            if (managerFound != null && !managerFound.getIC().equals(originalIC)) {
                icExists = true;
            } else if (counterStaffFound != null && !counterStaffFound.getIC().equals(originalIC)) {
                icExists = true;
            } else if (technicianFound != null && !technicianFound.getIC().equals(originalIC)) {
                icExists = true;
            } else {
                icExists = false;
            }

            if (icExists) {
                icError = "IC already registered.";
            } else {
                icError = "";
            }
        }

        // Email
        if (processEmail(email).equals("0")) {
            emailError = "Email is required.";
        } else if (processEmail(email).equals("2")) {
            emailError = "Email should have .com.";
        } else {
            emailError = "";
        }

        // Address
        if (processEmpty(address).equals("0")) {
            addressError = "Address is required.";
        } else {
            addressError = "";
        }

        // Created by
//        String createdBy = "";
        
        if (roleError.equals("") && nameError.equals("") && passwordError1.equals("") && passwordError2.equals("") && passwordError3.equals("") 
            && genderError.equals("") && phoneNumberError.equals("") && icError.equals("") && emailError.equals("") && addressError.equals("")) {
            valuesCorrect = true;
        }

        if (valuesCorrect == true) {
            String roleName = "";
            if (role.equals("manager")) {
                ManagerFacade.updateByID(updateId, name, password, gender, phone, ic, email, address);
                roleName = "Manager";
            } else if (role.equals("counter_staff")) {
                CounterStaffFacade.updateByID(updateId, name, password, gender, phone, ic, email, address);
                roleName = "Counter Staff";
            } else if (role.equals("technician")) {
                TechnicianFacade.updateByID(updateId, name, password, gender, phone, ic, email, address);
                roleName = "Technician";
            }
            return "/manager/manage_staff_info.xhtml?faces-redirect=true&updateId=" + updateId;
        }
        
        return null;
    }
}
