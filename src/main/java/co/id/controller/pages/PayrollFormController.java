package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.model.Payroll;
import co.id.model.Employee;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
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

public class PayrollFormController {
    @FXML 
    private LookupBox<Employee> lookupBoxEmployee;
    
    @FXML
    private TextField textFieldPeriod, textFieldBaseSalary, textFieldAllowance, 
            textFieldDeduction, textFieldBpjs, textFieldTaxPph21, textFieldNetSalary;
    
    @FXML
    private Button saveBtn;
    
    private MasterService masterService;
    private TransactionService transactionService;
    private Payroll selectedPayroll;
    
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
        String period = textFieldPeriod.getText();
        Employee selectedEmp = lookupBoxEmployee.getSelectedItem();
        Long baseSalary = Long.parseLong(textFieldBaseSalary.getText());
        Long allowance = Long.parseLong(textFieldAllowance.getText());
        Long deduction = Long.parseLong(textFieldDeduction.getText());
        Long bpjs = Long.parseLong(textFieldBpjs.getText());
        Long taxPph21 = Long.parseLong(textFieldTaxPph21.getText());
        Long netSalary = Long.parseLong(textFieldNetSalary.getText());
        
        Alert alert;
        
        if (selectedPayroll == null) {
            Payroll payroll = new Payroll();
            
            payroll.setEmployee(selectedEmp);
            payroll.setPeriod(period);
            payroll.setSalary(baseSalary);
            payroll.setAllowance(allowance);
            payroll.setDeduction(deduction);
            payroll.setBpjs(bpjs);
            payroll.setTaxPph21(taxPph21);
            payroll.setNetSalary(netSalary);
           
            payroll.setCreated_date(LocalDate.now());
            payroll.setCreated_by("Admin");
            
            transactionService.saveOrUpdatePayroll(payroll);
            
            alert = new Alert(AlertType.INFORMATION, "Data Has been Saved");
            
            alert.showAndWait();
        } 

        
//        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
//        alertStage.showAndWait();
        
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
