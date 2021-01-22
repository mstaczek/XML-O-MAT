package pl.edu.pw.mini.java.xmlomat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.synchronizedList;

public class XML_parser {
    private final xmlomat_UI ParentUI;
    private List<XML_worker> ActiveWorkers = synchronizedList(new ArrayList<>());

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
        private File thisFile;
        private boolean canceled = false;

        XML_worker(File file) {
            thisFile = file;
        }

        public void run() {
            System.out.println("Parsing file " + thisFile.getName() + "...");
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
