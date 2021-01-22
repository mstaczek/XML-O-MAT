package pl.edu.pw.mini.java.xmlomat;

import javax.xml.transform.TransformerConfigurationException;
import java.io.File;

public class TestEnvironment {

    public static void main(String[] args) throws TransformerConfigurationException {
        TestUI test = new TestUI();
    }

    public static class TestUI implements FileParsingUI {
        private XmlParser xmlparser;

        public TestUI() throws TransformerConfigurationException {
            System.out.println("Hi");
            xmlparser = new XmlParser(this);
            xmlparser.parseFiles(new File("example.xml"));
        }

        @Override
        public void onFileLoadFail(String path) {System.out.println("Couldn't load file " + path);}

        @Override
        public void onFileInvalidStructure(String path) {System.out.println("Invalid file structure " + path);}

        @Override
        public void onFileParsed(UnsavedFile unsavedFile) {
            System.out.println("File parsed!");
            unsavedFile.save("outputTest.xml");
        }

        @Override
        public void onFileSaveFail(UnsavedFile unsavedFile) {
            System.out.println("Couldn't save file");
        }

        @Override
        public void endFileProcessing() {
            System.out.println("Finished parsing!");
        }
    }
}