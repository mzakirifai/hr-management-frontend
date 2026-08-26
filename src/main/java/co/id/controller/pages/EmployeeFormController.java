package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.controller.layout.Session;
import co.id.model.Contract;
import co.id.model.Department;
import co.id.model.Employee;
import co.id.model.Position;
import co.id.service.MasterService;
import co.id.service.impl.MasterServiceImpl;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class EmployeeFormController {
    @FXML 
    private LookupBox<Department> lookupBoxDepartment;
    @FXML 
    private LookupBox<Position> lookupBoxPosition;
    @FXML 
    private LookupBox<Contract> lookupBoxContract;
    @FXML
    private TextField textFieldCode, textFieldName, textFieldPhone, textFieldEmail;
    @FXML
    private TextArea textAreaAddress;
    @FXML
    private RadioButton radioMale, radioFemale;
    @FXML
    private ComboBox<String> comboStatus;
    @FXML
    private DatePicker datePickerStartDate, datePickerEndDate, datePickerHireDate;
    @FXML
    private Button saveBtn;
    
    private MasterService masterService;
    private Employee selectedEmployee;
    
    private Runnable onSaveCallback;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        
        // Konfigurasi Lookup dengan data Department
        TableColumn<Department, String> colDept = new TableColumn<>("Department");
        colDept.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        lookupBoxDepartment.configure(
            () -> masterService.getAllDepartments(), List.of(colDept), Department::getName
        );

        // Konfigurasi Lookup dengan data Position
        TableColumn<Position, String> colPos = new TableColumn<>("Position");
        colPos.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));

        lookupBoxPosition.configure(
            () -> masterService.getAllPositions(), List.of(colPos), Position::getName
        );

        // Konfigurasi Lookup dengan data Contract
        TableColumn<Contract, String> colCont = new TableColumn<>("Contract");
        colCont.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));

        lookupBoxContract.configure(
            () -> masterService.getAllContracts(), List.of(colCont), Contract::getType
        );
    }
    //..
    public void setEmployee(Employee employee){
        this.selectedEmployee = employee;
        
        if (employee != null) {
            textFieldCode.setText(employee.getCode());
            textFieldName.setText(employee.getName());
            textAreaAddress.setText(employee.getAddress());
            textFieldPhone.setText(employee.getPhone());
            textFieldEmail.setText(employee.getEmail());
        
            if ("Male".equals(selectedEmployee.getGender())) {
                radioMale.setSelected(true);
            } else if ("Female".equals(selectedEmployee.getGender())) {
                radioFemale.setSelected(true);
            }

            datePickerHireDate.setValue(employee.getHireDate());
            comboStatus.setValue(employee.getStatus());
            datePickerStartDate.setValue(employee.getStartDate());
            datePickerEndDate.setValue(employee.getEndDate());
            
            if(employee.getDepartment() != null){
                lookupBoxDepartment.setSelectedItem(employee.getDepartment());
            } else {
                lookupBoxDepartment.setSelectedItem(null);
            }
            
            if(employee.getPosition()!= null){
                lookupBoxPosition.setSelectedItem(employee.getPosition());
            } else {
                lookupBoxPosition.setSelectedItem(null);
            }
            
            if(employee.getContract()!= null){
                lookupBoxContract.setSelectedItem(employee.getContract());
            } else {
                lookupBoxContract.setSelectedItem(null);
            }
        }
    }
    
    @FXML
    private void saveEntity(){
        String code = textFieldCode.getText();
        String name = textFieldName.getText();
        String address = textAreaAddress.getText();
        String phone = textFieldPhone.getText();
        String email = textFieldEmail.getText();
        
        String gender = null;
        if (radioMale.isSelected()) {
            gender = "Male";
        } else if (radioFemale.isSelected()) {
            gender = "Female";
        }
        
        LocalDate hireDate = datePickerHireDate.getValue();
        String status = comboStatus.getValue();
        LocalDate startDate = datePickerStartDate.getValue();
        LocalDate endDate = datePickerEndDate.getValue();
        
        Department selectedDept = lookupBoxDepartment.getSelectedItem();
        Position selectedPos = lookupBoxPosition.getSelectedItem();
        Contract selectedContract = lookupBoxContract.getSelectedItem();
        
        // --- VALIDASI ---
        if (code == null || code.isBlank() || name == null || name.isBlank()) {
            Alert warning = new Alert(AlertType.WARNING, "Employee Code dan Name wajib diisi");
            warning.showAndWait();
            return;
        }

        if (selectedDept == null || selectedPos == null || selectedContract == null) {
            Alert warning = new Alert(AlertType.WARNING, "Department, Position, dan Contract wajib dipilih");
            warning.showAndWait();
            return;
        }
        
        Alert alert;
        
        if (selectedEmployee == null) {
            Employee employee = new Employee();
            employee.setCode(code);
            employee.setName(name);
            employee.setAddress(address);
            employee.setPhone(phone);
            employee.setEmail(email);
            employee.setGender(gender);
            employee.setHireDate(hireDate);
            employee.setStatus(status);
            employee.setStartDate(startDate);
            employee.setEndDate(endDate);
            employee.setDepartment(selectedDept);
            employee.setPosition(selectedPos);
            employee.setContract(selectedContract);
            employee.setCreated_date(LocalDate.now());
            employee.setCreated_by(Session.getCurrentUser().getUsername());
            
            masterService.saveOrUpdateEmployee(employee);
            
            alert = new Alert(AlertType.INFORMATION, "Data Has been Saved");
  
        } else {
            selectedEmployee.setCode(code);
            selectedEmployee.setName(name);
            selectedEmployee.setAddress(address);
            selectedEmployee.setPhone(phone);
            selectedEmployee.setEmail(email);
            selectedEmployee.setGender(gender);
            selectedEmployee.setHireDate(hireDate);
            selectedEmployee.setStatus(status);
            selectedEmployee.setStartDate(startDate);
            selectedEmployee.setEndDate(endDate);
            selectedEmployee.setDepartment(selectedDept);
            selectedEmployee.setPosition(selectedPos);
            selectedEmployee.setContract(selectedContract);
            selectedEmployee.setUpdated_date(LocalDate.now());
            
            masterService.saveOrUpdateEmployee(selectedEmployee);
            
            alert = new Alert(AlertType.INFORMATION, "Data Has been Updated");
        }
        
        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
        alertStage.showAndWait();
        
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
    
    public void setOnSaveCallback(Runnable callback){
        this.onSaveCallback = callback;
    }
}
