package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.model.Employee;
import co.id.model.Performance;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import java.time.LocalDate;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PerformanceControllerInlineEditing {
    private MasterService masterService;
    private TransactionService transactionService;
    private ObservableList<Performance> observableList;
    
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
    private TableView<Performance> tableView;
    
    @FXML
    private TableColumn<Performance, String> tableColumnEmployee, tableColumnPeriod;
    
    @FXML
    private TableColumn<Performance, String> tableColumnDateCreated, tableColumnCreatedBy;
    
    
    @FXML
    private TableColumn<Performance, String> tableColumnScore;
    
    @FXML
    private TableColumn<Performance, String> tableColumnRemark;
    
    @FXML
    private Pagination pagination;
    
    private int editingRowIndex = -1;
    
    @FXML
    public void initialize(){
        masterService = new  MasterServiceImpl();
        transactionService = new TransactionServiceImpl();
        observableList = FXCollections.observableArrayList();
        
        TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
        colEmp.setCellValueFactory(clbck -> new SimpleStringProperty(clbck.getValue().getName()));
        
        lookupBoxEmployee.configure(
                () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
        );
        
        // Data untuk lookup employee
        tableColumnEmployee.setCellFactory(tc -> new TableCell<>(){
            private final LookupBox<Employee> employeeLookup = new LookupBox<>();
            {
                TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
                colEmp.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getName()));
                
                employeeLookup.configure(
                    () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
                );
                
                employeeLookup.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && getIndex() >= 0 && getIndex() < tableView.getItems().size()) {
                        Performance perf = tableView.getItems().get(getIndex());
                        perf.setEmployee(newVal);
                    }
                });
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Performance perf = getTableView().getItems().get(getIndex());
                    if (getIndex() == editingRowIndex) {
                        employeeLookup.setSelectedItem(perf.getEmployee());
                        setGraphic(employeeLookup);
                        setText(null);
                    } else {
                        setText(perf.getEmployee() != null ? perf.getEmployee().getName() : "");
                        setGraphic(null);
                    }
                }
            }
        });
        
        tableColumnPeriod.setCellFactory(tc ->
            new EdittableCell(
                    performance -> performance.getPeriod(), (performance, val) -> performance.setPeriod(val))
        );
        
        tableColumnScore.setCellFactory(tc ->
            new EdittableCell(
                    performance -> String.valueOf(performance.getScore()), (performance, val) -> {
                        try{
                            performance.setScore(Long.parseLong(val.trim()));
                        } catch (NumberFormatException e){
                            
                        }
                    })
        );
        
        tableColumnRemark.setCellFactory(tc ->
            new EdittableCell(
                    performance -> performance.getRemark(), (performance, val) -> performance.setRemark(val))
        );
        
        tableColumnDateCreated.setCellValueFactory(
            Performance -> new SimpleStringProperty(Performance.getValue().getCreated_date()!= null ? Performance.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnCreatedBy.setCellValueFactory(
            Performance -> new SimpleStringProperty(Performance.getValue().getCreated_by()
        ));
        
        // Pagination setup
        int totalRows = transactionService.countPerformances();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(pageIndex -> {
            loadPage(pageIndex, rowsPerPage);
            return new VBox(tableView);
        });
        
        addBtn.setOnAction(e -> addRow());
        filterBtn.setOnAction(e -> filterItems());
        refreshBtn.setOnAction(e -> refreshTable());
    }
    
    // Method Helper untuk icon
    private ImageView createIcon(String path){
        ImageView imageView = new ImageView(
                new Image(getClass().getResourceAsStream(path))
        );

        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        return imageView;
    }
    
    private void filterItems(){
        String keyword = lookupBoxEmployee.getText();
        
        if(keyword == null || keyword.isEmpty()){
            refreshTable();
        } else {
            observableList.setAll(transactionService.getPerformanceByEmployee(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void loadPage(int pageIndex, int rowsPerPage){
        observableList.setAll(transactionService.getPerformances(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = transactionService.countPerformances();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        pagination.setPageCount(pageCount);
        loadPage(pagination.getCurrentPageIndex(), rowsPerPage);
    }
    
    private void addRow(){
        if(editingRowIndex != -1){
            Alert alert = new Alert(Alert.AlertType.WARNING,
                                "Finish editing the current row before adding a new one.",
                                ButtonType.OK);
            alert.showAndWait();
            return;
        }
        
        Performance newPerformance = new Performance(0, "", 0L, "", LocalDate.now(), LocalDate.now(), "Admin", null);
        observableList.add(0, newPerformance);
        editingRowIndex = 0;
        tableView.refresh();
        tableView.scrollTo(0);
    }
    
    private class EdittableCell extends TableCell<Performance, String>{
        private final TextField tf = new TextField();
        private final Function<Performance, String> getter;
        private final BiConsumer<Performance, String> setter;
        
        EdittableCell (Function<Performance, String> getter, BiConsumer<Performance, String> setter){
            this.getter = getter;
            this.setter = setter;
            
            // listener: setiap perubahan langsung update ke model
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (getIndex()>= 0 && getIndex() < tableView.getItems().size()){
                    Performance performance = getTableView().getItems().get(getIndex());
                    setter.accept(performance, newVal);
                }
            });
        }
        
        @Override
        protected void updateItem(String item, boolean empty){
            super.updateItem(item, empty);
            
            if(empty){
                setGraphic(null);
                setText(null);
            } else {
                Performance performance = getTableView().getItems().get(getIndex());
                if(getIndex() == editingRowIndex){
                    tf.setText(getter.apply(performance));
                    setGraphic(tf);
                    setText(null);
                } else {
                    setText(getter.apply(performance));
                    setGraphic(null);
                }
            }
        }
    }
}
