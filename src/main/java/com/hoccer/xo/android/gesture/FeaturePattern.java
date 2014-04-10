package com.hoccer.xo.android.gesture;


public class FeaturePattern {

    private final String mPattern;

    public FeaturePattern(String pPattern) {

        mPattern = pPattern.toLowerCase();
    }

    /**
     * Checks if the pattern can be matched to the provided query. For now the query can only
     * contain asterixes.
     * 
     * @param pMatchQuery
     *            a query with asterixes (e.g. "<up>*<flat><*down>")
     * @return true if the provided query can be matched to the feature pattern
     */
    public boolean matches(String pMatchQuery) {

        pMatchQuery = pMatchQuery.toLowerCase();

        pMatchQuery = pMatchQuery.replace("*", ".*");
        pMatchQuery = pMatchQuery.replace("<.*", "<\\w*");
        pMatchQuery = pMatchQuery.replace(".*>", "\\w*>");

        return mPattern.matches(pMatchQuery);
    }

    @Override
    public String toString() {
        return mPattern;
    }

    public boolean contains(String pSubstring) {
        return mPattern.contains(pSubstring);
    }

    public boolean startsWith(String pSubstring) {
        return mPattern.startsWith(pSubstring);
    }

    public boolean endsWith(String pSubstring) {
        return mPattern.endsWith(pSubstring);
    }

    @Override
    public boolean equals(Object pPattern) {

        if (pPattern instanceof String) {
            return mPattern.equals(pPattern);
        }

        if (pPattern instanceof FeaturePattern) {
            return mPattern.equals(pPattern.toString());
        }

        return false;
    }

    /**
     * @return number of occurrences of given substring
     */
    public int count(String pSubstring) {
        return (" " + mPattern + " ").split(pSubstring).length - 1;
    }
}
