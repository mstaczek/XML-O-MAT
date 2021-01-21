package pl.edu.pw.mini.java.xmlomat;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.List;


public class SimpleGUI extends Application implements xmlomat_UI{

    public Button parseSingleFileButton;
    public Button parseMultipleFilesButton;
    public ImageView mainImage;
    private Stage stage;
//    private XML_parser xmlparser;
    private Image minilogo = new Image("file:images/minilogo.png");
    private Image processingimg = new Image("file:images/processing.png");
    private boolean processingManyFiles = false;
    private boolean processingSingleFile = false;


    @Override
    public void start(Stage primaryStage) throws Exception{
//        xmlparser = new XML_parser(this);
        stage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("XML-O-MAT");
        Scene scene = new Scene(root, 500, 400);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();

        mainImage = (ImageView) scene.lookup("#mainImage");
        mainImage.setImage(minilogo);
    }


    public static void main(String[] args) {
        launch(args);
    }

    public void parseSingleFileOnClick(ActionEvent actionEvent) {
        processingSingleFile = true;
        mainImage.setImage(processingimg);
        System.out.println("parse Single FileOnClick");

        FileChooser fil_chooser = new FileChooser();
        fil_chooser.setTitle("Choose single file");
        File file = fil_chooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println(file.getAbsolutePath());
//            xmlparser.parseFiles(file);
        }

        mainImage.setImage(minilogo); //just for tests now
    }

    public void parseMultipleFileOnClick(ActionEvent actionEvent) {
        processingManyFiles = true;
        mainImage.setImage(processingimg);
        System.out.println("parse Multiple FileOnClick");

        FileChooser fil_chooser = new FileChooser();
        fil_chooser.setTitle("Choose single file");
        List<File> files = fil_chooser.showOpenMultipleDialog(stage);
        if (!files.isEmpty()) {
            System.out.println(files);
//            xmlparser.parseFiles(files);
        }

        mainImage.setImage(minilogo); //just for tests now
    }


    @Override
    public void onFileLoadFail(String path) {

    }

    @Override
    public void onFileInvalidStructure(String path) {

    }

    @Override
    public void onFileParsed(Object unsaved_xml_file) {
        if(processingSingleFile){
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDirectory = directoryChooser.showDialog(stage);
            System.out.println(selectedDirectory.getAbsolutePath());

            if (selectedDirectory != null) {
//                unsaved_xml_file.save(selectedDirectory.getAbsolutePath());
                System.out.println(selectedDirectory.getAbsolutePath());
            }
        }
        mainImage.setImage(minilogo);
    }

    @Override
    public void onFileSaveFail(Object unsaved_xml_file) {

    }

    @Override
    public void endFileProcessing() {
//        xmlparser.cancelAll();
        mainImage.setImage(minilogo);
    }
}



