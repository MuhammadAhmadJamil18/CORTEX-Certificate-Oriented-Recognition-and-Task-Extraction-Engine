package org.qualitydxb.common.Enums;

public enum DocumentSource {

    LINK(1),
    UPLOAD(2);

    private final Integer source;

    DocumentSource(Integer source) {
        this.source = source;
    }

    public Integer getSource() {
        return source;
    }
}
