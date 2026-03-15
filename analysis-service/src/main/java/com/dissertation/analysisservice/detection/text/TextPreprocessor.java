package com.dissertation.analysisservice.detection.text;

import java.util.regex.Pattern;

public class TextPreprocessor {
    private static final Pattern URL = Pattern.compile("(https?://\\S+)|(www\\.\\S+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern MULTISPACE = Pattern.compile("\\s+");
    private static final Pattern REPEATED_PUNCT = Pattern.compile("([!?.,])\\1{2,}");

    public String normalize(String raw) {
        if (raw == null) return "";
        String s = raw.trim().toLowerCase();
        s = URL.matcher(s).replaceAll("<url>");
        s = REPEATED_PUNCT.matcher(s).replaceAll("$1$1");
        s = MULTISPACE.matcher(s).replaceAll(" ");
        return s;
    }
}
