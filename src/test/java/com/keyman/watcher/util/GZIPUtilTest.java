package com.keyman.watcher.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.keyman.watcher.global.GlobalStore;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class GZIPUtilTest {
    @Test
    public void gzip() {
        String str = "I am Kaymon. I am Kaymon. I am Kaymon. I am Kaymon.I am Kaymon. I am Kaymon. I am Kaymon. I am Kaymon.";
        byte[] compress = GZIPUtil.compress(str);
        Assert.assertEquals(str, GZIPUtil.decompress(compress));
    }


    @Test
    public void gzip2() {
        HashMap<String, String> map = new HashMap<>();
        map.put("1", "123456789");
        byte[] compress = GZIPUtil.compress(JsonUtil.writeToByte(map));
        byte[] bytes = GZIPUtil.decompressToByte(compress);
        Map<String, Object> result = JsonUtil.fromJsonToMap(bytes);
        System.out.println(result);
    }

    @Test
    public void gzip3() {
        byte[] bytes = {1, 2, 3};
        byte[] bytes2 = {1, 2, 3};
        System.out.println(Arrays.equals(bytes, bytes2));
    }

    private String handleResult(String key) {
        Map<String, Map<String, Object>> input = GlobalStore.getGlobalResult();
        Map<String, Object> valueMap = input.get(key);
        if (valueMap.size() <= 1) {
            Object oneValue = valueMap.values().iterator().next();
            String str;
            if (oneValue instanceof byte[]) {
                str = GZIPUtil.decompress((byte[]) oneValue);
            } else {
                str = oneValue.toString();
            }
            return str;
        }
        else {
            HashMap<String, Object> ipValue = new HashMap<>();
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                String content = "";
                String ip = entry.getKey();
                Object val = entry.getValue();
                if (val instanceof byte[]) {
                    content = GZIPUtil.decompress((byte[]) val);
                } else {
                    content = val.toString();
                }
                Map<String, Object> result = JsonUtil.fromJsonToMap(content);
                ipValue.put(ip, result);
            }
            return JsonUtil.writeToString(ipValue);
        }
    }
}