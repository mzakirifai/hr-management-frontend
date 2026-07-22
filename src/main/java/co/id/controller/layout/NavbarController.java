package co.id.controller.layout;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;

public class NavbarController {
    @FXML private Button toggleSidebarBtn;
    @FXML private ImageView profileImage;
    
    @FXML
    public void initialize(){
        double radius = Math.min(profileImage.getFitWidth(), profileImage.getFitHeight()) / 2;
        profileImage.setClip(new Circle(radius, radius, radius));
        
        toggleSidebarBtn.setOnAction(eh -> {
            SidebarController sidebar = ControllerRegistry.getSidebarController();
            if (sidebar != null) {
                sidebar.toggleSidebar();
            }
        });
    }
}
