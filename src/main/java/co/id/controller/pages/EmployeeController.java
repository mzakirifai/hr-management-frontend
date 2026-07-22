package co.id.controller.pages;

import co.id.model.Employee;
import co.id.service.MasterService;
import co.id.service.impl.MasterServiceImpl;
import java.io.IOException;
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

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EmployeeController {
    @FXML
    private TextField searchField;
    
    @FXML
    private Button filterBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button addBtn;
    
    @FXML
    private TableView<Employee> tableView;
    
    @FXML
    private TableColumn<Employee, String> tableColumnCode, tableColumnName, tableColumnAddress, tableColumnPhone, 
                tableColumnEmail, tableColumnGender, tableColumnDateHired, tableColumnStatus, tableColumnDateStart,
                tableColumnDateEnd, tableColumnContract, tableColumnDepartment, tableColumnPosition, tableColumnDateCreated, 
                tableColumnDateUpdated, tableColumnCreatedBy;
    
    @FXML
    private TableColumn<Employee, Void> tableColumnAction;
    
    @FXML
    private Pagination pagination;
    
    private MasterService masterService;
    private ObservableList<Employee> observableList;
    
    // Method Helper untuk icon
    private ImageView createIcon(String path) {
        ImageView imageView = new ImageView(
                new Image(getClass().getResourceAsStream(path))
        );

        imageView.setFitWidth(16);
        imageView.setFitHeight(16);

        return imageView;
    }
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        observableList = FXCollections.observableArrayList();
        
        tableColumnCode.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getCode()));
        tableColumnName.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getName()));
        tableColumnAddress.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getAddress()));
        tableColumnPhone.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getPhone()));
        tableColumnEmail.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getEmail()));
        tableColumnGender.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getGender()));
        tableColumnDateHired.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getHireDate() != null ? employee.getValue().getHireDate().toString() : ""
        ));
        tableColumnStatus.setCellValueFactory(employee -> new SimpleStringProperty(employee.getValue().getStatus()));
        tableColumnDateStart.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getStartDate()!= null ? employee.getValue().getStartDate().toString() : ""
        ));
        tableColumnDateEnd.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getEndDate()!= null ? employee.getValue().getEndDate().toString() : ""
        ));
        //;;
        
        tableColumnContract.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getContract().getType()
        ));
        
        tableColumnDepartment.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getDepartment().getName()
        ));
        
        tableColumnPosition.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getPosition().getName()
        ));
        
        tableColumnDateCreated.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getCreated_date()!= null ? employee.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnDateUpdated.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getUpdated_date()!= null ? employee.getValue().getUpdated_date().toString() : ""
        ));
        
        tableColumnCreatedBy.setCellValueFactory(
            employee -> new SimpleStringProperty(employee.getValue().getCreated_by()
        ));
        
        
        tableColumnAction.setCellFactory(clbck -> new TableCell<>(){
            private final Button buttonEdit = new Button("Edit");
            private final Button buttonDelete = new Button("Delete");
            private final HBox box = new HBox(5, buttonEdit, buttonDelete);
            {
                buttonEdit.setGraphic(createIcon("/icons/edit.png"));
                buttonDelete.setGraphic(createIcon("/icons/trash.png"));
                
                buttonEdit.getStyleClass().add("btn-edit");
                buttonDelete.getStyleClass().add("btn-delete");
    
                buttonEdit.setOnAction(eh -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    openForm(employee);
                });
                
                buttonDelete.setOnAction(eh -> {
                    Employee employee = getTableView().getItems().get(getIndex());
                    
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure want to delete?", ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    
                    confirm.showAndWait().ifPresent(action -> {
                        if (action == ButtonType.YES) {
                            masterService.deleteEmployee(employee.getId());
                            refreshTable();
                        }
                    });
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
        
        int totalRows = masterService.countEmployees();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        
        pagination.setPageCount(pageCount);
        pagination.setPageFactory(clbck -> {
            loadPage(clbck, rowsPerPage);
            return new VBox(tableView);
        });
        
        filterBtn.setOnAction(eh -> filterItems());
        addBtn.setOnAction(eh -> openForm(null));
        refreshBtn.setOnAction(e -> refreshTable());
    }
    
    private void filterItems(){
        String keyword = searchField.getText();
        
        if(keyword == null || keyword.isEmpty()){
            refreshTable();
        } else {
            observableList.setAll(masterService.getByEmployee(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void openForm(Employee employee){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/EmployeeForm.fxml"));
            Parent formRoot = loader.load();
            
            EmployeeFormController formController = loader.getController();
            //..
            if (employee != null) {
                formController.setEmployee(employee);
            }
            
            formController.setOnSaveCallback(() -> {
                refreshTable();
            });
            
            Region mainRoot = (Region) addBtn.getScene().getRoot();
            mainRoot.setEffect(new GaussianBlur(10));
            
            Stage dialog = new Stage();
            dialog.setTitle(employee == null ? "Add Employee" : "Edit Employee");
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
        observableList.setAll(masterService.getEmployees(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = masterService.countEmployees();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        pagination.setPageCount(pageCount);
        loadPage(pagination.getCurrentPageIndex(), rowsPerPage);
    }
}
