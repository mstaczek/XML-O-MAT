package pl.edu.pw.mini.java.xmlomat;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class UnsavedXmlFile implements UnsavedFile {
    public static Transformer outputTransformer;
    private final String inputPath;
    private final DOMSource content;
    private final FileParsingUI parentUI;

    public UnsavedXmlFile(DOMSource content, String inputPath, FileParsingUI parentUI) {
        this.content = content;
        this.inputPath = inputPath;
        this.parentUI = parentUI;
    }

    @Override
    public String getInputPath() {
        return inputPath;
    }

    @Override
    public void save(String path) {
        try {
            File myFile = new File(path);
            StreamResult file = new StreamResult(myFile);
            outputTransformer.transform(content, file);
        } catch (Exception e) {
            e.printStackTrace();
            parentUI.onFileSaveFail(this);
        }
    }
}
