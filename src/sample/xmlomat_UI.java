package sample;

public interface xmlomat_UI {
    public void onFileLoadFail(String path);
    public void onFileInvalidStructure(String path);
    public void onFileParsed(Object unsaved_xml_file);
    public void onFileSaveFail(Object unsaved_xml_file);
    public void endFileProcessing();
}
