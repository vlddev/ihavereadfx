package com.vlad.ihaveread.util;

import info.debatty.java.stringsimilarity.Cosine;
import javafx.scene.control.Alert;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;

public class Util {

    @Data
    @Builder
    static class SimilarityData {
        Double similarity;
        String bookFile;
        String origName;
        String nameInFile;
    }

    public static String trimOrNull(String str) {
        return str != null ? str.trim() : null;
    }

    public static String trimOrEmpty(String str) {
        return str != null ? str.trim() : "";
    }

    public static Alert warningAlert(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert;
    }

    public static Alert infoAlert(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert;
    }

    public static List<String> getSimilarFiles(String bookName, String bookDir) {
        List<String> retList = new ArrayList<>();
        List<SimilarityData> simList = new ArrayList<>();
        // use threshold or best matches
        double threshold = 0.4d;
        String bookNameLat = UkrainianToLatin.generateLat(bookName);
        List<String> origNames = new ArrayList<>();
        origNames.add(bookName);
        if (!bookNameLat.equalsIgnoreCase(bookName)) {
            origNames.add(bookNameLat);
        }
        List<String> bookFiles = Stream.of(new File(bookDir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
        Cosine cosine = new Cosine();
        for(String origName : origNames) {
            for (String bookFile : bookFiles) {
                // remove file extension
                String nameInFile = FilenameUtils.getBaseName(bookFile);
                if (nameInFile.toLowerCase().endsWith("fb2")) {
                    nameInFile = FilenameUtils.getBaseName(nameInFile);
                }
                SimilarityData similarityData = SimilarityData.builder()
                        .bookFile(bookFile).origName(origName).nameInFile(nameInFile)
                        .similarity(cosine.similarity(origName, nameInFile))
                        .build();
                simList.add(similarityData);
                if (similarityData.getSimilarity() > threshold) {
                    if (!retList.contains(bookFile)) {
                        retList.add(bookFile);
                    }
                }
            }
        }
        if (retList.size() == 0) {
            // get best two matches
            simList.sort(Comparator.comparing(SimilarityData::getSimilarity).reversed());
            for (int i = 0; i < simList.size() && i < 2; i++) {
                if (!retList.contains(simList.get(i).getBookFile())) {
                    retList.add(simList.get(i).getBookFile());
                }
            }
        }
        return retList;
    }
}
