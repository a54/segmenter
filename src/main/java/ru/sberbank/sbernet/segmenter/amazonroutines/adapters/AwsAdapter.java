package ru.sberbank.sbernet.segmenter.amazonroutines.adapters;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.sberbank.sbernet.segmenter.amazonroutines.adapters.interf.IAwsAdapter;

import java.io.InputStream;
import java.net.URL;

@Service
@Slf4j
@Profile("prod")
public class AwsAdapter implements IAwsAdapter {
    private final String bucketName;
    private final AmazonS3Client amazonS3Client;

    @Autowired
    public AwsAdapter(@Value("${aws.bucket-name}") String bucketName,
                      @Qualifier("cephClient") AmazonS3Client amazonS3Client) {
        this.bucketName = bucketName;
        this.amazonS3Client = amazonS3Client;
        this.checkIsBucketExist();
    }

    public URL storeObjectInS3(InputStream file, String fileName, String contentType, Long size) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(contentType);
        objectMetadata.setContentLength(size);

        try {
            this.amazonS3Client.putObject((new PutObjectRequest(this.bucketName, fileName, file, objectMetadata)).withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (AmazonClientException var7) {
            log.error(var7.getMessage());
            log.error(String.format("Ceph exception can't store object %s size:%s", fileName, size), var7.getMessage());
            throw new CephException(var7.getMessage());
        }

        return this.amazonS3Client.getUrl(this.bucketName, fileName);
    }

    public S3Object fetchObject(String awsFileName) {
        try {
            S3Object s3Object = this.amazonS3Client.getObject(new GetObjectRequest(this.bucketName, awsFileName));
            return s3Object;
        } catch (AmazonClientException var4) {
            log.error(String.format("Ceph exception can't read object %s", awsFileName), var4.getMessage());
            throw new CephException("Error while streaming File.");
        }
    }

    public void deleteObject(String key) {
        try {
            this.amazonS3Client.deleteObject(this.bucketName, key);
        } catch (AmazonServiceException var3) {
            log.error(var3.getErrorMessage());
        } catch (AmazonClientException var4) {
            log.error("Error while deleting File.");
        }

    }

    private void checkIsBucketExist() {
        if (this.amazonS3Client.doesBucketExist(this.bucketName)) {
            log.info("Bucket: " + this.bucketName + " already exists.");
        } else {
            try {
                this.amazonS3Client.createBucket(this.bucketName);
                log.info("Bucket: " + this.bucketName + " created.");
            } catch (AmazonS3Exception var2) {
                log.error(var2.getErrorMessage());
            }
        }

    }
}
