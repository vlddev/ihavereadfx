package com.vlad.ihaveread.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import info.debatty.java.stringsimilarity.*;

public final class UkrainianToLatin {
    private static final int INDEX_0 = 0;
    private static final int INDEX_1 = 1;
    private static final int INDEX_2 = 2;
    private static final int INDEX_3 = 3;
    private static final int INDEX_4 = 4;
    private static final int INDEX_8 = 8;
    private static final int LENGTH_2 = 2;
    private static final int LENGTH_3 = 3;
    private static final int LENGTH_4 = 4;
    private static final int LENGTH_8 = 8;
    private static final Set<String> PUNCTUATIONS = new HashSet<>(Arrays.asList(
            ",", "-", "!", "?", ":", ";", ".", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "…", "—", "“", "”",
            "«", "»", "[", "]", "–", "(", ")", "№", "{", "}", "/", "\\"));

    private enum Convert {
        AA("Аа"),
        BB("Бб"),
        VV("Вв"),
        HH("Гг"),
        GG("Ґґ"),
        DD("Дд"),
        EE("Ее"),
        JeJe("Єє"),
        ZhZh("Жж"),
        ZZ("Зз"),
        YY("Ии"),
        II("Іі"),
        JiJi("Її"),
        JJ("Йй"),
        KK("Кк"),
        LL("Лл"),
        MM("Мм"),
        NN("Нн"),
        OO("Оо"),
        PP("Пп"),
        RR("Рр"),
        SS("Сс"),
        TT("Тт"),
        UU("Уу"),
        FF("Фф"),
        KhKh("Хх"),
        TsTs("Цц"),
        ChCh("Чч"),
        ShSh("Шш"),
        ShchShch("Щщ"),
        JuJu("Юю"),
        JaJa("Яя");
        private String cyrillic;
        private Convert(String cyrillic) {
            this.cyrillic = cyrillic;
        }
        /**
         * Gets cyrillic.
         * @return the cyrillic
         */
        public String getCyrillic() {
            return cyrillic;
        }

    }
    private static Map<String, ConvertCase> cyrToLat;

    private static class ConvertCase {
        private final Convert convert;
        private final boolean lowerCase;
        public ConvertCase(Convert convert, boolean lowerCase) {
            this.convert = convert;
            this.lowerCase = lowerCase;
        }
        public Convert getConvert() {
            return convert;
        }
        public boolean isLowerCase() {
            return lowerCase;
        }
    }

    static {
        cyrToLat = new HashMap<>();
        for (Convert convert : Convert.values()) {
            cyrToLat.put(convert.getCyrillic().substring(INDEX_0, INDEX_1), new ConvertCase(convert, false));
            cyrToLat.put(convert.getCyrillic().substring(INDEX_1, INDEX_2), new ConvertCase(convert, true));
            if (convert == Convert.EE) {
                cyrToLat.put("Ё", new ConvertCase(convert, false));
                cyrToLat.put("ё", new ConvertCase(convert, true));
            }
        }
    }

    /**
     * Generates latin from cyrillic.
     * @param name the name
     * @return the result
     */
    public static String generateLat(String name) {
        StringBuffer result = new StringBuffer();
        ConvertCase prevConvertCase = null;
        for (int index = 0; index < name.length(); index += 1) {
            String curChar = name.substring(index, index + INDEX_1);
            String nextChar = index == name.length() - 1 ? null : name.substring(index + INDEX_1, index + INDEX_2);
            if (cyrToLat.get(curChar) == null) {
                if (" ".equals(curChar)) {
                    prevConvertCase = null;
                    result.append(' ');
                //} else if (curChar.matches("\\n") || PUNCTUATIONS.contains(curChar)) {
                } else if ("ь".equalsIgnoreCase(curChar)) {
                } else {
                        result.append(curChar);
                }
                continue;
            }
            ConvertCase convertCase = cyrToLat.get(curChar);
            if (prevConvertCase == null) {
                checkFirstChar(result, convertCase, cyrToLat.get(nextChar) == null ? convertCase : cyrToLat
                        .get(nextChar));
            } else {
                checkMiddleChar(result, convertCase, cyrToLat.get(nextChar) == null ? convertCase : cyrToLat
                        .get(nextChar));
            }
            prevConvertCase = convertCase;
        }
        return result.toString();
    }

