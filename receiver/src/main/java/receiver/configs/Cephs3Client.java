package receiver.configs;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.S3ClientOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Cephs3Client {
    private static final Logger log = LoggerFactory.getLogger(Cephs3Client.class);
    @Value("${ceph.keyId}")
    private String awsKeyId;
    @Value("${ceph.accessKey}")
    private String accessKey;
    @Value("${ceph.endpoint}")
    private String endpoint;


    @Bean({"cephClient"})
    public AmazonS3Client cephs3Client() {
        System.setProperty("com.amazonaws.sdk.disableCertChecking", "true");
        AWSCredentials credentials = new BasicAWSCredentials(awsKeyId, accessKey);
        ClientConfiguration clientConfiguration = new ClientConfiguration();
        clientConfiguration.setSignerOverride("S3SignerType");
        AmazonS3Client cephClient = new AmazonS3Client(credentials, clientConfiguration);
        cephClient.setEndpoint(endpoint);
        S3ClientOptions clientOptions = S3ClientOptions.builder().setPathStyleAccess(true).disableChunkedEncoding().build();
        cephClient.setS3ClientOptions(clientOptions);
        return cephClient;
    }

    public Cephs3Client() {
    }
}
