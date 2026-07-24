package co.id.controller.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;


public class SidebarController {
    @FXML private VBox sidebar;
    @FXML private Button masterMenu;
    @FXML private VBox masterMenuBox;
    @FXML private Button transactionMenu;
    @FXML private VBox transactionMenuBox;
    @FXML private Button reportMenu;
    @FXML private VBox reportMenuBox;
    
    private boolean masterExpanded = false;
    private boolean transactionExpanded = false;
    private boolean reportExpanded = false;
    private boolean collapsed = false;
    
    @FXML
    public void initialize(){
        ControllerRegistry.setSidebarController(this);
        
        // Tidak expand Master dan Transaction saat start
        masterExpanded = false;
        masterMenuBox.setVisible(false);
        masterMenuBox.setManaged(false);
        masterMenu.setText("Master ▸");
        
        transactionExpanded = false;
        transactionMenuBox.setVisible(false);
        transactionMenuBox.setManaged(false);
        transactionMenu.setText("Transaction ▸");
        
        reportExpanded = false;
        reportMenuBox.setVisible(false);
        reportMenuBox.setManaged(false);
        reportMenu.setText("Report ▸");
        
        // Langsung expand Master dan Transaction saat start
//        masterExpanded = true;
//        masterMenuBox.setVisible(true);
//        masterMenuBox.setManaged(true);
//        masterMenu.setText("Master ▿");
//        
//        transactionExpanded = true;
//        transactionMenuBox.setVisible(true);
//        transactionMenuBox.setManaged(true);
//        transactionMenu.setText("Transaction ▿");
//        
//        reportExpanded = true;
//        reportMenuBox.setVisible(true);
//        reportMenuBox.setManaged(true);
//        reportMenu.setText("Report ▿");
    }
    
    public void toggleSidebar(){
        if (collapsed) {
            sidebar.setPrefWidth(220);
            collapsed = false;
            
            //Kembalikan tekS aSli dari uSer data
            for (var node: sidebar.getChildren()) {
                if (node instanceof Button btn) {
                    Object originalText = btn.getUserData();
                    
                    if (originalText instanceof String text) {
                        btn.setText(text);
                    }
                }
            }
        } else {
            sidebar.setPrefWidth(50);
            collapsed = true;
            
            // Kosongkan teks semua button
            for(var node : sidebar.getChildren()){
                if (node instanceof Button btn) {
                    btn.setText("");
                }
            }
        }
    }
    
    @FXML
    private void toggleMasterMenu(){
        masterExpanded = !masterExpanded;
        masterMenuBox.setVisible(masterExpanded);
        masterMenuBox.setManaged(masterExpanded);
        masterMenu.setText(masterExpanded ? "Master ▿" : "Master ▸");
    }
    
    @FXML
    private void toggleTransactionMenu(){
        transactionExpanded = !transactionExpanded;
        transactionMenuBox.setVisible(transactionExpanded);
        transactionMenuBox.setManaged(transactionExpanded);
        transactionMenu.setText(transactionExpanded ? "Transaction ▿" : "Transaction ▸");
    }
    
    @FXML
    private void toggleReportMenu(){
        reportExpanded = !reportExpanded;
        reportMenuBox.setVisible(reportExpanded);
        reportMenuBox.setManaged(reportExpanded);
        reportMenu.setText(reportExpanded ? "Report ▿" : "Report ▸");
    }
    
    @FXML private void handleDashboardClick(){ loadPage("/pages/Dashboard.fxml"); }
    @FXML private void handleContractClick(){ loadPage("/pages/ContractInlineEditing.fxml"); }
    @FXML private void handleDepartmentClick(){ loadPage("/pages/DepartmentInlineEditing.fxml"); }
    @FXML private void handleEmployeeClick(){ loadPage("/pages/Employee.fxml"); }
    @FXML private void handlePositionClick(){ loadPage("/pages/PositionInlineEditing.fxml"); }
    
    @FXML private void handleAttendanceClick(){ loadPage("/pages/Attendance.fxml"); }
    @FXML private void handlePayrollClick(){ loadPage("/pages/Payroll.fxml"); }
    @FXML private void handlePerformanceClick(){ loadPage("/pages/PerformanceInlineEditing.fxml"); }
    @FXML private void handleRecruitmentClick(){ loadPage("/pages/RecruitmentInlineEditing.fxml"); }
    
    @FXML private void handleReportAttendanceClick(){ loadPage("/pages/ReportAttendance.fxml"); }
    @FXML private void handleReportPayrollClick(){ loadPage("/pages/ReportPayroll.fxml"); }
    @FXML private void handleReportPerformanceClick(){ loadPage("/pages/ReportPerformance.fxml"); }
    @FXML private void handleReportRecruitmentClick(){ loadPage("/pages/ReportRecruitment.fxml"); }
    
    @FXML private void handleSettingsClick(){ loadPage("/pages/Settings.fxml"); }
    @FXML private void handleHelpClick(){ loadPage("/pages/Help.fxml"); }
    @FXML private void handleAboutClick(){ loadPage("/pages/About.fxml"); }
    
    private void loadPage(String fxmlPath){
        MainLayoutController main = ControllerRegistry.getMainLayoutController();
        if (main != null){
            main.setContent(fxmlPath);
        }
    }
}
