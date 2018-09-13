package com.alfaloop.insoleble.utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
    public static class CsvWriter {
        private final String csvExtenName = ".csv";

        private final String FILE_EXITED = "File is already exited!";

        private File sdcardRoot = Environment.getExternalStorageDirectory();
        private File targetFloder = new File(sdcardRoot, "/DataLogger/");
        private BufferedWriter writer = null;

        public final static int DELETE_FILE_IF_EXIST = 0;
        public final static int EXCEPTION_FILE_IF_EXIST = 1;

        private String processingFileName = null;
        private Boolean isProcessing = false;

        public static CsvWriter initWriter(String fileName, int handleMethod) {
            CsvWriter cw = null;
            try {
                cw = new CsvWriter(fileName, handleMethod);
            } catch (Exception e) {

            } finally {
                return cw;
            }
        }

        /*   fileName: 20180101 without .csv   */
        private CsvWriter(String fileName, int handleMethod) throws Exception {
            fileName = fileName.replace(".csv", "");
            if(!targetFloder.isDirectory())
                targetFloder.mkdirs();
            File fileToCheck = new File(targetFloder, fileName.concat(csvExtenName));

            if(fileToCheck.exists()) {
                if(handleMethod == EXCEPTION_FILE_IF_EXIST) {
                    throw new Exception(FILE_EXITED);
                } else if(handleMethod == DELETE_FILE_IF_EXIST) {
                    fileToCheck.deleteOnExit();
                }
            }

            processingFileName = fileName;
            isProcessing = true;
            fileToCheck.createNewFile();
            writer = new BufferedWriter(new FileWriter(fileToCheck));
        }

        public void insertDateRow(String row) throws IOException {
            if(isProcessing) {
                writer.write(row);
                writer.write("\n");
                writer.flush();
            }
        }

        public void killWriter() throws IOException {
            if(isProcessing) {
                writer.close();
                isProcessing = false;
                processingFileName = null;
            }
        }

        public boolean isProcessing() {
            return isProcessing;
        }

        public String getProcessingFileName() {
            return processingFileName;
        }
    }
}
