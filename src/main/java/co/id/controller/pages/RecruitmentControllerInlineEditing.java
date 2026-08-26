package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.controller.layout.Session;
import co.id.model.Position;
import co.id.model.Recruitment;
import co.id.service.MasterService;
import co.id.service.TransactionService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.TransactionServiceImpl;
import java.time.LocalDate;
import java.util.List;
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

public class RecruitmentControllerInlineEditing {
    private MasterService masterService;
    private TransactionService transactionService;
    private ObservableList<Recruitment> observableList;
    
    @FXML
    private LookupBox<Position> lookupBoxPosition;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Button filterBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button addBtn;
    
    @FXML
    private TableColumn<Recruitment, Void> tableColumnAction;
    
    @FXML
    private TableView<Recruitment> tableView;
    
    @FXML
    private TableColumn<Recruitment, String> tableColumnPosition, tableColumnName;
    
    @FXML
    private TableColumn<Recruitment, String> tableColumnDateCreated, tableColumnCreatedBy;
    
    @FXML
    private TableColumn<Recruitment, String> tableColumnStatus;
    
    @FXML
    private Pagination pagination;
    
    private int editingRowIndex = -1;
    
    @FXML
    public void initialize(){
        masterService = new  MasterServiceImpl();
        transactionService = new TransactionServiceImpl();
        observableList = FXCollections.observableArrayList();   
        
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<Position, String> colPos = new TableColumn<>("Position");
        colPos.setCellValueFactory(clbck -> new SimpleStringProperty(clbck.getValue().getName()));
        
        lookupBoxPosition.configure(
                () -> masterService.getAllPositions(), List.of(colPos), Position::getName
        );
        
        // Data untuk lookup position
        tableColumnPosition.setCellFactory(tc -> new TableCell<>(){
            private final LookupBox<Position> positionLookup = new LookupBox<>();
            {
                TableColumn<Position, String> colPos = new TableColumn<>("Position");
                colPos.setCellValueFactory(position -> new SimpleStringProperty(position.getValue().getName()));
                
                positionLookup.configure(
                    () -> masterService.getAllPositions(), List.of(colPos), Position::getName
                );
                
                positionLookup.selectedItemProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && getIndex() >= 0 && getIndex() < tableView.getItems().size()) {
                        Recruitment rect = tableView.getItems().get(getIndex());
                        rect.setPosition(newVal);
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
                    Recruitment rect = getTableView().getItems().get(getIndex());
                    if (getIndex() == editingRowIndex) {
                        positionLookup.setSelectedItem(rect.getPosition());
                        setGraphic(positionLookup);
                        setText(null);
                    } else {
                        setText(rect.getPosition()!= null ? rect.getPosition().getName() : "");
                        setGraphic(null);
                    }
                }
            }
        });
        
        tableColumnName.setCellFactory(tc ->
            new EdittableCell(
                    recruitment -> recruitment.getName(), (recruitment, val) -> recruitment.setName(val))
        );
        
        tableColumnStatus.setCellFactory(tc ->
            new EdittableCell(
                    recruitment -> recruitment.getStatus(), (recruitment, val) -> recruitment.setStatus(val))
        );
        
        tableColumnAction.setCellFactory(params -> new TableCell<>() {
        private final Button btnSave = new Button("Save");
        private final Button btnCancel = new Button("Cancel");
        private final HBox boxView = new HBox();
        private final HBox boxEdit = new HBox(5, btnSave, btnCancel);

        {
            btnSave.setGraphic(createIcon("/icons/save.png"));
            btnCancel.setGraphic(createIcon("/icons/cancel.png"));

            btnSave.getStyleClass().add("btn-save");
            btnCancel.getStyleClass().add("btn-cancel");

            btnSave.setOnAction(e -> {
                Recruitment recruitment = getTableView().getItems().get(getIndex());

                if (recruitment.getPosition() == null) {
                    Alert warning = new Alert(Alert.AlertType.WARNING, "Position wajib dipilih");
                    warning.showAndWait();
                    return;
                }

                if (recruitment.getName() == null || recruitment.getName().isBlank()) {
                    Alert warning = new Alert(Alert.AlertType.WARNING, "Name wajib diisi");
                    warning.showAndWait();
                    return;
                }

                recruitment.setCreated_date(LocalDate.now());
                recruitment.setCreated_by(Session.getCurrentUser().getUsername());

                transactionService.saveOrUpdateRecruitment(recruitment);

                Alert alert = new Alert(Alert.AlertType.INFORMATION,
                            "Data has been saved",
                            ButtonType.OK);
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
        protected void updateItem(Void item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setGraphic(null);
            } else {
                setGraphic(getIndex() == editingRowIndex ? boxEdit : boxView);
            }
        }
    });
        
        tableColumnDateCreated.setCellValueFactory(
            Recruitment -> new SimpleStringProperty(Recruitment.getValue().getCreated_date()!= null ? Recruitment.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnCreatedBy.setCellValueFactory(
            Recruitment -> new SimpleStringProperty(Recruitment.getValue().getCreated_by()
        ));
        
        // Pagination setup
        int totalRows = transactionService.countRecruitments();
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
        String keyword = lookupBoxPosition.getText();
        
        if(keyword == null || keyword.isEmpty()){
            refreshTable();
        } else {
            observableList.setAll(transactionService.getRecruitmentByPosition(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void loadPage(int pageIndex, int rowsPerPage){
        observableList.setAll(transactionService.getRecruitments(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = transactionService.countRecruitments();
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
        
        Recruitment newRecruitment = new Recruitment(0, "", "", LocalDate.now(), LocalDate.now(), Session.getCurrentUser().getUsername(), null);
        observableList.add(0, newRecruitment);
        editingRowIndex = 0;
        tableView.refresh();
        tableView.scrollTo(0);
    }
    
    private class EdittableCell extends TableCell<Recruitment, String>{
        private final TextField tf = new TextField();
        private final Function<Recruitment, String> getter;
        private final BiConsumer<Recruitment, String> setter;
        
        EdittableCell (Function<Recruitment, String> getter, BiConsumer<Recruitment, String> setter){
            this.getter = getter;
            this.setter = setter;
            
            // listener: setiap perubahan langsung update ke model
            tf.textProperty().addListener((obs, oldVal, newVal) -> {
                if (getIndex()>= 0 && getIndex() < tableView.getItems().size()){
                    Recruitment recruitment = getTableView().getItems().get(getIndex());
                    setter.accept(recruitment, newVal);
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
                Recruitment recruitment = getTableView().getItems().get(getIndex());
                if(getIndex() == editingRowIndex){
                    tf.setText(getter.apply(recruitment));
                    setGraphic(tf);
                    setText(null);
                } else {
                    setText(getter.apply(recruitment));
                    setGraphic(null);
                }
            }
        }
    }
}