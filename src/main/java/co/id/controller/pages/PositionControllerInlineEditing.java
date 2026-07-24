package co.id.controller.pages;

import co.id.component.EditableCell;
import co.id.model.Position;
import co.id.service.MasterService;
import co.id.service.impl.MasterServiceImpl;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

public class PositionControllerInlineEditing {
    private MasterService masterService;
    private ObservableList<Position> observableList;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button filterBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button addBtn;
    
    @FXML
    private TableView<Position> tableView;
    
    @FXML
    private TableColumn<Position, String> tableColumnName, tableColumnDateCreated, tableColumnDateUpdated, tableColumnCreatedBy;
    
    @FXML
    private TableColumn<Position, String> tableColumnSalary;
    
    @FXML
    private TableColumn<Position, Void> tableColumnAction;
    
    @FXML
    private Pagination pagination;
    
    private int editingRowIndex = -1;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        observableList = FXCollections.observableArrayList();
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("id", "ID"));
        symbols.setCurrencySymbol("Rp ");
        symbols.setGroupingSeparator('.');
        symbols.setMonetaryDecimalSeparator(',');

        DecimalFormat rupiahFormat = new DecimalFormat("Rp ###,###.00", symbols);
        
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tableColumnName.setCellFactory(tc ->
            new EdittableCell(
                    position -> position.getName(), (position, val) -> position.setName(val))
        );
        
        /*
        tableColumnSalary.setCellFactory(tc ->
            new EdittableCell(
                    position -> String.valueOf(position.getSalary()), (position, val) -> {
                        try{
                            position.setSalary(Long.parseLong(val.trim()));
                        } catch (NumberFormatException e){
                            
                        }
                    })
        );
        */
        tableColumnSalary.setCellFactory(
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
        
        tableColumnDateCreated.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getCreated_date()!= null ? contract.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnDateUpdated.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getUpdated_date()!= null ? contract.getValue().getUpdated_date().toString() : ""
        ));
        
        tableColumnCreatedBy.setCellValueFactory(
            contract -> new SimpleStringProperty(contract.getValue().getCreated_by()
        ));
        
        // Action column with icon
        tableColumnAction.setCellFactory(params -> new TableCell<>(){
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Delete");
            private final Button btnSave = new Button("Save");
            private final Button btnCancel = new Button("Cancel");
            private final HBox boxView = new HBox(5, btnEdit, btnDelete);
            private final HBox boxEdit = new HBox(5, btnSave, btnCancel);
            
            {
                btnEdit.setGraphic(createIcon("/icons/edit.png"));
                btnDelete.setGraphic(createIcon("/icons/trash.png"));
                btnSave.setGraphic(createIcon("/icons/save.png"));
                btnCancel.setGraphic(createIcon("/icons/cancel.png"));
                
                btnEdit.getStyleClass().add("btn-edit");
                btnDelete.getStyleClass().add("btn-delete");
                btnSave.getStyleClass().add("btn-save");
                btnCancel.getStyleClass().add("btn-cancel");
                
                btnEdit.setOnAction(e -> {
                    editingRowIndex = getIndex();
                    tableView.refresh();
                });
                
                btnDelete.setOnAction(e -> {
                    Position position = getTableView().getItems().get(getIndex());
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                                "Are you sure want to delete?",
                                ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    
                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.YES){
                            masterService.deletePosition(position.getId());
                            
                            Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                "Data has been deleted!",
                                ButtonType.OK);
                            Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                            alert.showAndWait();
                            
                            refreshTable();
                        }
                    });
                });
                
                btnSave.setOnAction(e -> {
                    Position position = getTableView().getItems().get(getIndex());
                    
                    if(position.getId() == 0){
                        // mode save (record baru)
                        position.setName(position.getName());
                        position.setSalary(position.getSalary());
                        position.setCreated_date(LocalDate.now());
                        position.setUpdated_date(null);
                        position.setCreated_by("Admin");
                        
                        masterService.saveOrUpdatePosition(position);
                    } else {
                        position.setName(position.getName());
                        position.setSalary(position.getSalary());
                        position.setUpdated_date(LocalDate.now());
                        
                        masterService.saveOrUpdatePosition(position);
                    }
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION,
                                position.getId() == 0 ? "Data has been saved" : "Data has been updated",
                                ButtonType.OK);
                    Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                    alert.showAndWait();
                            
                    editingRowIndex = -1;
                    refreshTable();
                });
               
                btnCancel.setOnAction(e -> {
                    editingRowIndex = -1;
                    refreshTable();
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                
                if(empty){
                    setGraphic(null);
                } else {
                    setGraphic(getIndex() == editingRowIndex ? boxEdit : boxView);
                }
            }
        });
        
        // Pagination setup
        int totalRows = masterService.countPositions();
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
        String keyword = searchField.getText();
        
        if(keyword == null || keyword.isEmpty()){
            refreshTable();
        } else {
            observableList.setAll(masterService.getByPosition(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void loadPage(int pageIndex, int rowsPerPage){
        observableList.setAll(masterService.getPositions(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = masterService.countPositions();
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
        
        Position newPosition = new Position(0, "", 0L, LocalDate.now(), LocalDate.now(), "Admin");
        observableList.add(0, newPosition);
        editingRowIndex = 0;
        tableView.refresh();
        tableView.scrollTo(0);
    }
    
    private class EdittableCell extends TableCell<Position, String>{
        private final TextField tf = new TextField();
        private final Function<Position, String> getter;
        private final BiConsumer<Position, String> setter;
        
        EdittableCell (Function<Position, String> getter, BiConsumer<Position, String> setter){
            this.getter = getter;
            this.setter = setter;
            
            // listener: setiap perubahan langsung update ke model
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (getIndex()>= 0 && getIndex() < tableView.getItems().size()){
                    Position position = getTableView().getItems().get(getIndex());
                    setter.accept(position, newVal);
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
                Position position = getTableView().getItems().get(getIndex());
                if(getIndex() == editingRowIndex){
                    tf.setText(getter.apply(position));
                    setGraphic(tf);
                    setText(null);
                } else {
                    setText(getter.apply(position));
                    setGraphic(null);
                }
            }
        }
    }
}
