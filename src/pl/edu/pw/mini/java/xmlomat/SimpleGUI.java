package pl.edu.pw.mini.java.xmlomat;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.List;
import java.util.Optional;


public class SimpleGUI extends Application implements FileParsingUI {

    public Button parseSingleFileButton;
    public Button parseMultipleFilesButton;
    public ImageView mainImage;
    private Stage stage;
    private static XmlParser xmlparser;
    private Image minilogo = new Image("file:images/minilogo.png");
    private Image processingimg = new Image("file:images/processing.png");
    private static boolean processingManyFiles = false;
    private static boolean processingSingleFile = false;
    private static String outDirectory = null;


    @Override
    public void start(Stage primaryStage) throws Exception {
        xmlparser = new XmlParser(this);
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
        mainImage.setImage(processingimg);
        System.out.println("parse Single FileOnClick");

        FileChooser fil_chooser = new FileChooser();
        fil_chooser.setTitle("Choose single file");
        File file = fil_chooser.showOpenDialog(stage);
        if (file != null) {
            System.out.println("file chosen: "+file.getAbsolutePath());
            processingSingleFile = true;
            xmlparser.parseFiles(file);
        }
    }

    public void parseMultipleFileOnClick(ActionEvent actionEvent) {
        mainImage.setImage(processingimg);
        System.out.println("parse Multiple FileOnClick");

        FileChooser fil_chooser = new FileChooser();
        fil_chooser.setTitle("Choose one or more files");
        List<File> files = fil_chooser.showOpenMultipleDialog(stage);
        if (files != null) {
            outDirectory = null;
            System.out.println("files chosen: " + files);
            String inputDirectory = files.get(0).getAbsoluteFile().getParent();
            outDirectory = getOutputDirectoryPath(inputDirectory);

            if(outDirectory != null){
                processingManyFiles = true;
                xmlparser.parseFiles(files);
            }
            else{
                showCustomError("No output directory selected");
            }
        }
        else{
            showCustomError("No files were selected");
        }
    }


    private void showCustomError(String msg){
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Error encountered.");
        a.setContentText(msg);
        a.showAndWait();
    }

    private String getOutputDirectoryPath(String originalDirectory){
        String newOutDirectory = null;
        boolean choosingDirectoryFailed = false;
        do {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose output directory");
            directoryChooser.setInitialDirectory(new File(originalDirectory));
            File chosenOutDirectory = directoryChooser.showDialog(stage);
            if (chosenOutDirectory != null) {
                newOutDirectory = chosenOutDirectory.getAbsolutePath();
                if(newOutDirectory.equals(originalDirectory)){
                    showCustomError("Chosen output directory must be different from original.");
                }
            }
            else {
                choosingDirectoryFailed = true;
                System.out.println("hmm... don't know where to save many files");
                break;
            }
        } while (newOutDirectory.equals(originalDirectory));

        if (choosingDirectoryFailed) {
            if (onChoosingOutputDirFail().equals("Retry")) {
                System.out.println("retry choosing output directory file");
                return getOutputDirectoryPath(originalDirectory);
            } else {
                System.out.println("cancel processing");
                return null;
            }
        } else {
            System.out.println("chosen output directory is " + newOutDirectory);
            return newOutDirectory;
        }

    }

