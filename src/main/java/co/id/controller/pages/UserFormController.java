package co.id.controller.pages;

import co.id.component.LookupBox;
import co.id.model.Employee;
import co.id.model.User;
import co.id.service.MasterService;
import co.id.service.UserService;
import co.id.service.impl.MasterServiceImpl;
import co.id.service.impl.UserServiceImpl;
import java.time.LocalDate;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import co.id.component.PasswordToggleField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class UserFormController {
    @FXML
    private TextField textFieldUsername, textFieldFullName;
    
    @FXML
    private PasswordToggleField passwordFieldPassword;
    
    @FXML
    private ComboBox<String> comboRole;
    
    @FXML
    private LookupBox<Employee> lookupBoxEmployee;
    
    @FXML
    private Button saveBtn;
    
    private MasterService masterService;
    private UserService userService;
    private User selectedUser;
    
    private Runnable onSaveCallback;
    
    @FXML
    public void initialize(){
        masterService = new MasterServiceImpl();
        userService = new UserServiceImpl();
        passwordFieldPassword.setPromptText("Password");
        
        TableColumn<Employee, String> colEmp = new TableColumn<>("Employee");
        colEmp.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        
        lookupBoxEmployee.configure(
            () -> masterService.getAllEmployees(), List.of(colEmp), Employee::getName
        );
    }
    
    public void setUser(User user){
        this.selectedUser = user;

        if (user != null) {
            textFieldUsername.setText(user.getUsername());
            textFieldUsername.setDisable(true);
            textFieldFullName.setText(user.getFullName());
            comboRole.setValue(user.getRole());

            // Sembunyikan field password saat edit (gunakan tombol Reset Password di tabel)
            passwordFieldPassword.setVisible(false);
            passwordFieldPassword.setManaged(false);

            if (user.getEmployee() != null) {
                lookupBoxEmployee.setSelectedItem(user.getEmployee());
            } else {
                lookupBoxEmployee.setSelectedItem(null);
            }
        }
    }
    
    @FXML
    private void saveEntity(){
        String username = textFieldUsername.getText();
        String password = passwordFieldPassword.getText();
        String fullName = textFieldFullName.getText();
        String role = comboRole.getValue();
        Employee selectedEmployee = lookupBoxEmployee.getSelectedItem();
        
        if (username == null || username.isBlank() || fullName == null || fullName.isBlank() || role == null) {
            Alert warning = new Alert(AlertType.WARNING, "Username, Full Name, dan Role wajib diisi");
            warning.showAndWait();
            return;
        }
        
        Alert alert;
        
        if (selectedUser == null) {
            // Mode Add - password wajib diisi
            if (password == null || password.isBlank()) {
                Alert warning = new Alert(AlertType.WARNING, "Password wajib diisi untuk user baru");
                warning.showAndWait();
                return;
            }
            
            if (password.length() < 6) {
                Alert warning = new Alert(AlertType.WARNING, "Password minimal 6 karakter");
                warning.showAndWait();
                return;
            }
            
            User user = new User();
            user.setUsername(username);
            user.setFullName(fullName);
            user.setRole(role);
            user.setEmployee(selectedEmployee);
            
            try {
                userService.register(user, password);
                alert = new Alert(AlertType.INFORMATION, "User berhasil ditambahkan");
            } catch (Exception e) {
                Alert error = new Alert(AlertType.ERROR, "Gagal menambahkan user. Username mungkin sudah dipakai.");
                error.showAndWait();
                return;
            }
            
        } else {
            // Mode Edit
            
            // Cegah menurunkan role Admin terakhir jadi Staff
            if ("ADMIN".equals(selectedUser.getRole()) && "STAFF".equals(role)) {
                long totalAdmin = userService.getAllUsers().stream()
                        .filter(u -> "ADMIN".equals(u.getRole()))
                        .count();

                if (totalAdmin <= 1) {
                    Alert warning = new Alert(AlertType.WARNING, 
                        "Tidak bisa mengubah role Admin terakhir. Sistem harus punya minimal 1 Admin.");
                    warning.showAndWait();
                    return;
                }
            }
            
            selectedUser.setFullName(fullName);
            selectedUser.setRole(role);
            selectedUser.setEmployee(selectedEmployee);

            userService.updateProfile(selectedUser);

            alert = new Alert(AlertType.INFORMATION, "User berhasil diupdate");
        }

        alert.showAndWait();
        
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
    
    public void setOnSaveCallback(Runnable callback){
        this.onSaveCallback = callback;
    }
}