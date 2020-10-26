package org.bershath.photo.clean;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.BorderFactory;
import org.apache.commons.io.FileUtils;


public class FileFilter extends JPanel implements ActionListener, PropertyChangeListener{

    private static final long serialVersionUID = 3066607421403647003L;

    private JPanel rootPane = new JPanel(new BorderLayout());
    private JFrame frame = new JFrame("File cleaner");
    private JProgressBar jProgressBar;

    private File directory = null;
    private File newTempDir = null;

    private Set<String> rawFiles;
    private Set<String> jpgFiles;
    private Set<String> xmpFiles;

    private String rawFileType = null;
    private String baseDirName = null;

    private int percent;


    private void createAndShowGUI(){
        Dimension dimension = new Dimension(200, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenu menu = new JMenu("Options");
        menu.setMnemonic('O');

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem("Select a folder");
        menuItem.setMnemonic('s');
        menuItem.addActionListener(this);
        menu.add(menuItem);

        menuItem = new JMenuItem("Exit");
        menuItem.setMnemonic('x');
        menuItem.addActionListener(this);
        menu.add(menuItem);
        frame.setJMenuBar(menuBar);

        frame.add(rootPane);

        //Display the window.
        frame.pack();
        frame.setBounds(1000, 500, 700, 300);
        frame.setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if(e.getActionCommand() == "Exit"){
            System.exit(0);
        }

        if(e.getActionCommand() == "Select a folder"){

            JFileChooser fileChooser = new JFileChooser("Select a folder");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            int returnValue = fileChooser.showOpenDialog(FileFilter.this);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                directory = fileChooser.getSelectedFile();
                baseDirName = directory.getAbsolutePath();
                log("Base direcroty :"+baseDirName);
                percent = 0;
                DisplayProgress displayProgress = new DisplayProgress();
                displayProgress.start();
                CopyFile copyFile = new CopyFile();
                copyFile.start();
                log("Opening folder : " + directory.getName() + ".");
            } else {
                log("Open command cancelled by user.");
            }
            //log(fileChooser.getSelectedFile());
        }
    }



    private void cleanStaleFiles(Set raws, Set jpgs) throws IOException{
        log("Number of jpg files: " + jpgs.size());
        Set<PhotoLocationRecord> selectedFiles = new HashSet<PhotoLocationRecord>();

        if(jpgs.isEmpty()){
            log("No jpg files detected, exiting");
            System.exit(0);
        } else{
            newTempDir = new File(baseDirName+"/temp");
            if(!newTempDir.exists())
                newTempDir.mkdir();
        }


        Iterator<Object> iterator = raws.iterator();
        while(iterator.hasNext()){
            Object rawFileElement = iterator.next();
            if(!jpgs.contains(rawFileElement)){
                log("No maching jpeg found, not copying "+ rawFileElement.toString().toUpperCase().concat(getRawFileType()));
            } else{
                String newTempDirPathString = newTempDir.getAbsolutePath();
                String sourceFileStr = baseDirName+"/"+rawFileElement.toString().concat(getRawFileType()).toUpperCase();
                File sourceFileLocation = new File(sourceFileStr);
                File newFileLocation = new File(newTempDirPathString+"/"+ (rawFileElement.toString().concat(getRawFileType()).toUpperCase()) );
                PhotoLocationRecord record = new PhotoLocationRecord();
                record.setSourceFile(sourceFileLocation);
                record.setDestinationFile(newFileLocation);
                //Handle XMP files here
                if( xmpFiles.size() > 0 && xmpFiles.contains(rawFileElement.toString()) )
                {
                    File xmpSourceFile = new File(sourceFileLocation.toString()+".xmp");
                    File newXmpDestinationFileLocation = new File(newFileLocation.toString()+".xmp");
                    record.setXmpSourceFile(xmpSourceFile);
                    record.setXmpDestinationFile(newXmpDestinationFileLocation);
                }
                selectedFiles.add(record);
            }
        }


        Iterator<PhotoLocationRecord> selectedFilesIterator = selectedFiles.iterator();{
            int i = 0;
            while(selectedFilesIterator.hasNext()){
                PhotoLocationRecord photoLocationRecord = selectedFilesIterator.next();
                FileUtils.copyFile(photoLocationRecord.getSourceFile(), photoLocationRecord.getDestinationFile());

                if( (photoLocationRecord.getXmpSourceFile() != null)  &&  (photoLocationRecord.getXmpDestinationFile() != null)){
                    FileUtils.copyFile(photoLocationRecord.getXmpSourceFile(), photoLocationRecord.getXmpDestinationFile());
                }
                i++;
                percent = (i*100)/selectedFiles.size();
                jProgressBar.setValue(percent);
            }
        }
    }


    class CopyFile extends Thread implements Runnable{
        public void run(){
            File[] files = directory.listFiles();
            rawFiles = new HashSet<String>();
            jpgFiles = new HashSet<String>();
            xmpFiles = new HashSet<String>();

            if ( files.length > 0){
                log("Number of files "+files.length);
                for (int i=0; i < files.length; i++){
                    createFileStructures(files[i]);
                }
                try {
                    cleanStaleFiles(rawFiles,jpgFiles);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }



    class DisplayProgress extends Thread implements Runnable{
        public void run() {
            UIManager.put("ProgressBar.selectionForeground", Color.black);
            UIManager.put("ProgressBar.selectionBackground", Color.white);
            jProgressBar = new JProgressBar(0, 100);
            jProgressBar.setValue(0);
            jProgressBar.setStringPainted(true);
            jProgressBar.setBackground(Color.RED); //--- Please remove this
            jProgressBar.setForeground(Color.green);//--- Please remove this
            jProgressBar.setPreferredSize(new Dimension(400,25));

            JPanel panel = new JPanel();
            panel.add(jProgressBar);
            add(panel, BorderLayout.PAGE_START);
            setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            rootPane.removeAll();
            rootPane.add(panel);
            frame.add(rootPane);
            frame.pack();
            frame.setBounds(1000, 500, 700, 300);
            frame.setVisible(true);
        }
    }

    public FileFilter() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }


    public static void main(String[] args){
        FileFilter fileFilter = new FileFilter();
    }

    private static void log(Object o){
        System.out.println(o);
    }

    //@Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("progress" == evt.getPropertyName()) {
            int progress = (Integer) evt.getNewValue();
            jProgressBar.setValue(progress);
        }

    }

    private void createFileStructures(File file){
        String fileNameString = file.getName().toLowerCase();
        if (fileNameString.endsWith(".jpg")){
            jpgFiles.add(fileNameString.substring(0,fileNameString.lastIndexOf('.')));
        }
        if (fileNameString.endsWith(".nef")){
            rawFiles.add(fileNameString.substring(0,fileNameString.lastIndexOf('.')));
            setRawFileType(".NEF");
        }
        if (fileNameString.endsWith(".arw")){
            rawFiles.add(fileNameString.substring(0,fileNameString.lastIndexOf('.')));
            setRawFileType(".ARW");
        }
        if (fileNameString.endsWith(".cr2")){
            rawFiles.add(fileNameString.substring(0,fileNameString.lastIndexOf('.')));
            setRawFileType(".CR2");
        }
        if (fileNameString.endsWith(".xmp")){
            xmpFiles.add(fileNameString.substring(0,fileNameString.indexOf('.')));
        }
    }



    public String getRawFileType() {
        return rawFileType;
    }

    public void setRawFileType(String rawFileType) {
        this.rawFileType = rawFileType;
    }
}