package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.model.Employee;
import co.id.model.Performance;
import co.id.service.MasterService;
import co.id.service.ReportService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.ReportServiceImpl;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import win.zqxu.jrviewer.JRViewerFX;

public class ReportPerformanceController {
    @FXML private LookupBox<Employee> lookupBoxEmployee;
    @FXML private Button btnViewReport;
    @FXML private StackPane reportPane;
    
    private MasterService masterService;
    private ReportService reportService;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        reportService = new ReportServiceImpl();
        
        // Konfigurasi Lookup dengan data Employee
        TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
        colEmp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        
        lookupBoxEmployee.configure(
            () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
        );
        
        btnViewReport.setOnAction(eh -> onViewReport());
    }
    
    private void onViewReport(){
        Employee selectedEmp = lookupBoxEmployee.getSelectedItem();
        
        if(selectedEmp == null){
            Alert alert = new Alert(Alert.AlertType.WARNING, "No employee selected!",
                    ButtonType.OK);
            alert.showAndWait();
            return;
        }
        
        try{
            // Load file.jasper langsung dari resources/reports
            JasperReport jasperReport = (JasperReport) JRLoader.loadObject(
                getClass().getResourceAsStream("/reports/ReportPerformance.jasper")
            );
            
            // Ambil data dari service
            List<Performance> data = reportService.getPerformanceReport(selectedEmp.getName());
           
            // Isi report dengan data
            JRDataSource dataSource = new JRBeanCollectionDataSource(data);
            JasperPrint jasperprint = JasperFillManager.fillReport(jasperReport, new HashMap<>(), dataSource);
            
            // Tampilkan di stackPane dengan JRViewerFX
            JRViewerFX viewerFX = new JRViewerFX(jasperprint);
            reportPane.getChildren().setAll(viewerFX);
            
        } catch (JRException ex){
            ex.printStackTrace();
        }
    }
}
