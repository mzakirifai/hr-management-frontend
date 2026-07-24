package co.id.controller.pages;

import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class DashboardController {
    @FXML private Label lblTotalEmployee;
    @FXML private Label lblTotalDepartment;
    @FXML private Label lblTotalPosition;
    @FXML private Label lblTotalAttendance;
    @FXML private Label lblTotalContract;
    @FXML private Label lblTotalPayroll;
    @FXML private Label lblTotalPerformance;
    @FXML private Label lblTotalRecruitment;
    
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
    }
}
