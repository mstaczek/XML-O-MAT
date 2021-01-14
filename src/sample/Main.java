package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.InputStream;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("XML-O-MAT");
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.show();
        ImageView imageUI = (ImageView) scene.lookup("#imageUI");
        System.out.println("yay" + imageUI.toString());
        Image minilogo = new Image("file:images/minilogo.png");
        imageUI.setImage(minilogo);
    }


    public static void main(String[] args) {
        launch(args);
    }
}



