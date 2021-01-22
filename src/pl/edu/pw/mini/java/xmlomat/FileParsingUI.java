package pl.edu.pw.mini.java.xmlomat;

public interface FileParsingUI {
    void onFileLoadFail(String path);
    void onFileInvalidStructure(String path);
    void onFileParsed(UnsavedFile unsavedFile);
    void onFileSaveFail(UnsavedFile unsavedFile);
    void endFileProcessing();
}
