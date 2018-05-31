package com.kevindeyne.datascrambler.helper;

public class ProgressBar {
    public static void step(Long currentDownloadedRecords, Long totalRecordsToDownload) {
        if(currentDownloadedRecords % 111 == 0){
            System.out.print("Processing: " + currentDownloadedRecords + " / " + totalRecordsToDownload + "\r");
        }
    }

    public static void finish(Long totalRecordsToDownload) {
        System.out.print("Processing: " + totalRecordsToDownload + " / " + totalRecordsToDownload + "\r");
        System.out.println();
    }
}
