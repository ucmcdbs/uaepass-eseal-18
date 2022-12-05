package com.ucmcswg.samples.eseal;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class EsealController {
    @Autowired
    EsealClient esealClient;

    @Autowired
    EsealAnnotator esealAnnotator;

    @PostMapping("/")
    @ResponseBody
    public ResponseEntity<Resource> seal(@RequestParam("document") MultipartFile file) {
        String fileName = file.getOriginalFilename();

        try {
            byte[] document = file.getBytes();

            document = esealAnnotator.annotate(document);

            EsealResponse response = esealClient.sealDocument(document);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Status-Code", response.getStatus());

            if (response.isSuccess) {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(fileName).build().toString());

                ByteArrayResource resource = new ByteArrayResource(response.getData());
                return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(resource.contentLength())
                    .body(resource);
            } else {
                return new ResponseEntity<Resource>(HttpStatus.BAD_REQUEST);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<Resource>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