    private String onChoosingOutputDirFail(){
        Alert a = new Alert(Alert.AlertType.CONFIRMATION);
        a.setTitle("Error");
        a.setHeaderText("Error choosing output directory. You should choose a directory different from one containing processed files");
        a.setContentText("Would you like to choose another directory or cancel?");

        ButtonType buttonRetry = new ButtonType("Yes, retry");
        ButtonType buttonTypeCancel = new ButtonType("No,cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        a.getButtonTypes().setAll(buttonRetry, buttonTypeCancel);

        Optional<ButtonType> result = a.showAndWait();

        if (result.isPresent() && result.get() == buttonRetry) {
            return "Retry";
        } else {
            return "Cancel";
        }
    }

    @Override
    public void onFileLoadFail(String path) {
        if(processingSingleFile) {
            Platform.runLater(() -> {
                showCustomError("Could not access file: " + path);
            });
        }
        else{
            showErrorMultithread(path);
        }
    }

    private void showErrorMultithread(String path){
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Error");
            a.setHeaderText("Error encountered while reading or processing: "+path);
            a.setContentText("Skip this file or cancel all?");

            ButtonType buttonTypeSkip = new ButtonType("Skip");
            ButtonType buttonTypeCancelAll = new ButtonType("Cancel all", ButtonBar.ButtonData.CANCEL_CLOSE);
            a.getButtonTypes().setAll(buttonTypeSkip, buttonTypeCancelAll);

            Optional<ButtonType> result = a.showAndWait();

            if (result.isPresent() && result.get() == buttonTypeSkip) {
                System.out.println("skipped file");
            } else {
                System.out.println("cancel all files");
                xmlparser.cancelAll();
            }
        });

    }

    @Override
    public void onFileInvalidStructure(String path) {
         if(processingSingleFile){
             Platform.runLater(() -> {
                showCustomError("Could not parse file: " + path);
             });
        }
        else{
            showErrorMultithread(path);
        }
    }

    @Override
    public void onFileParsed(UnsavedFile unsavedXmlFile) {
        if(processingSingleFile){
            Platform.runLater(() -> {
                File file = chooseSingleOutputFile(unsavedXmlFile);

                if (file != null) {
                    System.out.println("saving to directory: " + file.getAbsolutePath());
                    unsavedXmlFile.save(file.getAbsolutePath());
                }
                else {
                    System.out.println("save location was not selected - decide what to do next");
                    processingSingleFile = true;
                    onFileSaveFail(unsavedXmlFile);
                }
            });
        }
        else{
            String processedFileName = new File(unsavedXmlFile.getInputPath()).getName();
            unsavedXmlFile.save(outDirectory + "\\" + processedFileName);
        }
    }

    private File chooseSingleOutputFile(UnsavedFile unsavedXmlFile){
        File processedFile = new File(unsavedXmlFile.getInputPath());
        String originalDirectory = processedFile.getAbsoluteFile().getParent();
        String originalName = processedFile.getName();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialFileName("processed_" + originalName + ".xml");
        fileChooser.setInitialDirectory(new File(originalDirectory));
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("XML files (*.xml)", "*.xml");
        fileChooser.getExtensionFilters().add(extFilter);
        return fileChooser.showSaveDialog(stage);
    }

    @Override
    public void onFileSaveFail(UnsavedFile unsavedXmlFile) {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION);
            a.setTitle("Error");
            a.setHeaderText("Error saving file. Retry?");
            ButtonType buttonRetry = new ButtonType("Yes, retry");

            if(processingSingleFile){
                a.setContentText("Would you like to choose another directory and try again?");
                ButtonType buttonTypeCancel = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
                a.getButtonTypes().setAll(buttonRetry, buttonTypeCancel);

                Optional<ButtonType> result = a.showAndWait();

                 if (result.isPresent() && result.get() == buttonRetry) {
                     System.out.println("retry saving file");
                     onFileParsed(unsavedXmlFile);
                 } else {
                     System.out.println("discard file");
                     endFileProcessing();
                }
            }
            else{
                a.setContentText("Would you like to skip this file or try again?");
                ButtonType buttonTypeSkip = new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE);
                a.getButtonTypes().setAll(buttonRetry, buttonTypeSkip);

                Optional<ButtonType> result = a.showAndWait();

                if (result.isPresent() && result.get() == buttonRetry) {
                    System.out.println("retry saving file");
                    onFileParsed(unsavedXmlFile);
                } else {
                    System.out.println("skip file");
                }
            }
        });
    }

    @Override
    public void endFileProcessing() {
        Platform.runLater(() -> {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Finished");
            a.setHeaderText("Finished");
            a.setContentText("Finished processing files.");
            a.showAndWait();
            resetGUIState();
        });
    }

    private void resetGUIState(){
        mainImage.setImage(minilogo);
        processingSingleFile = false;
        processingManyFiles = false;
    }
}

