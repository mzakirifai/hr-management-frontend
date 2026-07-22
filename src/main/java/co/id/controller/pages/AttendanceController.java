package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.model.Attendance;
import co.id.model.Employee;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import java.io.IOException;
import java.util.List;
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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class AttendanceController {
    @FXML
    private LookupBox<Employee> lookupBoxEmployee;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button filterBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button addBtn;
    
    @FXML
    private TableView<Attendance> tableView;
    
    @FXML
    private TableColumn<Attendance, String> tableColumnDate, tableColumnCheckIn, tableColumnCheckOut, tableColumnOvertimeHour, 
                tableColumnLeaveType, tableColumnDateCreated, tableColumnCreatedBy, tableColumnEmployee;
    
    @FXML
    private Pagination pagination;
    
    private MasterService masterService;
    private TransactionService transactionService;
    private ObservableList<Attendance> observableList;
    
    private final int editingRowIndex = -1;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        transactionService = new TransactionServiceImpl();
        observableList = FXCollections.observableArrayList();
        
        // System.out.println("observableList >>>"+observableList.size());
        // System.out.println("transactionService Paging>>>"+transactionService.getAttendances(1, 10));
        // System.out.println("transactionService All>>>"+transactionService.getAllAttendances());
        
        TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
        colEmp.setCellValueFactory(clbck -> new SimpleStringProperty(clbck.getValue().getName()));
        
        lookupBoxEmployee.configure(
                () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
        );
        
        tableColumnEmployee.setCellValueFactory(
            attendance -> new SimpleStringProperty(attendance.getValue().getEmployee().getName()
        ));
        
        tableColumnDate.setCellValueFactory(
            attendance -> new SimpleStringProperty(attendance.getValue().getDate() != null ? attendance.getValue().getDate().toString() : ""
        ));
        tableColumnCheckIn.setCellValueFactory(
            attendance -> new SimpleStringProperty(attendance.getValue().getCheckIn()!= null ? attendance.getValue().getCheckIn().toString() : ""
        ));
        tableColumnCheckOut.setCellValueFactory(
            attendance -> new SimpleStringProperty(attendance.getValue().getCheckOut()!= null ? attendance.getValue().getCheckOut().toString() : ""
        ));
        
        tableColumnOvertimeHour.setCellValueFactory(
            attendance -> new SimpleStringProperty(
                attendance.getValue().getOvertimeHour() != null
                    ? attendance.getValue().getOvertimeHour().toString()
                    : ""
            )
        );
        tableColumnLeaveType.setCellValueFactory(attendance -> new SimpleStringProperty(attendance.getValue().getLeaveType())); 
        
        tableColumnDateCreated.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getCreated_date()!= null ? contract.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnCreatedBy.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getCreated_by()
        ));
        
        int totalRows = transactionService.countAttendances();
        // System.out.println("totalRows >>>"+totalRows);
        // System.out.println("totalRows >>>"+transactionService.countAttendances());
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
            observableList.setAll(transactionService.getAttendanceByEmployee(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void openForm(Attendance attendance){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/AttendanceForm.fxml"));
            Parent formRoot = loader.load();
            
            AttendanceFormController formController = loader.getController();
            
            formController.setOnSaveCallback(() -> {
                refreshTable();
            });
            
            Region mainRoot = (Region) addBtn.getScene().getRoot();
            mainRoot.setEffect(new GaussianBlur(10));
            
            Stage dialog = new Stage();
            dialog.setTitle(attendance == null ? "Add Attendance" : "Edit Attendance");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            
            Scene scene = new Scene(formRoot, 435, 400);
            
            //.............tambahin ini
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
        observableList.setAll(transactionService.getAttendances(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = transactionService.countAttendances();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        pagination.setPageCount(pageCount);
        loadPage(pagination.getCurrentPageIndex(), rowsPerPage);
    }
}
