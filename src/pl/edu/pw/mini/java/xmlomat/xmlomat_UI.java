package pl.edu.pw.mini.java.xmlomat;

public interface xmlomat_UI {
    public void onFileLoadFail(String path);
    public void onFileInvalidStructure(String path);
    public void onFileParsed(UnsavedFile unsaved_xml_file);
    public void onFileSaveFail(UnsavedFile unsaved_xml_file);
    public void endFileProcessing();
}
