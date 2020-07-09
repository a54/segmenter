package receiver.adapters.interf;

import com.amazonaws.services.s3.model.S3Object;

import java.io.InputStream;
import java.net.URL;

public interface IAwsAdapter {
    URL storeObjectInS3(InputStream var1, String var2, String var3, Long var4);

    S3Object fetchObject(String var1);

    void deleteObject(String var1);
}