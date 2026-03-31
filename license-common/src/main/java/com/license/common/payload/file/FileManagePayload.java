package com.license.common.payload.file;

import com.license.common.payload.BasePayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileManagePayload implements BasePayload {

    private static final long serialVersionUID = 1L;

    private String fileType;
    private String fileName;
    private String filePath;
    private String content;
    private String encoding;
    private Long maxSize;
    private Boolean backup;
    private String backupPath;

    @Override
    public String getSoftwareType() {
        return "*";
    }
}
