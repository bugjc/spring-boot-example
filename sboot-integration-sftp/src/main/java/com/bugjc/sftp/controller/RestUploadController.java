package com.bugjc.sftp.controller;

import com.bugjc.sftp.config.SftpAdapter;
import com.bugjc.sftp.service.SftpService;
import com.bugjc.sftp.util.FileDownloadVO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
public class RestUploadController {

    private final Logger logger = LoggerFactory.getLogger(RestUploadController.class);

    @Autowired
    private SftpService sftpService;
    @Autowired
    private SftpAdapter sftpAdapter;

    /**
     * 单文件上传
     * @param uploadFile
     * @return
     */
    @PostMapping("/api/upload/file")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile uploadFile) {

        logger.info("Single file upload!");

        if (uploadFile.isEmpty()) {
            return new ResponseEntity<>("please select a file!", HttpStatus.OK);
        }

        try {
            sftpService.uploadFile(uploadFile);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Successfully uploaded - " +
                uploadFile.getOriginalFilename(), new HttpHeaders(), HttpStatus.OK);

    }

    /**
     * 批量上传文件
     * @param uploadFiles
     * @return
     */
    @PostMapping("/api/upload/files")
    public ResponseEntity<?> uploadFileMulti(@RequestParam("files") MultipartFile[] uploadFiles) {

        logger.info("multi files upload!");

        String uploadedFileName = Arrays.stream(uploadFiles).map(MultipartFile::getOriginalFilename)
                .filter(x -> !StringUtils.isEmpty(x)).collect(Collectors.joining(" , "));

        if (StringUtils.isEmpty(uploadedFileName)) {
            return new ResponseEntity<>("please select a file!", HttpStatus.OK);
        }
        try {
            sftpService.uploadFiles(Arrays.asList(uploadFiles));

        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>("Successfully uploaded - " + uploadedFileName, HttpStatus.OK);
    }

    /**
     * 下载文件
     * @param vo
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/download")
    public ResponseEntity<byte[]> testDownload(@RequestBody FileDownloadVO vo) throws IOException {
        File file = sftpService.downloadFile( sftpAdapter.remoteDirectory+File.separator+vo.getFileName(), "D:/temp/" + vo.getFileName());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDispositionFormData("attachment", vo.getFileName());
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        return new ResponseEntity<>(FileUtils.readFileToByteArray(file), headers, HttpStatus.CREATED);
    }

}
