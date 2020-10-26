package org.bershath.photo.clean;

import java.io.File;

public class PhotoLocationRecord {

    private File sourceFile;
    private File destinationFile;
    private File xmpSourceFile;
    private File xmpDestinationFile;

    public File getSourceFile() {
        return sourceFile;
    }
    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
    public File getDestinationFile() {
        return destinationFile;
    }
    public void setDestinationFile(File destinationFile) {
        this.destinationFile = destinationFile;
    }
    public File getXmpSourceFile() {
        return xmpSourceFile;
    }

    public void setXmpSourceFile(File xmpSourceFile) {
        this.xmpSourceFile = xmpSourceFile;
    }

    public File getXmpDestinationFile() {
        return xmpDestinationFile;
    }

    public void setXmpDestinationFile(File xmpDestinationFile) {
        this.xmpDestinationFile = xmpDestinationFile;
    }



}