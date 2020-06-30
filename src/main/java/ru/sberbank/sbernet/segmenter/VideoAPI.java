package ru.sberbank.sbernet.segmenter;

import feign.Body;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.sbernet.segmenter.amazonroutines.adapters.interf.IAwsAdapter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;


@RestController
@RequestMapping("/api/v1/media/video")
@Slf4j
@RequiredArgsConstructor
public class VideoAPI  {

    private final IAwsAdapter aws;


    @PostMapping
    public ResponseEntity<String> uploadVideo(@RequestParam("video") MultipartFile videoFile) throws IOException, InterruptedException {


        File targetFile = new File("/tmp/"+videoFile.getOriginalFilename());

        java.nio.file.Files.copy(
                videoFile.getInputStream(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);



        String livestream = targetFile.getAbsolutePath();


        String cmd="ffmpeg -re -i " +livestream +" -f hls -method PUT http://localhost:8080/sbernet/api/v1/media/video/"+videoFile.getOriginalFilename()+ ".m3u8 -hls_list_size 0";

        Process p = Runtime.getRuntime().exec(cmd);

        p.waitFor();

        targetFile.delete();

        return p.exitValue()==0?ResponseEntity.ok("s"):ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @PutMapping("/{name}")
    public  ResponseEntity uploadChunk(@PathVariable("name") String name, @RequestHeader(value = "Content-Type", defaultValue = "video/mp2t") String  contentType, @RequestBody byte[] chunk) throws IOException {

        aws.storeObjectInS3(new ByteArrayInputStream(chunk), "video/" + name, contentType, (long) chunk.length);

        log.info("File {} with contentType {} saved", name, contentType);
        return ResponseEntity.ok("ok");
    }



}