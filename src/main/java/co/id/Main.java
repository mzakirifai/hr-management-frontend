package co.id;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application{

    @Override
    public void start(Stage stage) throws Exception {
        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/ContractInlineEditing.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/PositionInlineEditing.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/DepartmentInlineEditing.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/RecruitmentInlineEditing.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/PerformanceInlineEditing.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Employee.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Attendance.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/Payroll.fxml"));
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/pages/ContractForm.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Sample");
        stage.setScene(scene);
        stage.show();*/
        
        Parent root = FXMLLoader.load(getClass().getResource("/layout/MainLayout.fxml"));
        
        // Ukuran Layar Utama
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();
        
        /*Misalnya set ukuran stage jadi setengah layar
        set ukuran stage jadi 3/4 layar
        */
        //Scene scene = new Scene(root, screenWidth / 2, screenHeight / 2);
        Scene scene = new Scene(root, screenWidth * 0.75, screenHeight * 0.75);
        
        scene.getStylesheets().add(
                getClass().getResource("/css/material.css").toExternalForm()
        );
        
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icons/favicon.png")));
        
        stage.setTitle("Human Resources");
        stage.setScene(scene);
        
        stage.setX(screenBounds.getMinX() + screenWidth * 0.125);
        stage.setY(screenBounds.getMinY() + screenHeight * 0.125);
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
