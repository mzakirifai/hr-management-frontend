package co.id.controller.pages;

import co.id.model.Attendance;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class DashboardController {
    @FXML private Label lblTotalEmployee;
    @FXML private Label lblTotalDepartment;
    @FXML private Label lblTotalPosition;
    @FXML private Label lblTotalAttendance;
    @FXML private Label lblTotalContract;
    @FXML private Label lblTotalPayroll;
    @FXML private Label lblTotalPerformance;
    @FXML private Label lblTotalRecruitment;
    
    @FXML private TableView<Attendance> tableTodayAttendance;
    @FXML private TableColumn<Attendance, String> colRecentEmployee;
    @FXML private TableColumn<Attendance, String> colRecentDate;
    @FXML private TableColumn<Attendance, String> colRecentCheckIn;
    @FXML private TableColumn<Attendance, String> colRecentCheckOut;
    @FXML private TableColumn<Attendance, String> colRecentLeaveType;
    
    private MasterService masterService;
    private TransactionService transactionService;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        transactionService = new TransactionServiceImpl();

        lblTotalEmployee.setText(String.valueOf(masterService.getAllEmployees().size()));
        lblTotalDepartment.setText(String.valueOf(masterService.getAllDepartments().size()));
        lblTotalPosition.setText(String.valueOf(masterService.getAllPositions().size()));
        lblTotalContract.setText(String.valueOf(masterService.getAllContracts().size()));
        lblTotalAttendance.setText(String.valueOf(transactionService.countAttendances()));
        lblTotalPayroll.setText(String.valueOf(transactionService.countPayrolls()));
        lblTotalPerformance.setText(String.valueOf(transactionService.countPerformances()));
        lblTotalRecruitment.setText(String.valueOf(transactionService.countRecruitments()));
        
        setupTodayAttendanceTable();
    }
    
    private void setupTodayAttendanceTable(){
        tableTodayAttendance.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        colRecentEmployee.setCellValueFactory(a -> 
            new SimpleStringProperty(a.getValue().getEmployee() != null ? a.getValue().getEmployee().getName() : ""));
        colRecentDate.setCellValueFactory(a -> 
            new SimpleStringProperty(a.getValue().getDate() != null ? a.getValue().getDate().toString() : ""));
        colRecentCheckIn.setCellValueFactory(a -> 
            new SimpleStringProperty(a.getValue().getCheckIn() != null ? a.getValue().getCheckIn().toString() : ""));
        colRecentCheckOut.setCellValueFactory(a -> 
            new SimpleStringProperty(a.getValue().getCheckOut() != null ? a.getValue().getCheckOut().toString() : ""));
        colRecentLeaveType.setCellValueFactory(a -> 
            new SimpleStringProperty(a.getValue().getLeaveType()));

        // Ambil 5 data attendance terbaru (halaman 1, size 5)
        tableTodayAttendance.getItems().setAll(transactionService.getTodayAttendances());
        
        tableTodayAttendance.setFixedCellSize(35);
        tableTodayAttendance.prefHeightProperty().bind(
            tableTodayAttendance.fixedCellSizeProperty().multiply(
                javafx.beans.binding.Bindings.size(tableTodayAttendance.getItems()).add(1.05)
            )
        );
    }
}