    /**
     * Converts first character in the word.
     * @param result resut buffer to store string in latin
     * @param convertCase current character object
     * @param nextConvertCase next character object
     */
    private static void checkFirstChar(StringBuffer result, ConvertCase convertCase, ConvertCase nextConvertCase) {
        String latName = convertCase.getConvert().name();
        switch (latName.length()) {
            case LENGTH_2:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_0, INDEX_1).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_0, INDEX_1) : latName.substring(INDEX_0, INDEX_1)
                        .toUpperCase());
                if (convertCase.getConvert() == Convert.ZZ && nextConvertCase.getConvert() == Convert.HH) {
                    result.append(nextConvertCase.isLowerCase() ? "g" : "G");
                }
                break;
            case LENGTH_3:
            case LENGTH_4:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_0, INDEX_2).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_0, INDEX_2) : latName.substring(INDEX_0, INDEX_2)
                        .toUpperCase());
                break;
            case LENGTH_8:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_0, INDEX_4).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_0, INDEX_4) : latName.substring(INDEX_0, INDEX_4)
                        .toUpperCase());
                break;
            default:
                break;
        }
    }

    /**
     * Converts middle or last character in the word.
     * @param result resut buffer to store string in latin
     * @param convertCase current character object
     * @param nextConvertCase next character object
     */
    private static void checkMiddleChar(StringBuffer result, ConvertCase convertCase, ConvertCase nextConvertCase) {
        String latName = convertCase.getConvert().name();
        switch (latName.length()) {
            case LENGTH_2:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_1, INDEX_2).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_1, INDEX_2) : latName.substring(INDEX_1, INDEX_2)
                        .toUpperCase());
                if (convertCase.getConvert() == Convert.ZZ && nextConvertCase.getConvert() == Convert.HH) {
                    result.append(nextConvertCase.isLowerCase() ? "g" : "G");
                }
                break;
            case LENGTH_3:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_2, INDEX_3).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_2, INDEX_3) : latName.substring(INDEX_2, INDEX_3)
                        .toUpperCase());
                break;
            case LENGTH_4:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_2, INDEX_4).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_2, INDEX_4) : latName.substring(INDEX_2, INDEX_4)
                        .toUpperCase());
                break;
            case LENGTH_8:
                result.append(convertCase.isLowerCase() ? latName.substring(INDEX_4, INDEX_8).toLowerCase() : nextConvertCase
                        .isLowerCase() ? latName.substring(INDEX_4, INDEX_8) : latName.substring(INDEX_4, INDEX_8)
                        .toUpperCase());
                break;
            default:
                break;
        }
    }

    public static void main(String[] args) {
        final String message = "Ця утиліта транслітерує український текст латинськими літерами.  Ёё Ьь ";
        String enMessage = "This utility transliterate ukrainian text in latin characters";
        System.out.println(message);
        System.out.println(UkrainianToLatin.generateLat(message));
        System.out.println(enMessage);
        System.out.println(UkrainianToLatin.generateLat(enMessage));
        String str1 = "Kokhantsi Yustytsii";
        String str2 = "Коханці Юстиції";
        String str2lat = UkrainianToLatin.generateLat(str2);
        Cosine cosine = new Cosine();
        Jaccard jaccard = new Jaccard();
        double sim = jaccard.similarity(str1, str2lat);
        System.out.println("str1 = "+str1);
        System.out.println("str2 = "+str2lat);
        System.out.println("jaccard similarity = "+sim);
        System.out.println("cosine similarity = "+cosine.similarity(str1, str2lat));
    }
}