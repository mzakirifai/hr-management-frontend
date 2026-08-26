package co.id.controller.layout;

import co.id.controller.layout.Session;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;


public class SidebarController {
    @FXML private VBox sidebar;
    @FXML private Button masterMenu;
    @FXML private VBox masterMenuBox;
    @FXML private Button transactionMenu;
    @FXML private VBox transactionMenuBox;
    @FXML private Button reportMenu;
    @FXML private VBox reportMenuBox;
    @FXML private Label brandLabel;
    @FXML private Button payrollBtn;
    @FXML private Button recruitmentBtn;
    @FXML private Button reportPayrollBtn;
    @FXML private Button reportRecruitmentBtn;
    @FXML private Button settingsBtn;
    @FXML private Button userManagementBtn;
    
    private boolean masterExpanded = false;
    private boolean transactionExpanded = false;
    private boolean reportExpanded = false;
    private boolean collapsed = false;
    
    @FXML
    public void initialize(){
        ControllerRegistry.setSidebarController(this);
        
        // Simpan teks asli semua tombol
        for (var node : sidebar.getChildren()) {
            if (node instanceof Button btn) {
                btn.setUserData(btn.getText());
            }
        }
        
        // Tidak expand menu saat aplikasi mulai
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
        
        applyRoleBasedAccess();
    }
    
    public void toggleSidebar(){
        if (collapsed) {
            sidebar.setPrefWidth(220);
            sidebar.setMinWidth(220);
            sidebar.setMaxWidth(220);

            collapsed = false;
            
            brandLabel.setText("Human Resource");

            //Kembalikan teks tombol
            for (var node: sidebar.getChildren()) {
                if (node instanceof Button btn) {
                    btn.getStyleClass().remove("menu-parent-collapsed");
                    btn.getStyleClass().add("menu-parent");
        
                    if (btn == masterMenu) {
                        btn.setText(masterExpanded ? "Master ▿" : "Master ▸");

                    } else if (btn == transactionMenu) {
                        btn.setText(transactionExpanded ? "Transaction ▿" : "Transaction ▸");

                    } else if (btn == reportMenu) {
                        btn.setText(reportExpanded ? "Report ▿" : "Report ▸");

                    } else {
                        Object originalText = btn.getUserData();

                        if (originalText instanceof String text) {
                            btn.setText(text);
                        }
                    }
                }
            }
        } else {
            // Tutup semua submenu dulu sebelum mengecilkan sidebar
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

            // Baru kecilkan sidebar
            sidebar.setPrefWidth(60);
            sidebar.setMinWidth(60);
            sidebar.setMaxWidth(60);

            collapsed = true;
            
            brandLabel.setText("");
            
            // Hilangkan teks tombol induk
            for(var node : sidebar.getChildren()){
                if (node instanceof Button btn) {
                    btn.setText("");
                    btn.getStyleClass().remove("menu-parent");
                    btn.getStyleClass().add("menu-parent-collapsed");
                }
            }
        }
    }
    
    @FXML
    private void toggleMasterMenu(){
        if (collapsed) {
            showFlyoutMenu(masterMenu, 
                new String[]{"Contract", "Department", "Employee", "Position"},
                new Runnable[]{
                    this::handleContractClick,
                    this::handleDepartmentClick,
                    this::handleEmployeeClick,
                    this::handlePositionClick
                });
            return;
        }

        masterExpanded = !masterExpanded;
        masterMenuBox.setVisible(masterExpanded);
        masterMenuBox.setManaged(masterExpanded);
        masterMenu.setText(masterExpanded ? "Master ▿" : "Master ▸");
    }
    
    @FXML
    private void toggleTransactionMenu(){
        if (collapsed) {
            showFlyoutMenu(transactionMenu, 
                new String[]{"Attendance", "Payroll", "Performance", "Recruitment"},
                new Runnable[]{
                    this::handleAttendanceClick,
                    this::handlePayrollClick,
                    this::handlePerformanceClick,
                    this::handleRecruitmentClick
                });
            return;
        }

        transactionExpanded = !transactionExpanded;
        transactionMenuBox.setVisible(transactionExpanded);
        transactionMenuBox.setManaged(transactionExpanded);
        transactionMenu.setText(transactionExpanded ? "Transaction ▿" : "Transaction ▸");
    }

    
    @FXML
    private void toggleReportMenu(){
        if (collapsed) {
            showFlyoutMenu(reportMenu, 
                new String[]{"Report Attendance", "Report Payroll", "Report Performance", "Report Recruitment"},
                new Runnable[]{
                    this::handleReportAttendanceClick,
                    this::handleReportPayrollClick,
                    this::handleReportPerformanceClick,
                    this::handleReportRecruitmentClick
                });
            return;
        }

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
    
    @FXML private void handleUserManagementClick(){ loadPage("/pages/UserManagement.fxml"); }
    @FXML private void handleSettingsClick(){ loadPage("/pages/Settings.fxml"); }
    @FXML private void handleHelpClick(){ loadPage("/pages/Help.fxml"); }
    @FXML private void handleAboutClick(){ loadPage("/pages/About.fxml"); }
    
    private void loadPage(String fxmlPath){
        MainLayoutController main = ControllerRegistry.getMainLayoutController();
        if (main != null){
            main.setContent(fxmlPath);
        }
    }
    
    private void showFlyoutMenu(Button anchor, String[] labels, Runnable[] actions){
        ContextMenu flyout = new ContextMenu();

        for (int i = 0; i < labels.length; i++) {
            MenuItem item = new MenuItem(labels[i]);
            Runnable action = actions[i];
            item.setOnAction(eh -> action.run());
            flyout.getItems().add(item);
        }

        flyout.show(anchor, Side.RIGHT, 0, 0);
    }
    
    private void applyRoleBasedAccess(){
        if (Session.isAdmin()) {
            return; // Admin bisa akses semua, tidak perlu disembunyikan apapun
        }

        // STAFF: sembunyikan menu-menu tertentu
        hideNode(payrollBtn);
        hideNode(recruitmentBtn);
        hideNode(reportPayrollBtn);
        hideNode(reportRecruitmentBtn);
        hideNode(settingsBtn);
        hideNode(userManagementBtn);
    }

    private void hideNode(javafx.scene.Node node){
        if (node != null) {
            node.setVisible(false);
            node.setManaged(false);
        }
    }
}