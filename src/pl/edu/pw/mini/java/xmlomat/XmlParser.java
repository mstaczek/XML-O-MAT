package pl.edu.pw.mini.java.xmlomat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.round;
import static java.util.Collections.synchronizedList;
import static pl.edu.pw.mini.java.xmlomat.Utilities.iterable;
import static pl.edu.pw.mini.java.xmlomat.Utilities.parseRandomNumber;

public class XmlParser {
    private final FileParsingUI parentUI;
    private final List<XmlWorker> activeWorkers = synchronizedList(new ArrayList<>());
    private final HashMap<String, List<Element>> defaultDefinitions = new HashMap<>();

    public XmlParser(FileParsingUI parentUI) {
        this.parentUI = parentUI;
        importDatasets();
    }

    private void importDatasets() {
        File dir = new File("datasets");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File set : directoryListing) {
                loadDataset(set);
            }
        } else {
            System.out.println("Couldn't find any datasets!");
        }
    }
    private void loadDataset(File file) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setIgnoringComments(true);
            dbFactory.setIgnoringElementContentWhitespace(true);

            Document doc = dbFactory.newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();


            List<Element> possibleElements = new ArrayList<>();
            for(Node child : iterable(doc.getDocumentElement().getChildNodes())) {
                try {possibleElements.add((Element) child);}
                catch(ClassCastException e) {}
            }

            String key = file.getName().substring(4,file.getName().length()-4);
            defaultDefinitions.put(key, possibleElements);

            System.out.println("Loaded dataset "+file.getName());
        } catch (Exception e) {
            System.out.println("Couldn't load dataset "+file.getName());
            e.printStackTrace();
        }
    }

    public void parseFiles(File... files) {
        for(File file : files)
            activateNewWorker(file);
    }
    public void parseFiles(Collection<File> files) {
        for(File file : files)
            activateNewWorker(file);
    }
    private void activateNewWorker(File file) {
        XmlWorker worker = new XmlWorker(file);
        activeWorkers.add(worker);
        worker.start();
    }

    public void cancelAll() {
        for(XmlWorker worker : activeWorkers)
            worker.cancel();
    }

    public class XmlWorker extends Thread {
        private final File thisFile;
        private boolean canceled = false;
        private final HashMap<String, List<Element>> customDefinitions = (HashMap<String, List<Element>>) defaultDefinitions.clone();

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
                dbFactory.setIgnoringComments(true);
                dbFactory.setIgnoringElementContentWhitespace(true);

                Document doc = dbFactory.newDocumentBuilder().parse(thisFile);
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

        private boolean parseXML(Element node) {
            if(canceled) return false;

            // Node repetition
            if(!node.getAttribute("XOM-repeat").isBlank()) {
                try {
                    long repeats = round(parseRandomNumber(node.getAttribute("XOM-repeat")))-1;
                    node.removeAttribute("XOM-repeat");
                    for(int i=0;i<repeats;i++)
                        node.getParentNode().insertBefore(node.cloneNode(true), node.getNextSibling());
                }
                catch(Exception e) {e.printStackTrace();}
            }

            // Custom definition
            if(node.getTagName().startsWith("XOM-define-")) {
                try {
                    List<Element> possibleElements = new ArrayList<>();
                    for(Node child : iterable(node.getChildNodes())) {
                        try {possibleElements.add((Element) child);}
                        catch(ClassCastException e) {}
                    }

                    String key = node.getTagName().substring(11);
                    customDefinitions.put(key, possibleElements);
                }
                catch(Exception e) {e.printStackTrace();}
                return true;
            }

            // Generating item from custom definition
            if(node.getTagName().startsWith("XOM-")) {
                try {
                    String key = node.getTagName().substring(4);
                    List<Element> possibleElements = customDefinitions.get(key);
                    if(possibleElements == null) return false;
                    Node selectedElement = possibleElements.get(ThreadLocalRandom.current().nextInt(0, possibleElements.size()));
                    node.getParentNode().insertBefore(node.getOwnerDocument().adoptNode(selectedElement.cloneNode(true)), node.getNextSibling());
                    return true;
                }
                catch(Exception e) {e.printStackTrace();}
                return false;
            }

            // Random inner value
            if(!node.getAttribute("XOM-random").isBlank()) {
                try {
                    double newValue = parseRandomNumber(node.getAttribute("XOM-random"));
                    node.removeAttribute("XOM-random");
                    node.setTextContent(String.valueOf(newValue));
                }
                catch(Exception e) {e.printStackTrace();}
            }

            // Recursion on children
            List<Node> deleteLater = new ArrayList<>();
            for(Node child : iterable(node.getChildNodes())) {
                try {
                    boolean shouldBeRemoved = parseXML((Element) child);
                    if(shouldBeRemoved)
                        deleteLater.add(child);
                }
                catch(ClassCastException e) {
                    if(child.getTextContent().isBlank())
                        deleteLater.add(child);
                }
            }
            for(Node child : deleteLater) {
                node.removeChild(child);
            }
            return false;
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
