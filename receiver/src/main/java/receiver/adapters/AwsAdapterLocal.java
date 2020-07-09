package receiver.adapters;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import receiver.adapters.interf.IAwsAdapter;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("test")
public class AwsAdapterLocal implements IAwsAdapter {
    @Value("${server.url}")
    private String LOCAL_URL;
    @Value("${server.local.storage.save}")
    private String STORAGE_PATH;

    public URL storeObjectInS3(InputStream file, String fileName, String contentType, Long size) {
        try {
            String[] split = fileName.split("/");
            Files.copy(file, Paths.get(this.STORAGE_PATH + split[split.length - 1]));
            return new URL(this.LOCAL_URL + fileName);
        } catch (IOException var6) {
            log.error("Failed save file to locale storage with name {}", fileName);
            return null;
        }
    }

    public S3Object fetchObject(String awsFileName) {
        try {
            String[] split = awsFileName.split("/");
            InputStream inputStream = new FileInputStream(STORAGE_PATH +"/" + split[split.length - 1]);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("");
            objectMetadata.setContentLength((long)inputStream.available());
            S3Object s3Object = new S3Object();
            s3Object.setObjectMetadata(objectMetadata);
            s3Object.setObjectContent(inputStream);
            return s3Object;
        } catch (IOException var6) {
            log.error("file not found in local storage with name {}", awsFileName);
            throw new CephException("Error while streaming File.");
        }
    }

    public void deleteObject(String key) {
    }

    @PostConstruct
    private void checkIsBucketExist(){
        try{
            Files.createDirectory(new File(STORAGE_PATH).toPath());
        }catch (Exception e){}

    }
}
