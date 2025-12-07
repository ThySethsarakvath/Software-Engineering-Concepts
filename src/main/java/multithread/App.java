package multithread;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        // Load FXML from resources root
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scence.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 600, 500);
        stage.setScene(scene);
        stage.setTitle("Scene Builder Layout");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
