package de.df.jauswertung.gui.plugins.upload;

public class UploadAttachmentDto {
    private final byte[] data;

    public UploadAttachmentDto(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }
}
