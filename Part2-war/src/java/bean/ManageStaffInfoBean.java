package bean;

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
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Named("ManageStaffInfoBean")
@ViewScoped

public class ManageStaffInfoBean implements Serializable {

    @EJB
    private ManagerFacade ManagerFacade;
    @EJB
    private CounterStaffFacade CounterStaffFacade;
    @EJB
    private TechnicianFacade TechnicianFacade;

    private List<Object> managerStaffs;
    private String updateId;
    private String updateSuccessMessage;
    private String deleteId;
    private String deleteSuccessMessage;

    public String getUpdateSuccessMessage() {
        return updateSuccessMessage;
    }

    public void setUpdateSuccessMessage(String updateSuccessMessage) {
        this.updateSuccessMessage = updateSuccessMessage;
    }

    public String getDeleteSuccessMessage() {
        return deleteSuccessMessage;
    }

    public void setDeleteSuccessMessage(String deleteSuccessMessage) {
        this.deleteSuccessMessage = deleteSuccessMessage;
    }

    @PostConstruct
    public void init() {
        String managerId = ""; // Set this as needed (e.g., from session)
        List<Manager> managers = ManagerFacade.findAllManagerStaffs(managerId);
        List<CounterStaff> counterStaffs = CounterStaffFacade.findAllManagerStaffs(managerId);
        List<Technician> technicians = TechnicianFacade.findAllManagerStaffs(managerId);

        managerStaffs = new ArrayList<>();
        managerStaffs.addAll(managers);
        managerStaffs.addAll(counterStaffs);
        managerStaffs.addAll(technicians);

        managerStaffs.sort(new Comparator<Object>() {
            @Override
            public int compare(Object o1, Object o2) {
                Long id1 = getIdFromStaff(o1);
                Long id2 = getIdFromStaff(o2);
                return id1.compareTo(id2);
            }

            private Long getIdFromStaff(Object staff) {
                if (staff instanceof Manager) {
                    return ((Manager) staff).getId();
                } else if (staff instanceof CounterStaff) {
                    return ((CounterStaff) staff).getId();
                } else if (staff instanceof Technician) {
                    return ((Technician) staff).getId();
                }
                return 0L;
            }
        });

        updateId = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("updateId");

        if (updateId == null) {
            updateSuccessMessage = "";
        } else {
            updateSuccessMessage = "Row " + updateId + " has been successfully updated!";
        }
        
        deleteId = FacesContext.getCurrentInstance()
                .getExternalContext()
                .getRequestParameterMap()
                .get("deleteId");

        if (deleteId == null) {
            deleteSuccessMessage = "";
        } else {
            deleteSuccessMessage = "Row " + deleteId + " has been successfully deleted!";
        }
    }

    public List<Object> getManagerStaffs() {
        return managerStaffs;
    }

    public String deleteStaff(String staffId) {
        Long staffIdLong = Long.parseLong(staffId);
        Manager managerFound = ManagerFacade.find(staffIdLong);
        CounterStaff counterStaffFound = CounterStaffFacade.find(staffIdLong);
        Technician technicianFound = TechnicianFacade.find(staffIdLong);
        if (managerFound != null) {
            ManagerFacade.remove(managerFound);
        } else if (counterStaffFound != null) {
            CounterStaffFacade.remove(counterStaffFound);
        } else if (technicianFound != null) {
            TechnicianFacade.remove(technicianFound);
        }
        return "/manager/manage_staff_info.xhtml?faces-redirect=true&deleteId=" + staffId;
    }
}
