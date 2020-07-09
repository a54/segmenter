package receiver.services;

import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import receiver.adapters.interf.IAwsAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class VideoService {

    @Value("${server.local.storage.save}")
    private String localStorage;
    @Value("${server.url}")
    private String url;
    @Value("${server.ffmpeg}")
    private String ffmpeg;
    private final IAwsAdapter iAwsAdapter;


    public String convertAndUploadVideo(MultipartFile videoFile) throws IOException, InterruptedException {
        String name = RandomStringUtils.randomAlphanumeric(16);
        File targetFile = new File(localStorage + name + "." + videoFile.getOriginalFilename().split("\\.")[1]);
        java.nio.file.Files.copy(
                videoFile.getInputStream(),
                targetFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING);

        String livestream = targetFile.getAbsolutePath();
        String cmd = ffmpeg + " -i " + livestream + " -codec: copy -start_number 0 -hls_time 10 -hls_list_size 9 -f hls -method PUT " + url +"segmenter/api/v1/receiver/media/video/" + name + ".m3u8";
        System.out.println(cmd);
        Process p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        boolean delete = targetFile.delete();
        return name;
    }

    public S3Object downloadVideo (String name) {
        S3Object s3Object = iAwsAdapter.fetchObject(name);

        return s3Object;
    }

}
