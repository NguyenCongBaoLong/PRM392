package com.prm392;

// CloudinaryManager.java
import com.cloudinary.Cloudinary;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryManager {
    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if (cloudinary == null) {
            Map config = new HashMap();
            config.put("cloud_name", "drlvtdthc");
            config.put("api_key", "993317962488434");
            config.put("api_secret", "5i55_iGIWBSvFYImcw4mrwoyk-M");
            cloudinary = new Cloudinary(config);
        }
        return cloudinary;
    }
}
