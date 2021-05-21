package com.keyman.watcher.file;

import com.keyman.watcher.global.GlobalStore;
import com.keyman.watcher.util.GZIPUtil;
import com.keyman.watcher.util.JsonUtil;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class FileTemplateTest {

    @Test
    public void test(){
        String path = "C:\\api\\";
        path = path.substring(0, path.length() - 1);
        System.out.println(path);
    }

    @Test
    public void hiearchy() {
        FilePathHierarchyParser parser = new FilePathHierarchyParser("C:\\Users\\khong\\Desktop\\api\\TS\\Newfolder\\Newfolder\\new");
        Map<String, String> map = parser.buildHierarchy();
        GlobalStore.putGlobalResult(map);
        System.out.println(handleResult(""));
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
            HashMap<String, String> ipValue = new HashMap<>();
            valueMap.forEach((ip, val) -> {
                if (val instanceof byte[]) {
                    String decompress = GZIPUtil.decompress((byte[]) val);
                    ipValue.put(ip, decompress);
                } else {
                    ipValue.put(ip, val.toString());
                }
            });
            return JsonUtil.writeToString(ipValue);
        }
    }

}