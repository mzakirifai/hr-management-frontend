package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.model.Attendance;
import co.id.model.Employee;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
//import java.time.LocalTime;
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

public class AttendanceFormController {
    @FXML 
    private LookupBox<Employee> lookupBoxEmployee;
    @FXML
    private TextField textFieldOvertimeHour;
    @FXML
    private TextField textFieldCheckIn;
    @FXML
    private TextField textFieldCheckOut;
    @FXML
    private ComboBox<String> comboLeaveType;
    @FXML
    private DatePicker datePickerDate;
    @FXML
    private Button saveBtn;
    
    private MasterService masterService;
    private TransactionService transactionService;
    private Attendance selectedAttendance;
    
    private Runnable onSaveCallback;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        transactionService = new TransactionServiceImpl();
        
        // Konfigurasi Lookup dengan data Employee
        TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
        colEmp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        
        lookupBoxEmployee.configure(
            () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
        );
    }
    
    @FXML
    private void saveEntity(){
        Employee selectedEmployee = lookupBoxEmployee.getSelectedItem();
        
        if(selectedEmployee == null){
            Alert alert = new Alert(AlertType.WARNING, "No employee selected!");
            alert.showAndWait();
            return;
        }
        
        LocalDate date = datePickerDate.getValue();
        
        LocalTime checkIn;
        LocalTime checkOut;
        try {
            checkIn = LocalTime.parse(textFieldCheckIn.getText().trim());
            checkOut = LocalTime.parse(textFieldCheckOut.getText().trim());
        } catch (DateTimeParseException e) {
            Alert alert = new Alert(AlertType.ERROR, "Format jam harus HH:mm, contoh 08:00");
            alert.showAndWait();
            return;
        }
        
        Long overtimeHour = Long.parseLong(textFieldOvertimeHour.getText());
        
        String leaveType = comboLeaveType.getValue();
        
        Alert alert = null;
        
        if (selectedAttendance == null) {
            Attendance attendance = new Attendance();
            
            attendance.setEmployee(selectedEmployee);
            attendance.setDate(date);
            attendance.setCheckIn(checkIn);
            attendance.setCheckOut(checkOut);
            attendance.setOvertimeHour(overtimeHour);
            
            attendance.setLeaveType(leaveType);
            attendance.setCreated_date(LocalDate.now());
            attendance.setCreated_by("Admin");
            
            transactionService.saveOrUpdateAttendance(attendance);
            
            alert = new Alert(AlertType.INFORMATION, "Data Has been Saved");
            
            alert.showAndWait();
        } 
        
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
