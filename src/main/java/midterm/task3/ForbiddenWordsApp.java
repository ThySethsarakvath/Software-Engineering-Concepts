package midterm.task3;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ForbiddenWordsApp extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("../../ForbiddenWordsScanner.fxml")
            );
            Parent root = loader.load();
            
            Scene scene = new Scene(root, 700, 850);
            
            primaryStage.setTitle("Forbidden Words Scanner");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading FXML: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}