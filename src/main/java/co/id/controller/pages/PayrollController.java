package co.id.controller.pages;

import co.id.component.EditableCell;
import co.id.component.LookupBox;
import co.id.model.Employee;
import co.id.model.Payroll;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class PayrollController {
    @FXML
    private LookupBox<Employee> lookupBoxEmployee;
    
    @FXML
    private Button filterBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button addBtn;
    
    @FXML
    private TableView<Payroll> tableView;
    
    @FXML
    private TableColumn<Payroll, String> tableColumnPeriod, tableColumnBaseSalary,
            tableColumnAllowance, tableColumnDeduction, tableColumnBpjs, tableColumnTaxPph21, 
            tableColumnNetSalary, tableColumnDateCreated, tableColumnCreatedBy, tableColumnEmployee;
    
    @FXML
    private Pagination pagination;
    
    private MasterService masterService;
    private TransactionService transactionService;
    private ObservableList<Payroll> observableList;
    
    private final int editingRowIndex = -1;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        transactionService = new TransactionServiceImpl();
        observableList = FXCollections.observableArrayList();
        
        TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
        colEmp.setCellValueFactory(clbck -> new SimpleStringProperty(clbck.getValue().getName()));
        
        lookupBoxEmployee.configure(
                () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
        );
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setCurrencySymbol("Rp ");
        symbols.setGroupingSeparator('.');
        symbols.setMonetaryDecimalSeparator(',');
        
        DecimalFormat rupiahFormat = new DecimalFormat("Rp ###,###.00", symbols);
        
        tableColumnEmployee.setCellValueFactory(
            payroll -> new SimpleStringProperty(payroll.getValue().getEmployee().getName()));
        
        tableColumnPeriod.setCellValueFactory(
            payroll -> new SimpleStringProperty(payroll.getValue().getPeriod()));
        

        tableColumnBaseSalary.setCellFactory(
            clbck -> new EditableCell<>(
                payroll -> rupiahFormat.format(payroll.getSalary()), (payroll, val) -> {                                 
                    try{
                        String clean = val.replaceAll("[^\\d]", "");
                        long parsed = Long.parseLong(clean);
                        payroll.setSalary(parsed);
                    } catch (NumberFormatException e) {
                        payroll.setSalary(0L);
                    }
                },() -> editingRowIndex
            )
        );
        
        tableColumnAllowance.setCellValueFactory(
            payroll -> new SimpleStringProperty(
                payroll.getValue().getAllowance()!= null
                    ? payroll.getValue().getAllowance().toString()
                    : ""
            )
        );
        tableColumnDeduction.setCellValueFactory(
            payroll -> new SimpleStringProperty(
                payroll.getValue().getDeduction()!= null
                    ? payroll.getValue().getDeduction().toString()
                    : ""
            )
        );
        tableColumnBpjs.setCellValueFactory(
            payroll -> new SimpleStringProperty(
                payroll.getValue().getBpjs()!= null
                    ? payroll.getValue().getBpjs().toString()
                    : ""
            )
        );
        tableColumnTaxPph21.setCellValueFactory(
            payroll -> new SimpleStringProperty(
                payroll.getValue().getTaxPph21()!= null
                    ? payroll.getValue().getTaxPph21().toString()
                    : ""
            )
        );
        tableColumnNetSalary.setCellValueFactory(
            payroll -> new SimpleStringProperty(
                payroll.getValue().getNetSalary()!= null
                    ? payroll.getValue().getNetSalary().toString()
                    : ""
            )
        );
        
        tableColumnDateCreated.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getCreated_date()!= null ? contract.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnCreatedBy.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getCreated_by()
        ));
        
        int totalRows = transactionService.countPayrolls();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(clbck -> {
            loadPage(clbck, rowsPerPage);
            return new VBox(tableView);
        });
        
        addBtn.setOnAction(eh -> openForm(null));
        filterBtn.setOnAction(e -> filterItems());
        refreshBtn.setOnAction(e -> refreshTable());
    }
    
    private void filterItems(){
        String keyword = lookupBoxEmployee.getText();
        
        if(keyword == null || keyword.isEmpty()){
            refreshTable();
        } else {
            observableList.setAll(transactionService.getPayrollByEmployee(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void openForm(Payroll payroll){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/PayrollForm.fxml"));
            Parent formRoot = loader.load();
            
            PayrollFormController formController = loader.getController();
            
            formController.setOnSaveCallback(() -> {
                refreshTable();
            });
            
            Region mainRoot = (Region) addBtn.getScene().getRoot();
            mainRoot.setEffect(new GaussianBlur(10));
            
            Stage dialog = new Stage();
            dialog.setTitle(payroll == null ? "Add Payroll" : "Edit Payroll");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            
            Scene scene = new Scene(formRoot, 435, 400);
            
            scene.getStylesheets().add(
                getClass().getResource("/css/material.css").toExternalForm()
            );
            
            dialog.setScene(scene);
            dialog.centerOnScreen();
            
            dialog.setOnHidden(eh -> {
                mainRoot.setEffect(null);
            });
            
            dialog.showAndWait();
        } catch (IOException e) {
            e.getMessage();
        }
    }
    
    private void loadPage(int pageIndex, int rowsPerPage){
        observableList.setAll(transactionService.getPayrolls(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = transactionService.countPayrolls();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        pagination.setPageCount(pageCount);
        loadPage(pagination.getCurrentPageIndex(), rowsPerPage);
    }
}
