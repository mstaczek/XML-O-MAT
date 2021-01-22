package pl.edu.pw.mini.java.xmlomat;

public interface FileParsingUI {
    void onFileLoadFail(String path);
    void onFileInvalidStructure(String path);
    void onFileParsed(UnsavedFile unsaved_xml_file);
    void onFileSaveFail(UnsavedFile unsaved_xml_file);
    void endFileProcessing();
}
