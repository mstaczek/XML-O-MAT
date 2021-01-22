package pl.edu.pw.mini.java.xmlomat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

import javax.print.Doc;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.synchronizedList;

public class XML_parser {
    private final xmlomat_UI ParentUI;
    private final List<XML_worker> ActiveWorkers = synchronizedList(new ArrayList<>());

    public XML_parser(xmlomat_UI parentUI) {
        ParentUI = parentUI;
    }


    public void parseFiles(File... files) {
        for(File file : files)
            activateNewWorker(file);
    }
    public void parseFiles(Collection<File> files) {
        for(File file : files)
            activateNewWorker(file);
    }
    private XML_worker activateNewWorker(File file) {
        XML_worker worker = new XML_worker(file);
        ActiveWorkers.add(worker);
        worker.start();
        return worker;
    }

    public void cancelAll() {
        for(XML_worker worker : ActiveWorkers)
            worker.cancel();
    }

    public class XML_worker extends Thread {
        private final File thisFile;
        private boolean canceled = false;

        XML_worker(File file) {
            thisFile = file;
        }

        public void run() {
            System.out.println("Parsing file " + thisFile.getName() + "...");
            Document xmlContent = loadFile();
            if(!canceled) parseXML(xmlContent.getDocumentElement());
            if(!canceled) convertToFile(xmlContent);
//            try {
//                for(int i = 4; i > 0; i--) {
//                    if(canceled) break;
//                    System.out.println("Thread: " + getName() + ", " + i);
//                    // Let the thread sleep for a while.
//                    Thread.sleep(500);
//                }
//            } catch (InterruptedException e) {
//                System.out.println("Thread " + thisFile.getName() + " interrupted.");
//                cleanPostExecution();
//            }
            System.out.println("Thread " + thisFile.getName() + " exiting.");
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
                ParentUI.onFileInvalidStructure(thisFile.getAbsolutePath());
                cancel();
            } catch (Exception e) {
                System.out.println("Couldn't load file "+thisFile.getName());
                e.printStackTrace();
                ParentUI.onFileLoadFail(thisFile.getAbsolutePath());
                cancel();
            }
            return null;
        }
        private void parseXML(Element node) {
            if(canceled) return;
            node.setAttribute("Wow", "17");
        }
        private void convertToFile(Document document) {
            try {
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transf = transformerFactory.newTransformer();

                transf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                transf.setOutputProperty(OutputKeys.INDENT, "yes");
                transf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

                DOMSource source = new DOMSource(document);
                File myFile = new File("outputTest.xml");
                StreamResult file = new StreamResult(myFile);
                transf.transform(source, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            canceled = true;
            cleanPostExecution();
        }
        private void cleanPostExecution() {
            boolean wasAnythingRemoved = ActiveWorkers.remove(this);
            if(wasAnythingRemoved && ActiveWorkers.isEmpty()) ParentUI.endFileProcessing();
        }
    }
}
