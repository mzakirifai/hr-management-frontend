package co.id.controller.pages;

import co.id.controller.layout.Session;
import co.id.model.User;
import co.id.service.UserService;
import co.id.service.impl.UserServiceImpl;
import java.io.IOException;
import java.util.Optional;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UserManagementController {
    @FXML
    private TextField searchField;
    
    @FXML
    private Button filterBtn;
    
    @FXML
    private Button refreshBtn;
    
    @FXML
    private Button addBtn;
    
    @FXML
    private TableView<User> tableView;
    
    @FXML
    private TableColumn<User, String> tableColumnUsername, tableColumnFullName, tableColumnRole, 
                tableColumnEmployee, tableColumnDateCreated;
    
    @FXML
    private TableColumn<User, Void> tableColumnAction;
    
    @FXML
    private Pagination pagination;
    
    private UserService userService;
    private ObservableList<User> observableList;
    
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
        userService = new UserServiceImpl();
        observableList = FXCollections.observableArrayList();
        
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        tableColumnUsername.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getUsername()));
        tableColumnFullName.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getFullName()));
        tableColumnRole.setCellValueFactory(u -> new SimpleStringProperty(u.getValue().getRole()));
        
        tableColumnEmployee.setCellValueFactory(u -> new SimpleStringProperty(
            u.getValue().getEmployee() != null && u.getValue().getEmployee().getName() != null 
                ? u.getValue().getEmployee().getName() 
                : "-"
        ));
        
        tableColumnDateCreated.setCellValueFactory(u -> new SimpleStringProperty(
            u.getValue().getCreated_date() != null ? u.getValue().getCreated_date().toString() : ""
        ));
        
        tableColumnAction.setCellFactory(clbck -> new TableCell<>(){
            private final Button buttonEdit = new Button("Edit");
            private final Button buttonDelete = new Button("Delete");
            private final Button buttonResetPassword = new Button("Reset Password");
            private final HBox box = new HBox(5, buttonEdit, buttonResetPassword, buttonDelete);
            {
                buttonEdit.setGraphic(createIcon("/icons/edit.png"));
                buttonDelete.setGraphic(createIcon("/icons/trash.png"));
                
                buttonEdit.getStyleClass().add("btn-edit");
                buttonDelete.getStyleClass().add("btn-delete");
                buttonResetPassword.getStyleClass().add("btn-save");
                
                buttonEdit.setOnAction(eh -> {
                    User user = getTableView().getItems().get(getIndex());
                    openForm(user);
                });
                
                buttonDelete.setOnAction(eh -> {
                    User user = getTableView().getItems().get(getIndex());
                    
                    // Cegah hapus akun sendiri
                    User currentUser = Session.getCurrentUser();
                    if (currentUser != null && currentUser.getId() == user.getId()) {
                        Alert warning = new Alert(Alert.AlertType.WARNING, 
                            "Anda tidak bisa menghapus akun Anda sendiri yang sedang login", ButtonType.OK);
                        warning.showAndWait();
                        return;
                    }
                    
                    // Cegah menghapus Admin terakhir
                    if ("ADMIN".equals(user.getRole())) {
                        long totalAdmin = userService.getAllUsers().stream()
                                .filter(u -> "ADMIN".equals(u.getRole()))
                                .count();

                        if (totalAdmin <= 1) {
                            Alert warning = new Alert(Alert.AlertType.WARNING, 
                                "Tidak bisa menghapus Admin terakhir. Sistem harus punya minimal 1 Admin.", ButtonType.OK);
                            warning.showAndWait();
                            return;
                        }
                    }
                    
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                        "Are you sure want to delete user '" + user.getUsername() + "'?", 
                        ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirm Delete");
                    confirm.setHeaderText(null);
                    
                    confirm.showAndWait().ifPresent(action -> {
                        if (action == ButtonType.YES) {
                            userService.delete(user.getId());
                            refreshTable();
                        }
                    });
                });
                
                buttonResetPassword.setOnAction(eh -> {
                    User user = getTableView().getItems().get(getIndex());
                    
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                        "Are you sure want to reset password for '" + user.getUsername() + "'?", 
                        ButtonType.YES, ButtonType.NO);
                    confirm.setTitle("Confirm Reset Password");
                    confirm.setHeaderText(null);
                    
                    confirm.showAndWait().ifPresent(action -> {
                        if (action == ButtonType.YES) {
                            TextInputDialog inputDialog = new TextInputDialog();
                            inputDialog.setTitle("Reset Password");
                            inputDialog.setHeaderText(null);
                            inputDialog.setContentText("New password for '" + user.getUsername() + "':");
                            
                            Optional<String> result = inputDialog.showAndWait();
                                
                            result.ifPresent(newPassword -> {
                                if (newPassword == null || newPassword.isBlank()) {
                                    Alert warning = new Alert(Alert.AlertType.WARNING, "Password tidak boleh kosong", ButtonType.OK);
                                    warning.showAndWait();
                                    return;
                                }
                                
                                if (newPassword.length() < 6) {
                                    Alert warning = new Alert(Alert.AlertType.WARNING, "Password minimal 6 karakter", ButtonType.OK);
                                    warning.showAndWait();
                                    return;
                                }
                                
                                userService.resetPassword(user.getId(), newPassword);
                                
                                Alert success = new Alert(Alert.AlertType.INFORMATION, "Password berhasil di-reset", ButtonType.OK);
                                success.showAndWait();
                            });
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
        
        int totalRows = userService.countUsers();
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
            observableList.setAll(userService.getByKeyword(keyword));
            tableView.setItems(observableList);
        }
    }
    
    private void openForm(User user){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/UserForm.fxml"));
            Parent formRoot = loader.load();
            
            UserFormController formController = loader.getController();
            
            if (user != null) {
                formController.setUser(user);
            }
            
            formController.setOnSaveCallback(() -> {
                refreshTable();
            });
            
            Region mainRoot = (Region) addBtn.getScene().getRoot();
            mainRoot.setEffect(new GaussianBlur(10));
            
            Stage dialog = new Stage();
            dialog.setTitle(user == null ? "Add User" : "Edit User");
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);
            
            Scene scene = new Scene(formRoot, 435, 450);
            
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
            e.printStackTrace();
        }
    }
    
    private void loadPage(int pageIndex, int rowsPerPage){
        observableList.setAll(userService.getUsers(pageIndex + 1, rowsPerPage));
        tableView.setItems(observableList);
    }
    
    private void refreshTable(){
        int totalRows = userService.countUsers();
        int rowsPerPage = 10;
        int pageCount = (int)Math.ceil((double) totalRows / rowsPerPage);
        pagination.setPageCount(pageCount);
        loadPage(pagination.getCurrentPageIndex(), rowsPerPage);
    }
}