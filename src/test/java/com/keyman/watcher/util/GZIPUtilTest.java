package com.keyman.watcher.util;

import org.junit.Assert;
import org.junit.Test;

public class GZIPUtilTest {
    @Test
    public void gzip() {
        String str = "I am Kaymon. I am Kaymon. I am Kaymon. I am Kaymon.I am Kaymon. I am Kaymon. I am Kaymon. I am Kaymon.";
        byte[] compress = GZIPUtil.compress(str);
        Assert.assertEquals(str, GZIPUtil.decompress(compress));
    }
}