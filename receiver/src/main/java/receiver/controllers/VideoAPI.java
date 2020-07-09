package receiver.controllers;

import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import receiver.adapters.interf.IAwsAdapter;
import receiver.services.VideoService;

import java.io.ByteArrayInputStream;
import java.io.IOException;


@RestController
@RequestMapping("/api/v1/receiver/media/video")
@Slf4j
@RequiredArgsConstructor
public class VideoAPI  {

    private final IAwsAdapter aws;
    private final VideoService videoService;


    @PostMapping
    public ResponseEntity<String> uploadVideo(@RequestParam("video") MultipartFile videoFile) throws IOException, InterruptedException {
        String name = videoService.convertAndUploadVideo(videoFile);
        return ResponseEntity.ok(name);
    }

    @GetMapping
    public ResponseEntity<Resource> downloadVideoChunk(@RequestParam("name") String name) throws IOException {

        S3Object s3Object = videoService.downloadVideo(name);

        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent().getDelegateStream());

        return ResponseEntity.ok()
                .contentLength(s3Object.getObjectContent().available())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @PutMapping("/{name}")
    public  ResponseEntity uploadChunk(@PathVariable("name") String name, @RequestHeader(value = "Content-Type", defaultValue = "video/mp2t") String  contentType, @RequestBody byte[] chunk) {

        aws.storeObjectInS3(new ByteArrayInputStream(chunk), "video/" + name, contentType, (long) chunk.length);

        log.info("File {} with contentType {} saved", name, contentType);
        return ResponseEntity.ok("ok");
    }



}