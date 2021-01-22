package pl.edu.pw.mini.java.xmlomat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.synchronizedList;

public class XmlParser {
    private final FileParsingUI parentUI;
    private final List<XmlWorker> activeWorkers = synchronizedList(new ArrayList<>());

    public XmlParser(FileParsingUI parentUI) throws TransformerConfigurationException {
        this.parentUI = parentUI;

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer xmlTransformer = transformerFactory.newTransformer();

        xmlTransformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        xmlTransformer.setOutputProperty(OutputKeys.INDENT, "yes");
        xmlTransformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        UnsavedXmlFile.outputTransformer = xmlTransformer;
    }


    public void parseFiles(File... files) {
        for(File file : files)
            activateNewWorker(file);
    }
    public void parseFiles(Collection<File> files) {
        for(File file : files)
            activateNewWorker(file);
    }
    private XmlWorker activateNewWorker(File file) {
        XmlWorker worker = new XmlWorker(file);
        activeWorkers.add(worker);
        worker.start();
        return worker;
    }

    public void cancelAll() {
        for(XmlWorker worker : activeWorkers)
            worker.cancel();
    }

    public class XmlWorker extends Thread {
        private final File thisFile;
        private boolean canceled = false;

        XmlWorker(File file) {
            thisFile = file;
        }

        public void run() {
            System.out.println("Parsing file " + thisFile.getName() + "...");
            Document xmlContent = loadFile();
            if(!canceled) parseXML(xmlContent.getDocumentElement());
            if(!canceled) parentUI.onFileParsed(convertToUnsavedFile(xmlContent));
            System.out.println("File " + thisFile.getName() + " finished parsing.");
            cleanPostExecution();
        }
        private Document loadFile() {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

                Document doc = dBuilder.parse(thisFile);
                doc.getDocumentElement().normalize();

                return doc;

            } catch (SAXParseException e) {
                System.out.println("Couldn't parse file "+thisFile.getName());
                e.printStackTrace();
                parentUI.onFileInvalidStructure(thisFile.getAbsolutePath());
                cancel();
            } catch (Exception e) {
                System.out.println("Couldn't load file "+thisFile.getName());
                e.printStackTrace();
                parentUI.onFileLoadFail(thisFile.getAbsolutePath());
                cancel();
            }
            return null;
        }
        private void parseXML(Element node) {
            if(canceled) return;
            node.setAttribute("Wow", "17");
        }
        private UnsavedFile convertToUnsavedFile(Document document) {
            DOMSource source = new DOMSource(document);
            return new UnsavedXmlFile(source, thisFile.getAbsolutePath(), parentUI);
        }

        public void cancel() {
            canceled = true;
            cleanPostExecution();
        }
        private void cleanPostExecution() {
            boolean wasAnythingRemoved = activeWorkers.remove(this);
            if(wasAnythingRemoved && activeWorkers.isEmpty()) parentUI.endFileProcessing();
        }
    }
}
